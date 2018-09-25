package com.hyc.m3u8downloader.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.NotificationManagerCompat
import android.widget.Toast

object NotificationUtil {
    @JvmStatic
    fun checkNotifyPermissionAndJump(context: Context):Boolean {
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return true
        }
        Toast.makeText(context, "未设置通知栏权限，请手动添加", Toast.LENGTH_LONG).show()
        var appIntent = context.packageManager.getLaunchIntentForPackage("com.iqoo.secure")
        if (appIntent != null) {
            context.startActivity(appIntent)
            return false
        }
        appIntent = context.packageManager.getLaunchIntentForPackage("com.oppo.safe")
        if (appIntent != null) {
            context.startActivity(appIntent)
            return false
        }
        appIntent = Intent()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                appIntent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                appIntent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                appIntent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                appIntent.putExtra("app_package", context.packageName)
                appIntent.putExtra("app_uid", context.applicationInfo.uid)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD -> {
                appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appIntent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                appIntent.data = Uri.fromParts("package", context.packageName, null)
            }
            else -> {
                appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appIntent.action = Intent.ACTION_VIEW
                appIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails")
                appIntent.putExtra("com.android.settings.ApplicationPkgName", context.packageName)
            }
        }
        context.startActivity(appIntent)
        return false
    }
}