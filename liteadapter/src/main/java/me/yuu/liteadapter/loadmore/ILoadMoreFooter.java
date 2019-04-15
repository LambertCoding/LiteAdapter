package me.yuu.liteadapter.loadmore;

import androidx.annotation.IntDef;
import android.view.View;

/**
 * @author yu
 * @date 2018/1/12
 */
public interface ILoadMoreFooter {

    int LOADING = 0;
    int COMPLETED = 1;
    int NO_MORE = 2;
    int ERROR = 3;

    @IntDef({LOADING, COMPLETED, NO_MORE, ERROR})
    @interface Status {
    }

    @Status
    int getStatus();

    void setStatus(@Status int state);

    View getView();

}
