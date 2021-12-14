package com.dounine.tmsdk.core

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.EncryptUtils
import com.dounine.tmsdk.model.Logs
import com.dounine.tmsdk.util.DeviceUtil
import com.dounine.tmsdk.util.StaticConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*

class TMSdk {

    class HandlerThread : Thread() {
        var handler: Handler? = null
        val okHttpClient: OkHttpClient = OkHttpClient()
        override fun run() {
            Looper.prepare()
            handler = object : Handler(Looper.myLooper()!!) {
                override fun handleMessage(msg: Message) {
                    if (msg.obj is Logs.UpdLog) {
                        Log.i(TAG, "udp message")
                        val uidLog = msg.obj as Logs.UpdLog
                        val data =
                            """${StaticConfig.APPID}:${StaticConfig.USERID}:${uidLog.uidfp}:${uidLog.count}""".trimIndent()
                                .encodeToByteArray()
                        val packet = DatagramPacket(
                            data,
                            data.size,
                            InetAddress.getByName(uidLog.address),
                            uidLog.port
                        )
                        val socket = DatagramSocket()
                        socket.send(packet)
                    } else if (msg.obj is Logs.Log) {
                        val log = msg.obj as Logs.Log
                        val jsonStr = JSON.toJSONString(log)
                        Log.i(TAG, "handleMessage: ${jsonStr}")
                        val build = Request.Builder()
                            .url(StaticConfig.UPLOAD_URL)
                            .post(
                                jsonStr.toRequestBody(
                                    "application/json;charset=utf-8".toMediaType()
                                )
                            )
                        okHttpClient!!.newCall(build.build()).enqueue(object : okhttp3.Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.e(TAG, """"onFailure ${e.message}""")
                            }

                            @Throws(IOException::class)
                            override fun onResponse(call: Call, response: Response) {
                                Log.i(TAG, """onResponse -> ${response.body!!.string()} """)
                            }
                        })
                    }
                }
            }
            Looper.loop()
        }

    }

    companion object {
        private var appid: String? = null
        private var channel: String? = null
        private var userId: String? = null
        private const val TAG: String = "TMSdk"
        private var udpIntent: Intent? = null
        private var handlerThread: HandlerThread? = null

        fun userId(userId: String) {
            this.userId = userId
        }

        fun sendUdpMessage(udpLog: Logs.UpdLog) {
            val message = Message()
            message.obj = udpLog
            handlerThread?.handler?.sendMessage(message)
        }

        /**
         * 初始化
         */
        fun init(context: Context, appid: String, channel: String) {
            this.appid = appid
            this.channel = channel

            if (this.userId == null) {
                this.userId = DeviceUtil.getDeviceId(context)
            }
            Log.i(TAG, "init: " + this.userId)
            this.userId = EncryptUtils.encryptMD5ToString(
                this.appid + this.userId
            ).lowercase(Locale.getDefault())

            StaticConfig.APPID = this.appid!!
            StaticConfig.CHANNEL = this.channel!!
            StaticConfig.USERID = this.userId!!

            Log.i(TAG, "init:")
            Log.i(TAG, "appid -> " + this.appid)
            Log.i(TAG, "channel -> " + this.channel)
            Log.i(TAG, "userId -> " + this.userId)

            if (null == udpIntent) {
                udpIntent = Intent(context, HeartService::class.java)
                context.startService(udpIntent)
            }

            if (null == handlerThread) {
                handlerThread = HandlerThread()
                handlerThread!!.start()
            }
        }

        fun appStart() {
            val message = Message()
            message.obj = Logs.Log(
                userLog = Logs.UserLog(
                    n = "a_tm_sdk",
                    v = StaticConfig.VERSION,
                    ext = Logs.Ext(
                        ccode = this.channel!!,
                        ak = this.appid!!,
                        type = "login_in",
                        tid = this.userId!!,
                        uid = this.userId!!,
                        uidfp = EncryptUtils.encryptSHA1ToString(this.userId!! + StaticConfig.UIDFP)
                            .lowercase(Locale.getDefault()),
                        gender = 0,
                        scene = ""
                    ),
                    device = Logs.Device(
                        model = DeviceUtils.getModel(),
                        version = DeviceUtils.getSDKVersionCode().toString(),
                        brand = DeviceUtils.getManufacturer(),
                        platform = "android"
                    )
                )
            )
            handlerThread?.handler?.sendMessage(message)
        }

        fun appExit(context: Context) {
            Log.i(TAG, "appExit")
            if (null != udpIntent) {
                context.stopService(udpIntent)
            }
            handlerThread?.interrupt()
        }

        fun appShow() {
            val message = Message()
            message.obj = Logs.Log(
                userLog = Logs.UserLog(
                    n = "a_tm_sdk",
                    v = StaticConfig.VERSION,
                    ext = Logs.Ext(
                        ccode = this.channel!!,
                        ak = this.appid!!,
                        type = "login_in",
                        tid = this.userId!!,
                        uid = this.userId!!,
                        uidfp = EncryptUtils.encryptSHA1ToString(this.userId!! + StaticConfig.UIDFP)
                            .lowercase(Locale.getDefault()),
                        gender = 0,
                        scene = ""
                    ),
                    device = null
                )
            )
            handlerThread?.handler?.sendMessage(message)
        }

        fun appHide() {
            val message = Message()
            message.obj = Logs.Log(
                userLog = Logs.UserLog(
                    n = "a_tm_sdk",
                    v = StaticConfig.VERSION,
                    ext = Logs.Ext(
                        ccode = this.channel!!,
                        ak = this.appid!!,
                        type = "login_out",
                        tid = this.userId!!,
                        uid = this.userId!!,
                        uidfp = EncryptUtils.encryptSHA1ToString(this.userId!! + StaticConfig.UIDFP)
                            .lowercase(Locale.getDefault()),
                        gender = 0,
                        scene = ""
                    ),
                    device = null
                )
            )
            handlerThread?.handler?.sendMessage(message)
        }
    }
}