package com.pickgoods.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickgoods.app.data.local.TokenManager
import com.pickgoods.app.data.model.AdminRole
import com.pickgoods.app.data.model.AdminUser
import com.pickgoods.app.data.model.Category
import com.pickgoods.app.data.model.Character
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.data.model.IP
import com.pickgoods.app.data.model.StorageNode
import com.pickgoods.app.data.model.Theme
import com.pickgoods.app.data.repository.AdminRepository
import com.pickgoods.app.data.repository.GoodsRepository
import com.pickgoods.app.data.repository.GoodsResult
import com.pickgoods.app.data.repository.LocationRepository
import com.pickgoods.app.data.repository.MetadataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val users: List<AdminUser> = emptyList(),
    val roles: List<AdminRole> = emptyList(),
    val usersPage: Int = 1,
    val usersPageSize: Int = 20,
    val usersTotalCount: Int = 0,
    val usersTotalPages: Int = 0,
    val isUsersLoading: Boolean = false,
    val isRolesLoading: Boolean = false,
    val isSavingUser: Boolean = false,
    val isUserDetailLoading: Boolean = false,
    val activeUserDetail: AdminUser? = null,
    val usersError: String? = null,
    val goods: List<GoodsListItem> = emptyList(),
    val goodsPage: Int = 1,
    val goodsPageSize: Int = 20,
    val goodsTotalCount: Int = 0,
    val goodsTotalPages: Int = 0,
    val goodsSearch: String = "",
    val goodsStatus: String? = null,
    val goodsUserId: Int? = null,
    val goodsIpId: Int? = null,
    val goodsCharacterId: Int? = null,
    val goodsCharacterIds: Set<Int> = emptySet(),
    val goodsCategoryId: Int? = null,
    val goodsThemeId: Int? = null,
    val goodsLocationId: Int? = null,
    val goodsOfficial: Boolean? = null,
    val ips: List<IP> = emptyList(),
    val characters: List<Character> = emptyList(),
    val categories: List<Category> = emptyList(),
    val themes: List<Theme> = emptyList(),
    val locations: List<StorageNode> = emptyList(),
    val isGoodsMetadataLoading: Boolean = false,
    val isGoodsLoading: Boolean = false,
    val isGoodsMutating: Boolean = false,
    val goodsError: String? = null,
    val baseUrl: String = TokenManager.DEFAULT_BASE_URL
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val goodsRepository: GoodsRepository,
    private val metadataRepository: MetadataRepository,
    private val locationRepository: LocationRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        refresh()
        loadBaseUrl()
    }

    fun refresh() {
        loadRoles()
        loadUsers(_uiState.value.usersPage)
        loadGoodsMetadata()
        loadGoods(_uiState.value.goodsPage)
    }

    private fun loadBaseUrl() {
        viewModelScope.launch {
            _uiState.update { it.copy(baseUrl = tokenManager.getBaseUrl()) }
        }
    }

    fun loadRoles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRolesLoading = true, usersError = null) }
            when (val result = adminRepository.getRoles()) {
                is GoodsResult.Success -> _uiState.update {
                    it.copy(isRolesLoading = false, roles = result.data)
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isRolesLoading = false, usersError = result.message)
                }
            }
        }
    }

    fun loadUsers(page: Int = 1) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUsersLoading = true, usersError = null) }
            val pageSize = _uiState.value.usersPageSize
            when (val result = adminRepository.getUsers(page = page, pageSize = pageSize)) {
                is GoodsResult.Success -> _uiState.update {
                    val totalPages = pageCount(result.data.count, result.data.pageSize)
                    it.copy(
                        isUsersLoading = false,
                        users = result.data.results,
                        usersPage = result.data.page,
                        usersTotalCount = result.data.count,
                        usersTotalPages = totalPages
                    )
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isUsersLoading = false, usersError = result.message)
                }
            }
        }
    }

    fun loadUserDetail(user: AdminUser) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    activeUserDetail = user,
                    isUserDetailLoading = true,
                    usersError = null
                )
            }
            when (val result = adminRepository.getUserDetail(user.id)) {
                is GoodsResult.Success -> _uiState.update { state ->
                    state.copy(
                        isUserDetailLoading = false,
                        activeUserDetail = result.data,
                        users = state.users.map { if (it.id == result.data.id) result.data else it }
                    )
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isUserDetailLoading = false, usersError = result.message)
                }
            }
        }
    }

    fun clearUserDetail() {
        _uiState.update {
            it.copy(activeUserDetail = null, isUserDetailLoading = false)
        }
    }

    fun saveUser(
        existing: AdminUser?,
        username: String,
        password: String,
        roleId: Int?,
        isActive: Boolean
    ) {
        if (username.isBlank()) {
            _uiState.update { it.copy(usersError = "请输入用户名") }
            return
        }
        if (roleId == null) {
            _uiState.update { it.copy(usersError = "请选择角色") }
            return
        }
        if (existing == null && password.length < 6) {
            _uiState.update { it.copy(usersError = "新用户密码至少 6 位") }
            return
        }
        if (existing != null && password.isNotBlank() && password.length < 6) {
            _uiState.update { it.copy(usersError = "新密码至少 6 位") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingUser = true, usersError = null) }
            val result = if (existing == null) {
                adminRepository.createUser(username, password, roleId)
            } else {
                adminRepository.updateUser(
                    id = existing.id,
                    roleId = roleId,
                    isActive = isActive,
                    password = password.takeIf { it.isNotBlank() }
                )
            }
            when (result) {
                is GoodsResult.Success -> {
                    _uiState.update { it.copy(isSavingUser = false, activeUserDetail = result.data) }
                    loadUsers(_uiState.value.usersPage)
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isSavingUser = false, usersError = result.message)
                }
            }
        }
    }

    fun toggleUserActive(user: AdminUser) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingUser = true, usersError = null) }
            when (val result = adminRepository.updateUser(id = user.id, isActive = !user.isActive)) {
                is GoodsResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isSavingUser = false,
                            activeUserDetail = state.activeUserDetail?.let {
                                if (it.id == result.data.id) result.data else it
                            }
                        )
                    }
                    loadUsers(_uiState.value.usersPage)
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isSavingUser = false, usersError = result.message)
                }
            }
        }
    }

    fun updateGoodsSearch(query: String) {
        _uiState.update { it.copy(goodsSearch = query) }
    }

    fun setGoodsUserFilter(userId: Int?) {
        _uiState.update { it.copy(goodsUserId = userId) }
        loadGoods(1)
    }

    fun setGoodsStatusFilter(status: String?) {
        _uiState.update { it.copy(goodsStatus = status) }
        loadGoods(1)
    }

    fun setGoodsIpFilter(ipId: Int?) {
        _uiState.update {
            it.copy(
                goodsIpId = ipId,
                goodsCharacterId = null,
                goodsCharacterIds = emptySet()
            )
        }
        ipId?.let(::loadCharactersForIp)
        loadGoods(1)
    }

    fun setGoodsCharacterFilter(characterId: Int?) {
        _uiState.update { current ->
            if (characterId == null) {
                current.copy(
                    goodsCharacterId = null,
                    goodsCharacterIds = emptySet()
                )
            } else {
                val next = if (characterId in current.goodsCharacterIds) {
                    current.goodsCharacterIds - characterId
                } else {
                    current.goodsCharacterIds + characterId
                }
                current.copy(
                    goodsCharacterId = next.singleOrNull(),
                    goodsCharacterIds = next
                )
            }
        }
        loadGoods(1)
    }

    fun setGoodsCategoryFilter(categoryId: Int?) {
        _uiState.update { it.copy(goodsCategoryId = categoryId) }
        loadGoods(1)
    }

    fun setGoodsThemeFilter(themeId: Int?) {
        _uiState.update { it.copy(goodsThemeId = themeId) }
        loadGoods(1)
    }

    fun setGoodsLocationFilter(locationId: Int?) {
        _uiState.update { it.copy(goodsLocationId = locationId) }
        loadGoods(1)
    }

    fun setGoodsOfficialFilter(isOfficial: Boolean?) {
        _uiState.update { it.copy(goodsOfficial = isOfficial) }
        loadGoods(1)
    }

    fun resetGoodsFilters() {
        _uiState.update {
            it.copy(
                goodsStatus = null,
                goodsUserId = null,
                goodsIpId = null,
                goodsCharacterId = null,
                goodsCharacterIds = emptySet(),
                goodsCategoryId = null,
                goodsThemeId = null,
                goodsLocationId = null,
                goodsOfficial = null
            )
        }
        loadGoods(1)
    }

    fun searchGoods() {
        loadGoods(1)
    }

    fun loadGoods(page: Int = 1) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoodsLoading = true, goodsError = null) }
            val state = _uiState.value
            val characterIds = state.goodsCharacterIds.takeIf { it.isNotEmpty() }?.toList()
            val singleCharacterId = if (characterIds == null) state.goodsCharacterId else null
            when (val result = goodsRepository.getList(
                page = page,
                pageSize = state.goodsPageSize,
                search = state.goodsSearch.ifBlank { null },
                status = state.goodsStatus,
                user = state.goodsUserId,
                ip = state.goodsIpId,
                character = singleCharacterId,
                characterIds = characterIds,
                category = state.goodsCategoryId,
                theme = state.goodsThemeId,
                location = state.goodsLocationId,
                isOfficial = state.goodsOfficial
            )) {
                is GoodsResult.Success -> _uiState.update {
                    it.copy(
                        isGoodsLoading = false,
                        goods = result.data.results,
                        goodsPage = result.data.page,
                        goodsTotalCount = result.data.count,
                        goodsTotalPages = pageCount(result.data.count, result.data.pageSize)
                    )
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isGoodsLoading = false, goodsError = result.message)
                }
            }
        }
    }

    fun deleteGoods(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoodsMutating = true, goodsError = null) }
            when (val result = goodsRepository.deleteGoods(id)) {
                is GoodsResult.Success -> {
                    _uiState.update { it.copy(isGoodsMutating = false) }
                    loadGoods(_uiState.value.goodsPage)
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isGoodsMutating = false, goodsError = result.message)
                }
            }
        }
    }

    fun moveGoods(goodsId: String, anchorId: String, position: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoodsMutating = true, goodsError = null) }
            when (val result = goodsRepository.moveGoods(goodsId, anchorId, position)) {
                is GoodsResult.Success -> {
                    _uiState.update { it.copy(isGoodsMutating = false) }
                    loadGoods(_uiState.value.goodsPage)
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isGoodsMutating = false, goodsError = result.message)
                }
            }
        }
    }

    private fun loadGoodsMetadata() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoodsMetadataLoading = true) }
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
                    isGoodsMetadataLoading = false,
                    ips = if (ips is GoodsResult.Success) ips.data else current.ips,
                    characters = if (characters is GoodsResult.Success) characters.data else current.characters,
                    categories = if (categories is GoodsResult.Success) categories.data else current.categories,
                    themes = if (themes is GoodsResult.Success) themes.data else current.themes,
                    locations = if (locations is GoodsResult.Success) locations.data else current.locations,
                    goodsError = listOf(ips, characters, categories, themes, locations)
                        .filterIsInstance<GoodsResult.Error>()
                        .firstOrNull()
                        ?.message
                        ?: current.goodsError
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

    private fun pageCount(total: Int, pageSize: Int): Int {
        if (pageSize <= 0) return 0
        return (total + pageSize - 1) / pageSize
    }
}
