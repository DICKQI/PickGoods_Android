package com.pickgoods.app.ui.showcase

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.data.model.GoodsCategoryTopItem
import com.pickgoods.app.data.model.GoodsCharacterTopItem
import com.pickgoods.app.data.model.GoodsIPTopItem
import com.pickgoods.app.data.model.GoodsLocationTopItem
import com.pickgoods.app.data.model.GoodsOfficialDistributionItem
import com.pickgoods.app.data.model.GoodsStatusDistributionItem
import com.pickgoods.app.data.model.GoodsSubjectTypeDistributionItem
import com.pickgoods.app.data.model.GoodsTrendBucket
import com.pickgoods.app.data.model.Showcase
import com.pickgoods.app.data.model.ShowcaseGoods
import com.pickgoods.app.data.repository.ShowcaseScope
import com.pickgoods.app.data.util.ImageCaptureUtils
import com.pickgoods.app.ui.common.CompactActionButton
import com.pickgoods.app.ui.common.DeleteConfirmDialog
import com.pickgoods.app.ui.common.EmptyMessage
import com.pickgoods.app.ui.common.ErrorMessage
import com.pickgoods.app.ui.common.MobileFormSheet
import com.pickgoods.app.ui.common.MobileHeaderCard
import com.pickgoods.app.ui.common.MobileInfoTile
import com.pickgoods.app.ui.common.MobileSectionHeader
import com.pickgoods.app.ui.common.PickGoodsTopBar
import com.pickgoods.app.ui.common.PickGoodsAnimatedContent
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.common.SearchField
import com.pickgoods.app.ui.common.SimpleListCard
import com.pickgoods.app.ui.common.SmallChoiceChip
import com.pickgoods.app.ui.goods.GoodsListContent
import com.pickgoods.app.ui.goods.GoodsListUiState
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
    var chromeCompact by remember { mutableStateOf(false) }
    val compactChrome = activeTab == 1 && chromeCompact
    val goodsTopPadding by animateDpAsState(
        targetValue = if (compactChrome) 4.dp else 8.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "goodsTabTopPadding"
    )
    val tabs = listOf("展柜", "谷仓", "统计看板")

    LaunchedEffect(activeTab) {
        if (activeTab != 1) {
            chromeCompact = false
        }
    }

    Scaffold(
        topBar = {
            PickGoodsTopBar(
                title = "拾谷 PickGoods",
                onSettingsClick = onSettingsClick,
                onRefreshClick = {
                    when (activeTab) {
                        0 -> showcaseViewModel.refreshShowcases()
                        1 -> goodsViewModel.refreshGoods()
                        else -> showcaseViewModel.refreshStats()
                    }
                },
                compact = compactChrome
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PickGoodsScreen(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                CloudTabBar(
                    tabs = tabs,
                    activeTab = activeTab,
                    compact = compactChrome,
                    onTabSelected = {
                        activeTab = it
                        if (it != 1) {
                            chromeCompact = false
                        }
                    }
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
                            onSimilarSeedStrategyChanged = goodsViewModel::setSimilarSeedStrategy,
                            onEnterSelectionMode = goodsViewModel::enterSelectionMode,
                            onExitSelectionMode = { goodsViewModel.exitSelectionMode(clearSelection = true) },
                            onToggleGoodsSelection = goodsViewModel::toggleGoodsSelection,
                            onRemoveGoodsSelection = goodsViewModel::removeGoodsSelection,
                            onClearGoodsSelection = goodsViewModel::clearGoodsSelection,
                            onResetFilters = goodsViewModel::resetFilters,
                            onRefreshMetadata = goodsViewModel::refreshMetadata,
                            onPageChanged = goodsViewModel::setPage,
                            onGoodsClick = onGoodsClick,
                            onCreateClick = onCreateGoods,
                            onRetry = { goodsViewModel.refreshGoods() },
                            onChromeCompactChanged = { chromeCompact = it },
                            modifier = Modifier.padding(top = goodsTopPadding)
                        )
                        2 -> StatsTab(
                            state = showcaseState,
                            goodsState = goodsState,
                            onTopChanged = showcaseViewModel::setStatsTop,
                            onGroupByChanged = showcaseViewModel::setStatsGroupBy,
                            onSearchChanged = showcaseViewModel::updateStatsSearchQuery,
                            onApplySearch = showcaseViewModel::applyStatsSearch,
                            onIpChanged = showcaseViewModel::setStatsIpFilter,
                            onCharacterChanged = showcaseViewModel::setStatsCharacterFilter,
                            onCategoryChanged = showcaseViewModel::setStatsCategoryFilter,
                            onThemeChanged = showcaseViewModel::setStatsThemeFilter,
                            onLocationChanged = showcaseViewModel::setStatsLocationFilter,
                            onPurchaseDatePresetChanged = showcaseViewModel::setStatsPurchaseDatePreset,
                            onCreatedDatePresetChanged = showcaseViewModel::setStatsCreatedDatePreset,
                            onStatusToggled = showcaseViewModel::toggleStatsStatus,
                            onOfficialChanged = showcaseViewModel::setStatsOfficialFilter,
                            onResetFilters = showcaseViewModel::resetStatsFilters
                        )
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
    compact: Boolean = false,
    onTabSelected: (Int) -> Unit
) {
    val outerHorizontal by animateDpAsState(
        targetValue = if (compact) 8.dp else 10.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "cloudTabsHorizontalPadding"
    )
    val outerVertical by animateDpAsState(
        targetValue = if (compact) 2.dp else 5.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "cloudTabsVerticalPadding"
    )
    val cardRadius by animateDpAsState(
        targetValue = if (compact) 11.dp else 14.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "cloudTabsRadius"
    )
    val rowPadding by animateDpAsState(
        targetValue = if (compact) 1.dp else 2.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "cloudTabsRowPadding"
    )
    val tabVerticalPadding by animateDpAsState(
        targetValue = if (compact) 4.dp else 7.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "cloudTabVerticalPadding"
    )

    PickGoodsCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = outerHorizontal, vertical = outerVertical),
        radius = cardRadius
    ) {
        Row(
            modifier = Modifier.padding(rowPadding),
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
                            .padding(horizontal = 8.dp, vertical = tabVerticalPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = contentColor,
                            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
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
    onAddGoods: (String, String?) -> Unit,
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
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    MobileHeaderCard(
                        title = if (state.scope == ShowcaseScope.Private) "我的展柜" else "公共展柜",
                        subtitle = showcaseListSummary(state.showcases),
                        trailing = {
                            if (state.scope == ShowcaseScope.Private) {
                                CompactActionButton(label = "新增", onClick = { showCreateDialog = true })
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
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
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        MobileInfoTile(
                            label = "展柜",
                            value = state.showcases.size.toString(),
                            subtitle = if (state.scope == ShowcaseScope.Private) "私人收藏" else "公开浏览",
                            accent = Gold,
                            modifier = Modifier.weight(1f)
                        )
                        MobileInfoTile(
                            label = "谷子",
                            value = showcaseListMetricValue(state.showcases),
                            subtitle = showcaseListMetricSubtitle(state.showcases),
                            accent = PurpleSecondary,
                            modifier = Modifier.weight(1f)
                        )
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
                    else -> items(state.showcases.chunked(2), key = { row -> row.joinToString("-") { it.id } }) { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { showcase ->
                                ShowcaseCard(
                                    showcase = showcase,
                                    baseUrl = state.baseUrl,
                                    modifier = Modifier.weight(1f),
                                    compact = true,
                                    onClick = { onSelect(showcase) },
                                    onEdit = if (state.scope == ShowcaseScope.Private) ({ editingShowcase = showcase }) else null,
                                    onDelete = if (state.scope == ShowcaseScope.Private) ({ deleteTarget = showcase }) else null
                                )
                            }
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
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
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?
) {
    val cover = showcase.coverImage
    val previewPhotos = showcase.previewPhotos.orEmpty().filter { it.isNotBlank() }
    val badgeText = showcaseBadgeText(showcase)
    PickGoodsCard(
        modifier = modifier.fillMaxWidth(),
        radius = 16.dp,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 188.dp else 206.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
            contentAlignment = Alignment.Center
        ) {
            when {
                !cover.isNullOrBlank() -> AsyncImage(
                    model = resolveImageUrl(cover, baseUrl),
                    contentDescription = showcase.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                previewPhotos.isNotEmpty() -> ShowcaseMosaicPreview(
                    photos = previewPhotos,
                    baseUrl = baseUrl,
                    contentDescription = showcase.name
                )
                else -> Text(
                    text = "展",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.08f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.64f)
                            )
                        )
                    )
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                shape = PickGoodsShape.Pill,
                color = White.copy(alpha = 0.9f)
            ) {
                Text(
                    text = if (showcase.isPublic) "公开" else "私有",
                    color = if (showcase.isPublic) Gold else PurpleSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }

            if (onEdit != null || onDelete != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = PickGoodsShape.Pill,
                    color = White.copy(alpha = 0.9f)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                        onEdit?.let {
                            IconButton(onClick = it, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Outlined.Edit, contentDescription = "编辑展柜", tint = PurpleSecondary)
                            }
                        }
                        onDelete?.let {
                            IconButton(onClick = it, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Outlined.Delete, contentDescription = "删除展柜", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = if (badgeText == null) 12.dp else 68.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = showcase.name,
                    color = White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = showcaseCardSubtitle(showcase),
                    color = White.copy(alpha = 0.84f),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (badgeText != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    shape = PickGoodsShape.Pill,
                    color = Color.Black.copy(alpha = 0.42f)
                ) {
                    Text(
                        text = badgeText,
                        color = White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun showcaseKnownGoodsTotal(showcases: List<Showcase>): Int? {
    val counts = showcases.mapNotNull { it.goodsCount }
    return counts.takeIf { it.isNotEmpty() }?.sum()
}

private fun showcasePreviewTotal(showcases: List<Showcase>): Int {
    return showcases.sumOf { it.previewPhotos.orEmpty().count(String::isNotBlank) }
}

private fun showcaseListSummary(showcases: List<Showcase>): String {
    val knownTotal = showcaseKnownGoodsTotal(showcases)
    return if (knownTotal != null) {
        "${showcases.size} 个展柜 · $knownTotal 件谷子"
    } else {
        "${showcases.size} 个展柜 · 点击进入查看内容"
    }
}

private fun showcaseListMetricValue(showcases: List<Showcase>): String {
    return (showcaseKnownGoodsTotal(showcases) ?: showcasePreviewTotal(showcases)).toString()
}

private fun showcaseListMetricSubtitle(showcases: List<Showcase>): String {
    return if (showcaseKnownGoodsTotal(showcases) != null) "展柜内条目" else "封面预览图"
}

private fun showcaseCardSubtitle(showcase: Showcase): String {
    showcase.description?.takeIf { it.isNotBlank() }?.let { return it }
    showcase.goodsCount?.let { return "共 $it 个谷子" }
    val previewCount = showcase.previewPhotos.orEmpty().count(String::isNotBlank)
    return if (previewCount > 0) "已生成 $previewCount 张预览图" else "点击查看展柜内容"
}

private fun showcaseBadgeText(showcase: Showcase): String? {
    showcase.goodsCount?.let { return "$it 件" }
    val previewCount = showcase.previewPhotos.orEmpty().count(String::isNotBlank)
    return previewCount.takeIf { it > 0 }?.let { "$it 图" }
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
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
            item {
                MobileSectionHeader(
                    title = "展柜谷子",
                    subtitle = "大图浏览，长按感更接近 Web 展柜",
                    accent = Gold
                )
            }
            itemsIndexed(
                state.showcaseGoods,
                key = { _, item -> item.id }
            ) { index, showcaseGoods ->
                val previous = state.showcaseGoods.getOrNull(index - 1)
                val next = state.showcaseGoods.getOrNull(index + 1)
                ShowcaseGoodsGridCard(
                    showcaseGoods = showcaseGoods,
                    baseUrl = state.baseUrl,
                    canMutate = !state.isShowcaseGoodsMutating,
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
                    modifier = Modifier.fillMaxWidth()
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
                    .height(252.dp)
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
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
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
private fun ShowcaseGoodsGridCard(
    showcaseGoods: ShowcaseGoods,
    baseUrl: String,
    canMutate: Boolean,
    onClick: () -> Unit,
    onRemove: (() -> Unit)?,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val goods = showcaseGoods.goods
    val image = resolveImageUrl(goods.mainPhoto, baseUrl)
    PickGoodsCard(
        modifier = modifier,
        radius = 16.dp,
        onClick = onClick
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(274.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
                contentAlignment = Alignment.Center
            ) {
                if (image != null) {
                    AsyncImage(
                        model = image,
                        contentDescription = goods.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Outlined.Image, contentDescription = null, tint = TextLighter, modifier = Modifier.size(34.dp))
                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f))
                            )
                        )
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = PickGoodsShape.Pill,
                    color = White.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = if (goods.isOfficial) "官谷" else "同人",
                        color = if (goods.isOfficial) Gold else PurpleSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (goods.quantity > 1) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        shape = PickGoodsShape.Pill,
                        color = Color.Black.copy(alpha = 0.44f)
                    ) {
                        Text(
                            text = "x${goods.quantity}",
                            color = White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (canMutate && (onRemove != null || onMoveUp != null || onMoveDown != null)) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        shape = PickGoodsShape.Pill,
                        color = White.copy(alpha = 0.9f)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                            if (onMoveUp != null) {
                                IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "上移", modifier = Modifier.size(18.dp))
                                }
                            }
                            if (onMoveDown != null) {
                                IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "下移", modifier = Modifier.size(18.dp))
                                }
                            }
                            if (onRemove != null) {
                                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = "移出展柜",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = goods.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        append(goods.ip.name)
                        if (goods.characters.isNotEmpty()) {
                            append(" · ")
                            append(goods.characters.take(2).joinToString("、") { it.name })
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = goods.category.name,
                    color = PurpleSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ShowcaseGoodsThumb(
    goods: GoodsListItem,
    baseUrl: String,
    size: androidx.compose.ui.unit.Dp = 64.dp
) {
    Box(
        modifier = Modifier
            .size(size)
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
    onAddGoods: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var notes by remember(state.selectedShowcase?.id) { mutableStateOf("") }
    val existingIds = remember(state.showcaseGoods) {
        state.showcaseGoods.map { it.goods.id }.toSet()
    }

    MobileFormSheet(
        title = "添加谷子到展柜",
        subtitle = "搜索后点选条目即可加入当前展柜",
        confirmText = "完成",
        isBusy = state.isShowcaseGoodsMutating,
        onDismiss = onDismiss,
        onConfirm = onDismiss
    ) {
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
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("加入展柜备注（可选）") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2,
            shape = PickGoodsShape.Control
        )
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
            else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.addGoodsCandidates.take(30).forEach { goods ->
                    val exists = goods.id in existingIds
                    AddGoodsCandidateRow(
                        goods = goods,
                        baseUrl = state.baseUrl,
                        exists = exists,
                        canAdd = !state.isShowcaseGoodsMutating && !exists,
                        onAdd = { onAddGoods(goods.id, notes.trim().ifBlank { null }) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddGoodsCandidateRow(
    goods: GoodsListItem,
    baseUrl: String,
    exists: Boolean,
    canAdd: Boolean,
    onAdd: () -> Unit
) {
    PickGoodsCard(
        radius = 16.dp,
        borderColor = if (exists) Gold.copy(alpha = 0.24f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
        onClick = if (canAdd) onAdd else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(9.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShowcaseGoodsThumb(goods = goods, baseUrl = baseUrl, size = 76.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = goods.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        append(goods.ip.name)
                        if (goods.characters.isNotEmpty()) {
                            append(" · ")
                            append(goods.characters.take(2).joinToString("、") { it.name })
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    shape = PickGoodsShape.Pill,
                    color = if (exists) GoldSoft else PurpleSoft
                ) {
                    Text(
                        text = if (exists) "已在展柜" else "点选加入",
                        color = if (exists) Gold else PurpleSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
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
    var pendingCoverCameraUri by remember(showcase?.id) { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedCoverUri = uri?.toString()
    }
    val coverCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCoverCameraUri?.let { selectedCoverUri = it.toString() }
        }
        pendingCoverCameraUri = null
    }

    MobileFormSheet(
        title = if (showcase == null) "新增展柜" else "编辑展柜",
        subtitle = "封面会优先显示，未设置时使用谷子图片拼贴",
        confirmEnabled = name.isNotBlank(),
        isBusy = isSaving || isUploadingCover,
        onDismiss = onDismiss,
        onConfirm = { onConfirm(name.trim(), description.trim(), isPublic, selectedCoverUri) }
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
            onTakeCover = {
                val uri = ImageCaptureUtils.createCaptureUri(context)
                pendingCoverCameraUri = uri
                coverCameraLauncher.launch(uri)
            },
            onClearSelection = { selectedCoverUri = null }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("公开展柜", modifier = Modifier.weight(1f))
            Switch(checked = isPublic, onCheckedChange = { isPublic = it })
        }
    }
}

@Composable
private fun ShowcaseCoverPicker(
    selectedCoverUri: String?,
    currentCover: String?,
    baseUrl: String,
    isUploading: Boolean,
    onPickCover: () -> Unit,
    onTakeCover: () -> Unit,
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
                .height(184.dp)
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
                Text("相册")
            }
            TextButton(
                onClick = onTakeCover,
                enabled = !isUploading,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                Text("拍照")
            }
        }
        if (selectedCoverUri != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "待上传",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onClearSelection,
                    enabled = !isUploading
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
private fun StatsTab(
    state: ShowcaseUiState,
    goodsState: GoodsListUiState,
    onTopChanged: (Int) -> Unit,
    onGroupByChanged: (String) -> Unit,
    onSearchChanged: (String) -> Unit,
    onApplySearch: () -> Unit,
    onIpChanged: (Int?) -> Unit,
    onCharacterChanged: (Int?) -> Unit,
    onCategoryChanged: (Int?) -> Unit,
    onThemeChanged: (Int?) -> Unit,
    onLocationChanged: (Int?) -> Unit,
    onPurchaseDatePresetChanged: (StatsDatePreset) -> Unit,
    onCreatedDatePresetChanged: (StatsDatePreset) -> Unit,
    onStatusToggled: (String) -> Unit,
    onOfficialChanged: (Boolean?) -> Unit,
    onResetFilters: () -> Unit
) {
    val stats = state.stats
    val filter = state.statsFilter
    val groupLabel = stats?.meta?.groupBy?.let(::statsGroupLabel) ?: statsGroupLabel(filter.groupBy)
    val scopeLabel = statsScopeLabel(filter, goodsState)

    LazyColumn(
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            MobileHeaderCard(
                title = "统计看板",
                subtitle = "Top ${stats?.meta?.top ?: filter.top} · $groupLabel · $scopeLabel",
                trailing = {
                    if (state.isStatsLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    }
                }
            ) {
                state.statsError?.takeIf { stats != null }?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item {
            StatsFilterPanel(
                filter = filter,
                goodsState = goodsState,
                onTopChanged = onTopChanged,
                onGroupByChanged = onGroupByChanged,
                onSearchChanged = onSearchChanged,
                onApplySearch = onApplySearch,
                onIpChanged = onIpChanged,
                onCharacterChanged = onCharacterChanged,
                onCategoryChanged = onCategoryChanged,
                onThemeChanged = onThemeChanged,
                onLocationChanged = onLocationChanged,
                onPurchaseDatePresetChanged = onPurchaseDatePresetChanged,
                onCreatedDatePresetChanged = onCreatedDatePresetChanged,
                onStatusToggled = onStatusToggled,
                onOfficialChanged = onOfficialChanged,
                onResetFilters = onResetFilters
            )
        }

        if (stats == null) {
            item {
                if (state.statsError != null) {
                    ErrorMessage(message = state.statsError, onRetry = onResetFilters)
                } else {
                    EmptyMessage(if (state.isStatsLoading) "正在加载统计数据..." else "暂无统计数据")
                }
            }
        } else {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard(
                            label = "谷子",
                            value = stats.overview.goodsCount.toString(),
                            subtitle = "记录数",
                            accent = Gold,
                            compact = true,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "数量",
                            value = stats.overview.quantitySum.toString(),
                            subtitle = "合计",
                            accent = PurpleSecondary,
                            compact = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    StatCard(
                        label = "估值",
                        value = formatMoney(stats.overview.valueSum),
                        subtitle = "已填价",
                        accent = Color(0xFFFF9A9E),
                        compact = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item { CompletenessSection(stats.overview) }

            stats.distributions?.status?.takeIf { it.isNotEmpty() }?.let { status ->
                val total = status.sumOf { it.goodsCount }
                item {
                    DistributionSection("状态分布", total, Gold) {
                        status.forEach { StatusDistributionRow(it, total) }
                    }
                }
            }

            stats.distributions?.isOfficial?.takeIf { it.isNotEmpty() }?.let { official ->
                val total = official.sumOf { it.goodsCount }
                item {
                    DistributionSection("官谷 / 同人", total, PurpleSecondary) {
                        official.forEach { OfficialDistributionRow(it, total) }
                    }
                }
            }

            stats.distributions?.ipSubjectType?.takeIf { it.isNotEmpty() }?.let { subjectTypes ->
                val total = subjectTypes.sumOf { it.goodsCount }
                item {
                    DistributionSection("作品类型", total, Color(0xFF84FAB0)) {
                        subjectTypes.forEach { SubjectTypeDistributionRow(it, total) }
                    }
                }
            }

            stats.distributions?.ipTop?.takeIf { it.isNotEmpty() }?.let { items ->
                item {
                    TopRankSection(
                        title = "IP Top ${stats.meta?.top ?: filter.top}",
                        items = items,
                        accent = PurpleSecondary
                    ) {
                        RankDisplayItem(
                            label = it.ipName,
                            count = it.goodsCount,
                            detail = it.subjectTypeLabel
                        )
                    }
                }
            }

            stats.distributions?.categoryTop?.takeIf { it.isNotEmpty() }?.let { items ->
                item {
                    TopRankSection(
                        title = "品类 Top ${stats.meta?.top ?: filter.top}",
                        items = items,
                        accent = Gold
                    ) {
                        RankDisplayItem(
                            label = it.categoryPathName ?: it.categoryName,
                            count = it.goodsCount,
                            detail = it.valueSum?.let(::formatMoney)
                        )
                    }
                }
            }

            stats.distributions?.characterTop?.takeIf { it.isNotEmpty() }?.let { items ->
                item {
                    TopRankSection(
                        title = "角色 Top ${stats.meta?.top ?: filter.top}",
                        items = items,
                        accent = Color(0xFFFF9A9E)
                    ) {
                        RankDisplayItem(
                            label = it.characterName ?: "未关联角色",
                            count = it.goodsCount,
                            detail = it.ipName
                        )
                    }
                }
            }

            stats.distributions?.locationTop?.takeIf { it.isNotEmpty() }?.let { items ->
                item {
                    TopRankSection(
                        title = "位置 Top ${stats.meta?.top ?: filter.top}",
                        items = items,
                        accent = Color(0xFF84FAB0)
                    ) {
                        RankDisplayItem(
                            label = it.locationPathName ?: it.locationName ?: "未定位",
                            count = it.goodsCount,
                            detail = it.valueSum?.let(::formatMoney)
                        )
                    }
                }
            }

            stats.trends?.purchaseDate?.takeIf { it.isNotEmpty() }?.let { trend ->
                item { TrendSection("入手趋势", trend, Gold) }
            }
            stats.trends?.createdAt?.takeIf { it.isNotEmpty() }?.let { trend ->
                item { TrendSection("录入趋势", trend, PurpleSecondary) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatsFilterPanel(
    filter: StatsFilterState,
    goodsState: GoodsListUiState,
    onTopChanged: (Int) -> Unit,
    onGroupByChanged: (String) -> Unit,
    onSearchChanged: (String) -> Unit,
    onApplySearch: () -> Unit,
    onIpChanged: (Int?) -> Unit,
    onCharacterChanged: (Int?) -> Unit,
    onCategoryChanged: (Int?) -> Unit,
    onThemeChanged: (Int?) -> Unit,
    onLocationChanged: (Int?) -> Unit,
    onPurchaseDatePresetChanged: (StatsDatePreset) -> Unit,
    onCreatedDatePresetChanged: (StatsDatePreset) -> Unit,
    onStatusToggled: (String) -> Unit,
    onOfficialChanged: (Boolean?) -> Unit,
    onResetFilters: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val filteredCharacters = remember(goodsState.characters, filter.ipId) {
        goodsState.characters.filter { character ->
            filter.ipId == null || character.ip.id == filter.ipId || character.ipId == filter.ipId
        }
    }

    PickGoodsCard(radius = 16.dp) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "统计筛选",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null
                    )
                    Text(if (expanded) "收起" else "更多")
                }
                TextButton(onClick = onResetFilters) {
                    Text("重置")
                }
            }

            StatsChipGroup(label = "Top N") {
                listOf(5, 10, 20, 30).forEach { top ->
                    SmallChoiceChip(
                        label = "Top $top",
                        selected = filter.top == top,
                        onClick = { onTopChanged(top) }
                    )
                }
            }

            StatsChipGroup(label = "趋势粒度") {
                listOf("month" to "月", "week" to "周", "day" to "日").forEach { (value, label) ->
                    SmallChoiceChip(
                        label = label,
                        selected = filter.groupBy == value,
                        onClick = { onGroupByChanged(value) }
                    )
                }
            }

            if (expanded) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchField(
                        value = filter.searchQuery,
                        onValueChange = onSearchChanged,
                        placeholder = "统计内搜索谷子、IP、角色...",
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onApplySearch) {
                        Text("应用")
                    }
                }

                StatsChipGroup(label = "IP 作品") {
                    SmallChoiceChip(
                        label = "全部",
                        selected = filter.ipId == null,
                        onClick = { onIpChanged(null) }
                    )
                    goodsState.ips.take(24).forEach { ip ->
                        SmallChoiceChip(
                            label = ip.name,
                            selected = filter.ipId == ip.id,
                            onClick = { onIpChanged(ip.id) }
                        )
                    }
                }

                StatsChipGroup(label = "角色") {
                    SmallChoiceChip(
                        label = "全部",
                        selected = filter.characterIds.isEmpty() && filter.characterId == null,
                        onClick = { onCharacterChanged(null) }
                    )
                    filteredCharacters.take(32).forEach { character ->
                        SmallChoiceChip(
                            label = character.name,
                            selected = character.id in filter.characterIds ||
                                (filter.characterIds.isEmpty() && filter.characterId == character.id),
                            onClick = { onCharacterChanged(character.id) }
                        )
                    }
                }

                StatsChipGroup(label = "品类") {
                    SmallChoiceChip(
                        label = "全部",
                        selected = filter.categoryId == null,
                        onClick = { onCategoryChanged(null) }
                    )
                    goodsState.categories.take(28).forEach { category ->
                        SmallChoiceChip(
                            label = category.pathName ?: category.name,
                            selected = filter.categoryId == category.id,
                            onClick = { onCategoryChanged(category.id) }
                        )
                    }
                }

                StatsChipGroup(label = "主题") {
                    SmallChoiceChip(
                        label = "全部",
                        selected = filter.themeId == null,
                        onClick = { onThemeChanged(null) }
                    )
                    goodsState.themes.take(24).forEach { theme ->
                        SmallChoiceChip(
                            label = theme.name,
                            selected = filter.themeId == theme.id,
                            onClick = { onThemeChanged(theme.id) }
                        )
                    }
                }

                StatsChipGroup(label = "位置") {
                    SmallChoiceChip(
                        label = "全部",
                        selected = filter.locationId == null,
                        onClick = { onLocationChanged(null) }
                    )
                    goodsState.locations.take(32).forEach { location ->
                        SmallChoiceChip(
                            label = location.pathName ?: location.name,
                            selected = filter.locationId == location.id,
                            onClick = { onLocationChanged(location.id) }
                        )
                    }
                }

                StatsChipGroup(label = "入手日期") {
                    statsDatePresets.forEach { preset ->
                        SmallChoiceChip(
                            label = preset.label,
                            selected = filter.purchaseDatePreset == preset,
                            onClick = { onPurchaseDatePresetChanged(preset) }
                        )
                    }
                }

                StatsChipGroup(label = "录入日期") {
                    statsDatePresets.forEach { preset ->
                        SmallChoiceChip(
                            label = preset.label,
                            selected = filter.createdDatePreset == preset,
                            onClick = { onCreatedDatePresetChanged(preset) }
                        )
                    }
                }

                StatsChipGroup(label = "状态") {
                    statsStatusOptions.forEach { (value, label) ->
                        SmallChoiceChip(
                            label = label,
                            selected = value in filter.statuses,
                            onClick = { onStatusToggled(value) }
                        )
                    }
                }

                StatsChipGroup(label = "官非") {
                    listOf(null to "全部", true to "官谷", false to "同人").forEach { (value, label) ->
                        SmallChoiceChip(
                            label = label,
                            selected = filter.isOfficial == value,
                            onClick = { onOfficialChanged(value) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatsChipGroup(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    subtitle: String,
    accent: Color,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    PickGoodsCard(modifier = modifier, radius = 16.dp) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 10.dp,
                vertical = if (compact) 9.dp else 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 5.dp)
        ) {
            Surface(shape = PickGoodsShape.Pill, color = accent.copy(alpha = 0.14f)) {
                Text(
                    text = label,
                    color = accent,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Text(
                text = value,
                style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CompletenessSection(overview: com.pickgoods.app.data.model.GoodsStatsOverview) {
    val goodsTotal = overview.goodsCount
    PickGoodsCard(radius = 16.dp) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "资料完整度",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            CompletenessRow(
                label = "位置",
                done = overview.withLocationCount,
                missing = overview.missingLocationCount,
                fallbackTotal = goodsTotal,
                accent = Gold
            )
            CompletenessRow(
                label = "主图",
                done = overview.withMainPhotoCount,
                missing = overview.missingMainPhotoCount,
                fallbackTotal = goodsTotal,
                accent = PurpleSecondary
            )
            CompletenessRow(
                label = "价格",
                done = overview.withPriceCount,
                missing = overview.missingPriceCount,
                fallbackTotal = goodsTotal,
                accent = Color(0xFFFF9A9E)
            )
            CompletenessRow(
                label = "入手日期",
                done = overview.withPurchaseDateCount,
                missing = overview.missingPurchaseDateCount,
                fallbackTotal = goodsTotal,
                accent = Color(0xFF84FAB0)
            )
        }
    }
}

@Composable
private fun CompletenessRow(
    label: String,
    done: Int,
    missing: Int,
    fallbackTotal: Int,
    accent: Color
) {
    val total = completenessTotal(done, missing, fallbackTotal)
    val ratio = safeRatio(done, total)
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$done/$total · ${formatPercent(ratio)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }
        LinearProgressIndicator(
            progress = { ratio },
            color = accent,
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(PickGoodsShape.Pill)
        )
    }
}

@Composable
private fun DistributionSection(
    title: String,
    total: Int,
    accent: Color,
    content: @Composable () -> Unit
) {
    PickGoodsCard(radius = 16.dp) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            SectionHeading(title = title, caption = "共 $total 件", accent = accent)
            content()
            if (total == 0) {
                Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatusDistributionRow(item: GoodsStatusDistributionItem, total: Int) {
    RatioRow(label = item.label, count = item.goodsCount, total = total, accent = Gold)
}

@Composable
private fun OfficialDistributionRow(item: GoodsOfficialDistributionItem, total: Int) {
    RatioRow(label = item.label, count = item.goodsCount, total = total, accent = PurpleSecondary)
}

@Composable
private fun SubjectTypeDistributionRow(item: GoodsSubjectTypeDistributionItem, total: Int) {
    RatioRow(label = item.label, count = item.goodsCount, total = total, accent = Color(0xFF84FAB0))
}

@Composable
private fun RatioRow(label: String, count: Int, total: Int, accent: Color) {
    val ratio = safeRatio(count, total)
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$count · ${formatPercent(ratio)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }
        LinearProgressIndicator(
            progress = { ratio },
            color = accent,
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(PickGoodsShape.Pill)
        )
    }
}

@Composable
private fun <T> TopRankSection(
    title: String,
    items: List<T>,
    accent: Color,
    limit: Int = 8,
    mapper: (T) -> RankDisplayItem
) {
    val visibleItems = items
        .map(mapper)
        .filter { it.label.isNotBlank() }
        .take(limit)
    val max = visibleItems.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    PickGoodsCard(radius = 16.dp) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            SectionHeading(title = title, caption = "按谷子件数排序", accent = accent)
            visibleItems.forEachIndexed { index, item ->
                RankRow(index = index, item = item, max = max, accent = accent)
                if (index != visibleItems.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
                }
            }
        }
    }
}

@Composable
private fun RankRow(index: Int, item: RankDisplayItem, max: Int, accent: Color) {
    val ratio = safeRatio(item.count, max)
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = PickGoodsShape.Pill, color = accent.copy(alpha = 0.14f), modifier = Modifier.size(26.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = (index + 1).toString(),
                        color = accent,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                item.detail?.takeIf { it.isNotBlank() }?.let { detail ->
                    Text(
                        text = detail,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Text(
                text = item.count.toString(),
                color = accent,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { ratio },
            color = accent,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(PickGoodsShape.Pill)
        )
    }
}

@Composable
private fun TrendSection(title: String, items: List<GoodsTrendBucket>, accent: Color) {
    val visibleItems = items.filter { !it.bucket.isNullOrBlank() }.takeLast(8)
    val max = visibleItems.maxOfOrNull { it.goodsCount }?.coerceAtLeast(1) ?: 1
    PickGoodsCard(radius = 16.dp) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            SectionHeading(title = title, caption = "最近 ${visibleItems.size} 个周期", accent = accent)
            visibleItems.forEach { bucket ->
                RatioRow(
                    label = formatBucket(bucket.bucket),
                    count = bucket.goodsCount,
                    total = max,
                    accent = accent
                )
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String, caption: String, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        Surface(shape = PickGoodsShape.Pill, color = accent.copy(alpha = 0.14f)) {
            Text(
                text = caption,
                color = accent,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

private data class RankDisplayItem(
    val label: String,
    val count: Int,
    val detail: String? = null
)

private val statsStatusOptions = listOf(
    "draft" to "草稿",
    "in_cabinet" to "在馆",
    "outdoor" to "出街",
    "sold" to "已出"
)

private val statsDatePresets = listOf(
    StatsDatePreset.ALL,
    StatsDatePreset.LAST_30_DAYS,
    StatsDatePreset.LAST_90_DAYS,
    StatsDatePreset.THIS_YEAR
)

private fun statsGroupLabel(groupBy: String): String {
    return when (groupBy) {
        "day" -> "按日"
        "week" -> "按周"
        else -> "按月"
    }
}

private fun statsScopeLabel(filter: StatsFilterState, goodsState: GoodsListUiState): String {
    val pieces = mutableListOf<String>()
    filter.searchQuery.takeIf { it.isNotBlank() }?.let { pieces += "搜索「${it.trim()}」" }
    goodsState.ips.firstOrNull { it.id == filter.ipId }?.let { pieces += it.name }
    val selectedCharacters = goodsState.characters.filter { it.id in filter.characterIds }
    when {
        selectedCharacters.size > 2 -> pieces += "${selectedCharacters.size} 个角色"
        selectedCharacters.isNotEmpty() -> pieces += selectedCharacters.joinToString(" / ") { it.name }
        filter.characterIds.isNotEmpty() -> pieces += "${filter.characterIds.size} 个角色"
        else -> goodsState.characters.firstOrNull { it.id == filter.characterId }?.let { pieces += it.name }
    }
    goodsState.categories.firstOrNull { it.id == filter.categoryId }?.let {
        pieces += (it.pathName ?: it.name)
    }
    goodsState.themes.firstOrNull { it.id == filter.themeId }?.let { pieces += it.name }
    goodsState.locations.firstOrNull { it.id == filter.locationId }?.let {
        pieces += (it.pathName ?: it.name)
    }
    if (filter.purchaseDatePreset != StatsDatePreset.ALL) {
        pieces += "入手${filter.purchaseDatePreset.label}"
    }
    if (filter.createdDatePreset != StatsDatePreset.ALL) {
        pieces += "录入${filter.createdDatePreset.label}"
    }
    if (filter.statuses.isNotEmpty()) {
        pieces += if (filter.statuses.size == 1) {
            statsStatusOptions.firstOrNull { it.first == filter.statuses.first() }?.second ?: "状态"
        } else {
            "多状态"
        }
    }
    filter.isOfficial?.let { pieces += if (it) "官谷" else "同人" }
    return pieces.takeIf { it.isNotEmpty() }?.joinToString(" / ") ?: "全部范围"
}

private fun formatMoney(value: String?): String {
    val cleaned = value?.takeIf { it.isNotBlank() } ?: "0"
    return "¥$cleaned"
}

private fun formatBucket(bucket: String?): String {
    val value = bucket?.takeIf { it.isNotBlank() } ?: return "未知"
    val date = value.take(10)
    return if (date.length == 10 && date.endsWith("-01")) date.take(7) else date
}

private fun completenessTotal(done: Int, missing: Int, fallbackTotal: Int): Int {
    val combined = done + missing
    return when {
        combined > 0 -> combined
        fallbackTotal > 0 -> fallbackTotal
        else -> 0
    }
}

private fun safeRatio(count: Int, total: Int): Float {
    return if (total <= 0) 0f else (count.toFloat() / total.toFloat()).coerceIn(0f, 1f)
}

private fun formatPercent(ratio: Float): String {
    return "${(ratio * 100).toInt()}%"
}
