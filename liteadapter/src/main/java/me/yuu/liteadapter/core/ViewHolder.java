package me.yuu.liteadapter.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;


/**
 * ViewHolder操作子视图的实现类
 *
 * @author yu
 */
public class ViewHolder extends RecyclerView.ViewHolder {

    /**
     * 缓存子视图,key为view id, 值为View。
     */
    private SparseArray<View> mCahceViews = new SparseArray<>();

    public ViewHolder(View itemView) {
        super(itemView);
    }

    public View getItemView() {
        return itemView;
    }

    public Context getContext() {
        return itemView.getContext();
    }

    public interface Action<V extends View> {
        void doAction(V view);
    }

    /**
     * 根据id查找view
     */
    public <T extends View> T findById(int viewId) {
        View target = mCahceViews.get(viewId);
        if (target == null) {
            target = itemView.findViewById(viewId);
            mCahceViews.put(viewId, target);
        }
        return (T) target;
    }

    public <V extends View> ViewHolder with(int viewId, Action<V> action) {
        action.doAction((V) findById(viewId));
        return this;
    }

    /**
     * @param viewId
     * @param stringId
     */
    public ViewHolder setText(int viewId, int stringId) {
        TextView textView = findById(viewId);
        textView.setText(stringId);
        return this;
    }

    public ViewHolder setText(int viewId, String text) {
        TextView textView = findById(viewId);
        textView.setText(text);
        return this;
    }


    public ViewHolder setText(int viewId, CharSequence text) {
        TextView textView = findById(viewId);
        textView.setText(text);
        return this;
    }

    public ViewHolder setTextColor(int viewId, int color) {
        TextView textView = findById(viewId);
        textView.setTextColor(color);
        return this;
    }

    /**
     * @param viewId
     * @param color
     */
    public ViewHolder setBackgroundColor(int viewId, int color) {
        View target = findById(viewId);
        target.setBackgroundColor(color);
        return this;
    }

    public ViewHolder setBackgroundResource(int viewId, int resId) {
        View target = findById(viewId);
        target.setBackgroundResource(resId);
        return this;
    }

    public ViewHolder setBackgroundDrawable(int viewId, Drawable drawable) {
        View target = findById(viewId);
        target.setBackgroundDrawable(drawable);
        return this;
    }

    @TargetApi(16)
    public ViewHolder setBackground(int viewId, Drawable drawable) {
        View target = findById(viewId);
        target.setBackground(drawable);
        return this;
    }

    public ViewHolder setImageBitmap(int viewId, Bitmap bitmap) {
        ImageView target = findById(viewId);
        target.setImageBitmap(bitmap);
        return this;
    }

    public ViewHolder setImageResource(int viewId, int resId) {
        ImageView target = findById(viewId);
        target.setImageResource(resId);
        return this;
    }

    public ViewHolder setImageDrawable(int viewId, Drawable drawable) {
        ImageView target = findById(viewId);
        target.setImageDrawable(drawable);
        return this;
    }

    public ViewHolder setImageDrawable(int viewId, Uri uri) {
        ImageView target = findById(viewId);
        target.setImageURI(uri);
        return this;
    }

    @TargetApi(16)
    public ViewHolder setImageAlpha(int viewId, int alpha) {
        ImageView target = findById(viewId);
        target.setImageAlpha(alpha);
        return this;
    }

    /**
     * @param viewId
     * @param checked
     */
    public ViewHolder setChecked(int viewId, boolean checked) {
        Checkable checkable = findById(viewId);
        checkable.setChecked(checked);
        return this;
    }

    public ViewHolder setProgress(int viewId, int progress) {
        ProgressBar view = findById(viewId);
        view.setProgress(progress);
        return this;
    }

    public ViewHolder setProgress(int viewId, int progress, int max) {
        ProgressBar view = findById(viewId);
        view.setMax(max);
        view.setProgress(progress);
        return this;
    }

    public ViewHolder setMax(int viewId, int max) {
        ProgressBar view = findById(viewId);
        view.setMax(max);
        return this;
    }

    public ViewHolder setRating(int viewId, float rating) {
        RatingBar view = findById(viewId);
        view.setRating(rating);
        return this;
    }

    public ViewHolder setSelected(int viewId, boolean isSelected) {
        View view = findById(viewId);
        view.setSelected(isSelected);
        return this;
    }

    public ViewHolder setRating(int viewId, float rating, int max) {
        RatingBar view = findById(viewId);
        view.setMax(max);
        view.setRating(rating);
        return this;
    }

    public ViewHolder setVisibility(int viewId, int visibility) {
        View view = findById(viewId);
        view.setVisibility(visibility);
        return this;
    }

    /**
     * @param viewId
     * @param listener
     */
    public ViewHolder setOnClickListener(int viewId, View.OnClickListener listener) {
        View view = findById(viewId);
        view.setOnClickListener(listener);
        return this;
    }

    public ViewHolder setOnTouchListener(int viewId, View.OnTouchListener listener) {
        View view = findById(viewId);
        view.setOnTouchListener(listener);
        return this;
    }

    public ViewHolder setOnLongClickListener(int viewId, View.OnLongClickListener listener) {
        View view = findById(viewId);
        view.setOnLongClickListener(listener);
        return this;
    }

    public ViewHolder setOnItemClickListener(int viewId, AdapterView.OnItemClickListener listener) {
        AdapterView view = findById(viewId);
        view.setOnItemClickListener(listener);
        return this;
    }

    public ViewHolder setOnItemLongClickListener(int viewId, AdapterView.OnItemLongClickListener listener) {
        AdapterView view = findById(viewId);
        view.setOnItemLongClickListener(listener);
        return this;
    }

    public ViewHolder setOnItemSelectedClickListener(int viewId, AdapterView.OnItemSelectedListener listener) {
        AdapterView view = findById(viewId);
        view.setOnItemSelectedListener(listener);
        return this;
    }
}
