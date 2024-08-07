package com.openxu.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import androidx.annotation.FloatRange


/**
 * 仪表盘
 */
class DashBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var length: Float = 0f //仪表盘半径
    private var r: Float = 0f
    private var backGroundColor: Int = Color.WHITE //背景色
    private var per: Float = 0f //指数百分比
    private var perOld: Float = 0f //变化前指针百分比
    private var perPoint: Float = 0f //缓存(变化中)指针百分比
    private var pointLength: Float = 0f //指针长度

    private val paint: Paint = Paint()
    private var strokePain: Paint = Paint()
    private var tmpPaint: Paint = Paint()
    private var rect: RectF

    private var scaleStartAngle = 105f//指针开始角度
    private var sweepAngle = 210f//仪表弧形角度
    private var scaleTextSize = 12f//刻度文字大小

    init {
        rect = RectF()
        strokePain.color = 0x3f979797
        strokePain.strokeWidth = 10f
        strokePain.shader = null
        strokePain.style = Paint.Style.STROKE
        //刻度画笔对象
        tmpPaint.strokeWidth = 1f
        tmpPaint.textSize = 35f
        tmpPaint.textAlign = Paint.Align.CENTER
        tmpPaint.textSize = scaleTextSize
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = width / 2 / 4 * 5
        initIndex(width / 2)
        setMeasuredDimension(width, height)
    }

    private fun initIndex(specSize: Int) {
        r = specSize.toFloat()
        length = r / 4 * 3
        pointLength = -(r * 0.4).toFloat()
        per = 0f
        perOld = 0f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        drawRing(canvas)
        drawScale(canvas)
        drawPointer(canvas)
        drawText(canvas)
    }

    /**
     * 画最外侧圆环
     */
    private fun drawRing(canvas: Canvas) {
        paint.isAntiAlias = true
        paint.strokeWidth = 2f
        canvas.save()
        //canvas中心移动到中间
        canvas.translate((canvas.width / 2).toFloat(), r)

        //渐变圆环
        paint.style = Paint.Style.FILL
        //设置渐变的颜色范围
        val colors = intArrayOf(Color.parseColor("#7BD776"), Color.parseColor("#FBCF41"), Color.parseColor("#FB7A3C"))
        //设置的渐变起止位置
        val positions = floatArrayOf(150f / 360f, 0.7f, 1.0f)
        //设置渐变的蒙版
        val sweepGradient = SweepGradient(0f, 0f, colors, positions)
        paint.shader = sweepGradient
        rect = RectF(-length, -length, length, length)
        canvas.rotate(15f, 0f, 0f)
        //绘制圆环
        canvas.drawArc(rect, 150f, sweepAngle, true, paint)

        //绘制描边效果
//        canvas.drawArc(rect, 170f, 200f, true, strokePain)
//        canvas.restore()
//        canvas.save()
//        canvas.translate((canvas.width / 2).toFloat(), r)

        //内部背景色填充
        paint.color = backGroundColor
        paint.shader = null
        rect = RectF(-(length - length / 3f - 2), -(length / 3f * 2f - 2), length - length / 3f - 2, length / 3f * 2f - 2)
        canvas.drawArc(rect, 0f, 360f, true, paint)
    }

    /**
     * 画刻度
     */
    private fun drawScale(canvas: Canvas) {
        canvas.restore()
        canvas.save()
        canvas.translate((canvas.width / 2).toFloat(), r)
        paint.color = Color.WHITE

        canvas.rotate(-scaleStartAngle, 0f, 0f)
        var y = length
        y = -y
        val count = 10 //总刻度数
        paint.color = backGroundColor
        val tempRou = sweepAngle / 10f
        //每次旋转的角度
        paint.color = Color.WHITE
        paint.strokeWidth = 1f

        //绘制刻度和百分比
        tmpPaint.color = Color.parseColor("#80000000")
        for (i in 0..count) {
            canvas.drawText((i * 10).toString(), 0f, y + length / 3f + scaleTextSize + 2f, tmpPaint)
            canvas.drawLine(0f, y, 0f, y + length, paint)
            canvas.rotate(tempRou, 0f, 0f)
        }
    }


    /**
     * 画指针
     */
    private fun drawPointer(canvas: Canvas) {
        paint.color = Color.BLACK
        val colors = intArrayOf(Color.parseColor("#FF7575"), Color.parseColor("#FFD9D9"))
        val linearGradient = LinearGradient(0f, 0f, pointLength, 0f, colors, null, Shader.TileMode.CLAMP)
        paint.shader = linearGradient
        canvas.restore()
        canvas.save()
        canvas.translate((canvas.width / 2).toFloat(), r)
        val change: Float = if (perPoint < 1) {
            perPoint * sweepAngle
        } else {
            sweepAngle
        }

        //根据参数得到旋转角度
        canvas.rotate(-scaleStartAngle + change, 0f, 0f)

        //绘制三角形形成指针
        val path = Path()
        path.moveTo(0f, pointLength)
        path.lineTo(-10f, 0f)
        path.lineTo(10f, 0f)
        path.lineTo(0f, pointLength)
        path.close()
        canvas.drawPath(path, paint)
    }


    /**
     * 内部圆环
     */
    private fun drawText(canvas: Canvas) {
        //抗锯齿
        canvas.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.restore()
        canvas.save()
        canvas.translate((canvas.width / 2).toFloat(), r)
        val rIndex = length / 8f
        val rIndex2 = length / 11.0f
        //中间外圆环
        paint.color = Color.parseColor("#FF3838")
        paint.shader = null
        paint.setShadowLayer(5f, 0f, 0f, 0x54000000)
        rect = RectF(-rIndex, -rIndex, rIndex, rIndex)
        canvas.drawArc(rect, 0f, 360f, true, paint)
        paint.clearShadowLayer()
        //中间内圆环
        paint.color = Color.parseColor("#FF6060")
        paint.shader = null
        rect = RectF(-rIndex2, -rIndex2, rIndex2, rIndex2)
        canvas.drawArc(rect, 0f, 360f, true, paint)
        canvas.restore()
        canvas.save()
    }

    /**
     * 修改进度
     */
    private fun changePerWithAnim(@FloatRange(from = 0.0, to = 1.0) per: Float) {
        this.perOld = per
        this.per = per
        val va = ValueAnimator.ofFloat(perOld, per)
        va.duration = 1000
        va.addUpdateListener { animation ->
            perPoint = animation.animatedValue as Float
            invalidate()
        }
        va.start()
    }

    /**
     * 修改刻度
     * @param anim 是否动画
     */
    fun changePer(
        @FloatRange(from = 0.0, to = 1.0) per: Float,
        anim: Boolean = false
    ) {
        if (anim) {
            changePerWithAnim(per)
        } else {
            this.perOld = per
            this.per = per
            perPoint = per
            invalidate()
        }
    }


    fun setBackGroundColor(color: Int) {
        backGroundColor = color
    }

    /**
     * 设置指针长度
     */
    fun setPointLength(pointLength: Float) {
        this.pointLength = -length * pointLength
    }

}