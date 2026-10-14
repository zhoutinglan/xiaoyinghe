package com.xiaoyinghe.tv.ui.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xiaoyinghe.tv.R;
import com.xiaoyinghe.tv.api.ApiConfig;
import com.xiaoyinghe.tv.cache.VodCollect;
import com.xiaoyinghe.tv.picasso.RoundTransformation;
import com.xiaoyinghe.tv.util.DefaultConfig;
import com.xiaoyinghe.tv.util.MD5;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import me.jessyan.autosize.utils.AutoSizeUtils;

public class CollectAdapter extends BaseQuickAdapter<VodCollect, BaseViewHolder> {
    public CollectAdapter() {
        super(R.layout.item_grid, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, VodCollect item) {
        helper.setVisible(R.id.tvNote, false);
        TextView tvYear = helper.getView(R.id.tvYear);
        if (ApiConfig.get().getSource(item.sourceKey)!=null) {
            tvYear.setText(ApiConfig.get().getSource(item.sourceKey).getName());
            tvYear.setVisibility(View.VISIBLE);
        } else {
            tvYear.setVisibility(View.GONE);
        }
        helper.setText(R.id.tvName, item.name);
        ImageView ivThumb = helper.getView(R.id.ivThumb);
        //由于部分电视机使用glide报错
        if (!TextUtils.isEmpty(item.pic)) {
            Picasso.get()
                    .load(DefaultConfig.checkReplaceProxy(item.pic))
                    .placeholder(R.drawable.img_loading_placeholder)
                    .error(R.drawable.img_loading_placeholder)
                    .into(ivThumb);
        } else {
            ivThumb.setImageResource(R.drawable.img_loading_placeholder);
        }
    }
}