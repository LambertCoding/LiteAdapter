package me.yuu.liteadapter.core

import androidx.annotation.LayoutRes

/**
 * @author yu
 * @date 2018/1/11
 */
abstract class ViewInjector<D>(@param:LayoutRes val layoutId: Int) {

    /**
     * 绑定item的数据
     *
     * @param holder   ViewHolder
     * @param item     实体对象
     * @param position 角标
     */
    abstract fun bind(holder: ViewHolder, item: D, position: Int)

}