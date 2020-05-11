package me.yuu.liteadapter.databinding

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import me.yuu.liteadapter.core.ViewHolder

/**
 * @author yu
 * @date 2019/4/15
 */
class DataBindingViewHolder(itemView: View) : ViewHolder(itemView) {

    val binding: ViewDataBinding? = DataBindingUtil.bind(itemView)

}