package me.yuu.liteadapter.loadmore

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import me.yuu.liteadapter.core.Utils

/**
 * @author yu
 * @date 2018/1/12
 */
class DefaultLoadMoreFooter : LinearLayout, ILoadMoreFooter {

    private var mState = ILoadMoreFooter.Status.LOADING
    private var mText: TextView? = null
    private var mProgressBar: ProgressBar? = null
    var str_no_more = "没有更多了"
    var str_loading = "正在加载..."
    var str_error = "出错啦...点击重试"

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    private fun initView(context: Context) {
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                Utils.dp2px(context, 50f))
        setLayoutParams(layoutParams)
        orientation = HORIZONTAL
        gravity = Gravity.CENTER

        mProgressBar = ProgressBar(context).apply {
            this.layoutParams = LayoutParams(Utils.dp2px(context, 25f),
                    Utils.dp2px(context, 25f))
        }
        addView(mProgressBar)

        mText = TextView(context).apply {
            textSize = 14f
            gravity = Gravity.CENTER
            this.layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT).apply {
                leftMargin = Utils.dp2px(context, 10f)
            }
        }
        addView(mText)

        status = ILoadMoreFooter.Status.COMPLETED
    }

    override val view: View
        get() = this

    override var status: ILoadMoreFooter.Status
        get() = mState
        set(state) {
            mState = state
            when (mState) {
                ILoadMoreFooter.Status.LOADING -> {
                    this.visibility = View.VISIBLE
                    mProgressBar!!.visibility = View.VISIBLE
                    mText!!.text = str_loading
                }
                ILoadMoreFooter.Status.COMPLETED -> this.visibility = View.GONE
                ILoadMoreFooter.Status.NO_MORE -> {
                    this.visibility = View.VISIBLE
                    mText!!.text = str_no_more
                    mProgressBar!!.visibility = View.GONE
                }
                ILoadMoreFooter.Status.ERROR -> {
                    this.visibility = View.VISIBLE
                    mText!!.text = str_error
                    mProgressBar!!.visibility = View.GONE
                }
            }
        }
}