package com.dounine.tmsdk.model

object Identifys {

    data class QueryData(
        val result: Boolean
    )

    data class QueryResponse(
        val err: Int,
        val data: QueryData,
        val msg: String
    )

    data class ReportResponse(
        val err: Int,
        val msg: String
    )

    data class IdentifyData(
        val result: Boolean
    )

    data class IdentifyResponse(
        val err: Int,
        val data: IdentifyData,
        val msg: String
    )
}