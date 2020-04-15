package id.ss564.lib.slidingbutton

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.*
import androidx.core.content.ContextCompat
import kotlin.math.max

/**
 * Created by Anwar on 29 Mar 2020.
 */

class SlidingButton : FrameLayout {

    private val inflatedView: View
    private lateinit var slidingImage: ImageView
    private lateinit var slidingText: TextView
    private lateinit var slidingIndicator: View

    private var mStateListener: OnStateChangeListener? = null
    private var slidingListener: OnSlidingListener? = null

    private var statusActive = false
        set(value) {
            field = value
            if (value && showIndicator) animatedIndicator()
            mStateListener?.onChange(value)
        }

    private var startOfButton = 0F
    private var endOfButton = 0F

    private var showIndicator = true
    private var enableTextAlpha = true

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
                else -> throw IllegalArgumentException("ScaleType $value aren't allowed, please use CENTER, CENTER_CROP, CENTER_INSIDE,FIT_CENTER, or FIT_XY")
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

    val textSize: Float
        get() = if (::slidingText.isInitialized) {
            slidingText.textSize
        } else {
            mTextSize
        }

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

    val currentTextColor =
        if (::slidingText.isInitialized) slidingText.currentTextColor else textColors?.defaultColor

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

    var buttonWidth: Int = 0
        set(value) {
            field = value
            if (::slidingImage.isInitialized) slidingImage.layoutParams.width = value
        }

    var buttonHeight: Int = 0
        set(value) {
            field = value
            if (::slidingImage.isInitialized) slidingImage.layoutParams.height = value
        }

    /**
     * [buttonMargins],[buttonPaddings]
     * index of the array mean [0] start,[1] top,[2] end,[3] bottom
     */
    val buttonMargins = intArrayOf(0, 0, 0, 0)
    val buttonPaddings = intArrayOf(0, 0, 0, 0)

    private var trackExtendedTo = TrackExtended.BUTTON

    var cornerRadius: Float = 0F
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        set(value) {
            field = value
            if (::slidingImage.isInitialized) {
                RoundedOutlineProvider(value).also {
                    this.outlineProvider = it
                    slidingImage.outlineProvider = it
                    slidingText.outlineProvider = it
                    slidingIndicator.outlineProvider = it
                }
            }
        }

    var trackBackground: Drawable? = null
        set(value) {
            field = value
            if (::slidingIndicator.isInitialized) {
                slidingIndicator.background = value
            }
        }

    var trackBackgroundTint: ColorStateList? = null
        set(value) {
            field = value
            if (::slidingIndicator.isInitialized) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    slidingIndicator.background?.setTintList(value)
                } else if (value != null) {
                    slidingIndicator.background?.colorFilter = PorterDuffColorFilter(
                        value.getColorForState(slidingIndicator.drawableState, value.defaultColor),
                        PorterDuff.Mode.SRC_IN
                    )
                }
            }
        }

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

        val ex by lazy {
            TypedValue().apply {
                context.theme.resolveAttribute(
                    R.attr.colorAccent,
                    this,
                    true
                )
            }.data
        }
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
            ?: ColorStateList.valueOf(ex)

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

        stateListIconTint = arr.getColorStateList(
            R.styleable.SlidingButton_sliding_button_icon_tint
        ) ?: ColorStateList.valueOf(colorAccent.data)

        buttonBackground = arr.getDrawable(R.styleable.SlidingButton_sliding_button_background)

        val defaultButtonSize = resources.getDimensionPixelSize(R.dimen.default_image_height)
        buttonWidth = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_width,
            defaultButtonSize
        )
        buttonHeight = arr.getDimensionPixelSize(
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

        enableTextAlpha = arr.getBoolean(R.styleable.SlidingButton_sliding_enabledTextAlpha, true)
        showIndicator = arr.getBoolean(R.styleable.SlidingButton_sliding_showTrack, false)

        trackBackground = arr.getDrawable(R.styleable.SlidingButton_sliding_trackBackground)
            ?: ContextCompat.getDrawable(context, R.drawable.default_sliding_indicator_background)

        trackBackgroundTint = arr.getColorStateList(
            R.styleable.SlidingButton_sliding_trackBackgroundTint
        )

        val index = arr.getInteger(R.styleable.SlidingButton_sliding_trackExtendTo, 1)
        trackExtendedTo = arrayOf(TrackExtended.CONTAINER, TrackExtended.BUTTON)[index]

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cornerRadius = arr.getDimension(R.styleable.SlidingButton_sliding_corner_radius, 0F)
            buttonIcon?.setTintList(stateListIconTint)
            trackBackground?.setTintList(trackBackgroundTint)
        } else {
            buttonIcon?.colorFilter = PorterDuffColorFilter(
                stateListIconTint.defaultColor,
                PorterDuff.Mode.SRC_IN
            )
            if (trackBackgroundTint != null && trackBackground != null) {
                trackBackground?.colorFilter = PorterDuffColorFilter(
                    trackBackgroundTint!!.getColorForState(
                        trackBackground!!.state,
                        trackBackgroundTint!!.defaultColor
                    ),
                    PorterDuff.Mode.SRC_IN
                )
            }
        }

        arr.recycle()

        inflatedView = LayoutInflater.from(context).inflate(R.layout.layout_button, this, true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        slidingText = inflatedView.findViewById(R.id.slidingText)
        slidingIndicator = inflatedView.findViewById(R.id.slidingIndicator)
        slidingImage = inflatedView.findViewById(R.id.slidingImage)

        configureTextView()
        configureTrackView()
        configureImageView()

        //Apply Rounded corner to views
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            RoundedOutlineProvider(cornerRadius).also {
                this.outlineProvider = it
                slidingText.outlineProvider = it
                slidingImage.outlineProvider = it
                slidingIndicator.outlineProvider = it
            }
        }

        configureTouch()
    }

    private fun configureTrackView() {
        if (showIndicator) {
            slidingIndicator.background = trackBackground
            slidingIndicator.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    slidingIndicator.visibility = View.VISIBLE
                    val lp = slidingIndicator.layoutParams as LayoutParams
                    when (trackExtendedTo) {
                        TrackExtended.BUTTON -> {
                            lp.width = startOfButton.toInt() + buttonWidth - buttonMargins[2]
                            lp.height = buttonHeight
                            lp.marginStart = buttonMargins[0]
                            lp.marginEnd = buttonMargins[2]
                        }
                        TrackExtended.CONTAINER -> {
                            lp.width = 0
                            lp.height = this@SlidingButton.measuredHeight
                            lp.marginStart = 0
                            lp.marginEnd = 0
                        }
                    }
                    slidingIndicator.layoutParams = lp
                    inflatedView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        } else {
            slidingIndicator.visibility = View.GONE
        }
    }

    private fun configureImageView() {
        slidingImage.background = buttonBackground
        slidingImage.layoutParams.let { it as LayoutParams }.also {
            it.width = buttonWidth
            it.height = buttonHeight
            it.marginStart = buttonMargins[0]
            it.topMargin = buttonMargins[1]
            it.marginEnd = buttonMargins[2]
            it.bottomMargin = buttonMargins[3]
            slidingImage.layoutParams = it
        }
        slidingImage.setPaddingRelative(
            buttonPaddings[0],
            buttonPaddings[1],
            buttonPaddings[2],
            buttonPaddings[3]
        )
        slidingImage.scaleType = iconScaleType
        slidingImage.setImageDrawable(buttonIcon)
    }

    private fun configureTextView() {
        slidingText.background = textBackground
        slidingText.setPaddingRelative(
            textPaddings[0],
            textPaddings[1],
            textPaddings[2],
            textPaddings[3]
        )
        slidingText.text = mText
        slidingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize)
        slidingText.setTextColor(textColors)
        slidingText.typeface = textTypeface
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        startOfButton = buttonMargins[0].toFloat()
        endOfButton =
            w.toFloat() - (buttonWidth.toFloat() + buttonMargins[2].toFloat() + paddingEnd.toFloat() + paddingStart.toFloat())
    }

    override fun removeAllViews() = throw IllegalStateException("This method isn't allowed ")

    override fun removeView(view: View?) = throw IllegalStateException("This method isn't allowed ")

    override fun removeViewAt(index: Int) =
        throw IllegalStateException("This method isn't allowed ")

    private fun configureTouch() {
        this.setOnTouchListener { _, event ->
            val startTouch = (paddingStart + buttonMargins[0]).toFloat()
            val maxStartTouch = startTouch + buttonWidth
            val isStartTouch = event.x in startTouch..maxStartTouch

            val maxEndTouch = (this.width - paddingEnd - buttonMargins[2]).toFloat()
            val isEndTouch = event.x in endOfButton..maxEndTouch

            when (event.action) {
                MotionEvent.ACTION_DOWN -> (isStartTouch && !statusActive) || (isEndTouch && statusActive)
                MotionEvent.ACTION_MOVE -> onMove(event, startTouch, endOfButton + buttonWidth)
                MotionEvent.ACTION_UP -> onUp()
                else -> true
            }
        }
    }

    override fun performClick(): Boolean = super.performClick()

    private fun onUp(): Boolean = when {
        slidingImage.x + buttonWidth < this.width * 0.55F && slidingImage.x > startOfButton -> {
            animatedToStart()
            true
        }
        slidingImage.x + buttonWidth >= this.width * 0.55F -> {
            animatedToEnd()
            true
        }
        slidingImage.x <= startOfButton -> {
            translateAnimation()
            true
        }
        else -> false
    }

    fun changeState(active: Boolean, animated: Boolean = false) {
        if (animated && active) {
            statusActive = true
            animatedToEnd()
        } else if (animated && !active) {
            statusActive = false
            animatedToStart()
        } else if (active) {
            slidingImage.x = endOfButton
            statusActive = true
        } else {
            slidingImage.x = startOfButton
            statusActive = false
        }
    }

    private fun animatedToStart() {
        if (isActivated) isActivated = false

        val floatAnimator = ValueAnimator.ofFloat(slidingImage.x, startOfButton)
        floatAnimator.addUpdateListener {
            updateSlidingXPosition(it.animatedValue as Float)
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
        floatAnimator.interpolator = AccelerateDecelerateInterpolator()
        floatAnimator.start()
    }

    private fun animatedToEnd() {
        val floatAnimator = ValueAnimator.ofFloat(slidingImage.x, endOfButton)
        floatAnimator.addUpdateListener {
            updateSlidingXPosition(it.animatedValue as Float)
        }
        floatAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                isActivated = true
                if (!statusActive) statusActive = true
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}
        })
        floatAnimator.duration = 115L
        floatAnimator.interpolator = AccelerateDecelerateInterpolator()
        floatAnimator.start()
    }

    private fun animatedIndicator() {
        val animation = ScaleAnimation(
            0F,
            1F,
            1F,
            1F,
            slidingIndicator.width.toFloat(),
            slidingIndicator.height * 0.5F
        )
        animation.duration = 500L
        animation.interpolator = DecelerateInterpolator()
        slidingIndicator.startAnimation(animation)
    }

    private fun onMove(event: MotionEvent, start: Float, end: Float): Boolean {
        if (isActivated) isActivated = false

        if (event.x < startOfButton + buttonWidth) {
            updateSlidingXPosition(startOfButton)
            return true
        }

        if (event.x in start..end) {
            updateSlidingXPosition(event.x - buttonWidth)
            return true
        }

        return false
    }

    private fun updateSlidingXPosition(x: Float) {
        slidingImage.x = x
        val realX = x - startOfButton
        val percent = realX / (endOfButton - startOfButton)

        if (enableTextAlpha) {
            slidingText.alpha = if (percent < 0.2F) 1F - percent else max(0F, 1F - (percent + 0.3F))
        }

        if (showIndicator) {
            val lp = slidingIndicator.layoutParams as LayoutParams
            lp.width = when {
                trackExtendedTo == TrackExtended.CONTAINER && percent == 0F -> 0
                trackExtendedTo == TrackExtended.CONTAINER && percent < 1F -> x.toInt() + buttonWidth
                trackExtendedTo == TrackExtended.CONTAINER && percent >= 1F -> x.toInt() + buttonWidth + buttonMargins[2]
                else -> x.toInt() + buttonWidth - buttonMargins[2]
            }
            slidingIndicator.layoutParams = lp
        }
        slidingListener?.onSliding(percent)
    }

    private fun translateAnimation() {
        slidingImage.clearAnimation()
        val animation = TranslateAnimation(0F, endOfButton, 0F, 0F)
        animation.interpolator = AccelerateDecelerateInterpolator()
        animation.duration = 350L
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                slidingImage.clearAnimation()
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
                anim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        slidingImage.clearAnimation()
                        slidingImage.scaleX = 1F
                        slidingImage.scaleY = 1F
                        if (showIndicator) slidingIndicator.alpha = 1F
                    }

                    override fun onAnimationStart(animation: Animation?) {}
                })
                slidingImage.startAnimation(anim)
            }

            override fun onAnimationStart(animation: Animation?) {
                if (showIndicator) slidingIndicator.alpha = 0F
            }
        })
        slidingImage.startAnimation(animation)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        slidingText.isEnabled = enabled
        slidingImage.isEnabled = enabled
        slidingIndicator.isEnabled = enabled
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

    fun setOnStateChangeListener(listener: OnStateChangeListener?) {
        mStateListener = listener
    }

    fun setOnStateChangeListener(l: (active: Boolean) -> Unit) {
        this.setOnStateChangeListener(object : OnStateChangeListener {
            override fun onChange(active: Boolean) {
                l.invoke(active)
            }
        })
    }

    fun setOnSlidingListener(listener: OnSlidingListener?) {
        slidingListener = listener
    }

    fun setOnSlidingListener(l: (progress: Float) -> Unit) {
        this.setOnSlidingListener(object : OnSlidingListener {
            override fun onSliding(progress: Float) {
                l.invoke(progress)
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

        trackBackgroundTint?.let {
            slidingIndicator.background?.colorFilter = PorterDuffColorFilter(
                it.getColorForState(slidingIndicator.drawableState, it.defaultColor),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    interface OnStateChangeListener {
        fun onChange(active: Boolean)
    }

    interface OnSlidingListener {
        fun onSliding(progress: Float)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private class RoundedOutlineProvider(val radius: Float) : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val rect = Rect(0, 0, view.width, view.height)
            outline.setRoundRect(rect, radius)
            view.clipToOutline = true
        }
    }

    @Keep
    enum class TrackExtended constructor(val value: Int) {
        CONTAINER(0),
        BUTTON(1)
    }
}