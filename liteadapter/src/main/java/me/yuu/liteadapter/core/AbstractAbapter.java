package me.yuu.liteadapter.core;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yu
 * @date 2018/1/14
 */
public abstract class AbstractAbapter extends RecyclerView.Adapter<ViewHolder> implements DataOperator {

    protected final List mDataSet = new ArrayList<>();

    public abstract int getHeadersCount();
    public abstract int getFootersCount();

    public List getDataSet() {
        return mDataSet;
    }

    @Override
    public void addData(@NonNull Object item) {
        if (item != null) {
            int position = getHeadersCount() + mDataSet.size();
            mDataSet.add(item);
            notifyItemInserted(position);
        }
    }

    @Override
    public void addData(@IntRange(from = 0) int position, @NonNull Object item) {
        if (item != null) {
            mDataSet.add(position, item);
            notifyItemInserted(getHeadersCount() + position);
        }
    }

    @Override
    public void addDataToHead(@NonNull Object item) {
        addData(0, item);
    }

    @Override
    public void addAll(@NonNull List items) {
        if (items != null && !items.isEmpty()) {
            int startPosition = getHeadersCount() + mDataSet.size();
            mDataSet.addAll(items);
            notifyItemRangeInserted(startPosition, items.size());
        }
    }

    @Override
    public void addAll(@IntRange(from = 0) int position, List items) {
        if (items != null && !items.isEmpty()) {
            int startPosition = getHeadersCount() + position;
            mDataSet.addAll(items);
            notifyItemRangeInserted(startPosition, items.size());
        }
    }

    @Override
    public void addAllToHead(@NonNull List items) {
        addAll(0, items);
    }

    @Override
    public void remove(@IntRange(from = 0) int position) {
        mDataSet.remove(position);
        if (mDataSet.isEmpty()) {
            notifyDataSetChanged();
        } else {
            notifyItemRemoved(getHeadersCount() + position);
        }
    }

    @Override
    public void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }

    @Override
    public void setNewData(List items) {
        mDataSet.clear();
        if (items != null && !items.isEmpty()) {
            mDataSet.addAll(items);
        }
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(@IntRange(from = 0) int position) {
        return mDataSet.get(position);
    }

    @Override
    public void modify(@IntRange(from = 0) int position, Object newData) {
        if (newData == null) {
            return;
        }
        mDataSet.set(position, newData);
        notifyItemChanged(getHeadersCount() + position);
    }

    @Override
    public void modify(@IntRange(from = 0) int position, Action action) {
        action.doAction(mDataSet.get(position));
        notifyItemChanged(getHeadersCount() + position);
    }

    public interface OnItemClickListener {
        void onItemClick(int position, Object item);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, Object item);
    }

}
