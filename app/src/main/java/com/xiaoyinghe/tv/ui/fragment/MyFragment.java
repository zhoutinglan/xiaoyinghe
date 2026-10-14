package com.xiaoyinghe.tv.ui.fragment;

import android.content.Intent;
import com.xiaoyinghe.tv.base.BaseVbFragment;
import com.xiaoyinghe.tv.databinding.FragmentMyBinding;
import com.xiaoyinghe.tv.ui.activity.AboutActivity;
import com.xiaoyinghe.tv.ui.settings.SettingsController;

public class MyFragment extends BaseVbFragment<FragmentMyBinding> {
    private SettingsController settings;

    @Override protected void init() {
        settings = new SettingsController(
                (com.xiaoyinghe.tv.base.BaseActivity) requireActivity(), mBinding.settings,
                (intent, code) -> startActivityForResult(intent, code));
        settings.init();
        mBinding.llAbout.setOnClickListener(v -> AboutActivity.start(requireActivity()));
    }

    @Override public void onResume() {
        super.onResume();
        if (settings != null) settings.refresh();
    }

    @Override public void onDestroyView() {
        if (settings != null) { settings.dispose(); settings = null; }
        super.onDestroyView();
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (settings != null) settings.onActivityResult(requestCode, resultCode, data);
    }
}
