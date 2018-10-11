package com.hyc.m3u8downloader.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.hyc.m3u8downloader.R
import com.hyc.m3u8downloader.VideoGestureLayout
import com.hyc.m3u8downloader.model.MediaHistory
import com.hyc.m3u8downloader.model.MediaItemDao
import com.hyc.m3u8downloader.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SetTextI18n")
class VideoActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {
    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        surface?.release()
        return true
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture?, width: Int, height: Int) {
        initPlayer(surfaceTexture)
    }

    @SuppressLint("SetTextI18n")
    private fun initPlayer(surfaceTexture: SurfaceTexture?) {
        val surface = Surface(surfaceTexture)
        player.setSurface(surface)
        player.setScreenOnWhilePlaying(true)
        player.setOnCompletionListener { onBackPressed() }
        player.dataSource = path
        player.prepareAsync()
        player.isLooping = false
        player.setOnPreparedListener { _ ->
            mDuration = player.duration
            mDurationTime = getTimeText(mDuration)
            tvDuration.text = "/$mDurationTime"
            startChangeProgress()
            sbProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    player.seekTo(sbProgress.progress * mDuration / 100)
                }
            })
            mHistory?.let {
                if (it.time + 5000 < mDuration) {
                    player.seekTo(it.time)
                }
            }
            player.start()
        }
    }

    override fun onPause() {
        super.onPause()
        if (player.isPlaying) {
            player.pause()
            needResume = true
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (needResume&&!player.isPlaying) {
            player.start()
            needResume = false
        }
    }

    fun startChangeProgress() {
        val time = player.currentPosition
        setTime(time)
        sbProgress.progress = (time * 100 / mDuration).toInt()
        mHandler.sendEmptyMessageDelayed(MSG_PROGRESS, 300)
    }

    private fun setTime(currentPosition: Long) {
        tvTime.text = getTimeText(currentPosition)
    }

    private fun getTimeText(time: Long): String {
        return formatTime(time)
    }

    val MSG_HIDE = 2
    val MSG_PROGRESS = 3
    val MSG_HIDE_TIP = 4

    private lateinit var txvVideo: TextureView
    private lateinit var vglScreen: VideoGestureLayout
    private lateinit var player: IjkMediaPlayer
    private lateinit var llControl: LinearLayout
    private lateinit var btPause: Button
    private lateinit var sbProgress: SeekBar
    private lateinit var tvTime: TextView
    private lateinit var tvDuration: TextView
    private lateinit var ico: View
    private lateinit var tvTip: TextView
    private lateinit var llTip: LinearLayout
    private var mDuration = 0L
    private lateinit var brightnessController: BrightnessController
    private var isControlShowing = true
    private var mSeekTime = 0F
    private var mDurationTime = ""
    private val mMaxVolume = AudioVolumeController.getInstance().getMaxVolume()
    private var mHistory: MediaHistory? = null
    private val mHandler = InnerHandler(this)
    private var needResume = false
    var path = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actiivty_video)
        initView()
        player = IjkMediaPlayer()
        initBrightness()
    }

    private fun initBrightness() {
        brightnessController = BrightnessController()
        brightnessController.closeAutoBrightness()
    }

    override fun onBackPressed() {
        if (Config.storeMediaHistory) {
            if (mHistory == null) {
                mHistory = MediaHistory()
                mHistory!!.filePath = path
            }
            mHistory!!.time = player.currentPosition
            MediaItemDao.insertMediaHistory(mHistory!!)
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
        player.stop()
        player.release()
        brightnessController.openAutoBrightness()
    }

    private fun initView() {
        ico = findViewById(R.id.ico)
        tvTip = findViewById(R.id.tv_tip)
        llTip = findViewById(R.id.ll_tip)
        vglScreen = findViewById(R.id.vgl_screen)
        txvVideo = findViewById(R.id.txv_video)
        llControl = findViewById(R.id.ll_control)
        btPause = findViewById(R.id.bt_pause)
        sbProgress = findViewById(R.id.sb_progress)
        tvTime = findViewById(R.id.tv_time)
        tvDuration = findViewById(R.id.tv_duration)
        vglScreen.listener = object : VideoGestureLayout.VideoGestureListener {
            override fun onBrightnessChange(size: Float) {
                Log.e("hyc++oo", "onBrightnessChange++$size")
                showBrightness(size)
            }

            override fun onVolumeChange(size: Float) {
                Log.e("hyc++oo", "onVolumeChange++$size")
                showVolume(size)
            }

            override fun onSeekChange(size: Float) {
                if (mDuration == 0L) {
                    return
                }
                mSeekTime -= size * 100
                showProgressTip()
                Log.e("hyc++oo", "onSeekChange++$size")
                Log.e("hyc++oo", "onUp-----$mSeekTime")

            }

            override fun onSingleTap() {
                if (mDuration == 0L) {
                    return
                }

                if (isControlShowing) {
                    hideControl()
                    mHandler.removeMessages(MSG_HIDE)
                } else {
                    showControl()
                    mHandler.sendEmptyMessageDelayed(MSG_HIDE, 2000L)
                }
                Log.e("hyc++oo", "onSingleTap++")
            }

            override fun onDoubleTap() {
                btPause.performClick()
                Log.e("hyc++oo", "onDoubleTap++")
            }

            override fun onUp() {
                Log.e("hyc++oo", "onUp++${mSeekTime}")
                if (mSeekTime != 0F) {
                    var position = getRealTime()
                    player.seekTo(position)
                    mSeekTime = 0F
                }
                mHandler.sendEmptyMessageAtTime(MSG_HIDE_TIP, 500)

            }
        }
        btPause.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
                btPause.setBackgroundResource(R.mipmap.ico_resume_all)
            } else {
                player.start()
                btPause.setBackgroundResource(R.mipmap.ico_pause_all)
            }
        }
        mHandler.sendEmptyMessageDelayed(MSG_HIDE, 2000L)

        path = intent.getStringExtra("path")
//        path = "/sdcard/m3u8/1.mp4"
        if (TextUtils.isEmpty(path)) {
            finish()
            return
        }
        if (Config.storeMediaHistory) {
            MediaItemDao.loadMediaHistory(path, Consumer { history -> mHistory = history })
        }
        txvVideo.surfaceTextureListener = this

    }

    fun getRealTime(): Long {
        var position = (player.currentPosition + mSeekTime).toLong()
        if (position < 0) {
            position = 0L
        } else if (position > mDuration) {
            position = mDuration
        }
        return position
    }

    fun showControl() {
        isControlShowing = true
        llControl.animate().translationY(0F).setDuration(200).start()
    }

    fun hideControl() {
        isControlShowing = false
        llControl.animate().translationY(llControl.height.toFloat()).setDuration(200).start()
    }


    private fun showBrightness(size: Float) {
        ico.setBackgroundResource(R.mipmap.ico_brightness)
        var target = (brightnessController.getBrightness() + size).toInt()
        if (target >= 255) {
            target = 255
        } else if (target < 0) {
            target = 0
        }
        tvTip.text = "$target/255"
        brightnessController.changeBrightness(target)

        llTip.visibility = View.VISIBLE
        mHandler.removeMessages(MSG_HIDE_TIP)
    }

    private fun showVolume(size: Float) {
        ico.setBackgroundResource(R.mipmap.ico_volume)
        var current = AudioVolumeController.getInstance().getCurrentVolume()
        if (Math.abs(size) >= dip2px(1f)) {
            val up = size > 0
            if (up) {
                current += 1
            } else {
                current -= 1
            }
            if (current > mMaxVolume) {
                current = mMaxVolume
            } else if (current < 0) {
                current = 0
            }
        }

        tvTip.text = "$current/$mMaxVolume"
        AudioVolumeController.getInstance().setVolume(current)
        llTip.visibility = View.VISIBLE
        mHandler.removeMessages(MSG_HIDE_TIP)
    }

    @SuppressLint("SetTextI18n")
    private fun showProgressTip() {
        ico.setBackgroundResource(R.mipmap.ico_video)
        tvTip.text = "${getTimeText(getRealTime())}/$mDurationTime"
        llTip.visibility = View.VISIBLE
        mHandler.removeMessages(MSG_HIDE_TIP)
    }

    fun hideTip() {
        llTip.visibility = View.INVISIBLE
    }

    companion object {
        @JvmStatic
        fun start(path: String, context: Context) {
            val intent = Intent(context, VideoActivity::class.java)
            intent.putExtra("path", path)
            context.startActivity(intent)
        }
    }

    private class InnerHandler(activity: VideoActivity) : Handler() {
        private val mActivity: WeakReference<VideoActivity> = WeakReference(activity)
        override fun handleMessage(msg: Message?) {
            mActivity.get()?.apply {
                when (msg!!.what) {
                    MSG_HIDE -> hideControl()
                    MSG_PROGRESS -> startChangeProgress()
                    MSG_HIDE_TIP -> hideTip()
                }
            }

        }
    }
}