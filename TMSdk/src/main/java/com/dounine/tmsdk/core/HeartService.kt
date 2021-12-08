package com.dounine.tmsdk.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import com.blankj.utilcode.util.EncryptUtils
import com.dounine.tmsdk.model.Logs
import com.dounine.tmsdk.util.AppUtil
import com.dounine.tmsdk.util.StaticConfig
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class HeartService : Service() {
    val ALARM_ACTION = "UDP_HEART_ACTION"
    private val TIME_INTERVAL = 1000 * 5
    private var pendingIntent: PendingIntent? = null
    private var alarmManager: AlarmManager? = null
    private val TAG = "TMSdk"
    private val count = AtomicInteger(0)

    var alarmReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i("TMSdk", "ServerHeartService receive")

            if (!AppUtil.isBackground(context)) {
                val port = 8080
                val opf = EncryptUtils.encryptSHA1ToString(StaticConfig.USERID + StaticConfig.UIDFP)
                    .lowercase(Locale.getDefault())
                TMSdk.sendUdpMessage(
                    Logs.UpdLog(
                        address = "localhost",
                        port = port,
                        count = count.getAndIncrement() % 15,
                        uidfp = opf
                    )
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager!!.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + TIME_INTERVAL,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager!!.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + TIME_INTERVAL,
                    pendingIntent
                )
            }
            Log.i(TAG, "app是否在后台:" + AppUtil.isBackground(context))
        }
    }

    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter(ALARM_ACTION)
        registerReceiver(alarmReceiver, intentFilter)
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        pendingIntent =
            PendingIntent.getBroadcast(this, 0, Intent(ALARM_ACTION), 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //6.0低电耗模式需要使用此方法才能准时触发定时任务
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                pendingIntent
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //4.4以上，使用此方法触发定时任务时间更为精准
            alarmManager?.setExact(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                pendingIntent
            )
        } else { //4.4以下，使用旧方法
            alarmManager?.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                TIME_INTERVAL.toLong(),
                pendingIntent
            )
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}