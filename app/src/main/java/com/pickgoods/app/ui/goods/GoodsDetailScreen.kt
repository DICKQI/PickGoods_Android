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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.pickgoods.app.data.model.GuziImage
import com.pickgoods.app.ui.common.DeleteConfirmDialog
import com.pickgoods.app.ui.common.EmptyMessage
import com.pickgoods.app.ui.common.ErrorMessage
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.goods.components.resolveImageUrl
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.GoldSoft
import com.pickgoods.app.ui.theme.PurpleSecondary
import com.pickgoods.app.ui.theme.PurpleSoft
import com.pickgoods.app.ui.theme.TextLighter
import com.pickgoods.app.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoodsDetailScreen(
    goodsId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
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
            TopAppBar(
                title = { Text("谷子详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
    onPreviewImage: (ImagePreview) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GoodsHeroCard(
                goods = goods,
                baseUrl = baseUrl,
                onPreviewImage = onPreviewImage
            )
        }
        item {
            PickGoodsCard(radius = 18.dp) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
        if (goods.additionalPhotos.isNotEmpty()) {
            item {
                Text("附加图片", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                .height(320.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft)))
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
                    contentScale = ContentScale.Crop,
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
        modifier = Modifier.width(158.dp),
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
                    .height(112.dp)
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
    Row {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.32f))
        Text(
            value,
            modifier = Modifier.weight(0.68f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
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
