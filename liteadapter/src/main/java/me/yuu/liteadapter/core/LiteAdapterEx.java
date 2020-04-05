package me.yuu.liteadapter.core;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.lang.ref.WeakReference;

import me.yuu.liteadapter.diff.DefaultDiffCallback;
import me.yuu.liteadapter.diff.LiteDiffUtil;
import me.yuu.liteadapter.loadmore.DefaultLoadMoreFooter;
import me.yuu.liteadapter.loadmore.ILoadMoreFooter;
import me.yuu.liteadapter.loadmore.MoreLoader;
import me.yuu.liteadapter.util.LiteAdapterUtils;
import me.yuu.liteadapter.util.Precondition;

import static me.yuu.liteadapter.util.LiteAdapterUtils.generateLayoutParamsForHeaderAndFooter;

/**
 * RecyclerView的通用Adapter
 *
 * @author yu
 * @date 2018/1/12
 */
public class LiteAdapterEx<T> extends LiteAdapter<T> {

    private static final int VIEW_TYPE_EMPTY = -7061;
    private static final int VIEW_TYPE_LOAD_MORE = -7062;
    private static final int VIEW_TYPE_HEADER_INDEX = -7060;
    private static final int VIEW_TYPE_FOOTER_INDEX = -8060;

    private WeakReference<RecyclerView> mRecyclerView;
    private int mOrientation;

    /**
     * key: viewType    value: headerView
     */
    private SparseArray<View> mHerders;
    /**
     * key: viewType    value: footerView
     */
    private SparseArray<View> mFooters;

    private final MoreLoader mMoreLoader;
    private final View mEmptyView;

    /**
     * 空布局是否要保持header和footer
     */
    private boolean mIsKeepHeaderAndFooter;

    public LiteAdapterEx(
            MoreLoader moreLoader,
            View emptyView,
            boolean keepHeaderAndFooter,
            SparseArray<View> headers,
            SparseArray<View> footers,
            InjectorFinder<T> injectorFinder,
            SparseArray<ViewInjector<T>> injectors,
            LiteDiffUtil.Callback diffCallback,
            LiteAdapterEx.OnItemClickListener onItemClickListener,
            LiteAdapterEx.OnItemLongClickListener onItemLongClickListener
    ) {
        super(injectors, injectorFinder, diffCallback, onItemClickListener, onItemLongClickListener);
        this.mMoreLoader = moreLoader;
        this.mEmptyView = emptyView;
        this.mHerders = headers;
        this.mIsKeepHeaderAndFooter = keepHeaderAndFooter;
        this.mFooters = footers;
    }

    public LiteAdapterEx<T> copyFrom(LiteAdapterEx<T> adapter) {
        return new LiteAdapterEx<>(
                adapter.getMoreLoader(),
                adapter.getEmptyView(),
                adapter.isKeepHeaderAndFooter(),
                adapter.getHerders(),
                adapter.getFooters(),
                adapter.getInjectorFinder(),
                adapter.getViewInjectors(),
                adapter.getDiffCallback(),
                adapter.getOnItemClickListener(),
                adapter.getOnItemLongClickListener()
        );
    }

    @Override
    public int getItemCount() {
        int itemCount = mDataSet.size() + mHerders.size() + mFooters.size();

        if (isEmptyViewEnable()) {
            if (mIsKeepHeaderAndFooter) {
                itemCount++;
            } else {
                itemCount = 1;
            }
        } else {
            // 数据不为空，loadMore可用则+1
            if (isLoadMoreEnable())
                itemCount++;
        }
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (isEmptyViewPosition(position)) {
            return VIEW_TYPE_EMPTY;
        }

        if (isHeader(position)) {
            return mHerders.keyAt(position);
        }

        if (isFooter(position)) {
            int index = position - mHerders.size() - mDataSet.size();
            if (isEmptyViewEnable()) index--;
            return mFooters.keyAt(index);
        }

        if (isLoadMoreEnable() && isLoadMorePosition(position)) {
            return VIEW_TYPE_LOAD_MORE;
        }

        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_EMPTY) {
            return new ViewHolder(mEmptyView);
        } else if (viewType == VIEW_TYPE_LOAD_MORE) {
            return new ViewHolder(mMoreLoader.getLoadMoreFooterView());
        } else if (isHeaderType(viewType)) {
            View view = mHerders.get(viewType);
            view.setLayoutParams(generateLayoutParamsForHeaderAndFooter(mOrientation, view));
            return new ViewHolder(view);
        } else if (isFooterType(viewType)) {
            View view = mFooters.get(viewType);
            view.setLayoutParams(generateLayoutParamsForHeaderAndFooter(mOrientation, view));
            return new ViewHolder(view);
        } else {
            return super.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (isHeader(position) || isFooter(position)
                || isEmptyViewPosition(position) || isLoadMorePosition(position)) {
            return;
        }
        super.onBindViewHolder(holder, position);
    }


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (mRecyclerView == null || mRecyclerView.get() == null) {
            mRecyclerView = new WeakReference<>(recyclerView);
        }

        initOrientation(recyclerView.getLayoutManager());

        setSpanSizeLookup4Grid(recyclerView);

        if (mMoreLoader != null && mFooters.size() == 0) {
            recyclerView.addOnScrollListener(mMoreLoader);
        }
    }

    private void setSpanSizeLookup4Grid(@NonNull RecyclerView recyclerView) {
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return (isEmptyViewPosition(position)
                            || isHeader(position)
                            || isFooter(position)
                            || isLoadMorePosition(position)) ? gridManager.getSpanCount() : 1;
                }
            });
        }
    }

    private void initOrientation(RecyclerView.LayoutManager manager) {
        if (manager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) manager;
            this.mOrientation = linearLayoutManager.getOrientation();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) manager;
            this.mOrientation = staggeredGridLayoutManager.getOrientation();
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        if (mMoreLoader != null)
            recyclerView.removeOnScrollListener(mMoreLoader);
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        setSpanSizeLookup4StaggeredGrid(holder);
    }

    private void setSpanSizeLookup4StaggeredGrid(@NonNull ViewHolder holder) {
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
            int position = holder.getLayoutPosition();
            if (isEmptyViewPosition(position)
                    || isHeader(position)
                    || isFooter(position)
                    || isLoadMorePosition(position)) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }
    }

    public void addFooter(View footer, boolean notify) {
        Precondition.checkNotNull(footer);
        mFooters.put(VIEW_TYPE_FOOTER_INDEX + mFooters.size(), footer);
        if (notify) notifyDataSetChanged();
    }

    public void removeFooter(int footerPosition, boolean notify) {
        removeAndOptimizeIndex(false, footerPosition, notify);
    }

    public void addHeader(View header, boolean notify) {
        Precondition.checkNotNull(header);
        mHerders.put(VIEW_TYPE_HEADER_INDEX + mHerders.size(), header);
        if (notify) notifyDataSetChanged();
    }

    public void removeHeader(int headerPosition, boolean notify) {
        removeAndOptimizeIndex(true, headerPosition, notify);
    }

    private void removeAndOptimizeIndex(boolean isHeader, int position, boolean notify) {
        SparseArray<View> target = isHeader ? mHerders : mFooters;
        Precondition.checkArgument(position >= 0 && position < target.size(),
                "Invalid position = " + position
                        + (isHeader ? ", mHeaders.Size() = " : ", mFooters.Size() = ") + target.size());

        // 因为header和footer的type都是按照添加的顺序自动生成的
        // 所以删除指定位置的header和footer后，需要重新优化key，否则再次addHeader或者addFooter可能会出错
        SparseArray<View> temp = new SparseArray<>();
        int viewTypeIndex = isHeader ? VIEW_TYPE_HEADER_INDEX : VIEW_TYPE_FOOTER_INDEX;

        for (int i = 0; i < target.size(); i++) {
            if (i == position) {
                continue;
            }
            temp.put(viewTypeIndex + temp.size(), target.valueAt(i));
        }

        if (isHeader) {
            mHerders = temp;
        } else {
            mFooters = temp;
        }

        if (notify) notifyDataSetChanged();
    }

    ///////////////////////////////////LoadMore///////////////////////////////////

    @Override
    protected void beforeUpdateData() {
        // setNewData后，notifyDataSetChanged之前回调
        // 设置数据后判断是否占满一页，如果不满一页就不开启load more，反之则开启
        if (mMoreLoader != null && mRecyclerView != null) {
            disableLoadMoreIfNotFullPage(mRecyclerView.get());
        }
    }

    private void disableLoadMoreIfNotFullPage(RecyclerView recyclerView) {
        if (recyclerView == null) return;

        final RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();

        if (manager == null) return;

        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int position = LiteAdapterUtils.findLastCompletelyVisibleItemPosition(manager);
                if (position + 1 != getItemCount()) {
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

    @Override
    public int adjustNotifyPosition(int position) {
        return position + mHerders.size();
    }

    @Override
    public int adjustGetItemPosition(int position) {
        return position - mHerders.size();
    }

    @Override
    protected boolean isReservedType(int viewType) {
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
        return position >= 0 && position < headersCount;
    }

    private boolean isFooter(int position) {
        int headerAndDataCount = mHerders.size() + mDataSet.size();
        if (isEmptyViewEnable()) {
            headerAndDataCount++;
        }
        return mFooters.size() > 0 && position >= headerAndDataCount
                && position < headerAndDataCount + mFooters.size();
    }

    private boolean isEmptyViewPosition(int position) {
        if (isEmptyViewEnable()) {
            if (mIsKeepHeaderAndFooter)
                return mHerders.size() == position;
            else
                return position == 0;
        }
        return false;
    }

    public boolean isEmptyViewEnable() {
        return mEmptyView != null && mDataSet.size() == 0;
    }

    public boolean isLoadMoreEnable() {
        return mMoreLoader != null && mMoreLoader.isLoadMoreEnable() && mFooters.size() == 0;
    }

    private boolean isLoadMorePosition(int position) {
        return position == mDataSet.size() + mHerders.size();
    }

    public SparseArray<View> getHerders() {
        return mHerders;
    }

    public SparseArray<View> getFooters() {
        return mFooters;
    }

    public MoreLoader getMoreLoader() {
        return mMoreLoader;
    }

    public View getEmptyView() {
        return mEmptyView;
    }

    public boolean isKeepHeaderAndFooter() {
        return mIsKeepHeaderAndFooter;
    }

    ////////////////////////////////////////////////////////////////////////////

    public static class Builder<D> {
        private Context context;
        private View emptyView;
        private MoreLoader moreLoader;
        private boolean keepHeaderAndFooter = true;
        private InjectorFinder<D> injectorFinder;
        private LiteDiffUtil.Callback diffCallback = new DefaultDiffCallback();
        private OnItemClickListener onItemClickListener;
        private OnItemLongClickListener onItemLongClickListener;
        private final SparseArray<View> headers = new SparseArray<>();
        private final SparseArray<View> footers = new SparseArray<>();
        private final SparseArray<ViewInjector<D>> injectors = new SparseArray<>();

        public Builder(Context context) {
            this.context = Precondition.checkNotNull(context);
        }

        public Builder<D> autoDiff(LiteDiffUtil.Callback diffCallback) {
            this.diffCallback = diffCallback;
            return this;
        }

        public Builder<D> emptyView(@LayoutRes int layoutId) {
            return emptyView(LayoutInflater.from(context).inflate(layoutId, null));
        }

        public Builder<D> keepHeaderAndFooter(boolean keep) {
            this.keepHeaderAndFooter = keep;
            return this;
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

        public Builder<D> injectorFinder(@NonNull InjectorFinder<D> finder) {
            Precondition.checkArgument(injectorFinder == null, "Only one InjectorFinder can be registered.");
            this.injectorFinder = Precondition.checkNotNull(finder);
            return this;
        }

        public Builder<D> headerView(@NonNull View header) {
            Precondition.checkNotNull(header);

            int headerType = VIEW_TYPE_HEADER_INDEX + headers.size();
            headers.put(headerType, header);

            return this;
        }

        public Builder<D> footerView(@NonNull View footer) {
            Precondition.checkNotNull(footer);

            int footerType = VIEW_TYPE_FOOTER_INDEX + footers.size();
            footers.put(footerType, footer);

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

        public Builder<D> register(@NonNull ViewInjector<D> injector) {
            Precondition.checkNotNull(injector);
            int viewType = injectors.size() + 1;
            injectors.put(viewType, injector);
            return this;
        }

        public LiteAdapterEx<D> create() {
            return new LiteAdapterEx<>(
                    moreLoader, emptyView, keepHeaderAndFooter, headers, footers, injectorFinder,
                    injectors, diffCallback, onItemClickListener, onItemLongClickListener
            );
        }
    }
}