package me.yuu.sample.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.layout_list.*
import me.yuu.liteadapter.ext.buildAdapter
import me.yuu.sample.R
import me.yuu.sample.entity.SampleEntity
import java.util.*

/**
 * @author yu
 */
class MainActivity : AppCompatActivity() {

    private val data = ArrayList<SampleEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_list)

        val adapter = buildAdapter<SampleEntity>(this) {
            register(R.layout.item_main) { holder, item, _ ->
                holder.setText(R.id.tvDesc, item.name)
            }
            itemClickListener { _, item ->
                startActivity(Intent(this@MainActivity, (item as SampleEntity).target))
            }
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            this.adapter = adapter
        }

        adapter.updateData(data)
    }

    init {
        data.add(SampleEntity("Header & Footer", HeaderAndFooterActivity::class.java))
        data.add(SampleEntity("Empty view & Auto load more", EmptyAndLoadMoreActivity::class.java))
        data.add(SampleEntity("DataBinding", DataBindingSampleActivity::class.java))
        data.add(SampleEntity("RadioActivity", RadioActivity::class.java))
    }
}
