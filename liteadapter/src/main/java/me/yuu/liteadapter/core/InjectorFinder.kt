package me.yuu.liteadapter.core

/**
 * @author yu
 * @date 2018/1/11
 */
interface InjectorFinder<T> {
    /**
     * 通过实体和角标返回item对应使用的ViewInjector角标，按register顺序
     *
     * @param item      实体
     * @param position  角标
     * @param itemCount itemCount
     * @return index 使用的ViewInjector角标，按register顺序
     */
    fun index(item: T, position: Int, itemCount: Int): Int
}