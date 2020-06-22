package me.yuu.liteadapter.databinding

import me.yuu.liteadapter.BR
import me.yuu.liteadapter.core.ViewHolder
import me.yuu.liteadapter.core.ViewInjector

/**
 * @author yu
 * @date 2019/4/15
 */
class DataBindingInjector<D>(layoutId: Int) : ViewInjector<D>(layoutId) {
    override fun bind(holder: ViewHolder, item: D, position: Int) {
        val dViewHolder = holder as DataBindingViewHolder
        val binding = dViewHolder.binding
        if (binding != null) {
            binding.setVariable(BR.item, item)
            binding.executePendingBindings()
        }
    }
}