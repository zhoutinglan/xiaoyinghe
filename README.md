<div align="center">

# 小影盒

**一个开源的 Android 影视播放器**

[![Build and Release](https://github.com/zhoutinglan/xiaoyinghe/actions/workflows/build.yml/badge.svg)](https://github.com/zhoutinglan/xiaoyinghe/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/zhoutinglan/xiaoyinghe)](https://github.com/zhoutinglan/xiaoyinghe/releases)
[![License](https://img.shields.io/badge/license-AGPL--3.0-blue)](LICENSE)

</div>

| 项目 | 值 |
| --- | --- |
| 应用名 | 小影盒 |
| 包名 | `com.xiaoyinghe.tv` |
| 当前版本 | 1.0.0 |
| 系统要求 | Android 7.0 及以上（API 24+），arm64-v8a |
| 开源协议 | AGPL-3.0 |

> 本项目基于 [TVboxOSC](https://github.com/kukuqi666/TVboxOSC) 源码二次开发。与原版使用**不同的包名**，两者可以同时安装、互不影响。

## 📲 下载

- 小影盒：[下载 v1.0.3](https://gh-proxy.com/https://github.com/zhoutinglan/xiaoyinghe/releases/download/v1.0.3/xiaoyinghe-v1.0.3.apk) · [所有版本](https://github.com/zhoutinglan/xiaoyinghe/releases)

## ✨ 功能说明

- **不内置任何订阅源**：首次启动不会自动写入任何第三方源，全部由用户自行导入（「我的 → 点播源 / 直播源」支持链接导入与本地文件导入）。
- 点播、直播、本地视频三大板块，底部导航直接切换。
- EPG 节目单、弹幕、字幕、倍速播放、投屏、远程控制。
- 历史记录与收藏在首页，设置集中在「我的」。
- 应用内检查更新：读取本仓库的 `update.json`，支持 Wi-Fi 后台下载，下载完成后由用户确认安装。

## 🚀 本地构建

**环境要求**：JDK 17 或更高、Android SDK（`platforms;android-34`、`build-tools;34.0.0`）。

```bash
# 1. 指定 Android SDK 路径
echo "sdk.dir=/path/to/Android/Sdk" > local.properties

# 2. 构建 Debug 并运行单元测试
./gradlew :app:assembleDebug :app:testDebugUnitTest

# 3. 安装到已连接的设备
./gradlew :app:installDebug
```

### 项目地址配置

应用内的「检查更新」和「关于页 - 项目地址」由 `gradle.properties` 统一控制，改这两行即可：

```properties
project.owner=zhoutinglan
project.repo=xiaoyinghe
```

### 发布签名

Release 构建需要签名密钥。在仓库根目录创建 `keystore.properties`（已在 `.gitignore` 中，**请勿提交**）：

```properties
storeFile=xiaoyinghe.jks
keyAlias=xiaoyinghe
keyPassword=<你的密码>
storePassword=<你的密码>
```

CI 环境则通过仓库 Secrets 提供：`XYH_KEYSTORE_BASE64`、`XYH_KEY_ALIAS`、`XYH_KEY_PASSWORD`、`XYH_STORE_PASSWORD`。

## 🔄 自动发布

流水线定义在 [`.github/workflows/build.yml`](.github/workflows/build.yml)：

| 触发条件 | 自动行为 |
| --- | --- |
| PR / 普通提交到 main | 构建 Debug、运行单元测试，产物保留 14 天 |
| main 上 `versionCode` 有变化 | 构建固定签名的 Release，创建 Tag 并发布 APK |
| 手动推送 `v*` Tag | 校验 Tag 与 `versionName` 一致后发布 |
| Release 发布完成 | 把真实 APK 的 `update.json`、README 下载链接和更新记录同步回 main |

发新版本只需要：改 `app/build.gradle` 里的 `versionCode` 和 `versionName` → 合并到 main → 等待自动发布完成。已公开发布的版本不会重复发布；发布失败时可在 GitHub Actions 里重新运行对应流程。

## 📄 开源协议

本项目基于 TVBox 开源项目二次开发，遵循 **GNU AGPL-3.0** 协议，详见 [LICENSE](LICENSE)。

- 二次开发、修改、自用都可以；
- 但**任何形式的分发**（发布 APK、上架应用商店、提供网络服务）都必须公开完整源代码并保留版权声明，**不可以闭源**。

**免责声明**：本应用仅提供播放器功能，不制作、不收集、不发布、不存储任何流媒体内容。应用内不内置任何订阅源，所有内容均由用户自行导入，使用风险由使用者自行承担。请勿用于任何商业用途。

## 𝟭. 更新记录

>* **小影盒 v1.0.3：** 修复「订阅失败」相关问题：**GitHub 源自动走加速镜像**（填 `raw.githubusercontent.com/...` 或 `github.com/.../raw/...` 会自动改成走 ghproxy.net，不再需要自己拼前缀）；订阅添加支持 `assets://` / `file://` 本地文件、忘记写 `http://` 会自动补全；**失败原因不再一律是「请检查地址或网络」**，会区分域名解析失败 / 连接超时 / 404 已删除 / 401 需登录 / 返回的不是配置；恢复两个内置本地订阅（不联网，离线可用）。

>* **小影盒 v1.0.2：** 更新检查和下载增加第二条加速线路（ghproxy.net）。实测国内网络下只有 gh-proxy.com 和 ghproxy.net 能连通，且两者会各自抖动，此前只配了一条，导致「检查更新」经常直接失败。

>* **小影盒 v1.0.1：** 修复慢速网络下更新包下不完的问题：应用内更新支持**断点续传**（切换下载线路、任务被中断或重试时都从已下载的进度继续，不再从 0 重来）；去掉下载请求 2 分钟的总超时限制（此前会在大文件传输中途强制中断）；失败重试次数由 1 次提高到 4 次。

>* **小影盒 v1.0.0：** 首个版本。基于 TVboxOSC 3.0.3 源码二次开发：应用名与包名独立、不内置任何订阅源、使用独立签名密钥、更新检查与自动发布接入本仓库。
