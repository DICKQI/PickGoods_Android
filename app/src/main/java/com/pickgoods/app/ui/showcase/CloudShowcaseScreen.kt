package com.pickgoods.app.ui.showcase

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.data.model.GoodsCategoryTopItem
import com.pickgoods.app.data.model.GoodsIPTopItem
import com.pickgoods.app.data.model.GoodsOfficialDistributionItem
import com.pickgoods.app.data.model.GoodsStatusDistributionItem
import com.pickgoods.app.data.model.Showcase
import com.pickgoods.app.data.model.ShowcaseGoods
import com.pickgoods.app.data.repository.ShowcaseScope
import com.pickgoods.app.ui.common.AddButton
import com.pickgoods.app.ui.common.DeleteConfirmDialog
import com.pickgoods.app.ui.common.EmptyMessage
import com.pickgoods.app.ui.common.ErrorMessage
import com.pickgoods.app.ui.common.PickGoodsTopBar
import com.pickgoods.app.ui.common.PickGoodsAnimatedContent
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.common.SearchField
import com.pickgoods.app.ui.common.SimpleListCard
import com.pickgoods.app.ui.goods.GoodsListContent
import com.pickgoods.app.ui.goods.GoodsViewModel
import com.pickgoods.app.ui.goods.components.resolveImageUrl
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.GoldSoft
import com.pickgoods.app.ui.theme.PurpleSecondary
import com.pickgoods.app.ui.theme.PurpleSoft
import com.pickgoods.app.ui.theme.TextLighter
import com.pickgoods.app.ui.theme.White

@Composable
fun CloudShowcaseScreen(
    onSettingsClick: () -> Unit,
    onGoodsClick: (String) -> Unit,
    onCreateGoods: () -> Unit,
    showcaseViewModel: ShowcaseViewModel = hiltViewModel(),
    goodsViewModel: GoodsViewModel = hiltViewModel()
) {
    val showcaseState by showcaseViewModel.uiState.collectAsStateWithLifecycle()
    val goodsState by goodsViewModel.uiState.collectAsStateWithLifecycle()
    var activeTab by remember { mutableIntStateOf(1) }
    val tabs = listOf("展柜", "谷仓", "统计看板")

    Scaffold(
        topBar = {
            PickGoodsTopBar(
                title = "✦ 拾谷 PickGoods",
                onSettingsClick = onSettingsClick,
                onRefreshClick = {
                    when (activeTab) {
                        0 -> showcaseViewModel.refreshShowcases()
                        1 -> goodsViewModel.refreshGoods()
                        else -> showcaseViewModel.refreshStats()
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PickGoodsScreen(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                CloudTabBar(
                    tabs = tabs,
                    activeTab = activeTab,
                    onTabSelected = { activeTab = it }
                )

                PickGoodsAnimatedContent(
                    targetState = activeTab,
                    modifier = Modifier.weight(1f)
                ) { tab ->
                    when (tab) {
                        0 -> ShowcaseTab(
                            state = showcaseState,
                            onScopeChanged = showcaseViewModel::setScope,
                            onRefresh = showcaseViewModel::refreshShowcases,
                            onSelect = showcaseViewModel::selectShowcase,
                            onBack = showcaseViewModel::clearSelectedShowcase,
                            onSave = showcaseViewModel::saveShowcase,
                            onDelete = showcaseViewModel::deleteShowcase,
                            onGoodsClick = onGoodsClick,
                            onUpdateAddGoodsSearch = showcaseViewModel::updateAddGoodsSearchQuery,
                            onSearchAddGoods = { showcaseViewModel.searchGoodsForAdd() },
                            onAddGoods = showcaseViewModel::addGoodsToSelectedShowcase,
                            onRemoveGoods = showcaseViewModel::removeGoodsFromSelectedShowcase,
                            onMoveGoods = showcaseViewModel::moveGoodsInSelectedShowcase
                        )
                        1 -> GoodsListContent(
                            uiState = goodsState,
                            onSearchQueryChanged = goodsViewModel::onSearchQueryChanged,
                            onStatusFilterChanged = goodsViewModel::setStatusFilter,
                            onStatusSelectionChanged = goodsViewModel::setStatusSelection,
                            onOfficialFilterChanged = goodsViewModel::setOfficialFilter,
                            onIpFilterChanged = goodsViewModel::setIpFilter,
                            onCharacterFilterChanged = goodsViewModel::setCharacterFilter,
                            onCategoryFilterChanged = goodsViewModel::setCategoryFilter,
                            onThemeFilterChanged = goodsViewModel::setThemeFilter,
                            onLocationFilterChanged = goodsViewModel::setLocationFilter,
                            onGroupByChanged = goodsViewModel::setGroupBy,
                            onViewModeChanged = goodsViewModel::setViewMode,
                            onResetFilters = goodsViewModel::resetFilters,
                            onRefreshMetadata = goodsViewModel::refreshMetadata,
                            onPageChanged = goodsViewModel::setPage,
                            onGoodsClick = onGoodsClick,
                            onCreateClick = onCreateGoods,
                            onRetry = { goodsViewModel.refreshGoods() },
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        2 -> StatsTab(state = showcaseState)
                    }
                }
            }
        }
    }
}

@Composable
private fun CloudTabBar(
    tabs: List<String>,
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    PickGoodsCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        radius = 16.dp
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = activeTab == index
                val containerColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    label = "cloudTabContainer"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "cloudTabContent"
                )
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(PickGoodsShape.Pill)
                        .clickable { onTabSelected(index) },
                    shape = PickGoodsShape.Pill,
                    color = containerColor
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = contentColor,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowcaseTab(
    state: ShowcaseUiState,
    onScopeChanged: (ShowcaseScope) -> Unit,
    onRefresh: () -> Unit,
    onSelect: (Showcase) -> Unit,
    onBack: () -> Unit,
    onSave: (Showcase?, String, String?, Boolean, String?) -> Unit,
    onDelete: (String) -> Unit,
    onGoodsClick: (String) -> Unit,
    onUpdateAddGoodsSearch: (String) -> Unit,
    onSearchAddGoods: () -> Unit,
    onAddGoods: (String) -> Unit,
    onRemoveGoods: (String) -> Unit,
    onMoveGoods: (String, String, String) -> Unit
) {
    var editingShowcase by remember { mutableStateOf<Showcase?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showAddGoodsDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Showcase?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.selectedShowcase != null) {
            ShowcaseDetailTab(
                state = state,
                onBack = onBack,
                onEdit = { editingShowcase = state.selectedShowcase },
                onDelete = { deleteTarget = state.selectedShowcase },
                onAddGoods = if (state.scope == ShowcaseScope.Private) {
                    {
                        onUpdateAddGoodsSearch("")
                        onSearchAddGoods()
                        showAddGoodsDialog = true
                    }
                } else {
                    null
                },
                onGoodsClick = onGoodsClick,
                onRemoveGoods = if (state.scope == ShowcaseScope.Private) onRemoveGoods else null,
                onMoveGoods = if (state.scope == ShowcaseScope.Private) onMoveGoods else null
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    PickGoodsCard(radius = 18.dp) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                FilterChip(
                                    selected = state.scope == ShowcaseScope.Private,
                                    onClick = { onScopeChanged(ShowcaseScope.Private) },
                                    label = { Text("我的展柜") }
                                )
                                FilterChip(
                                    selected = state.scope == ShowcaseScope.Public,
                                    onClick = { onScopeChanged(ShowcaseScope.Public) },
                                    label = { Text("公共展柜") }
                                )
                            }
                            if (state.scope == ShowcaseScope.Private) {
                                AddButton(onClick = { showCreateDialog = true })
                            }
                        }
                    }
                }
                state.error?.takeIf { state.showcases.isNotEmpty() }?.let { message ->
                    item {
                        ErrorMessage(message = message, onRetry = onRefresh)
                    }
                }

                when {
                    state.isLoading -> item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    state.error != null && state.showcases.isEmpty() -> item {
                        ErrorMessage(message = state.error, onRetry = onRefresh)
                    }
                    state.showcases.isEmpty() -> item {
                        EmptyMessage("暂无展柜")
                    }
                    else -> items(state.showcases, key = { it.id }) { showcase ->
                        ShowcaseCard(
                            showcase = showcase,
                            baseUrl = state.baseUrl,
                            onClick = { onSelect(showcase) },
                            onEdit = if (state.scope == ShowcaseScope.Private) ({ editingShowcase = showcase }) else null,
                            onDelete = if (state.scope == ShowcaseScope.Private) ({ deleteTarget = showcase }) else null
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog || editingShowcase != null) {
        ShowcaseEditDialog(
            showcase = editingShowcase,
            baseUrl = state.baseUrl,
            isSaving = state.isSaving,
            isUploadingCover = state.isUploadingCover,
            onDismiss = {
                showCreateDialog = false
                editingShowcase = null
            },
            onConfirm = { name, description, isPublic, coverUri ->
                onSave(editingShowcase, name, description, isPublic, coverUri)
                showCreateDialog = false
                editingShowcase = null
            }
        )
    }

    deleteTarget?.let { showcase ->
        DeleteConfirmDialog(
            title = "删除展柜",
            text = "确定删除「${showcase.name}」吗？",
            onDismiss = { deleteTarget = null },
            onConfirm = {
                onDelete(showcase.id)
                deleteTarget = null
            }
        )
    }

    if (showAddGoodsDialog && state.selectedShowcase != null) {
        AddGoodsDialog(
            state = state,
            onSearchChanged = onUpdateAddGoodsSearch,
            onSearch = onSearchAddGoods,
            onAddGoods = onAddGoods,
            onDismiss = { showAddGoodsDialog = false }
        )
    }
}

@Composable
private fun ShowcaseCard(
    showcase: Showcase,
    baseUrl: String,
    onClick: () -> Unit,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?
) {
    SimpleListCard(
        title = showcase.name,
        subtitle = showcase.description ?: "共 ${showcase.goodsCount ?: 0} 个谷子",
        meta = if (showcase.isPublic) "公开" else "私有",
        onClick = onClick,
        onEdit = onEdit,
        onDelete = onDelete,
        leading = {
            ShowcasePreview(showcase = showcase, baseUrl = baseUrl)
        }
    )
}

@Composable
private fun ShowcasePreview(showcase: Showcase, baseUrl: String) {
    val cover = showcase.coverImage
    val previewPhotos = showcase.previewPhotos.orEmpty().filter { it.isNotBlank() }
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
        contentAlignment = Alignment.Center
    ) {
        if (!cover.isNullOrBlank()) {
            AsyncImage(
                model = resolveImageUrl(cover, baseUrl),
                contentDescription = showcase.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (previewPhotos.isNotEmpty()) {
            ShowcaseMosaicPreview(
                photos = previewPhotos,
                baseUrl = baseUrl,
                contentDescription = showcase.name
            )
        } else {
            Text("展", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ShowcaseMosaicPreview(
    photos: List<String>,
    baseUrl: String,
    contentDescription: String
) {
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(2) { rowIndex ->
            Row(modifier = Modifier.weight(1f)) {
                repeat(2) { columnIndex ->
                    val image = photos.getOrNull(rowIndex * 2 + columnIndex)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(0.5.dp)
                            .background(White.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!image.isNullOrBlank()) {
                            AsyncImage(
                                model = resolveImageUrl(image, baseUrl),
                                contentDescription = contentDescription,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowcaseDetailTab(
    state: ShowcaseUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddGoods: (() -> Unit)?,
    onGoodsClick: (String) -> Unit,
    onRemoveGoods: ((String) -> Unit)?,
    onMoveGoods: ((String, String, String) -> Unit)?
) {
    val showcase = state.selectedShowcase ?: return
    LazyColumn(
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ShowcaseHeroCard(
                showcase = showcase,
                baseUrl = state.baseUrl,
                goodsCount = state.showcaseGoods.size.takeIf { it > 0 } ?: showcase.goodsCount ?: 0,
                canAddGoods = onAddGoods != null,
                isMutating = state.isShowcaseGoodsMutating,
                onBack = onBack,
                onAddGoods = onAddGoods,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }
        state.error?.let { message ->
            item {
                ErrorMessage(message = message)
            }
        }
        if (state.isDetailLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (state.showcaseGoods.isEmpty()) {
            item { EmptyMessage("这个展柜还没有谷子") }
        } else {
            itemsIndexed(state.showcaseGoods, key = { _, item -> item.id }) { index, showcaseGoods ->
                val previous = state.showcaseGoods.getOrNull(index - 1)
                val next = state.showcaseGoods.getOrNull(index + 1)
                ShowcaseGoodsRow(
                    showcaseGoods = showcaseGoods,
                    baseUrl = state.baseUrl,
                    onClick = { onGoodsClick(showcaseGoods.goods.id) },
                    onRemove = onRemoveGoods?.let { removeGoods ->
                        { removeGoods(showcaseGoods.goods.id) }
                    },
                    onMoveUp = if (onMoveGoods != null && previous != null) {
                        { onMoveGoods(showcaseGoods.goods.id, previous.goods.id, "before") }
                    } else {
                        null
                    },
                    onMoveDown = if (onMoveGoods != null && next != null) {
                        { onMoveGoods(showcaseGoods.goods.id, next.goods.id, "after") }
                    } else {
                        null
                    },
                    canMutate = !state.isShowcaseGoodsMutating
                )
            }
        }
    }
}

@Composable
private fun ShowcaseHeroCard(
    showcase: Showcase,
    baseUrl: String,
    goodsCount: Int,
    canAddGoods: Boolean,
    isMutating: Boolean,
    onBack: () -> Unit,
    onAddGoods: (() -> Unit)?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val heroImage = showcase.coverImage ?: showcase.previewPhotos?.firstOrNull()

    PickGoodsCard(radius = 20.dp) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
                contentAlignment = Alignment.Center
            ) {
                if (!heroImage.isNullOrBlank()) {
                    AsyncImage(
                        model = resolveImageUrl(heroImage, baseUrl),
                        contentDescription = showcase.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.18f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.34f)
                                    )
                                )
                            )
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = TextLighter,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingIconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (canAddGoods && onAddGoods != null) {
                        FloatingIconButton(
                            enabled = !isMutating,
                            onClick = onAddGoods
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = "添加谷子")
                        }
                    }
                    FloatingIconButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = "编辑")
                    }
                    FloatingIconButton(onClick = onDelete) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = showcase.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = PickGoodsShape.Pill,
                        color = if (showcase.isPublic) GoldSoft else PurpleSoft
                    ) {
                        Text(
                            text = if (showcase.isPublic) "公开" else "私有",
                            color = if (showcase.isPublic) Gold else PurpleSecondary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                if (!showcase.description.isNullOrBlank()) {
                    Text(
                        text = showcase.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "共 $goodsCount 个谷子",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FloatingIconButton(
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        shape = PickGoodsShape.Pill,
        color = White.copy(alpha = 0.9f),
        shadowElevation = 3.dp,
        modifier = Modifier.padding(start = 6.dp)
    ) {
        IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(38.dp)) {
            content()
        }
    }
}

@Composable
private fun ShowcaseGoodsRow(
    showcaseGoods: ShowcaseGoods,
    baseUrl: String,
    onClick: () -> Unit,
    onRemove: (() -> Unit)?,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    canMutate: Boolean
) {
    val goods = showcaseGoods.goods
    SimpleListCard(
        title = goods.name,
        subtitle = buildString {
            append(goods.ip.name)
            if (goods.characters.isNotEmpty()) {
                append(" · ")
                append(goods.characters.take(2).joinToString("、") { it.name })
            }
            goods.locationPath?.takeIf { it.isNotBlank() }?.let {
                append(" · ")
                append(it.split('/').lastOrNull().orEmpty())
            }
        },
        meta = "x${goods.quantity}",
        onClick = onClick,
        onDelete = if (onRemove != null && canMutate) onRemove else null,
        leading = {
            ShowcaseGoodsThumb(goods = goods, baseUrl = baseUrl)
        },
        trailing = if ((onMoveUp != null || onMoveDown != null) && canMutate) {
            {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = onMoveUp ?: {},
                        enabled = onMoveUp != null
                    ) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "上移")
                    }
                    IconButton(
                        onClick = onMoveDown ?: {},
                        enabled = onMoveDown != null
                    ) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "下移")
                    }
                }
            }
        } else {
            null
        }
    )
}

@Composable
private fun ShowcaseGoodsThumb(goods: GoodsListItem, baseUrl: String) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (!goods.mainPhoto.isNullOrBlank()) {
            AsyncImage(
                model = resolveImageUrl(goods.mainPhoto, baseUrl),
                contentDescription = goods.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text("谷", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AddGoodsDialog(
    state: ShowcaseUiState,
    onSearchChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onAddGoods: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val existingIds = remember(state.showcaseGoods) {
        state.showcaseGoods.map { it.goods.id }.toSet()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加谷子到展柜") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchField(
                        value = state.addGoodsSearchQuery,
                        onValueChange = onSearchChanged,
                        placeholder = "搜索谷子名称、IP、角色",
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onSearch, enabled = !state.isAddGoodsSearching) {
                        Text("搜索")
                    }
                }
                state.addGoodsError?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                when {
                    state.isAddGoodsSearching -> Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    state.addGoodsCandidates.isEmpty() -> EmptyMessage("没有找到可添加的谷子")
                    else -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                    ) {
                        items(state.addGoodsCandidates, key = { it.id }) { goods ->
                            val exists = goods.id in existingIds
                            AddGoodsCandidateRow(
                                goods = goods,
                                baseUrl = state.baseUrl,
                                exists = exists,
                                canAdd = !state.isShowcaseGoodsMutating && !exists,
                                onAdd = { onAddGoods(goods.id) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成")
            }
        }
    )
}

@Composable
private fun AddGoodsCandidateRow(
    goods: GoodsListItem,
    baseUrl: String,
    exists: Boolean,
    canAdd: Boolean,
    onAdd: () -> Unit
) {
    SimpleListCard(
        title = goods.name,
        subtitle = buildString {
            append(goods.ip.name)
            if (goods.characters.isNotEmpty()) {
                append(" · ")
                append(goods.characters.take(2).joinToString("、") { it.name })
            }
        },
        meta = if (exists) "已在展柜" else null,
        onClick = if (canAdd) onAdd else null,
        leading = {
            ShowcaseGoodsThumb(goods = goods, baseUrl = baseUrl)
        }
    )
}

@Composable
private fun ShowcaseEditDialog(
    showcase: Showcase?,
    baseUrl: String,
    isSaving: Boolean,
    isUploadingCover: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, Boolean, String?) -> Unit
) {
    var name by remember(showcase?.id) { mutableStateOf(showcase?.name.orEmpty()) }
    var description by remember(showcase?.id) { mutableStateOf(showcase?.description.orEmpty()) }
    var isPublic by remember(showcase?.id) { mutableStateOf(showcase?.isPublic ?: true) }
    var selectedCoverUri by remember(showcase?.id) { mutableStateOf<String?>(null) }
    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedCoverUri = uri?.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (showcase == null) "新增展柜" else "编辑展柜") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 560.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("展柜名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = PickGoodsShape.Control
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = PickGoodsShape.Control
                )
                ShowcaseCoverPicker(
                    selectedCoverUri = selectedCoverUri,
                    currentCover = showcase?.coverImage,
                    baseUrl = baseUrl,
                    isUploading = isUploadingCover,
                    onPickCover = {
                        coverPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onClearSelection = { selectedCoverUri = null }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("公开展柜", modifier = Modifier.weight(1f))
                    Switch(checked = isPublic, onCheckedChange = { isPublic = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && !isSaving && !isUploadingCover,
                onClick = { onConfirm(name.trim(), description.trim(), isPublic, selectedCoverUri) }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving && !isUploadingCover) { Text("取消") }
        }
    )
}

@Composable
private fun ShowcaseCoverPicker(
    selectedCoverUri: String?,
    currentCover: String?,
    baseUrl: String,
    isUploading: Boolean,
    onPickCover: () -> Unit,
    onClearSelection: () -> Unit
) {
    val previewModel = selectedCoverUri ?: resolveImageUrl(currentCover, baseUrl)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "封面图片",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (selectedCoverUri != null) {
                Text(
                    text = "待上传",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
            contentAlignment = Alignment.Center
        ) {
            if (previewModel != null) {
                AsyncImage(
                    model = previewModel,
                    contentDescription = "展柜封面",
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
                        text = "选择 1:1 或 4:3 图片",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = onPickCover,
                enabled = !isUploading,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
                Text(if (previewModel == null) "选择封面" else "更换封面")
            }
            if (selectedCoverUri != null) {
                TextButton(
                    onClick = onClearSelection,
                    enabled = !isUploading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = null)
                    Text("清除选择")
                }
            }
        }

        if (isUploading) {
            Text(
                text = "正在压缩并上传封面...",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun StatsTab(state: ShowcaseUiState) {
    val stats = state.stats
    LazyColumn(
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "统计看板",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        if (stats == null) {
            item { EmptyMessage("正在加载统计数据...") }
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("谷子数", stats.overview.goodsCount.toString(), Modifier.weight(1f))
                    StatCard("总数量", stats.overview.quantitySum.toString(), Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("估值", stats.overview.valueSum, Modifier.weight(1f))
                    StatCard("已定位", stats.overview.withLocationCount.toString(), Modifier.weight(1f))
                }
            }
            stats.distributions?.status?.takeIf { it.isNotEmpty() }?.let { status ->
                val total = status.sumOf { it.goodsCount }
                item { DistributionSection("状态分布", total) { status.forEach { StatusDistributionRow(it, total) } } }
            }
            stats.distributions?.isOfficial?.takeIf { it.isNotEmpty() }?.let { official ->
                val total = official.sumOf { it.goodsCount }
                item { DistributionSection("官谷 / 同人", total) { official.forEach { OfficialDistributionRow(it, total) } } }
            }
            stats.distributions?.ipTop?.takeIf { it.isNotEmpty() }?.let { items ->
                item { TopRankSection("IP Top", items) { it.ipName to it.goodsCount } }
            }
            stats.distributions?.categoryTop?.takeIf { it.isNotEmpty() }?.let { items ->
                item { TopRankSection("品类 Top", items) { it.categoryName to it.goodsCount } }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    PickGoodsCard(modifier = modifier, radius = 16.dp) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DistributionSection(title: String, total: Int, content: @Composable () -> Unit) {
    PickGoodsCard(radius = 18.dp) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            content()
            if (total == 0) {
                Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatusDistributionRow(item: GoodsStatusDistributionItem, total: Int) {
    RatioRow(label = item.label, count = item.goodsCount, total = total)
}

@Composable
private fun OfficialDistributionRow(item: GoodsOfficialDistributionItem, total: Int) {
    RatioRow(label = item.label, count = item.goodsCount, total = total)
}

@Composable
private fun RatioRow(label: String, count: Int, total: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row {
            Text(label, modifier = Modifier.weight(1f))
            Text(count.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else count.toFloat() / total.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun <T> TopRankSection(
    title: String,
    items: List<T>,
    mapper: (T) -> Pair<String, Int>
) {
    val max = items.maxOfOrNull { mapper(it).second }?.coerceAtLeast(1) ?: 1
    PickGoodsCard(radius = 18.dp) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            items.take(8).forEach { item ->
                val (name, count) = mapper(item)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(count.toString(), color = MaterialTheme.colorScheme.primary)
                    }
                    LinearProgressIndicator(
                        progress = { count.toFloat() / max.toFloat() },
                        color = if (title.contains("IP")) PurpleSecondary else Gold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                HorizontalDivider()
            }
        }
    }
}
