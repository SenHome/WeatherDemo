package com.starry.weatherdemo.view;

import android.animation.TypeEvaluator;

/**
 * Created by ivy on 2017/8/11.
 * Description：
 */

public class CircleTypeEvaluator implements TypeEvaluator<CircleInfo> {
    private final CircleInfo mCircleInfo;

    public CircleTypeEvaluator(CircleInfo circleInfo) {
        this.mCircleInfo=circleInfo;
    }

    //动画完成度，开始坐标，结束坐标
    @Override
    public CircleInfo evaluate(float fraction, CircleInfo startValue, CircleInfo endValue) {
        float y = startValue.getY() + fraction * (endValue.getY() - startValue.getY());
        float radius = startValue.getRadius() + fraction * (endValue.getRadius()-startValue.getRadius());
        mCircleInfo.setCircleInfo(mCircleInfo.getX(),y,radius);
        return mCircleInfo;
    }
}
