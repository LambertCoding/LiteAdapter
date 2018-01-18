package me.yuu.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import me.yuu.liteadapter.core.LiteAdapter;
import me.yuu.liteadapter.core.ViewHolder;
import me.yuu.liteadapter.core.ViewInjector;
import me.yuu.liteadapter.core.ViewTypeLinker;
import me.yuu.liteadapter.loadmore.MoreLoader;

public class EmptyAndLoadMoreActivity extends AppCompatActivity {

    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_BIG = 1;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private LiteAdapter<OnePiece> adapter;
    private List<OnePiece> data = new ArrayList<>();
    private Handler handler = new Handler();
    private int loadMoreCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_more);
        findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.clear();
            }
        });
        View emptyView = LayoutInflater.from(this).inflate(R.layout.empty_view, null);
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LiteAdapter.Builder<OnePiece>(this)
                .register(VIEW_TYPE_NORMAL, new ViewInjector<OnePiece>(R.layout.item_normal) {
                    @Override
                    public void bindData(ViewHolder holder, final OnePiece item, int position) {
                        bindDatas(holder, item);
                    }
                })
                .register(VIEW_TYPE_BIG, new ViewInjector<OnePiece>(R.layout.item_big) {
                    @Override
                    public void bindData(ViewHolder holder, final OnePiece item, int position) {
                        bindDatas(holder, item);
                    }
                })
                .viewTypeLinker(new ViewTypeLinker<OnePiece>() {
                    @Override
                    public int viewType(OnePiece item, int position) {
                        return item.isBigType() ? VIEW_TYPE_BIG : VIEW_TYPE_NORMAL;
                    }
                })
                .emptyView(emptyView)
                .enableLoadMore(new MoreLoader.LoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        loadMore();
                    }
                })
                .itemClickListener(new LiteAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position, Object item) {
                        showToast("position = " + position);
                    }
                })
                .itemLongClickListener(new LiteAdapter.OnItemLongClickListener() {
                    @Override
                    public void onItemLongClick(int position, Object item) {
                        adapter.remove(position);
                    }
                })
                .create();
        recyclerView.setAdapter(adapter);

        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
    }

    private void showToast(String str) {
        Toast.makeText(EmptyAndLoadMoreActivity.this, str, Toast.LENGTH_SHORT).show();
    }

    private void loadData() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.setNewData(data);
                refreshLayout.setRefreshing(false);
                loadMoreCount = 0;
            }
        }, 1000);
    }

    private void loadMore() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (loadMoreCount == 0 || loadMoreCount == 2) {
                    adapter.addAll(data);
                    adapter.loadMoreCompleted();
                    loadMoreCount++;
                } else if (loadMoreCount == 1) {
                    adapter.loadMoreError();
                    loadMoreCount++;
                } else {
                    adapter.noMore();
                }
            }
        }, 1000);

    }

    private void bindDatas(ViewHolder holder, final OnePiece item) {
        holder.setText(R.id.tvDesc, item.getDesc())
                .with(R.id.ivImage, new ViewHolder.Action<ImageView>() {
                    @Override
                    public void doAction(ImageView view) {
                        Glide.with(EmptyAndLoadMoreActivity.this)
                                .load(item.getImageRes())
                                .centerCrop()
                                .into(view);
                    }
                });
    }

    {
        data.add(new OnePiece("我是要做海贼王的男人", R.mipmap.ic_lufei, false));
        data.add(new OnePiece("路痴路痴路痴", R.mipmap.ic_suolong, false));
        data.add(new OnePiece("haha~~", R.mipmap.ic_big2, true));
        data.add(new OnePiece("色河童色河童色河童", R.mipmap.ic_shanzhi, false));
        data.add(new OnePiece("我是要做海贼王的男人", R.mipmap.ic_lufei, false));
        data.add(new OnePiece("haha~~~", R.mipmap.ic_big3, true));
        data.add(new OnePiece("路痴路痴路痴", R.mipmap.ic_suolong, false));
        data.add(new OnePiece("haha~", R.mipmap.ic_big1, true));
        data.add(new OnePiece("色河童色河童色河童", R.mipmap.ic_shanzhi, false));
        data.add(new OnePiece("haha~", R.mipmap.ic_big1, true));
        data.add(new OnePiece("我是要做海贼王的男人", R.mipmap.ic_lufei, false));
        data.add(new OnePiece("haha~~", R.mipmap.ic_big2, true));
        data.add(new OnePiece("路痴路痴路痴", R.mipmap.ic_suolong, false));
        data.add(new OnePiece("haha~~~", R.mipmap.ic_big3, true));
        data.add(new OnePiece("我是要做海贼王的男人", R.mipmap.ic_lufei, false));
    }
}
