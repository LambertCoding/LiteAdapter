package me.yuu.liteadapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * RecyclerView的通用Adapter
 *
 * @author yu
 */
@SuppressWarnings("all")
public class LiteAdapter extends RecyclerView.Adapter<ViewHolder> implements DataOperator {

    static final int VIEW_TYPE_EMPTY = -7061;
    static final int VIEW_TYPE_HEADER_INDEX = -7060;
    static final int VIEW_TYPE_FOOTER_INDEX = -8060;

    private ItemManager mItemManager;

    private LiteAdapter(Builder builder) {
        this.mItemManager = ItemManager.create(builder);
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
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams
                && (mItemManager.isHeader(holder.getLayoutPosition()) || mItemManager.isFooter(holder.getLayoutPosition()))) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            p.setFullSpan(true);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return mItemManager.createViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mItemManager.bindViewHolder(holder, position);
    }

    public List getDataSet() {
        return mItemManager.getDataSet();
    }

    @Override
    public boolean contains(Object d) {
        return mItemManager.getDataSet().contains(d);
    }

    @Override
    public void addItem(@NonNull Object item) {
        if (item != null) {
            mItemManager.getDataSet().add(item);
            notifyDataSetChanged();
        }
    }

    @Override
    public void addItems(@NonNull List items) {
        if (items != null && !items.isEmpty()) {
            mItemManager.getDataSet().addAll(items);
            notifyDataSetChanged();
        }
    }

    @Override
    public void addItem(int index, @NonNull Object item) {
        if (item != null) {
            mItemManager.getDataSet().add(index, item);
            notifyDataSetChanged();
        }
    }

    @Override
    public void addItems(int index, @NonNull List items) {
        if (items != null && !items.isEmpty()) {
            mItemManager.getDataSet().addAll(index, items);
            notifyDataSetChanged();
        }
    }

    @Override
    public void addItemToHead(@NonNull Object item) {
        mItemManager.getDataSet().add(0, item);
        notifyDataSetChanged();
    }

    @Override
    public void addItemsToHead(@NonNull List items) {
        if (items != null && !items.isEmpty()) {
            mItemManager.getDataSet().addAll(0, items);
            notifyDataSetChanged();
        }
    }

    @Override
    public void remove(int position) {
        mItemManager.getDataSet().remove(position);
        notifyDataSetChanged();
    }

    @Override
    public void remove(Object item) {
        mItemManager.getDataSet().remove(item);
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        mItemManager.getDataSet().clear();
        notifyDataSetChanged();
    }

    @Override
    public void setNewData(List items) {
        mItemManager.getDataSet().clear();
        if (items != null && !items.isEmpty()) {
            mItemManager.getDataSet().addAll(items);
        }
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return mItemManager.getDataSet().get(position);
    }

    @Override
    public void modify(Object oldData, Object newData) {
        modify(mItemManager.getDataSet().indexOf(oldData), newData);
    }

    @Override
    public void modify(int index, Object newData) {
        mItemManager.getDataSet().set(index, newData);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(int position, Object item);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, Object item);
    }

    public static final class Builder {
        OnItemClickListener onItemClickListener;
        OnItemLongClickListener onItemLongClickListener;
        ViewTypeLinker viewTypeLinker;
        SparseArray<ViewInjector> injectors = new SparseArray<>();
        SparseArray<View> herders = new SparseArray<>();
        SparseArray<View> footers = new SparseArray<>();
        View emptyView;

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
                throw new IllegalStateException("You already have one ViewTypeLinker");
            }
            this.viewTypeLinker = linker;
            return this;
        }

        public Builder headerView(Context context, @LayoutRes int header) {
            headerView(LayoutInflater.from(context).inflate(header, null));
            return this;
        }

        public Builder footerView(Context context, @LayoutRes int footer) {
            footerView(LayoutInflater.from(context).inflate(footer, null));
            return this;
        }

        public Builder headerView(@NonNull View header) {
            if (header == null) {
                throw new IllegalArgumentException("the header == null.");
            }
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            header.setLayoutParams(params);
            int headerType = VIEW_TYPE_HEADER_INDEX + herders.size();
            herders.put(headerType, header);
            return this;
        }

        public Builder footerView(@NonNull View footer) {
            if (footer == null) {
                throw new IllegalArgumentException("the footer == null.");
            }
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            footer.setLayoutParams(params);
            int footerType = VIEW_TYPE_FOOTER_INDEX + footers.size();
            footers.put(footerType, footer);
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