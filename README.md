项目中刚好需要用到这个同步进度圈，这里做一个总结：
开始同步：背景灰色的圈，两条弧，弧头有个类似的箭头，中间是提示字
正在同步：在开始同步基础上，有一个底色的外圈，在其上面有个根据进度条显示的进度圈，颜色随着进度而变化，提示字下面有个百分比进度显示；
同步完成：在同步基础上，出现一个对钩

View的基础信息，这里就不写了，可以看我上一篇文章：
http://blog.csdn.net/azhansy/article/details/52026877

这里总结一些我的想法：

 1. 一个View的创建在构造函数时，是还没创建完成，是没有宽高的，所以有时候我们直接获取宽高时是为0的。
 2. 修改view的大小可以根据onMeasure方法，我们可以根据产品的要求设置。
 3. View中的每个小view都需要一个矩形Rect或者Rectf来确定你所画的大小，Paint来确定你画的每个小view的颜色、是否填充、画笔粗浅、是否抗齿轮
 4. 画的形状根据是画扇形、圆形、矩形、字、bitmap、线条line等决定

![这里写图片描述](http://img.blog.csdn.net/20161103150221207)

 - 首先是整个自定义View圈的大小，view视图的大小根据onMeasure返回的，我们也可以通过setMeasureDimension重新设置一下view的大小，这里是获取宽高最小值来设置一个矩形：

```
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
```

 - 通过onMeasure()方法计算，获取宽高最小的做矩形，重新定义View的宽高，布局文件中如果是wrap_contact就使用默认的半径72dp，如果是match_parent和写多少dp，就根据用户的来设定。

```
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
progressRectF.set(0, 0, min, min);
```

 - 定好宽高后，画底部的白色背景圈，圈内是填充的fill，背景色为白色：

backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
backgroundPaint.setStyle(Paint.Style.FILL);
backgroundPaint.setColor(Color.parseColor("#ffffff"));
canvas.drawArc(progressRectF, -90, 360f, true, backgroundPaint);

 - 接着画双弧，和两个小三角形，因为角度为45度，可以用三角函数算出，对应的宽度：
   因为我们知道半径，根据三角函数，cos值为半径多出的距离，sin值为Y轴方向的偏移arc1Angle * Math.PI /
   180求得角度

```
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
```

```
//中间提示字 -正在同步
tipTextPaint.getTextBounds(tipText, 0, tipText.length(), tipTextBoundsRect);
float textY = height / 2f + tipTextBoundsRect.height() / 2f;
canvas.drawText(tipText, (width - tipTextBoundsRect.width()) / 2f, textY, tipTextPaint);

canvas.save();
Shader mShader = new SweepGradient(width / 2, height / 2, colors, null);
Matrix matrix = new Matrix();
matrix.setRotate(270, height / 2, height / 2); //这里旋转后，需要保存之前的状态
mShader.setLocalMatrix(matrix);
circleProgressPaint.setShader(mShader);
// 线条进度
canvas.drawArc(colorProgressRectF, -90, percent * 360 / 100, false, circleProgressPaint);
canvas.restore();

```

 - 百分比为100时，显示对钩：

```
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

```

 - View 的关键生命周期为 [改变可见性]  --> 构造View[没有宽高]   -->      onFinishInflate
   -->   onAttachedToWindow  -
   ->  onMeasure  -->  onSizeChanged  -->  onLayout  -->   onDraw  -->  onDetackedFromWindow

待解决的问题：
1、颜色值的position，不会控制显示的颜色。
2、对钩画得跟设计图不太一样

博客地址：
