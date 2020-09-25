package me.yuu.liteadapter.ext

import android.content.Context
import me.yuu.liteadapter.advenced.RadioAdapter
import me.yuu.liteadapter.core.LiteAdapter
import me.yuu.liteadapter.core.LiteAdapterEx
import me.yuu.liteadapter.entity.RadioEntity


fun <T> buildAdapter(context: Context, initiator: LiteAdapter<T>.() -> Unit): LiteAdapter<T> {
    return LiteAdapter<T>(context).apply(initiator)
}

fun <T> buildAdapterEx(context: Context, initiator: LiteAdapterEx<T>.() -> Unit): LiteAdapterEx<T> {
    return LiteAdapterEx<T>(context).apply(initiator)
}

fun <T : RadioEntity> buildRadioAdapter(context: Context, initiator: RadioAdapter<T>.() -> Unit): RadioAdapter<T> {
    return RadioAdapter<T>(context).apply(initiator)
}