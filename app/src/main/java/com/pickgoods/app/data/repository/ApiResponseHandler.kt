package com.pickgoods.app.data.repository

import retrofit2.Response

internal fun <T> Response<T>.toGoodsResult(emptyMessage: String = "数据解析失败"): GoodsResult<T> {
    return if (isSuccessful) {
        val body = body()
        if (body != null) {
            GoodsResult.Success(body)
        } else {
            GoodsResult.Error(emptyMessage, code())
        }
    } else {
        val rawBody = errorBody()?.string()
        GoodsResult.Error(rawBody?.take(200) ?: "请求失败", code(), rawBody)
    }
}

internal fun Response<Unit>.toUnitGoodsResult(): GoodsResult<Unit> {
    return if (isSuccessful) {
        GoodsResult.Success(Unit)
    } else {
        val rawBody = errorBody()?.string()
        GoodsResult.Error(rawBody?.take(200) ?: "请求失败", code(), rawBody)
    }
}
