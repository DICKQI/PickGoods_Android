package com.pickgoods.app.ui.goods

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.pickgoods.app.data.model.GoodsDetail
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.data.model.GuziImage
import com.pickgoods.app.ui.common.DeleteConfirmDialog
import com.pickgoods.app.ui.common.EmptyMessage
import com.pickgoods.app.ui.common.ErrorMessage
import com.pickgoods.app.ui.common.MobileInfoTile
import com.pickgoods.app.ui.common.MobileSectionHeader
import com.pickgoods.app.ui.common.PickGoodsBackTopBar
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.goods.components.resolveImageUrl
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.GoldSoft
import com.pickgoods.app.ui.theme.PurpleSecondary
import com.pickgoods.app.ui.theme.PurpleSoft
import com.pickgoods.app.ui.theme.SurfaceGray
import com.pickgoods.app.ui.theme.TextLighter
import com.pickgoods.app.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoodsDetailScreen(
    goodsId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onGoodsClick: (String) -> Unit,
    viewModel: GoodsDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var previewImage by remember { mutableStateOf<ImagePreview?>(null) }

    LaunchedEffect(goodsId) {
        viewModel.load(goodsId)
    }
    LaunchedEffect(state.deleted) {
        if (state.deleted) onBack()
    }

    Scaffold(
        topBar = {
            PickGoodsBackTopBar(
                title = "谷子详情",
                onBackClick = onBack,
                actions = {
                    state.goods?.let { goods ->
                        IconButton(onClick = { onEdit(goods.id) }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "编辑")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PickGoodsScreen(modifier = Modifier.padding(paddingValues)) {
            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                state.error != null && state.goods == null -> Box(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorMessage(state.error ?: "加载失败") { viewModel.load(goodsId) }
                }
                state.goods == null -> Box(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyMessage("谷子不存在")
                }
                else -> GoodsDetailContent(
                    goods = state.goods!!,
                    baseUrl = state.baseUrl,
                    isSameThemeLoading = state.isSameThemeLoading,
                    sameThemeGoods = state.sameThemeGoods,
                    sameThemeError = state.sameThemeError,
                    onGoodsClick = onGoodsClick,
                    onPreviewImage = { previewImage = it }
                )
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            title = "删除谷子",
            text = "确定删除「${state.goods?.name.orEmpty()}」吗？",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                viewModel.delete()
            }
        )
    }

    previewImage?.let { image ->
        ImagePreviewDialog(
            image = image,
            onDismiss = { previewImage = null }
        )
    }
}

@Composable
private fun GoodsDetailContent(
    goods: GoodsDetail,
    baseUrl: String,
    isSameThemeLoading: Boolean,
    sameThemeGoods: List<GoodsListItem>,
    sameThemeError: String?,
    onGoodsClick: (String) -> Unit,
    onPreviewImage: (ImagePreview) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            GoodsHeroCard(
                goods = goods,
                baseUrl = baseUrl,
                onPreviewImage = onPreviewImage
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MobileInfoTile(
                    label = "状态",
                    value = statusLabel(goods.status),
                    subtitle = if (goods.isOfficial) "官谷" else "同人",
                    accent = Gold,
                    modifier = Modifier.weight(1f)
                )
                MobileInfoTile(
                    label = "数量",
                    value = "x${goods.quantity}",
                    subtitle = goods.category.name,
                    accent = PurpleSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MobileInfoTile(
                    label = "价格",
                    value = goods.price?.takeIf { it.isNotBlank() }?.let { "¥$it" } ?: "未记录",
                    subtitle = goods.purchaseDate ?: "未记录入手日期",
                    accent = PurpleSecondary,
                    modifier = Modifier.weight(1f)
                )
                MobileInfoTile(
                    label = "位置",
                    value = goods.locationPath?.split('/')?.lastOrNull()?.takeIf { it.isNotBlank() } ?: "未设置",
                    subtitle = goods.locationPath ?: "暂无位置",
                    accent = Gold,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            MobileSectionHeader(
                title = "详细资料",
                subtitle = "IP、角色、收纳位置与购入信息",
                accent = PurpleSecondary
            )
        }
        item {
            PickGoodsCard(radius = 16.dp) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChipText(if (goods.isOfficial) "官谷" else "同人")
                        ChipText(statusLabel(goods.status))
                        if (goods.quantity > 1) ChipText("x${goods.quantity}")
                    }
                    DetailRow("IP", goods.ip.name)
                    DetailRow("角色", goods.characters.joinToString("、") { it.name })
                    DetailRow("品类", goods.category.pathName ?: goods.category.name)
                    DetailRow("主题", goods.theme?.name ?: "-")
                    DetailRow("位置", goods.locationPath ?: "-")
                    DetailRow("价格", goods.price ?: "-")
                    DetailRow("入手日期", goods.purchaseDate ?: "-")
                    DetailRow("备注", goods.notes ?: "-")
                }
            }
        }
        goods.theme?.let { theme ->
            item {
                SameThemeSection(
                    themeName = theme.name,
                    goods = sameThemeGoods,
                    baseUrl = baseUrl,
                    isLoading = isSameThemeLoading,
                    error = sameThemeError,
                    onGoodsClick = onGoodsClick
                )
            }
        }
        if (goods.additionalPhotos.isNotEmpty()) {
            item {
                MobileSectionHeader(
                    title = "附加图片",
                    subtitle = "包装、背面、瑕疵等细节图",
                    accent = Gold
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(goods.additionalPhotos, key = { it.id }) { photo ->
                        AdditionalPhotoCard(
                            photo = photo,
                            baseUrl = baseUrl,
                            onPreviewImage = onPreviewImage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SameThemeSection(
    themeName: String,
    goods: List<GoodsListItem>,
    baseUrl: String,
    isLoading: Boolean,
    error: String?,
    onGoodsClick: (String) -> Unit
) {
    PickGoodsCard(radius = 16.dp) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MobileSectionHeader(
                title = "相同主题",
                subtitle = "$themeName · ${goods.size} 件",
                accent = PurpleSecondary
            )
            when {
                isLoading -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .width(138.dp)
                                .height(172.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft)))
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.Center),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
                error != null -> Text(
                    text = "相同主题加载失败",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                goods.isEmpty() -> Text(
                    text = "暂无其他相同主题的谷子",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                else -> LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(goods, key = { it.id }) { item ->
                        SameThemeGoodsCard(
                            goods = item,
                            baseUrl = baseUrl,
                            onClick = { onGoodsClick(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SameThemeGoodsCard(
    goods: GoodsListItem,
    baseUrl: String,
    onClick: () -> Unit
) {
    PickGoodsCard(
        modifier = Modifier.width(174.dp),
        radius = 16.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
                contentAlignment = Alignment.Center
            ) {
                val image = resolveImageUrl(goods.mainPhoto, baseUrl)
                if (image != null) {
                    AsyncImage(
                        model = image,
                        contentDescription = goods.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Outlined.Image, contentDescription = null, tint = TextLighter)
                }
            }
            Text(
                text = goods.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = goods.characters.takeIf { it.isNotEmpty() }?.joinToString("、") { it.name } ?: goods.ip.name,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GoodsHeroCard(
    goods: GoodsDetail,
    baseUrl: String,
    onPreviewImage: (ImagePreview) -> Unit
) {
    val imageModel = resolveImageUrl(goods.mainPhoto, baseUrl)
    PickGoodsCard(radius = 20.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(386.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF111318))
                .then(
                    if (imageModel != null) {
                        Modifier.clickable {
                            onPreviewImage(ImagePreview(imageModel, goods.name))
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = goods.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = TextLighter
                    )
                    Text(
                        text = "暂无主图",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.18f),
                                Color.Black.copy(alpha = 0.64f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingBadge(
                    text = if (goods.isOfficial) "官谷" else "同人",
                    color = if (goods.isOfficial) Gold else PurpleSecondary
                )
                FloatingBadge(text = statusLabel(goods.status), color = Gold)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = goods.name,
                    color = White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        append(goods.ip.name)
                        if (goods.characters.isNotEmpty()) {
                            append(" · ")
                            append(goods.characters.joinToString("、") { it.name })
                        }
                    },
                    color = White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AdditionalPhotoCard(
    photo: GuziImage,
    baseUrl: String,
    onPreviewImage: (ImagePreview) -> Unit
) {
    val model = resolveImageUrl(photo.image, baseUrl)
    PickGoodsCard(
        modifier = Modifier.width(214.dp),
        radius = 16.dp,
        onClick = if (model != null) {
            { onPreviewImage(ImagePreview(model, photo.label ?: "附加图片")) }
        } else {
            null
        }
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
                contentAlignment = Alignment.Center
            ) {
                if (model != null) {
                    AsyncImage(
                        model = model,
                        contentDescription = photo.label,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Outlined.Image, contentDescription = null, tint = TextLighter)
                }
            }
            Text(
                text = photo.label?.takeIf { it.isNotBlank() } ?: "未命名图片",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FloatingBadge(text: String, color: Color) {
    Surface(
        shape = PickGoodsShape.Control,
        color = Color.White.copy(alpha = 0.88f),
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ImagePreviewDialog(
    image: ImagePreview,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
        ) {
            AsyncImage(
                model = image.model,
                contentDescription = image.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color.White.copy(alpha = 0.16f), PickGoodsShape.Pill)
            ) {
                Icon(Icons.Outlined.Close, contentDescription = "关闭预览", tint = White)
            }
            image.label?.takeIf { it.isNotBlank() }?.let { label ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(18.dp),
                    shape = PickGoodsShape.Pill,
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = label,
                        color = White,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Surface(
        shape = PickGoodsShape.Control,
        color = SurfaceGray.copy(alpha = 0.62f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(0.30f)
            )
            Text(
                value.ifBlank { "-" },
                modifier = Modifier.weight(0.70f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChipText(text: String) {
    Surface(
        shape = PickGoodsShape.Pill,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

private data class ImagePreview(
    val model: Any,
    val label: String?
)

private fun statusLabel(status: String): String {
    return when (status) {
        "in_cabinet" -> "在馆"
        "outdoor" -> "在外"
        "sold" -> "已出"
        "draft" -> "草稿"
        else -> status
    }
}
