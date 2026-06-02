package com.pickgoods.app.data.api

import com.pickgoods.app.data.model.AdminRole
import com.pickgoods.app.data.model.AdminUser
import com.pickgoods.app.data.model.AdminUserCreateRequest
import com.pickgoods.app.data.model.AdminUserUpdateRequest
import com.pickgoods.app.data.model.StandardPaginatedResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminApi {
    @GET("api/admin/users/")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<StandardPaginatedResponse<AdminUser>>

    @GET("api/admin/users/{id}/")
    suspend fun getUserDetail(@Path("id") id: Int): Response<AdminUser>

    @POST("api/admin/users/")
    suspend fun createUser(@Body request: AdminUserCreateRequest): Response<AdminUser>

    @PATCH("api/admin/users/{id}/")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body request: AdminUserUpdateRequest
    ): Response<AdminUser>

    @GET("api/admin/roles/")
    suspend fun getRoles(): Response<List<AdminRole>>
}
