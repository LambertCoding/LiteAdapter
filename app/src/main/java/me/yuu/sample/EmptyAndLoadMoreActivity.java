package me.yuu.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.yuu.liteadapter.core.LiteAdapter;
import me.yuu.liteadapter.core.ViewHolder;
import me.yuu.liteadapter.core.ViewInjector;
import me.yuu.liteadapter.core.ViewTypeLinker;
import me.yuu.liteadapter.loadmore.MoreLoader;

public class EmptyAndLoadMoreActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private LiteAdapter<OnePiece> adapter;
    private List<OnePiece> data = new ArrayList<>();
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_more);
        findViewById(R.id.btnInsert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.addAll(0, data);
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LiteAdapter.Builder<OnePiece>(this)
                .register(0, new ViewInjector<OnePiece>(R.layout.item_normal) {
                    @Override
                    public void bindData(ViewHolder holder, OnePiece item, int position) {

                    }
                })
                .register(1, new ViewInjector<OnePiece>(R.layout.item_big) {
                    @Override
                    public void bindData(ViewHolder holder, OnePiece item, int position) {

                    }
                })
                .viewTypeLinker(new ViewTypeLinker<OnePiece>() {
                    @Override
                    public int viewType(OnePiece item, int position) {
                        return 0;
                    }
                })
                .emptyView(R.layout.empty_view)
                .enableLoadMore(new MoreLoader.LoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        loadMore();
                    }
                })
                .itemClickListener(new LiteAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position, Object item) {
                        Toast.makeText(EmptyAndLoadMoreActivity.this,
                                "click position : " + position, Toast.LENGTH_SHORT).show();
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

    private void loadData() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.setNewData(data);
                refreshLayout.setRefreshing(false);
            }
        }, 1000);
    }

    private void loadMore() {
        Log.e("asd", "loadMore");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long timeMillis = System.currentTimeMillis();
                if (timeMillis % 5 == 0 || timeMillis % 5 == 1) {
                    adapter.addAll(data);
                    adapter.loadMoreCompleted();
                } else if (timeMillis % 5 == 2 || timeMillis % 5 == 3) {
                    adapter.loadMoreError();
                } else if (timeMillis % 5 == 4) {
                    adapter.noMore();
                }
            }
        }, 1000);

    }

    {

    }
}
