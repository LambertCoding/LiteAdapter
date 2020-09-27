package me.yuu.sample.ui

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import me.yuu.liteadapter.core.LiteAdapterEx
import me.yuu.liteadapter.core.ViewHolder
import me.yuu.liteadapter.ext.buildRadioAdapter
import me.yuu.sample.R
import me.yuu.sample.entity.OnePiece

/**
 * 单选
 * @author yu
 */
class RadioActivity : BaseActivity() {

    override fun createAdapter(): LiteAdapterEx<OnePiece> {

        val inflater = LayoutInflater.from(this)
        val header1 = inflater.inflate(R.layout.item_header, null)
        val header2 = inflater.inflate(R.layout.item_header, null)
        val header3 = inflater.inflate(R.layout.item_header, null)
        val footer = inflater.inflate(R.layout.item_footer, null)

        return buildRadioAdapter(this) {
            register(R.layout.item_normal) { holder, item, _ ->
                bindData2View(holder, item)
            }
            register(R.layout.item_big) { holder, item, _ ->
                bindData2View(holder, item)
            }

            injectorFinder { item, _, _ ->
                if (item.isBigType) 1 else 0
            }
            itemClickListener { index, item ->
                check(index, item as OnePiece)
            }

            addHeader(header1)
            addHeader(header2)
            addHeader(header3)
            addFooter(footer)
        }
    }

    private fun bindData2View(holder: ViewHolder, item: OnePiece) {
        holder.setVisibility(R.id.tvTag, if (item.isSelected) View.VISIBLE else View.GONE)
        holder.setText(R.id.tvDesc, item.desc)
                .doAction<ImageView>(R.id.ivImage) { view ->
                    if (item.imageRes == -1) item.imageRes = R.mipmap.ic_launcher
                    Glide.with(this)
                            .load(item.imageRes)
                            .apply(RequestOptions().centerCrop())
                            .into(view)
                }
    }
}
