package me.yuu.liteadapter.loadmore;

import androidx.annotation.IntDef;

import android.view.View;

/**
 * @author yu
 * @date 2018/1/12
 */
public interface ILoadMoreFooter {

    @IntDef({Status.LOADING, Status.COMPLETED, Status.NO_MORE, Status.ERROR})
    @interface Status {
        int LOADING = 0;
        int COMPLETED = 1;
        int NO_MORE = 2;
        int ERROR = 3;
    }

    @Status
    int getStatus();

    void setStatus(@Status int state);

    View getView();

}
