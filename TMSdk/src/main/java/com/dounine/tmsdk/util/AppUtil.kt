package com.dounine.tmsdk.util

import android.app.ActivityManager
import android.app.ActivityManager.RunningTaskInfo
import android.content.ComponentName
import android.content.Context

class AppUtil {
    companion object {
        fun isBackground(context: Context): Boolean {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val tasks = am.getRunningTasks(1)
            if (!tasks.isEmpty()) {
                val topActivity = tasks[0].topActivity
                if (topActivity!!.packageName != context.packageName) {
                    return true
                }
            }
            return false
        }
    }
}