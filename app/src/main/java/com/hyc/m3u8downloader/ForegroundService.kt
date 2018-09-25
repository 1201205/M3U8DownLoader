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

class ForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        var mBuilder = NotificationCompat.Builder(this);
        //设置小图标
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
        //设置大图标
        //设置标题
        mBuilder.setContentTitle("这是标题")
        //设置通知正文
        mBuilder.setContentText("这是正文，当前ID是：")
        //设置摘要
        mBuilder.setSubText("这是摘要")
        //设置是否点击消息后自动clean
        mBuilder.setAutoCancel(false)
        //显示指定文本
        mBuilder.setContentInfo("Info")
        //与setContentInfo类似，但如果设置了setContentInfo则无效果
        //用于当显示了多个相同ID的Notification时，显示消息总数
        mBuilder.setNumber(2);
        mBuilder.setChannelId("1000")
        //通知在状态栏显示时的文本
        mBuilder.setTicker("在状态栏上显示的文本")
        //设置优先级
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX)
        //自定义消息时间，以毫秒为单位，当前设置为比系统时间少一小时
        mBuilder.setWhen(System.currentTimeMillis() - 3600000)
        //设置为一个正在进行的通知，此时用户无法清除通知
        mBuilder.setOngoing(true);
        //设置消息的提醒方式，震动提醒：DEFAULT_VIBRATE     声音提醒：NotificationCompat.DEFAULT_SOUND
        //三色灯提醒NotificationCompat.DEFAULT_LIGHTS     以上三种方式一起：DEFAULT_ALL
        mBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        //设置震动方式，延迟零秒，震动一秒，延迟一秒、震动一秒

        var intent =  Intent(this, MainActivity::class.java);
        var pIntent = PendingIntent.getActivity(this, 0, intent, 0)
        mBuilder.setContentIntent(pIntent);


        var mNotificationManager =getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var channel = NotificationChannel("1000", "test", NotificationManager.IMPORTANCE_HIGH);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(channel)
        }
        mNotificationManager.notify(1000,mBuilder.build())

//        val builder = NotificationCompat.Builder(this)
//        builder.setSmallIcon(R.mipmap.ic_launcher)
//        builder.setContentTitle("Foreground")
//        builder.setContentText("I am a foreground service")
//        builder.setContentInfo("Content Info")
//        builder.setWhen(System.currentTimeMillis())
//        builder.setVisibility(Notification.VISIBILITY_PRIVATE)
//        val intent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//        builder.setContentIntent(pendingIntent)
//        val notification = builder.build()
//        startForeground(1000, notification)
        return super.onStartCommand(intent, flags, startId)
    }
}