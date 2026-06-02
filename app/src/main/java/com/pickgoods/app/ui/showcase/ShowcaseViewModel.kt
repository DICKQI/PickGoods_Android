package com.pickgoods.app.ui.showcase

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickgoods.app.data.local.TokenManager
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.data.model.GoodsStatsResponse
import com.pickgoods.app.data.model.Showcase
import com.pickgoods.app.data.model.ShowcaseDetail
import com.pickgoods.app.data.model.ShowcaseGoods
import com.pickgoods.app.data.model.ShowcaseRequest
import com.pickgoods.app.data.repository.GoodsRepository
import com.pickgoods.app.data.repository.GoodsResult
import com.pickgoods.app.data.repository.ShowcaseRepository
import com.pickgoods.app.data.repository.ShowcaseScope
import com.pickgoods.app.data.util.ImageUploadUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ShowcaseUiState(
    val isLoading: Boolean = false,
    val isDetailLoading: Boolean = false,
    val isStatsLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingCover: Boolean = false,
    val error: String? = null,
    val statsError: String? = null,
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
    val statsFilter: StatsFilterState = StatsFilterState(),
    val baseUrl: String = TokenManager.DEFAULT_BASE_URL
)

data class StatsFilterState(
    val top: Int = 10,
    val groupBy: String = "month",
    val searchQuery: String = "",
    val ipId: Int? = null,
    val characterId: Int? = null,
    val characterIds: Set<Int> = emptySet(),
    val categoryId: Int? = null,
    val themeId: Int? = null,
    val locationId: Int? = null,
    val purchaseDatePreset: StatsDatePreset = StatsDatePreset.ALL,
    val createdDatePreset: StatsDatePreset = StatsDatePreset.ALL,
    val statuses: Set<String> = emptySet(),
    val isOfficial: Boolean? = null
)

enum class StatsDatePreset(val label: String) {
    ALL("全部"),
    LAST_30_DAYS("近30天"),
    LAST_90_DAYS("近90天"),
    THIS_YEAR("今年")
}

private data class StatsDateBounds(
    val start: String?,
    val end: String?
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
        loadShowcaseDetail(showcase.id)
    }

    private fun loadShowcaseDetail(showcaseId: String) {
        viewModelScope.launch {
            when (val result = showcaseRepository.getShowcaseDetail(showcaseId)) {
                is GoodsResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isDetailLoading = false,
                            selectedShowcase = result.data.toShowcase(),
                            showcaseGoods = result.data.showcaseGoods
                        )
                    }
                }
                is GoodsResult.Error -> {
                    _uiState.update { it.copy(isDetailLoading = false, error = result.message) }
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

    fun addGoodsToSelectedShowcase(goodsId: String, notes: String? = null) {
        val showcaseId = _uiState.value.selectedShowcase?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isShowcaseGoodsMutating = true, error = null, addGoodsError = null) }
            when (val result = showcaseRepository.addGoods(showcaseId, goodsId, notes?.trim())) {
                is GoodsResult.Success -> {
                    loadShowcaseDetail(showcaseId)
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
                    loadShowcaseDetail(showcaseId)
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
                    loadShowcaseDetail(showcaseId)
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

    fun setStatsTop(top: Int) {
        _uiState.update {
            it.copy(statsFilter = it.statsFilter.copy(top = top.coerceIn(3, 30)))
        }
        refreshStats()
    }

    fun setStatsGroupBy(groupBy: String) {
        _uiState.update {
            it.copy(statsFilter = it.statsFilter.copy(groupBy = groupBy))
        }
        refreshStats()
    }

    fun setStatsIpFilter(ipId: Int?) {
        _uiState.update {
            it.copy(
                statsFilter = it.statsFilter.copy(
                    ipId = ipId,
                    characterId = null,
                    characterIds = emptySet()
                )
            )
        }
        refreshStats()
    }

    fun setStatsCharacterFilter(characterId: Int?) {
        _uiState.update { current ->
            val filter = current.statsFilter
            val nextFilter = if (characterId == null) {
                filter.copy(characterId = null, characterIds = emptySet())
            } else {
                val next = if (characterId in filter.characterIds) {
                    filter.characterIds - characterId
                } else {
                    filter.characterIds + characterId
                }
                filter.copy(
                    characterId = next.singleOrNull(),
                    characterIds = next
                )
            }
            current.copy(statsFilter = nextFilter)
        }
        refreshStats()
    }

    fun setStatsCategoryFilter(categoryId: Int?) {
        _uiState.update {
            it.copy(statsFilter = it.statsFilter.copy(categoryId = categoryId))
        }
        refreshStats()
    }

    fun setStatsThemeFilter(themeId: Int?) {
        _uiState.update {
            it.copy(statsFilter = it.statsFilter.copy(themeId = themeId))
        }
        refreshStats()
    }

    fun setStatsLocationFilter(locationId: Int?) {
        _uiState.update {
            it.copy(statsFilter = it.statsFilter.copy(locationId = locationId))
        }
        refreshStats()
    }

    fun updateStatsSearchQuery(query: String) {
        _uiState.update {
            it.copy(statsFilter = it.statsFilter.copy(searchQuery = query))
        }
    }

    fun applyStatsSearch() {
        refreshStats()
    }

    fun setStatsPurchaseDatePreset(preset: StatsDatePreset) {
        _uiState.update {
            it.copy(statsFilter = it.statsFilter.copy(purchaseDatePreset = preset))
        }
        refreshStats()
    }

    fun setStatsCreatedDatePreset(preset: StatsDatePreset) {
        _uiState.update {
            it.copy(statsFilter = it.statsFilter.copy(createdDatePreset = preset))
        }
        refreshStats()
    }

    fun toggleStatsStatus(status: String) {
        _uiState.update { current ->
            val statuses = current.statsFilter.statuses
            current.copy(
                statsFilter = current.statsFilter.copy(
                    statuses = if (status in statuses) statuses - status else statuses + status
                )
            )
        }
        refreshStats()
    }

    fun setStatsOfficialFilter(isOfficial: Boolean?) {
        _uiState.update {
            it.copy(statsFilter = it.statsFilter.copy(isOfficial = isOfficial))
        }
        refreshStats()
    }

    fun resetStatsFilters() {
        _uiState.update { it.copy(statsFilter = StatsFilterState()) }
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch {
            val filter = _uiState.value.statsFilter
            val singleStatus = filter.statuses.singleOrNull()
            val statusIn = filter.statuses
                .takeIf { it.size > 1 }
                ?.joinToString(",")
            val characterIds = filter.characterIds.takeIf { it.isNotEmpty() }?.toList()
            val singleCharacterId = if (characterIds == null) filter.characterId else null
            val purchaseDateBounds = filter.purchaseDatePreset.toBounds()
            val createdDateBounds = filter.createdDatePreset.toBounds()
            _uiState.update { it.copy(isStatsLoading = true, statsError = null) }
            when (val result = goodsRepository.getStats(
                top = filter.top,
                groupBy = filter.groupBy,
                search = filter.searchQuery.ifBlank { null },
                ip = filter.ipId,
                character = singleCharacterId,
                characterIds = characterIds,
                category = filter.categoryId,
                theme = filter.themeId,
                location = filter.locationId,
                status = singleStatus,
                statusIn = statusIn,
                isOfficial = filter.isOfficial,
                purchaseStart = purchaseDateBounds.start,
                purchaseEnd = purchaseDateBounds.end,
                createdStart = createdDateBounds.start,
                createdEnd = createdDateBounds.end
            )) {
                is GoodsResult.Success -> _uiState.update {
                    it.copy(isStatsLoading = false, stats = result.data)
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isStatsLoading = false, statsError = result.message)
                }
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

private fun ShowcaseDetail.toShowcase(): Showcase {
    val previewPhotos = showcaseGoods
        .mapNotNull { it.goods.mainPhoto?.takeIf(String::isNotBlank) }
        .take(4)
    return Showcase(
        id = id,
        name = name,
        description = description,
        coverImage = coverImage,
        order = order,
        isPublic = isPublic,
        createdAt = createdAt,
        updatedAt = updatedAt,
        goodsCount = showcaseGoods.size,
        previewPhotos = previewPhotos
    )
}

private fun StatsDatePreset.toBounds(today: LocalDate = LocalDate.now()): StatsDateBounds {
    return when (this) {
        StatsDatePreset.ALL -> StatsDateBounds(start = null, end = null)
        StatsDatePreset.LAST_30_DAYS -> StatsDateBounds(
            start = today.minusDays(29).toString(),
            end = today.toString()
        )
        StatsDatePreset.LAST_90_DAYS -> StatsDateBounds(
            start = today.minusDays(89).toString(),
            end = today.toString()
        )
        StatsDatePreset.THIS_YEAR -> StatsDateBounds(
            start = LocalDate.of(today.year, 1, 1).toString(),
            end = today.toString()
        )
    }
}
