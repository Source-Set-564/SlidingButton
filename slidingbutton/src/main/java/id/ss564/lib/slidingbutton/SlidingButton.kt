package id.ss564.lib.slidingbutton

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.*
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

/**
 * Created by Anwar on 29 Mar 2020.
 */

class SlidingButton : FrameLayout {

    private val inflatedView: View
    private lateinit var slidingImage: ImageView
    private lateinit var slidingText: TextView

    private var statusListener: OnStatusChangeListener? = null
    private var statusActive = false
        set(value) {
            field = value
            statusListener?.onStatusChange(value)
        }

    private var startOfButton = 0F
    private var endOfButton = 0F

    private val stateListIconTint: ColorStateList

    var buttonBackground: Drawable? = null
        set(value) {
            field = value
            if (::slidingImage.isInitialized)
                slidingImage.background = value
        }

    var buttonIcon: Drawable? = null
        set(value) {
            field = value
            if (::slidingImage.isInitialized)
                slidingImage.setImageDrawable(value)
        }

    var iconScaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_INSIDE
        set(value) {
            when (value) {
                ImageView.ScaleType.CENTER_INSIDE,
                ImageView.ScaleType.CENTER_CROP,
                ImageView.ScaleType.CENTER,
                ImageView.ScaleType.FIT_CENTER,
                ImageView.ScaleType.FIT_XY -> {
                    field = value
                    if (::slidingImage.isInitialized) slidingImage.scaleType = value
                }
                else -> throw IllegalArgumentException("ScaleType $value aren't allowed, please use CENTER_INSIDE,FIT_CENTER, or FIT_XY")
            }
        }

    var textBackground: Drawable? = null
        set(value) {
            field = value
            if (::slidingText.isInitialized) slidingText.background = value
        }

    private var mTextSize: Float = 0F
        private set(value) {
            field = value
            if (::slidingText.isInitialized)
                slidingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, value)
        }

    val textSize = mTextSize

    /**
     * [textPaddings]
     * index of the array mean [0] start,[1] top,[2] end,[3] bottom
     */
    val textPaddings = intArrayOf(0, 0, 0, 0)

    var textColors: ColorStateList? = null
        set(value) {
            field = value
            if (::slidingText.isInitialized)
                slidingText.setTextColor(value)
        }

    val currentTextColor = if (::slidingText.isInitialized) slidingText.currentTextColor else null

    var textTypeface: Typeface? = null
        set(value) {
            field = value
            if (::slidingText.isInitialized && value != null) slidingText.typeface = value
        }

    private var mText: String? = null
        set(value) {
            field = value
            if (::slidingText.isInitialized)
                slidingText.text = value
        }

    /**
     * [imageSize] is an array have 2 value, index  0 as Width and index 1 as Height
     */
    private val imageSize = intArrayOf(0, 0)
    var buttonWidth
        set(value) {
            imageSize[0] = value
            if (::slidingImage.isInitialized) slidingImage.layoutParams.width = value
        }
        get() = imageSize[0]
    var buttonHeight
        set(value) {
            imageSize[1] = value
            if (::slidingImage.isInitialized) slidingImage.layoutParams.height = value
        }
        get() = imageSize[1]

    /**
     * [buttonMargins],[buttonPaddings]
     * index of the array mean [0] start,[1] top,[2] end,[3] bottom
     */
    val buttonMargins = intArrayOf(0, 0, 0, 0)
    val buttonPaddings = intArrayOf(0, 0, 0, 0)

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        R.attr.slidingButtonStyle
    )

    constructor(
        _context: Context,
        attrs: AttributeSet?,
        defStyleInt: Int = R.attr.slidingButtonStyle
    ) : super(
        _context,
        attrs,
        defStyleInt
    ) {
        val colorPrimary = TypedValue()
        context.theme.resolveAttribute(R.attr.colorPrimary, colorPrimary, true)

        val colorAccent = TypedValue()
        context.theme.resolveAttribute(R.attr.colorAccent, colorAccent, true)

        val arr = context.obtainStyledAttributes(
            attrs,
            R.styleable.SlidingButton,
            defStyleInt,
            R.style.SlidingButton
        )

        /**
         * TextView attrs configuration
         */
        val defaultTextSize = context.resources.getDimension(R.dimen.default_text_size)
        mTextSize = arr.getDimension(R.styleable.SlidingButton_sliding_text_size, defaultTextSize)

        textColors = arr.getColorStateList(R.styleable.SlidingButton_sliding_text_color)
            ?: ColorStateList.valueOf(colorAccent.data)

        mText = arr.getString(R.styleable.SlidingButton_sliding_text)
        textBackground = arr.getDrawable(R.styleable.SlidingButton_sliding_text_background)

        val textStyle = arr.getInteger(R.styleable.SlidingButton_sliding_text_textStyle, 0).let {
            if (it < 0) 0 else it
        }
        val fontFamilyName = arr.getString(R.styleable.SlidingButton_sliding_text_fontFamily)
            ?: "sans-serif"
        textTypeface = Typeface.create(fontFamilyName, textStyle)
        textPaddings[0] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_text_paddingStart,
            0
        )
        textPaddings[1] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_text_paddingTop,
            0
        )
        textPaddings[2] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_text_paddingEnd,
            0
        )
        textPaddings[3] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_text_paddingBottom,
            0
        )

        /**
         * ImageView attrs configuration
         */
        val defaultButtonDrawable =
            ContextCompat.getDrawable(context, R.drawable.ic_default_slide_icon)
        buttonIcon = arr.getDrawable(R.styleable.SlidingButton_sliding_button_icon)
            ?: defaultButtonDrawable

        stateListIconTint =
            arr.getColorStateList(R.styleable.SlidingButton_sliding_button_icon_tint)
                ?: ColorStateList.valueOf(colorAccent.data)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            buttonIcon?.setTintList(stateListIconTint)
        } else {
            buttonIcon?.colorFilter =
                PorterDuffColorFilter(stateListIconTint.defaultColor, PorterDuff.Mode.SRC_IN)
        }

        buttonBackground = arr.getDrawable(R.styleable.SlidingButton_sliding_button_background)

        val defaultButtonSize = resources.getDimensionPixelSize(R.dimen.default_image_height)
        imageSize[0] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_width,
            defaultButtonSize
        )
        imageSize[1] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_height,
            defaultButtonSize
        )

        val scaleName = arr.getInteger(R.styleable.SlidingButton_sliding_icon_scaleType, 7).let {
            when (it) {
                1 -> "FIT_XY"
                3 -> "FIT_CENTER"
                5 -> "CENTER"
                6 -> "CENTER_CROP"
                else -> "CENTER_INSIDE"
            }
        }
        iconScaleType = ImageView.ScaleType.valueOf(scaleName)

        buttonMargins[0] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_marginStart,
            0
        )
        buttonMargins[1] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_marginTop,
            0
        )
        buttonMargins[2] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_marginEnd,
            0
        )
        buttonMargins[3] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_marginBottom,
            0
        )

        buttonPaddings[0] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_paddingStart,
            0
        )
        buttonPaddings[1] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_paddingTop,
            0
        )
        buttonPaddings[2] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_paddingEnd,
            0
        )
        buttonPaddings[3] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_paddingBottom,
            0
        )
        arr.recycle()

        inflatedView = LayoutInflater.from(context).inflate(R.layout.layout_button, this, true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        slidingText = inflatedView.findViewById(R.id.slidingText)
        slidingImage = inflatedView.findViewById(R.id.slidingImage)

        //configure ImageView
        slidingImage.background = buttonBackground
        slidingImage.layoutParams.let { it as LayoutParams }.also {
            it.width = imageSize[0]
            it.height = imageSize[1]
            it.setMargins(
                buttonMargins[0],
                buttonMargins[1],
                buttonMargins[2],
                buttonMargins[3]
            )
            slidingImage.layoutParams = it
        }
        slidingImage.setPadding(
            buttonPaddings[0],
            buttonPaddings[1],
            buttonPaddings[2],
            buttonPaddings[3]
        )
        slidingImage.scaleType = iconScaleType
        slidingImage.setImageDrawable(buttonIcon)

        //configure TextView
        slidingText.background = textBackground
        slidingText.setPadding(textPaddings[0], textPaddings[1], textPaddings[2], textPaddings[3])
        slidingText.text = mText
        slidingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize)
        slidingText.setTextColor(textColors)
        slidingText.typeface = textTypeface

        setupSlideTouch()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        startOfButton = buttonMargins[0].toFloat()
        endOfButton =
            w.toFloat() - (imageSize[0].toFloat() + buttonMargins[2].toFloat() + (paddingEnd * 2))
    }

    override fun removeAllViews() = throw IllegalStateException("This method isn't allowed ")

    override fun removeView(view: View?) = throw IllegalStateException("This method isn't allowed ")

    override fun removeViewAt(index: Int) =
        throw IllegalStateException("This method isn't allowed ")

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSlideTouch() {
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> (event.x <= slidingImage.x + slidingImage.width && slidingImage.x < slidingImage.width)
                        || (event.x >= this.width - slidingImage.width && slidingImage.x > slidingImage.width)
                MotionEvent.ACTION_MOVE -> {
                    onMove(event)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    onUp()
                    true
                }
                else -> false
            }
        }
    }

    private fun onUp() = when {
        slidingImage.x + slidingImage.width >= this.width * 0.58F -> {
            animatedToEnd()
        }
        slidingImage.x <= startOfButton -> {
            translateAnimation()
        }
        else -> animatedToStart()
    }

    private fun animatedToStart() {
        if (isActivated) isActivated = false

        val floatAnimator = ValueAnimator.ofFloat(slidingImage.x, startOfButton)
        floatAnimator.addUpdateListener {
            slidingImage.x = it.animatedValue as Float
        }
        floatAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                isActivated = false
                if (statusActive) statusActive = false
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}
        })
        floatAnimator.duration = 115L
        floatAnimator.interpolator = FastOutSlowInInterpolator()
        floatAnimator.start()
    }

    private fun animatedToEnd() {
        val floatAnimator = ValueAnimator.ofFloat(slidingImage.x, endOfButton)
        floatAnimator.addUpdateListener {
            slidingImage.x = it.animatedValue as Float
        }
        floatAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                isActivated = true
                if (!statusActive) statusActive = true
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {
                isActivated = false
            }
        })
        floatAnimator.duration = 115L
        floatAnimator.interpolator = FastOutSlowInInterpolator()
        floatAnimator.start()
    }

    private fun onMove(event: MotionEvent) {
        if (isActivated) isActivated = false

        if (event.x > slidingImage.width / 2
            && event.x + slidingImage.width / 2 < this.width
            && (event.x < slidingImage.x + slidingImage.width || slidingImage.x != 0F)
        ) {
            slidingImage.x = event.x - slidingImage.width / 2
        }

        if (slidingImage.x + slidingImage.width > this.width
            && slidingImage.x + slidingImage.width / 2 < this.width
        ) {
            slidingImage.x = this.width.toFloat() - slidingImage.width.toFloat()
        }

        if (event.x < slidingImage.width / 2 && slidingImage.x > 0) {
            slidingImage.x = startOfButton
        }
    }

    private fun translateAnimation() {
        val animation = TranslateAnimation(0F, measuredWidth.toFloat(), 0F, 0F)
        animation.interpolator = AccelerateDecelerateInterpolator()
        animation.duration = 350L
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                val anim = ScaleAnimation(
                    0.35F,
                    1F,
                    0.35F,
                    1F,
                    slidingImage.width.toFloat() / 2F,
                    slidingImage.height.toFloat() / 2F
                )
                anim.interpolator = DecelerateInterpolator()
                anim.duration = 225L
                slidingImage.startAnimation(anim)
            }

            override fun onAnimationStart(animation: Animation?) {}
        })
        slidingImage.startAnimation(animation)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        slidingText.isEnabled = enabled
        slidingImage.isEnabled = enabled
        changeStateDrawablePreLollipop()
    }

    fun setText(text: String) {
        mText = text
    }

    fun setText(@StringRes resId: Int) {
        mText = context.resources.getString(resId)
    }

    fun setTextSize(@Dimension size: Float) {
        mTextSize = size
    }

    fun setTextColor(@ColorInt color: Int) {
        textColors = ColorStateList.valueOf(color)
    }

    fun setTextBackground(@DrawableRes resId: Int) {
        textBackground = ContextCompat.getDrawable(context, resId)
    }

    fun setTextBackgroundColor(@ColorInt color: Int) {
        when (textBackground) {
            is ColorDrawable -> (slidingText.background as ColorDrawable).color = color
            else -> textBackground = ColorDrawable(color)
        }
    }

    fun setTextPadding(start: Int, top: Int, end: Int, bottom: Int) {
        textPaddings[0] = start
        textPaddings[1] = top
        textPaddings[2] = end
        textPaddings[3] = bottom
        slidingText.setPadding(textPaddings[0], textPaddings[1], textPaddings[2], textPaddings[3])
    }

    fun setButtonIcon(@DrawableRes resId: Int) {
        buttonIcon = ContextCompat.getDrawable(context, resId)
    }

    fun setButtonBackground(@DrawableRes resId: Int) {
        buttonBackground = ContextCompat.getDrawable(context, resId)
    }

    fun setButtonBackgroundColor(@ColorInt color: Int) {
        when (buttonBackground) {
            is ColorDrawable -> (slidingImage.background as ColorDrawable).color = color
            else -> buttonBackground = ColorDrawable(color)
        }
    }

    fun setButtonPadding(start: Int, top: Int, end: Int, bottom: Int) {
        buttonPaddings[0] = start
        buttonPaddings[1] = top
        buttonPaddings[2] = end
        buttonPaddings[3] = bottom
        slidingImage.setPadding(
            buttonPaddings[0],
            buttonPaddings[1],
            buttonPaddings[2],
            buttonPaddings[3]
        )
    }

    fun setButtonMargin(start: Int, top: Int, end: Int, bottom: Int) {
        buttonMargins[0] = start
        buttonMargins[1] = top
        buttonMargins[2] = end
        buttonMargins[3] = bottom
        slidingImage.layoutParams.let { it as LayoutParams }.setMargins(
            buttonMargins[0],
            buttonMargins[1],
            buttonMargins[2],
            buttonMargins[3]
        )
    }

    fun setOnStatusChangeListener(listener: OnStatusChangeListener?) {
        statusListener = listener
    }

    fun setOnStatusChangeListener(l: (active: Boolean) -> Unit) {
        this.setOnStatusChangeListener(object : OnStatusChangeListener {
            override fun onStatusChange(active: Boolean) {
                l.invoke(active)
            }
        })
    }

    override fun setActivated(activated: Boolean) {
        super.setActivated(activated)
        changeStateDrawablePreLollipop()
    }

    private fun changeStateDrawablePreLollipop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) return

        slidingImage.drawable.colorFilter = PorterDuffColorFilter(
            stateListIconTint.getColorForState(
                slidingImage.drawableState,
                stateListIconTint.defaultColor
            ), PorterDuff.Mode.SRC_IN
        )
    }

    interface OnStatusChangeListener {
        fun onStatusChange(active: Boolean)
    }
}