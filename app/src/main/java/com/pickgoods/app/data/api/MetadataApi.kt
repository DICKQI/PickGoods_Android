package com.pickgoods.app.data.api

import com.pickgoods.app.data.model.Category
import com.pickgoods.app.data.model.CategoryBatchUpdateOrderRequest
import com.pickgoods.app.data.model.CategoryBatchUpdateOrderResponse
import com.pickgoods.app.data.model.CategoryRequest
import com.pickgoods.app.data.model.Character
import com.pickgoods.app.data.model.CharacterRequest
import com.pickgoods.app.data.model.BgmCreateCharactersRequest
import com.pickgoods.app.data.model.BgmCreateCharactersResponse
import com.pickgoods.app.data.model.BgmGetCharactersRequest
import com.pickgoods.app.data.model.BgmGetCharactersResponse
import com.pickgoods.app.data.model.BgmSearchCharactersRequest
import com.pickgoods.app.data.model.BgmSearchCharactersResponse
import com.pickgoods.app.data.model.BgmSearchSubjectsRequest
import com.pickgoods.app.data.model.BgmSearchSubjectsResponse
import com.pickgoods.app.data.model.IP
import com.pickgoods.app.data.model.IPBatchUpdateOrderRequest
import com.pickgoods.app.data.model.IPBatchUpdateOrderResponse
import com.pickgoods.app.data.model.IPRequest
import com.pickgoods.app.data.model.Theme
import com.pickgoods.app.data.model.ThemeRequest
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

interface MetadataApi {
    @GET("api/ips/")
    suspend fun getIPs(
        @Query("search") search: String? = null,
        @Query("subject_type") subjectType: Int? = null
    ): Response<List<IP>>

    @POST("api/ips/")
    suspend fun createIP(@Body request: IPRequest): Response<IP>

    @GET("api/ips/{id}/")
    suspend fun getIPDetail(@Path("id") id: Int): Response<IP>

    @PUT("api/ips/{id}/")
    suspend fun updateIP(@Path("id") id: Int, @Body request: IPRequest): Response<IP>

    @PATCH("api/ips/{id}/")
    suspend fun patchIP(@Path("id") id: Int, @Body request: IPRequest): Response<IP>

    @DELETE("api/ips/{id}/")
    suspend fun deleteIP(@Path("id") id: Int): Response<Unit>

    @POST("api/ips/batch-update-order/")
    suspend fun batchUpdateIPOrder(
        @Body request: IPBatchUpdateOrderRequest
    ): Response<IPBatchUpdateOrderResponse>

    @GET("api/ips/{id}/characters/")
    suspend fun getIPCharacters(@Path("id") id: Int): Response<List<Character>>

    @GET("api/characters/")
    suspend fun getCharacters(
        @Query("search") search: String? = null,
        @Query("ip") ip: Int? = null
    ): Response<List<Character>>

    @POST("api/characters/")
    suspend fun createCharacter(@Body request: CharacterRequest): Response<Character>

    @GET("api/characters/{id}/")
    suspend fun getCharacterDetail(@Path("id") id: Int): Response<Character>

    @Multipart
    @POST("api/characters/")
    suspend fun createCharacterWithAvatar(
        @Part("name") name: RequestBody,
        @Part("ip_id") ipId: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part avatar: MultipartBody.Part
    ): Response<Character>

    @PUT("api/characters/{id}/")
    suspend fun updateCharacter(
        @Path("id") id: Int,
        @Body request: CharacterRequest
    ): Response<Character>

    @PATCH("api/characters/{id}/")
    suspend fun patchCharacter(
        @Path("id") id: Int,
        @Body request: CharacterRequest
    ): Response<Character>

    @Multipart
    @PUT("api/characters/{id}/")
    suspend fun updateCharacterWithAvatar(
        @Path("id") id: Int,
        @Part("name") name: RequestBody,
        @Part("ip_id") ipId: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part avatar: MultipartBody.Part
    ): Response<Character>

    @Multipart
    @PATCH("api/characters/{id}/")
    suspend fun patchCharacterWithAvatar(
        @Path("id") id: Int,
        @Part("name") name: RequestBody,
        @Part("ip_id") ipId: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part avatar: MultipartBody.Part
    ): Response<Character>

    @DELETE("api/characters/{id}/")
    suspend fun deleteCharacter(@Path("id") id: Int): Response<Unit>

    @GET("api/categories/")
    suspend fun getCategories(@Query("search") search: String? = null): Response<List<Category>>

    @GET("api/categories/tree/")
    suspend fun getCategoryTree(): Response<List<Category>>

    @POST("api/categories/")
    suspend fun createCategory(@Body request: CategoryRequest): Response<Category>

    @GET("api/categories/{id}/")
    suspend fun getCategoryDetail(@Path("id") id: Int): Response<Category>

    @PUT("api/categories/{id}/")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Body request: CategoryRequest
    ): Response<Category>

    @PATCH("api/categories/{id}/")
    suspend fun patchCategory(
        @Path("id") id: Int,
        @Body request: CategoryRequest
    ): Response<Category>

    @DELETE("api/categories/{id}/")
    suspend fun deleteCategory(@Path("id") id: Int): Response<Unit>

    @POST("api/categories/batch-update-order/")
    suspend fun batchUpdateCategoryOrder(
        @Body request: CategoryBatchUpdateOrderRequest
    ): Response<CategoryBatchUpdateOrderResponse>

    @GET("api/themes/")
    suspend fun getThemes(@Query("search") search: String? = null): Response<List<Theme>>

    @GET("api/themes/{id}/")
    suspend fun getThemeDetail(@Path("id") id: Int): Response<Theme>

    @POST("api/themes/")
    suspend fun createTheme(@Body request: ThemeRequest): Response<Theme>

    @PUT("api/themes/{id}/")
    suspend fun updateTheme(@Path("id") id: Int, @Body request: ThemeRequest): Response<Theme>

    @PATCH("api/themes/{id}/")
    suspend fun patchTheme(@Path("id") id: Int, @Body request: ThemeRequest): Response<Theme>

    @Multipart
    @POST("api/themes/{id}/upload-images/")
    suspend fun uploadThemeImages(
        @Path("id") id: Int,
        @Part additionalPhotos: List<MultipartBody.Part>,
        @Part("label") label: RequestBody? = null
    ): Response<Theme>

    @Multipart
    @POST("api/themes/{id}/upload-images/")
    suspend fun updateThemeImageLabel(
        @Path("id") id: Int,
        @Part("photo_ids") photoIds: List<RequestBody>,
        @Part("label") label: RequestBody
    ): Response<Theme>

    @DELETE("api/themes/{id}/images/{photoId}/")
    suspend fun deleteThemeImage(
        @Path("id") id: Int,
        @Path("photoId") photoId: Int
    ): Response<Theme>

    @DELETE("api/themes/{id}/images/")
    suspend fun deleteThemeImages(
        @Path("id") id: Int,
        @Query("photo_ids") photoIds: String
    ): Response<Theme>

    @DELETE("api/themes/{id}/")
    suspend fun deleteTheme(@Path("id") id: Int): Response<Unit>

    @POST("api/bgm/search-subjects/")
    suspend fun searchBgmSubjects(
        @Body request: BgmSearchSubjectsRequest
    ): Response<BgmSearchSubjectsResponse>

    @POST("api/bgm/search-characters/")
    suspend fun searchBgmCharacters(
        @Body request: BgmSearchCharactersRequest
    ): Response<BgmSearchCharactersResponse>

    @POST("api/bgm/get-characters-by-id/")
    suspend fun getBgmCharactersBySubjectId(
        @Body request: BgmGetCharactersRequest
    ): Response<BgmGetCharactersResponse>

    @POST("api/bgm/create-characters/")
    suspend fun createBgmCharacters(
        @Body request: BgmCreateCharactersRequest
    ): Response<BgmCreateCharactersResponse>
}
