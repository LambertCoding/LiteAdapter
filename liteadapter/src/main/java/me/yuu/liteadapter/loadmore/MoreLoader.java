package me.yuu.liteadapter.loadmore;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import me.yuu.liteadapter.util.Utils;

/**
 * @author yu
 * @date 2018/1/12
 */
public class MoreLoader extends RecyclerView.OnScrollListener {

    private boolean mLoadMoreSwitch = true;
    private LoadMoreListener mLoadMoreListener;
    private ILoadMoreFooter mLoadMoreFooter;

    public MoreLoader(LoadMoreListener loadmoreListener, ILoadMoreFooter loadMoreFooter) {
        this.mLoadMoreListener = loadmoreListener;
        this.mLoadMoreFooter = loadMoreFooter;
        this.mLoadMoreFooter.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLoadMoreFooter.getStatus() == ILoadMoreFooter.ERROR) {
                    mLoadMoreListener.onLoadMore();
                    mLoadMoreFooter.setStatus(ILoadMoreFooter.LOADING);
                }
            }
        });
    }

    public void setEnable(boolean enable) {
        this.mLoadMoreSwitch = enable;
        if (!enable) {
            mLoadMoreFooter.setStatus(ILoadMoreFooter.COMPLETED);
        }
    }

    public void loadMoreCompleted() {
        mLoadMoreFooter.setStatus(ILoadMoreFooter.COMPLETED);
    }

    public void loadMoreError() {
        mLoadMoreFooter.setStatus(ILoadMoreFooter.ERROR);
    }

    public void noMore() {
        mLoadMoreFooter.setStatus(ILoadMoreFooter.NO_MORE);
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (!mLoadMoreSwitch) {
            return;
        }
        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                if (mLoadMoreListener == null
                        || mLoadMoreFooter.getStatus() == ILoadMoreFooter.LOADING
                        || mLoadMoreFooter.getStatus() == ILoadMoreFooter.ERROR
                        || mLoadMoreFooter.getStatus() == ILoadMoreFooter.NO_MORE) {
                    return;
                }

                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int lastPosition;
                if (layoutManager instanceof GridLayoutManager) {
                    lastPosition = ((GridLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
                } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                    int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                    ((StaggeredGridLayoutManager) layoutManager).findLastCompletelyVisibleItemPositions(into);
                    lastPosition = Utils.findMax(into);
                } else {
                    lastPosition = ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
                }
                if (RecyclerView.NO_POSITION == lastPosition) {
                    return;
                }

                if (layoutManager.getChildCount() > 0
                        && lastPosition >= layoutManager.getItemCount() - 1) {
                    mLoadMoreFooter.setStatus(ILoadMoreFooter.LOADING);
                    mLoadMoreListener.onLoadMore();
                }
                break;
            default:
                break;
        }
    }

    public interface LoadMoreListener {
        void onLoadMore();
    }

}
