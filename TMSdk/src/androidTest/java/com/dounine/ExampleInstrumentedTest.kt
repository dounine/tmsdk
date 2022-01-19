package com.dounine.tmsdk

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alibaba.fastjson.JSON
import com.dounine.tmsdk.core.TMSdk
import com.dounine.tmsdk.model.Identifys
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.lang.Exception

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
//    @Test
//    fun useAppContext() {
//        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("com.dounine.test", appContext.packageName)
//    }

    @Test
    fun identify() {
        val contentType: MediaType = "application/json; charset=utf-8".toMediaType()
        val okHttpClient: OkHttpClient = OkHttpClient()
        val build = Request.Builder()
            .url("https://api.kuaiyugo.com/api/oauth/v1/programs/92c7461171c211ecaba983a259950266/check_id_card")
            .post(
                """{"open_id":"abc","name":"李德松","id_card":"452525197305054416"}""".toRequestBody(contentType)
            )
        val response = okHttpClient!!.newCall(build.build()).execute()
        val responseStr = response.body!!.string()
        assertEquals("abc",responseStr)
//        JSON.parseObject(responseStr, Identifys.IdentifyResponse::class.java)
    }
}