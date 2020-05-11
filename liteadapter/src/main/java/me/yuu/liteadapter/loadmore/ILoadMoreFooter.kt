package me.yuu.liteadapter.loadmore

import android.view.View
import androidx.annotation.IntDef

/**
 * @author yu
 * @date 2018/1/12
 */
interface ILoadMoreFooter {

    @IntDef(Status.LOADING, Status.COMPLETED, Status.NO_MORE, Status.ERROR)
    annotation class Status {
        companion object {
            const val LOADING = 0
            const val COMPLETED = 1
            const val NO_MORE = 2
            const val ERROR = 3
        }
    }

    @get:Status
    var status: Int

    val view: View
}