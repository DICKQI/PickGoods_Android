package com.pickgoods.app.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickgoods.app.data.local.TokenManager
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.data.model.StorageNode
import com.pickgoods.app.data.model.StorageNodeRequest
import com.pickgoods.app.data.repository.GoodsResult
import com.pickgoods.app.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val tokenManager: TokenManager
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
            when (val result = repository.getNodes()) {
                is GoodsResult.Success -> {
                    val selected = _uiState.value.selectedNode
                    val newSelected = selected?.let { old -> result.data.firstOrNull { it.id == old.id } }
                        ?: result.data.firstOrNull()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            nodes = result.data.sortedWith(compareBy<StorageNode> { nodeDepth(it) }.thenBy { nodePath(it) }),
                            selectedNode = newSelected
                        )
                    }
                    newSelected?.let { loadGoods(it.id) }
                }
                is GoodsResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
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

    fun saveNode(existing: StorageNode?, name: String, parent: Int?, description: String?, order: Int?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val request = StorageNodeRequest(
                name = name,
                parent = parent,
                description = description?.ifBlank { null },
                order = order
            )
            val result = if (existing == null) {
                repository.createNode(request)
            } else {
                repository.patchNode(existing.id, request)
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

private fun nodeDepth(node: StorageNode): Int = node.pathName?.count { it == '/' } ?: 0

private fun nodePath(node: StorageNode): String = node.pathName ?: node.name
