package me.yuu.liteadapter.diff

import androidx.recyclerview.widget.DiffUtil

class LiteDiffUtil(private val oldData: List<*>?,
                   private val newData: List<*>?,
                   private val diffCallback: Callback
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldData?.size ?: 0
    }

    override fun getNewListSize(): Int {
        return newData?.size ?: 0
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return diffCallback.areItemsTheSame(oldData!![oldItemPosition], newData!![newItemPosition])
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return diffCallback.areContentsTheSame(oldData!![oldItemPosition], newData!![newItemPosition])
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        // 当areItemsTheSame = true，areContentsTheSame = false时调用
        // 返回两个item内容差异的字段，在adapter的三个参数的onBindViewHolder方法中，可以实现差量更新
        return diffCallback.getChangePayload(oldItemPosition, newItemPosition)
    }

    interface Callback {
        fun areItemsTheSame(oldItem: Any?, newItem: Any?): Boolean
        fun areContentsTheSame(oldItem: Any?, newItem: Any?): Boolean
        fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any?
    }

}