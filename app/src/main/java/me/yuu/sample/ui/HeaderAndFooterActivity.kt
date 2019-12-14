package me.yuu.sample.ui

import android.view.LayoutInflater
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import me.yuu.liteadapter.core.LiteAdapter
import me.yuu.liteadapter.core.ViewHolder
import me.yuu.liteadapter.core.ViewInjector
import me.yuu.sample.R
import me.yuu.sample.entity.OnePiece

/**
 * @author yu
 */
class HeaderAndFooterActivity : BaseActivity() {

    override fun createAdapter(): LiteAdapter<OnePiece> {

        val inflater = LayoutInflater.from(this)
        val header1 = inflater.inflate(R.layout.item_header, null)
        val header2 = inflater.inflate(R.layout.item_header, null)
        val header3 = inflater.inflate(R.layout.item_header, null)
        val footer = inflater.inflate(R.layout.item_footer, null)

        return LiteAdapter.Builder<OnePiece>(this)
                .register(object : ViewInjector<OnePiece>(R.layout.item_normal) {
                    override fun bindData(holder: ViewHolder, item: OnePiece, position: Int) {
                        bindData2View(holder, item)
                    }
                })
                .register(object : ViewInjector<OnePiece>(R.layout.item_big) {
                    override fun bindData(holder: ViewHolder, item: OnePiece, position: Int) {
                        bindData2View(holder, item)
                    }
                })
                .injectorFinder { item, _, _ -> if (item.isBigType) 1 else 0 }
                .headerView(header1)
                .headerView(header2)
                .headerView(header3)
                .footerView(footer)
                .itemClickListener { position, _ -> showToast("click position : $position") }
                .create()
    }

    private fun bindData2View(holder: ViewHolder, item: OnePiece) {
        holder.setText(R.id.tvDesc, item.desc)
                .with(R.id.ivImage, ViewHolder.Action<ImageView> { view ->
                    if (item.imageRes == -1) item.imageRes = R.mipmap.ic_launcher
                    Glide.with(this)
                            .load(item.imageRes)
                            .apply(RequestOptions().centerCrop())
                            .into(view)
                })
    }

}
