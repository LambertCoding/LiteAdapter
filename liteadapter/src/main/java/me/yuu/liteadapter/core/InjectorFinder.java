package me.yuu.liteadapter.core;

/**
 * @author yu
 * @date 2018/1/11
 */
public interface InjectorFinder<T> {
    /**
     * 通过实体和角标返回item对应使用的ViewInjector角标，按register顺序
     *
     * @param item      实体
     * @param position  角标
     * @param itemCount itemCount
     * @return index 使用的ViewInjector角标，按register顺序
     */
    int index(T item, int position, int itemCount);
}
