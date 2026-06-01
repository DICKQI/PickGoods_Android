package com.pickgoods.app.data.api

import com.pickgoods.app.data.model.AuthTokenResponse
import com.pickgoods.app.data.model.LoginRequest
import com.pickgoods.app.data.model.RegisterRequest
import com.pickgoods.app.data.model.UserInfo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login/")
    suspend fun login(@Body request: LoginRequest): Response<AuthTokenResponse>

    @POST("api/auth/register/")
    suspend fun register(@Body request: RegisterRequest): Response<AuthTokenResponse>

    @GET("api/auth/me/")
    suspend fun getCurrentUser(): Response<UserInfo>

    @DELETE("api/auth/logout/")
    suspend fun logout(): Response<Unit>
}
