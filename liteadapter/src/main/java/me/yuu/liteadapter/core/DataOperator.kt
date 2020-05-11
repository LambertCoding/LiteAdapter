package me.yuu.liteadapter.core

/**
 * @author yu.
 * @date 2018/1/12
 */
interface DataOperator<D> {
    fun getItem(position: Int): D

    fun addData(item: D)
    fun addData(position: Int, item: D)
    fun addAll(items: List<D>)
    fun addAll(position: Int, items: List<D>)
    fun remove(position: Int)
    fun modify(position: Int, newData: D)
    fun modify(position: Int, action: (D) -> Unit)

    /**
     * 设置新的数据集合，自动应用DiffUtil进行差量更新
     */
    fun updateData(items: List<D>)

    fun clear()
}