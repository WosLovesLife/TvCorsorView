package com.feiboedu.common.view

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.FocusFinder
import android.view.MotionEvent
import android.view.View
import android.view.View.OnHoverListener
import android.view.ViewGroup
import android.widget.FrameLayout
import com.wosloveslife.tvcorsorview.R
import kotlin.math.roundToInt

class CursorView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var mShowing: Boolean = false

    private var mTop: Int = 0

    private val mBorderWidth: Int = (resources.displayMetrics.density * 20).toInt()

    private val mCursor = View(context)

    private val mAnimator = ValueAnimator.ofFloat(0F, 1F)
    private var mStartRect = Rect()
    private var mTargetRect = Rect()

    init {
        mCursor.setBackgroundResource(R.drawable.focusable_cursor)
        addView(mCursor)

        val animatorL = ValueAnimator()
        animatorL.duration = 300
        animatorL.addUpdateListener {
            mCursor.left = it.animatedValue as Int
        }

        val animatorT = ValueAnimator()
        animatorT.duration = 300
        animatorT.addUpdateListener {
            mCursor.top = it.animatedValue as Int
        }

        val animatorR = ValueAnimator()
        animatorR.duration = 300
        animatorR.addUpdateListener {
            mCursor.right = it.animatedValue as Int
        }

        val animatorB = ValueAnimator()
        animatorB.duration = 300
        animatorB.addUpdateListener {
            mCursor.bottom = it.animatedValue as Int
        }

        mAnimator.addUpdateListener { value ->
            // 目标点 - 起始点为需要偏移的总量 * 动画进度得到当前偏移量
            val left = (mTargetRect.left - mStartRect.left) * (value.animatedValue as Float)
            val top = (mTargetRect.top - mStartRect.top) * (value.animatedValue as Float)
            val right = (mTargetRect.right - mStartRect.right) * (value.animatedValue as Float)
            val bottom = (mTargetRect.bottom - mStartRect.bottom) * (value.animatedValue as Float)

            mCursor.left = mStartRect.left + left.roundToInt()
            mCursor.top = mStartRect.top + top.roundToInt()
            mCursor.right = mStartRect.right + right.roundToInt()
            mCursor.bottom = mStartRect.bottom + bottom.roundToInt()
        }

        viewTreeObserver.addOnGlobalFocusChangeListener { _, newFocus ->
            if (newFocus == null) return@addOnGlobalFocusChangeListener

            startTransitionAnim(calculateRect(newFocus))
        }
    }

    private fun calculateRect(focusedView: View): Rect {// 忽略RecyclerView的焦点
        if (focusedView is RecyclerView) {
            return mTargetRect
        }

        val locationInWindow = IntArray(2)
        focusedView.getLocationInWindow(locationInWindow)
        val left = locationInWindow[0]
        val top = locationInWindow[1] - mTop

        return Rect(left - mBorderWidth,
                top - mBorderWidth,
                left + focusedView.width + mBorderWidth,
                top + focusedView.height + mBorderWidth)
    }

    private fun startTransitionAnim(target: Rect) {
        if (target == mTargetRect) return

        mStartRect = Rect(mCursor.left, mCursor.top, mCursor.right, mCursor.bottom)
        mTargetRect = target
        if (mAnimator.isRunning) {
            mAnimator.cancel()
        }
        mAnimator.start()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val contentView = parent as ViewGroup
        contentView.viewTreeObserver.addOnScrollChangedListener {
            val focusedView = contentView.findFocus() ?: return@addOnScrollChangedListener

            startTransitionAnim(calculateRect(focusedView))
        }

        contentView.viewTreeObserver.addOnGlobalLayoutListener {
            registerHoverEventListenerOfFocusableView(contentView)
        }
    }

    private val onHoverListener = OnHoverListener { v, _ ->
        if (v.hasFocusable()) {
            startTransitionAnim(calculateRect(v))
        }
        return@OnHoverListener false
    }

    private fun registerHoverEventListenerOfFocusableView(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val childAt = viewGroup.getChildAt(i)
            if (childAt.isFocusable) {
                childAt.setOnHoverListener(onHoverListener)
            }
            if (childAt is ViewGroup) {
                registerHoverEventListenerOfFocusableView(childAt)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val view = parent as View
        val focusedView = view.findFocus()
        val rect = if (focusedView == null) {
            Rect()
        } else {
            calculateRect(focusedView)
        }

        val widthSpec = MeasureSpec.makeMeasureSpec(rect.right - rect.left, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(rect.bottom - rect.top, MeasureSpec.EXACTLY)

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChildWithMargins(child, widthSpec, 0, heightSpec, 0)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val locationInWindow = IntArray(2)
        getLocationInWindow(locationInWindow)
        mTop = locationInWindow[1]
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val view = parent as View
        val focusedView = view.findFocus()
        val rect = if (focusedView == null) {
            Rect()
        } else {
            calculateRect(focusedView)
        }

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.layout(rect.left, rect.top, rect.right, rect.bottom)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
//        return super.dispatchTouchEvent(event)
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        return super.onTouchEvent(event)
        return false
    }

    fun show(activity: Activity) {
        if (parent != null) {
            return
        }
        val container = activity.window.decorView.findViewById<View>(android.R.id.content) as ViewGroup
        container.addView(this)
        mShowing = true
    }

    fun show(dialog: Dialog) {
        if (parent != null) {
            return
        }
        val view = dialog.window?.decorView?.findViewById<View>(android.R.id.content)
                ?: throw IllegalStateException("请检查Dialog的Window状态, decorView 中是否包含 content")

        val container = view as ViewGroup
        container.viewTreeObserver.addOnGlobalLayoutListener {
            if (parent == null)
                container.addView(this, FrameLayout.LayoutParams(container.width, container.height))
            else
                layoutParams = FrameLayout.LayoutParams(container.width, container.height)
        }
        mShowing = true
    }

    fun dismiss() {
        val parent = parent ?: return
        if (parent is ViewGroup) {
            parent.removeView(this)
        }
        mShowing = false
    }
}
