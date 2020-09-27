package me.yuu.liteadapter.advenced

import android.content.Context
import me.yuu.liteadapter.core.LiteAdapterEx
import me.yuu.liteadapter.entity.SelectableItem

/**
 * 单选的列表，数据需要实现RadioEntity，并且一开始数据集合只能有一个或者没有选中项
 */
open class RadioAdapter<T : SelectableItem>(context: Context) : LiteAdapterEx<T>(context) {

    private var checkedIndex = -1
    var checkedItem: T? = null
        private set

    override fun updateData(items: List<T>) {
        super.updateData(items)
        checkedItem = items.find { it.isSelected }
    }

    /**
     * 选中某项
     */
    open fun check(index: Int, item: T) {
        if (checkedIndex == index) {
            return
        }

        modify(index) {
            it.isSelected = true
        }
        if (checkedIndex != -1) {
            modify(checkedIndex) {
                it.isSelected = false
            }
        }

        this.checkedItem = item
        this.checkedIndex = index
    }
}