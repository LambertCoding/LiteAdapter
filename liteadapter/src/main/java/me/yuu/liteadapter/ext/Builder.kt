package me.yuu.liteadapter.ext

import android.content.Context
import me.yuu.liteadapter.core.LiteAdapter
import me.yuu.liteadapter.core.LiteAdapterEx


fun <T> buildAdapter(context: Context, initiator: LiteAdapter<T>.() -> Unit): LiteAdapter<T> {
    return LiteAdapter<T>(context).apply(initiator)
}

fun <T> buildAdapterEx(context: Context, initiator: LiteAdapterEx<T>.() -> Unit): LiteAdapterEx<T> {
    return LiteAdapterEx<T>(context).apply(initiator)
}