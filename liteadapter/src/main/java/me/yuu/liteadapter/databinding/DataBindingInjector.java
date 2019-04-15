package me.yuu.liteadapter.databinding;

import androidx.databinding.ViewDataBinding;
import me.yuu.liteadapter.BR;
import me.yuu.liteadapter.core.ViewHolder;
import me.yuu.liteadapter.core.ViewInjector;

/**
 * @author yu
 * @date 2019/4/15
 */
public class DataBindingInjector<D> extends ViewInjector<D> {

    public DataBindingInjector(int layoutId) {
        super(layoutId);
    }

    @Override
    public void bindData(ViewHolder holder, D item, int position) {
        DataBindingViewHolder dViewHolder = (DataBindingViewHolder) holder;
        ViewDataBinding binding = dViewHolder.getBinding();
        if (binding != null) {
            binding.setVariable(BR.item, item);
            binding.executePendingBindings();
        }
    }
}
