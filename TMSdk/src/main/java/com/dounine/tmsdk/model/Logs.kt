package com.dounine.tmsdk.model


object Logs {
    data class Ext(
        val ccode: String,
        val ak: String,
        val aid: String,
        val type: String,
        val tid: String,
        val uid: String,
        val uidfp: String,
        val gender: Int = 0,
        val scene: String = ""
    )

    data class Device(
        val model: String,
        val version: String,
        val brand: String,
        val platform: String
    )

    data class UserLog(
        val n: String,
        val v: String,
        val device: Device?,
        val ext: Ext
    )

    data class Log(
        val userLog: UserLog
    )

    data class UpdLog(
        val address: String,
        val port: Int,
        val count: Int,
        val uidfp: String
    )

    data class Hello(
        val name: String
    )

    data class HelloNull(
        val name: String,
        val age: String? = null
    )
}