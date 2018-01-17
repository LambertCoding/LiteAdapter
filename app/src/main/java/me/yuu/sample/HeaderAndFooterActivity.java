package me.yuu.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import me.yuu.liteadapter.core.LiteAdapter;
import me.yuu.liteadapter.core.ViewHolder;
import me.yuu.liteadapter.core.ViewInjector;
import me.yuu.liteadapter.core.ViewTypeLinker;

/**
 * @author yu
 */
public class HeaderAndFooterActivity extends AppCompatActivity {

    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_BIG = 1;

    private RecyclerView recyclerView;
    private LiteAdapter<OnePiece> adapter;
    private List<OnePiece> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_list);

        LayoutInflater inflater = LayoutInflater.from(this);
        View header = inflater.inflate(R.layout.item_header, null);
        View footer = inflater.inflate(R.layout.item_footer, null);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LiteAdapter.Builder<OnePiece>(this)
                .register(VIEW_TYPE_NORMAL, new ViewInjector<OnePiece>(R.layout.item_normal) {
                    @Override
                    public void bindData(ViewHolder holder, final OnePiece item, int position) {
                        HeaderAndFooterActivity.this.bindData(holder, item);
                    }
                })
                .register(VIEW_TYPE_BIG, new ViewInjector<OnePiece>(R.layout.item_big) {
                    @Override
                    public void bindData(ViewHolder holder, final OnePiece item, int position) {
                        HeaderAndFooterActivity.this.bindData(holder, item);
                    }
                })
                .viewTypeLinker(new ViewTypeLinker<OnePiece>() {
                    @Override
                    public int viewType(OnePiece item, int position) {
                        return item.isBigType() ? VIEW_TYPE_BIG : VIEW_TYPE_NORMAL;
                    }
                })
                .headerView(header)
                .footerView(footer)
                .create();
        recyclerView.setAdapter(adapter);

        adapter.setNewData(data);
    }

    private void bindData(ViewHolder holder, final OnePiece item) {
        holder.setText(R.id.tvDesc, item.getDesc())
                .with(R.id.ivImage, new ViewHolder.Action<ImageView>() {
                    @Override
                    public void doAction(ImageView view) {
                        Glide.with(HeaderAndFooterActivity.this)
                                .load(item.getImageRes())
                                .centerCrop()
                                .into(view);
                    }
                });
    }

    {
        data.add(new OnePiece("我是要做海贼王的男人", R.mipmap.ic_lufei, false));
        data.add(new OnePiece("路痴路痴路痴", R.mipmap.ic_suolong, false));
        data.add(new OnePiece("haha~", R.mipmap.ic_big1, true));
        data.add(new OnePiece("色河童色河童色河童", R.mipmap.ic_shanzhi, false));
        data.add(new OnePiece("haha~~", R.mipmap.ic_big2, true));
        data.add(new OnePiece("haha~~~", R.mipmap.ic_big3, true));
        data.add(new OnePiece("我是要做海贼王的男人", R.mipmap.ic_lufei, false));
        data.add(new OnePiece("路痴路痴路痴", R.mipmap.ic_suolong, false));
        data.add(new OnePiece("haha~", R.mipmap.ic_big1, true));
        data.add(new OnePiece("色河童色河童色河童", R.mipmap.ic_shanzhi, false));
        data.add(new OnePiece("haha~~", R.mipmap.ic_big2, true));
        data.add(new OnePiece("haha~~~", R.mipmap.ic_big3, true));
    }
}
