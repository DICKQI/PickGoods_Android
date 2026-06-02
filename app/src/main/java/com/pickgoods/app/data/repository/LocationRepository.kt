package com.pickgoods.app.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pickgoods.app.data.api.LocationApi
import com.pickgoods.app.data.local.CacheDao
import com.pickgoods.app.data.local.CacheEntry
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.data.model.PaginatedResponse
import com.pickgoods.app.data.model.StorageNode
import com.pickgoods.app.data.model.StorageNodeRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationApi: LocationApi,
    private val cacheDao: CacheDao
) {
    private val gson = Gson()

    suspend fun getNodes(): GoodsResult<List<StorageNode>> = cachedNodeList("location:nodes") {
        locationApi.getLocationNodes()
    }

    suspend fun getTree(): GoodsResult<List<StorageNode>> = cachedNodeList("location:tree") {
        locationApi.getLocationTree()
    }

    suspend fun getNode(id: Int): GoodsResult<StorageNode> = safeCall {
        locationApi.getLocationNode(id).toGoodsResult()
    }

    suspend fun createNode(request: StorageNodeRequest): GoodsResult<StorageNode> = safeCall {
        locationApi.createLocationNode(request).toGoodsResult()
    }

    suspend fun createNodeWithImage(
        request: StorageNodeRequest,
        imageFile: File
    ): GoodsResult<StorageNode> = safeCall {
        locationApi.createLocationNodeWithImage(
            name = request.name.toPlainTextBody(),
            parent = request.parent.toNullablePlainTextBody(),
            description = request.description.toNullablePlainTextBody(),
            order = request.order.toNullablePlainTextBody(),
            image = imageFile.toImagePart("image")
        ).toGoodsResult("位置图片上传失败")
    }

    suspend fun patchNode(id: Int, request: StorageNodeRequest): GoodsResult<StorageNode> = safeCall {
        locationApi.patchLocationNode(id, request).toGoodsResult()
    }

    suspend fun updateNode(id: Int, request: StorageNodeRequest): GoodsResult<StorageNode> = safeCall {
        locationApi.updateLocationNode(id, request).toGoodsResult()
    }

    suspend fun patchNodeWithImage(
        id: Int,
        request: StorageNodeRequest,
        imageFile: File
    ): GoodsResult<StorageNode> = safeCall {
        locationApi.patchLocationNodeWithImage(
            id = id,
            name = request.name.toPlainTextBody(),
            parent = request.parent.toNullablePlainTextBody(),
            description = request.description.toNullablePlainTextBody(),
            order = request.order.toNullablePlainTextBody(),
            image = imageFile.toImagePart("image")
        ).toGoodsResult("位置图片上传失败")
    }

    suspend fun deleteNode(id: Int): GoodsResult<Unit> = safeCall {
        locationApi.deleteLocationNode(id).toUnitGoodsResult()
    }

    suspend fun getNodeGoods(
        id: Int,
        includeChildren: Boolean,
        page: Int = 1
    ): GoodsResult<PaginatedResponse<GoodsListItem>> = safeCall {
        when (val result = locationApi.getLocationNodeGoods(id, includeChildren, page).toGoodsResult()) {
            is GoodsResult.Success -> GoodsResult.Success(
                PaginatedResponse(
                    count = result.data.size,
                    page = 1,
                    pageSize = result.data.size,
                    results = result.data
                )
            )
            is GoodsResult.Error -> result
        }
    }

    private suspend inline fun <T> safeCall(block: suspend () -> GoodsResult<T>): GoodsResult<T> {
        return try {
            block()
        } catch (e: Exception) {
            Log.e("LocationRepository", "API request failed", e)
            GoodsResult.Error("请求失败: ${e.message ?: "未知错误"}")
        }
    }

    private suspend inline fun <reified T> cachedNodeList(
        key: String,
        crossinline networkCall: suspend () -> retrofit2.Response<List<T>>
    ): GoodsResult<List<T>> {
        return try {
            when (val result = networkCall().toGoodsResult()) {
                is GoodsResult.Success -> {
                    cacheDao.upsert(CacheEntry(key, gson.toJson(result.data)))
                    result
                }
                is GoodsResult.Error -> cachedListFallback<T>(key) ?: result
            }
        } catch (e: Exception) {
            Log.e("LocationRepository", "cached node list request failed", e)
            cachedListFallback<T>(key)
                ?: GoodsResult.Error("请求失败: ${e.message ?: "未知错误"}")
        }
    }

    private suspend inline fun <reified T> cachedListFallback(key: String): GoodsResult<List<T>>? {
        val entry = cacheDao.get(key) ?: return null
        return runCatching {
            val type = object : TypeToken<List<T>>() {}.type
            val list: List<T> = gson.fromJson(entry.payload, type)
            GoodsResult.Success(list)
        }.getOrNull()
    }

    private fun String.toPlainTextBody() = toRequestBody("text/plain".toMediaType())

    private fun Any?.toNullablePlainTextBody() = this?.toString()?.toRequestBody("text/plain".toMediaType())

    private fun File.toImagePart(fieldName: String): MultipartBody.Part {
        val body = asRequestBody("image/jpeg".toMediaType())
        return MultipartBody.Part.createFormData(fieldName, name, body)
    }
}
