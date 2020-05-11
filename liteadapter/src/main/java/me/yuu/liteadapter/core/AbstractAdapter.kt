package me.yuu.liteadapter.core

import androidx.annotation.IntRange
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import me.yuu.liteadapter.diff.LiteDiffUtil
import me.yuu.liteadapter.diff.LiteListUpdateCallback

/**
 * @author yu
 * @date 2018/1/14
 */
abstract class AbstractAdapter<T>(
        protected var diffCallback: LiteDiffUtil.Callback? = null
) : RecyclerView.Adapter<ViewHolder>(), DataOperator<T> {
    @JvmField
    protected var mDataSet: MutableList<T> = ArrayList()


    protected abstract fun beforeUpdateData()

    /**
     * 当有Header view时，修改数据并进行定向刷新，需要修正角标
     *
     * @param position position
     * @return position + headerCount
     */
    open fun adjustNotifyPosition(position: Int): Int {
        return position
    }

    /**
     * 当有Header view时，在mDataSet获取数据需要修正角标，减去headerCount
     *
     * @param position position
     * @return position - headerCount
     */
    open fun adjustGetItemPosition(position: Int): Int {
        return position
    }

    open fun isEmpty(): Boolean = mDataSet.isNullOrEmpty()


    override fun getItem(@IntRange(from = 0) position: Int): T {
        return mDataSet[position]
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
        val adjustNotifyPosition = adjustNotifyPosition(mDataSet.size)
        mDataSet.add(item)
        notifyItemInserted(adjustNotifyPosition)
    }

    override fun addData(@IntRange(from = 0) position: Int, item: T) {
        mDataSet.add(position, item)
        notifyItemInserted(adjustNotifyPosition(position))
    }

    override fun addAll(items: List<T>) {
        if (items.isNotEmpty()) {
            mDataSet.addAll(items)
            notifyItemRangeInserted(adjustNotifyPosition(mDataSet.size), items.size)
        }
    }

    override fun addAll(@IntRange(from = 0) position: Int, items: List<T>) {
        if (items.isNotEmpty()) {
            mDataSet.addAll(position, items)
            notifyItemRangeInserted(adjustNotifyPosition(position), items.size)
        }
    }

    override fun remove(@IntRange(from = 0) position: Int) {
        mDataSet.removeAt(position)
        if (mDataSet.isEmpty()) {
            notifyDataSetChanged()
        } else {
            notifyItemRemoved(adjustNotifyPosition(position))
        }
    }

    override fun clear() {
        mDataSet.clear()
        notifyDataSetChanged()
    }

    override fun modify(@IntRange(from = 0) position: Int, newData: T) {
        mDataSet[position] = newData
        notifyItemChanged(adjustNotifyPosition(position))
    }

    override fun modify(position: Int, action: (T) -> Unit) {
        action(mDataSet[position])
        notifyItemChanged(adjustNotifyPosition(position))
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, item: Any)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int, item: Any)
    }
}