package me.yuu.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.yuu.liteadapter.core.InjectorFinder;
import me.yuu.liteadapter.core.LiteAdapter;
import me.yuu.liteadapter.core.ViewHolder;
import me.yuu.liteadapter.core.ViewInjector;

/**
 * @author yu
 */
public class HeaderAndFooterActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LiteAdapter<OnePiece> adapter;
    private List<OnePiece> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_list);

        final LayoutInflater inflater = LayoutInflater.from(this);
        View header1 = inflater.inflate(R.layout.item_header, null);
        View header2 = inflater.inflate(R.layout.item_header, null);
        View header3 = inflater.inflate(R.layout.item_header, null);
        View footer = inflater.inflate(R.layout.item_footer, null);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LiteAdapter.Builder<OnePiece>(this)
                .register(new ViewInjector<OnePiece>(R.layout.item_normal) {
                    @Override
                    public void bindData(ViewHolder holder, final OnePiece item, int position) {
                        HeaderAndFooterActivity.this.bindData(holder, item);
                    }
                })
                .register(new ViewInjector<OnePiece>(R.layout.item_big) {
                    @Override
                    public void bindData(ViewHolder holder, final OnePiece item, int position) {
                        HeaderAndFooterActivity.this.bindData(holder, item);
                    }
                })
                .injectorFinder(new InjectorFinder<OnePiece>() {
                    @Override
                    public int index(OnePiece item, int position) {
                        return item.isBigType() ? 1 : 0;
                    }
                })
                .headerView(header1)
                .headerView(header2)
                .headerView(header3)
                .footerView(footer)
                .itemClickListener(new LiteAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position, Object item) {
                        showToast("click position : " + position);
                    }
                })
                .create();
        recyclerView.setAdapter(adapter);

        adapter.setNewData(data);
    }

    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private void bindData(ViewHolder holder, final OnePiece item) {
        holder.setText(R.id.tvDesc, item.getDesc())
                .with(R.id.ivImage, new ViewHolder.Action<ImageView>() {
                    @Override
                    public void doAction(ImageView view) {
                        Glide.with(HeaderAndFooterActivity.this)
                                .load(item.getImageRes())
                                .apply(new RequestOptions().centerCrop())
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
        data.add(new OnePiece("路痴路痴路痴", R.mipmap.ic_suolong, false));
        data.add(new OnePiece("色河童色河童色河童", R.mipmap.ic_shanzhi, false));
    }
}
