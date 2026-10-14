package com.xiaoyinghe.tv.ui.dialog;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoyinghe.tv.R;
import com.xiaoyinghe.tv.bean.VodInfo;
import com.xiaoyinghe.tv.ui.activity.DetailActivity;
import com.xiaoyinghe.tv.ui.adapter.SeriesAdapter;
import com.xiaoyinghe.tv.ui.widget.GridSpacingItemDecoration;
import com.xiaoyinghe.tv.util.Utils;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.interfaces.OnSelectListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 全集弹窗,不像全屏右侧弹窗一样共用activity的adapter,adapter横向和网格布局逻辑不同,同屏显示切换会有视觉差
 */
public class AllVodSeriesBottomDialog extends BottomPopupView {

    List<VodInfo.VodSeries> mList;
    private final OnSelectListener mSelectListener;

    public AllVodSeriesBottomDialog(@NonNull @NotNull Context context, List<VodInfo.VodSeries> list, OnSelectListener selectListener) {
        super(context);
        mList = list;
        mSelectListener = selectListener;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_all_series;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        RecyclerView rv = findViewById(R.id.rv);

        rv.setLayoutManager(new GridLayoutManager(getContext(), Utils.getSeriesSpanCount(mList)));
        rv.addItemDecoration(new GridSpacingItemDecoration(Utils.getSeriesSpanCount(mList), 20, true));

        SeriesAdapter seriesAdapter = new SeriesAdapter(true);
        seriesAdapter.setNewData(mList);
        rv.setAdapter(seriesAdapter);

        rv.postDelayed(() -> {//xpopup重写maxHeight后布局完成未滑动完毕导致定位异常,加延时可正常滑动
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i).selected){
                    rv.smoothScrollToPosition(i);
                }
            }
        },500);

        seriesAdapter.setOnItemClickListener((adapter, view, position) -> {
            for (int j = 0; j < seriesAdapter.getData().size(); j++) {
                seriesAdapter.getData().get(j).selected = false;
                seriesAdapter.notifyItemChanged(j);
            }
            seriesAdapter.getData().get(position).selected = true;
            seriesAdapter.notifyItemChanged(position);
            mSelectListener.onSelect(position,"");
        });

    }
}