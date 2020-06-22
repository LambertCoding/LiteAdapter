package me.yuu.liteadapter.core

/**
 * @author yu.
 * @date 2018/1/12
 */
interface DataOperator<D> {
    fun getItem(index: Int): D

    fun addData(item: D)
    fun addData(index: Int, item: D)
    fun addAll(items: List<D>)
    fun addAll(index: Int, items: List<D>)
    fun remove(index: Int)
    fun modify(index: Int, newData: D)
    fun modify(index: Int, action: (D) -> Unit)
    fun updateData(items: List<D>)

    fun clear()
}