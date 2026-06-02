package com.pickgoods.app.ui.goods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickgoods.app.data.local.TokenManager
import com.pickgoods.app.data.model.Category
import com.pickgoods.app.data.model.Character
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.data.model.IP
import com.pickgoods.app.data.model.StorageNode
import com.pickgoods.app.data.model.Theme
import com.pickgoods.app.data.repository.GoodsRepository
import com.pickgoods.app.data.repository.GoodsResult
import com.pickgoods.app.data.repository.LocationRepository
import com.pickgoods.app.data.repository.MetadataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoodsListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val goods: List<GoodsListItem> = emptyList(),
    val page: Int = 1,
    val pageSize: Int = 18,
    val totalCount: Int = 0,
    val totalPages: Int = 0,
    val searchQuery: String = "",
    val ips: List<IP> = emptyList(),
    val characters: List<Character> = emptyList(),
    val categories: List<Category> = emptyList(),
    val themes: List<Theme> = emptyList(),
    val locations: List<StorageNode> = emptyList(),
    val isMetadataLoading: Boolean = false,
    val selectedIpId: Int? = null,
    val selectedCharacterId: Int? = null,
    val selectedCharacterIds: Set<Int> = emptySet(),
    val selectedCategoryId: Int? = null,
    val selectedThemeId: Int? = null,
    val selectedLocationId: Int? = null,
    val statusFilter: String? = "in_cabinet",
    val statusIn: String? = null,
    val officialFilter: Boolean? = null,
    val groupBy: String? = null,
    val viewMode: GoodsViewMode = GoodsViewMode.STANDARD,
    val similarSeedStrategy: String = "diverse",
    val selectionMode: Boolean = false,
    val selectedGoodsById: Map<String, GoodsListItem> = emptyMap(),
    val baseUrl: String = TokenManager.DEFAULT_BASE_URL
)

enum class GoodsViewMode {
    STANDARD,
    SIMILAR_RANDOM
}

@HiltViewModel
class GoodsViewModel @Inject constructor(
    private val goodsRepo: GoodsRepository,
    private val metadataRepository: MetadataRepository,
    private val locationRepository: LocationRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoodsListUiState())
    val uiState: StateFlow<GoodsListUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadBaseUrl()
        loadFilterMetadata()
        loadGoods()
    }

    private fun loadBaseUrl() {
        viewModelScope.launch {
            val url = tokenManager.getBaseUrl()
            _uiState.update { it.copy(baseUrl = url) }
        }
    }

    fun loadGoods(page: Int = 1) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val state = _uiState.value
            val characterIds = state.selectedCharacterIds.takeIf { it.isNotEmpty() }?.toList()
            val singleCharacterId = if (characterIds == null) state.selectedCharacterId else null
            val result = if (state.viewMode == GoodsViewMode.SIMILAR_RANDOM) {
                goodsRepo.getSimilarRandomList(
                    page = 1,
                    pageSize = state.pageSize,
                    search = state.searchQuery.ifBlank { null },
                    ip = state.selectedIpId,
                    character = singleCharacterId,
                    characterIds = characterIds,
                    category = state.selectedCategoryId,
                    theme = state.selectedThemeId,
                    location = state.selectedLocationId,
                    status = state.statusFilter,
                    statusIn = state.statusIn,
                    isOfficial = state.officialFilter,
                    seedStrategy = state.similarSeedStrategy
                )
            } else {
                goodsRepo.getList(
                    page = page,
                    pageSize = state.pageSize,
                    search = state.searchQuery.ifBlank { null },
                    ip = state.selectedIpId,
                    character = singleCharacterId,
                    characterIds = characterIds,
                    category = state.selectedCategoryId,
                    theme = state.selectedThemeId,
                    location = state.selectedLocationId,
                    status = state.statusFilter,
                    statusIn = state.statusIn,
                    isOfficial = state.officialFilter,
                    groupBy = state.groupBy
                )
            }

            when (result) {
                is GoodsResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            goods = result.data.results,
                            page = result.data.page,
                            totalCount = result.data.count,
                            totalPages = (result.data.count + result.data.pageSize - 1) / result.data.pageSize
                        )
                    }
                }
                is GoodsResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun refreshGoods() {
        val refreshMode = _uiState.value.viewMode
        if (refreshMode == GoodsViewMode.SIMILAR_RANDOM) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val state = _uiState.value
                val characterIds = state.selectedCharacterIds.takeIf { it.isNotEmpty() }?.toList()
                val singleCharacterId = if (characterIds == null) state.selectedCharacterId else null
                when (val result = goodsRepo.getSimilarRandomList(
                    page = 1,
                    pageSize = state.pageSize,
                    search = state.searchQuery.ifBlank { null },
                    ip = state.selectedIpId,
                    character = singleCharacterId,
                    characterIds = characterIds,
                    category = state.selectedCategoryId,
                    theme = state.selectedThemeId,
                    location = state.selectedLocationId,
                    status = state.statusFilter,
                    statusIn = state.statusIn,
                    isOfficial = state.officialFilter,
                    seedStrategy = state.similarSeedStrategy,
                    refresh = true
                )) {
                    is GoodsResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                goods = result.data.results,
                                page = result.data.page,
                                totalCount = result.data.count,
                                totalPages = (result.data.count + result.data.pageSize - 1) / result.data.pageSize
                            )
                        }
                    }
                    is GoodsResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        } else {
            loadGoods(page = _uiState.value.page)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            loadGoods(page = 1)
        }
    }

    fun setPage(page: Int) {
        loadGoods(page)
    }

    fun setStatusFilter(status: String?) {
        _uiState.update {
            it.copy(
                statusFilter = status,
                statusIn = null
            )
        }
        loadGoods(page = 1)
    }

    fun setStatusSelection(statuses: Set<String>) {
        _uiState.update {
            it.copy(
                statusFilter = if (statuses.size == 1) statuses.first() else null,
                statusIn = statuses.takeIf { set -> set.size > 1 }?.joinToString(",")
            )
        }
        loadGoods(page = 1)
    }

    fun setOfficialFilter(isOfficial: Boolean?) {
        _uiState.update { it.copy(officialFilter = isOfficial) }
        loadGoods(page = 1)
    }

    fun setIpFilter(id: Int?) {
        _uiState.update {
            it.copy(
                selectedIpId = id,
                selectedCharacterId = null,
                selectedCharacterIds = emptySet()
            )
        }
        id?.let(::loadCharactersForIp)
        loadGoods(page = 1)
    }

    fun setCharacterFilter(id: Int?) {
        _uiState.update { current ->
            if (id == null) {
                current.copy(
                    selectedCharacterId = null,
                    selectedCharacterIds = emptySet()
                )
            } else {
                val next = if (id in current.selectedCharacterIds) {
                    current.selectedCharacterIds - id
                } else {
                    current.selectedCharacterIds + id
                }
                current.copy(
                    selectedCharacterId = next.singleOrNull(),
                    selectedCharacterIds = next
                )
            }
        }
        loadGoods(page = 1)
    }

    fun setCategoryFilter(id: Int?) {
        _uiState.update { it.copy(selectedCategoryId = id) }
        loadGoods(page = 1)
    }

    fun setThemeFilter(id: Int?) {
        _uiState.update { it.copy(selectedThemeId = id) }
        loadGoods(page = 1)
    }

    fun setLocationFilter(id: Int?) {
        _uiState.update { it.copy(selectedLocationId = id) }
        loadGoods(page = 1)
    }

    fun setGroupBy(groupBy: String?) {
        _uiState.update { it.copy(groupBy = groupBy, viewMode = GoodsViewMode.STANDARD) }
        loadGoods(page = 1)
    }

    fun setViewMode(viewMode: GoodsViewMode) {
        _uiState.update {
            it.copy(
                viewMode = viewMode,
                groupBy = if (viewMode == GoodsViewMode.SIMILAR_RANDOM) null else it.groupBy
            )
        }
        loadGoods(page = 1)
    }

    fun setSimilarSeedStrategy(strategy: String) {
        _uiState.update {
            it.copy(
                similarSeedStrategy = strategy,
                viewMode = GoodsViewMode.SIMILAR_RANDOM,
                groupBy = null
            )
        }
        loadGoods(page = 1)
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedIpId = null,
                selectedCharacterId = null,
                selectedCharacterIds = emptySet(),
                selectedCategoryId = null,
                selectedThemeId = null,
                selectedLocationId = null,
                statusFilter = "in_cabinet",
                statusIn = null,
                officialFilter = null,
                groupBy = null,
                viewMode = GoodsViewMode.STANDARD,
                similarSeedStrategy = "diverse"
            )
        }
        loadGoods(page = 1)
    }

    fun enterSelectionMode() {
        _uiState.update { it.copy(selectionMode = true) }
    }

    fun exitSelectionMode(clearSelection: Boolean = true) {
        _uiState.update {
            it.copy(
                selectionMode = false,
                selectedGoodsById = if (clearSelection) emptyMap() else it.selectedGoodsById
            )
        }
    }

    fun toggleGoodsSelection(goods: GoodsListItem) {
        _uiState.update { current ->
            val next = current.selectedGoodsById.toMutableMap()
            if (next.containsKey(goods.id)) {
                next.remove(goods.id)
            } else {
                next[goods.id] = goods
            }
            current.copy(
                selectionMode = true,
                selectedGoodsById = next
            )
        }
    }

    fun removeGoodsSelection(id: String) {
        _uiState.update { current ->
            current.copy(selectedGoodsById = current.selectedGoodsById - id)
        }
    }

    fun clearGoodsSelection() {
        _uiState.update { it.copy(selectedGoodsById = emptyMap()) }
    }

    fun refreshMetadata() {
        loadFilterMetadata()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadFilterMetadata() {
        viewModelScope.launch {
            _uiState.update { it.copy(isMetadataLoading = true) }
            val ipsDeferred = async { metadataRepository.getIPs() }
            val charactersDeferred = async { metadataRepository.getCharacters() }
            val categoriesDeferred = async { metadataRepository.getCategoryTree() }
            val themesDeferred = async { metadataRepository.getThemes() }
            val locationsDeferred = async { locationRepository.getNodes() }

            val ips = ipsDeferred.await()
            val characters = charactersDeferred.await()
            val categories = categoriesDeferred.await()
            val themes = themesDeferred.await()
            val locations = locationsDeferred.await()

            _uiState.update { current ->
                current.copy(
                    isMetadataLoading = false,
                    ips = if (ips is GoodsResult.Success) ips.data else current.ips,
                    characters = if (characters is GoodsResult.Success) characters.data else current.characters,
                    categories = if (categories is GoodsResult.Success) categories.data else current.categories,
                    themes = if (themes is GoodsResult.Success) themes.data else current.themes,
                    locations = if (locations is GoodsResult.Success) locations.data else current.locations,
                    error = listOf(ips, characters, categories, themes, locations)
                        .filterIsInstance<GoodsResult.Error>()
                        .firstOrNull()
                        ?.message
                )
            }
        }
    }

    private fun loadCharactersForIp(ipId: Int) {
        viewModelScope.launch {
            when (val result = metadataRepository.getIPCharacters(ipId)) {
                is GoodsResult.Success -> _uiState.update { current ->
                    val others = current.characters.filterNot { character ->
                        character.ip.id == ipId || character.ipId == ipId
                    }
                    current.copy(characters = (others + result.data).distinctBy { it.id })
                }
                is GoodsResult.Error -> Unit
            }
        }
    }
}
