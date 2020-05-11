package me.yuu.liteadapter.diff

class DefaultDiffCallback : LiteDiffUtil.Callback {
    override fun areItemsTheSame(oldItem: Any?, newItem: Any?): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Any?, newItem: Any?): Boolean {
        return true
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return null
    }
}