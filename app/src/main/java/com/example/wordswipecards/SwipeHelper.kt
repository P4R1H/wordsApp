package com.example.wordswipecards

import android.animation.Animator
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.OvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet.Motion
import com.example.wordswipecards.utils.animationUtils
class SwipeHelper(private val mSwipeStack: SwipeStack) : OnTouchListener {
    private var mObservedView: View? = null
    private var mListenForTouchEvents = false
    private var mDownX = 0f
    private var mDownY = 0f //f means float
    private var mInitialX = 0f
    private var mInitialY = 0f
    private var mPointerId = 0
    private var mRotateDegrees = SwipeStack.DEFAULT_SWIPE_ROTATION
    private var mOpacityEnd = SwipeStack.DEFAULT_SWIPE_OPACITY
    private var mAnimationDuration = SwipeStack.DEFAULT_ANIMATION_DURATION

    override fun onTouch(v : View, event: MotionEvent): Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                if(!mListenForTouchEvents || !mSwipeStack.isEnabled) {
                    return false
                }
                v.parent.requestDisallowInterceptTouchEvent(true)
                mSwipeStack.onSwipeStart()
                mPointerId = event.getPointerId(0)
                mDownX = event.getX(mPointerId)
                mDownY = event.getY(mPointerId)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(mPointerId)
                if(pointerIndex<0){
                    return false
                }
                val dx = event.getX(pointerIndex) - mDownX
                val dy = event.getY(pointerIndex) - mDownY

                val newX = mObservedView!!.x + dx
                val newY = mObservedView!!.y + dy
                mObservedView!!.x = newX
                mObservedView!!.y = newY

                val dragDistance = newX - mInitialX
                val swipeProgress = Math.min(
                    Math.max(
                        dragDistance / mSwipeStack.width, -1f
                    ), 1f
                )
                mSwipeStack.onSwipeProgress(swipeProgress)

                if(mRotateDegrees > 0) {
                    val rotation = mRotateDegrees * swipeProgress
                    mObservedView!!.rotation = rotation
                }
                if(mOpacityEnd < 1f) {
                    val alpha = 1 - Math.min(Math.abs(swipeProgress*2), 1f)
                    mObservedView!!.alpha = alpha
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                v.parent.requestDisallowInterceptTouchEvent(false)
                mSwipeStack.onSwipeEnd()
                checkViewPosition()
                return true
            }
        }
        return false
    }

    private fun checkViewPosition() {
        if (!mSwipeStack.isEnabled){
            resetViewPosition()
            return
        }
        val viewCenterHorizontal = mObservedView!!.x + mObservedView!!.width / 2
        val parentFirstThird = mSwipeStack.width/3f
        val parentLastThird = parentFirstThird*2

        if (viewCenterHorizontal < parentFirstThird &&
            mSwipeStack.allowedSwipeDirections != SwipeStack.SWIPE_DIRECTION_ONLY_RIGHT
        ) {
            swipeViewToLeft(mAnimationDuration/2)
        } else if(viewCenterHorizontal > parentLastThird &&
            mSwipeStack.allowedSwipeDirections != SwipeStack.SWIPE_DIRECTION_ONLY_LEFT
        ) {
            swipeViewToRight(mAnimationDuration/2)
        } else {
            resetViewPosition()
        }
    }

    private fun resetViewPosition() {
        mObservedView!!.animate()
            .x(mInitialX)
            .y(mInitialY)
            .rotation(0f)
            .alpha(1f)
            .setDuration(mAnimationDuration.toLong())
            .setInterpolator(OvershootInterpolator(1.4f))
            .setListener(null)
    }

    private fun swipeViewToLeft() {

    }

    private fun swipeViewToRight() {

    }
}