package me.yuu.sample.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.yuu.liteadapter.advenced.SectionAdapter
import me.yuu.liteadapter.entity.SectionItemType
import me.yuu.liteadapter.ext.buildSectionAdapter
import me.yuu.sample.R
import me.yuu.sample.entity.SimpleSectionItem

/**
 * 分组
 * @author yu
 */
class SectionActivity : AppCompatActivity() {

    protected var adapter: SectionAdapter<SimpleSectionItem>? = null
    protected var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_list)
        initView()
        loadData()
    }

    private fun initView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = createAdapter().also { adapter = it }
    }


    private fun createAdapter(): SectionAdapter<SimpleSectionItem> {
        return buildSectionAdapter(this) {
            register(R.layout.item_section_head) { holder, item, position ->
                if (item.itemType == SectionItemType.SINGLE) {
                    holder.setBackgroundDrawable(R.id.tvDesc, resources.getDrawable(R.drawable.bg_white_bottom_corner14))
                } else {
                    holder.setBackgroundColor(R.id.tvDesc, resources.getColor(android.R.color.white))
                }
            }
            register(R.layout.item_section_body) { holder, item, position ->

            }
            register(R.layout.item_section_foot) { holder, item, position ->

            }

            injectorFinder { item, _, _ ->
                return@injectorFinder when (item.itemType) {
                    SectionItemType.SINGLE,
                    SectionItemType.HEAD -> 0
                    SectionItemType.BODY -> 1
                    SectionItemType.FOOT -> 2
                }
            }
            itemClickListener { index, item ->
                Toast.makeText(this@SectionActivity, item.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadData() {
        val section1 = listOf(SimpleSectionItem(), SimpleSectionItem(), SimpleSectionItem())
        val section2 = listOf(SimpleSectionItem(), SimpleSectionItem())
        val section3 = listOf(SimpleSectionItem())
        val section4 = listOf(SimpleSectionItem(), SimpleSectionItem(), SimpleSectionItem(), SimpleSectionItem())
        val section5 = listOf(SimpleSectionItem(), SimpleSectionItem(), SimpleSectionItem())

        val d = arrayOf(section1, section2, section3, section4, section5)
        adapter?.addSectionData(*d)
    }
}
