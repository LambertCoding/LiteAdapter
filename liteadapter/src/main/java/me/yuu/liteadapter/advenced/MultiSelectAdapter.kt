package me.yuu.liteadapter.advenced

import android.content.Context
import me.yuu.liteadapter.core.LiteAdapterEx
import me.yuu.liteadapter.entity.SelectableItem

/**
 * 多选列表
 */
open class MultiSelectAdapter<T : SelectableItem>(context: Context) : LiteAdapterEx<T>(context) {

    /**
     * 全选
     */
    open fun selectAll() {
        mDataSet.forEach {
            it.isSelected = true
        }
        notifyDataSetChanged()
    }

    /**
     * 反选
     */
    open fun invertSelect() {
        mDataSet.forEach {
            it.isSelected = !it.isSelected
        }
        notifyDataSetChanged()
    }

    open fun toggle(index: Int) {
        modify(index) {
            it.isSelected = !it.isSelected
        }
    }

}