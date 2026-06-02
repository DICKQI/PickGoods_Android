package com.pickgoods.app.ui.metadata

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickgoods.app.data.local.TokenManager
import com.pickgoods.app.data.model.BgmCharacter
import com.pickgoods.app.data.model.BgmCreateCharacterItem
import com.pickgoods.app.data.model.BgmCreateCharactersResponse
import com.pickgoods.app.data.model.BgmSubject
import com.pickgoods.app.data.model.Category
import com.pickgoods.app.data.model.CategoryRequest
import com.pickgoods.app.data.model.Character
import com.pickgoods.app.data.model.CharacterRequest
import com.pickgoods.app.data.model.IP
import com.pickgoods.app.data.model.IPRequest
import com.pickgoods.app.data.model.MetadataOrderItem
import com.pickgoods.app.data.model.Theme
import com.pickgoods.app.data.model.ThemeRequest
import com.pickgoods.app.data.repository.GoodsResult
import com.pickgoods.app.data.repository.MetadataRepository
import com.pickgoods.app.data.util.ImageUploadUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MetadataUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isThemeDetailLoading: Boolean = false,
    val isUploadingThemeImages: Boolean = false,
    val error: String? = null,
    val baseUrl: String = TokenManager.DEFAULT_BASE_URL,
    val searchQuery: String = "",
    val ipSubjectTypeFilter: Int? = null,
    val characterIpFilterId: Int? = null,
    val selectedIpForCharacters: IP? = null,
    val ipCharacters: List<Character> = emptyList(),
    val isIpCharactersLoading: Boolean = false,
    val ips: List<IP> = emptyList(),
    val characters: List<Character> = emptyList(),
    val categories: List<Category> = emptyList(),
    val themes: List<Theme> = emptyList(),
    val activeThemeDetail: Theme? = null,
    val isBgmDialogOpen: Boolean = false,
    val bgmStep: BgmImportStep = BgmImportStep.Search,
    val bgmSearchQuery: String = "",
    val bgmSubjectType: Int? = null,
    val bgmCharacterKeyword: String = "",
    val bgmSubjects: List<BgmSubject> = emptyList(),
    val bgmSelectedSubject: BgmSubject? = null,
    val bgmSubjectName: String = "",
    val bgmCharacters: List<BgmCharacter> = emptyList(),
    val bgmSelectedCharacterIndexes: Set<Int> = emptySet(),
    val bgmImportResult: BgmCreateCharactersResponse? = null
)

enum class BgmImportStep {
    Search,
    Searching,
    Subjects,
    LoadingCharacters,
    Results,
    Importing,
    Imported
}

@HiltViewModel
class MetadataViewModel @Inject constructor(
    private val repository: MetadataRepository,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(MetadataUiState())
    val uiState: StateFlow<MetadataUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(baseUrl = tokenManager.getBaseUrl()) }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val search = _uiState.value.searchQuery.ifBlank { null }
            val ipSubjectType = _uiState.value.ipSubjectTypeFilter
            val characterIp = _uiState.value.characterIpFilterId
            val ipsDeferred = async { repository.getIPs(search, ipSubjectType) }
            val charactersDeferred = async { repository.getCharacters(search, characterIp) }
            val categoriesDeferred = async {
                if (search == null) repository.getCategoryTree() else repository.getCategories(search)
            }
            val themesDeferred = async { repository.getThemes(search) }

            val ips = ipsDeferred.await()
            val characters = charactersDeferred.await()
            val categories = categoriesDeferred.await()
            val themes = themesDeferred.await()

            val firstError = listOf<GoodsResult<*>>(ips, characters, categories, themes)
                .filterIsInstance<GoodsResult.Error>()
                .firstOrNull()

            _uiState.update {
                val latestIps = if (ips is GoodsResult.Success) ips.data else it.ips
                val selectedIp = it.selectedIpForCharacters?.let { selected ->
                    latestIps.firstOrNull { latest -> latest.id == selected.id }
                }
                it.copy(
                    isLoading = false,
                    error = firstError?.message,
                    ips = latestIps,
                    characters = if (characters is GoodsResult.Success) characters.data else it.characters,
                    categories = if (categories is GoodsResult.Success) categories.data else it.categories,
                    themes = if (themes is GoodsResult.Success) themes.data else it.themes,
                    selectedIpForCharacters = selectedIp,
                    ipCharacters = if (selectedIp == null) emptyList() else it.ipCharacters
                )
            }
            _uiState.value.selectedIpForCharacters?.let { selected ->
                loadIPCharactersInternal(selected)
            }
        }
    }

    fun onSearchChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250)
            refresh()
        }
    }

    fun setIpSubjectTypeFilter(subjectType: Int?) {
        _uiState.update { it.copy(ipSubjectTypeFilter = subjectType) }
        refresh()
    }

    fun setCharacterIpFilter(ipId: Int?) {
        _uiState.update { it.copy(characterIpFilterId = ipId) }
        refresh()
    }

    fun loadIPCharacters(ip: IP) {
        viewModelScope.launch {
            loadIPCharactersInternal(ip)
        }
    }

    fun clearIPCharacters() {
        _uiState.update {
            it.copy(
                selectedIpForCharacters = null,
                ipCharacters = emptyList(),
                isIpCharactersLoading = false
            )
        }
    }

    fun saveIP(existing: IP?, name: String, keywordsText: String, subjectType: Int?) {
        val keywords = keywordsText.split(',', '，')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { null }
        runMutation {
            if (existing == null) {
                repository.createIP(IPRequest(name = name, keywords = keywords, subjectType = subjectType))
            } else {
                repository.updateIP(
                    existing.id,
                    IPRequest(name = name, keywords = keywords, subjectType = subjectType, order = existing.order)
                )
            }
        }
    }

    fun deleteIP(id: Int) {
        _uiState.update {
            if (it.selectedIpForCharacters?.id == id || it.characterIpFilterId == id) {
                it.copy(
                    selectedIpForCharacters = it.selectedIpForCharacters?.takeIf { selected -> selected.id != id },
                    ipCharacters = if (it.selectedIpForCharacters?.id == id) emptyList() else it.ipCharacters,
                    characterIpFilterId = it.characterIpFilterId?.takeIf { selectedId -> selectedId != id },
                    isIpCharactersLoading = false
                )
            } else {
                it
            }
        }
        runMutation { repository.deleteIP(id) }
    }

    fun moveIP(id: Int, delta: Int) {
        val ordered = _uiState.value.ips.sortedWith(compareBy<IP> { it.order }.thenBy { it.id })
        val index = ordered.indexOfFirst { it.id == id }
        val targetIndex = (index + delta).coerceIn(0, ordered.lastIndex)
        if (index < 0 || targetIndex == index) return

        val moved = ordered.toMutableList()
        val item = moved.removeAt(index)
        moved.add(targetIndex, item)
        val orderItems = moved.mapIndexed { order, ip -> MetadataOrderItem(ip.id, order) }
        runMutation { repository.batchUpdateIPOrder(orderItems) }
    }

    fun saveCharacter(
        existing: Character?,
        name: String,
        ipId: Int,
        gender: String,
        avatar: String?,
        avatarUri: String? = null
    ) {
        runMutation {
            if (!avatarUri.isNullOrBlank()) {
                val file = ImageUploadUtils.compressImageUri(context, Uri.parse(avatarUri))
                if (existing == null) {
                    repository.createCharacterWithAvatar(name, ipId, gender, file)
                } else {
                    repository.updateCharacterWithAvatar(existing.id, name, ipId, gender, file)
                }
            } else {
                val request = CharacterRequest(
                    name = name,
                    ipId = ipId,
                    gender = gender,
                    avatar = avatar?.ifBlank { null }
                )
                if (existing == null) repository.createCharacter(request)
                else repository.updateCharacter(existing.id, request)
            }
        }
    }

    fun deleteCharacter(id: Int) = runMutation { repository.deleteCharacter(id) }

    fun saveCategory(existing: Category?, name: String, parent: Int?, colorTag: String?, order: Int?) {
        runMutation {
            val request = CategoryRequest(
                name = name,
                parent = parent,
                colorTag = colorTag?.ifBlank { null },
                order = order
            )
            if (existing == null) repository.createCategory(request)
            else repository.patchCategory(existing.id, request)
        }
    }

    fun deleteCategory(id: Int) = runMutation { repository.deleteCategory(id) }

    fun moveCategory(id: Int, delta: Int) {
        val categories = _uiState.value.categories
        val category = categories.firstOrNull { it.id == id } ?: return
        val siblings = categories
            .filter { it.parent == category.parent }
            .sortedWith(compareBy<Category> { it.order }.thenBy { it.id })
        val index = siblings.indexOfFirst { it.id == id }
        val targetIndex = (index + delta).coerceIn(0, siblings.lastIndex)
        if (index < 0 || targetIndex == index) return

        val moved = siblings.toMutableList()
        val item = moved.removeAt(index)
        moved.add(targetIndex, item)
        val orderItems = moved.mapIndexed { order, sibling -> MetadataOrderItem(sibling.id, order) }
        runMutation { repository.batchUpdateCategoryOrder(orderItems) }
    }

    fun loadThemeDetail(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isThemeDetailLoading = true, error = null, activeThemeDetail = null) }
            when (val result = repository.getThemeDetail(id)) {
                is GoodsResult.Success -> _uiState.update {
                    it.copy(isThemeDetailLoading = false, activeThemeDetail = result.data)
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isThemeDetailLoading = false, error = result.message)
                }
            }
        }
    }

    fun clearThemeDetail() {
        _uiState.update { it.copy(activeThemeDetail = null, isThemeDetailLoading = false) }
    }

    fun saveTheme(
        existing: Theme?,
        name: String,
        description: String?,
        imageUris: List<String> = emptyList(),
        imageLabel: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val request = ThemeRequest(name = name, description = description?.ifBlank { null })
            val result = if (existing == null) {
                repository.createTheme(request)
            } else {
                repository.patchTheme(existing.id, request)
            }

            when (result) {
                is GoodsResult.Success -> {
                    val uploadResult = uploadThemeImagesIfNeeded(result.data, imageUris, imageLabel)
                    _uiState.update {
                        when (uploadResult) {
                            is GoodsResult.Success -> it.copy(
                                isSaving = false,
                                isUploadingThemeImages = false,
                                activeThemeDetail = uploadResult.data
                            )
                            is GoodsResult.Error -> it.copy(
                                isSaving = false,
                                isUploadingThemeImages = false,
                                activeThemeDetail = result.data,
                                error = "主题已保存，但图片上传失败：${uploadResult.message}"
                            )
                            null -> it.copy(
                                isSaving = false,
                                isUploadingThemeImages = false,
                                activeThemeDetail = result.data
                            )
                        }
                    }
                    refresh()
                }
                is GoodsResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isUploadingThemeImages = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun updateThemeImageLabel(themeId: Int, photoId: Int, label: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val result = repository.updateThemeImageLabel(themeId, listOf(photoId), label.trim())) {
                is GoodsResult.Success -> _uiState.update {
                    it.copy(isSaving = false, activeThemeDetail = result.data)
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isSaving = false, error = result.message)
                }
            }
        }
    }

    fun deleteThemeImage(themeId: Int, photoId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val result = repository.deleteThemeImage(themeId, photoId)) {
                is GoodsResult.Success -> _uiState.update {
                    it.copy(isSaving = false, activeThemeDetail = result.data)
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isSaving = false, error = result.message)
                }
            }
        }
    }

    fun deleteThemeImages(themeId: Int, photoIds: Set<Int>) {
        if (photoIds.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val result = repository.deleteThemeImages(themeId, photoIds.toList())) {
                is GoodsResult.Success -> _uiState.update {
                    it.copy(isSaving = false, activeThemeDetail = result.data)
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isSaving = false, error = result.message)
                }
            }
        }
    }

    fun deleteTheme(id: Int) = runMutation { repository.deleteTheme(id) }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun openBgmImport() {
        _uiState.update {
            it.copy(isBgmDialogOpen = true)
        }
        resetBgmImport(keepOpen = true)
    }

    fun closeBgmImport() {
        resetBgmImport(keepOpen = false)
        refresh()
    }

    fun resetBgmImport(keepOpen: Boolean = true) {
        _uiState.update {
            it.copy(
                isBgmDialogOpen = keepOpen,
                bgmStep = BgmImportStep.Search,
                bgmSearchQuery = "",
                bgmSubjectType = null,
                bgmCharacterKeyword = "",
                bgmSubjects = emptyList(),
                bgmSelectedSubject = null,
                bgmSubjectName = "",
                bgmCharacters = emptyList(),
                bgmSelectedCharacterIndexes = emptySet(),
                bgmImportResult = null,
                error = null
            )
        }
    }

    fun updateBgmSearchQuery(value: String) {
        _uiState.update { it.copy(bgmSearchQuery = value) }
    }

    fun updateBgmSubjectType(value: Int?) {
        _uiState.update { it.copy(bgmSubjectType = value) }
    }

    fun updateBgmCharacterKeyword(value: String) {
        _uiState.update { it.copy(bgmCharacterKeyword = value) }
    }

    fun searchBgmSubjects() {
        val keyword = _uiState.value.bgmSearchQuery.trim()
        if (keyword.isBlank()) {
            _uiState.update { it.copy(error = "请输入 IP 作品名称") }
            return
        }
        viewModelScope.launch {
            val subjectType = _uiState.value.bgmSubjectType
            _uiState.update { it.copy(bgmStep = BgmImportStep.Searching, error = null) }
            when (val result = repository.searchBgmSubjects(keyword, subjectType)) {
                is GoodsResult.Success -> _uiState.update {
                    it.copy(
                        bgmStep = if (result.data.subjects.isEmpty()) BgmImportStep.Search else BgmImportStep.Subjects,
                        bgmSubjects = result.data.subjects,
                        error = if (result.data.subjects.isEmpty()) "未找到相关作品" else null
                    )
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(bgmStep = BgmImportStep.Search, error = result.message)
                }
            }
        }
    }

    fun searchBgmCharactersDirect() {
        val keyword = _uiState.value.bgmSearchQuery.trim()
        if (keyword.isBlank()) {
            _uiState.update { it.copy(error = "请输入 IP 作品名称") }
            return
        }
        viewModelScope.launch {
            val subjectType = _uiState.value.bgmSubjectType
            _uiState.update { it.copy(bgmStep = BgmImportStep.LoadingCharacters, error = null) }
            when (val result = repository.searchBgmCharacters(keyword, subjectType)) {
                is GoodsResult.Success -> _uiState.update {
                    it.copy(
                        bgmStep = if (result.data.characters.isEmpty()) BgmImportStep.Search else BgmImportStep.Results,
                        bgmSelectedSubject = null,
                        bgmSubjectName = result.data.ipName,
                        bgmCharacters = result.data.characters,
                        bgmSelectedCharacterIndexes = emptySet(),
                        bgmCharacterKeyword = "",
                        error = if (result.data.characters.isEmpty()) "未找到角色" else null
                    )
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(bgmStep = BgmImportStep.Search, error = result.message)
                }
            }
        }
    }

    fun selectBgmSubject(subject: BgmSubject) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    bgmStep = BgmImportStep.LoadingCharacters,
                    bgmSelectedSubject = subject,
                    error = null
                )
            }
            when (val result = repository.getBgmCharactersBySubjectId(subject.id)) {
                is GoodsResult.Success -> _uiState.update {
                    it.copy(
                        bgmStep = BgmImportStep.Results,
                        bgmSubjectName = result.data.subjectName,
                        bgmCharacters = result.data.characters,
                        bgmSelectedCharacterIndexes = emptySet(),
                        bgmCharacterKeyword = ""
                    )
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(bgmStep = BgmImportStep.Subjects, error = result.message)
                }
            }
        }
    }

    fun toggleBgmCharacter(index: Int) {
        _uiState.update {
            val next = if (index in it.bgmSelectedCharacterIndexes) {
                it.bgmSelectedCharacterIndexes - index
            } else {
                it.bgmSelectedCharacterIndexes + index
            }
            it.copy(bgmSelectedCharacterIndexes = next)
        }
    }

    fun selectAllBgmCharacters() {
        _uiState.update {
            it.copy(bgmSelectedCharacterIndexes = it.bgmCharacters.indices.toSet())
        }
    }

    fun clearBgmCharacterSelection() {
        _uiState.update { it.copy(bgmSelectedCharacterIndexes = emptySet()) }
    }

    fun importSelectedBgmCharacters() {
        val state = _uiState.value
        val subjectType = state.bgmSubjectType ?: state.bgmSelectedSubject?.type
        val ipName = state.bgmSubjectName.ifBlank {
            state.bgmSelectedSubject?.nameCn?.takeIf { !it.isNullOrBlank() }
                ?: state.bgmSelectedSubject?.name
                ?: state.bgmSearchQuery.trim()
        }
        val characters = state.bgmSelectedCharacterIndexes
            .sorted()
            .mapNotNull { index -> state.bgmCharacters.getOrNull(index) }
            .map { character ->
                BgmCreateCharacterItem(
                    ipName = ipName,
                    characterName = character.name,
                    subjectType = subjectType,
                    avatar = character.avatar
                )
            }
        if (characters.isEmpty()) {
            _uiState.update { it.copy(error = "请至少选择一个角色") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(bgmStep = BgmImportStep.Importing, error = null) }
            when (val result = repository.createBgmCharacters(characters)) {
                is GoodsResult.Success -> {
                    _uiState.update {
                        it.copy(
                            bgmStep = BgmImportStep.Imported,
                            bgmImportResult = result.data
                        )
                    }
                    refresh()
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(bgmStep = BgmImportStep.Results, error = result.message)
                }
            }
        }
    }

    private fun <T> runMutation(block: suspend () -> GoodsResult<T>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val result = block()) {
                is GoodsResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    refresh()
                }
                is GoodsResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, error = result.message) }
                }
            }
        }
    }

    private suspend fun loadIPCharactersInternal(ip: IP) {
        _uiState.update {
            it.copy(
                selectedIpForCharacters = ip,
                isIpCharactersLoading = true,
                error = null
            )
        }
        when (val result = repository.getIPCharacters(ip.id)) {
            is GoodsResult.Success -> _uiState.update {
                it.copy(
                    selectedIpForCharacters = ip,
                    ipCharacters = result.data,
                    isIpCharactersLoading = false
                )
            }
            is GoodsResult.Error -> _uiState.update {
                it.copy(
                    isIpCharactersLoading = false,
                    error = result.message
                )
            }
        }
    }

    private suspend fun uploadThemeImagesIfNeeded(
        theme: Theme,
        imageUris: List<String>,
        imageLabel: String?
    ): GoodsResult<Theme>? {
        if (imageUris.isEmpty()) return null
        _uiState.update { it.copy(isUploadingThemeImages = true) }
        return runCatching {
            val files = imageUris.map { uriString ->
                ImageUploadUtils.compressImageUri(context, Uri.parse(uriString))
            }
            repository.uploadThemeImages(theme.id, files, imageLabel)
        }.getOrElse { throwable ->
            GoodsResult.Error(throwable.message ?: "图片处理失败")
        }
    }
}
