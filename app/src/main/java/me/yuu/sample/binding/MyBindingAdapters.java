package me.yuu.sample.binding;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import androidx.databinding.BindingAdapter;

/**
 * @author yu
 * @date 2019/4/15
 */
public class MyBindingAdapters {

    @BindingAdapter(value = {"bind_imageId", "bind_place_holder"}, requireAll = false)
    public static void loadImage(ImageView imageView, int resId, int placeHolder) {
        Glide.with(imageView.getContext())
                .load(resId)
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(placeHolder)
                        .error(placeHolder)
                )
                .into(imageView);
    }

}
