package me.yuu.liteadapter.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * @author yu
 * @date 2018/1/12
 */

public class LiteAdapterUtils {

    public static int dp2px(Context context, final float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int findLastCompletelyVisibleItemPosition(RecyclerView.LayoutManager layoutManager) {
        int lastPosition;
        if (layoutManager instanceof GridLayoutManager) {
            lastPosition = ((GridLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
            ((StaggeredGridLayoutManager) layoutManager).findLastCompletelyVisibleItemPositions(into);
            lastPosition = findMax(into);
        } else {
            lastPosition = ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
        }
        return lastPosition;
    }

    private static int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }


    /**
     * 根据adapter的方向，为header和footer生成LayoutParams
     *
     * @param orientation
     * @param view
     * @return
     */
    public static RecyclerView.LayoutParams generateLayoutParamsForHeaderAndFooter(int orientation, View view) {

        ViewGroup.LayoutParams oldParams = view.getLayoutParams();
        int marginLeft = 0, marginRight = 0, marginTop = 0, marginBottom = 0;
        if (oldParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) oldParams;
            marginLeft = marginLayoutParams.leftMargin;
            marginRight = marginLayoutParams.rightMargin;
            marginTop = marginLayoutParams.topMargin;
            marginBottom = marginLayoutParams.bottomMargin;
        }

        int width, height;
        if (orientation == OrientationHelper.HORIZONTAL) {
            width = oldParams == null ? ViewGroup.LayoutParams.WRAP_CONTENT : view.getLayoutParams().width;
            height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            width = ViewGroup.LayoutParams.MATCH_PARENT;
            height = oldParams == null ? ViewGroup.LayoutParams.WRAP_CONTENT : view.getLayoutParams().height;
        }

        RecyclerView.LayoutParams newParams = new RecyclerView.LayoutParams(width, height);
        newParams.setMargins(marginLeft, marginTop, marginRight, marginBottom);

        return newParams;
    }

}
