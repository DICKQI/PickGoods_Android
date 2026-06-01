package com.pickgoods.app.data.repository

import android.util.Log
import com.pickgoods.app.data.api.AuthApi
import com.pickgoods.app.data.local.TokenManager
import com.pickgoods.app.data.model.LoginRequest
import com.pickgoods.app.data.model.RegisterRequest
import com.pickgoods.app.data.model.UserInfo
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val user: UserInfo) : AuthResult()
    data class Error(val message: String, val code: Int? = null) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "AuthRepository"
    }

    suspend fun login(username: String, password: String): AuthResult {
        return try {
            val response = authApi.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    tokenManager.saveToken(body.accessToken)
                    AuthResult.Success(UserInfo(id = 0, username = username, role = ""))
                } else {
                    AuthResult.Error("登录响应解析失败", response.code())
                }
            } else {
                AuthResult.Error(parseError(response.errorBody()?.string()), response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "login 异常", e)
            AuthResult.Error("登录失败: ${e.message ?: "网络错误"}", null)
        }
    }

    suspend fun register(username: String, password: String): AuthResult {
        return try {
            val response = authApi.register(RegisterRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    tokenManager.saveToken(body.accessToken)
                    AuthResult.Success(UserInfo(id = 0, username = username, role = ""))
                } else {
                    AuthResult.Error("注册响应解析失败", response.code())
                }
            } else {
                AuthResult.Error(parseError(response.errorBody()?.string()), response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "register 异常", e)
            AuthResult.Error("注册失败: ${e.message ?: "网络错误"}", null)
        }
    }

    suspend fun fetchCurrentUser(): AuthResult {
        return try {
            val response = authApi.getCurrentUser()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    AuthResult.Success(body)
                } else {
                    tokenManager.clearToken()
                    AuthResult.Error("用户信息解析失败", response.code())
                }
            } else {
                tokenManager.clearToken()
                AuthResult.Error("认证已过期，请重新登录", response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchCurrentUser 异常", e)
            AuthResult.Error("请求失败: ${e.message ?: "网络错误"}", null)
        }
    }

    suspend fun logout() {
        try {
            authApi.logout()
        } catch (_: Exception) { }
        tokenManager.clearToken()
    }

    suspend fun isLoggedIn(): Boolean = tokenManager.getToken() != null

    private fun parseError(errorBody: String?): String {
        if (errorBody == null) return "请求失败"
        return try {
            val gson = com.google.gson.Gson()
            val map = gson.fromJson(errorBody, Map::class.java)
            (map["detail"] as? String)
                ?: (map["non_field_errors"] as? List<*>)?.firstOrNull()?.toString()
                ?: errorBody.take(200)
        } catch (_: Exception) {
            errorBody.take(200)
        }
    }
}
