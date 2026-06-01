package com.pickgoods.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickgoods.app.data.local.TokenManager
import com.pickgoods.app.data.repository.AuthRepository
import com.pickgoods.app.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URL
import javax.inject.Inject

data class SettingsUiState(
    val baseUrl: String = "",
    val defaultBaseUrl: String = TokenManager.DEFAULT_BASE_URL,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val urlError: String? = null,
    val username: String? = null,
    val role: String? = null,
    val userId: Int? = null,
    val isAdmin: Boolean = false,
    val isRefreshing: Boolean = false,
    val showLogoutConfirm: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val url = tokenManager.getBaseUrl()
            _uiState.update { it.copy(baseUrl = url) }
        }
        refreshUserInfo()
    }

    fun onUrlChanged(url: String) {
        _uiState.update {
            it.copy(baseUrl = url, urlError = null, saveSuccess = false)
        }
    }

    fun validateUrl(url: String): String? {
        if (url.isBlank()) return "后端地址不能为空"
        return try {
            val parsed = URL(url.trim())
            if (parsed.protocol !in listOf("http", "https")) {
                "只支持 http:// 或 https:// 协议"
            } else {
                null
            }
        } catch (e: Exception) {
            "请输入有效的 URL 地址，例如：http://192.168.1.100:8000"
        }
    }

    fun saveUrl() {
        val url = _uiState.value.baseUrl.trim()
        val error = validateUrl(url)
        if (error != null) {
            _uiState.update { it.copy(urlError = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, urlError = null) }
            tokenManager.saveBaseUrl(url)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    saveSuccess = true
                )
            }
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            tokenManager.saveBaseUrl(TokenManager.DEFAULT_BASE_URL)
            _uiState.update {
                it.copy(
                    baseUrl = TokenManager.DEFAULT_BASE_URL,
                    urlError = null,
                    saveSuccess = false
                )
            }
        }
    }

    fun refreshUserInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            when (val result = authRepo.fetchCurrentUser()) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            username = result.user.username,
                            role = result.user.role,
                            userId = result.user.id,
                            isAdmin = result.user.role.equals("Admin", ignoreCase = true)
                        )
                    }
                }
                else -> {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
            }
        }
    }

    fun showLogoutConfirm() {
        _uiState.update { it.copy(showLogoutConfirm = true) }
    }

    fun dismissLogoutConfirm() {
        _uiState.update { it.copy(showLogoutConfirm = false) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
            _uiState.update {
                it.copy(
                    showLogoutConfirm = false,
                    username = null,
                    role = null,
                    userId = null,
                    isAdmin = false
                )
            }
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
