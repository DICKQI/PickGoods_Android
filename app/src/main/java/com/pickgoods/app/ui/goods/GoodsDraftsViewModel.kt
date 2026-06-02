package com.pickgoods.app.ui.goods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickgoods.app.data.local.TokenManager
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.data.repository.GoodsRepository
import com.pickgoods.app.data.repository.GoodsResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

data class GoodsDraftsUiState(
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,
    val drafts: List<GoodsListItem> = emptyList(),
    val page: Int = 1,
    val pageSize: Int = 18,
    val totalCount: Int = 0,
    val totalPages: Int = 1,
    val baseUrl: String = TokenManager.DEFAULT_BASE_URL
)

@HiltViewModel
class GoodsDraftsViewModel @Inject constructor(
    private val goodsRepository: GoodsRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(GoodsDraftsUiState())
    val uiState: StateFlow<GoodsDraftsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh(page: Int = _uiState.value.page) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    page = page.coerceAtLeast(1),
                    baseUrl = tokenManager.getBaseUrl()
                )
            }
            val current = _uiState.value
            when (
                val result = goodsRepository.getList(
                    page = current.page,
                    pageSize = current.pageSize,
                    status = "draft"
                )
            ) {
                is GoodsResult.Success -> {
                    val data = result.data
                    val pages = ceil(data.count.toDouble() / data.pageSize.coerceAtLeast(1)).toInt().coerceAtLeast(1)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            drafts = data.results,
                            page = data.page,
                            pageSize = data.pageSize.takeIf { size -> size > 0 } ?: current.pageSize,
                            totalCount = data.count,
                            totalPages = pages
                        )
                    }
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun deleteDraft(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            when (val result = goodsRepository.deleteGoods(id)) {
                is GoodsResult.Success -> {
                    val nextPage = if (_uiState.value.drafts.size <= 1 && _uiState.value.page > 1) {
                        _uiState.value.page - 1
                    } else {
                        _uiState.value.page
                    }
                    _uiState.update { it.copy(isDeleting = false) }
                    refresh(nextPage)
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isDeleting = false, error = result.message)
                }
            }
        }
    }
}
