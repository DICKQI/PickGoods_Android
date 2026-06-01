package com.pickgoods.app.ui.goods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickgoods.app.data.local.TokenManager
import com.pickgoods.app.data.model.GoodsDetail
import com.pickgoods.app.data.repository.GoodsRepository
import com.pickgoods.app.data.repository.GoodsResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoodsDetailUiState(
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,
    val goods: GoodsDetail? = null,
    val deleted: Boolean = false,
    val baseUrl: String = TokenManager.DEFAULT_BASE_URL
)

@HiltViewModel
class GoodsDetailViewModel @Inject constructor(
    private val repository: GoodsRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(GoodsDetailUiState())
    val uiState: StateFlow<GoodsDetailUiState> = _uiState.asStateFlow()

    fun load(id: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null, baseUrl = tokenManager.getBaseUrl())
            }
            when (val result = repository.getDetail(id)) {
                is GoodsResult.Success -> _uiState.update {
                    it.copy(isLoading = false, goods = result.data)
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun delete() {
        val goodsId = _uiState.value.goods?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            when (val result = repository.deleteGoods(goodsId)) {
                is GoodsResult.Success -> _uiState.update { it.copy(isDeleting = false, deleted = true) }
                is GoodsResult.Error -> _uiState.update { it.copy(isDeleting = false, error = result.message) }
            }
        }
    }
}
