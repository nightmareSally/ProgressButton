package com.sally.progressbutton

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.ColorUtils
import com.sally.progressbutton.Extensions.dpToPx

class ProgressButton : AppCompatTextView {

    companion object {
        private const val START = 0
        private const val END = 1
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttrs(context, attrs)
    }

    private var mBgColor: Int = Color.GRAY
    private var mBgDrawable: Drawable? = null
    private var mProgressColor: Int = Color.YELLOW
    private var mProgressDrawable: Drawable? = null
    private var mTextColor: Int = Color.BLACK
    private var mTextCoverColor: Int = Color.WHITE
    private var mFinishedText: String? = null
    private var mIcon: Drawable? = null
    private var mIconGravity: Int = START
    private var mIconPadding: Float = 10f.dpToPx()
    private var mIconMaskColor: Int = Color.GRAY
    private var mProgress = 0
    private var mMaxProgress = 1000
    private var mMinProgress = 0
    private var mBackgroundBounds: RectF? = null
    private var mCurrentText: String = ""

    private lateinit var mBackgroundPaint: Paint
    private lateinit var mTextPaint: Paint

    init {
        mBackgroundPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        mTextPaint = Paint().apply {
            isAntiAlias = true
        }

    }

    private fun initAttrs(context: Context, attrs: AttributeSet) {
        val attributeSet = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton)

        // progress bg
        when (attributeSet.getType(R.styleable.ProgressButton_progress_background)) {
            TypedValue.TYPE_STRING -> {
                mBgDrawable =
                    attributeSet.getDrawable(R.styleable.ProgressButton_progress_background)
            }
            else -> {
                mBgColor = attributeSet.getColor(
                    R.styleable.ProgressButton_progress_background,
                    Color.GRAY
                )
            }
        }

        // progress cover bg
        when (attributeSet.getType(R.styleable.ProgressButton_progress_cover_background)) {
            TypedValue.TYPE_STRING -> {
                mProgressDrawable =
                    attributeSet.getDrawable(R.styleable.ProgressButton_progress_cover_background)
            }
            else -> {
                mProgressColor = attributeSet.getColor(
                    R.styleable.ProgressButton_progress_cover_background,
                    Color.YELLOW
                )
            }
        }

        mTextColor = attributeSet.getColor(
            R.styleable.ProgressButton_progress_text_color,
            Color.BLACK
        )

        mTextCoverColor = attributeSet.getColor(
            R.styleable.ProgressButton_progress_text_cover_color,
            Color.WHITE
        )

        mIcon =
            attributeSet.getDrawable(R.styleable.ProgressButton_progress_icon)

        mIconGravity =
            attributeSet.getInt(R.styleable.ProgressButton_progress_icon_gravity, START)
        mIconMaskColor = attributeSet.getColor(
            R.styleable.ProgressButton_progress_icon_mask_color,
            Color.GRAY
        )
        mIconPadding = attributeSet.getDimension(
            R.styleable.ProgressButton_progress_icon_padding,
            10f.dpToPx()
        )
        mFinishedText = attributeSet.getString(R.styleable.ProgressButton_progress_text_finish)
        mProgress = attributeSet.getInt(R.styleable.ProgressButton_progress, 0)
        mMinProgress = attributeSet.getInt(R.styleable.ProgressButton_progress_min, 0)
        mMaxProgress = attributeSet.getInt(R.styleable.ProgressButton_progress_max, 100)

        attributeSet.recycle()
    }

    private fun drawBackground(canvas: Canvas, progressPercent: Float) {
        mBackgroundBounds = RectF().apply {
            left = 0f
            top = 0f
            right = measuredWidth.toFloat()
            bottom = measuredHeight.toFloat()
        }

        mBgDrawable?.let { drawable ->
            drawable.setBounds(0, 0, measuredWidth, measuredHeight)
            drawable.draw(canvas)
        } ?: run {
            mBackgroundPaint.color = mBgColor
            canvas.drawRect(mBackgroundBounds!!, mBackgroundPaint)
        }

        mProgressDrawable?.let { drawable ->
            drawable.setBounds(0, 0, measuredWidth, measuredHeight)
            canvas.save()
            canvas.clipRect(0, 0, (measuredWidth * progressPercent).toInt(), measuredHeight)
            drawable.draw(canvas)
            canvas.restore()
        } ?: run {
            mBackgroundPaint.color = mProgressColor
            mBackgroundBounds?.right = measuredWidth * progressPercent
            canvas.drawRect(mBackgroundBounds!!, mBackgroundPaint)
        }
    }

    private fun drawContent(canvas: Canvas, progressPercent: Float) {
        mTextPaint.textSize = textSize
        mTextPaint.typeface = typeface
        val textWidth = mTextPaint.measureText(mCurrentText)
        val textHeight = mTextPaint.fontMetrics.descent - mTextPaint.fontMetrics.ascent
        val textY = (canvas.height - mTextPaint.fontMetrics.bottom - mTextPaint.fontMetrics.top) / 2
        mIcon?.let { icon ->
            val scaleRadio = textHeight / icon.intrinsicHeight
            val imageWidth = icon.intrinsicWidth * scaleRadio
            val imageHeight = icon.intrinsicHeight * scaleRadio
            val contentWidth = textWidth + mIconPadding + imageWidth
            drawText(canvas, progressPercent, textWidth, textY, imageWidth)
            drawImage(canvas, icon, progressPercent, imageWidth, imageHeight, contentWidth)
        } ?: run {
            drawText(canvas, progressPercent, textWidth, textY)
        }
    }

    private fun drawText(
        canvas: Canvas,
        progressPercent: Float,
        textWidth: Float,
        textY: Float,
        imageWidth: Float? = null
    ) {
        val coverLength = measuredWidth * progressPercent
        var startIndicator = measuredWidth / 2 - textWidth / 2
        var endIndicator = measuredWidth / 2 + textWidth / 2

        imageWidth?.let { width ->
            val contentWidth = textWidth + mIconPadding + width
            when (mIconGravity) {
                START -> {
                    startIndicator = (measuredWidth - contentWidth) / 2 + width + mIconPadding
                    endIndicator = (measuredWidth + contentWidth) / 2
                }
                else -> {
                    startIndicator = (measuredWidth - contentWidth) / 2
                    endIndicator = startIndicator + textWidth
                }
            }
        }

        val coverTextLength = coverLength - startIndicator
        val textProgressPercent = coverTextLength / textWidth

        when {
            coverLength <= startIndicator -> {
                mTextPaint.shader = null
                mTextPaint.color = mTextColor
            }
            coverLength > endIndicator -> {
                mTextPaint.shader = null
                mTextPaint.color = textColors.getColorForState(drawableState, mTextCoverColor)

//                mTextPaint.color = mTextCoverColor
            }
            else -> {
                val progressTextGradient = LinearGradient(
                    startIndicator, 0f, endIndicator, 0f,
                    intArrayOf(mTextCoverColor, mTextColor),
                    floatArrayOf(textProgressPercent, textProgressPercent + 0.001f),
                    Shader.TileMode.CLAMP
                )
                mTextPaint.color =
                    if (ColorUtils.calculateLuminance(mTextColor) < ColorUtils.calculateLuminance(
                            mTextCoverColor
                        )
                    ) {
                        mTextColor
                    } else {
                        mTextCoverColor
                    }
                mTextPaint.shader = progressTextGradient
            }
        }
        canvas.drawText(mCurrentText, startIndicator, textY, mTextPaint)
    }

    private fun drawImage(
        canvas: Canvas,
        icon: Drawable,
        progressPercent: Float,
        imageWidth: Float,
        imageHeight: Float,
        contentWidth: Float
    ) {
        val coverLength = measuredWidth * progressPercent
        var startIndicator = (measuredWidth - contentWidth) / 2

        if (mIconGravity == END) {
            startIndicator = (measuredWidth + contentWidth) / 2 - imageWidth
        }

        val coverIconLength = coverLength - startIndicator
        val iconProgressPercent =
            when {
                coverIconLength / imageWidth > 1 -> 1f
                coverIconLength / imageWidth < 0 -> 0f
                else -> coverIconLength / imageWidth
            }

        icon.colorFilter =
            PorterDuffColorFilter(mIconMaskColor, PorterDuff.Mode.MULTIPLY)
        canvas.translate(startIndicator, (height - imageHeight) / 2)
        icon.setBounds(0, 0, imageWidth.toInt(), imageHeight.toInt())
        if (!isClickable) {
            icon.draw(canvas)
        }

        icon.clearColorFilter()
        canvas.save()
        canvas.clipRect(
            RectF(
                0f,
                0f,
                imageWidth * iconProgressPercent,
                imageHeight
            )
        )

        icon.colorFilter =
            PorterDuffColorFilter(textColors.getColorForState(drawableState, mTextCoverColor), PorterDuff.Mode.MULTIPLY)

        icon.draw(canvas)
        icon.clearColorFilter()
        canvas.restore()
    }

    override fun onDraw(canvas: Canvas) {
        val progressPercent = (mProgress.toFloat() - mMinProgress) / (mMaxProgress - mMinProgress)
        mCurrentText = if (mProgress == mMaxProgress) {
            mFinishedText ?: text.toString()
        } else {
            text.toString()
        }

        drawBackground(canvas, progressPercent)
        drawContent(canvas, progressPercent)
    }


    fun setProgress(progress: Int) {
        mProgress = progress
        isClickable = mProgress == mMaxProgress
        invalidate()
    }

    fun max() = mMaxProgress
    fun min() = mMinProgress
}