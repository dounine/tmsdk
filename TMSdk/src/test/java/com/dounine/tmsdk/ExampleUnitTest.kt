package com.dounine.tmsdk

import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.GsonUtils
import com.dounine.tmsdk.model.Logs
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
}