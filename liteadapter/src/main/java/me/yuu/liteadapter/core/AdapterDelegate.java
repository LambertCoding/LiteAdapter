package me.yuu.liteadapter.core;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import static me.yuu.liteadapter.core.LiteAdapter.VIEW_TYPE_EMPTY;

/**
 * @author yu
 * @date 2018/1/12
 */
class AdapterDelegate {

    private final List mDataSet = new ArrayList<>();
    private View mEmptyView;
    private SparseArray<View> mHerders;
    private SparseArray<View> mFooters;
    private ViewTypeLinker mViewTypeLinker;
    /**
     * key   : viewType
     * value : ViewInjector
     */
    private SparseArray<ViewInjector> mViewInjectors;
    private LiteAdapter.OnItemClickListener mOnItemClickListener;
    private LiteAdapter.OnItemLongClickListener mOnItemLongClickListener;

    private AdapterDelegate(View mEmptyView,
                            SparseArray<View> mHerders,
                            SparseArray<View> mFooters,
                            SparseArray<ViewInjector> mViewInjectors,
                            ViewTypeLinker mViewTypeLinker,
                            LiteAdapter.OnItemClickListener onItemClickListener,
                            LiteAdapter.OnItemLongClickListener onItemLongClickListener) {
        this.mEmptyView = mEmptyView;
        this.mHerders = mHerders;
        this.mFooters = mFooters;
        this.mViewInjectors = mViewInjectors;
        this.mViewTypeLinker = mViewTypeLinker;
        this.mOnItemClickListener = onItemClickListener;
        this.mOnItemLongClickListener = onItemLongClickListener;
    }

    static AdapterDelegate create(LiteAdapter.Builder builder) {
        return new AdapterDelegate(builder.emptyView, builder.herders, builder.footers, builder.injectors,
                builder.viewTypeLinker, builder.onItemClickListener, builder.onItemLongClickListener);
    }

    List getDataSet() {
        return mDataSet;
    }

    int adjustPosition(int position) {
        if (isHeader(position)) {
            return position;
        } else if (isFooter(position)) {
            return position - mHerders.size() - mDataSet.size();
        }
        return position - mHerders.size();
    }

    int getHeadersSize() {
        return mHerders.size();
    }

    /**
     * 是否是LiteAdapter的保留的view类型，比如空view，Header和Footer都有指定的ViewType
     *
     * @param viewType type
     * @return is Reserved view type
     */
    private boolean isReservedType(int viewType) {
        return viewType == VIEW_TYPE_EMPTY || mHerders.get(viewType) != null || mFooters.get(viewType) != null;
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

    int getItemCount() {
        if (isEmptyViewEnable()) {
            return 1;
        }
        return mDataSet.size() + mHerders.size() + mFooters.size();
    }

    int getItemViewType(int position) {
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
                return mViewTypeLinker.viewType(getItem(position), position - mHerders.size());
            }
        } else {
            if (mViewTypeLinker != null) {
                Log.i("LiteAdapter", "Single view type don't need ViewTypeLinker,Ignore!");
            }
        }
        return mViewInjectors.keyAt(0);
    }

    ViewHolder createViewHolder(ViewGroup parent, int viewType) {
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

    void bindViewHolder(ViewHolder holder, int position) {
        if (isHeader(position) || isFooter(position) || isEmptyViewEnable()) {
            return;
        }

        final Object item = getItem(position);
        int viewType = getItemViewType(position);

        if (isReservedType(viewType)) {
            throw new IllegalStateException("You use the reserved view type : " + viewType);
        }

        ViewInjector injector = mViewInjectors.get(viewType);
        if (injector == null) {
            throw new NullPointerException("You haven't registered this viewType yet : " + viewType
                    + ". Or you return the wrong value in the ViewTypeLinker.");
        }

        try {
            injector.bindData(holder, item, position - mHerders.size());
        } catch (ClassCastException e) {
            // 发生这个异常是由于使用多种实体类型的时候,ViewTypeLinker返回了错误的ViewType
            // 比如：注册了一个类型111，实体类型是User：adapter.register(111, new ViewInjector<User>(R.layout.item_user)
            //      注册了一个类型222，实体类型是Student：adapter.register(222, new ViewInjector<Student>(R.layout.item_student)
            // 但是在ViewTypeLinker中，获取到的实体是User，但是返回的条目类型是111，就会出现这个异常；
            throw new IllegalStateException("Returned the wrong view type in ViewTypeLinker.");
        }

        setupItemClickListener(holder);
        setupItemLongClickListener(holder);
    }

    private Object getItem(int position) {
        if (isHeader(position) || isFooter(position)) {
            return null;
        }
        return mDataSet.get(position - mHerders.size());
    }

    void setFullSpanForGridView(RecyclerView recyclerView) {
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
    }

    void setFullSpanForStaggeredGridView(ViewHolder holder) {
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

}
