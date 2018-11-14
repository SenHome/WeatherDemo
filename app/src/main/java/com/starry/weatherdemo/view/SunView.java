package com.starry.weatherdemo.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangsen on 2018/10/30.
 */

public class SunView extends View {


    private Paint mPaint;
    private boolean isDrawRing = false;
    private int ringColor = Color.parseColor("#ffcf45");
    private float ringWidth;
    private float minRingCenterWidth;

    private RectF mRectCenterArc;
    private RectF mRectOutSideArc;
    private float centerArcAngle, centerArcEndAngle;
    private float outSideArcAngle, outSideArcStartAngle;
    private boolean isDrawArcLine = false;

    private boolean isDrawSun = false;
    //渐变效果
    private LinearGradient mFlowerLinearGradient,mFlowerRotateLinearGradient,mCloudLinearGradient;
    private float sunWidth;
    private float finalSunWidth;
    private float maxSunFlowerWidth;
    private float sunRotateAngle = 0;
    private RectF mRectFSunFlower;

    private boolean isDrawSunShadow = false;
    private RectF mRectFSunShadow;
    private float sunShadowWidth,sunShadowHeight;
    private int sunShadowColor = Color.parseColor("#bac3c3");


    //绘制云朵
    private boolean isDrawCloud = false;
    private Path mPath;
    private CircleInfo mCircleInfoTopOne,mCircleInfoTopTwo,mCircleInfoBottomOne,mCircleInfoBottomTwo,mCircleInfoBottomThree;
    //云朵阴影
    private boolean isDrawCloudShadow;
    private RectF mRectFCloudShadow;
    private Path mCloudShadowPath;
    private int cloudShadowColor=Color.parseColor("#bc9a31");
    private int cloudShadowAlpha = 0;

    //存储所有动画方便管理
    private ConcurrentHashMap<String,ValueAnimator> animMap = new ConcurrentHashMap<>();
    //动画是否开始
    private boolean isStart = false;

    public SunView(Context context) {
        super(context);
        init();
    }

    public SunView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SunView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SunView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width, (int) (width*1.4f));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);

        //圆弧
        mRectCenterArc = new RectF();
        mRectOutSideArc = new RectF();

        //太阳
        mRectFSunFlower = new RectF();
        //太阳阴影
        mRectFSunShadow = new RectF();

        mPath = new Path();
        mRectFCloudShadow = new RectF();
        mCircleInfoTopOne = new CircleInfo();
        mCircleInfoTopTwo = new CircleInfo();
        mCircleInfoBottomOne = new CircleInfo();
        mCircleInfoBottomTwo = new CircleInfo();
        mCircleInfoBottomThree = new CircleInfo();

        mCloudShadowPath = new Path();


     }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(isDrawRing)
            drewZoomRing(canvas);
        if(isDrawArcLine)
            drawArcLine(canvas);
        if(isDrawSun)
            drawSun(canvas);
        if(isDrawSunShadow)
            drawSunShadow(canvas);
        if(isDrawCloud)
            drawCloud(canvas);
        if(isDrawCloudShadow)
            drawCloudShadow(canvas);
    }

    private void drewZoomRing(Canvas canvas){
        mPaint.setShader(null);
        mPaint.setStrokeWidth(0);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ringColor);
        canvas.drawCircle(getMeasuredWidth()/2,getMeasuredHeight()/2,ringWidth/2,mPaint);
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(getMeasuredWidth()/2,getMeasuredHeight()/2,minRingCenterWidth/2,mPaint);
    }

    private void drawArcLine(Canvas canvas){
        mPaint.setColor(ringColor);
        mPaint.setStyle(Paint.Style.STROKE);//描边
        mPaint.setStrokeCap(Paint.Cap.ROUND);//线条末端圆头
        mPaint.setStrokeWidth(getMeasuredWidth()/40);
        //圆弧角度270，扫描角度2，逆时针增长弧长
        canvas.drawArc(mRectCenterArc,centerArcEndAngle - centerArcAngle,centerArcAngle,false,mPaint);
        mPaint.setStrokeWidth(getMeasuredWidth()/25);
        //绘制圆弧，矩形：两头已经设置为椭圆，圆弧开始的角度180，顺时针扫描的角度90，不链接圆心，画笔。顺时针增长弧长
        canvas.drawArc(mRectOutSideArc,outSideArcStartAngle,outSideArcAngle,false,mPaint);

    }

    //两个渐变方形，一个圆形
    private void drawSun(Canvas canvas){
        mPaint.setStrokeWidth(0);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ringColor);
        mPaint.setShader(mFlowerLinearGradient);//着色，渐变颜色
        canvas.save();//保存当前矩阵到私有堆栈中
        //两个光环用的是同一个矩形。
        //绘制第一个太阳光环
        canvas.rotate(sunRotateAngle,getMeasuredWidth()/2,getMeasuredHeight()/2);
        canvas.drawRect(mRectFSunFlower,mPaint);

        //画布旋转45度，改变着色，绘制第二个光环
        canvas.rotate(45,getMeasuredWidth()/2,getMeasuredHeight()/2);
        mPaint.setShader(mFlowerRotateLinearGradient);
        canvas.drawRect(mRectFSunFlower,mPaint);
        canvas.restore();//恢复初始状态，防治后续影响

        //着色为空，画太阳
        mPaint.setShader(null);
        //sunWidth不断变大，太阳逐渐变大
        canvas.drawCircle(getMeasuredWidth()/2,getMeasuredHeight()/2,sunWidth/2,mPaint);
    }

    private void drawSunShadow(Canvas canvas){
        mPaint.setColor(sunShadowColor);
        mPaint.setStyle(Paint.Style.FILL);
        mRectFSunShadow.set(getMeasuredWidth()/2-sunShadowWidth/2,getMeasuredHeight()-sunShadowHeight,
                getMeasuredWidth()/2+sunShadowWidth/2,getMeasuredHeight());
        canvas.drawOval(mRectFSunShadow,mPaint);//绘制椭圆

    }

    private void drawCloud(Canvas canvas){
        //CircleInfo用于记录每个圆的信息，圆心、半径、是否可见
        mPath.reset();//清除path,成为空
        mPaint.setShader(mCloudLinearGradient);
        if(mCircleInfoBottomOne.isCanDraw())
            //闭合圆形，圆心x,y,半径r,如何定向:CW顺时针方向
            mPath.addCircle(mCircleInfoBottomOne.getX(),mCircleInfoBottomOne.getY(),mCircleInfoBottomOne.getRadius(),Path.Direction.CW);//坐下1
        if(mCircleInfoBottomTwo.isCanDraw())
            mPath.addCircle(mCircleInfoBottomTwo.getX(),mCircleInfoBottomTwo.getY(),mCircleInfoBottomTwo.getRadius(),Path.Direction.CW);
        if(mCircleInfoBottomThree.isCanDraw())
            mPath.addCircle(mCircleInfoBottomThree.getX(),mCircleInfoBottomThree.getY(),mCircleInfoBottomThree.getRadius(),Path.Direction.CW);
        if(mCircleInfoTopOne.isCanDraw())
            mPath.addCircle(mCircleInfoTopOne.getX(),mCircleInfoTopOne.getY(),mCircleInfoTopOne.getRadius(),Path.Direction.CW);
        if(mCircleInfoTopTwo.isCanDraw())
            mPath.addCircle(mCircleInfoTopTwo.getX(),mCircleInfoTopTwo.getY(),mCircleInfoTopTwo.getRadius(),Path.Direction.CW);
        //保存画布状态，之后可以旋转，缩放等操作
        canvas.save();
        //clipRect：矩形做剪辑器，矩形用局部坐标表示
        canvas.clipRect(0,0,getMeasuredWidth(),getMeasuredHeight()/2+getMeasuredWidth()/7f);
        //绘制指定path
        canvas.drawPath(mPath,mPaint);
        //回复状态，防治之前的操作对以后操作的影响
        canvas.restore();
        mPaint.setShader(null);
    }

    private void drawCloudShadow(Canvas canvas){
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(cloudShadowColor);
        mPaint.setAlpha(cloudShadowAlpha);
        canvas.save();
        canvas.clipRect(0,getMeasuredHeight()/2+getMeasuredWidth()/7f,getMeasuredWidth(),getMeasuredHeight());
        mRectFCloudShadow.set(getMeasuredWidth()/2-finalSunWidth/2,getMeasuredHeight()/2-finalSunWidth/2,
                getMeasuredWidth()/2+finalSunWidth/2,getMeasuredHeight()/2+finalSunWidth/2);
        mCloudShadowPath.reset();
        mCloudShadowPath.moveTo(mCircleInfoBottomOne.getX(),getMeasuredHeight()/2+getMeasuredWidth()/7f);
        mCloudShadowPath.arcTo(mRectFCloudShadow,15,45,false);
        canvas.drawPath(mCloudShadowPath,mPaint);
        canvas.restore();
        mPaint.setAlpha(255);
    }

    public static final String ANIM_CLOUD_SHADOW = "anim_cloud_shadow";
    private void startCloudShadow(){
        isDrawCloudShadow = true;
        ValueAnimator valueAnimator = animMap.get(ANIM_CLOUD_SHADOW);
        if(valueAnimator == null){
            valueAnimator = valueAnimator.ofInt(0,255).setDuration(600);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    cloudShadowAlpha = (int) valueAnimator.getAnimatedValue();
                }
            });
            animMap.put(ANIM_CLOUD_SHADOW,valueAnimator);
        }
        startValueAnimator(valueAnimator);
    }


    public static final String ANIM_CLOUD_TOP_ONE="anim_cloud_top_one";
    public static final String ANIM_CLOUD_TOP_TWO="anim_cloud_top_two";
    public static final String ANIM_CLOUD_BOTTOM_ONE="anim_cloud_bottom_one";
    public static final String ANIM_CLOUD_BOTTOM_TWO="anim_cloud_bottom_two";
    public static final String ANIM_CLOUD_BOTTOM_THREE="anim_cloud_bottom_three";
    private void startCloud(){
        isDrawCloud = true;
        //每个圆形开始动画的时间不同，圆形有放大效果
        startCloudTemplate(ANIM_CLOUD_BOTTOM_ONE,0,mCircleInfoBottomOne);
        startCloudTemplate(ANIM_CLOUD_BOTTOM_TWO,200,mCircleInfoBottomTwo);
        startCloudTemplate(ANIM_CLOUD_BOTTOM_THREE,400,mCircleInfoBottomThree);
        startCloudTemplate(ANIM_CLOUD_TOP_ONE,300,mCircleInfoTopOne);
        startCloudTemplate(ANIM_CLOUD_TOP_TWO,350,mCircleInfoTopTwo);
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                //云朵阴影开始
                startCloudShadow();

            }
        },600);

    }

    //云朵圆形动画
    private void startCloudTemplate(String mapTag, long delay, final CircleInfo circleInfo){
        ValueAnimator valueAnimator = animMap.get(mapTag);
        if(valueAnimator == null){
            //估值器，起始值0，终点值：圆形最终半径大小，
            valueAnimator = ValueAnimator.ofObject(new CircleTypeEvaluator(circleInfo),
                    new CircleInfo(circleInfo.getX(),circleInfo.getY()+circleInfo.getRadius(),0),
                    new CircleInfo(circleInfo.getX(),circleInfo.getY(),circleInfo.getRadius()));
            valueAnimator.setDuration(600);
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    //动画开始，可以进行绘制
                    circleInfo.setCanDraw(true);
                }
            });
            animMap.put(mapTag,valueAnimator);
        }
        valueAnimator.setObjectValues(new CircleInfo(circleInfo.getX(),circleInfo.getY()+circleInfo.getRadius(),0),
                new CircleInfo(circleInfo.getX(),circleInfo.getY(),circleInfo.getRadius()));
        valueAnimator.setStartDelay(delay);
        startValueAnimator(valueAnimator);
    }

    public static final String ANIM_WEATHER_SHADOW = "anim_weather_shadow";
    private void startSunShadow(){
        isDrawSunShadow = true;
        ValueAnimator sunShadowAnimator = animMap.get(ANIM_WEATHER_SHADOW);
        if (sunShadowAnimator == null) {
            sunShadowAnimator = ValueAnimator.ofFloat().setDuration(400);
            sunShadowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    sunShadowWidth = (float) valueAnimator.getAnimatedValue();
                }
            });
            animMap.put(ANIM_WEATHER_SHADOW,sunShadowAnimator);
        }
        sunShadowAnimator.setFloatValues(0,getMeasuredWidth(),getMeasuredWidth()* 0.8f);
        startValueAnimator(sunShadowAnimator);
    }

    //太阳动画
    public static final String ANIM_SUN_ZOOM = "anim_sun_zoom";
    public static final String ANIM_FLOWER_ZOOM = "anim_flower_zoom";
    private void startSun(){
        isDrawSun = true;
        //太阳内部圆形
        ValueAnimator sunAnim = animMap.get(ANIM_SUN_ZOOM);
        if(sunAnim == null){
            sunAnim = ValueAnimator.ofFloat().setDuration(400);
            //差值器，中间快，两边慢
            sunAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            sunAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    //sunWidth从0开始
                    sunWidth = (float) valueAnimator.getAnimatedValue();
                }
            });
            animMap.put(ANIM_SUN_ZOOM,sunAnim);
        }
        //sunWidth从0开始，增到到getMeasuredWidth(),在减少到finalSunWidth
        sunAnim.setFloatValues(sunWidth,getMeasuredWidth(),finalSunWidth);
        startValueAnimator(sunAnim);

        //太阳光环
        ValueAnimator flowerAnim = animMap.get(ANIM_FLOWER_ZOOM);
        if(flowerAnim == null){
            flowerAnim = ValueAnimator.ofFloat().setDuration(400);
            flowerAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            flowerAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float width = (float) valueAnimator.getAnimatedValue();
                    //光环left,right,top,bottom
                    mRectFSunFlower.set(getMeasuredWidth()/2f-width/2,getMeasuredHeight()/2f-width/2
                            ,getMeasuredWidth()/2f+width/2,getMeasuredHeight()/2f+width/2);
                }
            });
            flowerAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    //太阳光环旋转
                    startSunRotate();
                }
            });
            flowerAnim.setStartDelay(100);
            animMap.put(ANIM_FLOWER_ZOOM,flowerAnim);
        }
        flowerAnim.setFloatValues(0,maxSunFlowerWidth,maxSunFlowerWidth*0.9f);
        startValueAnimator(flowerAnim);

        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                //云彩动画开始
                startCloud();
            }
        },300);

        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                //阴影动画
                startSunShadow();
            }
        },200);

    }

    //太阳光环旋转
    public static final String ANIM_SUN_ROTATE = "anim_sun_rotate";
    private void startSunRotate(){
        ValueAnimator rotateAnim = animMap.get(ANIM_SUN_ROTATE);
        if(rotateAnim == null){
            rotateAnim = ValueAnimator.ofFloat(0,360f).setDuration(4*1000);
            rotateAnim.setRepeatCount(ValueAnimator.INFINITE);
            rotateAnim.setInterpolator(new LinearInterpolator());
            rotateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    sunRotateAngle = (float) valueAnimator.getAnimatedValue();
                }
            });
            animMap.put(ANIM_SUN_ROTATE,rotateAnim);

        }
        startValueAnimator(rotateAnim);
    }

    public static final String ANIM_ARC_LINE_CENTER_ANGLE = "anim_arc_line_center_angle";
    public static final String ANIM_ARC_LINE_CENTER_MOVE = "anim_arc_line_center_move";
    public static final String ANIM_ARC_LINE_OUTSIZE_ANGEL = "anim_arc_line_outsize_angle";
    public static final String ANIM_ARC_LINE_OUTSIZE_MOVE = "anim_arc_line_outsize_move";
    private void startArcLine(){
        isDrawArcLine = true;
        //内部圆弧长度控制
        ValueAnimator centerArcLineAngleAnim = animMap.get(ANIM_ARC_LINE_CENTER_ANGLE);
        if(centerArcLineAngleAnim == null){
            centerArcLineAngleAnim = ValueAnimator.ofFloat().setDuration(500);
            centerArcLineAngleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    centerArcAngle = (float) valueAnimator.getAnimatedValue();
                }
            });
            animMap.put(ANIM_ARC_LINE_CENTER_ANGLE,centerArcLineAngleAnim);
        }
        //圆弧从2增长到180,在减少到0
        centerArcLineAngleAnim.setFloatValues(centerArcAngle,180,0);
        startValueAnimator(centerArcLineAngleAnim);

        //内部圆弧移动控制
        ValueAnimator centerArcLineMoveAnim = animMap.get(ANIM_ARC_LINE_CENTER_MOVE);
        if(centerArcLineMoveAnim == null){
            centerArcLineMoveAnim = ValueAnimator.ofFloat().setDuration(400);
            centerArcLineMoveAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    centerArcEndAngle = (float) valueAnimator.getAnimatedValue();
                }
            });
            centerArcLineMoveAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    //结束
                    isDrawArcLine = false;
                }
            });
            animMap.put(ANIM_ARC_LINE_CENTER_MOVE,centerArcLineMoveAnim);
        }
        //顺时针移动，从270到270+360=630,即顺时针移动一圈。
        centerArcLineMoveAnim.setFloatValues(centerArcEndAngle,630);
        startValueAnimator(centerArcLineMoveAnim);

        //外部圆弧长度控制
        ValueAnimator outSizeArcLineAngleAnim = animMap.get(ANIM_ARC_LINE_OUTSIZE_ANGEL);
        if(outSizeArcLineAngleAnim == null){
            //动画时间
            outSizeArcLineAngleAnim = ValueAnimator.ofFloat().setDuration(400);
            outSizeArcLineAngleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    //获取扫描角度，开始为90，
                    outSideArcAngle = (float) valueAnimator.getAnimatedValue();
                }
            });
            animMap.put(ANIM_ARC_LINE_OUTSIZE_ANGEL,outSizeArcLineAngleAnim);
        }
        //扫描的角度，即弧长，从90增长到180，在减少到0，圆弧消失
        outSizeArcLineAngleAnim.setFloatValues(outSideArcAngle,180,0);
        startValueAnimator(outSizeArcLineAngleAnim);

        //外部圆弧移动控制，移动比长度变化动画结束要快一点，400毫秒
        ValueAnimator outSizeArcLineMoveAnim = animMap.get(ANIM_ARC_LINE_OUTSIZE_MOVE);
        if(outSizeArcLineMoveAnim == null){
            outSizeArcLineMoveAnim = ValueAnimator.ofFloat().setDuration(300);
            outSizeArcLineMoveAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    outSideArcStartAngle = (float) valueAnimator.getAnimatedValue();
                }
            });
            animMap.put(ANIM_ARC_LINE_OUTSIZE_MOVE,outSizeArcLineMoveAnim);
        }
        //一段动画的角度变化，角度，坐标系顺时针角度变化为正值，180变成-90,左上角圆弧变成右上角圆弧
        outSizeArcLineMoveAnim.setFloatValues(outSideArcStartAngle,-90);
        startValueAnimator(outSizeArcLineMoveAnim);

        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                startSun();
            }
        },250);



    }

    public static final String ANIM_RING_ZOOM = "anim_ring_zoom";
    public static final String ANIM_RING_CIRCLE_ZOOM = "anim_ring_circle_zoom";
    private void startRing(){
        isDrawRing = true;
        ValueAnimator zoomAnim = animMap.get(ANIM_RING_ZOOM);
        if(zoomAnim == null){
            zoomAnim = ValueAnimator.ofFloat().setDuration(500);
            zoomAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    ringWidth = (float) animator.getAnimatedValue();
                }
            });
            zoomAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    //开始圆弧动画
                    startArcLine();
                }
            });

            animMap.put(ANIM_RING_ZOOM,zoomAnim);
        }
        zoomAnim.setFloatValues(ringWidth,getMeasuredWidth()*0.8f);
        startValueAnimator(zoomAnim);

        ValueAnimator circleZoom = animMap.get(ANIM_RING_CIRCLE_ZOOM);
        if(circleZoom == null){
            circleZoom = ValueAnimator.ofFloat().setDuration(300);
            circleZoom.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    //获取当前属性值
                    minRingCenterWidth = (float) valueAnimator.getAnimatedValue();
                }
            });
            circleZoom.setStartDelay(300);
            circleZoom.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isDrawRing = false;
                }
            });
            animMap.put(ANIM_RING_CIRCLE_ZOOM,circleZoom);
        }
        circleZoom.setFloatValues(minRingCenterWidth,getMeasuredWidth()*0.8f);
        startValueAnimator(circleZoom);

    }

    //整个动画开始
    public void startAnim(){
        resetAnim();
        startInvalidateAnim();
    }

    //动画控制失效
    private static final String ANIM_CONTROL_INVALIDATE = "anim_control_invalidate";
    private void startInvalidateAnim() {
        isStart = true;
        ValueAnimator valueAnimator = animMap.get(ANIM_CONTROL_INVALIDATE);
        if(valueAnimator == null){
            //设置浮点之间的动画，比如移动
            valueAnimator = valueAnimator.ofFloat(0,1);
            //动画线性增加
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.setDuration(300);
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    invalidate();
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    //开始圆环放大缩小消失效果
                    startRing();
                }
            });
            animMap.put(ANIM_CONTROL_INVALIDATE,valueAnimator);
        }

        startValueAnimator(valueAnimator);

    }

    private void startValueAnimator(ValueAnimator valueAnimator){
        if(isStart){
            valueAnimator.start();
        }
    }

    /**
     * 重置动画
     */
    private void resetAnim() {
        stopAnimAndRemoveCallbacks();
        isDrawRing = false;
        //圆环部分
        minRingCenterWidth = 10;
        ringWidth = 3 * minRingCenterWidth;

        //圆弧部分
        isDrawArcLine = false;
        centerArcAngle = 2;
        centerArcEndAngle = 270 + centerArcAngle;
        outSideArcStartAngle = 180;
        outSideArcAngle = 90;
        mRectCenterArc.set(getMeasuredWidth()/2-getMeasuredWidth()/6,getMeasuredHeight()/2-getMeasuredWidth()/6
                ,getMeasuredWidth()/2+getMeasuredWidth()/6,getMeasuredHeight()/2+getMeasuredWidth()/6);
        mRectOutSideArc.set(getMeasuredWidth()/2-getMeasuredWidth()/4,getMeasuredHeight()/2-getMeasuredWidth()/4
                ,getMeasuredWidth()/2+getMeasuredWidth()/4,getMeasuredHeight()/2+getMeasuredWidth()/4);


        isDrawSun = false;
        //太阳旋转
        sunRotateAngle = 0;
        //太阳出现
        sunWidth = 0;
        finalSunWidth = getMeasuredWidth()*0.7f;
        maxSunFlowerWidth = (float) Math.sqrt(Math.pow(getMeasuredWidth()/2.0f,2)*2);
        mRectFSunFlower.set(0,0,0,0);
        //起始坐标，终点坐标，开始颜色，结束颜色，titleMode平铺模式，颜色可以写成一个color数组，position数组代表颜色的位置
        mFlowerLinearGradient = new LinearGradient(getMeasuredWidth()/2 - maxSunFlowerWidth/2,getMeasuredHeight()/2-maxSunFlowerWidth/2,
                getMeasuredWidth()/2+maxSunFlowerWidth/2,getMeasuredHeight()/2+maxSunFlowerWidth/2,
                new int[]{Color.parseColor("#fff38e"),Color.parseColor("#ebb228"),Color.parseColor("#ae8200")},new float[]{0,0.5f,1}, Shader.TileMode.REPEAT);
        mFlowerRotateLinearGradient=new LinearGradient(getMeasuredWidth()/2-maxSunFlowerWidth/2,getMeasuredHeight()/2-maxSunFlowerWidth/2,
                getMeasuredWidth()/2+maxSunFlowerWidth/2,getMeasuredHeight()/2-maxSunFlowerWidth/2,
                Color.parseColor("#f7b600") ,Color.parseColor("#ae8200"), Shader.TileMode.REPEAT);

        //太阳阴影
        isDrawSunShadow = false;
        sunShadowHeight = getMeasuredWidth()/25;
        //云
        isDrawCloud = false;
        mCircleInfoBottomOne.setCircleInfo(getMeasuredWidth()/2,getMeasuredHeight()/2+getMeasuredWidth()/10,getMeasuredWidth()/10,false);//左下1
        mCircleInfoTopOne.setCircleInfo(getMeasuredWidth()/2+getMeasuredWidth()/14,getMeasuredHeight()/2-getMeasuredWidth()/30,getMeasuredWidth()/9,false);//顶1
        mCircleInfoBottomTwo.setCircleInfo(getMeasuredWidth()/2+getMeasuredWidth()/10*1.8f,getMeasuredHeight()/2+getMeasuredWidth()/10,getMeasuredWidth()/9,false);//底部2
        mCircleInfoTopTwo.setCircleInfo(getMeasuredWidth()/2+getMeasuredWidth()/10*2.2f,getMeasuredHeight()/2-getMeasuredWidth()/120,getMeasuredWidth()/10,false);//顶2
        mCircleInfoBottomThree.setCircleInfo(getMeasuredWidth()/2+getMeasuredWidth()/10*3.5f,getMeasuredHeight()/2+getMeasuredWidth()/10,getMeasuredWidth()/12,false);//底3
        mCloudLinearGradient =new LinearGradient(mCircleInfoBottomOne.getX()-mCircleInfoBottomOne.getRadius(),mCircleInfoTopOne.getY()-mCircleInfoTopOne.getRadius(),getMeasuredWidth(),getMeasuredHeight()/2+getMeasuredWidth()/7f,
                new int[]{Color.parseColor("#fcfbf3"),Color.parseColor("#efebdf") ,Color.parseColor("#d7d7c7")},new float[]{0,0.5f,1}, Shader.TileMode.REPEAT);
        //云阴影
        isDrawCloudShadow = false;
        cloudShadowAlpha = 0;

        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimAndRemoveCallbacks();
    }

    private void stopAnimAndRemoveCallbacks(){
        isStart=false;
        for (Map.Entry<String, ValueAnimator> entry : animMap.entrySet()) {
            entry.getValue().end();
        }
        Handler handler=this.getHandler();
        if (handler!=null){
            handler.removeCallbacksAndMessages(null);
        }
    }


}
