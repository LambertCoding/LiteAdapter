package me.yuu.sample.entity

import me.yuu.liteadapter.entity.SectionItem
import me.yuu.liteadapter.entity.SectionItemType

/**
 * @author yu
 * @date 2018/1/16
 */
class SimpleSectionItem : SectionItem {

    override var itemType: SectionItemType = SectionItemType.BODY

    override fun toString(): String {
        return itemType.toString()
    }
}