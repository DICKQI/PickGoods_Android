package com.pickgoods.app.ui.goods

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pickgoods.app.data.model.GuziImage
import com.pickgoods.app.data.model.GoodsDuplicateCandidate
import com.pickgoods.app.ui.common.ErrorMessage
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.goods.components.resolveImageUrl
import com.pickgoods.app.ui.theme.GoldSoft
import com.pickgoods.app.ui.theme.PurpleSoft
import com.pickgoods.app.ui.theme.TextLighter
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GoodsFormScreen(
    goodsId: String?,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: GoodsFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.updateMainPhotoUri(uri?.toString())
    }
    val additionalPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(8)
    ) { uris ->
        viewModel.addAdditionalPhotoUris(uris.map { it.toString() })
    }

    LaunchedEffect(goodsId) {
        viewModel.load(goodsId)
    }
    LaunchedEffect(state.savedGoodsId) {
        state.savedGoodsId?.let(onSaved)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (goodsId == null) "新增谷子" else "编辑谷子") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::save, enabled = !state.isSaving) {
                        Icon(Icons.Outlined.Save, contentDescription = "保存")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PickGoodsScreen(modifier = Modifier.padding(paddingValues)) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                state.error?.let { error ->
                    item { ErrorMessage(error) }
                }
                item {
                    MainPhotoSection(
                        selectedMainPhotoUri = state.selectedMainPhotoUri,
                        currentMainPhoto = state.currentMainPhoto,
                        baseUrl = state.baseUrl,
                        isUploadingPhoto = state.isUploadingPhoto,
                        onPickPhoto = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onClearSelection = { viewModel.updateMainPhotoUri(null) }
                    )
                }
                item {
                    AdditionalPhotosSection(
                        currentPhotos = state.currentAdditionalPhotos,
                        selectedPhotoUris = state.selectedAdditionalPhotoUris,
                        additionalPhotoLabel = state.additionalPhotoLabel,
                        baseUrl = state.baseUrl,
                        isUploading = state.isUploadingAdditionalPhotos,
                        onPickPhotos = {
                            additionalPhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onRemoveSelected = viewModel::removeAdditionalPhotoUri,
                        onLabelChanged = viewModel::updateAdditionalPhotoLabel,
                        onDeleteExisting = viewModel::deleteAdditionalPhoto
                    )
                }
                item {
                    PickGoodsCard(radius = 18.dp) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = state.name,
                                onValueChange = viewModel::updateName,
                                label = { Text("谷子名称") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = PickGoodsShape.Control
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("官谷", modifier = Modifier.weight(1f))
                                Switch(checked = state.isOfficial, onCheckedChange = viewModel::updateIsOfficial)
                            }
                            OutlinedTextField(
                                value = state.quantity,
                                onValueChange = viewModel::updateQuantity,
                                label = { Text("数量") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = PickGoodsShape.Control
                            )
                            OutlinedTextField(
                                value = state.price,
                                onValueChange = viewModel::updatePrice,
                                label = { Text("价格（可选）") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = PickGoodsShape.Control
                            )
                            OutlinedTextField(
                                value = state.purchaseDate,
                                onValueChange = viewModel::updatePurchaseDate,
                                label = { Text("入手日期 YYYY-MM-DD（可选）") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = PickGoodsShape.Control
                            )
                            OutlinedTextField(
                                value = state.notes,
                                onValueChange = viewModel::updateNotes,
                                label = { Text("备注") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                shape = PickGoodsShape.Control
                            )
                        }
                    }
                }

                item {
                    OptionSection(title = "状态") {
                        statusOptions.forEach { option ->
                            FilterChip(
                                selected = state.status == option.first,
                                onClick = { viewModel.updateStatus(option.first) },
                                label = { Text(option.second) }
                            )
                        }
                    }
                }

                item {
                    OptionSection(title = "IP 作品") {
                        state.ips.forEach { ip ->
                            FilterChip(
                                selected = state.ipId == ip.id,
                                onClick = { viewModel.updateIp(ip.id) },
                                label = { Text(ip.name) }
                            )
                        }
                    }
                }

                item {
                    val filteredCharacters = state.characters
                        .filter { state.ipId == null || it.ip.id == state.ipId || it.ipId == state.ipId }
                    OptionSection(title = "角色（可多选）") {
                        filteredCharacters.forEach { character ->
                            FilterChip(
                                selected = character.id in state.characterIds,
                                onClick = { viewModel.toggleCharacter(character.id) },
                                label = { Text(character.name) }
                            )
                        }
                    }
                }

                item {
                    OptionSection(title = "品类") {
                        state.categories.forEach { category ->
                            FilterChip(
                                selected = state.categoryId == category.id,
                                onClick = { viewModel.updateCategory(category.id) },
                                label = { Text(category.pathName ?: category.name) }
                            )
                        }
                    }
                }

                item {
                    OptionSection(title = "主题") {
                        FilterChip(
                            selected = state.themeId == null,
                            onClick = { viewModel.updateTheme(null) },
                            label = { Text("无主题") }
                        )
                        state.themes.forEach { theme ->
                            FilterChip(
                                selected = state.themeId == theme.id,
                                onClick = { viewModel.updateTheme(theme.id) },
                                label = { Text(theme.name) }
                            )
                        }
                    }
                }

                item {
                    OptionSection(title = "收纳位置") {
                        FilterChip(
                            selected = state.locationId == null,
                            onClick = { viewModel.updateLocation(null) },
                            label = { Text("暂不设置") }
                        )
                        state.locations.forEach { location ->
                            FilterChip(
                                selected = state.locationId == location.id,
                                onClick = { viewModel.updateLocation(location.id) },
                                label = { Text(location.pathName ?: location.name) }
                            )
                        }
                    }
                }
            }
        }
        }
    }

    if (state.duplicateCandidates.isNotEmpty()) {
        DuplicateGoodsDialog(
            message = state.duplicateMessage ?: "检测到可能重复的谷子",
            candidates = state.duplicateCandidates,
            selectedId = state.selectedDuplicateId,
            isSaving = state.isSaving,
            onSelect = viewModel::selectDuplicateCandidate,
            onDismiss = viewModel::dismissDuplicateDialog,
            onCreateNew = viewModel::createAsNewAfterDuplicate,
            onMerge = viewModel::mergeDuplicate
        )
    }
}

@Composable
private fun MainPhotoSection(
    selectedMainPhotoUri: String?,
    currentMainPhoto: String?,
    baseUrl: String,
    isUploadingPhoto: Boolean,
    onPickPhoto: () -> Unit,
    onClearSelection: () -> Unit
) {
    val previewModel = selectedMainPhotoUri ?: resolveImageUrl(currentMainPhoto, baseUrl)

    PickGoodsCard(radius = 18.dp) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "主图",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (selectedMainPhotoUri != null) {
                    Text(
                        text = "待上传",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
                contentAlignment = Alignment.Center
            ) {
                if (previewModel != null) {
                    AsyncImage(
                        model = previewModel,
                        contentDescription = "主图",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = TextLighter
                        )
                        Text(
                            text = "选择一张主图",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onPickPhoto,
                    enabled = !isUploadingPhoto,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
                    Text(if (previewModel == null) "选择图片" else "更换图片")
                }
                if (selectedMainPhotoUri != null) {
                    TextButton(
                        onClick = onClearSelection,
                        enabled = !isUploadingPhoto,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = null)
                        Text("清除选择")
                    }
                }
            }
            if (isUploadingPhoto) {
                Text(
                    text = "正在压缩并上传主图...",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun AdditionalPhotosSection(
    currentPhotos: List<GuziImage>,
    selectedPhotoUris: List<String>,
    additionalPhotoLabel: String,
    baseUrl: String,
    isUploading: Boolean,
    onPickPhotos: () -> Unit,
    onRemoveSelected: (String) -> Unit,
    onLabelChanged: (String) -> Unit,
    onDeleteExisting: (Int) -> Unit
) {
    PickGoodsCard(radius = 18.dp) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "附加图片",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "补充包装、背面、瑕疵等细节",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (selectedPhotoUris.isNotEmpty()) {
                    Text(
                        text = "${selectedPhotoUris.size} 张待上传",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            if (currentPhotos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(currentPhotos, key = { it.id }) { photo ->
                        ExistingAdditionalPhotoThumb(
                            photo = photo,
                            baseUrl = baseUrl,
                            canDelete = !isUploading,
                            onDelete = { onDeleteExisting(photo.id) }
                        )
                    }
                }
            }

            if (selectedPhotoUris.isNotEmpty()) {
                OutlinedTextField(
                    value = additionalPhotoLabel,
                    onValueChange = onLabelChanged,
                    label = { Text("本次上传图片标签（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = PickGoodsShape.Control
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(selectedPhotoUris, key = { it }) { uri ->
                        PendingAdditionalPhotoThumb(
                            uri = uri,
                            onRemove = { onRemoveSelected(uri) }
                        )
                    }
                }
            }

            TextButton(
                onClick = onPickPhotos,
                enabled = !isUploading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
                Text(if (selectedPhotoUris.isEmpty()) "选择附加图片" else "继续添加")
            }

            if (isUploading) {
                Text(
                    text = "正在压缩并上传附加图片...",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ExistingAdditionalPhotoThumb(
    photo: GuziImage,
    baseUrl: String,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    PickGoodsCard(
        modifier = Modifier.size(width = 138.dp, height = 158.dp),
        radius = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = resolveImageUrl(photo.image, baseUrl),
                contentDescription = photo.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = photo.label ?: "未命名图片",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    enabled = canDelete,
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "删除附加图片",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingAdditionalPhotoThumb(
    uri: String,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft)))
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "待上传附加图片",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
                    shape = PickGoodsShape.Pill
                )
        ) {
            Icon(Icons.Outlined.Close, contentDescription = "移除待上传图片")
        }
    }
}

@Composable
private fun DuplicateGoodsDialog(
    message: String,
    candidates: List<GoodsDuplicateCandidate>,
    selectedId: String?,
    isSaving: Boolean,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreateNew: () -> Unit,
    onMerge: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("检测到相似谷子") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                candidates.take(4).forEach { candidate ->
                    DuplicateCandidateCard(
                        candidate = candidate,
                        selected = selectedId == candidate.id,
                        onClick = { onSelect(candidate.id) }
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    enabled = !isSaving,
                    onClick = onCreateNew
                ) {
                    Text("仍然新建")
                }
                TextButton(
                    enabled = selectedId != null && !isSaving,
                    onClick = onMerge
                ) {
                    Text("合并数量")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun DuplicateCandidateCard(
    candidate: GoodsDuplicateCandidate,
    selected: Boolean,
    onClick: () -> Unit
) {
    PickGoodsCard(
        modifier = Modifier.fillMaxWidth(),
        radius = 14.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
                contentAlignment = Alignment.Center
            ) {
                if (!candidate.mainPhotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = candidate.mainPhotoUrl,
                        contentDescription = candidate.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = TextLighter
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = candidate.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (selected) {
                        Text(
                            text = "已选择",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Text(
                    text = buildString {
                        append(candidate.ip.name)
                        if (candidate.characters.isNotEmpty()) {
                            append(" · ")
                            append(candidate.characters.joinToString("、") { it.name })
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "库存数量 x${candidate.quantity}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptionSection(
    title: String,
    content: @Composable () -> Unit
) {
    PickGoodsCard(radius = 18.dp) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}

private val statusOptions = listOf(
    "in_cabinet" to "在馆",
    "outdoor" to "在外",
    "sold" to "已出",
    "draft" to "草稿"
)
