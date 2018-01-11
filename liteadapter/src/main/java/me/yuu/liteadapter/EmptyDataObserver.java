package me.yuu.liteadapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author yu
 *         Create on 2018/1/11.
 */

public class EmptyDataObserver extends RecyclerView.AdapterDataObserver {

    private View mEmptyView;

    public EmptyDataObserver(View emptyView) {
        this.mEmptyView = emptyView;
    }

    @Override
    public void onChanged() {
        // TODO: 2018/1/11  
    }
}
