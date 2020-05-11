package me.yuu.liteadapter.core

import android.util.SparseArray
import me.yuu.liteadapter.diff.DefaultDiffCallback
import me.yuu.liteadapter.diff.LiteDiffUtil

abstract class LiteAdapterBuilder<D, T : LiteAdapter<D>> {

    protected var diffCallback: LiteDiffUtil.Callback? = null
    protected var injectorFinder: InjectorFinder<D>? = null
    protected var onItemClickListener: AbstractAdapter.OnItemClickListener? = null
    protected var onItemLongClickListener: AbstractAdapter.OnItemLongClickListener? = null
    protected val injectors = SparseArray<ViewInjector<D>>()

    open fun autoDiff(diffCallback: LiteDiffUtil.Callback? = DefaultDiffCallback()): LiteAdapterBuilder<D, T> {
        this.diffCallback = diffCallback
        return this
    }

    open fun itemClickListener(listener: (Int, Any) -> Unit): LiteAdapterBuilder<D, T> {
        onItemClickListener = object : AbstractAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, item: Any) {
                listener(position, item)
            }
        }
        return this
    }

    open fun itemLongClickListener(listener: (Int, Any) -> Unit): LiteAdapterBuilder<D, T> {
        onItemLongClickListener = object : AbstractAdapter.OnItemLongClickListener {
            override fun onItemLongClick(position: Int, item: Any) {
                listener(position, item)
            }
        }
        return this
    }

    open fun injectorFinder(finder: (item: D, position: Int, itemCount: Int) -> Int): LiteAdapterBuilder<D, T> {
        require(injectorFinder == null) {
            "Only one InjectorFinder can be registered."
        }
        injectorFinder = object : InjectorFinder<D> {
            override fun index(item: D, position: Int, itemCount: Int): Int {
               return finder.invoke(item, position, itemCount)
            }
        }
        return this
    }

    open fun register(injector: ViewInjector<D>): LiteAdapterBuilder<D, T> {
        val viewType = injectors.size() + 1
        injectors.put(viewType, injector)
        return this
    }

    abstract fun create(): T
}