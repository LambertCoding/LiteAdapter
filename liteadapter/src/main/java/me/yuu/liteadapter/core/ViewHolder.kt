package me.yuu.liteadapter.core

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.SparseArray
import android.view.View
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.recyclerview.widget.RecyclerView

/**
 * ViewHolder操作子视图的实现类
 *
 * @author yu
 * @date 2018/1/12
 */
open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    /**
     * 缓存子视图,key为view id, 值为View。
     */
    private val mCacheViews = SparseArray<View>()

    val context: Context = itemView.context

    /**
     * 根据id查找view
     */
    fun <T : View?> findById(viewId: Int): T? {
        var target = mCacheViews[viewId]
        if (target == null) {
            target = itemView.findViewById(viewId)
            mCacheViews.put(viewId, target)
        }
        return target as T?
    }

    fun <V : View> doAction(viewId: Int, action: (V)->Unit): ViewHolder {
        action(findById<View>(viewId) as V)
        return this
    }

    /**
     * @param viewId
     * @param stringId
     */
    fun setText(viewId: Int, stringId: Int): ViewHolder {
        findById<TextView>(viewId)?.setText(stringId)
        return this
    }

    fun setText(viewId: Int, text: String?): ViewHolder {
        findById<TextView>(viewId)?.text = text
        return this
    }

    fun setText(viewId: Int, text: CharSequence?): ViewHolder {
        findById<TextView>(viewId)?.text = text
        return this
    }

    fun setTextColor(viewId: Int, color: Int): ViewHolder {
        findById<TextView>(viewId)?.setTextColor(color)
        return this
    }

    /**
     * @param viewId
     * @param color
     */
    fun setBackgroundColor(viewId: Int, color: Int): ViewHolder {
        findById<View>(viewId)?.setBackgroundColor(color)
        return this
    }

    fun setBackgroundResource(viewId: Int, resId: Int): ViewHolder {
        findById<View>(viewId)?.setBackgroundResource(resId)
        return this
    }

    fun setBackgroundDrawable(viewId: Int, drawable: Drawable?): ViewHolder {
        findById<View>(viewId)?.setBackgroundDrawable(drawable)
        return this
    }

    @TargetApi(16)
    fun setBackground(viewId: Int, drawable: Drawable?): ViewHolder {
        findById<View>(viewId)?.background = drawable
        return this
    }

    fun setImageBitmap(viewId: Int, bitmap: Bitmap?): ViewHolder {
        findById<ImageView>(viewId)?.setImageBitmap(bitmap)
        return this
    }

    fun setImageResource(viewId: Int, resId: Int): ViewHolder {
        findById<ImageView>(viewId)?.setImageResource(resId)
        return this
    }

    fun setImageDrawable(viewId: Int, drawable: Drawable?): ViewHolder {
        findById<ImageView>(viewId)?.setImageDrawable(drawable)
        return this
    }

    fun setImageDrawable(viewId: Int, uri: Uri?): ViewHolder {
        findById<ImageView>(viewId)?.setImageURI(uri)
        return this
    }

    @TargetApi(16)
    fun setImageAlpha(viewId: Int, alpha: Int): ViewHolder {
        findById<ImageView>(viewId)?.imageAlpha = alpha
        return this
    }

    /**
     * @param viewId
     * @param checked
     */
    fun setChecked(viewId: Int, checked: Boolean): ViewHolder {
        findById<View>(viewId)?.apply {
            if (this is Checkable) {
                isChecked = checked
            }
        }
        return this
    }

    fun setProgress(viewId: Int, progress: Int): ViewHolder {
        findById<ProgressBar>(viewId)?.progress = progress
        return this
    }

    fun setProgress(viewId: Int, progress: Int, max: Int): ViewHolder {
        findById<ProgressBar>(viewId)?.let {
            it.progress = progress
            it.max = max
        }
        return this
    }

    fun setMax(viewId: Int, max: Int): ViewHolder {
        findById<ProgressBar>(viewId)?.max = max
        return this
    }

    fun setRating(viewId: Int, rating: Float): ViewHolder {
        findById<RatingBar>(viewId)?.rating = rating
        return this
    }

    fun setSelected(viewId: Int, isSelected: Boolean): ViewHolder {
        findById<View>(viewId)?.isSelected = isSelected
        return this
    }

    fun setRating(viewId: Int, rating: Float, max: Int): ViewHolder {
        findById<RatingBar>(viewId)?.let {
            it.max = max
            it.rating = rating
        }
        return this
    }

    fun setVisibility(viewId: Int, visibility: Int): ViewHolder {
        findById<View>(viewId)?.visibility = visibility
        return this
    }

    /**
     * @param viewId
     * @param listener
     */
    fun setOnClickListener(viewId: Int, listener: View.OnClickListener?): ViewHolder {
        findById<View>(viewId)?.setOnClickListener(listener)
        return this
    }

    fun setOnTouchListener(viewId: Int, listener: OnTouchListener?): ViewHolder {
        findById<View>(viewId)?.setOnTouchListener(listener)
        return this
    }

    fun setOnLongClickListener(viewId: Int, listener: OnLongClickListener?): ViewHolder {
        findById<View>(viewId)?.setOnLongClickListener(listener)
        return this
    }

    fun setOnItemClickListener(viewId: Int, listener: AdapterView.OnItemClickListener?): ViewHolder {
        findById<AdapterView<*>>(viewId)?.onItemClickListener = listener
        return this
    }

    fun setOnItemLongClickListener(viewId: Int, listener: AdapterView.OnItemLongClickListener?): ViewHolder {
        findById<AdapterView<*>>(viewId)?.onItemLongClickListener = listener
        return this
    }

    fun setOnItemSelectedClickListener(viewId: Int, listener: OnItemSelectedListener?): ViewHolder {
        findById<AdapterView<*>>(viewId)?.onItemSelectedListener = listener
        return this
    }
}