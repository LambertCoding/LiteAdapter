package me.yuu.liteadapter.diff;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class LiteDiffUtil extends DiffUtil.Callback {

    private List<?> oldData;
    private List<?> newData;
    private Callback diffCallback;

    public LiteDiffUtil(List<?> oldData, List<?> newData, Callback diffCallback) {
        this.oldData = oldData;
        this.newData = newData;
        this.diffCallback = diffCallback;
    }

    @Override
    public int getOldListSize() {
        return oldData == null ? 0 : oldData.size();
    }

    @Override
    public int getNewListSize() {
        return newData == null ? 0 : newData.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return diffCallback.areItemsTheSame(oldData.get(oldItemPosition), newData.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return diffCallback.areContentsTheSame(oldData.get(oldItemPosition), newData.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // 当areItemsTheSame = true，areContentsTheSame = false时调用
        // 返回两个item内容差异的字段，在adapter的三个参数的onBindViewHolder方法中，可以实现差量更新
        return diffCallback.getChangePayload(oldItemPosition, newItemPosition);
    }

    public interface Callback {
        boolean areItemsTheSame(Object oldItem, Object newItem);

        boolean areContentsTheSame(Object oldItem, Object newItem);

        Object getChangePayload(int oldItemPosition, int newItemPosition);
    }
}
