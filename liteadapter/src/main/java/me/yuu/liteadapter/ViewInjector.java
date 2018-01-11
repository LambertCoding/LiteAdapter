package me.yuu.liteadapter;

/**
 * @author yu
 * @date 2018/1/11
 */
public interface ViewInjector<D> {

    /**
     * 绑定item的数据
     *
     * @param holder   ViewHolder
     * @param item     实体对象
     * @param position 角标
     */
    void bindData(ViewHolder holder, D item, int position);
}
