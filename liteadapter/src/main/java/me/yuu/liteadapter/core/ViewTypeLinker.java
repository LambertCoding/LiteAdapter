package me.yuu.liteadapter.core;

/**
 * @author yu
 * @date 2018/1/11
 */
public interface ViewTypeLinker {
    /**
     * 通过实体和角标返回item对应的viewType
     *
     * @param item     实体
     * @param position 角标
     * @return viewType
     */
    int viewType(Object item, int position);
}
