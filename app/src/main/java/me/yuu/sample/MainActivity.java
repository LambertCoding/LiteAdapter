package me.yuu.sample;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.yuu.liteadapter.LiteAdapter;
import me.yuu.liteadapter.ViewHolder;
import me.yuu.liteadapter.ViewInjector;
import me.yuu.liteadapter.ViewTypeLinker;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private LiteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        refreshLayout = findViewById(R.id.refreshLayout);

        adapter = new LiteAdapter.Builder()
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
                .register(2, new ViewInjector<Setion>(R.layout.item_gray) {
                    @Override
                    public void bindData(ViewHolder holder, Setion item, int position) {

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
                .headerView(this, R.layout.item_head1)
                .headerView(this, R.layout.item_head2)
                .headerView(this, R.layout.item_head3)
                .footerView(this, R.layout.item_footer1)
                .footerView(this, R.layout.item_footer2)
                .itemClickListener(new LiteAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position, Object item) {
                        Toast.makeText(MainActivity.this, item.getClass().getName() + "::" + position, Toast.LENGTH_SHORT).show();
                    }
                })
                .create();
        recyclerView.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
                refreshLayout.setRefreshing(false);
            }
        });
    }

    private void loadData() {
        List<Object> data = new ArrayList<>();
        data.add(new User());
        data.add(new User());
        data.add(new Setion());
        data.add(new User());
        data.add(new User());
        data.add(new Setion());
        data.add(new Setion());
        data.add(new User());
        data.add(new User());
        data.add(new User());
        data.add(new Setion());
        data.add(new User());
        data.add(new User());
        data.add(new Setion());
        data.add(new Setion());
        data.add(new Setion());
        data.add(new User());
        data.add(new User());
        data.add(new User());
        data.add(new Setion());
        data.add(new Setion());
        data.add(new User());
        data.add(new Setion());
        data.add(new User());
        data.add(new User());
        data.add(new Setion());
        data.add(new Setion());
        data.add(new Setion());
        data.add(new User());
        data.add(new User());
        data.add(new User());
        data.add(new Setion());
        data.add(new Setion());

        adapter.setNewData(data);
    }
}
