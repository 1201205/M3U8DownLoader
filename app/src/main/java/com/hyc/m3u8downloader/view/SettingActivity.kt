package com.hyc.m3u8downloader.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import com.hyc.m3u8downloader.DownloadManager
import com.hyc.m3u8downloader.ForegroundService
import com.hyc.m3u8downloader.R
import com.hyc.m3u8downloader.utils.Config
import com.hyc.m3u8downloader.utils.NotificationUtil

class SettingActivity : AppCompatActivity() {
    private lateinit var cbWifi: CheckBox
    private lateinit var cb4G: CheckBox
    private lateinit var cbBackground: CheckBox
    private lateinit var cbForeground: CheckBox
    private lateinit var sbThread: SeekBar
    private lateinit var sbFile: SeekBar
    private lateinit var tvThread: TextView
    private lateinit var tvFile: TextView
    private val fileCount = Config.maxFile
    private val threadCount = Config.maxThread
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actiivty_settings)
        cbWifi = findViewById(R.id.cb_wifi)
        cb4G = findViewById(R.id.cb_4g)
        cbBackground = findViewById(R.id.cb_background)
        cbForeground = findViewById(R.id.cb_foreground)
        sbThread = findViewById(R.id.sb_thread)
        sbFile = findViewById(R.id.sb_file)
        tvThread = findViewById(R.id.tv_thread)
        tvFile = findViewById(R.id.tv_file)
        init()
    }

    override fun onBackPressed() {
        val fileChange = Config.maxFile - fileCount
        if (fileChange != 0) {
            DownloadManager.getInstance().onFileCountChanged(fileChange)
        }
        val threadChange = Config.maxThread - threadCount
        if (threadChange != 0) {
            DownloadManager.getInstance().onThreadCountChanged(threadChange)
        }
        super.onBackPressed()
    }

    private fun init() {
        cb4G.isChecked = Config.dataWork
        cb4G.setOnClickListener {
            if (!cb4G.isChecked) {
                cb4G.isChecked = false
                Config.dataWork = false
            } else {
                show4GDialog(this@SettingActivity, object : PositiveClickListener {
                    override fun onPositiveClicked() {
                        cb4G.isChecked = true
                        Config.dataWork = true
                    }
                }, object : NegativeClickListener {
                    override fun onNegativeClicked() {
                        cb4G.isChecked = false
                        Config.dataWork = false
                    }
                })
            }
        }
        cbBackground.isChecked = Config.backgroundWork
        cbBackground.setOnClickListener {
            cbBackground.isChecked = cbBackground.isChecked
            Config.backgroundWork = cbBackground.isChecked
        }
        cbForeground.isChecked = Config.foregroundWork
        cbForeground.setOnClickListener {
            if (!cbForeground.isChecked) {
                cbForeground.isChecked = false
                Config.foregroundWork = false
            } else {
                showForegroundDialog(SettingActivity@ this, object : PositiveClickListener {
                    override fun onPositiveClicked() {
                        cbForeground.isChecked = true
                        Config.foregroundWork = true
                        if (NotificationUtil.checkNotifyPermissionAndJump(this@SettingActivity)) {
                            startService(Intent(this@SettingActivity, ForegroundService::class.java))
                        }

                    }
                }, object : NegativeClickListener {
                    override fun onNegativeClicked() {
                        cbForeground.isChecked = false
                        Config.foregroundWork = false
                    }
                })
            }
        }
        cbWifi.isChecked = Config.autoWork
        cbWifi.setOnClickListener {
            cbWifi.isChecked = cbWifi.isChecked
            Config.autoWork = cbWifi.isChecked
        }
        tvThread.text = "文件最大下载线程数量：${Config.maxThread}"
        sbThread.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Config.maxThread = progress + 1
                tvThread.text = "文件最大下载线程数量：${progress + 1}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        sbThread.progress = Config.maxThread - 1

        tvFile.text = "最大同时下载文件数量：${Config.maxFile}"
        sbFile.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Config.maxFile = progress + 1
                tvFile.text = "最大同时下载文件数量：${progress + 1}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        sbFile.progress = Config.maxFile - 1
    }
}