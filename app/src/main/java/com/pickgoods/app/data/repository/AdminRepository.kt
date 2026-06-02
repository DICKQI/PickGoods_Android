package com.pickgoods.app.data.repository

import android.util.Log
import com.pickgoods.app.data.api.AdminApi
import com.pickgoods.app.data.model.AdminRole
import com.pickgoods.app.data.model.AdminUser
import com.pickgoods.app.data.model.AdminUserCreateRequest
import com.pickgoods.app.data.model.AdminUserUpdateRequest
import com.pickgoods.app.data.model.PaginatedResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val adminApi: AdminApi
) {
    companion object {
        private const val TAG = "AdminRepository"
    }

    suspend fun getUsers(page: Int, pageSize: Int = 20): GoodsResult<PaginatedResponse<AdminUser>> {
        return try {
            when (val result = adminApi.getUsers(page = page, pageSize = pageSize).toGoodsResult("用户列表解析失败")) {
                is GoodsResult.Success -> GoodsResult.Success(
                    PaginatedResponse(
                        count = result.data.count,
                        page = page,
                        pageSize = pageSize,
                        results = result.data.results
                    )
                )
                is GoodsResult.Error -> result
            }
        } catch (e: Exception) {
            Log.e(TAG, "getUsers 异常", e)
            GoodsResult.Error("获取用户失败: ${e.message}", null)
        }
    }

    suspend fun getRoles(): GoodsResult<List<AdminRole>> {
        return try {
            adminApi.getRoles().toGoodsResult("角色列表解析失败")
        } catch (e: Exception) {
            Log.e(TAG, "getRoles 异常", e)
            GoodsResult.Error("获取角色失败: ${e.message}", null)
        }
    }

    suspend fun getUserDetail(id: Int): GoodsResult<AdminUser> {
        return try {
            adminApi.getUserDetail(id).toGoodsResult("用户详情解析失败")
        } catch (e: Exception) {
            Log.e(TAG, "getUserDetail 异常", e)
            GoodsResult.Error("获取用户详情失败: ${e.message}", null)
        }
    }

    suspend fun createUser(username: String, password: String, roleId: Int): GoodsResult<AdminUser> {
        return try {
            adminApi.createUser(
                AdminUserCreateRequest(
                    username = username.trim(),
                    password = password,
                    roleId = roleId
                )
            ).toGoodsResult("用户创建失败")
        } catch (e: Exception) {
            Log.e(TAG, "createUser 异常", e)
            GoodsResult.Error("创建用户失败: ${e.message}", null)
        }
    }

    suspend fun updateUser(
        id: Int,
        roleId: Int? = null,
        isActive: Boolean? = null,
        password: String? = null
    ): GoodsResult<AdminUser> {
        return try {
            adminApi.updateUser(
                id,
                AdminUserUpdateRequest(
                    roleId = roleId,
                    isActive = isActive,
                    password = password?.takeIf { it.isNotBlank() }
                )
            ).toGoodsResult("用户更新失败")
        } catch (e: Exception) {
            Log.e(TAG, "updateUser 异常", e)
            GoodsResult.Error("更新用户失败: ${e.message}", null)
        }
    }
}
