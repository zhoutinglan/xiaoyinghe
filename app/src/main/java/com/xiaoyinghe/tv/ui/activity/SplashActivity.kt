package com.xiaoyinghe.tv.ui.activity

import android.content.Intent
import android.os.Build
import android.os.SystemClock
import com.xiaoyinghe.tv.R
import com.xiaoyinghe.tv.base.App
import com.xiaoyinghe.tv.base.BaseVbActivity
import com.xiaoyinghe.tv.databinding.ActivitySplashBinding
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions

class SplashActivity : BaseVbActivity<ActivitySplashBinding>() {
    private var startedAt = 0L
    private var openingMain = false
    private val openMainAction = Runnable {
        if (!isFinishing && !isDestroyed) {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }
    }
    override fun shouldApplyWallpaper() = false
    override fun init() {
        App.getInstance().isNormalStart = true
        startedAt = SystemClock.uptimeMillis()
        mBinding.splashContent.apply {
            alpha = 0f
            scaleX = 0.92f
            scaleY = 0.92f
            animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(750).start()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !XXPermissions.isGranted(this, POST_NOTIFICATIONS_PERMISSION)
        ) {
            XXPermissions.with(this)
                .permission(POST_NOTIFICATIONS_PERMISSION)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: List<String>, all: Boolean) {
                        openMain()
                    }

                    override fun onDenied(permissions: List<String>, never: Boolean) {
                        openMain()
                    }
                })
        } else {
            openMain()
        }
    }

    private fun openMain() {
        if (openingMain) return
        openingMain = true
        mBinding.root.postDelayed(openMainAction, (1400 - (SystemClock.uptimeMillis() - startedAt)).coerceAtLeast(0))
    }
    override fun onDestroy() {
        mBinding.root.removeCallbacks(openMainAction)
        mBinding.splashContent.animate().cancel()
        super.onDestroy()
    }

    private companion object {
        const val POST_NOTIFICATIONS_PERMISSION = "android.permission.POST_NOTIFICATIONS"
    }
}
