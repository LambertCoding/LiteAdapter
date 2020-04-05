package me.yuu.sample.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.yuu.liteadapter.core.LiteAdapter
import me.yuu.liteadapter.core.ViewHolder
import me.yuu.liteadapter.core.ViewInjector
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

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = LiteAdapter.Builder<SampleEntity>(this)
                .register(object : ViewInjector<SampleEntity>(R.layout.item_main) {
                    override fun bindData(holder: ViewHolder, item: SampleEntity, position: Int) {
                        holder.setText(R.id.tvDesc, item.name)
                    }
                })
                .itemClickListener { _, item -> startActivity(Intent(this, (item as SampleEntity).target)) }
                .create()
                .attachTo(recyclerView)

        adapter.updateData(data)
    }

    init {
        data.add(SampleEntity("Header & Footer", HeaderAndFooterActivity::class.java))
        data.add(SampleEntity("Empty view & Auto load more", EmptyAndLoadMoreActivity::class.java))
        data.add(SampleEntity("DataBinding", DataBindingSampleActivity::class.java))
    }
}
