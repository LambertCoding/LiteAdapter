package me.yuu.liteadapter.entity

/**
 * 分组列表需要实现
 */
interface SectionItem {
    var itemType: SectionItemType
}

enum class SectionItemType {
    HEAD {
        override fun toString(): String {
            return "head"
        }
    },
    BODY {
        override fun toString(): String {
            return "body"
        }
    },
    FOOT {
        override fun toString(): String {
            return "foot"
        }
    },
    SINGLE {
        override fun toString(): String {
            return "single"
        }
    }
}