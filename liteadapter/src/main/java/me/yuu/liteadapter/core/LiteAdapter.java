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
public class LiteAdapter extends AbstractAbapter {

    public static final int VIEW_TYPE_EMPTY = -7061;
    public static final int VIEW_TYPE_HEADER_INDEX = -7060;
    public static final int VIEW_TYPE_FOOTER_INDEX = -8060;

    private final SparseArray<View> mHerders;
    private final SparseArray<View> mFooters;
    private final SparseArray<ViewInjector> mViewInjectors;
    private final ViewTypeLinker mViewTypeLinker;
    private final MoreLoader mMoreLoader;
    private final View mEmptyView;
    private final LiteAdapter.OnItemClickListener mOnItemClickListener;
    private final LiteAdapter.OnItemLongClickListener mOnItemLongClickListener;

    private boolean hasAddLoadMoreFooter;

    private LiteAdapter(Builder builder) {
        this.mMoreLoader = builder.moreLoader;
        this.mEmptyView = builder.emptyView;
        this.mHerders = builder.herders;
        this.mFooters = builder.footers;
        this.mViewTypeLinker = builder.viewTypeLinker;
        this.mViewInjectors = builder.injectors;
        this.mOnItemClickListener = builder.onItemClickListener;
        this.mOnItemLongClickListener = builder.onItemLongClickListener;
        this.hasAddLoadMoreFooter = builder.hasAddLoadMoreFooter;
    }

    @Override
    public int getHeadersCount() {
        return mHerders == null ? 0 : mHerders.size();
    }

    @Override
    public int getFootersCount() {
        return mFooters == null ? 0 : mFooters.size();
    }

    /**
     * @param viewType type
     * @return is Reserved view type
     */
    private boolean isReservedType(int viewType) {
        return viewType == VIEW_TYPE_EMPTY || isHeaderType(viewType) || isFooterType(viewType);
    }

    private boolean isHeaderType(int viewType) {
        return mHerders != null && mHerders.size() > 0 && mHerders.get(viewType) != null;
    }

    private boolean isFooterType(int viewType) {
        return mFooters != null && mFooters.size() > 0 && mFooters.get(viewType) != null;
    }

    private boolean isHeader(int position) {
        return mHerders != null && mHerders.size() > 0 && position >= 0 && position < mHerders.size();
    }

    private boolean isFooter(int position) {
        return mFooters != null && mFooters.size() > 0 && position >= mHerders.size() + mDataSet.size();
    }

    private boolean isEmptyViewEnable() {
        return mEmptyView != null && mDataSet.size() == 0;
    }

    @Override
    public int getItemCount() {
        if (isEmptyViewEnable()) {
            return 1;
        }
        return mDataSet.size() + mHerders.size() + mFooters.size();
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
            if (mViewTypeLinker == null) {
                throw new NullPointerException("Multiple view types are registered. You must set a ViewTypeInjector for LiteAdapter");
            } else {
                return mViewTypeLinker.viewType(getRealItem(position), position - mHerders.size());
            }
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
            if (injector == null) {
                throw new NullPointerException("You haven't registered this viewType yet : " + viewType
                        + ". Or you return the wrong value in the ViewTypeLinker.");
            }
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
        if (injector == null) {
            throw new NullPointerException("You haven't registered this view type(" + viewType
                    + ") yet . Or you return the wrong view type  in ViewTypeLinker.");
        }

        try {
            injector.bindData(holder, item, position - getHeadersCount());
        } catch (ClassCastException e) {
            // 发生这个异常是由于使用多种实体类型的时候,ViewTypeLinker返回了错误的ViewType
            // 比如：注册了一个类型111，实体类型是User：adapter.register(111, new ViewInjector<User>(R.layout.item_user)
            //      注册了一个类型222，实体类型是Student：adapter.register(222, new ViewInjector<Student>(R.layout.item_student)
            // 但是在ViewTypeLinker中，获取到的实体是User，但是返回的条目类型是222，就会出现这个异常；
            throw new IllegalStateException("Returned the wrong view type in ViewTypeLinker."
                    + "position = " + (position - getHeadersCount()) + " ViewType = " + viewType
                    + "item class = " + item.getClass().getName());
        }

        setupItemClickListener(holder);
        setupItemLongClickListener(holder);
    }

    private Object getRealItem(int position) {
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
                    mOnItemClickListener.onItemClick(position - mHerders.size(), getItem(position));
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
                    mOnItemLongClickListener.onItemLongClick(position - mHerders.size(), getItem(position));
                }
                return true;
            }
        });
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
        if (mMoreLoader == null) {
            throw new NullPointerException("MoreLoader == null, " +
                    "You should call enableLoadMore when you build the LiteAdapter.");
        }

        mMoreLoader.setEnable(enable);
        if (enable) {
            if (!hasAddLoadMoreFooter) {
                int footerType = VIEW_TYPE_FOOTER_INDEX + mFooters.size();
                mFooters.put(footerType, mMoreLoader.getLoadMoreFooterView());
                hasAddLoadMoreFooter = true;
                notifyDataSetChanged();
            }
        } else {
            // load more footer is aways the last one, remove it.
            if (hasAddLoadMoreFooter) {
                mFooters.removeAt(mFooters.size() - 1);
                hasAddLoadMoreFooter = false;
                notifyDataSetChanged();
            }
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

    public static class Builder {
        public Context context;
        public View emptyView;
        public MoreLoader moreLoader;
        public ViewTypeLinker viewTypeLinker;
        public OnItemClickListener onItemClickListener;
        public OnItemLongClickListener onItemLongClickListener;
        public final SparseArray<View> herders = new SparseArray<>();
        public final SparseArray<View> footers = new SparseArray<>();
        public final SparseArray<ViewInjector> injectors = new SparseArray<>();

        public boolean hasAddLoadMoreFooter;

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

            if (hasAddLoadMoreFooter) {
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
            if (this.moreLoader != null) {
                throw new IllegalStateException("You have already called enableLoadMore, Don't call again!");
            }
            footerView(loadMoreFooter.getView());
            this.hasAddLoadMoreFooter = true;
            this.moreLoader = new MoreLoader(loadMoreListener, loadMoreFooter);
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