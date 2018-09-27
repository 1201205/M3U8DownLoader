package com.hyc.m3u8downloader

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log

class ForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("hyc-service", "onCreate---")
    }

    override fun onDestroy() {
        super.onDestroy()
        mNotificationManager!!.cancel(1000)
        Log.e("hyc-service", "onDestroy---")
    }

    var mNotificationManager: NotificationManager? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (mNotificationManager!=null) {
            return super.onStartCommand(intent, flags, startId)
        }
        Log.e("hyc-service", "onStartCommand---$flags---$startId")
//        for (item in intent!!.extras.keySet()) {
//            Log.e("hyc-service", "onStartCommand---${item}---${intent.getStringExtra(item)}")
//        }
        var mBuilder = NotificationCompat.Builder(this)
        //设置小图标
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
        //设置大图标
        //设置标题
        mBuilder.setContentTitle("M3U8下载器")
        //设置通知正文
        mBuilder.setContentText("正在下载中")
        //设置摘要
//        mBuilder.setSubText("这是摘要")
        //设置是否点击消息后自动clean
        mBuilder.setAutoCancel(false)
        //显示指定文本
//        mBuilder.setContentInfo("Info")
        //与setContentInfo类似，但如果设置了setContentInfo则无效果
        //用于当显示了多个相同ID的Notification时，显示消息总数
        mBuilder.setNumber(2)
        mBuilder.setChannelId("1000")
        //通知在状态栏显示时的文本
        mBuilder.setTicker("在状态栏上显示的文本")
        //设置优先级
        mBuilder.priority = NotificationCompat.PRIORITY_MAX
        //自定义消息时间，以毫秒为单位，当前设置为比系统时间少一小时
        mBuilder.setWhen(System.currentTimeMillis() - 3600000)
        //设置为一个正在进行的通知，此时用户无法清除通知
        mBuilder.setOngoing(true);
        //设置消息的提醒方式，震动提醒：DEFAULT_VIBRATE     声音提醒：NotificationCompat.DEFAULT_SOUND
        //三色灯提醒NotificationCompat.DEFAULT_LIGHTS     以上三种方式一起：DEFAULT_ALL
        mBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
        //设置震动方式，延迟零秒，震动一秒，延迟一秒、震动一秒

        var intent = Intent(this, MainActivity::class.java)
        var pIntent = PendingIntent.getActivity(this, 0, intent, 0)
        mBuilder.setContentIntent(pIntent)


        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel = NotificationChannel("1000", "下载", NotificationManager.IMPORTANCE_HIGH)
            if (mNotificationManager != null) {
                mNotificationManager!!.createNotificationChannel(channel)
            }
        }
        mNotificationManager!!.notify(1000, mBuilder.build())
        return super.onStartCommand(intent, flags, startId)
    }

    object Helper {
        @JvmStatic
        fun startService() {
            val context = MainApplication.instance
            context.startService(Intent(context, ForegroundService::class.java))
        }

        @JvmStatic
        fun stopService() {
            val context = MainApplication.instance
            context.stopService(Intent(context, ForegroundService::class.java))
        }
    }
}