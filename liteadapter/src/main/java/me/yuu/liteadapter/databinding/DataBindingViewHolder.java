package me.yuu.liteadapter.databinding;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import me.yuu.liteadapter.core.ViewHolder;

/**
 * @author yu
 * @date 2019/4/15
 */
public class DataBindingViewHolder extends ViewHolder {

    private ViewDataBinding mBinding;

    public DataBindingViewHolder(@NonNull View itemView) {
        super(itemView);
        mBinding = DataBindingUtil.bind(itemView);
    }

    ViewDataBinding getBinding() {
        return mBinding;
    }
}
