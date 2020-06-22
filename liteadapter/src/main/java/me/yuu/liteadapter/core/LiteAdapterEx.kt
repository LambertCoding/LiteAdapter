package me.yuu.liteadapter.core

import android.content.Context
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import me.yuu.liteadapter.loadmore.DefaultLoadMoreFooter
import me.yuu.liteadapter.loadmore.ILoadMoreFooter
import me.yuu.liteadapter.loadmore.LoadMoreListener
import me.yuu.liteadapter.loadmore.MoreLoader
import java.lang.ref.WeakReference

/**
 * RecyclerView的通用Adapter
 *
 * @author yu
 * @date 2018/1/12
 */
class LiteAdapterEx<T>(context: Context) : LiteAdapter<T>(context) {

    private var moreLoader: MoreLoader? = null
    private var herders: SparseArray<View> = SparseArray()
    private var footers: SparseArray<View> = SparseArray()
    private var mRecyclerView: WeakReference<RecyclerView?>? = null
    private var mOrientation = 0
    var emptyView: View? = null
        set(value) {
            value?.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            field = value
        }

    // 空布局是否要保持header和footer
    var keepHeadAndFoot: Boolean = false

    override fun getItemCount(): Int {
        var itemCount = mDataSet.size + herders.size() + footers.size()
        if (isEmptyViewEnable()) {
            if (keepHeadAndFoot) {
                itemCount++
            } else {
                itemCount = 1
            }
        } else {
            // 数据不为空，loadMore可用则+1
            if (isLoadMoreEnable()) {
                itemCount++
            }
        }
        return itemCount
    }

    override fun getItemViewType(position: Int): Int {
        if (isEmptyViewIndex(position)) {
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
        return if (isLoadMoreEnable() && isLoadMoreIndex(position)) {
            VIEW_TYPE_LOAD_MORE
        } else super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when {
            viewType == VIEW_TYPE_EMPTY -> ViewHolder(emptyView!!)
            viewType == VIEW_TYPE_LOAD_MORE -> ViewHolder(moreLoader!!.loadMoreFooterView)
            isHeaderType(viewType) -> {
                val view = herders[viewType].apply {
                    layoutParams = Utils.generateLayoutParamsForHeaderAndFooter(mOrientation, this)
                }
                ViewHolder(view)
            }
            isFooterType(viewType) -> {
                val view = footers[viewType].apply {
                    layoutParams = Utils.generateLayoutParamsForHeaderAndFooter(mOrientation, this)
                }
                ViewHolder(view)
            }
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (isHeader(position) || isFooter(position)
                || isEmptyViewIndex(position) || isLoadMoreIndex(position)) {
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
        moreLoader?.let {
            if (footers.size() == 0) {
                recyclerView.addOnScrollListener(it)
            } else {
                Log.i(TAG, "已有footer布局，不能添加loadMore footer")
            }
        }
    }

    private fun setSpanSizeLookup4Grid(recyclerView: RecyclerView) {
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(index: Int): Int {
                    return if (isEmptyViewIndex(index)
                            || isHeader(index)
                            || isFooter(index)
                            || isLoadMoreIndex(index)) manager.spanCount else 1
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
        moreLoader?.let {
            recyclerView.removeOnScrollListener(it)
        }
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        setSpanSizeLookup4StaggeredGrid(holder)
    }

    private fun setSpanSizeLookup4StaggeredGrid(holder: ViewHolder) {
        val lp = holder.itemView.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            val index = holder.layoutPosition
            if (isEmptyViewIndex(index)
                    || isHeader(index)
                    || isFooter(index)
                    || isLoadMoreIndex(index)) {
                lp.isFullSpan = true
            }
        }
    }

    fun addFooter(footer: View, notifyAtOnce: Boolean = false) {
        footers.put(VIEW_TYPE_FOOTER_INDEX + footers.size(), footer)
        if (notifyAtOnce) notifyDataSetChanged()
    }

    fun removeFooter(index: Int, notifyAtOnce: Boolean = false) {
        removeAndOptimizeIndex(false, index, notifyAtOnce)
    }

    fun addHeader(header: View, notifyAtOnce: Boolean = false) {
        herders.put(VIEW_TYPE_HEADER_INDEX + herders.size(), header)
        if (notifyAtOnce) notifyDataSetChanged()
    }

    fun removeHeader(index: Int, notifyAtOnce: Boolean = false) {
        removeAndOptimizeIndex(true, index, notifyAtOnce)
    }

    private fun removeAndOptimizeIndex(isHeader: Boolean, index: Int, notify: Boolean) {
        val target = if (isHeader) herders else footers
        require(index in 0 until target.size()) {
            "Invalid index = $index ${if (isHeader) ", mHeaders.Size() = " else ", mFooters.Size() = "} ${target.size()}"
        }

        // 因为header和footer的type都是按照添加的顺序自动生成的
        // 所以删除指定位置的header和footer后，需要重新优化key，否则再次addHeader或者addFooter可能会出错
        val temp = SparseArray<View>()
        val viewTypeIndex = if (isHeader) VIEW_TYPE_HEADER_INDEX else VIEW_TYPE_FOOTER_INDEX
        for (i in 0 until target.size()) {
            if (i == index) {
                continue
            }
            temp.put(viewTypeIndex + temp.size(), target.valueAt(i))
        }
        if (isHeader) {
            herders = temp
        } else {
            footers = temp
        }
        if (notify) {
            notifyDataSetChanged()
        }
    }


    override fun beforeUpdateData() {
        // setNewData后，notifyDataSetChanged之前回调
        // 设置数据后判断是否占满一页，如果不满一页就不开启load more，反之则开启
        if (moreLoader != null && mRecyclerView != null) {
            disableLoadMoreIfNotFullPage(mRecyclerView!!.get())
        }
    }

    private fun disableLoadMoreIfNotFullPage(recyclerView: RecyclerView?) {
        val manager = recyclerView?.layoutManager ?: return
        recyclerView.postDelayed(Runnable {
            val index = Utils.findLastCompletelyVisibleItemPosition(manager)
            moreLoader?.isLoadMoreEnable = index + 1 != itemCount
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

    override fun adjustNotifyIndex(index: Int): Int {
        return index + herders.size()
    }

    override fun adjustGetItemIndex(index: Int): Int {
        return index - herders.size()
    }

    override fun isReservedType(viewType: Int): Boolean {
        return viewType == VIEW_TYPE_EMPTY || viewType == VIEW_TYPE_LOAD_MORE
                || isHeaderType(viewType) || isFooterType(viewType)
    }

    private fun isHeaderType(viewType: Int): Boolean {
        return herders.size() > 0 && herders[viewType] != null
    }

    private fun isFooterType(viewType: Int): Boolean {
        return footers.size() > 0 && footers[viewType] != null
    }

    private fun isHeader(index: Int): Boolean {
        return index in 0 until herders.size()
    }

    private fun isFooter(index: Int): Boolean {
        var headerAndDataCount = herders.size() + mDataSet.size
        if (isEmptyViewEnable()) {
            headerAndDataCount++
        }
        return footers.size() > 0
                && index in headerAndDataCount until headerAndDataCount + footers.size()
    }

    private fun isEmptyViewIndex(index: Int): Boolean {
        return if (isEmptyViewEnable()) {
            if (keepHeadAndFoot) herders.size() == index else index == 0
        } else false
    }

    private fun isEmptyViewEnable(): Boolean {
        return emptyView != null && mDataSet.size == 0
    }

    fun enableLoadMore(loadMoreFooter: ILoadMoreFooter = DefaultLoadMoreFooter(context),
                       loadMoreListener: () -> Unit) {
        moreLoader = MoreLoader(object : LoadMoreListener {
            override fun onLoadMore() {
                loadMoreListener()
            }
        }, loadMoreFooter).also {
            it.isLoadMoreEnable = true
        }
    }

    private fun isLoadMoreEnable(): Boolean {
        return moreLoader?.isLoadMoreEnable ?: false
                && footers.size() == 0
    }

    private fun isLoadMoreIndex(index: Int): Boolean {
        return index == mDataSet.size + herders.size()
    }

    companion object {
        private const val TAG = "LiteAdapterEx"
        private const val VIEW_TYPE_EMPTY = -7061
        private const val VIEW_TYPE_LOAD_MORE = -7062
        private const val VIEW_TYPE_HEADER_INDEX = -7060
        private const val VIEW_TYPE_FOOTER_INDEX = -8060
    }

}