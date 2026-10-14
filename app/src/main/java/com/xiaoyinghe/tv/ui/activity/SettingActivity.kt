package com.xiaoyinghe.tv.ui.activity

import android.content.Intent
import com.xiaoyinghe.tv.base.BaseVbActivity
import com.xiaoyinghe.tv.databinding.ActivitySettingBinding
import com.xiaoyinghe.tv.ui.settings.SettingsController

/** Compatibility host for settings links; My uses the same controller directly. */
class SettingActivity : BaseVbActivity<ActivitySettingBinding>() {
    private lateinit var settings: SettingsController
    override fun init() {
        mBinding.titleBar.leftView.setOnClickListener { onBackPressed() }
        settings = SettingsController(this, mBinding.settings) { intent, code -> startActivityForResult(intent, code) }
        settings.init()
    }
    override fun onResume() { super.onResume(); settings.refresh() }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        settings.onActivityResult(requestCode, resultCode, data)
    }
    override fun onDestroy() { settings.dispose(); super.onDestroy() }
}
