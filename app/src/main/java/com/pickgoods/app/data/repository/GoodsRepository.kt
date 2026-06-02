package com.pickgoods.app.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pickgoods.app.data.api.GoodsApi
import com.pickgoods.app.data.local.CacheDao
import com.pickgoods.app.data.local.CacheEntry
import com.pickgoods.app.data.model.GoodsCreateRequest
import com.pickgoods.app.data.model.GoodsDetail
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.data.model.GoodsMoveRequest
import com.pickgoods.app.data.model.GoodsStatsResponse
import com.pickgoods.app.data.model.PaginatedResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class GoodsResult<out T> {
    data class Success<T>(val data: T) : GoodsResult<T>()
    data class Error(
        val message: String,
        val code: Int? = null,
        val rawBody: String? = null
    ) : GoodsResult<Nothing>()
}

@Singleton
class GoodsRepository @Inject constructor(
    private val goodsApi: GoodsApi,
    private val cacheDao: CacheDao
) {
    companion object {
        private const val TAG = "GoodsRepository"
    }

    private val gson = Gson()

    suspend fun getList(
        page: Int = 1,
        pageSize: Int = 18,
        search: String? = null,
        ip: Int? = null,
        character: Int? = null,
        characterIds: List<Int>? = null,
        category: Int? = null,
        theme: Int? = null,
        location: Int? = null,
        status: String? = null,
        statusIn: String? = null,
        isOfficial: Boolean? = null,
        user: Int? = null,
        groupBy: String? = null
    ): GoodsResult<PaginatedResponse<GoodsListItem>> {
        val key = cacheKey(
            "list",
            page,
            pageSize,
            search,
            ip,
            character,
            characterIds?.joinToString(","),
            category,
            theme,
            location,
            status,
            statusIn,
            isOfficial,
            user,
            groupBy
        )
        return try {
            val response = goodsApi.getGoodsList(
                page = page, pageSize = pageSize, search = search,
                ip = ip, character = character,
                characterIds = characterIds,
                category = category, theme = theme, location = location,
                status = status, statusIn = statusIn, isOfficial = isOfficial,
                user = user,
                groupBy = groupBy
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    cachePut(key, body)
                    GoodsResult.Success(body)
                } else {
                    Log.e(TAG, "响应解析失败，body 为 null，原始响应: ${response.errorBody()?.string()?.take(500)}")
                    cacheFallback<PaginatedResponse<GoodsListItem>>(key)
                        ?: GoodsResult.Error("数据解析失败，可能后端数据格式与 App 不匹配", response.code())
                }
            } else {
                cacheFallback<PaginatedResponse<GoodsListItem>>(key) ?: GoodsResult.Error(
                    message = response.errorBody()?.string()?.take(200) ?: "请求失败",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "网络请求异常", e)
            cacheFallback<PaginatedResponse<GoodsListItem>>(key)
                ?: GoodsResult.Error("请求失败: ${e.message ?: "未知错误"}", null)
        }
    }

    suspend fun getSimilarRandomList(
        page: Int = 1,
        pageSize: Int = 18,
        search: String? = null,
        ip: Int? = null,
        character: Int? = null,
        characterIds: List<Int>? = null,
        category: Int? = null,
        theme: Int? = null,
        location: Int? = null,
        status: String? = null,
        statusIn: String? = null,
        isOfficial: Boolean? = null,
        user: Int? = null,
        seedStrategy: String? = null,
        refresh: Boolean = false
    ): GoodsResult<PaginatedResponse<GoodsListItem>> {
        return try {
            val response = goodsApi.getSimilarRandomGoodsList(
                page = page,
                pageSize = pageSize,
                search = search,
                ip = ip,
                character = character,
                characterIds = characterIds,
                category = category,
                theme = theme,
                location = location,
                status = status,
                statusIn = statusIn,
                isOfficial = isOfficial,
                user = user,
                seedStrategy = seedStrategy,
                refresh = if (refresh) 1 else null
            )
            response.toGoodsResult()
        } catch (e: Exception) {
            Log.e(TAG, "getSimilarRandomList 异常", e)
            GoodsResult.Error("请求失败: ${e.message}", null)
        }
    }

    suspend fun getDetail(id: String): GoodsResult<GoodsDetail> {
        val key = goodsDetailCacheKey(id)
        return try {
            val response = goodsApi.getGoodsDetail(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    cachePut(key, body)
                    GoodsResult.Success(body)
                } else {
                    cacheFallback<GoodsDetail>(key)
                        ?: GoodsResult.Error("数据解析失败", response.code())
                }
            } else {
                cacheFallback<GoodsDetail>(key)
                    ?: GoodsResult.Error(response.errorBody()?.string()?.take(200) ?: "请求失败", response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "getDetail 异常", e)
            cacheFallback<GoodsDetail>(key)
                ?: GoodsResult.Error("请求失败: ${e.message}", null)
        }
    }

    suspend fun getStats(
        top: Int = 8,
        groupBy: String? = null,
        search: String? = null,
        ip: Int? = null,
        character: Int? = null,
        characterIds: List<Int>? = null,
        category: Int? = null,
        theme: Int? = null,
        location: Int? = null,
        status: String? = null,
        statusIn: String? = null,
        isOfficial: Boolean? = null,
        purchaseStart: String? = null,
        purchaseEnd: String? = null,
        createdStart: String? = null,
        createdEnd: String? = null
    ): GoodsResult<GoodsStatsResponse> {
        val key = cacheKey(
            "stats",
            top,
            groupBy,
            search,
            ip,
            character,
            characterIds?.joinToString(","),
            category,
            theme,
            location,
            status,
            statusIn,
            isOfficial,
            purchaseStart,
            purchaseEnd,
            createdStart,
            createdEnd
        )
        return try {
            val response = goodsApi.getGoodsStats(
                top = top,
                groupBy = groupBy,
                search = search,
                ip = ip,
                character = character,
                characterIds = characterIds,
                category = category,
                theme = theme,
                location = location,
                status = status,
                statusIn = statusIn,
                isOfficial = isOfficial,
                purchaseStart = purchaseStart,
                purchaseEnd = purchaseEnd,
                createdStart = createdStart,
                createdEnd = createdEnd
            )
            when (val result = response.toGoodsResult()) {
                is GoodsResult.Success -> {
                    cachePut(key, result.data)
                    result
                }
                is GoodsResult.Error -> cacheFallback<GoodsStatsResponse>(key) ?: result
            }
        } catch (e: Exception) {
            Log.e(TAG, "getStats 异常", e)
            cacheFallback<GoodsStatsResponse>(key)
                ?: GoodsResult.Error("请求失败: ${e.message}", null)
        }
    }

    suspend fun createGoods(request: GoodsCreateRequest): GoodsResult<GoodsDetail> {
        return try {
            val response = goodsApi.createGoods(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    cachePut(goodsDetailCacheKey(body.id), body)
                    cacheDao.deleteByPrefix("goods:list")
                    GoodsResult.Success(body)
                }
                else GoodsResult.Error("数据解析失败", response.code())
            } else {
                val rawBody = response.errorBody()?.string()
                GoodsResult.Error(rawBody?.take(200) ?: "创建失败", response.code(), rawBody)
            }
        } catch (e: Exception) {
            Log.e(TAG, "createGoods 异常", e)
            GoodsResult.Error("请求失败: ${e.message}", null)
        }
    }

    suspend fun updateGoods(id: String, request: GoodsCreateRequest): GoodsResult<GoodsDetail> {
        return try {
            val response = goodsApi.updateGoods(id, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    cachePut(goodsDetailCacheKey(body.id), body)
                    cacheDao.deleteByPrefix("goods:list")
                    GoodsResult.Success(body)
                }
                else GoodsResult.Error("数据解析失败", response.code())
            } else {
                val rawBody = response.errorBody()?.string()
                GoodsResult.Error(rawBody?.take(200) ?: "更新失败", response.code(), rawBody)
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateGoods 异常", e)
            GoodsResult.Error("请求失败: ${e.message}", null)
        }
    }

    suspend fun patchGoods(id: String, request: GoodsCreateRequest): GoodsResult<GoodsDetail> {
        return try {
            val response = goodsApi.patchGoods(id, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    cachePut(goodsDetailCacheKey(body.id), body)
                    cacheDao.deleteByPrefix("goods:list")
                    GoodsResult.Success(body)
                }
                else GoodsResult.Error("数据解析失败", response.code())
            } else {
                val rawBody = response.errorBody()?.string()
                GoodsResult.Error(rawBody?.take(200) ?: "更新失败", response.code(), rawBody)
            }
        } catch (e: Exception) {
            Log.e(TAG, "patchGoods 异常", e)
            GoodsResult.Error("请求失败: ${e.message}", null)
        }
    }

    suspend fun deleteGoods(id: String): GoodsResult<Unit> {
        return try {
            val response = goodsApi.deleteGoods(id)
            if (response.isSuccessful) {
                cacheDao.delete(goodsDetailCacheKey(id))
                cacheDao.deleteByPrefix("goods:list")
                GoodsResult.Success(Unit)
            }
            else GoodsResult.Error(response.errorBody()?.string()?.take(200) ?: "删除失败", response.code())
        } catch (e: Exception) {
            Log.e(TAG, "deleteGoods 异常", e)
            GoodsResult.Error("请求失败: ${e.message}", null)
        }
    }

    suspend fun moveGoods(id: String, anchorId: String, position: String): GoodsResult<Unit> {
        return try {
            val response = goodsApi.moveGoods(id, GoodsMoveRequest(anchorId, position))
            if (response.isSuccessful) {
                cacheDao.deleteByPrefix("goods:list")
                GoodsResult.Success(Unit)
            } else {
                GoodsResult.Error(response.errorBody()?.string()?.take(200) ?: "移动失败", response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "moveGoods 异常", e)
            GoodsResult.Error("请求失败: ${e.message}", null)
        }
    }

    suspend fun uploadMainPhoto(id: String, imageFile: File): GoodsResult<GoodsDetail> {
        return try {
            val part = imageFile.toImagePart("main_photo")
            val response = goodsApi.uploadMainPhoto(id, part)
            when (val result = response.toGoodsResult("主图上传失败")) {
                is GoodsResult.Success -> {
                    cachePut(goodsDetailCacheKey(result.data.id), result.data)
                    cacheDao.deleteByPrefix("goods:list")
                    result
                }
                is GoodsResult.Error -> result
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadMainPhoto 异常", e)
            GoodsResult.Error("主图上传失败: ${e.message}", null)
        }
    }

    suspend fun uploadAdditionalPhotos(
        id: String,
        imageFiles: List<File>,
        label: String? = null,
        photoIds: List<Int>? = null
    ): GoodsResult<GoodsDetail> {
        return try {
            val parts = imageFiles.map { it.toImagePart("additional_photos") }
            val photoIdBodies = photoIds
                ?.takeIf { it.isNotEmpty() }
                ?.map { it.toString().toRequestBody("text/plain".toMediaType()) }
            val labelBody = label
                ?.trim()
                ?.toRequestBody("text/plain".toMediaType())
            val response = goodsApi.uploadAdditionalPhotos(id, parts, photoIdBodies, labelBody)
            when (val result = response.toGoodsResult("附加图片上传失败")) {
                is GoodsResult.Success -> {
                    cachePut(goodsDetailCacheKey(result.data.id), result.data)
                    cacheDao.deleteByPrefix("goods:list")
                    result
                }
                is GoodsResult.Error -> result
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadAdditionalPhotos 异常", e)
            GoodsResult.Error("附加图片上传失败: ${e.message}", null)
        }
    }

    suspend fun updateAdditionalPhotoLabel(
        id: String,
        photoIds: List<Int>,
        label: String
    ): GoodsResult<GoodsDetail> {
        return try {
            if (photoIds.isEmpty()) return GoodsResult.Error("请选择要更新的图片", null)
            val photoIdBodies = photoIds.map {
                it.toString().toRequestBody("text/plain".toMediaType())
            }
            val labelBody = label.trim().toRequestBody("text/plain".toMediaType())
            when (val result = goodsApi.uploadAdditionalPhotos(
                id = id,
                additionalPhotos = emptyList(),
                photoIds = photoIdBodies,
                label = labelBody
            ).toGoodsResult("附加图片标签更新失败")) {
                is GoodsResult.Success -> {
                    cachePut(goodsDetailCacheKey(result.data.id), result.data)
                    result
                }
                is GoodsResult.Error -> result
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateAdditionalPhotoLabel 异常", e)
            GoodsResult.Error("附加图片标签更新失败: ${e.message}", null)
        }
    }

    suspend fun deleteAdditionalPhoto(id: String, photoId: Int): GoodsResult<GoodsDetail> {
        return try {
            when (val result = goodsApi.deleteAdditionalPhoto(id, photoId).toGoodsResult("附加图片删除失败")) {
                is GoodsResult.Success -> {
                    cachePut(goodsDetailCacheKey(result.data.id), result.data)
                    cacheDao.deleteByPrefix("goods:list")
                    result
                }
                is GoodsResult.Error -> result
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteAdditionalPhoto 异常", e)
            GoodsResult.Error("附加图片删除失败: ${e.message}", null)
        }
    }

    suspend fun deleteAdditionalPhotos(id: String, photoIds: List<Int>): GoodsResult<GoodsDetail> {
        return try {
            val query = photoIds.joinToString(",")
            when (val result = goodsApi.deleteAdditionalPhotos(id, query).toGoodsResult("附加图片删除失败")) {
                is GoodsResult.Success -> {
                    cachePut(goodsDetailCacheKey(result.data.id), result.data)
                    cacheDao.deleteByPrefix("goods:list")
                    result
                }
                is GoodsResult.Error -> result
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteAdditionalPhotos 异常", e)
            GoodsResult.Error("附加图片删除失败: ${e.message}", null)
        }
    }

    private fun File.toImagePart(fieldName: String): MultipartBody.Part {
        val body = asRequestBody("image/jpeg".toMediaType())
        return MultipartBody.Part.createFormData(fieldName, name, body)
    }

    private suspend fun cachePut(key: String, value: Any) {
        cacheDao.upsert(CacheEntry(key, gson.toJson(value)))
    }

    private suspend inline fun <reified T> cacheFallback(key: String): GoodsResult<T>? {
        val entry = cacheDao.get(key) ?: return null
        return runCatching {
            val type = object : TypeToken<T>() {}.type
            val data: T = gson.fromJson(entry.payload, type)
            GoodsResult.Success(data)
        }.getOrNull()
    }

    private fun goodsDetailCacheKey(id: String): String = "goods:detail:$id"

    private fun cacheKey(prefix: String, vararg parts: Any?): String {
        return buildString {
            append("goods:")
            append(prefix)
            parts.forEach { part ->
                append(':')
                append(part?.toString()?.takeIf { it.isNotBlank() } ?: "_")
            }
        }
    }
}
