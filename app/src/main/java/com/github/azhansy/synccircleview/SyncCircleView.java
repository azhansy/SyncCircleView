package com.github.azhansy.synccircleview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by zhanshuyong on 2016/10/19.
 * 同步 进度圈
 */
public class SyncCircleView extends View {
    private static final String TAG = SyncCircleView.class.getSimpleName();
    private int radius = 72; //半径
    private int textSize = 21; //sp
    private int percentTextSize = 18; //sp
    private float arcPadding = 8 + 5 / 2; //笔宽5，要预留笔宽的一半才能完全显示padding
    private int circle_color = Color.parseColor("#C6EBFC");
    private int circle_progress_color = Color.parseColor("#00d4b2");
    private int text_color = Color.parseColor("#00A8FF");
    private int arc_color = Color.parseColor("#d6f0fe");
    private int text2percent = dp2px(getContext(), 9);
    //双弧起始角度
    private float arc1Angle = -40f;
    private float arc2Angle = 140f;

    private int[] colors = new int[]{0xFF05caf3, 0xFF00d7a0, 0xFF06e154, 0xFF99ea0a, 0xFF05caf3};

//    private float[] positions = new float[]{0.4f, 0.5f, 0.6f, 0.7f,0.4f};
    /**
     * 浅色圈线宽  5dp
     */
    private int circle_stroke_width = 5;

    /**
     * 进度圈背景paint
     */
    private Paint circleBgPaint;
    /**
     * 进度圈进度paint
     */
    private Paint circleProgressPaint;
    private Paint arcProgressPaint;
    private Paint arcAnglePaint;// 两个三角形
    /**
     * 百分数字体paint
     */
    private Paint percentTextPaint;
    /**
     * 提示字paint
     */
    private Paint tipTextPaint;
    /*勾的提示*/
    private Paint gouPaint;
    /**
     * 背景paint
     */
    private Paint backgroundPaint;
    /**
     * 百分数的矩形
     */
    private RectF progressRectF = new RectF();
    private RectF colorProgressRectF = new RectF();
    private RectF arcRectF = new RectF();
    private Rect tipTextBoundsRect = new Rect();

    private Rect percentTextBoundsRect = new Rect();

    private int percent = 0;
    private String percentText = "0%";

    public enum SyncType {
        BACKUP,//开始备份
        BACKUPING,//正在备份
        BACKUPED,//备份完成
        SYNC,//开始同步
        SYNCING,//正在同步
        SYNCED,//同步完成
        RECOVER,//开始恢复
        RECONVERING, //正在恢复
        RECOVERED //恢复成功
    }
    /*实际项目中，这里分了三种不同的接口，现在简化说明实现*/
    public String getSyncTypeText(SyncType type) {
        switch (type) {
            case BACKUP:
                return "开始备份";
            case BACKUPING:
                return "正在备份";
            case BACKUPED:
                return "备份完成";
            case SYNC:
                return "开始同步";
            case SYNCING:
                return "正在同步";
            case SYNCED:
                return "同步完成";
            case RECOVER:
                return "开始恢复";
            case RECONVERING:
                return "正在恢复";
            case RECOVERED:
                return "恢复成功";
            default:
                return "开始同步";
        }
    }

    public SyncCircleView(Context context) {
        this(context, null);
    }

    public SyncCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SyncCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
        initPaint();

    }

    private void init(Context context, AttributeSet attrs) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        radius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius, metrics);
        circle_stroke_width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, circle_stroke_width, metrics);
        textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, metrics);
        percentTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, percentTextSize, metrics);
        arcPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, arcPadding, metrics);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SyncCircleView);
        try {
            setRadius(a.getDimensionPixelSize(R.styleable.SyncCircleView_circle_radius, radius));
            setTextSize(a.getDimensionPixelSize(R.styleable.SyncCircleView_text_size, textSize));
            setCircle_stroke_width(a.getDimensionPixelSize(R.styleable.SyncCircleView_circle_stroke_width, circle_stroke_width));
            setArcPadding(a.getDimension(R.styleable.SyncCircleView_arc_padding, arcPadding));
            setCircle_color(a.getColor(R.styleable.SyncCircleView_circle_color, circle_color));
            setCircle_progress_color(a.getColor(R.styleable.SyncCircleView_circle_progress_color, circle_progress_color));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            a.recycle();
        }
    }

    private void initPaint() {
        circleBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleBgPaint.setColor(circle_color);
        circleBgPaint.setStyle(Paint.Style.STROKE);
        circleBgPaint.setStrokeWidth(circle_stroke_width);

        circleProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleProgressPaint.setColor(circle_progress_color);
        circleProgressPaint.setStyle(Paint.Style.STROKE);
        circleProgressPaint.setStrokeWidth(circle_stroke_width);
        circleProgressPaint.setStrokeCap(Paint.Cap.ROUND);// 笔刷样式为圆形

        arcProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcProgressPaint.setColor(arc_color);
        arcProgressPaint.setStyle(Paint.Style.STROKE);
        arcProgressPaint.setStrokeWidth(circle_stroke_width);
        arcProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        arcAnglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcAnglePaint.setColor(arc_color);
        arcAnglePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        arcProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        arcAnglePaint.setStrokeWidth(circle_stroke_width);

        percentTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        percentTextPaint.setTextSize(percentTextSize);
        percentTextPaint.setColor(text_color);

        tipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tipTextPaint.setTextSize(textSize);
        tipTextPaint.setColor(text_color);

        gouPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gouPaint.setTextSize(textSize);
        gouPaint.setColor(text_color);
        gouPaint.setStrokeWidth(circle_stroke_width);
        gouPaint.setStrokeCap(Paint.Cap.ROUND);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.parseColor("#ffffff"));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取尺寸最小的做正方形
        int min = Math.min(onMeasureOrigin(widthMeasureSpec), onMeasureOrigin(heightMeasureSpec));
        Log.d(TAG, "直径为： " + min);
        progressRectF.set(0, 0, min, min);
        colorProgressRectF.set(circle_stroke_width / 2, circle_stroke_width / 2, min - circle_stroke_width / 2, min - circle_stroke_width / 2);
        arcRectF.set(arcPadding, arcPadding, getRadius() * 2 - arcPadding, getRadius() * 2 - arcPadding);
        setMeasuredDimension(min, min);
    }

    private int onMeasureOrigin(int origin) {
        int specMode = MeasureSpec.getMode(origin); //得到的模式
        int specSize = MeasureSpec.getSize(origin); //得到的尺寸
        Log.d(TAG, "onMeasureOrigin:  " + specMode + " : " + specSize);
        switch (specMode) {
            //准确知道该视图的尺寸 如：andorid:layout_width="48dp"，或者为match_parent
            case MeasureSpec.EXACTLY:
                radius = specSize / 2;
                break;
            //size给出了父控件允许的最大尺寸 如：wrap_content  （这里直接使用默认的defaultResult）
            case MeasureSpec.AT_MOST:
                break;
            //父类没有给大小它，It can be whatever size it wants
            case MeasureSpec.UNSPECIFIED:
                break;
        }
        return radius * 2;
    }

    /**
     * @param canvas 画开始时的双弧
     * @param width  画板的宽
     * @param height 画板的高
     */
    private void drawTwoArc(Canvas canvas, int width, int height) {
        //画开始的双弧
        canvas.drawArc(arcRectF, arc1Angle, 170f, false, arcProgressPaint);
        canvas.drawArc(arcRectF, arc2Angle, 170f, false, arcProgressPaint);
        //画两个三角
        float arcCos1 = (float) Math.cos(arc1Angle * Math.PI / 180) * (getRadius() - arcPadding);
        float arcSin1 = (float) Math.sin(arc2Angle * Math.PI / 180) * (getRadius() - arcPadding);
        Log.d(TAG, "onDraw: arcCos1: " + arcCos1 + "arcSin1: " + arcSin1);
        Path path1 = new Path();
        path1.moveTo(width / 2 + arcCos1, height / 2 - arcSin1);// 此点为多边形的起点
        path1.lineTo(width / 2 + arcCos1, height / 2 - arcSin1 + dp2px(getContext(), 8));
        path1.lineTo(width / 2 + arcCos1 + dp2px(getContext(), 2), height / 2 - arcSin1 + dp2px(getContext(), 7));
        path1.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path1, arcAnglePaint);

        Path path2 = new Path();
        path2.moveTo(width / 2 - arcCos1, height / 2 + arcSin1);// 此点为多边形的起点
        path2.lineTo(width / 2 - arcCos1, height / 2 + arcSin1 - dp2px(getContext(), 8));
        path2.lineTo(width / 2 - arcCos1 - dp2px(getContext(), 2), height / 2 + arcSin1 - dp2px(getContext(), 7));
        path2.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path2, arcAnglePaint);
    }

    /**
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth(); //获取这个View的宽高
        int height = getHeight();
        Log.d(TAG, "onDraw: width:" + width + "  height:" + height);
        String tipText = getSyncTypeText(type);
        if (percent < 1) {
            //画白色底部
            canvas.drawArc(progressRectF, -90, 360f, true, backgroundPaint);
            drawTwoArc(canvas, width, height);
            //中间提示字 -正在同步
            tipTextPaint.getTextBounds(tipText, 0, tipText.length(), tipTextBoundsRect);
            float textY = height / 2f + tipTextBoundsRect.height() / 2f;
            canvas.drawText(tipText, (width - tipTextBoundsRect.width()) / 2f, textY, tipTextPaint);
        } else {
            //画白色底部
            canvas.drawArc(progressRectF, -90, 360f, true, backgroundPaint);

            // 底部 灰色线条圆圈
            canvas.drawCircle(width / 2, height / 2, getRadius() - circle_stroke_width / 2, circleBgPaint);

            canvas.save();
            Shader mShader = new SweepGradient(width / 2, height / 2, colors, null);
            Matrix matrix = new Matrix();
            matrix.setRotate(270, height / 2, height / 2);
            mShader.setLocalMatrix(matrix);
            circleProgressPaint.setShader(mShader);
            // 线条进度
            canvas.drawArc(colorProgressRectF, -90, percent * 360 / 100, false, circleProgressPaint);
            canvas.restore();
            //中间提示字 -正在同步
            tipTextPaint.getTextBounds(tipText, 0, tipText.length(), tipTextBoundsRect);
            float textY = height / 2f + tipTextBoundsRect.height() / 2f; //提示字的下面
            Log.d(TAG, "onDraw: 中间提示字下面" + textY);
            canvas.drawText(tipText, (width - tipTextBoundsRect.width()) / 2f, textY, tipTextPaint);

            // 中间百分比文本
            percentTextPaint.getTextBounds(percentText, 0, percentText.length(), percentTextBoundsRect);
//        float percentY = (float) (textY + percentTextBoundsRect.height() * 1.8);
            float percentY = textY + tipTextBoundsRect.height() + text2percent; //
            if (percent == 100) {
//                Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_contact_sync_finish);
//                canvas.drawBitmap(b, width / 2 - 48, textY + percentTextBoundsRect.height() / 2, null);
                float unitHeight = dp2px(getContext(), 14); //勾距离上部字
                float unitWidth = dp2px(getContext(), 9); //勾距离中间12dp
                float center1_x = width / 2 - unitWidth; //
                float center1_y = textY + unitHeight + circle_stroke_width; //要注意笔的宽度

                //画第一根线
                canvas.drawLine(center1_x, center1_y, width / 2 - dp2px(getContext(), 2), center1_y + unitWidth, gouPaint);
                //画第二根线
                canvas.drawLine(width / 2 - dp2px(getContext(), 2), center1_y + unitWidth, width / 2 + dp2px(getContext(), 13), textY + unitHeight / 2 + circle_stroke_width, gouPaint);

            } else {
                canvas.drawText(percentText, (width - percentTextBoundsRect.width()) / 2f, percentY, percentTextPaint);
            }
        }
    }

    private int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    SyncType type = SyncType.SYNC;

    /**
     * 设置进度
     *
     * @param percent [0-100]
     */
    public void setPercent(int percent) {
        if (percent < 0) {
            percent = 0;
            type = SyncType.SYNC;
        } else if (percent >= 100) {
            percent = 100;
            type = SyncType.SYNCED;
        } else {
            type = SyncType.SYNCING;
        }
        this.percent = percent;
        this.percentText = percent + "%";
        invalidate();
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public float getArcPadding() {
        return arcPadding;
    }

    public void setArcPadding(float arcPadding) {
        this.arcPadding = arcPadding + circle_stroke_width / 2;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getCircle_color() {
        return circle_color;
    }

    public void setCircle_color(int circle_color) {
        this.circle_color = circle_color;
    }

    public int getCircle_progress_color() {
        return circle_progress_color;
    }

    public void setCircle_progress_color(int circle_progress_color) {
        this.circle_progress_color = circle_progress_color;
    }

    public int getCircle_stroke_width() {
        return circle_stroke_width;
    }

    public void setCircle_stroke_width(int circle_stroke_width) {
        this.circle_stroke_width = circle_stroke_width;
    }
}
