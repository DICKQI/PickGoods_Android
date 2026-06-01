package com.pickgoods.app.ui.showcase

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickgoods.app.data.local.TokenManager
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.data.model.GoodsStatsResponse
import com.pickgoods.app.data.model.Showcase
import com.pickgoods.app.data.model.ShowcaseGoods
import com.pickgoods.app.data.model.ShowcaseRequest
import com.pickgoods.app.data.repository.GoodsRepository
import com.pickgoods.app.data.repository.GoodsResult
import com.pickgoods.app.data.repository.ShowcaseRepository
import com.pickgoods.app.data.repository.ShowcaseScope
import com.pickgoods.app.data.util.ImageUploadUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShowcaseUiState(
    val isLoading: Boolean = false,
    val isDetailLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingCover: Boolean = false,
    val error: String? = null,
    val scope: ShowcaseScope = ShowcaseScope.Private,
    val showcases: List<Showcase> = emptyList(),
    val selectedShowcase: Showcase? = null,
    val showcaseGoods: List<ShowcaseGoods> = emptyList(),
    val isShowcaseGoodsMutating: Boolean = false,
    val isAddGoodsSearching: Boolean = false,
    val addGoodsSearchQuery: String = "",
    val addGoodsCandidates: List<GoodsListItem> = emptyList(),
    val addGoodsError: String? = null,
    val totalCount: Int = 0,
    val stats: GoodsStatsResponse? = null,
    val baseUrl: String = TokenManager.DEFAULT_BASE_URL
)

@HiltViewModel
class ShowcaseViewModel @Inject constructor(
    private val showcaseRepository: ShowcaseRepository,
    private val goodsRepository: GoodsRepository,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShowcaseUiState())
    val uiState: StateFlow<ShowcaseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(baseUrl = tokenManager.getBaseUrl()) }
        }
        refreshAll()
    }

    fun refreshAll() {
        refreshShowcases()
        refreshStats()
    }

    fun setScope(scope: ShowcaseScope) {
        _uiState.update { it.copy(scope = scope, selectedShowcase = null, showcaseGoods = emptyList()) }
        refreshShowcases()
    }

    fun refreshShowcases() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = showcaseRepository.getShowcases(_uiState.value.scope)) {
                is GoodsResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showcases = result.data.results,
                            totalCount = if (result.data.count > 0) result.data.count else result.data.results.size
                        )
                    }
                }
                is GoodsResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun selectShowcase(showcase: Showcase) {
        _uiState.update { it.copy(selectedShowcase = showcase, isDetailLoading = true, error = null) }
        loadShowcaseGoods(showcase.id)
    }

    private fun loadShowcaseGoods(showcaseId: String) {
        viewModelScope.launch {
            val goodsDeferred = async { showcaseRepository.getShowcaseGoods(showcaseId) }
            when (val goodsResult = goodsDeferred.await()) {
                is GoodsResult.Success -> {
                    _uiState.update { it.copy(isDetailLoading = false, showcaseGoods = goodsResult.data) }
                }
                is GoodsResult.Error -> {
                    _uiState.update { it.copy(isDetailLoading = false, error = goodsResult.message) }
                }
            }
        }
    }

    fun clearSelectedShowcase() {
        _uiState.update {
            it.copy(
                selectedShowcase = null,
                showcaseGoods = emptyList(),
                addGoodsSearchQuery = "",
                addGoodsCandidates = emptyList(),
                addGoodsError = null
            )
        }
    }

    fun updateAddGoodsSearchQuery(query: String) {
        _uiState.update { it.copy(addGoodsSearchQuery = query) }
    }

    fun searchGoodsForAdd(page: Int = 1) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isAddGoodsSearching = true, addGoodsError = null) }
            when (val result = goodsRepository.getList(
                page = page,
                pageSize = 12,
                search = state.addGoodsSearchQuery.ifBlank { null }
            )) {
                is GoodsResult.Success -> _uiState.update {
                    it.copy(
                        isAddGoodsSearching = false,
                        addGoodsCandidates = result.data.results
                    )
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isAddGoodsSearching = false, addGoodsError = result.message)
                }
            }
        }
    }

    fun addGoodsToSelectedShowcase(goodsId: String) {
        val showcaseId = _uiState.value.selectedShowcase?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isShowcaseGoodsMutating = true, error = null, addGoodsError = null) }
            when (val result = showcaseRepository.addGoods(showcaseId, goodsId)) {
                is GoodsResult.Success -> {
                    loadShowcaseGoods(showcaseId)
                    _uiState.update { it.copy(isShowcaseGoodsMutating = false) }
                    refreshShowcases()
                }
                is GoodsResult.Error -> {
                    val message = if (result.code == 400) {
                        result.message.ifBlank { "该谷子已在展柜中" }
                    } else {
                        result.message
                    }
                    _uiState.update {
                        it.copy(
                            isShowcaseGoodsMutating = false,
                            addGoodsError = message
                        )
                    }
                }
            }
        }
    }

    fun removeGoodsFromSelectedShowcase(goodsId: String) {
        val showcaseId = _uiState.value.selectedShowcase?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isShowcaseGoodsMutating = true, error = null) }
            when (val result = showcaseRepository.removeGoods(showcaseId, goodsId)) {
                is GoodsResult.Success -> {
                    loadShowcaseGoods(showcaseId)
                    _uiState.update { it.copy(isShowcaseGoodsMutating = false) }
                    refreshShowcases()
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isShowcaseGoodsMutating = false, error = result.message)
                }
            }
        }
    }

    fun moveGoodsInSelectedShowcase(goodsId: String, anchorGoodsId: String, position: String) {
        val showcaseId = _uiState.value.selectedShowcase?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isShowcaseGoodsMutating = true, error = null) }
            when (val result = showcaseRepository.moveGoods(showcaseId, goodsId, anchorGoodsId, position)) {
                is GoodsResult.Success -> {
                    loadShowcaseGoods(showcaseId)
                    _uiState.update { it.copy(isShowcaseGoodsMutating = false) }
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isShowcaseGoodsMutating = false, error = result.message)
                }
            }
        }
    }

    fun saveShowcase(
        existing: Showcase?,
        name: String,
        description: String?,
        isPublic: Boolean,
        coverUri: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val request = ShowcaseRequest(
                name = name,
                description = description?.ifBlank { null },
                isPublic = isPublic
            )
            val result = if (existing == null) {
                showcaseRepository.createShowcase(request)
            } else {
                showcaseRepository.patchShowcase(existing.id, request)
            }
            when (result) {
                is GoodsResult.Success -> {
                    val finalShowcaseResult = uploadSelectedCoverIfNeeded(result.data, coverUri)
                    _uiState.update {
                        when (finalShowcaseResult) {
                            is GoodsResult.Error -> it.copy(
                                isSaving = false,
                                isUploadingCover = false,
                                selectedShowcase = result.data,
                                error = "展柜已保存，但封面上传失败：${finalShowcaseResult.message}"
                            )
                            is GoodsResult.Success -> it.copy(
                                isSaving = false,
                                isUploadingCover = false,
                                selectedShowcase = finalShowcaseResult.data
                            )
                            null -> it.copy(
                                isSaving = false,
                                isUploadingCover = false,
                                selectedShowcase = result.data
                            )
                        }
                    }
                    refreshShowcases()
                }
                is GoodsResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, isUploadingCover = false, error = result.message) }
                }
            }
        }
    }

    fun deleteShowcase(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val result = showcaseRepository.deleteShowcase(id)) {
                is GoodsResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            selectedShowcase = null,
                            showcaseGoods = emptyList()
                        )
                    }
                    refreshShowcases()
                }
                is GoodsResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, error = result.message) }
                }
            }
        }
    }

    fun refreshStats() {
        viewModelScope.launch {
            when (val result = goodsRepository.getStats()) {
                is GoodsResult.Success -> _uiState.update { it.copy(stats = result.data) }
                is GoodsResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    private suspend fun uploadSelectedCoverIfNeeded(
        showcase: Showcase,
        coverUri: String?
    ): GoodsResult<Showcase>? {
        if (coverUri.isNullOrBlank()) return null
        _uiState.update { it.copy(isUploadingCover = true) }
        return runCatching {
            val file = ImageUploadUtils.compressImageUri(context, Uri.parse(coverUri))
            showcaseRepository.uploadCoverImage(showcase.id, file)
        }.getOrElse { throwable ->
            GoodsResult.Error(throwable.message ?: "图片处理失败")
        }
    }
}
