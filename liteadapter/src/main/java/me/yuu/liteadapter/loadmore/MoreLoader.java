package me.yuu.liteadapter.loadmore;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import me.yuu.liteadapter.util.LiteAdapterUtils;

/**
 * @author yu
 * @date 2018/1/12
 */
public class MoreLoader extends RecyclerView.OnScrollListener {

    private boolean isLoadMoreEnable;
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

    public boolean isLoadMoreEnable() {
        return isLoadMoreEnable;
    }

    public void setLoadMoreEnable(boolean enable) {
        this.isLoadMoreEnable = enable;
    }

    public View getLoadMoreFooterView() {
        return mLoadMoreFooter.getView();
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
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (!isLoadMoreEnable) {
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
                if (layoutManager == null) return;

                int lastPosition = LiteAdapterUtils.findLastCompletelyVisibleItemPosition(layoutManager);

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
