package me.yuu.liteadapter.diff

import androidx.recyclerview.widget.ListUpdateCallback
import me.yuu.liteadapter.core.AbstractAdapter

/**
 * 由于可能有header和footer，所以DiffResult需要修正角标
 */
class LiteListUpdateCallback(
        /**
         * Creates an AdapterListUpdateCallback that will dispatch update events to the given adapter.
         *
         * @param adapter The Adapter to send updates to.
         */
        private val mAdapter: AbstractAdapter<*>
) : ListUpdateCallback {

    override fun onInserted(position: Int, count: Int) {
        mAdapter.notifyItemRangeInserted(mAdapter.adjustNotifyPosition(position), count)
    }

    override fun onRemoved(position: Int, count: Int) {
        mAdapter.notifyItemRangeRemoved(mAdapter.adjustNotifyPosition(position), count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        mAdapter.notifyItemMoved(
                mAdapter.adjustNotifyPosition(fromPosition),
                mAdapter.adjustNotifyPosition(toPosition)
        )
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        mAdapter.notifyItemRangeChanged(mAdapter.adjustNotifyPosition(position), count, payload)
    }

}