package com.lhzw.bluetooth.glide;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lhzw.bluetooth.application.App;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * @author huqiang
 * @date 2018/6/11
 * @describe glide工具类
 * @org 十米科技(shimi.com)
 */
public class GlideUtils {

    /**
     * 普通加网络图片带占位符
     *
     * @param context
     * @param view       显示控件
     * @param url        网络地址
     * @param defaultImg 默认图
     */
    public static void showPlaceholder(Context context, ImageView view, String url, int defaultImg) {
        RequestOptions options = new RequestOptions();
        options.placeholder(defaultImg);
        options.error(defaultImg);
        options.centerCrop();
        Glide.with(context).load(url).apply(options).into(view);

    }

    /**
     * 简单动画加载图片
     *
     * @param view
     * @param url
     * @param defaultImg
     */
    public static void showAnimation(ImageView view, String url, int defaultImg) {
        RequestOptions options = new RequestOptions();
        options.placeholder(defaultImg);
        options.error(defaultImg);
        options.centerCrop();
        Glide.with(App.instance).load(url).transition(withCrossFade()).apply(options).into(view);

    }

    /**
     * 加载圆形图片
     *
     * @param url        请求地址
     * @param view       图片控件
     * @param defaultImg 默认图
     */
    public static void showCircle(ImageView view, String url, int defaultImg) {
        RequestOptions options = RequestOptions.circleCropTransform();
        options.placeholder(defaultImg);
        options.error(defaultImg);
        options.centerCrop();
        Glide.with(App.instance).load(TextUtils.isEmpty(url) ? defaultImg : url).apply(options).into(view);
    }

    /**
     * 加载圆形图片，带边框的
     *
     * @param view
     * @param url
     * @param defaultImg
     * @param BorderColor
     */
    public static void showCircleWithBorder(ImageView view, String url, int defaultImg, int BorderColor) {
        RequestOptions options = new RequestOptions();
        options.placeholder(defaultImg);
        options.error(defaultImg);
        options.centerCrop();
        options.transform(new GlideCircleTransform(3, BorderColor));
        Glide.with(App.instance).load(url).apply(options).into(view);
    }

    /**
     * 加载圆角图片
     *
     * @param view
     * @param url
     * @param defaultImg
     * @param RoundDp
     */
    public static void showRound(ImageView view, String url, int defaultImg, int RoundDp) {
        RequestOptions options = new RequestOptions();
        options.placeholder(defaultImg);
        options.error(defaultImg);
        options.centerCrop();
        options.transform(new GlideRoundTransform(RoundDp));
        Glide.with(App.instance).load(url).apply(options).into(view);
    }

    /**
     * 模糊加载图片
     *
     * @param view
     * @param url
     * @param defaultImg
     */
    public static void showIndistinct(ImageView view, String url, int defaultImg) {
        Glide.with(App.instance).load(url).apply(RequestOptions.bitmapTransform(new GlideBlurformation(App.instance))).into(view);
    }

}
