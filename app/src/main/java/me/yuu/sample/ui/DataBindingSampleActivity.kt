package me.yuu.sample.ui

import android.view.LayoutInflater
import me.yuu.liteadapter.core.LiteAdapterEx
import me.yuu.liteadapter.databinding.DataBindingInjector
import me.yuu.sample.R
import me.yuu.sample.entity.OnePiece

/**
 * @author yu
 */
class DataBindingSampleActivity : BaseActivity() {

    override fun createAdapter(): LiteAdapterEx<OnePiece> {

        val inflater = LayoutInflater.from(this)
        val header = inflater.inflate(R.layout.item_header, null)
        val footer = inflater.inflate(R.layout.item_footer, null)

        return LiteAdapterEx.Builder<OnePiece>(this)
                .register(DataBindingInjector(R.layout.item_big_data_binding))
                .headerView(header)
                .footerView(footer)
                .itemClickListener { position, _ -> showToast("click position : $position") }
                .itemLongClickListener { position, _ -> adapter.remove(position) }
                .create()
    }

}
