package me.yuu.liteadapter.loadmore

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import me.yuu.liteadapter.core.Utils

/**
 * @author yu
 * @date 2018/1/12
 */
class MoreLoader(
        private val mLoadMoreListener: LoadMoreListener?,
        private val mLoadMoreFooter: ILoadMoreFooter
) : RecyclerView.OnScrollListener() {

    var isLoadMoreEnable = false

    val loadMoreFooterView: View
        get() = mLoadMoreFooter.view

    fun loadMoreCompleted() {
        mLoadMoreFooter.status = ILoadMoreFooter.Status.COMPLETED
    }

    fun loadMoreError() {
        mLoadMoreFooter.status = ILoadMoreFooter.Status.ERROR
    }

    fun noMore() {
        mLoadMoreFooter.status = ILoadMoreFooter.Status.NO_MORE
    }

    init {
        mLoadMoreFooter.view.setOnClickListener {
            if (mLoadMoreFooter.status == ILoadMoreFooter.Status.ERROR) {
                mLoadMoreListener?.onLoadMore()
                mLoadMoreFooter.status = ILoadMoreFooter.Status.LOADING
            }
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (!isLoadMoreEnable) {
            return
        }
        when (newState) {
            RecyclerView.SCROLL_STATE_IDLE -> {
                if (mLoadMoreListener == null
                        || mLoadMoreFooter.status == ILoadMoreFooter.Status.LOADING
                        || mLoadMoreFooter.status == ILoadMoreFooter.Status.ERROR
                        || mLoadMoreFooter.status == ILoadMoreFooter.Status.NO_MORE) {
                    return
                }
                val layoutManager = recyclerView.layoutManager ?: return
                val lastPosition = Utils.findLastCompletelyVisibleItemPosition(layoutManager)
                if (layoutManager.childCount > 0
                        && lastPosition >= layoutManager.itemCount - 1) {
                    mLoadMoreFooter.status = ILoadMoreFooter.Status.LOADING
                    mLoadMoreListener.onLoadMore()
                }
            }
        }
    }
}

interface LoadMoreListener {
    fun onLoadMore()
}
