package com.pickgoods.app.ui.goods

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pickgoods.app.data.model.GuziImage
import com.pickgoods.app.data.model.GoodsDuplicateCandidate
import com.pickgoods.app.data.util.ImageCaptureUtils
import com.pickgoods.app.ui.common.ChoiceChipFlow
import com.pickgoods.app.ui.common.ErrorMessage
import com.pickgoods.app.ui.common.GoldAccentLine
import com.pickgoods.app.ui.common.MobileFormSheet
import com.pickgoods.app.ui.common.MobileHeaderCard
import com.pickgoods.app.ui.common.MobileInfoTile
import com.pickgoods.app.ui.common.MobileSectionHeader
import com.pickgoods.app.ui.common.PickGoodsBackTopBar
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.common.SmallChoiceChip
import com.pickgoods.app.ui.goods.components.resolveImageUrl
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.GoldSoft
import com.pickgoods.app.ui.theme.PurpleOnSecondary
import com.pickgoods.app.ui.theme.PurpleSecondary
import com.pickgoods.app.ui.theme.PurpleSoft
import com.pickgoods.app.ui.theme.TextLighter
import coil.compose.AsyncImage
import com.pickgoods.app.data.util.ImageEditUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GoodsFormScreen(
    goodsId: String?,
    onBack: () -> Unit,
    onDraftsClick: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: GoodsFormViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingMainCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingAdditionalCameraUri by remember { mutableStateOf<Uri?>(null) }
    var editingMainPhotoUri by remember { mutableStateOf<String?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val value = uri?.toString()
        viewModel.updateMainPhotoUri(value)
        editingMainPhotoUri = value
    }
    val additionalPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(8)
    ) { uris ->
        viewModel.addAdditionalPhotoUris(uris.map { it.toString() })
    }
    val mainCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingMainCameraUri?.let {
                val value = it.toString()
                viewModel.updateMainPhotoUri(value)
                editingMainPhotoUri = value
            }
        }
        pendingMainCameraUri = null
    }
    val additionalCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingAdditionalCameraUri?.let { viewModel.addAdditionalPhotoUris(listOf(it.toString())) }
        }
        pendingAdditionalCameraUri = null
    }

    LaunchedEffect(goodsId) {
        viewModel.load(goodsId)
    }
    LaunchedEffect(state.savedGoodsId) {
        state.savedGoodsId?.let(onSaved)
    }

    Scaffold(
        topBar = {
            PickGoodsBackTopBar(
                title = if (goodsId == null) "新增谷子" else "编辑谷子",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = onDraftsClick) {
                        Icon(Icons.Outlined.NoteAlt, contentDescription = "草稿箱")
                    }
                    IconButton(onClick = viewModel::save, enabled = !state.isSaving) {
                        Icon(Icons.Outlined.Save, contentDescription = "保存")
                    }
                }
            )
        },
        bottomBar = {
            if (!state.isLoading) {
                GoodsFormActionBar(
                    isBusy = state.isSaving || state.isUploadingPhoto || state.isUploadingAdditionalPhotos,
                    isEdit = goodsId != null,
                    isDraftStatus = state.status == "draft",
                    onSaveDraft = viewModel::saveAsDraft,
                    onPublish = viewModel::publish
                )
            }
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
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                state.error?.let { error ->
                    item { ErrorMessage(error) }
                }
                item {
                    MobileHeaderCard(
                        title = if (goodsId == null) "新增谷子" else "编辑谷子",
                        subtitle = "图片、分类与位置保存后会同步到云展柜"
                    )
                }
                item {
                    FormSnapshotRow(
                        status = state.status,
                        isOfficial = state.isOfficial,
                        mainPhotoReady = !state.selectedMainPhotoUri.isNullOrBlank() || !state.currentMainPhoto.isNullOrBlank(),
                        additionalCount = state.currentAdditionalPhotos.size + state.selectedAdditionalPhotoUris.size,
                        ipName = state.ips.firstOrNull { it.id == state.ipId }?.name,
                        categoryName = state.categories.firstOrNull { it.id == state.categoryId }?.let { it.pathName ?: it.name }
                    )
                }
                item {
                    MobileSectionHeader(
                        title = "图片",
                        subtitle = "主图会决定列表卡片和展柜观感",
                        accent = MaterialTheme.colorScheme.primary
                    )
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
                        onTakePhoto = {
                            val uri = ImageCaptureUtils.createCaptureUri(context)
                            pendingMainCameraUri = uri
                            mainCameraLauncher.launch(uri)
                        },
                        onClearSelection = {
                            editingMainPhotoUri = null
                            viewModel.updateMainPhotoUri(null)
                        },
                        onEditSelection = {
                            state.selectedMainPhotoUri?.let { editingMainPhotoUri = it }
                        }
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
                        onTakePhoto = {
                            val uri = ImageCaptureUtils.createCaptureUri(context)
                            pendingAdditionalCameraUri = uri
                            additionalCameraLauncher.launch(uri)
                        },
                        onRemoveSelected = viewModel::removeAdditionalPhotoUri,
                        onLabelChanged = viewModel::updateAdditionalPhotoLabel,
                        onDeleteExisting = viewModel::deleteAdditionalPhoto,
                        onDeleteExistingBatch = viewModel::deleteAdditionalPhotos,
                        onUpdateExistingLabel = viewModel::updateExistingAdditionalPhotoLabel
                    )
                }
                item {
                    MobileSectionHeader(
                        title = "基础信息",
                        subtitle = "名称、数量、价格和购入记录",
                        accent = MaterialTheme.colorScheme.secondary
                    )
                }
                item {
                    PickGoodsCard(radius = 16.dp) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = state.quantity,
                                    onValueChange = viewModel::updateQuantity,
                                    label = { Text("数量") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.38f),
                                    singleLine = true,
                                    shape = PickGoodsShape.Control
                                )
                                OutlinedTextField(
                                    value = state.price,
                                    onValueChange = viewModel::updatePrice,
                                    label = { Text("价格（可选）") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(0.62f),
                                    singleLine = true,
                                    shape = PickGoodsShape.Control
                                )
                            }
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
                    MobileSectionHeader(
                        title = "分类绑定",
                        subtitle = "IP、角色、品类、主题与收纳位置",
                        accent = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    ChoiceChipFlow(
                        title = "状态",
                        options = statusOptions.map { it.first },
                        selected = state.status,
                        label = { statusLabel(it) },
                        onSelected = { selected -> viewModel.updateStatus(selected ?: state.status) }
                    )
                }

                item {
                    ChoiceChipFlow(
                        title = "IP 作品",
                        options = state.ips,
                        selected = state.ips.firstOrNull { it.id == state.ipId },
                        label = { it.name },
                        onSelected = { selected -> selected?.let { viewModel.updateIp(it.id) } },
                        maxItems = 80
                    )
                }

                item {
                    val filteredCharacters = state.characters
                        .filter { state.ipId == null || it.ip.id == state.ipId || it.ipId == state.ipId }
                    OptionSection(title = "角色（可多选）") {
                        filteredCharacters.forEach { character ->
                            SmallChoiceChip(
                                label = character.name,
                                selected = character.id in state.characterIds,
                                onClick = { viewModel.toggleCharacter(character.id) }
                            )
                        }
                    }
                }

                item {
                    ChoiceChipFlow(
                        title = "品类",
                        options = state.categories,
                        selected = state.categories.firstOrNull { it.id == state.categoryId },
                        label = { it.pathName ?: it.name },
                        onSelected = { selected -> selected?.let { viewModel.updateCategory(it.id) } },
                        maxItems = 80
                    )
                }

                item {
                    ChoiceChipFlow(
                        title = "主题",
                        options = state.themes,
                        selected = state.themes.firstOrNull { it.id == state.themeId },
                        label = { it.name },
                        onSelected = { selected -> viewModel.updateTheme(selected?.id) },
                        emptyLabel = "无主题",
                        maxItems = 80
                    )
                }

                item {
                    ChoiceChipFlow(
                        title = "收纳位置",
                        options = state.locations,
                        selected = state.locations.firstOrNull { it.id == state.locationId },
                        label = { it.pathName ?: it.name },
                        onSelected = { selected -> viewModel.updateLocation(selected?.id) },
                        emptyLabel = "暂不设置",
                        maxItems = 80
                    )
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

    editingMainPhotoUri?.let { uri ->
        MainPhotoEditSheet(
            uri = uri,
            onDismiss = { editingMainPhotoUri = null },
            onCrop = { aspectWidth, aspectHeight ->
                val croppedUri = ImageEditUtils.centerCropToAspectUri(
                    context = context,
                    uri = Uri.parse(uri),
                    aspectWidth = aspectWidth,
                    aspectHeight = aspectHeight
                )
                val value = croppedUri.toString()
                viewModel.updateMainPhotoUri(value)
                editingMainPhotoUri = value
            }
        )
    }
}

@Composable
private fun GoodsFormActionBar(
    isBusy: Boolean,
    isEdit: Boolean,
    isDraftStatus: Boolean,
    onSaveDraft: () -> Unit,
    onPublish: () -> Unit
) {
    val primaryText = when {
        isDraftStatus -> "发布入馆"
        isEdit -> "保存修改"
        else -> "发布"
    }
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shadowElevation = 10.dp
    ) {
        Column {
            GoldAccentLine(modifier = Modifier.height(1.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onSaveDraft,
                    enabled = !isBusy,
                    modifier = Modifier
                        .weight(0.42f)
                        .height(48.dp),
                    shape = PickGoodsShape.Pill,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldSoft,
                        contentColor = Gold
                    )
                ) {
                    Text(
                        text = if (isBusy) "保存中..." else "保存草稿",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Button(
                    onClick = onPublish,
                    enabled = !isBusy,
                    modifier = Modifier
                        .weight(0.58f)
                        .height(48.dp),
                    shape = PickGoodsShape.Pill,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleSecondary,
                        contentColor = PurpleOnSecondary
                    )
                ) {
                    Text(
                        text = if (isBusy) "处理中..." else primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun FormSnapshotRow(
    status: String,
    isOfficial: Boolean,
    mainPhotoReady: Boolean,
    additionalCount: Int,
    ipName: String?,
    categoryName: String?
) {
    val tiles = listOf(
        FormSnapshotTile("状态", statusLabel(status), if (isOfficial) "官谷" else "同人", Gold),
        FormSnapshotTile(
            "图片",
            "${additionalCount + if (mainPhotoReady) 1 else 0} 张",
            if (mainPhotoReady) "主图已准备" else "未设置主图",
            PurpleSecondary
        ),
        FormSnapshotTile("IP", ipName ?: "未选择", "作品归属", PurpleSecondary),
        FormSnapshotTile("品类", categoryName ?: "未选择", "筛选与统计标签", Gold)
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tiles) { tile ->
            MobileInfoTile(
                label = tile.label,
                value = tile.value,
                subtitle = tile.subtitle,
                accent = tile.accent,
                modifier = Modifier.width(148.dp)
            )
        }
    }
}

private data class FormSnapshotTile(
    val label: String,
    val value: String,
    val subtitle: String,
    val accent: Color
)

@Composable
private fun MainPhotoSection(
    selectedMainPhotoUri: String?,
    currentMainPhoto: String?,
    baseUrl: String,
    isUploadingPhoto: Boolean,
    onPickPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onClearSelection: () -> Unit,
    onEditSelection: () -> Unit
) {
    val previewModel = selectedMainPhotoUri ?: resolveImageUrl(currentMainPhoto, baseUrl)

    PickGoodsCard(radius = 16.dp) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    .height(350.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (previewModel != null) Color(0xFF111318) else Color.Transparent)
                    .background(Brush.linearGradient(listOf(GoldSoft.copy(alpha = 0.7f), PurpleSoft.copy(alpha = 0.7f)))),
                contentAlignment = Alignment.Center
            ) {
                if (previewModel != null) {
                    AsyncImage(
                        model = previewModel,
                        contentDescription = "主图",
                        contentScale = ContentScale.Fit,
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
                    Text("相册")
                }
                TextButton(
                    onClick = onTakePhoto,
                    enabled = !isUploadingPhoto,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                    Text("拍照")
                }
            }
            if (selectedMainPhotoUri != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = onEditSelection,
                        enabled = !isUploadingPhoto,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Image, contentDescription = null)
                        Text("预览裁剪")
                    }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MainPhotoEditSheet(
    uri: String,
    onDismiss: () -> Unit,
    onCrop: (Int, Int) -> Unit
) {
    MobileFormSheet(
        title = "主图预览与裁剪",
        subtitle = "选择比例会生成一张上传副本，原图不受影响",
        confirmText = "完成",
        onDismiss = onDismiss,
        onConfirm = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft)))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = uri,
                contentDescription = "主图预览",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = "快速裁剪",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CropPresetButton(label = "方图 1:1", onClick = { onCrop(1, 1) })
            CropPresetButton(label = "卡片 4:3", onClick = { onCrop(4, 3) })
            CropPresetButton(label = "竖图 3:4", onClick = { onCrop(3, 4) })
            CropPresetButton(label = "长图 16:9", onClick = { onCrop(16, 9) })
        }
        Text(
            text = "当前为居中裁剪，适合把主图快速整理成云展柜卡片更稳定的比例。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun CropPresetButton(
    label: String,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Text(label)
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
    onTakePhoto: () -> Unit,
    onRemoveSelected: (String) -> Unit,
    onLabelChanged: (String) -> Unit,
    onDeleteExisting: (Int) -> Unit,
    onDeleteExistingBatch: (Set<Int>) -> Unit,
    onUpdateExistingLabel: (Int, String) -> Unit
) {
    var selectedExistingPhotoIds by remember(currentPhotos.map { it.id }) {
        mutableStateOf<Set<Int>>(emptySet())
    }

    PickGoodsCard(radius = 16.dp) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                when {
                    selectedExistingPhotoIds.isNotEmpty() -> TextButton(
                        enabled = !isUploading,
                        onClick = {
                            onDeleteExistingBatch(selectedExistingPhotoIds)
                            selectedExistingPhotoIds = emptySet()
                        }
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null)
                        Text("删除 ${selectedExistingPhotoIds.size} 张")
                    }
                    selectedPhotoUris.isNotEmpty() -> Text(
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
                            canSaveLabel = !isUploading,
                            selected = photo.id in selectedExistingPhotoIds,
                            onToggleSelected = {
                                selectedExistingPhotoIds = if (photo.id in selectedExistingPhotoIds) {
                                    selectedExistingPhotoIds - photo.id
                                } else {
                                    selectedExistingPhotoIds + photo.id
                                }
                            },
                            onDelete = { onDeleteExisting(photo.id) },
                            onUpdateLabel = { label -> onUpdateExistingLabel(photo.id, label) }
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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onPickPhotos,
                    enabled = !isUploading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
                    Text(if (selectedPhotoUris.isEmpty()) "相册" else "继续添加")
                }
                TextButton(
                    onClick = onTakePhoto,
                    enabled = !isUploading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                    Text("拍照")
                }
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
    canSaveLabel: Boolean,
    selected: Boolean,
    onToggleSelected: () -> Unit,
    onDelete: () -> Unit,
    onUpdateLabel: (String) -> Unit
) {
    var label by remember(photo.id, photo.label) { mutableStateOf(photo.label.orEmpty()) }
    val originalLabel = photo.label.orEmpty()

    PickGoodsCard(
        modifier = Modifier.size(width = 222.dp, height = 294.dp),
        radius = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(9.dp)) {
            Box {
                AsyncImage(
                    model = resolveImageUrl(photo.image, baseUrl),
                    contentDescription = photo.label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(174.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                IconButton(
                    enabled = canDelete,
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
                    enabled = canSaveLabel && label != originalLabel,
                    onClick = { onUpdateLabel(label.trim()) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("保存标签", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
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
            .size(144.dp)
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
    MobileFormSheet(
        title = "检测到相似谷子",
        subtitle = message,
        confirmText = "合并数量",
        confirmEnabled = selectedId != null,
        isBusy = isSaving,
        onDismiss = onDismiss,
        onConfirm = onMerge
    ) {
        candidates.take(4).forEach { candidate ->
            DuplicateCandidateCard(
                candidate = candidate,
                selected = selectedId == candidate.id,
                onClick = { onSelect(candidate.id) }
            )
        }
        TextButton(
            enabled = !isSaving,
            onClick = onCreateNew,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("仍然新建")
        }
    }
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
        borderColor = if (selected) Gold.copy(alpha = 0.76f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
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
                        Surface(shape = PickGoodsShape.Pill, color = GoldSoft) {
                            Text(
                                text = "已选择",
                                color = Gold,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
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
    PickGoodsCard(radius = 16.dp) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

private fun statusLabel(status: String): String {
    return statusOptions.firstOrNull { it.first == status }?.second ?: status
}
