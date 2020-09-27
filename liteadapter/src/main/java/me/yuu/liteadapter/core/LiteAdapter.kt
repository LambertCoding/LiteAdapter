package me.yuu.liteadapter.core

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
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
open class LiteAdapter<T>(protected val context: Context) : AbsAdapter<T>() {

    /**
     * key: viewType    value: [ViewInjector]
     */
    private val viewInjectors by lazy { SparseArray<ViewInjector<T>>() }
    private var injectorFinder: InjectorFinder<T>? = null
    private var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val injector = requireNotNull(viewInjectors[viewType]) {
            "You haven't registered this view type($viewType) yet . Or you return the wrong view type in InjectorFinder."
        }

        val itemView = LayoutInflater.from(parent.context).inflate(injector.layoutId, parent, false)
        val holder = createCustomViewHolder(itemView, injector)
        setupItemClickListener(holder)
        setupItemLongClickListener(holder)

        return holder
    }

    protected open fun createCustomViewHolder(itemView: View, injector: ViewInjector<*>): ViewHolder {
        return if (injector is DataBindingInjector<*>) {
            DataBindingViewHolder(itemView)
        } else {
            ViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mDataSet[adjustGetItemIndex(position)]
        val viewType = getItemViewType(position)

        require(!isReservedType(viewType)) {
            "You use the reserved view type : $viewType"
        }

        val injector = requireNotNull(viewInjectors[viewType]) {
            "You haven't registered this view type(  $viewType ) yet . Or you return the wrong view type in InjectorFinder."
        }

        injector.bind(holder, item, adjustGetItemIndex(position))
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun getItemViewType(position: Int): Int {
        return getViewTypeFromInjectors(position)
    }

    protected open fun getViewTypeFromInjectors(position: Int): Int {
        require(viewInjectors.size() != 0) {
            "No view type is registered."
        }

        val index = if (viewInjectors.size() > 1) {
            requireNotNull(injectorFinder) {
                "Multiple view types are registered. You must set a injectorFinder"
            }

            val adjustPosition = adjustGetItemIndex(position)
            injectorFinder!!.index(mDataSet[adjustPosition], adjustPosition, itemCount)
        } else {
            0
        }

        require(index in 0 until viewInjectors.size()) {
            "return wrong index =  $index  in InjectorFinder, You have registered ${viewInjectors.size()} ViewInjector!"
        }

        return viewInjectors.keyAt(index)
    }

    private fun setupItemClickListener(viewHolder: ViewHolder) {
        viewHolder.itemView.setOnClickListener {
            val position = adjustGetItemIndex(viewHolder.layoutPosition)
            onItemClickListener?.invoke(position, mDataSet[position] as Any)
        }
    }

    private fun setupItemLongClickListener(viewHolder: ViewHolder) {
        viewHolder.itemView.setOnLongClickListener {
            val position = adjustGetItemIndex(viewHolder.layoutPosition)
            onItemLongClickListener?.invoke(position, mDataSet[position] as Any)
            true
        }
    }

    protected open fun isReservedType(viewType: Int): Boolean {
        return false
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    fun register(injector: ViewInjector<T>) {
        val viewType = viewInjectors.size() + 1
        viewInjectors.put(viewType, injector)
    }

    fun register(@LayoutRes layoutId: Int, block: (holder: ViewHolder, item: T, position: Int) -> Unit) {
        register(object : ViewInjector<T>(layoutId) {
            override fun bind(holder: ViewHolder, item: T, position: Int) {
                block.invoke(holder, item, position)
            }
        })
    }

    fun injectorFinder(finder: InjectorFinder<T>) {
        this.injectorFinder = finder
    }

    fun injectorFinder(finder: (item: T, position: Int, itemCount: Int) -> Int) {
        this.injectorFinder = object : InjectorFinder<T> {
            override fun index(item: T, position: Int, itemCount: Int): Int {
                return finder(item, position, itemCount)
            }
        }
    }

    fun itemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    fun itemLongClickListener(listener: OnItemLongClickListener) {
        this.onItemLongClickListener = listener
    }


    open fun autoDiff(diffCallback: LiteDiffUtil.Callback? = DefaultDiffCallback()) {
        this.diffCallback = diffCallback
    }

}