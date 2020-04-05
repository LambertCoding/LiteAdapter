package me.yuu.liteadapter.core;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import me.yuu.liteadapter.diff.LiteDiffUtil;
import me.yuu.liteadapter.diff.LiteListUpdateCallback;

/**
 * @author yu
 * @date 2018/1/14
 */
public abstract class AbstractAdapter<T> extends RecyclerView.Adapter<ViewHolder> implements DataOperator<T> {

    protected List<T> mDataSet = new ArrayList<>();
    protected LiteDiffUtil.Callback mDiffCallback;

    protected abstract void beforeUpdateData();

    /**
     * 当有Header view时，修改数据并进行定向刷新，需要修正角标
     *
     * @param position position
     * @return position + headerCount
     */
    public int adjustNotifyPosition(int position) {
        return position;
    }

    /**
     * 当有Header view时，在mDataSet获取数据需要修正角标，减去headerCount
     *
     * @param position position
     * @return position - headerCount
     */
    public int adjustGetItemPosition(int position) {
        return position;
    }

    public LiteDiffUtil.Callback getDiffCallback() {
        return mDiffCallback;
    }

    public List<T> getDataSet() {
        return mDataSet;
    }

    @Override
    public T getItem(@IntRange(from = 0) int position) {
        return mDataSet.get(position);
    }

    @Override
    public void updateData(List<T> items) {
        if (mDiffCallback != null) {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                    new LiteDiffUtil(mDataSet, items, mDiffCallback)
            );
            this.mDataSet = new ArrayList<>(items);
            beforeUpdateData();
            diffResult.dispatchUpdatesTo(new LiteListUpdateCallback(this));
        } else {
            mDataSet.clear();
            if (items != null && !items.isEmpty()) {
                mDataSet.addAll(items);
            }
            beforeUpdateData();
            notifyDataSetChanged();
        }
    }

    @Override
    public void addData(@NonNull T item) {
        int adjustNotifyPosition = adjustNotifyPosition(mDataSet.size());
        mDataSet.add(item);
        notifyItemInserted(adjustNotifyPosition);
    }

    @Override
    public void addData(@IntRange(from = 0) int position, @NonNull T item) {
        mDataSet.add(position, item);
        notifyItemInserted(adjustNotifyPosition(position));
    }

    @Override
    public void addAll(@NonNull List<T> items) {
        if (!items.isEmpty()) {
            mDataSet.addAll(items);
            notifyItemRangeInserted(adjustNotifyPosition(mDataSet.size()), items.size());
        }
    }

    @Override
    public void addAll(@IntRange(from = 0) int position, @NonNull List<T> items) {
        if (!items.isEmpty()) {
            mDataSet.addAll(position, items);
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
