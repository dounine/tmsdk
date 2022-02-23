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
import com.dounine.tmsdk.model.Identifys
import com.dounine.tmsdk.model.Logs
import com.dounine.tmsdk.model.Wechat
import com.dounine.tmsdk.util.DeviceUtil
import com.dounine.tmsdk.util.StaticConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.lang.Exception
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*

class TMSdk {

    class HandlerThread : Thread() {
        var handler: Handler? = null
        val okHttpClient: OkHttpClient = OkHttpClient()
        val contentType: MediaType = "application/json; charset=utf-8".toMediaType()
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
                                    contentType
                                )
                            )
                        okHttpClient.newCall(build.build()).enqueue(object : okhttp3.Callback {
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
        private var gender: Int = 0
        private var programId: String? = null
        private const val TAG: String = "TMSdk"
        private var udpIntent: Intent? = null
        private var handlerThread: HandlerThread? = null
        private var start: Boolean = false
        private val contentType: MediaType = "application/json; charset=utf-8".toMediaType()

        fun getUserId(): String {
            return this.userId!!
        }

        fun setGender(gender: Int) {
            this.gender = gender
        }

        fun createUserId(context: Context): String {
            return DeviceUtil.getDeviceId(context)!!
        }

        fun md5(value: String): String {
            return EncryptUtils.encryptMD5ToString(
                value
            ).lowercase(Locale.getDefault())
        }

        fun sendUdpMessage(udpLog: Logs.UpdLog) {
            val message = Message()
            message.obj = udpLog
            handlerThread?.handler?.sendMessage(message)
        }

        /**
         * 初始化
         */
        fun init(
            context: Context,
            appid: String,
            programId: String,
            userId: String,
            channel: String
        ) {
            this.appid = appid
            this.programId = programId
            this.channel = channel

            if (userId.length > 64) {
                throw Exception(TAG + ":用户id长度不能超出64位")
            }

            this.userId = userId

            Log.i(TAG, "init: " + this.userId)
            StaticConfig.APPID = this.appid!!
            StaticConfig.CHANNEL = this.channel!!
            StaticConfig.USERID = this.userId!!

            Log.i(TAG, "init:")
            Log.i(TAG, "appid -> " + this.appid)
            Log.i(TAG, "programId -> " + this.programId)
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
            if (!this.start) {
                this.userId.let {
                    this.start = true
                    val message = Message()
                    message.obj = Logs.Log(
                        userLog = Logs.UserLog(
                            n = "a_tm_sdk",
                            v = StaticConfig.VERSION,
                            ext = Logs.Ext(
                                ccode = this.channel!!,
                                ak = this.programId!!,
                                aid = this.appid!!,
                                type = "login_in",
                                tid = it!!,
                                uid = it!!,
                                uidfp = EncryptUtils.encryptSHA1ToString(it!! + StaticConfig.UIDFP)
                                    .lowercase(Locale.getDefault()),
                                gender = this.gender,
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
                    identifyReport(programId!!, userId!!, true)
                }
            }
        }

        fun appExit(context: Context) {
            if (this.start) {
                this.userId.let {
                    Log.i(TAG, "appExit")
                    if (null != udpIntent) {
                        context.stopService(udpIntent)
                    }
                    handlerThread?.interrupt()
                }
                identifyReport(programId!!, userId!!, false)
            }
        }

//        fun appShow() {
//            this.userId.let {
//                val message = Message()
//                message.obj = Logs.Log(
//                    userLog = Logs.UserLog(
//                        n = "a_tm_sdk",
//                        v = StaticConfig.VERSION,
//                        ext = Logs.Ext(
//                            ccode = this.channel!!,
//                            ak = this.appid!!,
//                            type = "login_in",
//                            tid = it!!,
//                            uid = it!!,
//                            uidfp = EncryptUtils.encryptSHA1ToString(it!! + StaticConfig.UIDFP)
//                                .lowercase(Locale.getDefault()),
//                            gender = 0,
//                            scene = ""
//                        ),
//                        device = null
//                    )
//                )
//                handlerThread?.handler?.sendMessage(message)
//            }
//        }
//
//        fun appHide() {
//            this.userId.let {
//                val message = Message()
//                message.obj = Logs.Log(
//                    userLog = Logs.UserLog(
//                        n = "a_tm_sdk",
//                        v = StaticConfig.VERSION,
//                        ext = Logs.Ext(
//                            ccode = this.channel!!,
//                            ak = this.appid!!,
//                            type = "login_out",
//                            tid = it!!,
//                            uid = it!!,
//                            uidfp = EncryptUtils.encryptSHA1ToString(it!! + StaticConfig.UIDFP)
//                                .lowercase(Locale.getDefault()),
//                            gender = 0,
//                            scene = ""
//                        ),
//                        device = null
//                    )
//                )
//                handlerThread?.handler?.sendMessage(message)
//            }
//        }

        fun wechatUserInfo(programId: String, wx_code: String): Wechat.LoginResponse? {
            val okHttpClient: OkHttpClient = OkHttpClient()
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val build = Request.Builder()
                .url("https://api.kuaiyugo.com/api/platuser/v1/programs/${programId}/app_sessions")
                .post(
                    """{"wx_code":"$wx_code"}""".toRequestBody(contentType)
                ).addHeader(
                    "version", StaticConfig.VERSION
                )
                .addHeader(
                    "device", DeviceUtils.getModel()
                )
                .addHeader(
                    "timestamp", timestamp
                )
                .addHeader(
                    "sign", EncryptUtils.encryptMD5ToString(programId + timestamp)
                        .lowercase(Locale.getDefault())
                )
            try {
                val response = okHttpClient.newCall(build.build()).execute()
                val responseStr = response.body!!.string()
                Log.i(TAG, "微信用户信息查询结果 " + responseStr)
                return JSON.parseObject(responseStr, Wechat.LoginResponse::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "微信用户信息查询失败")
                return Wechat.LoginResponse(
                    err = -1,
                    data = null,
                    msg = e.message!!
                )
            }
        }

        /**
         * 实名认证
         * name 姓名
         * id 身份证号
         * {
         * "err": 0,     //0成功，非0请求异常
         * "data": {
         * "result": true  //true认证成功，false需要认证
         * },
         * "msg": "请求成功"
         * }
         */
        fun identify(
            programId: String,
            userId: String,
            name: String,
            id: String
        ): Identifys.IdentifyResponse {
            val okHttpClient: OkHttpClient = OkHttpClient()
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val build = Request.Builder()
                .url("https://api.kuaiyugo.com/api/oauth/v1/programs/${programId}/check_id_card")
                .post(
                    """{"open_id":"${userId}","name":"${name}","id_card":"${id}"}""".toRequestBody(
                        contentType
                    )
                )
                .addHeader(
                    "timestamp", timestamp
                )
                .addHeader(
                    "sign", EncryptUtils.encryptMD5ToString(programId + timestamp)
                        .lowercase(Locale.getDefault())
                )
            try {
                val response = okHttpClient.newCall(build.build()).execute()
                val responseStr = response.body!!.string()
                Log.i(TAG, "实名认证结果 " + responseStr)
                return JSON.parseObject(responseStr, Identifys.IdentifyResponse::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "实名认证失败")
                return Identifys.IdentifyResponse(
                    err = -1,
                    data = Identifys.IdentifyData(
                        result = false
                    ),
                    msg = e.message!!
                )
            }
        }

        /**
         * 用户认证查询
         */
        fun identifyQuery(programId: String, userId: String): Identifys.QueryResponse {
            val okHttpClient: OkHttpClient = OkHttpClient()
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val build = Request.Builder()
                .url("https://api.kuaiyugo.com/api/oauth/v1/programs/${programId}/query_id_card?open_id=${userId}")
                .get()
                .addHeader(
                    "timestamp", timestamp
                )
                .addHeader(
                    "sign", EncryptUtils.encryptMD5ToString(programId + timestamp)
                        .lowercase(Locale.getDefault())
                )
            try {
                val response = okHttpClient.newCall(build.build()).execute()
                val responseStr = response.body!!.string()
                Log.i(TAG, "用户认证信息查询结果 " + responseStr)
                return JSON.parseObject(responseStr, Identifys.QueryResponse::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "用户认证信息查询失败")
                return Identifys.QueryResponse(
                    err = -1,
                    data = Identifys.QueryData(
                        result = false
                    ),
                    msg = e.message!!
                )
            }
        }

        /**
         * 用户上下线上报
         */
        fun identifyReport(
            programId: String,
            userId: String,
            online: Boolean
        ): Identifys.ReportResponse {
            val okHttpClient: OkHttpClient = OkHttpClient()
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val onlineInt: Int = if (online) 1 else 0
            val build = Request.Builder()
                .url("https://api.kuaiyugo.com/api/oauth/v1/programs/${programId}/report_user_behavior?open_id=${userId}&behavior=${onlineInt}")
                .get()
                .addHeader(
                    "timestamp", timestamp
                )
                .addHeader(
                    "sign", EncryptUtils.encryptMD5ToString(programId + timestamp)
                        .lowercase(Locale.getDefault())
                )
            try {
                val response = okHttpClient.newCall(build.build()).execute()
                val responseStr = response.body!!.string()
                if (online) {
                    Log.i(TAG, "用户上线上报 " + responseStr)
                } else {
                    Log.i(TAG, "用户下线上报 " + responseStr)
                }
                return JSON.parseObject(responseStr, Identifys.ReportResponse::class.java)
            } catch (e: Exception) {
                Log.i(TAG, "用户上下线上报失败")
                return Identifys.ReportResponse(
                    err = -1,
                    msg = e.message!!
                )
            }
        }

        /**
         * 定单查询
         * orderId 定单号
         */
        fun orderQuery(programId: String, orderId: String): Wechat.OrderQueryResponse {
            val okHttpClient: OkHttpClient = OkHttpClient()
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val build = Request.Builder()
                .url("https://api.kuaiyugo.com/api/payment/v1/programs/${programId}/order_status?order_code=${orderId}")
                .get()
                .addHeader(
                    "timestamp", timestamp
                )
                .addHeader(
                    "sign", EncryptUtils.encryptMD5ToString(programId + timestamp)
                        .lowercase(Locale.getDefault())
                )
            try {
                val response = okHttpClient.newCall(build.build()).execute()
                val responseStr = response.body!!.string()
                Log.i(TAG, "定单查询结果 " + responseStr)
                return JSON.parseObject(responseStr, Wechat.OrderQueryResponse::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "定单查询失败")
                return Wechat.OrderQueryResponse(
                    err = -1,
                    data = null,
                    msg = e.message!!
                )
            }
        }

        /**
         * 微信创建支付
         * coin 金额（角）
         * userId 用户id
         * programParam 自定义参数
         */
        fun weixinPayCreateOrder(
            programId: String,
            coin: Int,
            userId: String,
            programParam: String,
            deviceBrand: String,
            deviceModel: String,
            goodsName: String,
            zone: String,
            gameNickname: String,
            gameUid: String
        ): Wechat.OrderResponse {
            val okHttpClient: OkHttpClient = OkHttpClient()
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val build = Request.Builder()
                .url("https://api.kuaiyugo.com/api/payment/v1/programs/${programId}/app_wx_orders")
                .post(
                    """{"coin":$coin,"open_id":"${userId}","program_param":"${programParam}","device_brand":"${deviceBrand}","device_model":"${deviceModel}","goods_name":"${goodsName}","zone":"${zone}","game_uid":"${gameUid}","game_nickname":"${gameNickname}"}""".toRequestBody(
                        contentType
                    )
                )
                .addHeader(
                    "timestamp", timestamp
                )
                .addHeader(
                    "sign", EncryptUtils.encryptMD5ToString(programId + timestamp)
                        .lowercase(Locale.getDefault())
                )
            try {
                val response = okHttpClient.newCall(build.build()).execute()
                val responseStr = response.body!!.string()
                Log.i(TAG, "微信支付创建结果 " + responseStr)
                return JSON.parseObject(responseStr, Wechat.OrderResponse::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "定单创建失败")
                return Wechat.OrderResponse(
                    err = -1,
                    data = null,
                    msg = e.message!!
                )
            }
        }

        fun objectToJson(obj: Object): String {
            return JSON.toJSONString(obj)
        }
    }
}