package com.pickgoods.app.data.api

import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.data.model.StorageNode
import com.pickgoods.app.data.model.StorageNodeRequest
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

interface LocationApi {
    @GET("api/location/tree/")
    suspend fun getLocationTree(): Response<List<StorageNode>>

    @GET("api/location/nodes/")
    suspend fun getLocationNodes(): Response<List<StorageNode>>

    @GET("api/location/nodes/{id}/")
    suspend fun getLocationNode(@Path("id") id: Int): Response<StorageNode>

    @POST("api/location/nodes/")
    suspend fun createLocationNode(@Body request: StorageNodeRequest): Response<StorageNode>

    @Multipart
    @POST("api/location/nodes/")
    suspend fun createLocationNodeWithImage(
        @Part("name") name: RequestBody,
        @Part("parent") parent: RequestBody? = null,
        @Part("description") description: RequestBody? = null,
        @Part("order") order: RequestBody? = null,
        @Part image: MultipartBody.Part
    ): Response<StorageNode>

    @PUT("api/location/nodes/{id}/")
    suspend fun updateLocationNode(
        @Path("id") id: Int,
        @Body request: StorageNodeRequest
    ): Response<StorageNode>

    @PATCH("api/location/nodes/{id}/")
    suspend fun patchLocationNode(
        @Path("id") id: Int,
        @Body request: StorageNodeRequest
    ): Response<StorageNode>

    @Multipart
    @PATCH("api/location/nodes/{id}/")
    suspend fun patchLocationNodeWithImage(
        @Path("id") id: Int,
        @Part("name") name: RequestBody,
        @Part("parent") parent: RequestBody? = null,
        @Part("description") description: RequestBody? = null,
        @Part("order") order: RequestBody? = null,
        @Part image: MultipartBody.Part
    ): Response<StorageNode>

    @DELETE("api/location/nodes/{id}/")
    suspend fun deleteLocationNode(@Path("id") id: Int): Response<Unit>

    @GET("api/location/nodes/{id}/goods/")
    suspend fun getLocationNodeGoods(
        @Path("id") id: Int,
        @Query("include_children") includeChildren: Boolean = false,
        @Query("page") page: Int = 1
    ): Response<List<GoodsListItem>>
}
