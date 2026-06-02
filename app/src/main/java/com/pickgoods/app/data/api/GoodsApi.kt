package com.pickgoods.app.data.api

import com.pickgoods.app.data.model.GoodsCreateRequest
import com.pickgoods.app.data.model.GoodsDetail
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.data.model.GoodsMoveRequest
import com.pickgoods.app.data.model.GoodsMoveResponse
import com.pickgoods.app.data.model.GoodsStatsResponse
import com.pickgoods.app.data.model.PaginatedResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GoodsApi {
    @GET("api/goods/")
    suspend fun getGoodsList(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 18,
        @Query("search") search: String? = null,
        @Query("ip") ip: Int? = null,
        @Query("character") character: Int? = null,
        @Query("character") characterIds: List<Int>? = null,
        @Query("category") category: Int? = null,
        @Query("theme") theme: Int? = null,
        @Query("location") location: Int? = null,
        @Query("status") status: String? = null,
        @Query("status__in") statusIn: String? = null,
        @Query("is_official") isOfficial: Boolean? = null,
        @Query("user") user: Int? = null,
        @Query("group_by") groupBy: String? = null
    ): Response<PaginatedResponse<GoodsListItem>>

    @GET("api/goods/similar-random/")
    suspend fun getSimilarRandomGoodsList(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 18,
        @Query("search") search: String? = null,
        @Query("ip") ip: Int? = null,
        @Query("character") character: Int? = null,
        @Query("character") characterIds: List<Int>? = null,
        @Query("category") category: Int? = null,
        @Query("theme") theme: Int? = null,
        @Query("location") location: Int? = null,
        @Query("status") status: String? = null,
        @Query("status__in") statusIn: String? = null,
        @Query("is_official") isOfficial: Boolean? = null,
        @Query("user") user: Int? = null,
        @Query("seed_strategy") seedStrategy: String? = null,
        @Query("refresh") refresh: Int? = null
    ): Response<PaginatedResponse<GoodsListItem>>

    @GET("api/goods/{id}/")
    suspend fun getGoodsDetail(@Path("id") id: String): Response<GoodsDetail>

    @GET("api/goods/stats/")
    suspend fun getGoodsStats(
        @Query("top") top: Int = 8,
        @Query("group_by") groupBy: String? = null,
        @Query("search") search: String? = null,
        @Query("ip") ip: Int? = null,
        @Query("character") character: Int? = null,
        @Query("character") characterIds: List<Int>? = null,
        @Query("category") category: Int? = null,
        @Query("theme") theme: Int? = null,
        @Query("location") location: Int? = null,
        @Query("status") status: String? = null,
        @Query("status__in") statusIn: String? = null,
        @Query("is_official") isOfficial: Boolean? = null,
        @Query("purchase_start") purchaseStart: String? = null,
        @Query("purchase_end") purchaseEnd: String? = null,
        @Query("created_start") createdStart: String? = null,
        @Query("created_end") createdEnd: String? = null
    ): Response<GoodsStatsResponse>

    @POST("api/goods/")
    suspend fun createGoods(@Body request: GoodsCreateRequest): Response<GoodsDetail>

    @PUT("api/goods/{id}/")
    suspend fun updateGoods(
        @Path("id") id: String,
        @Body request: GoodsCreateRequest
    ): Response<GoodsDetail>

    @PATCH("api/goods/{id}/")
    suspend fun patchGoods(
        @Path("id") id: String,
        @Body request: GoodsCreateRequest
    ): Response<GoodsDetail>

    @DELETE("api/goods/{id}/")
    suspend fun deleteGoods(@Path("id") id: String): Response<Unit>

    @POST("api/goods/{id}/move/")
    suspend fun moveGoods(
        @Path("id") id: String,
        @Body request: GoodsMoveRequest
    ): Response<GoodsMoveResponse>

    @Multipart
    @POST("api/goods/{id}/upload-main-photo/")
    suspend fun uploadMainPhoto(
        @Path("id") id: String,
        @Part mainPhoto: MultipartBody.Part
    ): Response<GoodsDetail>

    @Multipart
    @POST("api/goods/{id}/upload-additional-photos/")
    suspend fun uploadAdditionalPhotos(
        @Path("id") id: String,
        @Part additionalPhotos: List<MultipartBody.Part>,
        @Part("photo_ids") photoIds: List<RequestBody>? = null,
        @Part("label") label: RequestBody? = null
    ): Response<GoodsDetail>

    @DELETE("api/goods/{id}/additional-photos/{photoId}/")
    suspend fun deleteAdditionalPhoto(
        @Path("id") id: String,
        @Path("photoId") photoId: Int
    ): Response<GoodsDetail>

    @DELETE("api/goods/{id}/additional-photos/")
    suspend fun deleteAdditionalPhotos(
        @Path("id") id: String,
        @Query("photo_ids") photoIds: String
    ): Response<GoodsDetail>
}
