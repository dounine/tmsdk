package com.dounine.tmsdk

import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.GsonUtils
import com.dounine.tmsdk.model.Logs
import com.dounine.tmsdk.model.Wechat
import org.junit.Test

import org.junit.Assert.*
import java.util.*

class ExampleUnitTest {
    @Test
    fun jsonSerializer() {
        val hello = Logs.Hello("lake")
        assertEquals(
            GsonUtils.toJson(hello),
            """{"name":"lake"}""".trimIndent()
        )
    }

    @Test
    fun jsonNullSerializer() {
        val hello = Logs.HelloNull("lake")
        assertEquals(
            JSON.toJSONString(hello),
            """{"name":"lake"}""".trimIndent()
        )
    }

    @Test
    fun md5Test() {
        val md5Value = EncryptUtils.encryptMD5ToString("abc")
        assertEquals("900150983cd24fb0d6963f7d28e17f72", md5Value.lowercase(Locale.getDefault()))
    }

    @Test
    fun ttJson(){
        val json = """{"err":0,"data":{"app_id":"wx1968f4cbe8ebfe5d","open_id":"oeCdT6vf30VwgQla7X_WcxxLneVc","union_id":"oj7QN1cI4TCKVcSB0cXYj6q-GoPk","nick_name":"抖音充值","avatar_url":"https://thirdwx.qlogo.cn/mmopen/vi_32/Vegkzdr6BasIicicyjic1JJNtick8qO03oGc7Go6t0Jhm4cqiaNdqNYgElPCqiaqsIQvPTv8Rm85yPA1fMiaaRWmSsqLA/132","gender":0,"city":"","province":"","country":"","language":"","gold":0,"diamond":0,"share_new":0,"share_times":0,"online_days":0,"online_duration":0,"last_login_time":1644566248,"login_times":91,"from_scene":"","from_code":"channel","create_time":1642493669,"recharged_times":0,"recharged":0,"update_time":1644566322,"is_new":false,"login_province":"广东省","login_city":"广州市","login_district":"海珠区"},"msg":"请求成功"}"""
        JSON.parseObject(json, Wechat.LoginResponse::class.java)
    }
}