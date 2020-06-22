package me.yuu.liteadapter.core

import androidx.annotation.IntRange
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import me.yuu.liteadapter.diff.LiteDiffUtil
import me.yuu.liteadapter.diff.LiteListUpdateCallback

typealias OnItemClickListener = (index: Int, item: Any) -> Unit
typealias OnItemLongClickListener = (index: Int, item: Any) -> Unit

/**
 * @author yu
 * @date 2018/1/14
 */
abstract class AbsAdapter<T>(
) : RecyclerView.Adapter<ViewHolder>(), DataOperator<T> {

    protected var diffCallback: LiteDiffUtil.Callback? = null

    @JvmField
    protected var mDataSet: MutableList<T> = ArrayList()

    open fun beforeUpdateData() {

    }

    /**
     * 当有Header view时，修改数据并进行定向刷新，需要修正角标
     *
     * @param index index
     * @return index + headerCount
     */
    open fun adjustNotifyIndex(index: Int): Int {
        return index
    }

    /**
     * 当有Header view时，在mDataSet获取数据需要修正角标，减去headerCount
     *
     * @param index index
     * @return index - headerCount
     */
    open fun adjustGetItemIndex(index: Int): Int {
        return index
    }

    open fun isEmpty(): Boolean = mDataSet.isNullOrEmpty()


    override fun getItem(@IntRange(from = 0) index: Int): T {
        return mDataSet[index]
    }

    override fun updateData(items: List<T>) {
        if (diffCallback != null && mDataSet.isNotEmpty()) {
            val diffResult = DiffUtil.calculateDiff(LiteDiffUtil(mDataSet, items, diffCallback!!))
            mDataSet = ArrayList(items)
            beforeUpdateData()
            diffResult.dispatchUpdatesTo(LiteListUpdateCallback(this))
        } else {
            mDataSet.clear()
            mDataSet.addAll(items)
            beforeUpdateData()
            notifyDataSetChanged()
        }
    }

    override fun addData(item: T) {
        val adjustNotifyPosition = adjustNotifyIndex(mDataSet.size)
        mDataSet.add(item)
        notifyItemInserted(adjustNotifyPosition)
    }

    override fun addData(@IntRange(from = 0) index: Int, item: T) {
        mDataSet.add(index, item)
        notifyItemInserted(adjustNotifyIndex(index))
    }

    override fun addAll(items: List<T>) {
        if (items.isNotEmpty()) {
            mDataSet.addAll(items)
            notifyItemRangeInserted(adjustNotifyIndex(mDataSet.size), items.size)
        }
    }

    override fun addAll(@IntRange(from = 0) index: Int, items: List<T>) {
        if (items.isNotEmpty()) {
            mDataSet.addAll(index, items)
            notifyItemRangeInserted(adjustNotifyIndex(index), items.size)
        }
    }

    override fun remove(@IntRange(from = 0) index: Int) {
        mDataSet.removeAt(index)
        if (mDataSet.isEmpty()) {
            notifyDataSetChanged()
        } else {
            notifyItemRemoved(adjustNotifyIndex(index))
        }
    }

    override fun clear() {
        mDataSet.clear()
        notifyDataSetChanged()
    }

    override fun modify(@IntRange(from = 0) index: Int, newData: T) {
        mDataSet[index] = newData
        notifyItemChanged(adjustNotifyIndex(index))
    }

    override fun modify(index: Int, action: (T) -> Unit) {
        action(mDataSet[index])
        notifyItemChanged(adjustNotifyIndex(index))
    }

}