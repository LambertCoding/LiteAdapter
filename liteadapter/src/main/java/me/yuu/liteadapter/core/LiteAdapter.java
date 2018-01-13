package me.yuu.liteadapter.core;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import me.yuu.liteadapter.loadmore.DefaultLoadMoreFooter;
import me.yuu.liteadapter.loadmore.ILoadMoreFooter;
import me.yuu.liteadapter.loadmore.MoreLoader;
import me.yuu.liteadapter.util.Utils;

/**
 * RecyclerView的通用Adapter
 *
 * @author yu
 * @date 2018/1/12
 */
@SuppressWarnings("all")
public class LiteAdapter extends RecyclerView.Adapter<ViewHolder> implements DataOperator {

    public static final int VIEW_TYPE_EMPTY = -7061;
    public static final int VIEW_TYPE_HEADER_INDEX = -7060;
    public static final int VIEW_TYPE_FOOTER_INDEX = -8060;

    private final ItemManager mItemManager;
    private final MoreLoader mMoreLoader;

    private LiteAdapter(Builder builder) {
        this.mItemManager = ItemManager.create(builder);
        this.mMoreLoader = builder.moreLoader;
    }

    @Override
    public int getItemCount() {
        return mItemManager.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return mItemManager.getItemViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return mItemManager.createViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mItemManager.bindViewHolder(holder, position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return (mItemManager.isEmptyViewEnable()
                            || mItemManager.isHeader(position)
                            || mItemManager.isFooter(position))
                            ? gridManager.getSpanCount() : 1;
                }
            });
        }

        if (mMoreLoader != null) {
            recyclerView.addOnScrollListener(mMoreLoader);
        }
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
            if (mItemManager.isEmptyViewEnable()
                    || mItemManager.isHeader(holder.getLayoutPosition())
                    || mItemManager.isFooter(holder.getLayoutPosition())) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }
    }

    ///////////////////////////////////LoadMore////////////////////////////////

    public void setLoadMoreEnable(boolean enable) {
        if (mMoreLoader == null) {
            if (enable)
                throw new NullPointerException("MoreLoader == null, " +
                        "You should call enableLoadMore when you build the LiteAdapter.");
        } else {
            mMoreLoader.setEnable(enable);
        }
    }

    public void disableLoadMoreIfNotFullPage(RecyclerView recyclerView) {
        if (mMoreLoader == null || recyclerView == null) {
            return;
        }
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager == null) {
            return;
        }
        mMoreLoader.setEnable(false);
        if (manager instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) manager;
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if ((linearLayoutManager.findLastCompletelyVisibleItemPosition() + 1) != getItemCount()) {
                        mMoreLoader.setEnable(true);
                    }
                }
            }, 50);
        } else if (manager instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) manager;
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final int[] positions = new int[staggeredGridLayoutManager.getSpanCount()];
                    staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(positions);
                    int pos = Utils.findMax(positions) + 1;
                    if (pos != getItemCount()) {
                        mMoreLoader.setEnable(true);
                    }
                }
            }, 50);
        }
    }

    public void loadMoreCompleted() {
        if (mMoreLoader != null) {
            mMoreLoader.loadMoreCompleted();
        }
    }

    public void loadMoreError() {
        if (mMoreLoader != null) {
            mMoreLoader.loadMoreError();
        }
    }

    public void noMore() {
        if (mMoreLoader != null) {
            mMoreLoader.noMore();
        }
    }

    ///////////////////////////////////DataOperator////////////////////////////////

    public List getDataSet() {
        return mItemManager.getDataSet();
    }

    @Override
    public void addData(@NonNull Object item) {
        if (item != null) {
            int position = mItemManager.getHeadersSize() + getDataSet().size();
            getDataSet().add(item);
            notifyItemInserted(position);
        }
    }

    @Override
    public void addData(@IntRange(from = 0) int position, @NonNull Object item) {
        if (item != null) {
            getDataSet().add(position, item);
            notifyItemInserted(mItemManager.getHeadersSize() + position);
        }
    }

    @Override
    public void addDataToHead(@NonNull Object item) {
        addData(0, item);
    }

    @Override
    public void addAll(@NonNull List items) {
        if (items != null && !items.isEmpty()) {
            int startPosition = mItemManager.getHeadersSize() + getDataSet().size();
            getDataSet().addAll(items);
            notifyItemRangeInserted(startPosition, items.size());
        }
    }

    @Override
    public void addAll(@IntRange(from = 0) int position, List items) {
        if (items != null && !items.isEmpty()) {
            int startPosition = mItemManager.getHeadersSize() + position;
            getDataSet().addAll(items);
            notifyItemRangeInserted(startPosition, items.size());
        }
    }

    @Override
    public void addAllToHead(@NonNull List items) {
        addAll(0, items);
    }

    @Override
    public void remove(@IntRange(from = 0) int position) {
        getDataSet().remove(position);
        if (getDataSet().isEmpty()) {
            notifyDataSetChanged();
        } else {
            notifyItemRemoved(mItemManager.getHeadersSize() + position);
        }
    }

    @Override
    public void clear() {
        getDataSet().clear();
        notifyDataSetChanged();
    }

    @Override
    public void setNewData(List items) {
        getDataSet().clear();
        if (items != null && !items.isEmpty()) {
            getDataSet().addAll(items);
        }
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(@IntRange(from = 0) int position) {
        return getDataSet().get(position);
    }

    @Override
    public void modify(@IntRange(from = 0) int position, Object newData) {
        if (newData == null) {
            return;
        }
        getDataSet().set(position, newData);
        notifyItemChanged(mItemManager.getHeadersSize() + position);
    }

    @Override
    public void modify(@IntRange(from = 0) int position, Action action) {
        action.doAction(getDataSet().get(position));
        notifyItemChanged(mItemManager.getHeadersSize() + position);
    }

    public interface OnItemClickListener {
        void onItemClick(int position, Object item);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, Object item);
    }

    public static final class Builder {
        public Context context;
        public View emptyView;
        public MoreLoader moreLoader;
        public ViewTypeLinker viewTypeLinker;
        public OnItemClickListener onItemClickListener;
        public OnItemLongClickListener onItemLongClickListener;
        public final SparseArray<View> herders = new SparseArray<>();
        public final SparseArray<View> footers = new SparseArray<>();
        public final SparseArray<ViewInjector> injectors = new SparseArray<>();

        public Builder(Context context) {
            this.context = context;
        }

        public Builder emptyView(View empty) {
            if (emptyView != null) {
                throw new IllegalStateException("You have already set a empty view.");
            }
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            empty.setLayoutParams(params);
            this.emptyView = empty;
            return this;
        }

        public Builder emptyView(Context context, @LayoutRes int layoutId) {
            return emptyView(LayoutInflater.from(context).inflate(layoutId, null));
        }

        public Builder itemClickListener(@NonNull OnItemClickListener listener) {
            this.onItemClickListener = listener;
            return this;
        }

        public Builder itemLongClickListener(@NonNull OnItemLongClickListener listener) {
            this.onItemLongClickListener = listener;
            return this;
        }

        public Builder viewTypeLinker(@NonNull ViewTypeLinker linker) {
            if (viewTypeLinker != null) {
                throw new IllegalStateException("Only one ViewTypeLinker can be registered.");
            }
            this.viewTypeLinker = linker;
            return this;
        }

        public Builder headerView(@NonNull View header) {
            if (header == null) {
                throw new IllegalArgumentException("the header == null.");
            }

            int headerType = VIEW_TYPE_HEADER_INDEX + herders.size();
            herders.put(headerType, header);
            return this;
        }

        public Builder footerView(@NonNull View footer) {
            if (footer == null) {
                throw new IllegalArgumentException("the footer == null.");
            }

            if (moreLoader != null) {
                int key = footers.keyAt(footers.size() - 1);
                View loadMoreFooter = footers.valueAt(footers.size() - 1);

                footers.put(key, footer);

                int footerType = VIEW_TYPE_FOOTER_INDEX + footers.size();
                footers.put(footerType, loadMoreFooter);
            } else {

                int footerType = VIEW_TYPE_FOOTER_INDEX + footers.size();
                footers.put(footerType, footer);
            }

            return this;
        }

        public Builder enableLoadMore(@NonNull MoreLoader.LoadMoreListener loadMoreListener) {
            return enableLoadMore(loadMoreListener, new DefaultLoadMoreFooter(context));
        }

        public Builder enableLoadMore(@NonNull MoreLoader.LoadMoreListener loadMoreListener, @NonNull ILoadMoreFooter loadMoreFooter) {
            if (moreLoader != null) {
                throw new IllegalStateException("You have already called enableLoadMore, Don't call again!");
            }

            footerView(loadMoreFooter.getView());
            moreLoader = new MoreLoader(loadMoreListener, loadMoreFooter);
            return this;
        }

        public <T> Builder register(int viewType, @NonNull ViewInjector<T> injector) {
            if (injector == null) {
                throw new IllegalArgumentException("the injector == null.");
            }
            if (injectors.get(viewType) == null) {
                injectors.put(viewType, injector);
            } else {
                throw new IllegalArgumentException("You have registered this viewType:" + viewType);
            }
            return this;
        }

        public LiteAdapter create() {
            return new LiteAdapter(this);
        }
    }
}