package com.pickgoods.app.ui.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pickgoods.app.data.model.StorageNode
import com.pickgoods.app.ui.common.AddButton
import com.pickgoods.app.ui.common.DeleteConfirmDialog
import com.pickgoods.app.ui.common.EmptyMessage
import com.pickgoods.app.ui.common.ErrorMessage
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsTopBar
import com.pickgoods.app.ui.common.SimpleListCard
import com.pickgoods.app.ui.goods.components.GoodsCard

@Composable
fun LocationScreen(
    onSettingsClick: () -> Unit,
    viewModel: LocationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            PickGoodsTopBar(
                title = "位置管理",
                onSettingsClick = onSettingsClick,
                onRefreshClick = viewModel::refresh
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PickGoodsScreen(modifier = Modifier.padding(paddingValues)) {
            LocationContent(
                state = state,
                onSelect = viewModel::selectNode,
                onIncludeChildrenChanged = viewModel::setIncludeChildren,
                onSave = viewModel::saveNode,
                onDelete = viewModel::deleteNode
            )
        }
    }
}

@Composable
private fun LocationContent(
    state: LocationUiState,
    onSelect: (StorageNode) -> Unit,
    onIncludeChildrenChanged: (Boolean) -> Unit,
    onSave: (StorageNode?, String, Int?, String?, Int?) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingNode by remember { mutableStateOf<StorageNode?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<StorageNode?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("收纳位置", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("树形管理盒子、柜层、抽屉与展示位", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                AddButton(onClick = { showCreateDialog = true })
            }
        }

        if (state.isLoading && state.nodes.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (state.error != null && state.nodes.isEmpty()) {
            item { ErrorMessage(state.error) }
        } else if (state.nodes.isEmpty()) {
            item { EmptyMessage("暂无位置节点") }
        } else {
            items(state.nodes, key = { it.id }) { node ->
                LocationNodeRow(
                    node = node,
                    selected = state.selectedNode?.id == node.id,
                    onClick = { onSelect(node) },
                    onEdit = { editingNode = node },
                    onDelete = { deleteTarget = node }
                )
            }
        }

        state.selectedNode?.let { selected ->
            item {
                LocationDetailCard(
                    node = selected,
                    includeChildren = state.includeChildren,
                    isGoodsLoading = state.isGoodsLoading,
                    goodsCount = state.goods.size,
                    onIncludeChildrenChanged = onIncludeChildrenChanged
                )
            }
            if (state.isGoodsLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (state.goods.isEmpty()) {
                item { EmptyMessage("这个位置下还没有谷子") }
            } else {
                items(state.goods, key = { it.id }) { goods ->
                    GoodsCard(goods = goods, baseUrl = state.baseUrl, onClick = {})
                }
            }
        }
    }

    if (showCreateDialog || editingNode != null) {
        LocationEditDialog(
            node = editingNode,
            nodes = state.nodes,
            onDismiss = {
                showCreateDialog = false
                editingNode = null
            },
            onConfirm = { name, parent, description, order ->
                onSave(editingNode, name, parent, description, order)
                showCreateDialog = false
                editingNode = null
            }
        )
    }

    deleteTarget?.let { node ->
        DeleteConfirmDialog(
            title = "删除位置",
            text = "确定删除「${node.name}」吗？",
            onDismiss = { deleteTarget = null },
            onConfirm = {
                onDelete(node.id)
                deleteTarget = null
            }
        )
    }
}

@Composable
private fun LocationNodeRow(
    node: StorageNode,
    selected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    SimpleListCard(
        title = node.name,
        subtitle = node.pathName ?: node.description,
        meta = if (selected) "已选" else null,
        onClick = onClick,
        onEdit = onEdit,
        onDelete = onDelete,
        leading = {
            Spacer(modifier = Modifier.width((nodeDepth(node) * 12).dp))
            Text("位", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    )
}

@Composable
private fun LocationDetailCard(
    node: StorageNode,
    includeChildren: Boolean,
    isGoodsLoading: Boolean,
    goodsCount: Int,
    onIncludeChildrenChanged: (Boolean) -> Unit
) {
    PickGoodsCard(radius = 18.dp) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(node.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(node.pathName ?: node.name, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!node.description.isNullOrBlank()) {
                Text(node.description, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = includeChildren,
                    onClick = { onIncludeChildrenChanged(!includeChildren) },
                    label = { Text(if (includeChildren) "含子节点" else "仅当前节点") }
                )
                Text(
                    if (isGoodsLoading) "加载中..." else "谷子 $goodsCount 个",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun LocationEditDialog(
    node: StorageNode?,
    nodes: List<StorageNode>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int?, String?, Int?) -> Unit
) {
    var name by remember(node?.id) { mutableStateOf(node?.name.orEmpty()) }
    var description by remember(node?.id) { mutableStateOf(node?.description.orEmpty()) }
    var parentText by remember(node?.id) { mutableStateOf(node?.parent?.toString().orEmpty()) }
    var orderText by remember(node?.id) { mutableStateOf(node?.order?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (node == null) "新增位置" else "编辑位置") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("位置名称") }, singleLine = true)
                OutlinedTextField(
                    value = parentText,
                    onValueChange = { parentText = it.filter(Char::isDigit) },
                    label = { Text("父节点 ID（可空）") },
                    supportingText = {
                        val candidates = nodes.take(4).joinToString(" / ") { "${it.id}:${it.name}" }
                        if (candidates.isNotBlank()) Text("可选：$candidates")
                    },
                    singleLine = true
                )
                OutlinedTextField(value = orderText, onValueChange = { orderText = it.filter(Char::isDigit) }, label = { Text("排序") }, singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("备注") }, minLines = 3)
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onConfirm(
                        name.trim(),
                        parentText.toIntOrNull(),
                        description.trim(),
                        orderText.toIntOrNull()
                    )
                }
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

private fun nodeDepth(node: StorageNode): Int = node.pathName?.count { it == '/' } ?: 0
