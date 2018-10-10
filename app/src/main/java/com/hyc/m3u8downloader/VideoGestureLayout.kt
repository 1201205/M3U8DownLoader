package com.hyc.m3u8downloader

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.RelativeLayout

class VideoGestureLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    init {
        val gestureDetector = GestureDetector(context, this)
        gestureDetector.setIsLongpressEnabled(false)
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                state = STATE_NONE
                listener?.onUp()
            }
            gestureDetector.onTouchEvent(event)
        }
    }

    var listener: VideoGestureListener? = null
    private val STATE_NONE = 0
    private val STATE_FOR_BRIGHTNESS = 1
    private val STATE_FOR_VOLUME = 2
    private val STATE_FOR_SEEK = 3
    private var state: Int = STATE_NONE
    private val OFFSET = ViewConfiguration.get(getContext()).scaledTouchSlop

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        when (state) {
            STATE_NONE -> state = if (Math.abs(distanceX) - Math.abs(distanceY) > 1 && Math.abs(distanceX) > OFFSET) {
                STATE_FOR_SEEK
            } else {
                if (e1!!.x > width / 2) {
                    STATE_FOR_VOLUME
                } else {
                    STATE_FOR_BRIGHTNESS
                }
            }
            STATE_FOR_VOLUME ->
                    listener?.onVolumeChange(distanceY)
            STATE_FOR_BRIGHTNESS -> listener?.onBrightnessChange(distanceY)
            STATE_FOR_SEEK -> listener?.onSeekChange(distanceX)
        }
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        listener?.onDoubleTap()
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        listener?.onSingleTap()
        return true
    }

    public interface VideoGestureListener {
        fun onBrightnessChange(size: Float)
        fun onVolumeChange(size: Float)
        fun onSeekChange(size: Float)
        fun onSingleTap()
        fun onDoubleTap()
        fun onUp()
    }
}