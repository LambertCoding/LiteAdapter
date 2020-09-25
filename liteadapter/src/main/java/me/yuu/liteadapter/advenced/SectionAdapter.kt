package me.yuu.liteadapter.advenced

import android.content.Context
import me.yuu.liteadapter.core.LiteAdapterEx
import me.yuu.liteadapter.entity.SectionItem
import me.yuu.liteadapter.entity.SectionItemType

/**
 * 分组的列表
 */
open class SectionAdapter<T : SectionItem>(context: Context) : LiteAdapterEx<T>(context) {

    /**
     * 处理分组数据，处理其实就是为对象的itemType字段赋值，然后在绑定数据的时候可以通过不同的itemType区分不同的布局类型
     * 其中分组的第一个为头，中间的为body，最后一个为脚，如果该分组只有一条数据，itemType 为 SINGLE
     * @param sectionList 每一个集合是一个分组
     */
    fun addSectionData(vararg sectionList: List<T>) {
        val dataList = mutableListOf<T>()
        sectionList.forEach {
            if (it.isNotEmpty()) {
                if (it.size == 1) {
                    it[0].itemType = SectionItemType.SINGLE
                } else {
                    it.forEachIndexed { index, sectionItem ->
                        when (index) {
                            0 -> sectionItem.itemType = SectionItemType.HEAD
                            it.size - 1 -> sectionItem.itemType = SectionItemType.FOOT
                            else -> sectionItem.itemType = SectionItemType.BODY
                        }
                    }
                }
                dataList.addAll(it)
            }
        }
        addAll(dataList)
    }
}