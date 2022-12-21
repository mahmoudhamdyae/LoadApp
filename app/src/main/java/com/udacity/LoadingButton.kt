package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthSize = 0
    private var heightSize = 0

    private var valueAnimator = ValueAnimator()
    private var buttonProgress: Float = 0f
    private var backgroundButtonColor = 0
    private var circleProgressColor = 0

    private var oval = RectF(80f, 30f, 200f, 150f)

    var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Loading -> {
                valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                    addUpdateListener {
                        buttonProgress = animatedValue as Float
                        invalidate()
                    }
                    repeatCount = ValueAnimator.INFINITE
                    repeatMode = ValueAnimator.RESTART
                    duration = 5000
                    start()
                }
                // Disable Button
                isEnabled = false
            }

            ButtonState.Completed -> {
                // Cancel Animator
                valueAnimator.cancel()
                // Update view
                invalidate()
                // Enable Button
                isEnabled = true
            }
        }
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            backgroundButtonColor =
                getColor(R.styleable.LoadingButton_backgroundButtonColor, ContextCompat.getColor(context, R.color.colorPrimary))
            circleProgressColor =
                getColor(R.styleable.LoadingButton_circleProgressColor, ContextCompat.getColor(context, R.color.colorAccent))
        }
        buttonState = ButtonState.Completed
    }

    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = backgroundButtonColor
    }

    private val paintText = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        typeface = Typeface.create("", Typeface.BOLD)
        textSize = 20.0f * resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = circleProgressColor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBackground(canvas)
        when (buttonState) {
            ButtonState.Loading -> {
                drawBackground(canvas, buttonProgress)
                canvas.drawArc(
                    oval,
                    -180f,
                    buttonProgress * 360,
                    true,
                    paintProgress
                )
                drawText(canvas, resources.getString(R.string.button_loading))
            }

            ButtonState.Completed -> {
                drawText(canvas, resources.getString(R.string.button_text))
            }
        }
        invalidate()
    }

    private fun drawBackground(canvas: Canvas, progress: Float? = null) {
        if (progress != null) {
            paintBackground.alpha = 220
        }
        canvas.drawRect(0f, 0f, width.toFloat() * (progress ?: 1f), height.toFloat(), paintBackground)
    }

    private fun drawText(canvas: Canvas, text: String) {
        val posX = (width / 2).toFloat()
        val posY = ((height - (paintText.descent() + paintText.ascent())) / 2)
        canvas.drawText(text, posX, posY, paintText)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minWidth, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }
}