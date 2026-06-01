package com.pickgoods.app.data.api

import com.pickgoods.app.data.model.PaginatedResponse
import com.pickgoods.app.data.model.Showcase
import com.pickgoods.app.data.model.ShowcaseAddGoodsRequest
import com.pickgoods.app.data.model.ShowcaseGoods
import com.pickgoods.app.data.model.ShowcaseMoveGoodsRequest
import com.pickgoods.app.data.model.ShowcaseRemoveGoodsRequest
import com.pickgoods.app.data.model.ShowcaseRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ShowcaseApi {
    @GET("api/showcases/")
    suspend fun getShowcases(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<PaginatedResponse<Showcase>>

    @GET("api/showcases/public/")
    suspend fun getPublicShowcases(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<PaginatedResponse<Showcase>>

    @GET("api/showcases/private/")
    suspend fun getPrivateShowcases(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<PaginatedResponse<Showcase>>

    @GET("api/showcases/{id}/")
    suspend fun getShowcaseDetail(@Path("id") id: String): Response<Showcase>

    @POST("api/showcases/")
    suspend fun createShowcase(@Body request: ShowcaseRequest): Response<Showcase>

    @PATCH("api/showcases/{id}/")
    suspend fun patchShowcase(
        @Path("id") id: String,
        @Body request: ShowcaseRequest
    ): Response<Showcase>

    @Multipart
    @POST("api/showcases/{id}/upload-cover-image/")
    suspend fun uploadCoverImage(
        @Path("id") id: String,
        @Part coverImage: MultipartBody.Part
    ): Response<Showcase>

    @DELETE("api/showcases/{id}/")
    suspend fun deleteShowcase(@Path("id") id: String): Response<Unit>

    @GET("api/showcases/{id}/goods/")
    suspend fun getShowcaseGoods(@Path("id") id: String): Response<List<ShowcaseGoods>>

    @POST("api/showcases/{id}/add-goods/")
    suspend fun addGoods(
        @Path("id") id: String,
        @Body request: ShowcaseAddGoodsRequest
    ): Response<ShowcaseGoods>

    @POST("api/showcases/{id}/remove-goods/")
    suspend fun removeGoods(
        @Path("id") id: String,
        @Body request: ShowcaseRemoveGoodsRequest
    ): Response<Map<String, String>>

    @POST("api/showcases/{id}/move-goods/")
    suspend fun moveGoods(
        @Path("id") id: String,
        @Body request: ShowcaseMoveGoodsRequest
    ): Response<Map<String, String>>
}
