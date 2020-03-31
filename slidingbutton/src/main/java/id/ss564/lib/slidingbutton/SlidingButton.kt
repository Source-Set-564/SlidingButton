package id.ss564.lib.slidingbutton

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.annotation.Dimension
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

/**
 * Created by Anwar on 29 Mar 2020.
 */

class SlidingButton : FrameLayout {

    private lateinit var inflatedView: View
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

    private var mTextBackground: Drawable? = null
        set(value) {
            field = value
            if (::slidingText.isInitialized) slidingText.background = value
        }

    val textBackground = mTextBackground

    private var mTextSize: Float = 0F
        private set(value) {
            field = value
            if (::slidingText.isInitialized)
                slidingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, value)
        }

    val textSize = mTextSize

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
     * [imageMargins],[imagePaddings]
     * index of the array mean [0] start,[1] top,[2] end,[3] bottom
     */
    private val imageMargins = intArrayOf(0, 0, 0, 0)
    private val imagePaddings = intArrayOf(0, 0, 0, 0)

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleInt: Int) : super(
        context,
        attrs,
        defStyleInt
    ) {
        initialized(attrs, defStyleInt, R.style.SlidingButton)
    }

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet?, defStyleInt: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleInt,
        defStyleRes
    ) {
        initialized(attrs, defStyleInt, defStyleRes)
    }

    private fun initialized(attrs: AttributeSet?, defStyleInt: Int, defStyleRes: Int) {
        val colorPrimary = TypedValue()
        context.theme.resolveAttribute(R.attr.colorPrimary, colorPrimary, true)

        val colorAccent = TypedValue()
        context.theme.resolveAttribute(R.attr.colorAccent, colorAccent, true)

        val arr = context.obtainStyledAttributes(
            attrs,
            R.styleable.SlidingButton,
            defStyleInt,
            defStyleRes
        )

        /**
         * TextView attrs configuration
         */
        val defaultTextSize = context.resources.getDimension(R.dimen.default_text_size)
        mTextSize = arr.getDimension(R.styleable.SlidingButton_sliding_text_size, defaultTextSize)

        textColors = arr.getColorStateList(R.styleable.SlidingButton_sliding_text_color)
            ?: ColorStateList.valueOf(colorAccent.data)

        mText = arr.getString(R.styleable.SlidingButton_sliding_text)
        mTextBackground = arr.getDrawable(R.styleable.SlidingButton_sliding_text_background)

        val textStyle = arr.getInteger(R.styleable.SlidingButton_sliding_text_textStyle, 0).let {
            if (it < 0) 0 else it
        }
        val fontFamilyName = arr.getString(R.styleable.SlidingButton_sliding_text_fontFamily)
            ?: "sans-serif"
        textTypeface = Typeface.create(fontFamilyName, textStyle)

        /**
         * ImageView attrs configuration
         */
        val defaultButtonDrawable =
            ContextCompat.getDrawable(context, R.drawable.ic_default_slide_icon)
        buttonIcon = arr.getDrawable(R.styleable.SlidingButton_sliding_button_icon)
            ?: defaultButtonDrawable

        val stateListIconTint =
            arr.getColorStateList(R.styleable.SlidingButton_sliding_button_icon_tint)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            buttonIcon?.setTintList(stateListIconTint)
        } else if (stateListIconTint != null) {
            buttonIcon?.colorFilter =
                PorterDuffColorFilter(stateListIconTint.defaultColor, PorterDuff.Mode.SRC_IN)
        }

        val defaultButtonBackground = ContextCompat.getDrawable(
            context,
            R.drawable.default_slidingbutton_background
        )
        buttonBackground = arr.getDrawable(R.styleable.SlidingButton_sliding_button_background)
            ?: defaultButtonBackground

        val defaultButtonSize = resources.getDimensionPixelSize(R.dimen.default_image_height)
        imageSize[0] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_width,
            defaultButtonSize
        )
        imageSize[1] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_height,
            defaultButtonSize
        )

        imageMargins[0] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_marginStart,
            0
        )
        imageMargins[1] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_marginTop,
            0
        )
        imageMargins[2] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_marginEnd,
            0
        )
        imageMargins[3] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_marginBottom,
            0
        )

        imagePaddings[0] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_paddingStart,
            0
        )
        imagePaddings[1] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_paddingTop,
            0
        )
        imagePaddings[2] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_paddingEnd,
            0
        )
        imagePaddings[3] = arr.getDimensionPixelSize(
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
                imageMargins[0],
                imageMargins[1],
                imageMargins[2],
                imageMargins[3]
            )
            slidingImage.layoutParams = it
        }
        slidingImage.setPadding(
            imagePaddings[0],
            imagePaddings[1],
            imagePaddings[2],
            imagePaddings[3]
        )
        slidingImage.setImageDrawable(buttonIcon)

        //configure TextView
        slidingText.background = mTextBackground
        slidingText.text = mText
        slidingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize)
        slidingText.setTextColor(textColors)
        slidingText.typeface = textTypeface

        setupSlideTouch()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        startOfButton = imageMargins[0].toFloat()
        endOfButton = w.toFloat() - (imageSize[0].toFloat() + imageMargins[2].toFloat() + (paddingEnd * 2))
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
        /*endOfButton =
            this.width.toFloat() - (imageSize[0].toFloat() + imageMargins[2].toFloat() + (paddingStart.toFloat() / 2))*/
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
                val anim = TranslateAnimation(-slidingImage.width.toFloat(), 0F, 0F, 0F)
                anim.interpolator = AccelerateDecelerateInterpolator()
                anim.duration = 225L
                slidingImage.startAnimation(anim)
            }

            override fun onAnimationStart(animation: Animation?) {}
        })
        slidingImage.startAnimation(animation)
    }

    override fun setActivated(activated: Boolean) {
        super.setActivated(activated)
        slidingText.isActivated = activated
        slidingImage.isActivated = activated
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        slidingText.isEnabled = enabled
        slidingImage.isEnabled = enabled
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

    interface OnStatusChangeListener {
        fun onStatusChange(active: Boolean)
    }
}