package com.dounine.tmsdk.model

object Wechat {

    data class LoginData(
        val app_id: String,//微信appid
        val open_id: String,//玩家open_id
        val union_id: String,//玩家union_id
        val nick_name: String,//玩家昵称
        val avatar_url: String,//玩家头像
        val gender: Int,//性别
        val last_login_time: Long,//最近登录时间（秒）
        val recharged_times: Int,//累计充值次数
        val recharged: Int,//累计充值金额（分）
        val is_new: Boolean,//是否新用户
        val login_province: String,//省份
        val login_city: String,//城市
        val login_district: String//区/县
    )

    data class PayResponse(
        val action: String,
        val orderId: String?,
        val msg: String?
    )

    data class LoginResponse(
        val err: Int,
        val data: LoginData?,
        val msg: String
    )

    data class OrderItem(
        val appid: String,
        val noncestr: String,
        val partnerid: String,
        val `package`: String,
        val prepayid: String,
        val timestamp: String,
        val sign: String,
        val order_code: String
    )

    data class OrderData(
        val item: OrderItem
    )

    data class OrderResponse(
        val err: Int,
        val data: OrderData?,
        val msg: String
    )

    data class OrderQueryData(
        val pay_status: Int,
        val order_status: Int
    )

    data class OrderQueryResponse(
        val err: Int,
        val data: OrderQueryData?,
        val msg: String
    )
}