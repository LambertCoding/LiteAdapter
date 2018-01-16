package me.yuu.liteadapter.loadmore;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.yuu.liteadapter.util.Utils;

/**
 * @author yu
 * @date 2018/1/12
 */
public class DefaultLoadMoreFooter extends LinearLayout implements ILoadMoreFooter {

    private static final String STR_NO_MORE = "没有更多了";
    private static final String STR_LOADING = "正在加载...";
    private static final String STR_ERROR = "出错啦...点击重试";

    private TextView mText;
    private ProgressBar mProgressBar;
    private int mState;

    public DefaultLoadMoreFooter(Context context) {
        super(context);
        initView(context);
    }

    public DefaultLoadMoreFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    @Override
    public View getView() {
        return this;
    }

    public void initView(Context context) {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                Utils.dp2px(context, 50));
        setLayoutParams(layoutParams);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        LayoutParams pbParams = new LayoutParams(Utils.dp2px(context, 25), Utils.dp2px(context, 25));
        mProgressBar = new ProgressBar(context);
        mProgressBar.setLayoutParams(pbParams);
        addView(mProgressBar);

        LayoutParams textParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        textParams.leftMargin = Utils.dp2px(context, 10);
        mText = new TextView(context);
        mText.setTextSize(14);
        mText.setLayoutParams(textParams);
        mText.setGravity(Gravity.CENTER);
        addView(mText);

        setStatus(COMPLETED);
    }

    @Override
    public int getStatus() {
        return mState;
    }

    @Override
    public void setStatus(@Status int state) {
        mState = state;
        switch (mState) {
            case LOADING:
                this.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                mText.setText(STR_LOADING);
                break;
            case COMPLETED:
                this.setVisibility(View.GONE);
                break;
            case NO_MORE:
                this.setVisibility(View.VISIBLE);
                mText.setText(STR_NO_MORE);
                mProgressBar.setVisibility(View.GONE);
                break;
            case ERROR:
                this.setVisibility(View.VISIBLE);
                mText.setText(STR_ERROR);
                mProgressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }
}
