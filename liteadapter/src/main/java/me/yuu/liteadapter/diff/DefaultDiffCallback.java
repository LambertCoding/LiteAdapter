package me.yuu.liteadapter.diff;

public class DefaultDiffCallback implements LiteDiffUtil.Callback {

    @Override
    public boolean areItemsTheSame(Object oldItem, Object newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(Object oldItem, Object newItem) {
        return true;
    }

    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return null;
    }
}
