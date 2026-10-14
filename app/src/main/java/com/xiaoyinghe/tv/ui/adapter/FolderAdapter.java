package com.xiaoyinghe.tv.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xiaoyinghe.tv.R;
import com.xiaoyinghe.tv.api.ApiConfig;
import com.xiaoyinghe.tv.bean.VideoFolder;
import com.xiaoyinghe.tv.bean.VideoInfo;
import com.xiaoyinghe.tv.cache.VodCollect;
import com.xiaoyinghe.tv.picasso.RoundTransformation;
import com.xiaoyinghe.tv.util.DefaultConfig;
import com.xiaoyinghe.tv.util.MD5;
import com.squareup.picasso.Callback;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.jessyan.autosize.utils.AutoSizeUtils;

public class FolderAdapter extends BaseQuickAdapter<VideoFolder, BaseViewHolder> {
    public FolderAdapter() {
        super(R.layout.item_folder);
    }

    @Override
    protected void convert(BaseViewHolder helper, VideoFolder item) {
        List<VideoInfo> videoList = item.getVideoList();
        helper.setText(R.id.tv_name,item.getName());
        helper.setText(R.id.tv_count,videoList.size()+"个视频");

        Glide.with(mContext)
                .load(videoList.get(0).getPath()) // 第一个视频做封面
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.iv_video)
                .centerCrop()
                .into((ImageView) helper.getView(R.id.iv));
    }
}