package com.pickgoods.app.data.repository

import android.util.Log
import com.pickgoods.app.data.api.ShowcaseApi
import com.pickgoods.app.data.model.PaginatedResponse
import com.pickgoods.app.data.model.Showcase
import com.pickgoods.app.data.model.ShowcaseAddGoodsRequest
import com.pickgoods.app.data.model.ShowcaseDetail
import com.pickgoods.app.data.model.ShowcaseGoods
import com.pickgoods.app.data.model.ShowcaseMoveGoodsRequest
import com.pickgoods.app.data.model.ShowcaseRemoveGoodsRequest
import com.pickgoods.app.data.model.ShowcaseRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowcaseRepository @Inject constructor(
    private val showcaseApi: ShowcaseApi
) {
    suspend fun getShowcases(
        scope: ShowcaseScope,
        page: Int = 1,
        pageSize: Int = 20
    ): GoodsResult<PaginatedResponse<Showcase>> = safeCall {
        when (val result = when (scope) {
            ShowcaseScope.Private -> showcaseApi.getPrivateShowcases(page, pageSize)
            ShowcaseScope.Public -> showcaseApi.getPublicShowcases(page, pageSize)
        }.toGoodsResult()) {
            is GoodsResult.Success -> {
                val data = result.data
                GoodsResult.Success(
                    data.copy(
                        count = data.count.takeIf { it > 0 } ?: data.results.size,
                        page = data.page.takeIf { it > 0 } ?: page,
                        pageSize = data.pageSize.takeIf { it > 0 } ?: pageSize
                    )
                )
            }
            is GoodsResult.Error -> result
        }
    }

    suspend fun getShowcaseGoods(showcaseId: String): GoodsResult<List<ShowcaseGoods>> = safeCall {
        showcaseApi.getShowcaseGoods(showcaseId).toGoodsResult()
    }

    suspend fun getShowcaseDetail(showcaseId: String): GoodsResult<ShowcaseDetail> = safeCall {
        showcaseApi.getShowcaseDetail(showcaseId).toGoodsResult()
    }

    suspend fun createShowcase(request: ShowcaseRequest): GoodsResult<Showcase> = safeCall {
        showcaseApi.createShowcase(request).toGoodsResult()
    }

    suspend fun patchShowcase(id: String, request: ShowcaseRequest): GoodsResult<Showcase> = safeCall {
        showcaseApi.patchShowcase(id, request).toGoodsResult()
    }

    suspend fun updateShowcase(id: String, request: ShowcaseRequest): GoodsResult<ShowcaseDetail> = safeCall {
        showcaseApi.updateShowcase(id, request).toGoodsResult()
    }

    suspend fun uploadCoverImage(id: String, imageFile: File): GoodsResult<Showcase> = safeCall {
        val part = imageFile.toImagePart("cover_image")
        showcaseApi.uploadCoverImage(id, part).toGoodsResult("封面上传失败")
    }

    suspend fun deleteShowcase(id: String): GoodsResult<Unit> = safeCall {
        showcaseApi.deleteShowcase(id).toUnitGoodsResult()
    }

    suspend fun addGoods(
        showcaseId: String,
        goodsId: String,
        notes: String? = null
    ): GoodsResult<ShowcaseGoods> = safeCall {
        showcaseApi.addGoods(
            showcaseId,
            ShowcaseAddGoodsRequest(
                goodsId = goodsId,
                notes = notes?.takeIf { it.isNotBlank() }
            )
        ).toGoodsResult()
    }

    suspend fun removeGoods(showcaseId: String, goodsId: String): GoodsResult<Map<String, String>> = safeCall {
        showcaseApi.removeGoods(showcaseId, ShowcaseRemoveGoodsRequest(goodsId)).toGoodsResult()
    }

    suspend fun moveGoods(
        showcaseId: String,
        goodsId: String,
        anchorGoodsId: String,
        position: String
    ): GoodsResult<Unit> = safeCall {
        showcaseApi.moveGoods(
            showcaseId,
            ShowcaseMoveGoodsRequest(
                goodsId = goodsId,
                anchorGoodsId = anchorGoodsId,
                position = position
            )
        ).toGoodsResult().let { result ->
            when (result) {
                is GoodsResult.Success -> GoodsResult.Success(Unit)
                is GoodsResult.Error -> result
            }
        }
    }

    private suspend inline fun <T> safeCall(block: suspend () -> GoodsResult<T>): GoodsResult<T> {
        return try {
            block()
        } catch (e: Exception) {
            Log.e("ShowcaseRepository", "API request failed", e)
            GoodsResult.Error("请求失败: ${e.message ?: "未知错误"}")
        }
    }

    private fun File.toImagePart(fieldName: String): MultipartBody.Part {
        val body = asRequestBody("image/jpeg".toMediaType())
        return MultipartBody.Part.createFormData(fieldName, name, body)
    }
}

enum class ShowcaseScope {
    Private,
    Public
}
