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
    private val ALARM_ACTION = "UDP_HEART_ACTION"
    private val TIME_INTERVAL = 1000 * 5
    private var pendingIntent: PendingIntent? = null
    private var alarmManager: AlarmManager? = null
    private val TAG = "TMSdk"
    private val count = AtomicInteger(0)

    var alarmReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "ServerHeartService receive")
            if (!AppUtil.isBackground(context)) {
                val opf = EncryptUtils.encryptSHA1ToString(StaticConfig.USERID + StaticConfig.UIDFP)
                    .lowercase(Locale.getDefault())
                TMSdk.sendUdpMessage(
                    Logs.UpdLog(
                        address = StaticConfig.HEART_HOST,
                        port = StaticConfig.HEART_PORT,
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
        }
    }

    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter(ALARM_ACTION)
        registerReceiver(alarmReceiver, intentFilter)
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        pendingIntent =
            PendingIntent.getBroadcast(this, 0, Intent(ALARM_ACTION), 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //6.0??????????????????????????????????????????????????????????????????
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                pendingIntent
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //4.4????????????????????????????????????????????????????????????
            alarmManager?.setExact(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                pendingIntent
            )
        } else { //4.4????????????????????????
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