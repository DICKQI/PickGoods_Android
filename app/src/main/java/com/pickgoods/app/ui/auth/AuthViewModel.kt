package com.pickgoods.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickgoods.app.data.local.TokenManager
import com.pickgoods.app.data.model.UserInfo
import com.pickgoods.app.data.repository.AuthRepository
import com.pickgoods.app.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: UserInfo? = null,
    val isLoggedIn: Boolean = false,
    val isInitialized: Boolean = false,
    val baseUrl: String = TokenManager.DEFAULT_BASE_URL,
    val urlError: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkLoginState()
    }

    private fun checkLoginState() {
        viewModelScope.launch {
            val url = tokenManager.getBaseUrl()
            _uiState.update { it.copy(baseUrl = url) }

            val loggedIn = authRepo.isLoggedIn()
            if (loggedIn) {
                when (val result = authRepo.fetchCurrentUser()) {
                    is AuthResult.Success -> {
                        _uiState.update {
                            it.copy(
                                user = result.user,
                                isLoggedIn = true,
                                isInitialized = true
                            )
                        }
                    }
                    else -> {
                        _uiState.update { it.copy(isInitialized = true) }
                    }
                }
            } else {
                _uiState.update { it.copy(isInitialized = true) }
            }
        }
    }

    fun onBaseUrlChanged(url: String) {
        _uiState.update { it.copy(baseUrl = url, urlError = null) }
    }

    fun saveBaseUrl(url: String) {
        viewModelScope.launch {
            tokenManager.saveBaseUrl(url.trim())
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 先保存 URL，确保用最新地址请求
            tokenManager.saveBaseUrl(_uiState.value.baseUrl.trim())

            when (val result = authRepo.login(username, password)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            user = result.user,
                            isLoggedIn = true,
                            isInitialized = true,
                            isLoading = false
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            tokenManager.saveBaseUrl(_uiState.value.baseUrl.trim())

            when (val result = authRepo.register(username, password)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            user = result.user,
                            isLoggedIn = true,
                            isInitialized = true,
                            isLoading = false
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
            _uiState.value = AuthUiState(
                isInitialized = true,
                baseUrl = _uiState.value.baseUrl
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
