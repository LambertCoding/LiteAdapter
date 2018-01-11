package me.yuu.liteadapter;

import android.support.annotation.LayoutRes;

/**
 * @author yu
 * @date 2018/1/11
 */
public abstract class ViewInjector<D> {

    private int layoutId;

    public ViewInjector(@LayoutRes int layoutId) {
        this.layoutId = layoutId;
    }

    int getLayoutId() {
        return layoutId;
    }

    /**
     * 绑定item的数据
     *
     * @param holder   ViewHolder
     * @param item     实体对象
     * @param position 角标
     */
    public abstract void bindData(ViewHolder holder, D item, int position);
}
