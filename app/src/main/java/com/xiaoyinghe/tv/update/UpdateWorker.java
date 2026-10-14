package com.xiaoyinghe.tv.update;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.xiaoyinghe.tv.BuildConfig;
import com.xiaoyinghe.tv.util.UpdateManifest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/** Persistent background work, independent of the About view or current Activity. */
public class UpdateWorker extends Worker {
    /** 仓库地址来自 gradle.properties（project.owner / project.repo），改一处即可。 */
    private static final String REPOSITORY = BuildConfig.PROJECT_OWNER + "/" + BuildConfig.PROJECT_REPO;
    private static final String MANIFEST_URL = "https://raw.githubusercontent.com/" + REPOSITORY + "/main/update.json";
    // Use the same mirror-first order for every update check and APK download.
    private static final String[] ROUTES = {"https://gh-proxy.com/", ""};
    private final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).callTimeout(2, TimeUnit.MINUTES).build();
    // A 40 MB APK over a slow international link takes tens of minutes, so the total-call timeout
    // that protects the small manifest check must not abort downloads. A stalled socket is still
    // detected by readTimeout.
    private final OkHttpClient downloadClient = client.newBuilder().callTimeout(0, TimeUnit.MILLISECONDS).build();
    private volatile Call running;
    private final UpdateStore store;

    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters parameters) {
        super(context, parameters);
        store = new UpdateStore(context);
    }

    @NonNull @Override public Result doWork() {
        try {
            return getInputData().getBoolean("download", false) ? download() : check();
        } catch (Exception error) {
            android.util.Log.w("AppUpdate", "Background update failed", error);
            if (!isStopped()) store.state(UpdateStore.ERROR, error instanceof IllegalStateException
                    ? error.getMessage() : "更新暂不可用，请检查网络后重试", 0);
            // Slow links need several attempts; every retry resumes where the last one stopped.
            return !isStopped() && !(error instanceof IllegalStateException) && getRunAttemptCount() < 5 ? Result.retry() : Result.failure();
        } finally { running = null; }
    }

    private Result check() throws Exception {
        if (UpdateStore.DOWNLOADING.equals(store.state()) || UpdateStore.VERIFYING.equals(store.state())) return Result.success();
        boolean ready = store.hasReadyUpdate();
        if (!ready) store.state(UpdateStore.CHECKING, "正在检查新版本…", 0);
        Exception last = null;
        for (String route : ROUTES) {
            if (isStopped()) return Result.failure();
            try (Response response = request(route + MANIFEST_URL + "?t=" + System.currentTimeMillis())) {
                if (!response.isSuccessful() || response.body() == null) throw new IOException("无法连接更新服务");
                String json = response.body().string();
                if (UpdateManifest.legacyIsNotNewer(json, BuildConfig.VERSION_NAME)) {
                    store.prefs.edit().putLong("last_check", System.currentTimeMillis()).apply();
                    if (!ready) store.state(UpdateStore.CURRENT, "当前已是最新版本", 0);
                    return Result.success();
                }
                UpdateManifest manifest = new UpdateManifest(json, getApplicationContext().getPackageName());
                if (isStopped()) return Result.failure();
                store.prefs.edit().putLong("last_check", System.currentTimeMillis()).apply();
                if (!manifest.isNewer(BuildConfig.VERSION_CODE)) {
                    if (!ready) store.state(UpdateStore.CURRENT, "当前已是最新版本", 0);
                    return Result.success();
                }
                UpdateManifest previous = store.manifest();
                if (ready && previous != null && previous.versionCode >= manifest.versionCode) return Result.success();
                store.prefs.edit().putString("manifest", json).apply();
                store.state(UpdateStore.AVAILABLE, store.autoDownload() ? "发现 v" + manifest.version + "，等待 Wi-Fi 后台下载"
                        : "发现 v" + manifest.version + "，可手动下载", 0);
                if (store.autoDownload()) UpdateCoordinator.download(getApplicationContext(), false);
                return Result.success();
            } catch (Exception error) { last = error; }
        }
        if (ready) return Result.success(); // Keep an already verified download available during network outages.
        throw last == null ? new IOException("无法获取更新信息") : last;
    }

    private Result download() throws Exception {
        boolean manual = getInputData().getBoolean("manual", false);
        if (!manual && !store.autoDownload()) return Result.success();
        if (store.hasReadyUpdate()) return Result.success();
        UpdateManifest manifest = store.manifest();
        if (manifest == null || !manifest.isNewer(BuildConfig.VERSION_CODE)) return Result.success();
        File apk = store.apk(manifest);
        File directory = apk.getParentFile();
        if (!directory.isDirectory() && !directory.mkdirs()) throw new IOException("无法创建下载目录");
        if (manifest.size <= 0) throw new IOException("更新包大小异常");
        // One stable partial file per version: an interrupted run (route switch, stopped worker,
        // WorkManager retry, or a manual download replacing the Wi-Fi task) resumes from the bytes
        // already on disk instead of starting over.
        File part = new File(directory, "xiaoyinghe-v" + manifest.version + ".part.apk");
        if (part.isFile() && part.length() > manifest.size) part.delete();
        File[] stale = directory.listFiles((dir, name) -> name.startsWith("xiaoyinghe-v")
                && name.endsWith(".part.apk") && !name.equals(part.getName()));
        if (stale != null) for (File file : stale) file.delete();
        Exception last = null;
        for (String route : ROUTES) {
            if (isStopped()) return Result.failure();
            long resumeFrom = part.isFile() ? part.length() : 0;
            if (resumeFrom < manifest.size) {
                int startPercent = (int) (resumeFrom * 100 / manifest.size);
                store.state(UpdateStore.DOWNLOADING, resumeFrom > 0
                        ? "正在续传 v" + manifest.version + " · " + startPercent + "%"
                        : "正在后台下载 v" + manifest.version, startPercent);
                try {
                    try (Response response = request(route + manifest.apkUrl, resumeFrom)) {
                        if (!response.isSuccessful() || response.body() == null) throw new IOException("无法下载更新包");
                        // A server that ignores Range answers 200; only 206 may be appended to.
                        boolean append = resumeFrom > 0 && response.code() == 206;
                        long downloaded = append ? resumeFrom : 0, lastProgress = 0;
                        try (InputStream input = response.body().byteStream();
                             FileOutputStream output = new FileOutputStream(part, append)) {
                            byte[] buffer = new byte[32768];
                            int count;
                            while ((count = input.read(buffer)) != -1) {
                                if (isStopped()) return Result.failure();
                                downloaded += count;
                                if (downloaded > manifest.size) throw new IOException("更新包大小异常");
                                output.write(buffer, 0, count);
                                long now = System.currentTimeMillis();
                                if (now - lastProgress >= 250) {
                                    int percent = (int) (downloaded * 100 / manifest.size);
                                    store.state(UpdateStore.DOWNLOADING, "正在后台下载 v" + manifest.version + " · " + percent + "%", percent);
                                    lastProgress = now;
                                }
                            }
                        }
                    }
                    if (part.length() != manifest.size) throw new IOException("更新包不完整");
                } catch (Exception error) {
                    // Keep the partial file so the next route or retry resumes from it.
                    last = error;
                    continue;
                }
            }
            if (isStopped()) return Result.failure();
            store.state(UpdateStore.VERIFYING, "下载完成，正在校验更新包…", 100);
            try {
                manifest.verifyDownload(part);
                UpdateInstaller.verifyArchive(getApplicationContext(), part, manifest);
                if (isStopped()) return Result.failure();
                if (apk.exists() && !apk.delete()) throw new IOException("无法替换更新包");
                if (!part.renameTo(apk)) throw new IOException("无法保存更新包");
                store.state(UpdateStore.READY, "v" + manifest.version + " 已准备好，可安装更新", 100);
                UpdateCoordinator.notifyReady(getApplicationContext(), manifest.version);
                return Result.success();
            } catch (Exception error) {
                // Wrong bytes (checksum mismatch, or not an APK for this app): discard and retry.
                last = error;
                part.delete();
            }
        }
        throw last == null ? new IOException("下载失败") : last;
    }

    protected Response request(String url) throws IOException {
        running = client.newCall(build(url, 0));
        if (isStopped()) throw new IOException("更新任务已停止");
        return running.execute();
    }

    /** Same as {@link #request(String)} but may resume from a byte offset and has no total-call timeout. */
    protected Response request(String url, long fromByte) throws IOException {
        running = downloadClient.newCall(build(url, fromByte));
        if (isStopped()) throw new IOException("更新任务已停止");
        return running.execute();
    }

    private Request build(String url, long fromByte) {
        Request.Builder builder = new Request.Builder().url(url).header("User-Agent", "xiaoyinghe")
                .header("Cache-Control", "no-cache");
        if (fromByte > 0) builder.header("Range", "bytes=" + fromByte + "-");
        return builder.build();
    }

    @Override public void onStopped() {
        Call call = running;
        if (call != null) call.cancel();
        super.onStopped();
    }
}
