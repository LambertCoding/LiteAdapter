package me.yuu.liteadapter.diff;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListUpdateCallback;

import me.yuu.liteadapter.core.AbstractAdapter;

/**
 * 由于可能有header和footer，所以DiffResult需要修正角标
 */
public class LiteListUpdateCallback implements ListUpdateCallback {
    @NonNull
    private final AbstractAdapter mAdapter;

    /**
     * Creates an AdapterListUpdateCallback that will dispatch update events to the given adapter.
     *
     * @param adapter The Adapter to send updates to.
     */
    public LiteListUpdateCallback(@NonNull AbstractAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public void onInserted(int position, int count) {
        mAdapter.notifyItemRangeInserted(mAdapter.adjustNotifyPosition(position), count);
    }

    @Override
    public void onRemoved(int position, int count) {
        mAdapter.notifyItemRangeRemoved(mAdapter.adjustNotifyPosition(position), count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        mAdapter.notifyItemMoved(
                mAdapter.adjustNotifyPosition(fromPosition),
                mAdapter.adjustNotifyPosition(toPosition)
        );
    }

    @Override
    public void onChanged(int position, int count, Object payload) {
        mAdapter.notifyItemRangeChanged(mAdapter.adjustNotifyPosition(position), count, payload);
    }
}
