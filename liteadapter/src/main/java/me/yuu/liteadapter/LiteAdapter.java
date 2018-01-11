package me.yuu.liteadapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView的通用Adapter
 *
 * @author yu
 */
public class LiteAdapter extends RecyclerView.Adapter<ViewHolder> implements DataHelper {

    private List<Object> mDataSet = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private ViewTypeLinker mViewTypeLinker;
    /**
     * index : viewType
     * key   : layoutId
     * value : ViewInjector
     */
    private final SparseArray<ViewInjector> mViewInjectors = new SparseArray<>();

    public <T> LiteAdapter register(@LayoutRes int layoutId, @NonNull ViewInjector<T> binder) {
        mViewInjectors.put(layoutId, binder);
        return this;
    }

    public LiteAdapter viewTypeLinker(@NonNull ViewTypeLinker linker) {
        if (mViewTypeLinker != null) {
            throw new IllegalStateException("You already have one ViewTypeLinker");
        }
        this.mViewTypeLinker = linker;
        return this;
    }

    public LiteAdapter attachTo(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(this);
        return this;
    }

    public LiteAdapter data(List data) {
        if (data != null) {
            mDataSet = data;
        }
        return this;
    }

    @Override
    public int getItemCount() {
        return mDataSet == null ? 0 : mDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        // 如果是多种条目类型，就调用ViewTypeLinker获取,否则返回默认的0
        if (mViewInjectors.size() > 1) {
            if (mViewTypeLinker == null) {
                throw new NullPointerException("Multiple view types are registered. You must set a ViewTypeInjector for LiteAdapter");
            } else {
                return mViewTypeLinker.viewType(mDataSet.get(position), position);
            }
        }
        return 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(mViewInjectors.keyAt(viewType), parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Object item = getItem(position);
        int viewType = getItemViewType(position);

        mViewInjectors.valueAt(viewType).bindData(holder, item, position);

        setupItemClickListener(holder, position);
        setupItemLongClickListener(holder, position);
    }

    protected void setupItemClickListener(final ViewHolder viewHolder, final int position) {
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

    protected void setupItemLongClickListener(final ViewHolder viewHolder, final int position) {
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

    public List getDataSet() {
        return mDataSet;
    }

    @Override
    public boolean contains(Object d) {
        return mDataSet.contains(d);
    }

    @Override
    public void addItem(Object item) {
        mDataSet.add(item);
        notifyDataSetChanged();
    }

    @Override
    public void addItems(List items) {
        if (items != null && !items.isEmpty()) {
            mDataSet.addAll(items);
            notifyDataSetChanged();
        }
    }

    @Override
    public void addItemToHead(Object item) {
        mDataSet.add(0, item);
        notifyDataSetChanged();
    }

    @Override
    public void addItemsToHead(List items) {
        if (items != null && !items.isEmpty()) {
            mDataSet.addAll(0, items);
            notifyDataSetChanged();
        }
    }

    @Override
    public void remove(int position) {
        mDataSet.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public void remove(Object item) {
        mDataSet.remove(item);
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }

    @Override
    public void setNewData(List items) {
        mDataSet.clear();
        if (items != null && !items.isEmpty()) {
            mDataSet.addAll(items);
        }
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return mDataSet.get(position);
    }

    @Override
    public void modify(Object oldData, Object newData) {
        modify(mDataSet.indexOf(oldData), newData);
    }

    @Override
    public void modify(int index, Object newData) {
        mDataSet.set(index, newData);
        notifyDataSetChanged();
    }

    public LiteAdapter setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
        return this;
    }

    public LiteAdapter setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
        return this;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, Object item);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, Object item);
    }

}

