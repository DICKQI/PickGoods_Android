package com.pickgoods.app.data.model

import com.google.gson.annotations.SerializedName

data class AdminRole(
    val id: Int,
    val name: String,
    @SerializedName("created_at") val createdAt: String? = null
)

data class AdminUser(
    val id: Int,
    val username: String,
    val role: AdminRole? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class AdminUserCreateRequest(
    val username: String,
    val password: String,
    @SerializedName("role_id") val roleId: Int
)

data class AdminUserUpdateRequest(
    @SerializedName("role_id") val roleId: Int? = null,
    @SerializedName("is_active") val isActive: Boolean? = null,
    val password: String? = null
)
