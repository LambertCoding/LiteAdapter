package me.yuu.liteadapter.core;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.SoftReference;

import me.yuu.liteadapter.loadmore.DefaultLoadMoreFooter;
import me.yuu.liteadapter.loadmore.ILoadMoreFooter;
import me.yuu.liteadapter.loadmore.MoreLoader;
import me.yuu.liteadapter.util.Precondition;
import me.yuu.liteadapter.util.Utils;

/**
 * RecyclerView的通用Adapter
 *
 * @author yu
 * @date 2018/1/12
 */
public class LiteAdapter<T> extends AbstractAdapter<T> {

    private static final int VIEW_TYPE_EMPTY = -7061;
    private static final int VIEW_TYPE_LOAD_MORE = -7062;
    private static final int VIEW_TYPE_HEADER_INDEX = -7060;
    private static final int VIEW_TYPE_FOOTER_INDEX = -8060;

    private SoftReference<RecyclerView> mRecyclerView;

    private final SparseArray<View> mHerders;
    private final SparseArray<View> mFooters;
    private final SparseArray<ViewInjector> mViewInjectors;
    private final ViewTypeLinker mViewTypeLinker;
    private final MoreLoader mMoreLoader;
    private final View mEmptyView;
    private final LiteAdapter.OnItemClickListener mOnItemClickListener;
    private final LiteAdapter.OnItemLongClickListener mOnItemLongClickListener;

    private LiteAdapter(Builder builder) {
        this.mMoreLoader = builder.moreLoader;
        this.mEmptyView = builder.emptyView;
        this.mHerders = builder.headers;
        this.mFooters = builder.footers;
        this.mViewTypeLinker = builder.viewTypeLinker;
        this.mViewInjectors = builder.injectors;
        this.mOnItemClickListener = builder.onItemClickListener;
        this.mOnItemLongClickListener = builder.onItemLongClickListener;
    }

    @Override
    protected int adjustNotifyPosition(int position) {
        return position + mHerders.size();
    }

    public T getRealItem(int position) {
        if (isHeader(position) || isFooter(position)) {
            return null;
        }
        return mDataSet.get(position - mHerders.size());
    }

    private boolean isReservedType(int viewType) {
        return viewType == VIEW_TYPE_EMPTY || viewType == VIEW_TYPE_LOAD_MORE
                || isHeaderType(viewType) || isFooterType(viewType);
    }

    private boolean isHeaderType(int viewType) {
        return mHerders.size() > 0 && mHerders.get(viewType) != null;
    }

    private boolean isFooterType(int viewType) {
        return mFooters.size() > 0 && mFooters.get(viewType) != null;
    }

    private boolean isHeader(int position) {
        int headersCount = mHerders.size();
        return headersCount > 0 && position >= 0 && position < headersCount;
    }

    private boolean isFooter(int position) {
        int headerAndDataCount = mHerders.size() + mDataSet.size();
        return mFooters.size() > 0 && position >= headerAndDataCount
                && position < headerAndDataCount + mFooters.size();
    }

    private boolean isEmptyViewEnable() {
        // Disable the empty view if have header view or footer view.
        // not included loadMoreFooter view
        return mEmptyView != null && mDataSet.size() + mHerders.size() + mFooters.size() == 0;
    }

    private boolean isLoadMoreEnable() {
        return mMoreLoader != null && mMoreLoader.isLoadMoreEnable();
    }

    private boolean isLoadMorePosition(int position) {
        return position == mDataSet.size() + mHerders.size() + mFooters.size();
    }

    @Override
    public int getItemCount() {
        if (isEmptyViewEnable()) {
            return 1;
        }
        int itemCount = mDataSet.size() + mHerders.size() + mFooters.size();
        if (isLoadMoreEnable()) {
            itemCount++;
        }
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (isEmptyViewEnable()) {
            return VIEW_TYPE_EMPTY;
        }

        if (isHeader(position)) {
            return mHerders.keyAt(position);
        }

        if (isFooter(position)) {
            return mFooters.keyAt(position - mHerders.size() - mDataSet.size());
        }

        if (isLoadMoreEnable() && isLoadMorePosition(position)) {
            return VIEW_TYPE_LOAD_MORE;
        }

        return getFromInjector(position);
    }

    private int getFromInjector(int position) {
        Precondition.checkState(mViewInjectors.size() != 0, "No view type is registered.");

        if (mViewInjectors.size() > 1) {
            Precondition.checkNotNull(mViewTypeLinker,
                    "Multiple view types are registered. You must set a ViewTypeInjector for LiteAdapter");
            int adjustPosition = position - mHerders.size();
            return mViewTypeLinker.viewType(mDataSet.get(adjustPosition), adjustPosition);
        } else {
            if (mViewTypeLinker != null) {
                Log.i("LiteAdapter", "Single view type don't need ViewTypeLinker,Ignore!");
            }
        }
        return mViewInjectors.keyAt(0);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_EMPTY) {
            return new ViewHolder(mEmptyView);
        } else if (viewType == VIEW_TYPE_LOAD_MORE) {
            return new ViewHolder(mMoreLoader.getLoadMoreFooterView());
        } else if (isHeaderType(viewType)) {
            return new ViewHolder(mHerders.get(viewType));
        } else if (isFooterType(viewType)) {
            return new ViewHolder(mFooters.get(viewType));
        } else {
            ViewInjector injector = mViewInjectors.get(viewType);

            Precondition.checkNotNull(injector, "You haven't registered this view type("
                    + viewType + ") yet . Or you return the wrong view type in ViewTypeLinker.");

            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(injector.getLayoutId(), parent, false);
            return new ViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (isHeader(position) || isFooter(position)
                || isEmptyViewEnable() || isLoadMorePosition(position)) {
            return;
        }

        final Object item = mDataSet.get(position - mHerders.size());
        int viewType = getItemViewType(position);

        Precondition.checkState(!isReservedType(viewType), "You use the reserved view type : " + viewType);

        ViewInjector injector = mViewInjectors.get(viewType);

        Precondition.checkNotNull(injector, "You haven't registered this view type("
                + viewType + ") yet . Or you return the wrong view type in ViewTypeLinker.");

        try {
            injector.bindData(holder, item, position - mHerders.size());
        } catch (ClassCastException e) {
            // 发生这个异常是由于使用多种实体类型的时候,ViewTypeLinker返回了错误的ViewType
            // 比如：注册了一个类型111，实体类型是User：adapter.register(111, new ViewInjector<User>(R.layout.item_user)
            //      注册了一个类型222，实体类型是Student：adapter.register(222, new ViewInjector<Student>(R.layout.item_student)
            // 但是在ViewTypeLinker中，获取到的实体是User，但是返回的条目类型是222，就会出现这个异常；
            throw new IllegalStateException("Returned the wrong view type in ViewTypeLinker."
                    + "position = " + (position - mHerders.size()) + " ViewType = " + viewType
                    + "item class = " + item.getClass().getName());
        }

        setupItemClickListener(holder);
        setupItemLongClickListener(holder);
    }

    private void setupItemClickListener(final ViewHolder viewHolder) {
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    int position = viewHolder.getLayoutPosition() - mHerders.size();
                    mOnItemClickListener.onItemClick(position, mDataSet.get(position));
                }
            }
        });
    }

    private void setupItemLongClickListener(final ViewHolder viewHolder) {
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mOnItemLongClickListener != null) {
                    int position = viewHolder.getLayoutPosition() - mHerders.size();
                    mOnItemLongClickListener.onItemLongClick(position, mDataSet.get(position));
                }
                return true;
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (mRecyclerView == null) {
            mRecyclerView = new SoftReference<>(recyclerView);
        }

        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return (isEmptyViewEnable()
                            || isHeader(position)
                            || isFooter(position)
                            || isLoadMorePosition(position))
                            ? gridManager.getSpanCount() : 1;
                }
            });
        }

        if (mMoreLoader != null) {
            recyclerView.addOnScrollListener(mMoreLoader);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        if (mMoreLoader != null) {
            recyclerView.removeOnScrollListener(mMoreLoader);
        }
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
            int position = holder.getLayoutPosition();
            if (isEmptyViewEnable()
                    || isHeader(position)
                    || isFooter(position)
                    || isLoadMorePosition(position)) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }
    }

    ///////////////////////////////////LoadMore///////////////////////////////////

    @Override
    protected void beforeSetNewData() {
        // setNewData后，notifyDataSetChanged之前回调
        // 设置数据后判断是否占满一页，如果不满一页就不开启load more，反之则开启
        if (mMoreLoader != null && mRecyclerView != null) {
            disableLoadMoreIfNotFullPage(mRecyclerView.get());
        }
    }

    private void disableLoadMoreIfNotFullPage(RecyclerView recyclerView) {
        if (recyclerView == null) {
            return;
        }
        final RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager == null) {
            return;
        }

        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int lastCompletelyVisibleItemPosition = Utils.findLastCompletelyVisibleItemPosition(manager);
                if (lastCompletelyVisibleItemPosition + 1 != getItemCount()) {
                    setLoadMoreEnable(true);
                } else {
                    setLoadMoreEnable(false);
                }
            }
        }, 100);
    }

    public void setLoadMoreEnable(boolean enable) {
        Precondition.checkNotNull(mMoreLoader, "MoreLoader == null, " +
                "You should call enableLoadMore when you build the LiteAdapter.");
        mMoreLoader.setLoadMoreEnable(enable);
        notifyDataSetChanged();
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

    public static class Builder<D> {
        Context context;
        View emptyView;
        MoreLoader moreLoader;
        ViewTypeLinker viewTypeLinker;
        OnItemClickListener onItemClickListener;
        OnItemLongClickListener onItemLongClickListener;
        final SparseArray<View> headers = new SparseArray<>();
        final SparseArray<View> footers = new SparseArray<>();
        final SparseArray<ViewInjector> injectors = new SparseArray<>();

        public Builder(Context context) {
            this.context = Precondition.checkNotNull(context);
        }

        public Builder<D> emptyView(@LayoutRes int layoutId) {
            return emptyView(LayoutInflater.from(context).inflate(layoutId, null));
        }

        public Builder<D> emptyView(View empty) {
            Precondition.checkArgument(emptyView == null, "You have already set a empty view.");
            this.emptyView = Precondition.checkNotNull(empty);

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            emptyView.setLayoutParams(params);
            return this;
        }

        public Builder<D> itemClickListener(@NonNull OnItemClickListener listener) {
            this.onItemClickListener = Precondition.checkNotNull(listener);
            return this;
        }

        public Builder<D> itemLongClickListener(@NonNull OnItemLongClickListener listener) {
            this.onItemLongClickListener = Precondition.checkNotNull(listener);
            return this;
        }

        public Builder<D> viewTypeLinker(@NonNull ViewTypeLinker<D> linker) {
            Precondition.checkArgument(viewTypeLinker == null, "Only one ViewTypeLinker can be registered.");
            this.viewTypeLinker = Precondition.checkNotNull(linker);
            return this;
        }

        public Builder<D> headerView(@NonNull View header) {
            Precondition.checkNotNull(header);

            int headerType = VIEW_TYPE_HEADER_INDEX + headers.size();
            headers.put(headerType, header);

            return this;
        }

        public Builder<D> headerView(@LayoutRes int headerLayout) {
            headerView(LayoutInflater.from(context).inflate(headerLayout, null));
            return this;
        }

        public Builder<D> footerView(@NonNull View footer) {
            Precondition.checkNotNull(footer);

            int footerType = VIEW_TYPE_FOOTER_INDEX + footers.size();
            footers.put(footerType, footer);

            return this;
        }

        public Builder<D> footerView(@LayoutRes int footerLayout) {
            footerView(LayoutInflater.from(context).inflate(footerLayout, null));
            return this;
        }

        public Builder<D> enableLoadMore(@NonNull MoreLoader.LoadMoreListener loadMoreListener) {
            return enableLoadMore(loadMoreListener, new DefaultLoadMoreFooter(context));
        }

        public Builder<D> enableLoadMore(@NonNull MoreLoader.LoadMoreListener loadMoreListener,
                                         @NonNull ILoadMoreFooter loadMoreFooter) {
            Precondition.checkArgument(this.moreLoader == null,
                    "You have already called enableLoadMore, Don't call again!");
            Precondition.checkNotNull(loadMoreListener);
            Precondition.checkNotNull(loadMoreFooter);

            this.moreLoader = new MoreLoader(loadMoreListener, loadMoreFooter);
            return this;
        }

        public <T> Builder<D> register(int viewType, @NonNull ViewInjector<T> injector) {
            Precondition.checkNotNull(injector);
            Precondition.checkState(injectors.get(viewType) == null,
                    "You have already registered this viewType:" + viewType);
            injectors.put(viewType, injector);
            return this;
        }

        public LiteAdapter<D> create() {
            return new LiteAdapter<>(this);
        }
    }
}