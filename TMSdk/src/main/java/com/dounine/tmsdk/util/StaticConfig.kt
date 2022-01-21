package com.dounine.tmsdk.util

class StaticConfig {
    companion object {
        const val UPLOAD_URL = "https://h5game-log.kuaiyugo.com/dataAnalysis/saveUserBehaviorLogV2"
        const val IDENTIFY_QUERY_URL = ""
        const val HEART_HOST = "h5game-log.kuaiyugo.com"
        const val HEART_PORT = 5003
        const val VERSION = "1.0.0"
        const val UIDFP = "aixx04Zt"

        //可变
        var APPID: String = ""
        var programId: String = ""
        var USERID: String = ""
        var CHANNEL: String = ""
    }
}