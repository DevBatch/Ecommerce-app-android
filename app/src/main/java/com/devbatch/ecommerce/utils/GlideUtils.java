package com.devbatch.ecommerce.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.devbatch.ecommerce.R;
import com.devbatch.ecommerce.widgets.LoadingSquaredImageView;

import static com.devbatch.ecommerce.Application.drawable;

import java.io.File;

/**
 * Created by Irfan Arshad on 4/30/2015.
 */
public class GlideUtils {
    public static void setImage(Context context, ImageView imageView, String url) {

        String mUrl = getUrl(url);
        Drawable[] fallbackDrawables = getFallbackDrawables(context);
        int index = Math.abs(mUrl.hashCode() % fallbackDrawables.length);
        Glide.with(context).load(mUrl).error(fallbackDrawables[index])
                .placeholder(fallbackDrawables[index]).centerCrop().into(imageView);
    }

    public static String getUrl(String url) {
        return url == null || TextUtils.isEmpty(url) ? "http:" : url;
    }

    public static void setImage(Context context, ImageView imageView, String url, Drawable fallback) {
        Glide.with(context).load(getUrl(url)).error(fallback).fitCenter()
                .placeholder(fallback).into(imageView);
    }

    public static void setImage1(Context context, ImageView imageView, String url) {
        Glide.with(context).load(getUrl(url)).into(imageView);
    }

    public static void setImage(Context context, ImageView imageView, String url, Drawable fallback, final View wheel) {
        Glide.with(context).load(getUrl(url)).error(fallback).fitCenter()
                .placeholder(fallback).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                wheel.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                wheel.setVisibility(View.GONE);
                return false;
            }
        }).into(imageView);
    }

    public static void setLoadingImage(Context context, final LoadingSquaredImageView imageView, String url, Drawable fallback) {
        imageView.startLoading();
        Glide.with(context).load(getUrl(url)).error(fallback).fitCenter()
                .placeholder(fallback).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                imageView.stopLoading();
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                imageView.stopLoading();
                return false;
            }
        }).into(imageView);
    }

    public static void setLoadingImage(Context context, final LoadingSquaredImageView imageView, String url) {
        imageView.startLoading();
        Glide.with(context).load(getUrl(url)).error(drawable(R.drawable.place_holder_1))
                .listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                imageView.stopLoading();
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                imageView.stopLoading();
                return false;
            }
        }).placeholder(drawable(R.drawable.place_holder_1))
                .centerCrop().into(imageView);
    }

    public static void setImage(Context context, ImageView imageView, String url, Drawable fallback, int size) {
        Glide.with(context).load(getUrl(url)).error(fallback).fitCenter().override(size, size)
                .placeholder(fallback).centerCrop().into(imageView);
    }

    public static void setImage(Context context, ImageView imageView, String url, final View wheel) {
        Glide.with(context).load(getUrl(url)).error(drawable(R.drawable.place_holder_1))
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model,
                                               Target<GlideDrawable> target,
                                               boolean isFirstResource) {
                        wheel.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model,
                                                   Target<GlideDrawable> target, boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        wheel.setVisibility(View.GONE);
                        return false;
                    }
                })
                .placeholder(drawable(R.drawable.place_holder_1))
                .centerCrop().into(imageView);
    }

//    public static void setImage(Context context, KenBurnAspectLockImageView imageView, String url, final View wheel) {
//        Glide.with(context).load(getUrl(url)).error(AppController.drawable(R.drawable.place_holder_1))
//                .listener(new RequestListener<String, GlideDrawable>() {
//                    @Override
//                    public boolean onException(Exception e, String model,
//                                               Target<GlideDrawable> target,
//                                               boolean isFirstResource) {
//                        wheel.setVisibility(View.GONE);
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(GlideDrawable resource, String model,
//                                                   Target<GlideDrawable> target, boolean isFromMemoryCache,
//                                                   boolean isFirstResource) {
//                        wheel.setVisibility(View.GONE);
//                        return false;
//                    }
//                })
//                .placeholder(AppController.drawable(R.drawable.place_holder_1))
//                .centerCrop().into(imageView);
//    }

    public static void setImage(Context context, ImageView imageView, String url, final View wheel, int size) {
        Glide.with(context).load(getUrl(url)).error(drawable(R.drawable.place_holder_1))
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model,
                                               Target<GlideDrawable> target,
                                               boolean isFirstResource) {
                        wheel.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model,
                                                   Target<GlideDrawable> target, boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        wheel.setVisibility(View.GONE);
                        return false;
                    }
                })
                .placeholder(drawable(R.drawable.place_holder_1))
                .centerCrop().override(size, size).into(imageView);
    }

    public static void setSliderImage(Context context, ImageView imageView, String url, final View wheel) {
        Glide.with(context).load(getUrl(url)).error(drawable(R.drawable.place_holder_1))
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model,
                                               Target<GlideDrawable> target,
                                               boolean isFirstResource) {
                        wheel.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model,
                                                   Target<GlideDrawable> target, boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        wheel.setVisibility(View.GONE);
                        return false;
                    }
                })
                .placeholder(drawable(R.drawable.place_holder_1))
                .fitCenter().into(imageView);
    }

    public static void setImage(Context context, ImageView imageView, String url, final View wheel, RoundedCornersTransformation transformation) {
        Glide.with(context).load(getUrl(url)).error(drawable(R.drawable.place_holder_1)).bitmapTransform(transformation)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model,
                                               Target<GlideDrawable> target,
                                               boolean isFirstResource) {
                        wheel.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model,
                                                   Target<GlideDrawable> target, boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        wheel.setVisibility(View.GONE);
                        return false;
                    }
                })
                .placeholder(drawable(R.drawable.place_holder_1))
                .centerCrop().into(imageView);
    }

    public static void setImage(Fragment fragment, ImageView imageView, String url, Drawable fallback) {
        Glide.with(fragment).load(getUrl(url)).error(fallback)
                .placeholder(fallback).centerCrop().into(imageView);
    }

//    public static void setCircleImage(Fragment fragment, ImageView imageView, String url, Drawable fallback) {
//        Glide.with(fragment).load(getUrl(url))
//                .bitmapTransform(new CropCircleTransformation(fragment.getContext())).error(fallback)
//                .placeholder(fallback).into(imageView);
//    }
//
//    public static void setCircleImage(Context context, ImageView imageView, String url, Drawable fallback) {
//        Glide.with(context).load(getUrl(url))
//                .bitmapTransform(new CropCircleTransformation(context))
//                .error(fallback).placeholder(fallback)
//                .into(imageView);
//
//
//    }

    public static void setImage(Context context, ImageView imageView, File file, Drawable fallback) {
        Glide.with(context).load(file).error(fallback)
                .placeholder(fallback).centerCrop().into(imageView);
    }

//    public static void showStaticMap(Context context, ImageView imageView, StaticMap map, final ProgressWheel progress) {
//        progress.setVisibility(View.VISIBLE);
//        Drawable[] fallbackDrawables = getFallbackDrawables(context);
//        int index = Math.abs(map.hashCode() % fallbackDrawables.length);
//        Glide.with(context).load(map)
//                .placeholder(fallbackDrawables[index])
//                .error(R.drawable.frown_cloud)
//                .listener(new RequestListener<StaticMap, GlideDrawable>() {
//                    @Override
//                    public boolean onException(Exception e, StaticMap model, Target<GlideDrawable> target, boolean isFirstResource) {
//                        progress.setVisibility(View.GONE);
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(GlideDrawable resource, StaticMap model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                        progress.setVisibility(View.GONE);
//                        return false;
//                    }
//                })
//                .into(imageView);
//    }

    public static Drawable[] getFallbackDrawables(Context context) {
        Resources resources = context.getResources();
        return new Drawable[]{
                new ColorDrawable(resources.getColor(R.color.md_purple_800)),
                new ColorDrawable(resources.getColor(R.color.md_light_green_600)),
                new ColorDrawable(resources.getColor(R.color.colorPrimary)),
                new ColorDrawable(resources.getColor(R.color.md_orange_600)),
                new ColorDrawable(resources.getColor(R.color.md_red_600))
        };
    }
}
