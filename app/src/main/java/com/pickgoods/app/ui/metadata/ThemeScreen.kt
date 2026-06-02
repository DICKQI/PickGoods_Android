package com.pickgoods.app.ui.metadata

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.pickgoods.app.data.model.Theme
import com.pickgoods.app.data.model.ThemeImage
import com.pickgoods.app.data.util.ImageCaptureUtils
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
import com.pickgoods.app.ui.common.SearchField
import com.pickgoods.app.ui.common.SimpleListCard
import com.pickgoods.app.ui.goods.components.resolveImageUrl
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.GoldSoft
import com.pickgoods.app.ui.theme.PurpleSecondary
import com.pickgoods.app.ui.theme.PurpleSoft

@Composable
fun ThemeScreen(
    onSettingsClick: () -> Unit,
    viewModel: MetadataViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            PickGoodsTopBar(
                title = "主题管理",
                onSettingsClick = onSettingsClick,
                onRefreshClick = viewModel::refresh
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PickGoodsScreen(modifier = Modifier.padding(paddingValues)) {
            ThemeContent(
                state = state,
                onSearch = viewModel::onSearchChanged,
                onSave = viewModel::saveTheme,
                onLoadDetail = viewModel::loadThemeDetail,
                onClearDetail = viewModel::clearThemeDetail,
                onUpdateImageLabel = viewModel::updateThemeImageLabel,
                onDeleteImage = viewModel::deleteThemeImage,
                onDeleteImages = viewModel::deleteThemeImages,
                onDelete = viewModel::deleteTheme,
                onRefresh = viewModel::refresh
            )
        }
    }
}

@Composable
private fun ThemeContent(
    state: MetadataUiState,
    onSearch: (String) -> Unit,
    onSave: (Theme?, String, String?, List<String>, String?) -> Unit,
    onLoadDetail: (Int) -> Unit,
    onClearDetail: () -> Unit,
    onUpdateImageLabel: (Int, Int, String) -> Unit,
    onDeleteImage: (Int, Int) -> Unit,
    onDeleteImages: (Int, Set<Int>) -> Unit,
    onDelete: (Int) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editing by remember { mutableStateOf<Theme?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Theme?>(null) }
    val imageCount = remember(state.themes) { state.themes.sumOf { it.images?.size ?: 0 } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            MobileHeaderCard(
                title = "主题管理",
                subtitle = "${state.themes.size} 个主题集合",
                trailing = {
                    CompactActionButton(label = "新增", onClick = { showCreate = true })
                }
            ) {
                SearchField(
                    value = state.searchQuery,
                    onValueChange = onSearch,
                    placeholder = "搜索主题...",
                    modifier = Modifier.height(48.dp)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MobileInfoTile(
                    label = "主题",
                    value = state.themes.size.toString(),
                    subtitle = "主题集合",
                    accent = Gold,
                    modifier = Modifier.weight(1f)
                )
                MobileInfoTile(
                    label = "图片",
                    value = imageCount.toString(),
                    subtitle = "主题素材",
                    accent = PurpleSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (state.themes.isNotEmpty()) {
            item {
                ThemePreviewRail(
                    themes = state.themes.take(12),
                    baseUrl = state.baseUrl,
                    onSelect = { theme ->
                        editing = theme
                        onLoadDetail(theme.id)
                    }
                )
            }
        }
        item {
            MobileSectionHeader(
                title = "主题列表",
                subtitle = "用于系列、场景和拍摄方案归类",
                accent = PurpleSecondary
            )
        }
        state.error?.takeIf { state.themes.isNotEmpty() }?.let { error ->
            item { ErrorMessage(error, onRefresh) }
        }
        when {
            state.isLoading && state.themes.isEmpty() -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null && state.themes.isEmpty() -> item { ErrorMessage(state.error, onRefresh) }
            state.themes.isEmpty() -> item { EmptyMessage("暂无主题") }
            else -> items(state.themes, key = { it.id }) { theme ->
                ThemeListCard(
                    theme = theme,
                    baseUrl = state.baseUrl,
                    onEdit = {
                        editing = theme
                        onLoadDetail(theme.id)
                    },
                    onDelete = { deleteTarget = theme }
                )
            }
        }
    }

    if (showCreate || editing != null) {
        ThemeEditDialog(
            theme = editing,
            detailTheme = state.activeThemeDetail,
            baseUrl = state.baseUrl,
            isLoadingDetail = state.isThemeDetailLoading,
            isSaving = state.isSaving,
            isUploadingImages = state.isUploadingThemeImages,
            onDismiss = {
                showCreate = false
                editing = null
                onClearDetail()
            },
            onConfirm = { name, description, imageUris, imageLabel ->
                onSave(editing, name, description, imageUris, imageLabel)
                showCreate = false
                editing = null
                onClearDetail()
            },
            onUpdateImageLabel = onUpdateImageLabel,
            onDeleteImage = onDeleteImage,
            onDeleteImages = onDeleteImages
        )
    }
    deleteTarget?.let { theme ->
        DeleteConfirmDialog(
            title = "删除主题",
            text = "确定删除「${theme.name}」吗？",
            onDismiss = { deleteTarget = null },
            onConfirm = {
                onDelete(theme.id)
                deleteTarget = null
            }
        )
    }
}

@Composable
private fun ThemePreviewRail(
    themes: List<Theme>,
    baseUrl: String,
    onSelect: (Theme) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MobileSectionHeader(
            title = "主题预览",
            subtitle = "图片优先浏览，适合快速找系列",
            accent = Gold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(themes, key = { it.id }) { theme ->
                ThemePreviewCard(
                    theme = theme,
                    baseUrl = baseUrl,
                    onClick = { onSelect(theme) }
                )
            }
        }
    }
}

@Composable
private fun ThemePreviewCard(
    theme: Theme,
    baseUrl: String,
    onClick: () -> Unit
) {
    val image = theme.images?.firstOrNull()?.image
    PickGoodsCard(
        modifier = Modifier.width(238.dp),
        radius = 16.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(9.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(PurpleSoft, GoldSoft))),
                contentAlignment = Alignment.Center
            ) {
                if (!image.isNullOrBlank()) {
                    AsyncImage(
                        model = resolveImageUrl(image, baseUrl),
                        contentDescription = theme.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Outlined.Image, contentDescription = null, tint = PurpleSecondary)
                }
            }
            Text(
                text = theme.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = theme.description ?: "${theme.images?.size ?: 0} 张主题图",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ThemeListCard(
    theme: Theme,
    baseUrl: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    SimpleListCard(
        title = theme.name,
        subtitle = theme.description ?: "创建于 ${theme.createdAt ?: "-"}",
        meta = theme.createdAt?.take(10),
        onClick = onEdit,
        onEdit = onEdit,
        onDelete = onDelete,
        leading = {
            ThemeThumb(theme = theme, baseUrl = baseUrl)
        }
    )
}

@Composable
private fun ThemeThumb(theme: Theme, baseUrl: String) {
    val image = theme.images?.firstOrNull()?.image
    Box(
        modifier = Modifier
            .size(82.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(PurpleSoft, GoldSoft))),
        contentAlignment = Alignment.Center
    ) {
        if (!image.isNullOrBlank()) {
            AsyncImage(
                model = resolveImageUrl(image, baseUrl),
                contentDescription = theme.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = PurpleSecondary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun ThemeEditDialog(
    theme: Theme?,
    detailTheme: Theme?,
    baseUrl: String,
    isLoadingDetail: Boolean,
    isSaving: Boolean,
    isUploadingImages: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, List<String>, String?) -> Unit,
    onUpdateImageLabel: (Int, Int, String) -> Unit,
    onDeleteImage: (Int, Int) -> Unit,
    onDeleteImages: (Int, Set<Int>) -> Unit
) {
    var name by remember(theme?.id) { mutableStateOf(theme?.name.orEmpty()) }
    var description by remember(theme?.id) { mutableStateOf(theme?.description.orEmpty()) }
    var newImageUris by remember(theme?.id) { mutableStateOf<List<String>>(emptyList()) }
    var newImageLabel by remember(theme?.id) { mutableStateOf("") }
    var pendingCameraUri by remember(theme?.id) { mutableStateOf<Uri?>(null) }
    var selectedImageIds by remember(theme?.id, detailTheme?.images?.map { it.id }) {
        mutableStateOf<Set<Int>>(emptySet())
    }
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(8)
    ) { uris ->
        newImageUris = (newImageUris + uris.map { it.toString() }).distinct()
    }
    val imageCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { uri ->
                newImageUris = (newImageUris + uri.toString()).distinct()
            }
        }
        pendingCameraUri = null
    }
    val detail = detailTheme?.takeIf { it.id == theme?.id }
    val existingImages = detail?.images.orEmpty()
    val busy = isSaving || isUploadingImages

    MobileFormSheet(
        title = if (theme == null) "新增主题" else "编辑主题",
        subtitle = "主题图片可作为系列、场景或拍摄方案的补充素材",
        confirmEnabled = name.isNotBlank(),
        isBusy = busy,
        onDismiss = onDismiss,
        onConfirm = {
            onConfirm(
                name.trim(),
                description.trim(),
                newImageUris,
                newImageLabel.trim().ifBlank { null }
            )
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("主题名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = PickGoodsShape.Control
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("主题描述") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = PickGoodsShape.Control
        )

        ThemeImageManagerHeader(
            isLoadingDetail = isLoadingDetail,
            isUploadingImages = isUploadingImages,
            selectedCount = selectedImageIds.size,
            onDeleteSelected = theme?.let { currentTheme ->
                {
                    onDeleteImages(currentTheme.id, selectedImageIds)
                    selectedImageIds = emptySet()
                }
            }
        )

        if (isLoadingDetail && theme != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (theme != null && existingImages.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(existingImages, key = { it.id }) { image ->
                    ExistingThemeImageCard(
                        themeId = theme.id,
                        image = image,
                        baseUrl = baseUrl,
                        busy = busy,
                        selected = image.id in selectedImageIds,
                        onToggleSelected = {
                            selectedImageIds = if (image.id in selectedImageIds) {
                                selectedImageIds - image.id
                            } else {
                                selectedImageIds + image.id
                            }
                        },
                        onUpdateLabel = onUpdateImageLabel,
                        onDeleteImage = onDeleteImage
                    )
                }
            }
        } else if (theme != null) {
            Text(
                text = "暂无主题图片",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (newImageUris.isNotEmpty()) {
            OutlinedTextField(
                value = newImageLabel,
                onValueChange = { newImageLabel = it },
                label = { Text("本次上传图片标签（可选）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = PickGoodsShape.Control
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(newImageUris, key = { it }) { uri ->
                    NewThemeImageCard(
                        uri = uri,
                        onRemove = {
                            newImageUris = newImageUris.filterNot { it == uri }
                        }
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                enabled = !busy,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
                Text("相册")
            }
            TextButton(
                onClick = {
                    val uri = ImageCaptureUtils.createCaptureUri(context)
                    pendingCameraUri = uri
                    imageCameraLauncher.launch(uri)
                },
                enabled = !busy,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                Text("拍照")
            }
        }
    }
}

@Composable
private fun ThemeImageManagerHeader(
    isLoadingDetail: Boolean,
    isUploadingImages: Boolean,
    selectedCount: Int = 0,
    onDeleteSelected: (() -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "主题图片",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "上传后会按后端规则压缩到适合体积",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (selectedCount > 0 && onDeleteSelected != null) {
            TextButton(
                enabled = !isLoadingDetail && !isUploadingImages,
                onClick = onDeleteSelected
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null)
                Text("删除 $selectedCount 张")
            }
        } else if (isLoadingDetail || isUploadingImages) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun ExistingThemeImageCard(
    themeId: Int,
    image: ThemeImage,
    baseUrl: String,
    busy: Boolean,
    selected: Boolean,
    onToggleSelected: () -> Unit,
    onUpdateLabel: (Int, Int, String) -> Unit,
    onDeleteImage: (Int, Int) -> Unit
) {
    var label by remember(image.id, image.label) { mutableStateOf(image.label.orEmpty()) }
    val originalLabel = image.label.orEmpty()

    PickGoodsCard(
        modifier = Modifier.size(width = 228.dp, height = 296.dp),
        radius = 16.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(178.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = resolveImageUrl(image.image, baseUrl),
                    contentDescription = image.label ?: "主题图片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    enabled = !busy,
                    onClick = onToggleSelected,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(30.dp)
                        .background(
                            color = if (selected) Gold.copy(alpha = 0.92f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                            shape = PickGoodsShape.Pill
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = if (selected) "取消选择" else "选择图片",
                        tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("标签") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                shape = PickGoodsShape.Control
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    enabled = !busy && label != originalLabel,
                    onClick = { onUpdateLabel(themeId, image.id, label) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("保存标签", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(
                    enabled = !busy,
                    onClick = { onDeleteImage(themeId, image.id) }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "删除图片",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun NewThemeImageCard(
    uri: String,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(144.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(PurpleSoft, GoldSoft)))
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "待上传图片",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                    shape = PickGoodsShape.Pill
                )
        ) {
            Icon(Icons.Outlined.Close, contentDescription = "移除待上传图片")
        }
    }
}
