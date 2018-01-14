package me.yuu.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private LiteAdapter adapter;
    private List<Object> data = new ArrayList<>();
    private Handler handler = new Handler();

    boolean loadMoreEnable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnInsert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                adapter.addAll(2, data);
//                adapter.addDataToHead(new Section());
                adapter.setLoadMoreEnable(loadMoreEnable = !loadMoreEnable);
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        adapter = new LiteAdapter.Builder(this)
                .register(0, new ViewInjector<User>(R.layout.item_normal) {
                    @Override
                    public void bindData(ViewHolder holder, User item, int position) {

                    }
                })
                .register(1, new ViewInjector<User>(R.layout.item_yellow) {
                    @Override
                    public void bindData(ViewHolder holder, User item, int position) {

                    }
                })
                .register(2, new ViewInjector<Section>(R.layout.item_gray) {
                    @Override
                    public void bindData(ViewHolder holder, Section item, int position) {

                    }
                })
                .viewTypeLinker(new ViewTypeLinker() {
                    @Override
                    public int viewType(Object item, int position) {
                        if (item instanceof User) {
                            if (position % 2 == 0) {
                                return 0;
                            } else {
                                return 1;
                            }
                        } else {
                            return 2;
                        }
                    }
                })
                .emptyView(this, R.layout.empty_view)
                .headerView(View.inflate(this, R.layout.item_head1, null))
                .headerView(View.inflate(this, R.layout.item_head2, null))
                .footerView(View.inflate(this, R.layout.item_footer1, null))
                .footerView(View.inflate(this, R.layout.item_footer2, null))
                .enableLoadMore(new MoreLoader.LoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        loadMore();
                    }
                })
                .itemClickListener(new LiteAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position, Object item) {
                        Toast.makeText(MainActivity.this, item.getClass().getName() + "::" + position, Toast.LENGTH_SHORT).show();
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
                adapter.disableLoadMoreIfNotFullPage(recyclerView);
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
        data.add(new User());
        data.add(new Section());
        data.add(new Section());
        data.add(new User());
        data.add(new Section());
    }
}
