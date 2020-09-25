package me.yuu.liteadapter.ext

import android.content.Context
import me.yuu.liteadapter.advenced.RadioAdapter
import me.yuu.liteadapter.advenced.SectionAdapter
import me.yuu.liteadapter.core.LiteAdapter
import me.yuu.liteadapter.core.LiteAdapterEx
import me.yuu.liteadapter.entity.RadioItem
import me.yuu.liteadapter.entity.SectionItem


/**
 * 基础列表，仅包含多布局
 */
fun <T> buildAdapter(context: Context, initiator: LiteAdapter<T>.() -> Unit): LiteAdapter<T> {
    return LiteAdapter<T>(context).apply(initiator)
}

/**
 * 扩展列表，包含 footer & header & empty view & auto load more 等扩展功能
 */
fun <T> buildAdapterEx(context: Context, initiator: LiteAdapterEx<T>.() -> Unit): LiteAdapterEx<T> {
    return LiteAdapterEx<T>(context).apply(initiator)
}

/**
 * 单选列表
 */
fun <T : RadioItem> buildRadioAdapter(context: Context, initiator: RadioAdapter<T>.() -> Unit): RadioAdapter<T> {
    return RadioAdapter<T>(context).apply(initiator)
}

/**
 * 分组列表
 */
fun <T : SectionItem> buildSectionAdapter(context: Context, initiator: SectionAdapter<T>.() -> Unit): SectionAdapter<T> {
    return SectionAdapter<T>(context).apply(initiator)
}