package me.yuu.sample.ui

import android.os.Handler
import android.view.LayoutInflater
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_load_more.*
import me.yuu.liteadapter.core.LiteAdapterEx
import me.yuu.liteadapter.core.ViewHolder
import me.yuu.liteadapter.core.ViewInjector
import me.yuu.sample.R
import me.yuu.sample.entity.OnePiece
import java.util.*

class EmptyAndLoadMoreActivity : BaseActivity() {

    private val handler = Handler()
    private var loadMoreCount = 0
    private var insertCount = 0

    override fun getLayoutId(): Int = R.layout.activity_load_more

    override fun initView() {
        super.initView()
        btnClear.setOnClickListener { adapter.clear() }
        btnAdd.setOnClickListener {
            if (adapter.dataSet.size == 0) {
                showToast("请先点击重试！")
                return@setOnClickListener
            }
            if (insertCount <= 2) {
                adapter.addData(2, OnePiece("我是新增的item " + insertCount++))
            } else {
                val newData = ArrayList<OnePiece>()
                newData.add(OnePiece("批量新增的item" + insertCount++))
                newData.add(OnePiece("批量新增的item" + insertCount++))
                adapter.addAll(2, newData)
            }
        }
        refreshLayout.setOnRefreshListener {
            insertCount = 0
            refresh()
        }
    }

    override fun createAdapter(): LiteAdapterEx<OnePiece> {
        val inflater = LayoutInflater.from(this)
        val header1 = inflater.inflate(R.layout.item_header, null)
        val header2 = inflater.inflate(R.layout.item_header, null)
        val header3 = inflater.inflate(R.layout.item_header, null)
        val footer = inflater.inflate(R.layout.item_footer, null)

        val emptyView = LayoutInflater.from(this).inflate(R.layout.empty_view, null)
                .apply { setOnClickListener { refresh() } }

        return LiteAdapterEx.Builder<OnePiece>(this)
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
                .emptyView(emptyView)
                .keepHeaderAndFooter(true)
                .headerView(header1)
                .headerView(header2)
                .headerView(header3)
//                .footerView(footer) // footer和loadMore互斥，如果添加了footer就不能加载更多
//                .autoDiff(DefaultDiffCallback())
                .enableLoadMore { loadMore() }
                .itemClickListener { position, _ -> showToast("position = $position") }
                .itemLongClickListener { position, _ -> adapter.remove(position) }
                .create()
    }

    private fun refresh() {
        handler.postDelayed({
            adapter.updateData(data)
            refreshLayout.isRefreshing = false
            loadMoreCount = 0
        }, 1000)
    }

    private fun loadMore() {
        handler.postDelayed({
            if (loadMoreCount == 0 || loadMoreCount == 2) {
                adapter.addAll(data)
                adapter.loadMoreCompleted()
                loadMoreCount++
            } else if (loadMoreCount == 1) {
                adapter.loadMoreError()
                loadMoreCount++
            } else {
                adapter.noMore()
            }
        }, 1000)
    }

    private fun bindData2View(holder: ViewHolder, item: OnePiece) {
        holder.setText(R.id.tvDesc, item.desc)
                .with(R.id.ivImage, ViewHolder.Action<ImageView> { view ->
                    if (item.imageRes == -1) item.imageRes = R.mipmap.ic_launcher
                    Glide.with(this@EmptyAndLoadMoreActivity)
                            .load(item.imageRes)
                            .apply(RequestOptions().centerCrop())
                            .into(view)
                })
    }

}
