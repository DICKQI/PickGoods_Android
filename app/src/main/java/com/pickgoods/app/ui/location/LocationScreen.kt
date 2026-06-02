package com.pickgoods.app.ui.location

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.pickgoods.app.data.model.StorageNode
import com.pickgoods.app.data.util.ImageCaptureUtils
import com.pickgoods.app.ui.common.ChoiceChipFlow
import com.pickgoods.app.ui.common.CompactActionButton
import com.pickgoods.app.ui.common.DeleteConfirmDialog
import com.pickgoods.app.ui.common.EmptyMessage
import com.pickgoods.app.ui.common.ErrorMessage
import com.pickgoods.app.ui.common.MobileFormSheet
import com.pickgoods.app.ui.common.MobileHeaderCard
import com.pickgoods.app.ui.common.MobileInfoTile
import com.pickgoods.app.ui.common.MobileSectionHeader
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.common.PickGoodsTopBar
import com.pickgoods.app.ui.common.SimpleListCard
import com.pickgoods.app.ui.goods.components.GoodsCard
import com.pickgoods.app.ui.goods.components.resolveImageUrl
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.GoldSoft
import com.pickgoods.app.ui.theme.PurpleSecondary
import com.pickgoods.app.ui.theme.PurpleSoft
import com.pickgoods.app.ui.theme.TextLighter

@Composable
fun LocationScreen(
    onSettingsClick: () -> Unit,
    onGoodsClick: (String) -> Unit = {},
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
                onDelete = viewModel::deleteNode,
                onGoodsClick = onGoodsClick
            )
        }
    }
}

@Composable
private fun LocationContent(
    state: LocationUiState,
    onSelect: (StorageNode) -> Unit,
    onIncludeChildrenChanged: (Boolean) -> Unit,
    onSave: (StorageNode?, String, Int?, String?, Int?, String?) -> Unit,
    onDelete: (Int) -> Unit,
    onGoodsClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingNode by remember { mutableStateOf<StorageNode?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<StorageNode?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            MobileHeaderCard(
                title = "收纳位置",
                subtitle = "盒子、柜层、抽屉与展示位",
                trailing = {
                    CompactActionButton(label = "新增", onClick = { showCreateDialog = true })
                }
            ) {
                state.selectedNode?.let { selected ->
                    Text(
                        text = "当前：${selected.pathName ?: selected.name}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MobileInfoTile(
                    label = "节点",
                    value = state.nodes.size.toString(),
                    subtitle = "位置层级",
                    accent = Gold,
                    modifier = Modifier.weight(1f)
                )
                MobileInfoTile(
                    label = "当前",
                    value = state.goods.size.toString(),
                    subtitle = state.selectedNode?.name ?: "未选择",
                    accent = PurpleSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (state.nodes.isNotEmpty()) {
            item {
                LocationQuickRail(
                    nodes = state.nodes,
                    selectedId = state.selectedNode?.id,
                    baseUrl = state.baseUrl,
                    onSelect = onSelect
                )
            }
        }

        state.selectedNode?.let { selected ->
            item {
                LocationDetailCard(
                    node = selected,
                    baseUrl = state.baseUrl,
                    includeChildren = state.includeChildren,
                    isGoodsLoading = state.isGoodsLoading,
                    goodsCount = state.goods.size,
                    onIncludeChildrenChanged = onIncludeChildrenChanged
                )
            }
            item {
                MobileSectionHeader(
                    title = "此处谷子",
                    subtitle = if (state.includeChildren) "包含子节点 · ${state.goods.size} 件" else "当前节点 · ${state.goods.size} 件",
                    accent = Gold
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
                    GoodsCard(
                        goods = goods,
                        baseUrl = state.baseUrl,
                        onClick = { onGoodsClick(goods.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            MobileSectionHeader(
                title = "位置节点",
                subtitle = "点选位置即可查看其中谷子",
                accent = PurpleSecondary
            )
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
                    baseUrl = state.baseUrl,
                    selected = state.selectedNode?.id == node.id,
                    onClick = { onSelect(node) },
                    onEdit = { editingNode = node },
                    onDelete = { deleteTarget = node }
                )
            }
        }
    }

    if (showCreateDialog || editingNode != null) {
        LocationEditDialog(
            node = editingNode,
            nodes = state.nodes,
            baseUrl = state.baseUrl,
            onDismiss = {
                showCreateDialog = false
                editingNode = null
            },
            onConfirm = { name, parent, description, order, imageUri ->
                onSave(editingNode, name, parent, description, order, imageUri)
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
private fun LocationQuickRail(
    nodes: List<StorageNode>,
    selectedId: Int?,
    baseUrl: String,
    onSelect: (StorageNode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MobileSectionHeader(
            title = "快速定位",
            subtitle = "常用位置横向浏览，点一下直接查看",
            accent = Gold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(
                nodes.sortedWith(compareBy<StorageNode> { nodeDepth(it) }.thenBy { it.order }.thenBy { it.name })
                    .take(18),
                key = { it.id }
            ) { node ->
                LocationQuickCard(
                    node = node,
                    selected = node.id == selectedId,
                    baseUrl = baseUrl,
                    onClick = { onSelect(node) }
                )
            }
        }
    }
}

@Composable
private fun LocationQuickCard(
    node: StorageNode,
    selected: Boolean,
    baseUrl: String,
    onClick: () -> Unit
) {
    PickGoodsCard(
        modifier = Modifier.width(196.dp),
        radius = 16.dp,
        borderColor = if (selected) Gold.copy(alpha = 0.72f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(9.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
                contentAlignment = Alignment.Center
            ) {
                val image = resolveImageUrl(node.image, baseUrl)
                if (image != null) {
                    AsyncImage(
                        model = image,
                        contentDescription = node.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("位", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = node.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = node.pathName ?: "顶级位置",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LocationNodeRow(
    node: StorageNode,
    baseUrl: String,
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
            LocationNodeThumb(node = node, baseUrl = baseUrl)
        }
    )
}

@Composable
private fun LocationNodeThumb(node: StorageNode, baseUrl: String) {
    val image = resolveImageUrl(node.image, baseUrl)
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
        contentAlignment = Alignment.Center
    ) {
        if (image != null) {
            AsyncImage(
                model = image,
                contentDescription = node.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text("位", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LocationDetailCard(
    node: StorageNode,
    baseUrl: String = "",
    includeChildren: Boolean,
    isGoodsLoading: Boolean,
    goodsCount: Int,
    onIncludeChildrenChanged: (Boolean) -> Unit
) {
    val image = resolveImageUrl(node.image, baseUrl)
    PickGoodsCard(radius = 16.dp) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(248.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
                contentAlignment = Alignment.Center
            ) {
                if (image != null) {
                    AsyncImage(
                        model = image,
                        contentDescription = node.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("位", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    node.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    node.pathName ?: node.name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!node.description.isNullOrBlank()) {
                    Text(
                        node.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = includeChildren,
                        onClick = { onIncludeChildrenChanged(!includeChildren) },
                        label = { Text(if (includeChildren) "含子节点" else "仅当前") }
                    )
                    Text(
                        if (isGoodsLoading) "加载中..." else "$goodsCount 件",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationEditDialog(
    node: StorageNode?,
    nodes: List<StorageNode>,
    baseUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Int?, String?, Int?, String?) -> Unit
) {
    var name by remember(node?.id) { mutableStateOf(node?.name.orEmpty()) }
    var description by remember(node?.id) { mutableStateOf(node?.description.orEmpty()) }
    var parentId by remember(node?.id) { mutableStateOf(node?.parent) }
    var orderText by remember(node?.id) { mutableStateOf(node?.order?.toString() ?: "0") }
    var selectedImageUri by remember(node?.id) { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember(node?.id) { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri?.toString()
    }
    val imageCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { selectedImageUri = it.toString() }
        }
        pendingCameraUri = null
    }
    val parentOptions = remember(nodes, node?.id) {
        nodes
            .filter { it.id != node?.id }
            .sortedWith(compareBy<StorageNode> { nodeDepth(it) }.thenBy { it.order }.thenBy { it.name })
    }
    val selectedParent = parentOptions.firstOrNull { it.id == parentId }

    MobileFormSheet(
        title = if (node == null) "新增位置" else "编辑位置",
        subtitle = "用父级位置组织柜子、盒子和展示位",
        confirmEnabled = name.isNotBlank(),
        onDismiss = onDismiss,
        onConfirm = {
            onConfirm(
                name.trim(),
                parentId,
                description.trim(),
                orderText.toIntOrNull(),
                selectedImageUri
            )
        }
    ) {
        LocationImagePicker(
            currentImage = resolveImageUrl(node?.image, baseUrl)?.toString(),
            selectedImageUri = selectedImageUri,
            onPick = {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onTakePhoto = {
                val uri = ImageCaptureUtils.createCaptureUri(context)
                pendingCameraUri = uri
                imageCameraLauncher.launch(uri)
            },
            onClearSelected = { selectedImageUri = null }
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("位置名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = PickGoodsShape.Control
        )
        ChoiceChipFlow(
            title = "父级位置",
            options = parentOptions,
            selected = selectedParent,
            label = { it.pathName ?: it.name },
            onSelected = { parentId = it?.id },
            emptyLabel = "顶级位置"
        )
        OutlinedTextField(
            value = orderText,
            onValueChange = { orderText = it.filter(Char::isDigit) },
            label = { Text("排序") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = PickGoodsShape.Control
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("备注") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = PickGoodsShape.Control
        )
    }
}

@Composable
private fun LocationImagePicker(
    currentImage: String?,
    selectedImageUri: String?,
    onPick: () -> Unit,
    onTakePhoto: () -> Unit,
    onClearSelected: () -> Unit
) {
    val preview = selectedImageUri ?: currentImage
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(184.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
            contentAlignment = Alignment.Center
        ) {
            if (!preview.isNullOrBlank()) {
                AsyncImage(
                    model = preview,
                    contentDescription = "位置图片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.Image, contentDescription = null, tint = TextLighter)
                    Text(
                        text = "给这个位置添加一张照片",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (selectedImageUri != null) {
                IconButton(
                    onClick = onClearSelected,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(34.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.84f), RoundedCornerShape(999.dp))
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = "清除位置图片")
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onPick, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
                Text(if (preview.isNullOrBlank()) "相册" else "更换")
            }
            TextButton(onClick = onTakePhoto, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                Text("拍照")
            }
        }
    }
}

private fun nodeDepth(node: StorageNode): Int = node.pathName?.count { it == '/' } ?: 0
