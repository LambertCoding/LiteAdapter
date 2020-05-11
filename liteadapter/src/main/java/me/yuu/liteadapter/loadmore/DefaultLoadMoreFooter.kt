package me.yuu.liteadapter.loadmore

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import me.yuu.liteadapter.util.LiteAdapterUtils

/**
 * @author yu
 * @date 2018/1/12
 */
class DefaultLoadMoreFooter : LinearLayout, ILoadMoreFooter {
    private var mText: TextView? = null
    private var mProgressBar: ProgressBar? = null
    private var mState = 0

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    override val view: View
        get() = this

    fun initView(context: Context) {
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                LiteAdapterUtils.dp2px(context, 50f))
        setLayoutParams(layoutParams)
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        val pbParams = LayoutParams(LiteAdapterUtils.dp2px(context, 25f), LiteAdapterUtils.dp2px(context, 25f))
        mProgressBar = ProgressBar(context)
        mProgressBar!!.layoutParams = pbParams
        addView(mProgressBar)
        val textParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        textParams.leftMargin = LiteAdapterUtils.dp2px(context, 10f)
        mText = TextView(context)
        mText!!.textSize = 14f
        mText!!.layoutParams = textParams
        mText!!.gravity = Gravity.CENTER
        addView(mText)
        status = ILoadMoreFooter.Status.COMPLETED
    }

    override var status: Int
        get() = mState
        set(state) {
            mState = state
            when (mState) {
                ILoadMoreFooter.Status.LOADING -> {
                    this.visibility = View.VISIBLE
                    mProgressBar!!.visibility = View.VISIBLE
                    mText!!.text = STR_LOADING
                }
                ILoadMoreFooter.Status.COMPLETED -> this.visibility = View.GONE
                ILoadMoreFooter.Status.NO_MORE -> {
                    this.visibility = View.VISIBLE
                    mText!!.text = STR_NO_MORE
                    mProgressBar!!.visibility = View.GONE
                }
                ILoadMoreFooter.Status.ERROR -> {
                    this.visibility = View.VISIBLE
                    mText!!.text = STR_ERROR
                    mProgressBar!!.visibility = View.GONE
                }
                else -> {
                }
            }
        }

    companion object {
        private const val STR_NO_MORE = "没有更多了"
        private const val STR_LOADING = "正在加载..."
        private const val STR_ERROR = "出错啦...点击重试"
    }
}