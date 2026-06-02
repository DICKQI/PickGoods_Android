package com.pickgoods.app.ui.goods

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.pickgoods.app.data.local.TokenManager
import com.pickgoods.app.data.model.Category
import com.pickgoods.app.data.model.Character
import com.pickgoods.app.data.model.GoodsCreateRequest
import com.pickgoods.app.data.model.GoodsDetail
import com.pickgoods.app.data.model.GoodsDuplicateCandidate
import com.pickgoods.app.data.model.GoodsDuplicateConflictResponse
import com.pickgoods.app.data.model.GuziImage
import com.pickgoods.app.data.model.IP
import com.pickgoods.app.data.model.StorageNode
import com.pickgoods.app.data.model.Theme
import com.pickgoods.app.data.repository.GoodsRepository
import com.pickgoods.app.data.repository.GoodsResult
import com.pickgoods.app.data.repository.LocationRepository
import com.pickgoods.app.data.repository.MetadataRepository
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

data class GoodsFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val error: String? = null,
    val savedGoodsId: String? = null,
    val ips: List<IP> = emptyList(),
    val characters: List<Character> = emptyList(),
    val categories: List<Category> = emptyList(),
    val themes: List<Theme> = emptyList(),
    val locations: List<StorageNode> = emptyList(),
    val baseUrl: String = TokenManager.DEFAULT_BASE_URL,
    val name: String = "",
    val ipId: Int? = null,
    val characterIds: Set<Int> = emptySet(),
    val categoryId: Int? = null,
    val themeId: Int? = null,
    val locationId: Int? = null,
    val status: String = "in_cabinet",
    val quantity: String = "1",
    val price: String = "",
    val purchaseDate: String = "",
    val isOfficial: Boolean = true,
    val notes: String = "",
    val currentMainPhoto: String? = null,
    val selectedMainPhotoUri: String? = null,
    val currentAdditionalPhotos: List<GuziImage> = emptyList(),
    val selectedAdditionalPhotoUris: List<String> = emptyList(),
    val additionalPhotoLabel: String = "",
    val isUploadingAdditionalPhotos: Boolean = false,
    val duplicateMessage: String? = null,
    val duplicateCandidates: List<GoodsDuplicateCandidate> = emptyList(),
    val selectedDuplicateId: String? = null
)

@HiltViewModel
class GoodsFormViewModel @Inject constructor(
    private val goodsRepository: GoodsRepository,
    private val metadataRepository: MetadataRepository,
    private val locationRepository: LocationRepository,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(GoodsFormUiState())
    val uiState: StateFlow<GoodsFormUiState> = _uiState.asStateFlow()

    private var editingId: String? = null
    private var pendingCreateRequest: GoodsCreateRequest? = null
    private val gson = Gson()

    fun load(goodsId: String?) {
        if (_uiState.value.ips.isNotEmpty() && editingId == goodsId) return
        editingId = goodsId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val ipsDeferred = async { metadataRepository.getIPs() }
            val charactersDeferred = async { metadataRepository.getCharacters() }
            val categoriesDeferred = async { metadataRepository.getCategoryTree() }
            val themesDeferred = async { metadataRepository.getThemes() }
            val locationsDeferred = async { locationRepository.getNodes() }
            val baseUrlDeferred = async { tokenManager.getBaseUrl() }
            val detailDeferred = goodsId?.let { async { goodsRepository.getDetail(it) } }

            val ips = ipsDeferred.await()
            val characters = charactersDeferred.await()
            val categories = categoriesDeferred.await()
            val themes = themesDeferred.await()
            val locations = locationsDeferred.await()
            val baseUrl = baseUrlDeferred.await()
            val detail = detailDeferred?.await()

            val firstError = listOfNotNull<GoodsResult<*>>(ips, characters, categories, themes, locations, detail)
                .filterIsInstance<GoodsResult.Error>()
                .firstOrNull()

            _uiState.update { current ->
                val newState = current.copy(
                    isLoading = false,
                    error = firstError?.message,
                    ips = if (ips is GoodsResult.Success) ips.data else current.ips,
                    characters = if (characters is GoodsResult.Success) characters.data else current.characters,
                    categories = if (categories is GoodsResult.Success) categories.data else current.categories,
                    themes = if (themes is GoodsResult.Success) themes.data else current.themes,
                    locations = if (locations is GoodsResult.Success) locations.data else current.locations,
                    baseUrl = baseUrl
                )
                if (detail is GoodsResult.Success) {
                    val goods = detail.data
                    newState.copy(
                        name = goods.name,
                        ipId = goods.ip.id,
                        characterIds = goods.characters.map { it.id }.toSet(),
                        categoryId = goods.category.id,
                        themeId = goods.theme?.id,
                        locationId = goods.location,
                        status = goods.status,
                        quantity = goods.quantity.toString(),
                        price = goods.price.orEmpty(),
                        purchaseDate = goods.purchaseDate.orEmpty(),
                        isOfficial = goods.isOfficial,
                        notes = goods.notes.orEmpty(),
                        currentMainPhoto = goods.mainPhoto,
                        selectedMainPhotoUri = null,
                        currentAdditionalPhotos = goods.additionalPhotos,
                        selectedAdditionalPhotoUris = emptyList(),
                        additionalPhotoLabel = ""
                    )
                } else {
                    newState
                }
            }
        }
    }

    fun updateName(value: String) = update { it.copy(name = value) }
    fun updateIp(id: Int?) {
        update { it.copy(ipId = id, characterIds = emptySet()) }
        id?.let(::loadCharactersForIp)
    }
    fun toggleCharacter(id: Int) = update {
        val next = if (id in it.characterIds) it.characterIds - id else it.characterIds + id
        it.copy(characterIds = next)
    }
    fun updateCategory(id: Int?) = update { it.copy(categoryId = id) }
    fun updateTheme(id: Int?) = update { it.copy(themeId = id) }
    fun updateLocation(id: Int?) = update { it.copy(locationId = id) }
    fun updateStatus(value: String) = update { it.copy(status = value) }
    fun updateQuantity(value: String) = update { it.copy(quantity = value.filter(Char::isDigit).ifBlank { "1" }) }
    fun updatePrice(value: String) = update { it.copy(price = value) }
    fun updatePurchaseDate(value: String) = update { it.copy(purchaseDate = value) }
    fun updateIsOfficial(value: Boolean) = update { it.copy(isOfficial = value) }
    fun updateNotes(value: String) = update { it.copy(notes = value) }
    fun updateMainPhotoUri(value: String?) = update { it.copy(selectedMainPhotoUri = value) }
    fun addAdditionalPhotoUris(values: List<String>) = update {
        it.copy(selectedAdditionalPhotoUris = (it.selectedAdditionalPhotoUris + values).distinct())
    }
    fun removeAdditionalPhotoUri(value: String) = update {
        it.copy(selectedAdditionalPhotoUris = it.selectedAdditionalPhotoUris.filterNot { uri -> uri == value })
    }
    fun updateAdditionalPhotoLabel(value: String) = update { it.copy(additionalPhotoLabel = value) }
    fun selectDuplicateCandidate(id: String) = update { it.copy(selectedDuplicateId = id) }

    fun deleteAdditionalPhoto(photoId: Int) {
        val goodsId = editingId ?: _uiState.value.savedGoodsId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAdditionalPhotos = true, error = null) }
            when (val result = goodsRepository.deleteAdditionalPhoto(goodsId, photoId)) {
                is GoodsResult.Success -> _uiState.update {
                    it.copy(
                        isUploadingAdditionalPhotos = false,
                        currentMainPhoto = result.data.mainPhoto,
                        currentAdditionalPhotos = result.data.additionalPhotos
                    )
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isUploadingAdditionalPhotos = false, error = result.message)
                }
            }
        }
    }

    fun deleteAdditionalPhotos(photoIds: Set<Int>) {
        val goodsId = editingId ?: _uiState.value.savedGoodsId ?: return
        if (photoIds.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAdditionalPhotos = true, error = null) }
            when (val result = goodsRepository.deleteAdditionalPhotos(goodsId, photoIds.toList())) {
                is GoodsResult.Success -> _uiState.update {
                    it.copy(
                        isUploadingAdditionalPhotos = false,
                        currentMainPhoto = result.data.mainPhoto,
                        currentAdditionalPhotos = result.data.additionalPhotos
                    )
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isUploadingAdditionalPhotos = false, error = result.message)
                }
            }
        }
    }

    fun updateExistingAdditionalPhotoLabel(photoId: Int, label: String) {
        val goodsId = editingId ?: _uiState.value.savedGoodsId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAdditionalPhotos = true, error = null) }
            when (val result = goodsRepository.updateAdditionalPhotoLabel(goodsId, listOf(photoId), label)) {
                is GoodsResult.Success -> _uiState.update {
                    it.copy(
                        isUploadingAdditionalPhotos = false,
                        currentMainPhoto = result.data.mainPhoto,
                        currentAdditionalPhotos = result.data.additionalPhotos
                    )
                }
                is GoodsResult.Error -> _uiState.update {
                    it.copy(isUploadingAdditionalPhotos = false, error = result.message)
                }
            }
        }
    }

    fun dismissDuplicateDialog() {
        pendingCreateRequest = null
        update {
            it.copy(
                duplicateMessage = null,
                duplicateCandidates = emptyList(),
                selectedDuplicateId = null
            )
        }
    }

    fun save() {
        saveWithStatus(_uiState.value.status)
    }

    fun saveAsDraft() {
        saveWithStatus("draft", syncStatusToUi = true)
    }

    fun publish() {
        val currentStatus = _uiState.value.status
        val publishStatus = if (currentStatus == "draft") "in_cabinet" else currentStatus
        saveWithStatus(publishStatus, syncStatusToUi = currentStatus != publishStatus)
    }

    private fun saveWithStatus(targetStatus: String, syncStatusToUi: Boolean = false) {
        val state = _uiState.value
        val ipId = state.ipId
        val categoryId = state.categoryId
        if (state.name.isBlank() || ipId == null || categoryId == null ||
            (targetStatus != "draft" && state.characterIds.isEmpty())
        ) {
            _uiState.update {
                it.copy(
                    error = if (targetStatus == "draft") {
                        "草稿至少需要填写名称、IP 和品类"
                    } else {
                        "发布前请填写名称、IP、品类，并至少选择一个角色"
                    }
                )
            }
            return
        }

        if (syncStatusToUi) {
            _uiState.update { it.copy(status = targetStatus) }
        }

        val request = GoodsCreateRequest(
            name = state.name.trim(),
            ipId = ipId,
            characterIds = state.characterIds.toList(),
            categoryId = categoryId,
            themeId = state.themeId,
            location = state.locationId,
            status = targetStatus,
            quantity = state.quantity.toIntOrNull() ?: 1,
            price = state.price.ifBlank { null },
            purchaseDate = state.purchaseDate.ifBlank { null },
            isOfficial = state.isOfficial,
            notes = state.notes.ifBlank { null }
        )

        submit(request)
    }

    fun createAsNewAfterDuplicate() {
        val request = pendingCreateRequest ?: return
        submit(request.copy(mergeStrategy = "new"))
    }

    fun mergeDuplicate() {
        val request = pendingCreateRequest ?: return
        val targetId = _uiState.value.selectedDuplicateId ?: return
        submit(
            request.copy(
                mergeStrategy = "merge",
                mergeTargetId = targetId
            )
        )
    }

    private fun submit(request: GoodsCreateRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = editingId?.let { goodsRepository.updateGoods(it, request) }
                ?: goodsRepository.createGoods(request)
            when (result) {
                is GoodsResult.Success -> {
                    pendingCreateRequest = null
                    editingId = result.data.id
                    val photoUploadResult = uploadSelectedMainPhotoIfNeeded(result.data.id)
                    val additionalUploadResult = uploadSelectedAdditionalPhotosIfNeeded(result.data.id)
                    _uiState.update {
                        val uploadError = when {
                            photoUploadResult is GoodsResult.Error -> "主图上传失败：${photoUploadResult.message}"
                            additionalUploadResult is GoodsResult.Error -> "附加图片上传失败：${additionalUploadResult.message}"
                            else -> null
                        }
                        val latestGoods = (additionalUploadResult as? GoodsResult.Success)?.data
                            ?: (photoUploadResult as? GoodsResult.Success)?.data
                            ?: result.data

                        it.copy(
                            isSaving = false,
                            isUploadingPhoto = false,
                            isUploadingAdditionalPhotos = false,
                            savedGoodsId = if (uploadError == null) result.data.id else null,
                            error = uploadError?.let { message -> "谷子已保存，但$message" },
                            currentMainPhoto = latestGoods.mainPhoto,
                            selectedMainPhotoUri = null,
                            currentAdditionalPhotos = latestGoods.additionalPhotos,
                            selectedAdditionalPhotoUris = if (additionalUploadResult is GoodsResult.Error) {
                                it.selectedAdditionalPhotoUris
                            } else {
                                emptyList()
                            },
                            additionalPhotoLabel = if (additionalUploadResult is GoodsResult.Error) {
                                it.additionalPhotoLabel
                            } else {
                                ""
                            },
                            duplicateMessage = null,
                            duplicateCandidates = emptyList(),
                            selectedDuplicateId = null
                        )
                    }
                }
                is GoodsResult.Error -> _uiState.update {
                    val conflict = parseDuplicateConflict(result)
                    if (conflict != null && editingId == null) {
                        pendingCreateRequest = request
                        it.copy(
                            isSaving = false,
                            error = null,
                            duplicateMessage = conflict.detail ?: "检测到可能重复的谷子",
                            duplicateCandidates = conflict.candidates.orEmpty(),
                            selectedDuplicateId = null
                        )
                    } else {
                        it.copy(isSaving = false, error = result.message)
                    }
                }
            }
        }
    }

    private suspend fun uploadSelectedMainPhotoIfNeeded(goodsId: String): GoodsResult<GoodsDetail>? {
        val uriString = _uiState.value.selectedMainPhotoUri ?: return null
        _uiState.update { it.copy(isUploadingPhoto = true) }
        return runCatching {
            val file = ImageUploadUtils.compressImageUri(context, Uri.parse(uriString))
            goodsRepository.uploadMainPhoto(goodsId, file)
        }.getOrElse { throwable ->
            GoodsResult.Error(throwable.message ?: "图片处理失败")
        }
    }

    private suspend fun uploadSelectedAdditionalPhotosIfNeeded(goodsId: String): GoodsResult<GoodsDetail>? {
        val uriStrings = _uiState.value.selectedAdditionalPhotoUris
        if (uriStrings.isEmpty()) return null
        _uiState.update { it.copy(isUploadingAdditionalPhotos = true) }
        return runCatching {
            val files = uriStrings.map { uriString ->
                ImageUploadUtils.compressImageUri(context, Uri.parse(uriString))
            }
            goodsRepository.uploadAdditionalPhotos(
                id = goodsId,
                imageFiles = files,
                label = _uiState.value.additionalPhotoLabel
            )
        }.getOrElse { throwable ->
            GoodsResult.Error(throwable.message ?: "图片处理失败")
        }
    }

    private fun parseDuplicateConflict(error: GoodsResult.Error): GoodsDuplicateConflictResponse? {
        if (error.code != 409 || error.rawBody.isNullOrBlank()) return null
        return runCatching {
            gson.fromJson(error.rawBody, GoodsDuplicateConflictResponse::class.java)
        }.getOrNull()?.takeIf {
            it.code == "goods_duplicate" && !it.candidates.isNullOrEmpty()
        }
    }

    private fun update(block: (GoodsFormUiState) -> GoodsFormUiState) {
        _uiState.update(block)
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
                is GoodsResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }
}
