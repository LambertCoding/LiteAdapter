package me.yuu.liteadapter.core

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import me.yuu.liteadapter.diff.LiteDiffUtil
import me.yuu.liteadapter.loadmore.DefaultLoadMoreFooter
import me.yuu.liteadapter.loadmore.ILoadMoreFooter
import me.yuu.liteadapter.loadmore.MoreLoader
import me.yuu.liteadapter.loadmore.MoreLoader.LoadMoreListener
import me.yuu.liteadapter.util.LiteAdapterUtils
import java.lang.ref.WeakReference

/**
 * RecyclerView的通用Adapter
 *
 * @author yu
 * @date 2018/1/12
 */
class LiteAdapterEx<T>(
        private val moreLoader: MoreLoader?,
        private val emptyView: View?,
        /**
         * 空布局是否要保持header和footer
         */
        private val isKeepHeaderAndFooter: Boolean,
        /**
         * key: viewType    value: headerView
         */
        var herders: SparseArray<View>,
        /**
         * key: viewType    value: footerView
         */
        var footers: SparseArray<View>,
        injectorFinder: InjectorFinder<T>?,
        injectors: SparseArray<ViewInjector<T>>,
        diffCallback: LiteDiffUtil.Callback?,
        onItemClickListener: OnItemClickListener?,
        onItemLongClickListener: OnItemLongClickListener?
) : LiteAdapter<T>(injectors, injectorFinder, diffCallback, onItemClickListener, onItemLongClickListener) {

    private var mRecyclerView: WeakReference<RecyclerView?>? = null
    private var mOrientation = 0

    fun copyFrom(adapter: LiteAdapterEx<T>): LiteAdapterEx<T> {
        return LiteAdapterEx(
                adapter.moreLoader,
                adapter.emptyView,
                adapter.isKeepHeaderAndFooter,
                adapter.herders,
                adapter.footers,
                adapter.injectorFinder,
                adapter.viewInjectors,
                adapter.diffCallback,
                adapter.onItemClickListener,
                adapter.onItemLongClickListener
        )
    }

    override fun getItemCount(): Int {
        var itemCount = mDataSet.size + herders.size() + footers.size()
        if (isEmptyViewEnable()) {
            if (isKeepHeaderAndFooter) {
                itemCount++
            } else {
                itemCount = 1
            }
        } else {
            // 数据不为空，loadMore可用则+1
            if (isLoadMoreEnable) itemCount++
        }
        return itemCount
    }

    override fun getItemViewType(position: Int): Int {
        if (isEmptyViewPosition(position)) {
            return VIEW_TYPE_EMPTY
        }
        if (isHeader(position)) {
            return herders.keyAt(position)
        }
        if (isFooter(position)) {
            var index = position - herders.size() - mDataSet.size
            if (isEmptyViewEnable()) index--
            return footers.keyAt(index)
        }
        return if (isLoadMoreEnable && isLoadMorePosition(position)) {
            VIEW_TYPE_LOAD_MORE
        } else super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when {
            viewType == VIEW_TYPE_EMPTY -> ViewHolder(emptyView!!)
            viewType == VIEW_TYPE_LOAD_MORE -> ViewHolder(moreLoader!!.loadMoreFooterView)
            isHeaderType(viewType) -> {
                val view = herders[viewType].apply {
                    layoutParams = LiteAdapterUtils.generateLayoutParamsForHeaderAndFooter(mOrientation, this)
                }
                ViewHolder(view)
            }
            isFooterType(viewType) -> {
                val view = footers[viewType].apply {
                    layoutParams = LiteAdapterUtils.generateLayoutParamsForHeaderAndFooter(mOrientation, this)
                }
                ViewHolder(view)
            }
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (isHeader(position)
                || isFooter(position)
                || isEmptyViewPosition(position)
                || isLoadMorePosition(position)) {
            return
        }
        super.onBindViewHolder(holder, position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (mRecyclerView == null || mRecyclerView!!.get() == null) {
            mRecyclerView = WeakReference(recyclerView)
        }
        initOrientation(recyclerView.layoutManager)
        setSpanSizeLookup4Grid(recyclerView)
        if (moreLoader != null && footers.size() == 0) {
            recyclerView.addOnScrollListener(moreLoader)
        }
    }

    private fun setSpanSizeLookup4Grid(recyclerView: RecyclerView) {
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (isEmptyViewPosition(position)
                            || isHeader(position)
                            || isFooter(position)
                            || isLoadMorePosition(position)) manager.spanCount else 1
                }
            }
        }
    }

    private fun initOrientation(manager: RecyclerView.LayoutManager?) {
        if (manager is LinearLayoutManager) {
            mOrientation = manager.orientation
        } else if (manager is StaggeredGridLayoutManager) {
            mOrientation = manager.orientation
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        if (moreLoader != null) recyclerView.removeOnScrollListener(moreLoader)
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        setSpanSizeLookup4StaggeredGrid(holder)
    }

    private fun setSpanSizeLookup4StaggeredGrid(holder: ViewHolder) {
        val lp = holder.itemView.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            val position = holder.layoutPosition
            if (isEmptyViewPosition(position)
                    || isHeader(position)
                    || isFooter(position)
                    || isLoadMorePosition(position)) {
                lp.isFullSpan = true
            }
        }
    }

    fun addFooter(footer: View, notify: Boolean) {
        footers.put(VIEW_TYPE_FOOTER_INDEX + footers.size(), footer)
        if (notify) notifyDataSetChanged()
    }

    fun removeFooter(footerPosition: Int, notify: Boolean) {
        removeAndOptimizeIndex(false, footerPosition, notify)
    }

    fun addHeader(header: View, notify: Boolean) {
        herders.put(VIEW_TYPE_HEADER_INDEX + herders.size(), header)
        if (notify) notifyDataSetChanged()
    }

    fun removeHeader(headerPosition: Int, notify: Boolean) {
        removeAndOptimizeIndex(true, headerPosition, notify)
    }

    private fun removeAndOptimizeIndex(isHeader: Boolean, position: Int, notify: Boolean) {
        val target = if (isHeader) herders else footers
        require(position >= 0 && position < target.size()) {
            "Invalid position = $position ${if (isHeader) ", mHeaders.Size() = " else ", mFooters.Size() = "} ${target.size()}"
        }

        // 因为header和footer的type都是按照添加的顺序自动生成的
        // 所以删除指定位置的header和footer后，需要重新优化key，否则再次addHeader或者addFooter可能会出错
        val temp = SparseArray<View>()
        val viewTypeIndex = if (isHeader) VIEW_TYPE_HEADER_INDEX else VIEW_TYPE_FOOTER_INDEX
        for (i in 0 until target.size()) {
            if (i == position) {
                continue
            }
            temp.put(viewTypeIndex + temp.size(), target.valueAt(i))
        }
        if (isHeader) {
            herders = temp
        } else {
            footers = temp
        }
        if (notify) notifyDataSetChanged()
    }

    ///////////////////////////////////LoadMore///////////////////////////////////
    override fun beforeUpdateData() {
        // setNewData后，notifyDataSetChanged之前回调
        // 设置数据后判断是否占满一页，如果不满一页就不开启load more，反之则开启
        if (moreLoader != null && mRecyclerView != null) {
            disableLoadMoreIfNotFullPage(mRecyclerView!!.get())
        }
    }

    private fun disableLoadMoreIfNotFullPage(recyclerView: RecyclerView?) {
        if (recyclerView == null) return
        val manager = recyclerView.layoutManager ?: return
        recyclerView.postDelayed(Runnable {
            val position = LiteAdapterUtils.findLastCompletelyVisibleItemPosition(manager)
            isLoadMoreEnable = position + 1 != itemCount
        }, 100)
    }

    fun loadMoreCompleted() {
        moreLoader?.loadMoreCompleted()
    }

    fun loadMoreError() {
        moreLoader?.loadMoreError()
    }

    fun noMore() {
        moreLoader?.noMore()
    }

    override fun adjustNotifyPosition(position: Int): Int {
        return position + herders.size()
    }

    override fun adjustGetItemPosition(position: Int): Int {
        return position - herders.size()
    }

    override fun isReservedType(viewType: Int): Boolean {
        return viewType == VIEW_TYPE_EMPTY || viewType == VIEW_TYPE_LOAD_MORE || isHeaderType(viewType) || isFooterType(viewType)
    }

    private fun isHeaderType(viewType: Int): Boolean {
        return herders.size() > 0 && herders[viewType] != null
    }

    private fun isFooterType(viewType: Int): Boolean {
        return footers.size() > 0 && footers[viewType] != null
    }

    private fun isHeader(position: Int): Boolean {
        val headersCount = herders.size()
        return position in 0 until headersCount
    }

    private fun isFooter(position: Int): Boolean {
        var headerAndDataCount = herders.size() + mDataSet.size
        if (isEmptyViewEnable()) {
            headerAndDataCount++
        }
        return footers.size() > 0 && position >= headerAndDataCount && position < headerAndDataCount + footers.size()
    }

    private fun isEmptyViewPosition(position: Int): Boolean {
        return if (isEmptyViewEnable()) {
            if (isKeepHeaderAndFooter) herders.size() == position else position == 0
        } else false
    }

    fun isEmptyViewEnable(): Boolean {
        return emptyView != null && mDataSet.size == 0
    }


    var isLoadMoreEnable: Boolean
        get() = moreLoader != null && moreLoader.isLoadMoreEnable && footers.size() == 0
        set(enable) {
            requireNotNull(moreLoader) {
                "MoreLoader == null, You should call enableLoadMore when you build the LiteAdapter."
            }.isLoadMoreEnable = enable
            notifyDataSetChanged()
        }

    private fun isLoadMorePosition(position: Int): Boolean {
        return position == mDataSet.size + herders.size()
    }

    ////////////////////////////////////////////////////////////////////////////
    class Builder<D>(private val context: Context) : LiteAdapterBuilder<D, LiteAdapterEx<D>>() {

        private var emptyView: View? = null
        private var moreLoader: MoreLoader? = null
        private var keepHeaderAndFooter = true
        private val headers = SparseArray<View>()
        private val footers = SparseArray<View>()

        override fun autoDiff(diffCallback: LiteDiffUtil.Callback?): Builder<D> {
            super.autoDiff(diffCallback)
            return this
        }

        override fun itemClickListener(listener: (Int, Any) -> Unit): Builder<D> {
            super.itemClickListener(listener)
            return this
        }

        override fun itemLongClickListener(listener: (Int, Any) -> Unit): Builder<D> {
            super.itemLongClickListener(listener)
            return this
        }

        override fun injectorFinder(finder: (item: D, position: Int, itemCount: Int) -> Int): Builder<D> {
            super.injectorFinder(finder)
            return this
        }

        override fun register(injector: ViewInjector<D>): Builder<D> {
            super.register(injector)
            return this
        }


        fun emptyView(@LayoutRes layoutId: Int): Builder<D> {
            return emptyView(LayoutInflater.from(context).inflate(layoutId, null))
        }

        fun keepHeaderAndFooter(keep: Boolean): Builder<D> {
            keepHeaderAndFooter = keep
            return this
        }

        fun emptyView(empty: View): Builder<D> {
            require(emptyView == null) {
                "You have already set a empty view."
            }
            emptyView = empty.apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
            }
            return this
        }


        fun headerView(header: View): Builder<D> {
            val headerType = VIEW_TYPE_HEADER_INDEX + headers.size()
            headers.put(headerType, header)
            return this
        }

        fun footerView(footer: View): Builder<D> {
            val footerType = VIEW_TYPE_FOOTER_INDEX + footers.size()
            footers.put(footerType, footer)
            return this
        }

        @JvmOverloads
        fun enableLoadMore(loadMoreFooter: ILoadMoreFooter = DefaultLoadMoreFooter(context),
                           loadMoreListener: () -> Unit): Builder<D> {
            require(moreLoader == null) {
                "You have already called enableLoadMore, Don't call again!"
            }
            moreLoader = MoreLoader(object : LoadMoreListener {
                override fun onLoadMore() {
                    loadMoreListener()
                }
            }, loadMoreFooter)
            return this
        }

        override fun create(): LiteAdapterEx<D> {
            return LiteAdapterEx(
                    moreLoader, emptyView, keepHeaderAndFooter, headers, footers, injectorFinder,
                    injectors, diffCallback, onItemClickListener, onItemLongClickListener
            )
        }
    }

    companion object {
        private const val VIEW_TYPE_EMPTY = -7061
        private const val VIEW_TYPE_LOAD_MORE = -7062
        private const val VIEW_TYPE_HEADER_INDEX = -7060
        private const val VIEW_TYPE_FOOTER_INDEX = -8060
    }

}