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
public abstract class AbstractAdapter<T> extends RecyclerView.Adapter<ViewHolder> implements DataOperator<T> {

    protected final List<T> mDataSet = new ArrayList<>();

    protected abstract void beforeSetNewData();

    /**
     * 当有Header view时，修改数据并进行定向刷新，需要修正角标
     *
     * @param position position
     * @return position + headerViews.size
     */
    protected int adjustNotifyPosition(int position) {
        return position;
    }

    public List getDataSet() {
        return mDataSet;
    }

    @Override
    public T getItem(@IntRange(from = 0) int position) {
        return mDataSet.get(position);
    }

    @Override
    public void setNewData(List<T> items) {
        mDataSet.clear();
        if (items != null && !items.isEmpty()) {
            mDataSet.addAll(items);
        }
        beforeSetNewData();
        notifyDataSetChanged();
    }

    @Override
    public void addData(@NonNull T item) {
        int position = adjustNotifyPosition(mDataSet.size());
        mDataSet.add(item);
        notifyItemInserted(position);
    }

    @Override
    public void addData(@IntRange(from = 0) int position, @NonNull T item) {
        mDataSet.add(position, item);
        notifyItemInserted(adjustNotifyPosition(position));
    }

    @Override
    public void addAll(@NonNull List<T> items) {
        if (!items.isEmpty()) {
            int startPosition = adjustNotifyPosition(mDataSet.size());
            mDataSet.addAll(items);
            notifyItemRangeInserted(startPosition, items.size());
        }
    }

    @Override
    public void addAll(@IntRange(from = 0) int position, @NonNull List<T> items) {
        if (!items.isEmpty()) {
            mDataSet.addAll(items);
            notifyItemRangeInserted(adjustNotifyPosition(position), items.size());
        }
    }

    @Override
    public void remove(@IntRange(from = 0) int position) {
        mDataSet.remove(position);
        if (mDataSet.isEmpty()) {
            notifyDataSetChanged();
        } else {
            notifyItemRemoved(adjustNotifyPosition(position));
        }
    }

    @Override
    public void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }

    @Override
    public void modify(@IntRange(from = 0) int position, @NonNull T newData) {
        mDataSet.set(position, newData);
        notifyItemChanged(adjustNotifyPosition(position));
    }

    @Override
    public void modify(@IntRange(from = 0) int position, Action<T> action) {
        action.doAction(mDataSet.get(position));
        notifyItemChanged(adjustNotifyPosition(position));
    }

    public interface OnItemClickListener {
        void onItemClick(int position, Object item);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, Object item);
    }

}
