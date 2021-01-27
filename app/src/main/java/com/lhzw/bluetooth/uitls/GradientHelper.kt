package com.lhzw.bluetooth.uitls

import android.graphics.Color

/**
 *
@author：created by xtqb
@description:
@date : 2019/11/26 14:57
 *
 */

class GradientHelper(private var startColor: Int, private var endColor: Int) {
    private var i = -1;
    private var gradient = ArrayList<RGB>()
    private var totalPoint: Int = 500
    /**
     * totalDistance = 0//渐变的范围
     * startColor = 0//渐变的起始颜色
     * endColor = //渐变的结束颜色
     */
    /**
     * 获取当前渐变颜色
     * @return 当前颜色的RGB值
     */

    fun retPoint(totalPoint : Int){
        this.totalPoint = totalPoint
    }

    fun retValue(value : Int){
        i = value
    }

    fun retColor(startColor : Int, endColor : Int){
        this.startColor = startColor
        this.endColor = endColor
    }

    fun getGradient(): Int {
        i++;
        var gradient_R = 0
        var gradient_G = 0
        var gradient_B = 0
        var delta_R = Color.red(startColor) - Color.red(endColor);
        var delta_G = Color.green(startColor) - Color.green(endColor);
        var delta_B = Color.blue(startColor) - Color.blue(endColor);

        if (i <= totalPoint) {
            gradient_R = Color.red(endColor) + delta_R * i / totalPoint;
            gradient_G = Color.green(endColor) + delta_G * i / totalPoint;
            gradient_B = Color.blue(endColor) + delta_B * i / totalPoint;
            gradient.add(RGB(gradient_R, gradient_G, gradient_B));
        } else if (i <= totalPoint * 2 + 1) {
            var rgb_1 = gradient.get(gradient.size - i % (totalPoint + 1) - 1);
            gradient_R = rgb_1.gradient_R;
            gradient_G = rgb_1.gradient_G;
            gradient_B = rgb_1.gradient_B;
        } else {
            var rgb_2 = gradient.get(0);
            gradient_R = rgb_2.gradient_R;
            gradient_G = rgb_2.gradient_R;
            gradient_B = rgb_2.gradient_R;
            gradient.clear();
            i = -1;
        }
        return Color.argb(255, gradient_R, gradient_G, gradient_B);
    }

    private data class RGB(var gradient_R: Int, var gradient_G: Int, var gradient_B: Int) {}
}