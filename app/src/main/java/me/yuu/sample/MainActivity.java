package me.yuu.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.yuu.liteadapter.core.LiteAdapter;
import me.yuu.liteadapter.core.ViewHolder;
import me.yuu.liteadapter.core.ViewInjector;

/**
 * @author yu
 */
public class MainActivity extends AppCompatActivity {

    private final List<SampleEntity> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_list);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        LiteAdapter<SampleEntity> adapter = new LiteAdapter.Builder<SampleEntity>(this)
                .register(0, new ViewInjector<SampleEntity>(R.layout.item_main) {
                    @Override
                    public void bindData(ViewHolder holder, SampleEntity item, int position) {
                        holder.setText(R.id.tvDesc, item.getName());
                    }
                })
                .itemClickListener(new LiteAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position, Object item) {
                        startActivity(new Intent(MainActivity.this,
                                ((SampleEntity) item).getTarget()));
                    }
                })
                .create();
        recyclerView.setAdapter(adapter);

        adapter.setNewData(data);
    }

    {
        data.add(new SampleEntity("Header & Footer", HeaderAndFooterActivity.class));
        data.add(new SampleEntity("Empty view & Auto load more", EmptyAndLoadMoreActivity.class));
    }
}
