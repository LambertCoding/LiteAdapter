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
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.lang.ref.WeakReference;

import me.yuu.liteadapter.databinding.DataBindingInjector;
import me.yuu.liteadapter.databinding.DataBindingViewHolder;
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

    protected static final int VIEW_TYPE_EMPTY = -7061;
    protected static final int VIEW_TYPE_LOAD_MORE = -7062;
    protected static final int VIEW_TYPE_HEADER_INDEX = -7060;
    protected static final int VIEW_TYPE_FOOTER_INDEX = -8060;

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
    /**
     * key: viewType    value: ViewInjector
     */
    private final SparseArray<ViewInjector<T>> mViewInjectors;

    private final InjectorFinder<T> mInjectorFinder;
    private final MoreLoader mMoreLoader;
    private final View mEmptyView;
    private LiteAdapter.OnItemClickListener mOnItemClickListener;
    private LiteAdapter.OnItemLongClickListener mOnItemLongClickListener;

    public LiteAdapter(
            MoreLoader moreLoader,
            View emptyView,
            SparseArray<View> headers,
            SparseArray<View> footers,
            InjectorFinder<T> injectorFinder,
            SparseArray<ViewInjector<T>> injectors,
            LiteAdapter.OnItemClickListener onItemClickListener,
            LiteAdapter.OnItemLongClickListener onItemLongClickListener
    ) {
        this.mMoreLoader = moreLoader;
        this.mEmptyView = emptyView;
        this.mHerders = headers;
        this.mFooters = footers;
        this.mInjectorFinder = injectorFinder;
        this.mViewInjectors = injectors;
        this.mOnItemClickListener = onItemClickListener;
        this.mOnItemLongClickListener = onItemLongClickListener;
    }

    public LiteAdapter(Builder<T> builder) {
        this(
                builder.moreLoader,
                builder.emptyView,
                builder.headers,
                builder.footers,
                builder.injectorFinder,
                builder.injectors,
                builder.onItemClickListener,
                builder.onItemLongClickListener
        );
    }

    public LiteAdapter(LiteAdapter<T> adapter) {
        this(
                adapter.getMoreLoader(),
                adapter.getEmptyView(),
                adapter.getHerders(),
                adapter.getFooters(),
                adapter.getInjectorFinder(),
                adapter.getViewInjectors(),
                adapter.getOnItemClickListener(),
                adapter.getOnItemLongClickListener()
        );
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

        return getViewTypeFromInjectors(position);
    }

    protected int getViewTypeFromInjectors(int position) {
        Precondition.checkState(mViewInjectors.size() != 0, "No view type is registered.");

        int index = 0;
        if (mViewInjectors.size() > 1) {
            Precondition.checkNotNull(mInjectorFinder,
                    "Multiple view types are registered. You must set a ViewTypeInjector for LiteAdapter");
            int adjustPosition = position - mHerders.size();
            index = mInjectorFinder.index(mDataSet.get(adjustPosition), adjustPosition, getItemCount());

            Precondition.checkArgument(index >= 0 && index < mViewInjectors.size(),
                    "return wrong index = " + index + " in InjectorFinder, You have registered"
                            + mViewInjectors.size() + " ViewInjector!");
        }
        return mViewInjectors.keyAt(index);
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
            view.setLayoutParams(generateLayoutParamsForHeaderAndFooter(view));
            return new ViewHolder(view);
        } else if (isFooterType(viewType)) {
            View view = mFooters.get(viewType);
            view.setLayoutParams(generateLayoutParamsForHeaderAndFooter(view));
            return new ViewHolder(view);
        } else {
            ViewInjector injector = mViewInjectors.get(viewType);

            Precondition.checkNotNull(injector, "You haven't registered this view type("
                    + viewType + ") yet . Or you return the wrong view type in InjectorFinder.");

            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(injector.getLayoutId(), parent, false);

            ViewHolder holder = createDataItemViewHolder(itemView, injector);

            setupItemClickListener(holder);
            setupItemLongClickListener(holder);

            return holder;
        }
    }

    protected ViewHolder createDataItemViewHolder(View itemView, ViewInjector injector) {
        ViewHolder holder;
        if (injector instanceof DataBindingInjector) {
            holder = new DataBindingViewHolder(itemView);
        } else {
            holder = new ViewHolder(itemView);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (isHeader(position) || isFooter(position)
                || isEmptyViewEnable() || isLoadMorePosition(position)) {
            return;
        }
        bindFromViewInjector(holder, position);
    }

    protected void bindFromViewInjector(@NonNull ViewHolder holder, int position) {
        final T item = mDataSet.get(position - mHerders.size());
        final int viewType = getItemViewType(position);

        Precondition.checkState(!isReservedType(viewType),
                "You use the reserved view type : " + viewType);

        ViewInjector<T> injector = Precondition.checkNotNull(mViewInjectors.get(viewType),
                "You haven't registered this view type(" + viewType +
                        ") yet . Or you return the wrong view type in InjectorFinder.");

        injector.bindData(holder, item, position - mHerders.size());
    }

    private RecyclerView.LayoutParams generateLayoutParamsForHeaderAndFooter(View view) {
        if (mOrientation == OrientationHelper.HORIZONTAL) {
            ViewGroup.LayoutParams oldParams = view.getLayoutParams();

            int marginLeft = 0, marginRight = 0, marginTop = 0, marginBottom = 0;
            if (oldParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) oldParams;
                marginLeft = marginLayoutParams.leftMargin;
                marginRight = marginLayoutParams.rightMargin;
                marginTop = marginLayoutParams.topMargin;
                marginBottom = marginLayoutParams.bottomMargin;
            }

            int width = oldParams == null ? ViewGroup.LayoutParams.WRAP_CONTENT : view.getLayoutParams().width;

            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(marginLeft, marginTop, marginRight, marginBottom);

            return params;
        } else {
            ViewGroup.LayoutParams oldParams = view.getLayoutParams();

            int marginLeft = 0, marginRight = 0, marginTop = 0, marginBottom = 0;
            if (oldParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) oldParams;
                marginLeft = marginLayoutParams.leftMargin;
                marginRight = marginLayoutParams.rightMargin;
                marginTop = marginLayoutParams.topMargin;
                marginBottom = marginLayoutParams.bottomMargin;
            }

            int height = view.getLayoutParams() == null ? ViewGroup.LayoutParams.WRAP_CONTENT : view.getLayoutParams().height;

            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            params.setMargins(marginLeft, marginTop, marginRight, marginBottom);

            return params;
        }
    }

    protected void setupItemClickListener(final ViewHolder viewHolder) {
        if (mOnItemClickListener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = viewHolder.getLayoutPosition() - mHerders.size();
                    mOnItemClickListener.onItemClick(position, mDataSet.get(position));
                }
            });
        }
    }

    protected void setupItemLongClickListener(final ViewHolder viewHolder) {
        if (mOnItemLongClickListener != null) {
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = viewHolder.getLayoutPosition() - mHerders.size();
                    mOnItemLongClickListener.onItemLongClick(position, mDataSet.get(position));
                    return true;
                }
            });
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (mRecyclerView == null || mRecyclerView.get() == null) {
            mRecyclerView = new WeakReference<>(recyclerView);
        }

        initOrientation(recyclerView.getLayoutManager());

        setSpanSizeLookup4Grid(recyclerView);

        if (mMoreLoader != null) {
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
                    return (isEmptyViewEnable()
                            || isHeader(position)
                            || isFooter(position)
                            || isLoadMorePosition(position))
                            ? gridManager.getSpanCount() : 1;
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
        if (mMoreLoader != null) {
            recyclerView.removeOnScrollListener(mMoreLoader);
        }
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
            if (isEmptyViewEnable()
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

    public LiteAdapter<T> attachTo(RecyclerView recyclerView) {
        recyclerView.setAdapter(this);
        return this;
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

    protected boolean isReservedType(int viewType) {
        return viewType == VIEW_TYPE_EMPTY || viewType == VIEW_TYPE_LOAD_MORE
                || isHeaderType(viewType) || isFooterType(viewType);
    }

    protected boolean isHeaderType(int viewType) {
        return mHerders.size() > 0 && mHerders.get(viewType) != null;
    }

    protected boolean isFooterType(int viewType) {
        return mFooters.size() > 0 && mFooters.get(viewType) != null;
    }

    protected boolean isHeader(int position) {
        int headersCount = mHerders.size();
        return position >= 0 && position < headersCount;
    }

    protected boolean isFooter(int position) {
        int headerAndDataCount = mHerders.size() + mDataSet.size();
        return mFooters.size() > 0 && position >= headerAndDataCount
                && position < headerAndDataCount + mFooters.size();
    }

    protected boolean isEmptyViewEnable() {
        // Disable the empty view if have header view or footer view.
        // not included loadMoreFooter view
        return mEmptyView != null && mDataSet.size() + mHerders.size() + mFooters.size() == 0;
    }

    protected boolean isLoadMoreEnable() {
        return mMoreLoader != null && mMoreLoader.isLoadMoreEnable();
    }

    protected boolean isLoadMorePosition(int position) {
        return position == mDataSet.size() + mHerders.size() + mFooters.size();
    }

    public SparseArray<View> getHerders() {
        return mHerders;
    }

    public SparseArray<View> getFooters() {
        return mFooters;
    }

    public SparseArray<ViewInjector<T>> getViewInjectors() {
        return mViewInjectors;
    }

    public InjectorFinder<T> getInjectorFinder() {
        return mInjectorFinder;
    }

    public MoreLoader getMoreLoader() {
        return mMoreLoader;
    }

    public View getEmptyView() {
        return mEmptyView;
    }

    public OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public OnItemLongClickListener getOnItemLongClickListener() {
        return mOnItemLongClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener mOnItemLongClickListener) {
        this.mOnItemLongClickListener = mOnItemLongClickListener;
    }

    ////////////////////////////////////////////////////////////////////////////

    public static class Builder<D> {
        private Context context;
        private View emptyView;
        private MoreLoader moreLoader;
        private InjectorFinder<D> injectorFinder;
        private OnItemClickListener onItemClickListener;
        private OnItemLongClickListener onItemLongClickListener;
        private final SparseArray<View> headers = new SparseArray<>();
        private final SparseArray<View> footers = new SparseArray<>();
        private final SparseArray<ViewInjector<D>> injectors = new SparseArray<>();

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

        public Builder<D> injectorFinder(@NonNull InjectorFinder<D> linker) {
            Precondition.checkArgument(injectorFinder == null, "Only one InjectorFinder can be registered.");
            this.injectorFinder = Precondition.checkNotNull(linker);
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

        public LiteAdapter<D> create() {
            return new LiteAdapter<>(this);
        }
    }
}