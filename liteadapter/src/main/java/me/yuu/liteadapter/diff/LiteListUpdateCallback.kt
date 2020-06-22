package me.yuu.liteadapter.diff

import androidx.recyclerview.widget.ListUpdateCallback
import me.yuu.liteadapter.core.AbsAdapter

/**
 * 由于可能有header和footer，所以DiffResult需要修正角标
 */
class LiteListUpdateCallback(
        /**
         * Creates an AdapterListUpdateCallback that will dispatch update events to the given adapter.
         *
         * @param adapter The Adapter to send updates to.
         */
        private val mAdapter: AbsAdapter<*>
) : ListUpdateCallback {

    override fun onInserted(position: Int, count: Int) {
        mAdapter.notifyItemRangeInserted(mAdapter.adjustNotifyIndex(position), count)
    }

    override fun onRemoved(position: Int, count: Int) {
        mAdapter.notifyItemRangeRemoved(mAdapter.adjustNotifyIndex(position), count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        mAdapter.notifyItemMoved(
                mAdapter.adjustNotifyIndex(fromPosition),
                mAdapter.adjustNotifyIndex(toPosition)
        )
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        mAdapter.notifyItemRangeChanged(mAdapter.adjustNotifyIndex(position), count, payload)
    }

}