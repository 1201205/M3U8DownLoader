package com.hyc.m3u8downloader.view

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.widget.CheckBox
import com.hyc.m3u8downloader.ForegroundService
import com.hyc.m3u8downloader.R
import com.hyc.m3u8downloader.utils.Config
import com.hyc.m3u8downloader.utils.NetStateChangeReceiver
import com.hyc.m3u8downloader.utils.NotificationUtil
import kotlinx.android.synthetic.main.actiivty_settings.view.*

class SettingActivity : AppCompatActivity() {
    private lateinit var cbWifi: CheckBox
    private lateinit var cb4G: CheckBox
    private lateinit var cbBackground: CheckBox
    private lateinit var cbForeground: CheckBox
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actiivty_settings)
        cbWifi = findViewById(R.id.cb_wifi)
        cb4G = findViewById(R.id.cb_4g)
        cbBackground = findViewById(R.id.cb_background)
        cbForeground = findViewById(R.id.cb_foreground)
        init()
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
                            startService(Intent(this@SettingActivity,ForegroundService::class.java))
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
    }
}