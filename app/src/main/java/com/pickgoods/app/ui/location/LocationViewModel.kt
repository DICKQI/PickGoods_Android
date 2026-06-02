package com.pickgoods.app.ui.location

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickgoods.app.data.local.TokenManager
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.data.model.StorageNode
import com.pickgoods.app.data.model.StorageNodeRequest
import com.pickgoods.app.data.repository.GoodsResult
import com.pickgoods.app.data.repository.LocationRepository
import com.pickgoods.app.data.util.ImageUploadUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocationUiState(
    val isLoading: Boolean = false,
    val isGoodsLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val nodes: List<StorageNode> = emptyList(),
    val selectedNode: StorageNode? = null,
    val goods: List<GoodsListItem> = emptyList(),
    val includeChildren: Boolean = false,
    val baseUrl: String = TokenManager.DEFAULT_BASE_URL
)

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val repository: LocationRepository,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(baseUrl = tokenManager.getBaseUrl()) }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val treeResult = repository.getTree()
            val nodesResult = repository.getNodes()
            val treeNodes = when (treeResult) {
                is GoodsResult.Success -> treeResult.data
                is GoodsResult.Error -> null
            }
            val detailNodes = when (nodesResult) {
                is GoodsResult.Success -> nodesResult.data
                is GoodsResult.Error -> null
            }
            val mergedNodes = mergeLocationNodes(treeNodes, detailNodes)

            if (mergedNodes.isNotEmpty()) {
                val sortedNodes = mergedNodes.sortedWith(compareBy<StorageNode> { nodeDepth(it) }.thenBy { nodePath(it) }.thenBy { it.order })
                val selected = _uiState.value.selectedNode
                val newSelected = selected?.let { old -> sortedNodes.firstOrNull { it.id == old.id } }
                    ?: sortedNodes.firstOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        nodes = sortedNodes,
                        selectedNode = newSelected
                    )
                }
                newSelected?.let { loadGoods(it.id) }
                return@launch
            }

            val message = when (treeResult) {
                is GoodsResult.Error -> treeResult.message
                is GoodsResult.Success -> null
            } ?: when (nodesResult) {
                is GoodsResult.Error -> nodesResult.message
                is GoodsResult.Success -> null
            } ?: "位置数据加载失败"
            _uiState.update { it.copy(isLoading = false, error = message) }
        }
    }

    fun selectNode(node: StorageNode) {
        _uiState.update { it.copy(selectedNode = node) }
        loadGoods(node.id)
    }

    fun setIncludeChildren(include: Boolean) {
        _uiState.update { it.copy(includeChildren = include) }
        _uiState.value.selectedNode?.let { loadGoods(it.id) }
    }

    fun saveNode(
        existing: StorageNode?,
        name: String,
        parent: Int?,
        description: String?,
        order: Int?,
        imageUri: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val request = StorageNodeRequest(
                name = name,
                parent = parent,
                description = description?.ifBlank { null },
                order = order
            )
            val imageFile = imageUri?.let { uri ->
                runCatching {
                    ImageUploadUtils.compressImageUri(context, Uri.parse(uri))
                }.getOrElse { throwable ->
                    _uiState.update { it.copy(isSaving = false, error = throwable.message ?: "位置图片处理失败") }
                    return@launch
                }
            }
            val result = when {
                existing == null && imageFile != null -> repository.createNodeWithImage(request, imageFile)
                existing != null && imageFile != null -> {
                    when (val imageResult = repository.patchNodeWithImage(existing.id, request, imageFile)) {
                        is GoodsResult.Success -> {
                            if (parent == null && existing.parent != null) {
                                repository.patchNode(existing.id, request)
                            } else {
                                imageResult
                            }
                        }
                        is GoodsResult.Error -> imageResult
                    }
                }
                existing == null -> repository.createNode(request)
                else -> repository.patchNode(existing.id, request)
            }
            when (result) {
                is GoodsResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, selectedNode = result.data) }
                    refresh()
                }
                is GoodsResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, error = result.message) }
                }
            }
        }
    }

    fun deleteNode(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val result = repository.deleteNode(id)) {
                is GoodsResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, selectedNode = null, goods = emptyList()) }
                    refresh()
                }
                is GoodsResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, error = result.message) }
                }
            }
        }
    }

    private fun loadGoods(nodeId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoodsLoading = true) }
            when (val result = repository.getNodeGoods(nodeId, _uiState.value.includeChildren)) {
                is GoodsResult.Success -> {
                    _uiState.update { it.copy(isGoodsLoading = false, goods = result.data.results) }
                }
                is GoodsResult.Error -> {
                    _uiState.update { it.copy(isGoodsLoading = false, error = result.message) }
                }
            }
        }
    }
}

private fun mergeLocationNodes(
    treeNodes: List<StorageNode>?,
    detailNodes: List<StorageNode>?
): List<StorageNode> {
    val detailById = detailNodes.orEmpty().associateBy { it.id }
    val treeById = treeNodes.orEmpty().associateBy { it.id }
    val orderedIds = buildList {
        treeNodes.orEmpty().forEach { add(it.id) }
        detailNodes.orEmpty().forEach { node ->
            if (node.id !in treeById) add(node.id)
        }
    }
    val sourceIds = orderedIds.ifEmpty { detailNodes.orEmpty().map { it.id } }
    return sourceIds.mapNotNull { id ->
        val tree = treeById[id]
        val detail = detailById[id]
        when {
            tree != null && detail != null -> tree.copy(
                image = detail.image ?: tree.image,
                description = detail.description ?: tree.description
            )
            tree != null -> tree
            else -> detail
        }
    }
}

private fun nodeDepth(node: StorageNode): Int = node.pathName?.count { it == '/' } ?: 0

private fun nodePath(node: StorageNode): String = node.pathName ?: node.name
