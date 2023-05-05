package com.lazyiones.focussunview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class FocusSunView : View {
    private var paintColor = Color.WHITE
    private val sunPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var borderWidth = 3f
    private var progress = 0.5f
    private var realProcess = 0.5f
    private var angle = 360f
    private var circleY: Float = -1f
    private var lastCircleY: Float = 0f
    private var posY: Float = 0f
    private var curPosY: Float = 0f
    private val porterDuffDstOut = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    private var dp10 = 0f
    private var dp8 = 0f
    private var dp6 = 0f
    private var dp5 = 0f
    private var dp3 = 0f
    private var dp2 = 0f
    private var centerOfCircle = 0f
    private var circleRadius = 0f
    private var frameRectF = RectF(0f, 0f, 0f, 0f)
    private var frameRadius = 0f
    private var _14 = 0f
    private var countdown: CountDownTimer? = null
    private var showLine = false
    private var upperExposureLimit = 2f
    private var lowerExposureLimit = -2f
    private var onExposureChangeListener: OnExposureChangeListener? = null
    private var oldExposure = 0f
    private var focusAnimator: ValueAnimator? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?,
        defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        sunPaint.strokeCap = Paint.Cap.ROUND
        sunPaint.style = Paint.Style.STROKE
        moonPaint.style = Paint.Style.FILL
        moonPaint.strokeCap = Paint.Cap.ROUND
        framePaint.strokeCap = Paint.Cap.ROUND
        framePaint.style = Paint.Style.STROKE
        framePaint.strokeJoin = Paint.Join.ROUND
        dp10 = dp2px(context, 10f)
        dp8 = dp2px(context, 8f)
        dp6 = dp2px(context, 6f)
        dp5 = dp2px(context, 5f)
        dp3 = dp2px(context, 3f)
        dp2 = dp2px(context, 2f)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        centerOfCircle = (width / 10f) * 9f
        circleRadius = width / 30f
        frameRadius = width / 5f
        frameRectF.left = (width / 2f) - frameRadius
        frameRectF.right = (width / 2f) + frameRadius
        frameRectF.top = (height / 2f) - frameRadius
        frameRectF.bottom = (height / 2f) + frameRadius
        _14 = frameRectF.height() / 4f
    }

    /**
     * 设置曝光上限和下限
     */
    fun setExposureLimit(upperExposureLimit: Float?, lowerExposureLimit: Float?) {
        if (upperExposureLimit != null && lowerExposureLimit != null) {
            this.upperExposureLimit = upperExposureLimit
            this.lowerExposureLimit = lowerExposureLimit
        }
    }

    /**
     * 点击动画/重置
     */
    fun startCountdown(reset: Boolean = true) {
        paintColor = Color.WHITE
        if (reset) {
            progress = 0.5f
            realProcess = 0.5f
            circleY = height * progress
            lastCircleY = circleY
        }
        postInvalidate()
        if (countdown != null) {
            countdown?.cancel()
            countdown = null
        }
        if (focusAnimator != null) {
            focusAnimator?.cancel()
            focusAnimator = null
        }
        if (countdown == null) {
            if (!reset) {
                countdown = object : CountDownTimer(5000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        if (millisUntilFinished in 1000..2500) {
                            paintColor = Color.parseColor("#FFAAAAAA")
                            postInvalidate()
                        }
                    }

                    override fun onFinish() {
                        countdown = null
                        visibility = GONE
                    }
                }.start()
            } else {
                focusAnimator = ValueAnimator.ofFloat(0f, 1.3f, 1f).setDuration(500)
                if (focusAnimator != null) {
                    focusAnimator?.addUpdateListener { animation ->
                        val value = animation.animatedValue as Float
                        val left = (width / 2f) - frameRadius
                        val right = (width / 2f) + frameRadius
                        val top = (height / 2f) - frameRadius
                        val bottom = (height / 2f) + frameRadius
                        frameRectF.left = left - (((right - left) / 5f) - (((right - left) / 5f) * value))
                        frameRectF.right = right + ((right - left) / 5f - (((right - left) / 5f) * value))
                        frameRectF.top = top - ((bottom - top) / 5f - (((bottom - top) / 5f) * value))
                        frameRectF.bottom = bottom + ((bottom - top) / 5f - (((bottom - top) / 5f) * value))
                        postInvalidate()
                    }
                    focusAnimator?.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            focusAnimator = null
                            countdown = object : CountDownTimer(5000, 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    if (millisUntilFinished in 1000..2500) {
                                        paintColor = Color.parseColor("#FFAAAAAA")
                                        postInvalidate()
                                    }
                                }

                                override fun onFinish() {
                                    countdown = null
                                    visibility = GONE
                                }
                            }.start()
                        }
                    })
                    focusAnimator?.start()
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        focusAnimator?.cancel()
        focusAnimator = null
        countdown?.cancel()
        countdown = null
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            framePaint.color = paintColor
            // 画对焦框 start
            borderWidth = 4f
            framePaint.strokeWidth = borderWidth
            val points = floatArrayOf(
                frameRectF.left, frameRectF.top, frameRectF.left, frameRectF.top + _14,
                frameRectF.left, frameRectF.top, frameRectF.left + _14, frameRectF.top,
                frameRectF.left, frameRectF.bottom, frameRectF.left, frameRectF.bottom - _14,
                frameRectF.left, frameRectF.bottom, frameRectF.left + _14, frameRectF.bottom,
                frameRectF.right, frameRectF.top, frameRectF.right, frameRectF.top + _14,
                frameRectF.right, frameRectF.top, frameRectF.right - _14, frameRectF.top,
                frameRectF.right, frameRectF.bottom, frameRectF.right, frameRectF.bottom - _14,
                frameRectF.right, frameRectF.bottom, frameRectF.right - _14, frameRectF.bottom)
            drawLines(points, framePaint)
            // 画对焦框 end
            // 画小太阳SeekBar start
            borderWidth = 4f
            sunPaint.color = paintColor
            moonPaint.color = paintColor
            sunPaint.strokeWidth = borderWidth
            // 画直线
            if (showLine) {
                if (circleY != circleRadius + dp8) {
                    drawLine(centerOfCircle, 0f, centerOfCircle, (height * progress) - (circleRadius) - dp10, sunPaint)
                }
                if (circleY != height - (circleRadius) - dp8) {
                    drawLine(centerOfCircle, (height * progress) + (circleRadius) + dp10, centerOfCircle, height * 1f, sunPaint)
                }
            }
            borderWidth = 3f
            sunPaint.strokeWidth = borderWidth
            // 画圆,空心圆
            drawCircle(centerOfCircle, height * progress, circleRadius, sunPaint)
            // 画线条
            for (i in 0 until 8) {
                val startPointF = calculationPoint(angle - (i * 45f), circleRadius + dp3)
                val endPointF = calculationPoint(angle - (i * 45f), circleRadius + dp5)
                borderWidth = 5f
                sunPaint.strokeWidth = borderWidth
                canvas.drawLine(startPointF.x, startPointF.y, endPointF.x, endPointF.y, sunPaint)
                borderWidth = 3f
            }
            // 画中间月亮效果
            if (realProcess < .5f) {
                val left = centerOfCircle - (((circleRadius - dp2) * 2f) * abs(realProcess - 0.5f))
                val top = (height * progress) - (circleRadius - dp2)
                val right = centerOfCircle + (((circleRadius - dp2) * 2f) * abs(realProcess - 0.5f))
                val bottom = (height * progress) + (circleRadius - dp2)
                drawOval(left, top, right, bottom, moonPaint)
                drawArc(centerOfCircle - (circleRadius - dp2), (height * progress) - (circleRadius - dp2),
                    centerOfCircle + (circleRadius - dp2), (height * progress) + (circleRadius - dp2),
                    90f, 180f, false, moonPaint)
            } else if (realProcess == .5f) {
                drawArc(centerOfCircle - (circleRadius - dp2), (height * progress) - (circleRadius - dp2),
                    centerOfCircle + (circleRadius - dp2), (height * progress) + (circleRadius - dp2),
                    90f, 180f, false, moonPaint)
            } else {
                val save = saveLayer(null, null)
                val left = centerOfCircle - (((circleRadius - dp2) * 2f) * abs(realProcess - 0.5f))
                val top = (height * progress) - (circleRadius - dp2)
                val right = centerOfCircle + (((circleRadius - dp2) * 2f) * abs(realProcess - 0.5f))
                val bottom = (height * progress) + (circleRadius - dp2)
                drawArc(centerOfCircle - (circleRadius - dp2 - 1), (height * progress) - (circleRadius - dp2 - 1),
                    centerOfCircle + (circleRadius - dp2 - 1), (height * progress) + (circleRadius - dp2 - 1),
                    90f, 180f, false, moonPaint)
                moonPaint.xfermode = porterDuffDstOut
                drawOval(left, top, right, bottom, moonPaint)
                moonPaint.xfermode = null
                restoreToCount(save)
            }
            // 画小太阳SeekBar end
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (circleY < 0f) {
                        circleY = height * progress
                        lastCircleY = circleY
                    }
                    posY = event.y
                    paintColor = Color.WHITE
                }
                MotionEvent.ACTION_MOVE -> {
                    curPosY = event.y
                    paintColor = Color.WHITE
                    if ((curPosY - posY > 0) || (curPosY - posY < 0)) {
                        showLine = true
                        circleY = (curPosY - posY) + lastCircleY
                        if (circleY >= height - circleRadius - dp8) {
                            circleY = height - circleRadius - dp8
                        }
                        if (circleY < circleRadius + dp8) {
                            circleY = circleRadius + dp8
                        }
                        realProcess = (((circleY - (circleRadius + dp8)) / ((height - circleRadius - dp8) - (circleRadius + dp8))) * 100f).roundToInt() / 100.0f
                        progress = circleY / height
                        angle = 360f * realProcess
                        val absolutelyProcess = (((((height - circleRadius - dp8) - (circleRadius + dp8)) - (circleY - (circleRadius + dp8))) / ((height - circleRadius - dp8) - (circleRadius + dp8))) * 100f).roundToInt() / 100.0f
                        val step = upperExposureLimit - lowerExposureLimit
                        val exposure = (((step * absolutelyProcess) + lowerExposureLimit) * 100f).roundToInt() / 100.0f
                        if (onExposureChangeListener != null && oldExposure != exposure) {
                            oldExposure = exposure
                            onExposureChangeListener?.onExposureChangeListener(exposure)
                        }
                        if (countdown != null) {
                            countdown?.cancel()
                            countdown = null
                        }
                        invalidate()
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    lastCircleY = circleY
                    showLine = false
                    invalidate()
                    startCountdown(false)
                }
                else -> {

                }
            }
        }
        return true
    }

    /**
     * 计算圆上任意点的坐标
     * @param angle 角度
     * @param radius 半径
     * @return 点坐标
     */
    private fun calculationPoint(angle: Float, radius: Float): PointF {
        val x = (centerOfCircle) + (radius) * cos(angle * Math.PI / 180f).toFloat()
        val y = (height * progress) + (radius) * sin(angle * Math.PI / 180f).toFloat()
        return PointF(x, y)
    }

    /**
     * 设置曝光回调监听
     */
    fun setOnExposureChangeListener(onExposureChangeListener: OnExposureChangeListener) {
        this.onExposureChangeListener = onExposureChangeListener
    }

    /**
     * 曝光回调
     */
    interface OnExposureChangeListener {
        /**
         * 曝光回调
         *
         * @param exposure 曝光
         */
        fun onExposureChangeListener(exposure: Float)
    }

    /**
     * dp 转 px
     */
    private fun dp2px(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density + .5f
}