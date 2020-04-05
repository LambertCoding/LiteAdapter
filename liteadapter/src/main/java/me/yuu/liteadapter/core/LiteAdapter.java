package me.yuu.liteadapter.core;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import me.yuu.liteadapter.databinding.DataBindingInjector;
import me.yuu.liteadapter.databinding.DataBindingViewHolder;
import me.yuu.liteadapter.diff.DefaultDiffCallback;
import me.yuu.liteadapter.diff.LiteDiffUtil;
import me.yuu.liteadapter.util.Precondition;

/**
 * 简易adapter，仅支持多ViewType
 * 如需要支持Header、Footer、EmptyView、LoadMore等扩展功能，请使用{@link LiteAdapterEx}
 *
 * @param <T>
 */
public class LiteAdapter<T> extends AbstractAdapter<T> {

    /**
     * key: viewType    value: {@link ViewInjector}
     */
    private final SparseArray<ViewInjector<T>> mViewInjectors;
    private final InjectorFinder<T> mInjectorFinder;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public LiteAdapter(
            SparseArray<ViewInjector<T>> injectors,
            InjectorFinder<T> injectorFinder,
            LiteDiffUtil.Callback diffCallback,
            OnItemClickListener onItemClickListener,
            OnItemLongClickListener onItemLongClickListener
    ) {
        this.mViewInjectors = injectors;
        this.mInjectorFinder = injectorFinder;
        this.mDiffCallback = diffCallback;
        this.mOnItemClickListener = onItemClickListener;
        this.mOnItemLongClickListener = onItemLongClickListener;
    }

    @Override
    protected void beforeUpdateData() {

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewInjector injector = mViewInjectors.get(viewType);

        Precondition.checkNotNull(injector, "You haven't registered this view type("
                + viewType + ") yet . Or you return the wrong view type in InjectorFinder.");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(injector.getLayoutId(), parent, false);

        ViewHolder holder = createCustomViewHolder(itemView, injector);

        setupItemClickListener(holder);
        setupItemLongClickListener(holder);

        return holder;
    }

    protected ViewHolder createCustomViewHolder(View itemView, ViewInjector injector) {
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
        bindFromViewInjector(holder, position);
    }

    protected void bindFromViewInjector(@NonNull ViewHolder holder, int position) {
        final T item = mDataSet.get(adjustGetItemPosition(position));
        final int viewType = getItemViewType(position);

        Precondition.checkState(!isReservedType(viewType),
                "You use the reserved view type : " + viewType);

        ViewInjector<T> injector = Precondition.checkNotNull(mViewInjectors.get(viewType),
                "You haven't registered this view type(" + viewType +
                        ") yet . Or you return the wrong view type in InjectorFinder.");

        injector.bindData(holder, item, adjustGetItemPosition(position));
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getViewTypeFromInjectors(position);
    }

    protected int getViewTypeFromInjectors(int position) {
        Precondition.checkState(mViewInjectors.size() != 0, "No view type is registered.");

        int index = 0;
        if (mViewInjectors.size() > 1) {
            Precondition.checkNotNull(mInjectorFinder,
                    "Multiple view types are registered. You must set a ViewTypeInjector for LiteAdapter");
            int adjustPosition = adjustGetItemPosition(position);
            index = mInjectorFinder.index(mDataSet.get(adjustPosition), adjustPosition, getItemCount());

            Precondition.checkArgument(index >= 0 && index < mViewInjectors.size(),
                    "return wrong index = " + index + " in InjectorFinder, You have registered"
                            + mViewInjectors.size() + " ViewInjector!");
        }
        return mViewInjectors.keyAt(index);
    }

    protected void setupItemClickListener(final ViewHolder viewHolder) {
        if (mOnItemClickListener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = adjustGetItemPosition(viewHolder.getLayoutPosition());
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
                    int position = adjustGetItemPosition(viewHolder.getLayoutPosition());
                    mOnItemLongClickListener.onItemLongClick(position, mDataSet.get(position));
                    return true;
                }
            });
        }
    }

    public LiteAdapter<T> attachTo(RecyclerView recyclerView) {
        recyclerView.setAdapter(this);
        return this;
    }

    protected boolean isReservedType(int viewType) {
        return false;
    }

    public SparseArray<ViewInjector<T>> getViewInjectors() {
        return mViewInjectors;
    }

    public InjectorFinder<T> getInjectorFinder() {
        return mInjectorFinder;
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

    public static class Builder<D> {
        protected Context context;
        private LiteDiffUtil.Callback diffCallback = new DefaultDiffCallback();
        private InjectorFinder<D> injectorFinder;
        private OnItemClickListener onItemClickListener;
        private OnItemLongClickListener onItemLongClickListener;
        private final SparseArray<ViewInjector<D>> injectors = new SparseArray<>();

        public Builder(Context context) {
            this.context = Precondition.checkNotNull(context);
        }

        public Builder<D> autoDiff(LiteDiffUtil.Callback diffCallback) {
            this.diffCallback = diffCallback;
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

        public Builder<D> register(@NonNull ViewInjector<D> injector) {
            Precondition.checkNotNull(injector);
            int viewType = injectors.size() + 1;
            injectors.put(viewType, injector);
            return this;
        }

        public LiteAdapter<D> create() {
            return new LiteAdapter<>(
                    injectors, injectorFinder, diffCallback, onItemClickListener, onItemLongClickListener
            );
        }
    }
}
