package com.example.util.simpletimetracker.feature_dialogs.duration.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.toDuration
import com.example.util.simpletimetracker.feature_dialogs.R
import java.lang.Float.min

class DurationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(
    context,
    attrs,
    defStyleAttr,
) {

    // Attrs
    private var textColor: Int = 0
    private var legendTextColor: Int = 0
    private var legendTextSize: Float = 0f
    private var legendPadding: Float = 0f
    // End of attrs

    private var data: ViewData = ViewData.Empty
    private val textPaint: Paint = Paint()
    private val legendTextPaint: Paint = Paint()
    private var textStartHorizontal: Float = 0f
    private var textStartVertical: Float = 0f
    private val bounds: Rect = Rect()

    private val hourString: String by lazy { context.getString(R.string.time_hour) }
    private val minuteString: String by lazy { context.getString(R.string.time_minute) }
    private val secondString: String by lazy { context.getString(R.string.time_second) }

    init {
        initArgs(context, attrs, defStyleAttr)
        initPaint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = resolveSize(0, widthMeasureSpec)
        val h = resolveSize(w, heightMeasureSpec)

        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        calculateDimensions(w, h)
        drawText(canvas)
    }

    fun setData(data: ViewData) {
        this.data = data
        invalidate()
    }

    private fun initArgs(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) {
        context
            .obtainStyledAttributes(
                attrs,
                R.styleable.DurationView, defStyleAttr, 0,
            )
            .run {
                textColor = getColor(
                    R.styleable.DurationView_durationTextColor, Color.BLACK,
                )
                legendTextColor = getColor(
                    R.styleable.DurationView_durationLegendTextColor, Color.BLACK,
                )
                legendTextSize = getDimensionPixelSize(
                    R.styleable.DurationView_durationLegendTextSize, 14,
                ).toFloat()
                legendPadding = getDimensionPixelSize(
                    R.styleable.DurationView_durationLegendPadding, 0,
                ).toFloat()

                recycle()
            }
    }

    private fun initPaint() {
        textPaint.apply {
            isAntiAlias = true
            color = textColor
        }
        legendTextPaint.apply {
            isAntiAlias = true
            color = legendTextColor
            textSize = legendTextSize
        }
    }

    private fun calculateDimensions(w: Float, h: Float) {
        val legendsTextWidth = listOfNotNull(
            hourString,
            minuteString,
            secondString.takeIf { data.showSeconds },
        ).map(legendTextPaint::measureText).sum()
        val paddingsCount = if (data.showSeconds) 2 else 1
        val desiredWidth = w - legendsTextWidth - paddingsCount * legendPadding
        val text = data.hours.format() +
            data.minutes.format() +
            data.seconds.takeIf { data.showSeconds }?.format().orEmpty()
        val textLength = text.length
        val hoursWidth = min(data.hours.format().length.toFloat() / textLength * desiredWidth, h)
        setTextSizeForWidth(textPaint, data.hours.format(), hoursWidth)

        val fullTextWidth = textPaint.measureText(data.hours.format()) +
            textPaint.measureText(data.minutes.format()) +
            data.seconds.takeIf { data.showSeconds }
                ?.let { textPaint.measureText(it.format()) }.orZero() +
            legendsTextWidth
        textStartHorizontal = (w - fullTextWidth - 2 * legendPadding) / 2

        textPaint.getTextBounds("0", 0, 1, bounds)
        val textHeight = bounds.height()
        textStartVertical = textHeight + (h - textHeight) / 2
    }

    private fun drawText(canvas: Canvas) {
        // Center text
        canvas.translate(textStartHorizontal, textStartVertical)

        val hoursText = data.hours.format()
        val minutesText = data.minutes.format()
        val secondsText = data.seconds.format()
        val hoursNotEmpty = textHasValues(hoursText)
        val minutesNotEmpty = hoursNotEmpty || textHasValues(minutesText)
        val secondsNotEmpty = hoursNotEmpty || minutesNotEmpty || textHasValues(secondsText)

        var color = if (hoursNotEmpty) textColor else legendTextColor
        textPaint.color = color
        legendTextPaint.color = color
        canvas.drawText(hoursText, 0f, 0f, textPaint)
        canvas.translate(textPaint.measureText(hoursText), 0f)
        canvas.drawText(hourString, 0f, 0f, legendTextPaint)
        canvas.translate(legendTextPaint.measureText(hourString) + legendPadding, 0f)

        color = if (minutesNotEmpty) textColor else legendTextColor
        textPaint.color = color
        legendTextPaint.color = color
        canvas.drawText(minutesText, 0f, 0f, textPaint)
        canvas.translate(textPaint.measureText(minutesText), 0f)
        canvas.drawText(minuteString, 0f, 0f, legendTextPaint)
        canvas.translate(legendTextPaint.measureText(minuteString) + legendPadding, 0f)

        if (data.showSeconds) {
            color = if (secondsNotEmpty) textColor else legendTextColor
            textPaint.color = color
            legendTextPaint.color = color
            canvas.drawText(secondsText, 0f, 0f, textPaint)
            canvas.translate(textPaint.measureText(secondsText), 0f)
            canvas.drawText(secondString, 0f, 0f, legendTextPaint)
            canvas.translate(legendTextPaint.measureText(secondString), 0f)
        }
    }

    private fun setTextSizeForWidth(paint: Paint, text: String, desiredWidth: Float) {
        val testTextSize = 48f
        paint.textSize = testTextSize
        val width = paint.measureText(text)

        val desiredTextSize = testTextSize * desiredWidth / width
        paint.textSize = desiredTextSize
    }

    private fun textHasValues(text: String): Boolean {
        return text != "00"
    }

    private fun Long.format(): String {
        return this.toDuration()
    }

    data class ViewData(
        val hours: Long,
        val minutes: Long,
        val seconds: Long,
        val showSeconds: Boolean,
    ) {

        companion object {
            val Empty = ViewData(
                hours = 0,
                minutes = 0,
                seconds = 0,
                showSeconds = true,
            )
        }
    }
}