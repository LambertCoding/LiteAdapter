package me.yuu.liteadapter.core

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.yuu.liteadapter.databinding.DataBindingInjector
import me.yuu.liteadapter.databinding.DataBindingViewHolder
import me.yuu.liteadapter.diff.DefaultDiffCallback
import me.yuu.liteadapter.diff.LiteDiffUtil

/**
 * 简易adapter，仅支持多ViewType
 * 如需要支持Header、Footer、EmptyView、LoadMore等扩展功能，请使用[LiteAdapterEx]
 *
 * @param <T>
</T> */
open class LiteAdapter<T>(
        /**
         * key: viewType    value: [ViewInjector]
         */
        val viewInjectors: SparseArray<ViewInjector<T>>,
        val injectorFinder: InjectorFinder<T>?,
        diffCallback: LiteDiffUtil.Callback?,
        var onItemClickListener: OnItemClickListener?,
        var onItemLongClickListener: OnItemLongClickListener?
) : AbstractAdapter<T>(diffCallback) {

    override fun beforeUpdateData() {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val injector = requireNotNull(viewInjectors[viewType]) {
            "You haven't registered this view type($viewType) yet . Or you return the wrong view type in InjectorFinder."
        }

        val itemView = LayoutInflater.from(parent.context)
                .inflate(injector.layoutId, parent, false)
        val holder = createCustomViewHolder(itemView, injector)
        setupItemClickListener(holder)
        setupItemLongClickListener(holder)

        return holder
    }

    protected fun createCustomViewHolder(itemView: View, injector: ViewInjector<*>): ViewHolder {
        return if (injector is DataBindingInjector<*>) {
            DataBindingViewHolder(itemView)
        } else {
            ViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        bindFromViewInjector(holder, position)
    }

    protected fun bindFromViewInjector(holder: ViewHolder, position: Int) {
        val item = mDataSet[adjustGetItemPosition(position)]
        val viewType = getItemViewType(position)

        require(!isReservedType(viewType)) {
            "You use the reserved view type : $viewType"
        }

        val injector = requireNotNull(viewInjectors[viewType]) {
            "You haven't registered this view type(  $viewType ) yet . Or you return the wrong view type in InjectorFinder."
        }

        injector.bindData(holder, item, adjustGetItemPosition(position))
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun getItemViewType(position: Int): Int {
        return getViewTypeFromInjectors(position)
    }

    protected fun getViewTypeFromInjectors(position: Int): Int {
        require(viewInjectors.size() != 0) {
            "No view type is registered."
        }

        val index = if (viewInjectors.size() > 1) {
            requireNotNull(injectorFinder) {
                "Multiple view types are registered. You must set a ViewTypeInjector for LiteAdapter"
            }

            val adjustPosition = adjustGetItemPosition(position)
            injectorFinder.index(mDataSet[adjustPosition], adjustPosition, itemCount)
        } else {
            0
        }

        require(index >= 0 && index < viewInjectors.size()) {
            "return wrong index =  $index  in InjectorFinder, You have registered ${viewInjectors.size()} ViewInjector!"
        }

        return viewInjectors.keyAt(index)
    }

    protected fun setupItemClickListener(viewHolder: ViewHolder) {
        if (onItemClickListener != null) {
            viewHolder.itemView.setOnClickListener {
                val position = adjustGetItemPosition(viewHolder.layoutPosition)
                onItemClickListener!!.onItemClick(position, mDataSet[position] as Any)
            }
        }
    }

    protected fun setupItemLongClickListener(viewHolder: ViewHolder) {
        if (onItemLongClickListener != null) {
            viewHolder.itemView.setOnLongClickListener {
                val position = adjustGetItemPosition(viewHolder.layoutPosition)
                onItemLongClickListener!!.onItemLongClick(position, mDataSet[position] as Any)
                true
            }
        }
    }

    fun attachTo(recyclerView: RecyclerView): LiteAdapter<T> {
        recyclerView.adapter = this
        return this
    }

    protected open fun isReservedType(viewType: Int): Boolean {
        return false
    }


    open class Builder<D> : LiteAdapterBuilder<D, LiteAdapter<D>>() {
        override fun create(): LiteAdapter<D> {
            return LiteAdapter(
                    injectors, injectorFinder, diffCallback, onItemClickListener, onItemLongClickListener
            )
        }
    }

}