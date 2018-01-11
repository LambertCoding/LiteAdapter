package me.yuu.liteadapter;

import android.util.SparseArray;
import android.view.View;

/**
 * 在LiteAdapter的基础上拓展了添加头布局和脚布局的能力，增加了加载更多的功能
 *
 * @author yu
 * @date 2018/1/11
 */
public abstract class LiteAdapterEx<D> {

    private SparseArray<View> mHeaderViews = new SparseArray<>();
    private SparseArray<View> mFooterViews = new SparseArray<>();

}
