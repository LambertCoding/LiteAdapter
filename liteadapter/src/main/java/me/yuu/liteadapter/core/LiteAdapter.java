package me.yuu.liteadapter.core;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.util.Preconditions;
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
import me.yuu.liteadapter.util.Utils;

/**
 * RecyclerView的通用Adapter
 *
 * @author yu
 * @date 2018/1/12
 */
@SuppressWarnings("all")
public class LiteAdapter<T> extends AbstractAdapter<T> {

    public static final int VIEW_TYPE_EMPTY = -7061;
    public static final int VIEW_TYPE_HEADER_INDEX = -7060;
    public static final int VIEW_TYPE_FOOTER_INDEX = -8060;

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
    public int getHeadersCount() {
        return mHerders == null ? 0 : mHerders.size();
    }

    @Override
    public int getFootersCount() {
        if (mFooters == null) {
            return 0;
        } else {
            if (mMoreLoader != null
                    && !mMoreLoader.isLoadMoreEnable()
                    && mMoreLoader.isAddLoadMoreFooter()) {
                return mFooters.size() - 1;
            }
        }
        return mFooters.size();
    }

    /**
     * @param viewType type
     * @return is Reserved view type
     */
    private boolean isReservedType(int viewType) {
        return viewType == VIEW_TYPE_EMPTY || isHeaderType(viewType) || isFooterType(viewType);
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
        return mFooters.size() > 0 && position >= mHerders.size() + mDataSet.size();
    }

    private boolean isEmptyViewEnable() {
        // Disable the empty view if have header view or footer view.
        // not included loadMoreFooter view
        int itemCount = mDataSet.size() + mHerders.size() + mFooters.size();
        if (mMoreLoader != null && mMoreLoader.isAddLoadMoreFooter()) {
            itemCount -= 1;
        }
        return mEmptyView != null && itemCount == 0;
    }

    @Override
    public int getItemCount() {
        if (isEmptyViewEnable()) {
            return 1;
        }
        int itemCount = mDataSet.size() + mHerders.size() + mFooters.size();
        if (mMoreLoader != null
                && !mMoreLoader.isLoadMoreEnable()
                && mMoreLoader.isAddLoadMoreFooter()) {
            itemCount -= 1;
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

        return getFromInjector(position);
    }

    private int getFromInjector(int position) {
        if (mViewInjectors.size() == 0) {
            throw new NullPointerException("No view type is registered.");
        }

        if (mViewInjectors.size() > 1) {
            Preconditions.checkNotNull(mViewTypeLinker,
                    "Multiple view types are registered. You must set a ViewTypeInjector for LiteAdapter");

            return mViewTypeLinker.viewType(getRealItem(position), position - mHerders.size());
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
        } else if (isHeaderType(viewType)) {
            return new ViewHolder(mHerders.get(viewType));
        } else if (isFooterType(viewType)) {
            return new ViewHolder(mFooters.get(viewType));
        } else {
            ViewInjector injector = mViewInjectors.get(viewType);

            Preconditions.checkNotNull(injector, "You haven't registered this view type(" + viewType
                    + ") yet . Or you return the wrong view type in ViewTypeLinker.");

            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(injector.getLayoutId(), parent, false);
            return new ViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (isHeader(position) || isFooter(position) || isEmptyViewEnable()) {
            return;
        }

        final Object item = getRealItem(position);
        int viewType = getItemViewType(position);

        if (isReservedType(viewType)) {
            throw new IllegalStateException("You use the reserved view type : " + viewType);
        }

        ViewInjector injector = mViewInjectors.get(viewType);

        Preconditions.checkNotNull(injector, "You haven't registered this view type(" + viewType
                + ") yet . Or you return the wrong view type in ViewTypeLinker.");

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

    public T getRealItem(int position) {
        if (isHeader(position) || isFooter(position)) {
            return null;
        }
        return mDataSet.get(position - mHerders.size());
    }

    private void setupItemClickListener(final ViewHolder viewHolder) {
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    int position = viewHolder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(position - mHerders.size(), getRealItem(position));
                }
            }
        });
    }

    private void setupItemLongClickListener(final ViewHolder viewHolder) {
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mOnItemLongClickListener != null) {
                    int position = viewHolder.getLayoutPosition();
                    mOnItemLongClickListener.onItemLongClick(position - mHerders.size(), getRealItem(position));
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
                            || isFooter(position))
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
            if (isEmptyViewEnable()
                    || isHeader(holder.getLayoutPosition())
                    || isFooter(holder.getLayoutPosition())) {
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

    public void disableLoadMoreIfNotFullPage(RecyclerView recyclerView) {
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
        Preconditions.checkNotNull(mMoreLoader, "MoreLoader == null, " +
                "You should call enableLoadMore when you build the LiteAdapter.");

        mMoreLoader.setLoadMoreEnable(enable);
        if (enable) {
            if (!mMoreLoader.isAddLoadMoreFooter()) {
                int footerType = VIEW_TYPE_FOOTER_INDEX + mFooters.size();
                mFooters.put(footerType, mMoreLoader.getLoadMoreFooterView());
                mMoreLoader.setAddLoadMoreFooter(true);
            }
        } else {
            // load more footer is aways the last one, remove it.
            if (mMoreLoader.isAddLoadMoreFooter()) {
                mFooters.removeAt(mFooters.size() - 1);
                mMoreLoader.setAddLoadMoreFooter(false);
            }
        }
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
        public Context context;
        public View emptyView;
        public MoreLoader moreLoader;
        public ViewTypeLinker viewTypeLinker;
        public OnItemClickListener onItemClickListener;
        public OnItemLongClickListener onItemLongClickListener;
        public final SparseArray<View> headers = new SparseArray<>();
        public final SparseArray<View> footers = new SparseArray<>();
        public final SparseArray<ViewInjector> injectors = new SparseArray<>();

        public Builder(Context context) {
            this.context = Preconditions.checkNotNull(context);
        }

        public Builder<D> emptyView(@LayoutRes int layoutId) {
            return emptyView(LayoutInflater.from(context).inflate(layoutId, null));
        }

        public Builder<D> emptyView(View empty) {
            Preconditions.checkArgument(emptyView == null, "You have already set a empty view.");
            this.emptyView = Preconditions.checkNotNull(empty);

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            emptyView.setLayoutParams(params);
            return this;
        }

        public Builder<D> itemClickListener(@NonNull OnItemClickListener listener) {
            this.onItemClickListener = Preconditions.checkNotNull(listener);
            return this;
        }

        public Builder<D> itemLongClickListener(@NonNull OnItemLongClickListener listener) {
            this.onItemLongClickListener = Preconditions.checkNotNull(listener);
            return this;
        }

        public Builder<D> viewTypeLinker(@NonNull ViewTypeLinker<D> linker) {
            Preconditions.checkArgument(viewTypeLinker == null, "Only one ViewTypeLinker can be registered.");
            this.viewTypeLinker = Preconditions.checkNotNull(linker);
            return this;
        }

        public Builder<D> headerView(@NonNull View header) {
            Preconditions.checkNotNull(header);

            int headerType = VIEW_TYPE_HEADER_INDEX + headers.size();
            headers.put(headerType, header);

            return this;
        }

        public Builder<D> headerView(@LayoutRes int headerLayout) {
            headerView(LayoutInflater.from(context).inflate(headerLayout, null));
            return this;
        }

        public Builder<D> footerView(@NonNull View footer) {
            Preconditions.checkNotNull(footer);

            if (moreLoader != null && moreLoader.isAddLoadMoreFooter()) {
                // Make sure that loadMoreFooterView is always the last footer.
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

        public Builder<D> footerView(@LayoutRes int footerLayout) {
            footerView(LayoutInflater.from(context).inflate(footerLayout, null));
            return this;
        }

        public Builder<D> enableLoadMore(@NonNull MoreLoader.LoadMoreListener loadMoreListener) {
            return enableLoadMore(loadMoreListener, new DefaultLoadMoreFooter(context));
        }

        public Builder<D> enableLoadMore(@NonNull MoreLoader.LoadMoreListener loadMoreListener, @NonNull ILoadMoreFooter loadMoreFooter) {
            Preconditions.checkArgument(this.moreLoader == null,
                    "You have already called enableLoadMore, Don't call again!");
            Preconditions.checkNotNull(loadMoreListener);
            Preconditions.checkNotNull(loadMoreFooter);

            footerView(loadMoreFooter.getView());
            this.moreLoader = new MoreLoader(loadMoreListener, loadMoreFooter);
            this.moreLoader.setAddLoadMoreFooter(true);
            return this;
        }

        public <T> Builder<D> register(int viewType, @NonNull ViewInjector<T> injector) {
            Preconditions.checkNotNull(injector);
            Preconditions.checkState(injectors.get(viewType) == null,
                    "You have already registered this viewType:" + viewType);
            injectors.put(viewType, injector);
            return this;
        }

        public LiteAdapter<D> create() {
            return new LiteAdapter<D>(this);
        }
    }
}