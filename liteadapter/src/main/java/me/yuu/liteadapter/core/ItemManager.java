package me.yuu.liteadapter.core;

import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import me.yuu.liteadapter.LiteAdapter;

import static me.yuu.liteadapter.LiteAdapter.VIEW_TYPE_EMPTY;

/**
 * @author yu
 * @date 2018/1/12
 */
public class ItemManager {

    private List mDataSet = new ArrayList<>();
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

    private ItemManager(View mEmptyView,
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

    public static ItemManager create(LiteAdapter.Builder builder) {
        return new ItemManager(builder.emptyView, builder.herders, builder.footers, builder.injectors,
                builder.viewTypeLinker, builder.onItemClickListener, builder.onItemLongClickListener);
    }

    public List getDataSet() {
        return mDataSet;
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

    public boolean isHeader(int position) {
        return mHerders != null && mHerders.size() > 0 && position >= 0 && position < mHerders.size();
    }

    public boolean isFooter(int position) {
        return mFooters != null && mFooters.size() > 0 && position >= mHerders.size() + mDataSet.size();
    }

    public boolean isEmptyViewEnable() {
        return mEmptyView != null && mDataSet.size() == 0;
    }

    public int getItemCount() {
        if (isEmptyViewEnable()) {
            return 1;
        }
        return mDataSet.size() + mHerders.size() + mFooters.size();
    }

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
                return mViewTypeLinker.viewType(getItem(position), position - mHerders.size());
            }
        } else {
            if (mViewTypeLinker != null) {
                Log.i("LiteAdapter", "Single view type don't need ViewTypeLinker,Ignore!");
            }
        }
        return mViewInjectors.keyAt(0);
    }

    public ViewHolder createViewHolder(ViewGroup parent, int viewType) {
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

    public void bindViewHolder(ViewHolder holder, int position) {
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
            injector.bindData(holder, item, position);
        } catch (ClassCastException e) {
            // 发生这个异常是由于使用多种实体类型的时候,ViewTypeLinker返回了错误的ViewType
            // 比如：注册了一个类型111，实体类型是User：adapter.register(111, new ViewInjector<User>(R.layout.item_user)
            //      注册了一个类型222，实体类型是Student：adapter.register(222, new ViewInjector<Student>(R.layout.item_student)
            // 但是在ViewTypeLinker中，获取到的实体是User，但是返回的条目类型是111，就会出现这个异常；
            throw new IllegalStateException("Returned the wrong view type in ViewTypeLinker.");
        }


        setupItemClickListener(holder, position);
        setupItemLongClickListener(holder, position);
    }

    private Object getItem(int position) {
        if (isHeader(position) || isFooter(position)) {
            return null;
        }
        return mDataSet.get(position - mHerders.size());
    }

    private void setupItemClickListener(final ViewHolder viewHolder, final int position) {
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    int pos = viewHolder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(pos, getItem(pos));
                }
            }
        });
    }

    private void setupItemLongClickListener(final ViewHolder viewHolder, final int position) {
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mOnItemLongClickListener != null) {
                    int pos = viewHolder.getLayoutPosition();
                    mOnItemLongClickListener.onItemLongClick(pos, getItem(pos));
                }
                return false;
            }
        });
    }
}
