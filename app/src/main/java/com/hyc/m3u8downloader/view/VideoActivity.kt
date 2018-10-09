package com.hyc.m3u8downloader.view

import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.Surface
import android.view.TextureView
import com.hyc.m3u8downloader.R
import com.hyc.m3u8downloader.VideoGestureLayout
import tv.danmaku.ijk.media.player.IjkMediaPlayer

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

    private fun initPlayer(surfaceTexture: SurfaceTexture?) {
        val surface = Surface(surfaceTexture)
        player.setSurface(surface)
        player.dataSource = path
        player.prepareAsync()
        player.isLooping = false
        player.setOnPreparedListener { _ -> player.start() }
    }

    lateinit var txvVideo: TextureView
    lateinit var vglScreen: VideoGestureLayout
    lateinit var player: IjkMediaPlayer
    var path = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actiivty_video)
        vglScreen = findViewById(R.id.vgl_screen)
        txvVideo = findViewById(R.id.txv_video)
        txvVideo.surfaceTextureListener = this
        player = IjkMediaPlayer()

        initView()
    }

    private fun initView() {
        vglScreen.listener = object : VideoGestureLayout.VideoGestureListener {
            override fun onBrightnessChange(size: Float) {
                Log.e("hyc++oo", "onBrightnessChange++$size")
            }

            override fun onVolumeChange(size: Float) {
                Log.e("hyc++oo", "onVolumeChange++$size")
            }

            override fun onSeekChange(size: Float) {
                Log.e("hyc++oo", "onSeekChange++$size")
            }

            override fun onSingleTap() {
                Log.e("hyc++oo", "onSingleTap++")

            }

            override fun onDoubleTap() {
                Log.e("hyc++oo", "onDoubleTap++")
            }

            override fun onUp() {
                Log.e("hyc++oo", "onUp++")

            }
        }
//        path = intent.getStringExtra("path")
        path = "/sdcard/m3u8/1.mp4"
        if (TextUtils.isEmpty(path)) {
            finish()
            return
        }
    }

    companion object {
        @JvmStatic
        fun start(path: String, context: Context) {
            val intent = Intent(context, VideoActivity::class.java)
            intent.putExtra("path", path)
            context.startActivity(intent)
        }
    }
}