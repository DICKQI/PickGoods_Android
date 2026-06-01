package com.pickgoods.app.data.repository

import android.util.Log
import com.pickgoods.app.data.api.LocationApi
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.data.model.PaginatedResponse
import com.pickgoods.app.data.model.StorageNode
import com.pickgoods.app.data.model.StorageNodeRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationApi: LocationApi
) {
    suspend fun getNodes(): GoodsResult<List<StorageNode>> = safeCall {
        locationApi.getLocationNodes().toGoodsResult()
    }

    suspend fun createNode(request: StorageNodeRequest): GoodsResult<StorageNode> = safeCall {
        locationApi.createLocationNode(request).toGoodsResult()
    }

    suspend fun patchNode(id: Int, request: StorageNodeRequest): GoodsResult<StorageNode> = safeCall {
        locationApi.patchLocationNode(id, request).toGoodsResult()
    }

    suspend fun deleteNode(id: Int): GoodsResult<Unit> = safeCall {
        locationApi.deleteLocationNode(id).toUnitGoodsResult()
    }

    suspend fun getNodeGoods(
        id: Int,
        includeChildren: Boolean,
        page: Int = 1
    ): GoodsResult<PaginatedResponse<GoodsListItem>> = safeCall {
        locationApi.getLocationNodeGoods(id, includeChildren, page).toGoodsResult()
    }

    private suspend inline fun <T> safeCall(block: suspend () -> GoodsResult<T>): GoodsResult<T> {
        return try {
            block()
        } catch (e: Exception) {
            Log.e("LocationRepository", "API request failed", e)
            GoodsResult.Error("请求失败: ${e.message ?: "未知错误"}")
        }
    }
}
