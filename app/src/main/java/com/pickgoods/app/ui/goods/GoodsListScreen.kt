package com.pickgoods.app.ui.goods

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.ui.common.PickGoodsTopBar
import com.pickgoods.app.ui.common.EmptyMessage
import com.pickgoods.app.ui.common.ErrorMessage
import com.pickgoods.app.ui.common.GoldAccentLine
import com.pickgoods.app.ui.common.PickGoodsAnimatedContent
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.common.ShimmerBlock
import com.pickgoods.app.ui.goods.components.GoodsCard
import com.pickgoods.app.ui.goods.components.resolveImageUrl
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.GoldSoft
import com.pickgoods.app.ui.theme.PurpleSecondary
import com.pickgoods.app.ui.theme.PurpleSoft
import com.pickgoods.app.ui.theme.SurfaceGray
import com.pickgoods.app.ui.theme.TextLighter
import com.pickgoods.app.ui.theme.White
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoodsListScreen(
    onSettingsClick: (() -> Unit)? = null,
    onGoodsClick: (String) -> Unit = {},
    onCreateClick: () -> Unit = {},
    showTopBar: Boolean = true,
    viewModel: GoodsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var chromeCompact by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (showTopBar) {
                PickGoodsTopBar(
                    title = "拾谷 PickGoods",
                    onSettingsClick = onSettingsClick,
                    onRefreshClick = { viewModel.refreshGoods() },
                    compact = chromeCompact
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PickGoodsScreen(modifier = Modifier.padding(paddingValues)) {
            GoodsListContent(
                uiState = uiState,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onStatusFilterChanged = viewModel::setStatusFilter,
                onStatusSelectionChanged = viewModel::setStatusSelection,
                onOfficialFilterChanged = viewModel::setOfficialFilter,
                onIpFilterChanged = viewModel::setIpFilter,
                onCharacterFilterChanged = viewModel::setCharacterFilter,
                onCategoryFilterChanged = viewModel::setCategoryFilter,
                onThemeFilterChanged = viewModel::setThemeFilter,
                onLocationFilterChanged = viewModel::setLocationFilter,
                onGroupByChanged = viewModel::setGroupBy,
                onViewModeChanged = viewModel::setViewMode,
                onSimilarSeedStrategyChanged = viewModel::setSimilarSeedStrategy,
                onEnterSelectionMode = viewModel::enterSelectionMode,
                onExitSelectionMode = { viewModel.exitSelectionMode(clearSelection = true) },
                onToggleGoodsSelection = viewModel::toggleGoodsSelection,
                onRemoveGoodsSelection = viewModel::removeGoodsSelection,
                onClearGoodsSelection = viewModel::clearGoodsSelection,
                onResetFilters = viewModel::resetFilters,
                onRefreshMetadata = viewModel::refreshMetadata,
                onPageChanged = viewModel::setPage,
                onGoodsClick = onGoodsClick,
                onCreateClick = onCreateClick,
                onRetry = { viewModel.refreshGoods() },
                onChromeCompactChanged = { chromeCompact = it }
            )
        }
    }
}

@Composable
fun GoodsListContent(
    uiState: GoodsListUiState,
    onSearchQueryChanged: (String) -> Unit,
    onStatusFilterChanged: (String?) -> Unit,
    onStatusSelectionChanged: (Set<String>) -> Unit = {},
    onOfficialFilterChanged: (Boolean?) -> Unit,
    onIpFilterChanged: (Int?) -> Unit = {},
    onCharacterFilterChanged: (Int?) -> Unit = {},
    onCategoryFilterChanged: (Int?) -> Unit = {},
    onThemeFilterChanged: (Int?) -> Unit = {},
    onLocationFilterChanged: (Int?) -> Unit = {},
    onGroupByChanged: (String?) -> Unit = {},
    onViewModeChanged: (GoodsViewMode) -> Unit = {},
    onSimilarSeedStrategyChanged: (String) -> Unit = {},
    onEnterSelectionMode: () -> Unit = {},
    onExitSelectionMode: () -> Unit = {},
    onToggleGoodsSelection: (GoodsListItem) -> Unit = {},
    onRemoveGoodsSelection: (String) -> Unit = {},
    onClearGoodsSelection: () -> Unit = {},
    onResetFilters: () -> Unit = {},
    onRefreshMetadata: () -> Unit = {},
    onPageChanged: (Int) -> Unit = {},
    onGoodsClick: (String) -> Unit = {},
    onCreateClick: () -> Unit = {},
    onRetry: () -> Unit,
    onChromeCompactChanged: (Boolean) -> Unit = {},
    bottomChromePadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    var filterExpanded by remember { mutableStateOf(false) }
    var searchVisible by remember { mutableStateOf(false) }
    var showMultiDisplaySheet by remember { mutableStateOf(false) }
    val showPagination = uiState.viewMode == GoodsViewMode.STANDARD && uiState.totalPages > 1
    val selectedGoods = remember(uiState.selectedGoodsById) { uiState.selectedGoodsById.values.toList() }
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val contentState = when {
        uiState.isLoading && uiState.goods.isEmpty() -> "loading"
        uiState.error != null && uiState.goods.isEmpty() -> "error"
        uiState.goods.isEmpty() -> "empty"
        else -> "list"
    }
    val compactControls by remember(contentState) {
        derivedStateOf {
            contentState == "list" &&
                (gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 72)
        }
    }
    val showFloatingPagination by remember(showPagination, contentState) {
        derivedStateOf {
            if (!showPagination || contentState != "list") {
                false
            } else {
                val layoutInfo = gridState.layoutInfo
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                totalItems > 0 && lastVisibleIndex >= totalItems - 3
            }
        }
    }
    val controlCardRadius by animateDpAsState(
        targetValue = if (compactControls) 12.dp else 14.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "goodsControlsRadius"
    )
    val controlHorizontalPadding by animateDpAsState(
        targetValue = if (compactControls) 7.dp else 8.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "goodsControlsHorizontalPadding"
    )
    val controlVerticalPadding by animateDpAsState(
        targetValue = if (compactControls) 5.dp else 7.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "goodsControlsVerticalPadding"
    )
    val controlSpacing by animateDpAsState(
        targetValue = if (compactControls) 5.dp else 7.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "goodsControlsSpacing"
    )
    val searchHeight by animateDpAsState(
        targetValue = if (compactControls) 42.dp else 46.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "goodsSearchHeight"
    )
    val gridBottomPadding by animateDpAsState(
        targetValue = bottomChromePadding + if (showFloatingPagination) 72.dp else 12.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "goodsGridBottomPadding"
    )
    val paginationBottomPadding by animateDpAsState(
        targetValue = bottomChromePadding + 10.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "goodsPaginationBottomPadding"
    )
    val showSearchField = searchVisible || uiState.searchQuery.isNotBlank()
    val revealSearchConnection = remember(gridState, uiState.searchQuery) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) {
                    return Offset.Zero
                }
                val atTop = gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0
                if (atTop && available.y > 10f) {
                    searchVisible = true
                } else if (available.y < -10f && uiState.searchQuery.isBlank()) {
                    searchVisible = false
                }
                return Offset.Zero
            }
        }
    }
    val changePage: (Int) -> Unit = { page ->
        onPageChanged(page)
        coroutineScope.launch {
            gridState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(compactControls) {
        onChromeCompactChanged(compactControls)
        if (compactControls) {
            filterExpanded = false
            if (uiState.searchQuery.isBlank()) {
                searchVisible = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(revealSearchConnection)
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        AnimatedVisibility(
            visible = showSearchField,
            enter = expandVertically(animationSpec = tween(190, easing = FastOutSlowInEasing)) + fadeIn(tween(150)),
            exit = shrinkVertically(animationSpec = tween(170, easing = FastOutSlowInEasing)) + fadeOut(tween(130))
        ) {
            PickGoodsCard(
                modifier = Modifier.fillMaxWidth(),
                radius = controlCardRadius
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = controlHorizontalPadding, vertical = controlVerticalPadding),
                    verticalArrangement = Arrangement.spacedBy(controlSpacing)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = {
                            Text(
                                text = if (compactControls) "搜索谷子" else "搜名称 / IP / 角色",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        onSearchQueryChanged("")
                                        searchVisible = false
                                    }
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(searchHeight),
                        shape = PickGoodsShape.Control,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.36f),
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.selectionMode,
            enter = expandVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) + fadeIn(tween(160)),
            exit = shrinkVertically(animationSpec = tween(160, easing = FastOutSlowInEasing)) + fadeOut(tween(140))
        ) {
            SelectionStatusBar(
                selectedCount = selectedGoods.size,
                onDisplay = {
                    if (selectedGoods.isNotEmpty()) {
                        showMultiDisplaySheet = true
                    }
                },
                onClear = onClearGoodsSelection
            )
        }

        AnimatedVisibility(
            visible = !compactControls,
            enter = expandVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) + fadeIn(tween(160)),
            exit = shrinkVertically(animationSpec = tween(160, easing = FastOutSlowInEasing)) + fadeOut(tween(140))
        ) {
            GoodsInlineFilterPanel(
                uiState = uiState,
                expanded = filterExpanded,
                onExpandedChange = { filterExpanded = it },
                onStatusSelectionChanged = onStatusSelectionChanged,
                onOfficialFilterChanged = onOfficialFilterChanged,
                onIpFilterChanged = onIpFilterChanged,
                onCharacterFilterChanged = onCharacterFilterChanged,
                onCategoryFilterChanged = onCategoryFilterChanged,
                onThemeFilterChanged = onThemeFilterChanged,
                onLocationFilterChanged = onLocationFilterChanged,
                onGroupByChanged = onGroupByChanged,
                onViewModeChanged = onViewModeChanged,
                onSimilarSeedStrategyChanged = onSimilarSeedStrategyChanged,
                onResetFilters = onResetFilters,
                onRefreshMetadata = onRefreshMetadata
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            PickGoodsAnimatedContent(targetState = contentState, modifier = Modifier.fillMaxSize()) { state ->
                when (state) {
                    "loading" -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(158.dp),
                            contentPadding = PaddingValues(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(6) {
                                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    ShimmerBlock(modifier = Modifier.fillMaxWidth().aspectRatio(0.86f), radius = 16.dp)
                                    ShimmerBlock(modifier = Modifier.fillMaxWidth().height(18.dp), radius = 8.dp)
                                    ShimmerBlock(modifier = Modifier.fillMaxWidth(0.72f).height(14.dp), radius = 8.dp)
                                }
                            }
                        }
                    }

                    "error" -> {
                        ErrorMessage(
                            message = uiState.error ?: "加载失败",
                            onRetry = onRetry
                        )
                    }

                    "empty" -> EmptyMessage("暂无谷子数据")

                    else -> {
                        GoodsGrid(
                            uiState = uiState,
                            state = gridState,
                            bottomPadding = gridBottomPadding,
                            onGoodsClick = onGoodsClick,
                            onToggleGoodsSelection = onToggleGoodsSelection
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showFloatingPagination,
                enter = slideInVertically(
                    animationSpec = tween(240, easing = FastOutSlowInEasing),
                    initialOffsetY = { it + 18 }
                ) + fadeIn(tween(180)),
                exit = slideOutVertically(
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    targetOffsetY = { it + 18 }
                ) + fadeOut(tween(150)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                GoodsPaginationDock(
                    page = uiState.page,
                    totalPages = uiState.totalPages,
                    totalCount = uiState.totalCount,
                    onPageChanged = changePage,
                    modifier = Modifier
                        .padding(
                            start = 8.dp,
                            top = 10.dp,
                            end = 8.dp,
                            bottom = paginationBottomPadding
                        )
                )
            }
        }
    }

    if (showMultiDisplaySheet) {
        GoodsMultiDisplaySheet(
            selectedGoods = selectedGoods,
            baseUrl = uiState.baseUrl,
            onRemove = onRemoveGoodsSelection,
            onClear = onClearGoodsSelection,
            onDismiss = { showMultiDisplaySheet = false }
        )
    }
}

@Composable
private fun GoodsGrid(
    uiState: GoodsListUiState,
    state: LazyGridState,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onGoodsClick: (String) -> Unit,
    onToggleGoodsSelection: (GoodsListItem) -> Unit
) {
    val groups = remember(uiState.goods, uiState.groupBy) {
        groupGoods(uiState.goods, uiState.groupBy)
    }
    val selectedIds = uiState.selectedGoodsById.keys

    LazyVerticalGrid(
        columns = GridCells.Adaptive(158.dp),
        state = state,
        contentPadding = PaddingValues(bottom = bottomPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (uiState.groupBy.isNullOrBlank()) {
            items(uiState.goods, key = { it.id }) { goods ->
                GoodsCard(
                    goods = goods,
                    baseUrl = uiState.baseUrl,
                    selectable = uiState.selectionMode,
                    selected = goods.id in selectedIds,
                    onSelect = { onToggleGoodsSelection(goods) },
                    onClick = { onGoodsClick(goods.id) }
                )
            }
        } else {
            groups.forEach { group ->
                item(
                    key = "group-${group.label}",
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    GoodsGroupHeader(
                        label = group.label,
                        count = group.items.size
                    )
                }
                items(group.items, key = { it.id }) { goods ->
                    GoodsCard(
                        goods = goods,
                        baseUrl = uiState.baseUrl,
                        selectable = uiState.selectionMode,
                        selected = goods.id in selectedIds,
                        onSelect = { onToggleGoodsSelection(goods) },
                        onClick = { onGoodsClick(goods.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GoodsGroupHeader(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = PickGoodsShape.Control,
        color = White.copy(alpha = 0.92f),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = PickGoodsShape.Pill,
                    color = GoldSoft
                ) {
                    Text(
                        text = "$count 件",
                        style = MaterialTheme.typography.labelMedium,
                        color = Gold,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }
            GoldAccentLine(
                modifier = Modifier
                    .padding(top = 9.dp)
                    .height(1.dp)
            )
        }
    }
}

@Composable
private fun GoodsPaginationDock(
    page: Int,
    totalPages: Int,
    totalCount: Int,
    onPageChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .widthIn(max = 214.dp)
            .clip(PickGoodsShape.Pill),
        shape = PickGoodsShape.Pill,
        color = White.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, Gold.copy(alpha = 0.22f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            White.copy(alpha = 0.98f),
                            GoldSoft.copy(alpha = 0.44f),
                            White.copy(alpha = 0.98f)
                        )
                    )
                )
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaginationIconButton(
                enabled = page > 1,
                onClick = { onPageChanged((page - 1).coerceAtLeast(1)) },
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "上一页"
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = "$page / $totalPages",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Surface(
                    shape = PickGoodsShape.Pill,
                    color = White.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.18f))
                ) {
                    Text(
                        text = "$totalCount 件",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gold,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
            PaginationIconButton(
                enabled = page < totalPages,
                onClick = { onPageChanged((page + 1).coerceAtMost(totalPages)) },
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "下一页",
                emphasized = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoodsMultiDisplaySheet(
    selectedGoods: List<GoodsListItem>,
    baseUrl: String,
    onRemove: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var density by remember { mutableStateOf(GoodsDisplayDensity.Standard) }
    val gridMin = when (density) {
        GoodsDisplayDensity.Compact -> 138.dp
        GoodsDisplayDensity.Standard -> 182.dp
        GoodsDisplayDensity.Grid -> 118.dp
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 14.dp, end = 14.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = PickGoodsShape.Pill, color = SurfaceGray.copy(alpha = 0.86f)) {
                    Text(
                        text = "已选 ${selectedGoods.size} 件谷子",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onClear, enabled = selectedGoods.isNotEmpty()) {
                        Text("清空")
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "关闭")
                    }
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(displayDensityOptions) { option ->
                    FilterChip(
                        selected = density == option,
                        onClick = { density = option },
                        leadingIcon = if (option == GoodsDisplayDensity.Grid) {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.GridView,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            null
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceGray.copy(alpha = 0.72f),
                            selectedContainerColor = PurpleSecondary.copy(alpha = 0.16f),
                            selectedLabelColor = PurpleSecondary
                        ),
                        label = { Text(option.label) }
                    )
                }
            }

            if (selectedGoods.isEmpty()) {
                EmptyMessage("暂无已选谷子")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(gridMin),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp, max = 560.dp),
                    contentPadding = PaddingValues(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (density == GoodsDisplayDensity.Grid) 5.dp else 10.dp),
                    verticalArrangement = Arrangement.spacedBy(if (density == GoodsDisplayDensity.Grid) 5.dp else 10.dp)
                ) {
                    items(selectedGoods, key = { it.id }) { goods ->
                        GoodsDisplayTile(
                            goods = goods,
                            baseUrl = baseUrl,
                            density = density,
                            onRemove = { onRemove(goods.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoodsDisplayTile(
    goods: GoodsListItem,
    baseUrl: String,
    density: GoodsDisplayDensity,
    onRemove: () -> Unit
) {
    val imageUrl = resolveImageUrl(goods.mainPhoto, baseUrl)
    val imageModifier = if (density == GoodsDisplayDensity.Grid) {
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    } else {
        Modifier
            .fillMaxWidth()
            .height(if (density == GoodsDisplayDensity.Compact) 164.dp else 214.dp)
    }

    PickGoodsCard(
        radius = if (density == GoodsDisplayDensity.Grid) 8.dp else 14.dp,
        borderColor = if (density == GoodsDisplayDensity.Grid) MaterialTheme.colorScheme.outline.copy(alpha = 0.12f) else Gold.copy(alpha = 0.22f),
        containerColor = if (density == GoodsDisplayDensity.Grid) White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f)
    ) {
        Box(
            modifier = imageModifier
                .clip(RoundedCornerShape(if (density == GoodsDisplayDensity.Grid) 8.dp else 14.dp))
                .background(Brush.linearGradient(listOf(PurpleSoft, GoldSoft))),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = goods.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (density == GoodsDisplayDensity.Grid) White else MaterialTheme.colorScheme.onSurface)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = TextLighter,
                    modifier = Modifier.size(34.dp)
                )
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(30.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
                        shape = PickGoodsShape.Pill
                    )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "移除",
                    tint = White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        if (density != GoodsDisplayDensity.Grid) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = goods.name,
                    color = White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        append(goods.ip.name)
                        val characters = goods.characters.joinToString("、") { it.name }
                        if (characters.isNotBlank()) {
                            append(" / ")
                            append(characters)
                        }
                    },
                    color = White.copy(alpha = 0.66f),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PaginationIconButton(
    enabled: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    size: Dp = 30.dp,
    emphasized: Boolean = false
) {
    val containerColor = when {
        !enabled -> SurfaceGray.copy(alpha = 0.72f)
        emphasized -> PurpleSecondary
        else -> GoldSoft.copy(alpha = 0.9f)
    }
    val iconColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.36f)
        emphasized -> White
        else -> Gold
    }

    Surface(
        modifier = Modifier
            .size(size)
            .clip(PickGoodsShape.Pill)
            .clickable(enabled = enabled, onClick = onClick),
        shape = PickGoodsShape.Pill,
        color = containerColor,
        shadowElevation = if (enabled && emphasized) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun CompactToolbarButton(
    label: String,
    icon: ImageVector,
    emphasized: Boolean = false,
    size: Dp = 44.dp,
    onClick: () -> Unit
) {
    val container = if (emphasized) PurpleSecondary else SurfaceGray.copy(alpha = 0.8f)
    val content = if (emphasized) White else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = PickGoodsShape.Pill,
        color = container,
        shadowElevation = if (emphasized) 2.dp else 0.dp,
        modifier = Modifier
            .size(size)
            .clip(PickGoodsShape.Pill)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = content, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SummaryPill(
    text: String,
    highlighted: Boolean
) {
    Surface(
        modifier = Modifier.widthIn(max = 168.dp),
        shape = PickGoodsShape.Pill,
        color = if (highlighted) GoldSoft else SurfaceGray.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, if (highlighted) Gold.copy(alpha = 0.32f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Text(
            text = text,
            color = if (highlighted) Gold else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun CompactFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = White,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = PurpleSecondary.copy(alpha = 0.16f),
            selectedLabelColor = PurpleSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f),
            selectedBorderColor = PurpleSecondary.copy(alpha = 0.18f)
        ),
        label = {
            Text(
                label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@Composable
private fun SelectionModeChip(
    active: Boolean,
    count: Int,
    onClick: () -> Unit
) {
    FilterChip(
        selected = active,
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        leadingIcon = {
            Icon(
                imageVector = if (active) Icons.Outlined.Image else Icons.Outlined.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = White,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = GoldSoft,
            selectedLabelColor = Gold
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = active,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f),
            selectedBorderColor = Gold.copy(alpha = 0.35f)
        ),
        label = {
            Text(
                text = if (active && count > 0) "展示 $count" else if (active) "多选中" else "多选",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@Composable
private fun SelectionExitChip(onClick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = SurfaceGray.copy(alpha = 0.72f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
            selectedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
        ),
        label = { Text("退出") }
    )
}

@Composable
private fun SelectionStatusBar(
    selectedCount: Int,
    onDisplay: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        shape = PickGoodsShape.Control,
        color = GoldSoft.copy(alpha = 0.74f),
        border = BorderStroke(1.dp, Gold.copy(alpha = 0.26f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "已选 $selectedCount 件谷子",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "可继续搜索、筛选或翻页添加",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TextButton(onClick = onClear, enabled = selectedCount > 0) {
                Text("清空")
            }
            TextButton(onClick = onDisplay, enabled = selectedCount > 0) {
                Text("同屏")
            }
        }
    }
}

@Composable
private fun GoodsInlineFilterPanel(
    uiState: GoodsListUiState,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onStatusSelectionChanged: (Set<String>) -> Unit,
    onOfficialFilterChanged: (Boolean?) -> Unit,
    onIpFilterChanged: (Int?) -> Unit,
    onCharacterFilterChanged: (Int?) -> Unit,
    onCategoryFilterChanged: (Int?) -> Unit,
    onThemeFilterChanged: (Int?) -> Unit,
    onLocationFilterChanged: (Int?) -> Unit,
    onGroupByChanged: (String?) -> Unit,
    onViewModeChanged: (GoodsViewMode) -> Unit,
    onSimilarSeedStrategyChanged: (String) -> Unit,
    onResetFilters: () -> Unit,
    onRefreshMetadata: () -> Unit
) {
    val selectedStatuses = currentStatusSet(uiState)
    val filteredCharacters = uiState.characters.filter {
        uiState.selectedIpId == null || it.ip.id == uiState.selectedIpId || it.ipId == uiState.selectedIpId
    }

    PickGoodsCard(
        modifier = Modifier.fillMaxWidth(),
        radius = 12.dp,
        borderColor = Gold.copy(alpha = 0.24f)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(White, GoldSoft.copy(alpha = 0.72f), White)))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "筛选谷子",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Gold,
                    modifier = Modifier.weight(1f)
                )
                InlineModeToggle(
                    viewMode = uiState.viewMode,
                    onViewModeChanged = onViewModeChanged
                )
                HeaderIconButton(
                    icon = Icons.Outlined.Refresh,
                    contentDescription = "重置筛选",
                    onClick = onResetFilters
                )
                HeaderIconButton(
                    icon = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = if (expanded) "收起筛选" else "展开筛选",
                    onClick = { onExpandedChange(!expanded) }
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) + fadeIn(tween(160)),
                exit = shrinkVertically(animationSpec = tween(160, easing = FastOutSlowInEasing)) + fadeOut(tween(140))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InlineFilterDropdown(
                        label = "IP作品",
                        placeholder = "选择IP",
                        selectedLabel = uiState.ips.firstOrNull { it.id == uiState.selectedIpId }?.name,
                        options = listOf(FilterItem<Int?>("全部", null)) + uiState.ips.map { FilterItem<Int?>(it.name, it.id) },
                        onSelected = onIpFilterChanged
                    )
                    InlineFilterDropdown(
                        label = "角色",
                        placeholder = if (uiState.selectedIpId == null) "先选择IP" else "选择角色",
                        selectedLabel = selectedCharacterLabel(uiState),
                        options = listOf(FilterItem<Int?>("全部", null)) + filteredCharacters.map { FilterItem<Int?>(it.name, it.id) },
                        enabled = uiState.selectedIpId != null,
                        onSelected = onCharacterFilterChanged
                    )
                    InlineFilterDropdown(
                        label = "品类",
                        placeholder = "选择品类",
                        selectedLabel = uiState.categories.firstOrNull { it.id == uiState.selectedCategoryId }?.let { it.pathName ?: it.name },
                        options = listOf(FilterItem<Int?>("全部", null)) + uiState.categories.map { FilterItem<Int?>(it.pathName ?: it.name, it.id) },
                        onSelected = onCategoryFilterChanged
                    )
                    InlineFilterDropdown(
                        label = "主题",
                        placeholder = "选择主题",
                        selectedLabel = uiState.themes.firstOrNull { it.id == uiState.selectedThemeId }?.name,
                        options = listOf(FilterItem<Int?>("全部", null)) + uiState.themes.map { FilterItem<Int?>(it.name, it.id) },
                        onSelected = onThemeFilterChanged
                    )
                    InlineStatusSelector(
                        selectedStatuses = selectedStatuses,
                        onStatusSelectionChanged = onStatusSelectionChanged
                    )
                    InlineFilterDropdown(
                        label = "是否官谷",
                        placeholder = "全部",
                        selectedLabel = officialFilters.firstOrNull { it.value == uiState.officialFilter }?.label,
                        options = officialFilters,
                        onSelected = onOfficialFilterChanged
                    )
                    InlineFilterDropdown(
                        label = "位置",
                        placeholder = "选择位置",
                        selectedLabel = uiState.locations.firstOrNull { it.id == uiState.selectedLocationId }?.let { it.pathName ?: it.name },
                        options = listOf(FilterItem<Int?>("全部", null)) + uiState.locations.map { FilterItem<Int?>(it.pathName ?: it.name, it.id) },
                        onSelected = onLocationFilterChanged
                    )
                    InlineFilterDropdown(
                        label = "分组显示",
                        placeholder = "不分组",
                        selectedLabel = groupByOptions.firstOrNull { it.value == uiState.groupBy }?.label,
                        options = groupByOptions,
                        onSelected = onGroupByChanged
                    )
                    if (uiState.viewMode == GoodsViewMode.SIMILAR_RANDOM) {
                        InlineFilterDropdown(
                            label = "相似策略",
                            placeholder = "均衡",
                            selectedLabel = similarSeedStrategyOptions.firstOrNull { it.value == uiState.similarSeedStrategy }?.label,
                            options = similarSeedStrategyOptions,
                            onSelected = onSimilarSeedStrategyChanged
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onRefreshMetadata, enabled = !uiState.isMetadataLoading) {
                            Text(if (uiState.isMetadataLoading) "加载中..." else "刷新选项")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineModeToggle(
    viewMode: GoodsViewMode,
    onViewModeChanged: (GoodsViewMode) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = SurfaceGray.copy(alpha = 0.82f)
    ) {
        Row(modifier = Modifier.padding(3.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            InlineModeOption(
                label = "标准",
                selected = viewMode == GoodsViewMode.STANDARD,
                onClick = { onViewModeChanged(GoodsViewMode.STANDARD) }
            )
            InlineModeOption(
                label = "相似",
                selected = viewMode == GoodsViewMode.SIMILAR_RANDOM,
                onClick = { onViewModeChanged(GoodsViewMode.SIMILAR_RANDOM) }
            )
        }
    }
}

@Composable
private fun InlineModeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(7.dp),
        color = if (selected) White else SurfaceGray.copy(alpha = 0f),
        shadowElevation = if (selected) 1.dp else 0.dp,
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(7.dp))
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (selected) Gold else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SurfaceGray.copy(alpha = 0.82f),
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun <T> InlineFilterDropdown(
    label: String,
    placeholder: String,
    selectedLabel: String?,
    options: List<FilterItem<T>>,
    enabled: Boolean = true,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Box {
            Surface(
                shape = PickGoodsShape.Control,
                color = if (enabled) White else SurfaceGray.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (enabled) 0.28f else 0.16f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(PickGoodsShape.Control)
                    .clickable(enabled = enabled) { expanded = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = selectedLabel ?: placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedLabel == null) TextLighter else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.74f else 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .widthIn(min = 260.dp)
                    .heightIn(max = 280.dp)
                    .background(White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {
                            expanded = false
                            onSelected(option.value)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineStatusSelector(
    selectedStatuses: Set<String>,
    onStatusSelectionChanged: (Set<String>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            text = "状态",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            statusFilters.filter { it.value != null && it.value != "draft" }.forEach { item ->
                val status = item.value ?: return@forEach
                val selected = status in selectedStatuses
                Surface(
                    shape = PickGoodsShape.Control,
                    color = if (selected) GoldSoft else SurfaceGray.copy(alpha = 0.62f),
                    border = BorderStroke(1.dp, if (selected) Gold.copy(alpha = 0.42f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(PickGoodsShape.Control)
                        .clickable {
                            val next = if (selected) selectedStatuses - status else selectedStatuses + status
                            onStatusSelectionChanged(next)
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = item.label,
                            color = if (selected) Gold else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun selectedCharacterLabel(uiState: GoodsListUiState): String? {
    val selectedIds = uiState.selectedCharacterIds
    if (selectedIds.size > 1) {
        return "${selectedIds.size} 个角色"
    }
    val selectedId = selectedIds.singleOrNull() ?: uiState.selectedCharacterId
    return uiState.characters.firstOrNull { it.id == selectedId }?.name
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun GoodsFilterSheet(
    uiState: GoodsListUiState,
    onDismiss: () -> Unit,
    onStatusSelectionChanged: (Set<String>) -> Unit,
    onOfficialFilterChanged: (Boolean?) -> Unit,
    onIpFilterChanged: (Int?) -> Unit,
    onCharacterFilterChanged: (Int?) -> Unit,
    onCategoryFilterChanged: (Int?) -> Unit,
    onThemeFilterChanged: (Int?) -> Unit,
    onLocationFilterChanged: (Int?) -> Unit,
    onGroupByChanged: (String?) -> Unit,
    onViewModeChanged: (GoodsViewMode) -> Unit,
    onSimilarSeedStrategyChanged: (String) -> Unit,
    onResetFilters: () -> Unit,
    onRefreshMetadata: () -> Unit
) {
    val selectedStatuses = currentStatusSet(uiState)
    val filteredCharacters = uiState.characters.filter {
        uiState.selectedIpId == null || it.ip.id == uiState.selectedIpId || it.ipId == uiState.selectedIpId
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("筛选谷仓", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "按作品、角色、品类、主题和位置快速收束",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onResetFilters) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null)
                        Text("重置")
                    }
                }
            }

            item {
                FilterSection("展示模式") {
                    SelectionChip(
                        label = "标准",
                        selected = uiState.viewMode == GoodsViewMode.STANDARD,
                        onClick = { onViewModeChanged(GoodsViewMode.STANDARD) }
                    )
                    SelectionChip(
                        label = "相似",
                        selected = uiState.viewMode == GoodsViewMode.SIMILAR_RANDOM,
                        onClick = { onViewModeChanged(GoodsViewMode.SIMILAR_RANDOM) }
                    )
                }
            }

            if (uiState.viewMode == GoodsViewMode.SIMILAR_RANDOM) {
                item {
                    FilterSection("相似策略") {
                        similarSeedStrategyOptions.forEach { item ->
                            SelectionChip(
                                label = item.label,
                                selected = uiState.similarSeedStrategy == item.value,
                                onClick = { onSimilarSeedStrategyChanged(item.value) }
                            )
                        }
                    }
                }
            }

            item {
                FilterSection("IP 作品") {
                    SelectionChip("全部", uiState.selectedIpId == null) { onIpFilterChanged(null) }
                    uiState.ips.take(40).forEach { ip ->
                        SelectionChip(
                            label = ip.name,
                            selected = uiState.selectedIpId == ip.id,
                            onClick = { onIpFilterChanged(ip.id) }
                        )
                    }
                }
            }

            item {
                FilterSection("角色") {
                    SelectionChip(
                        "全部",
                        uiState.selectedCharacterIds.isEmpty() && uiState.selectedCharacterId == null
                    ) { onCharacterFilterChanged(null) }
                    filteredCharacters.take(50).forEach { character ->
                        SelectionChip(
                            label = character.name,
                            selected = character.id in uiState.selectedCharacterIds ||
                                (uiState.selectedCharacterIds.isEmpty() && uiState.selectedCharacterId == character.id),
                            onClick = { onCharacterFilterChanged(character.id) }
                        )
                    }
                }
            }

            item {
                FilterSection("品类") {
                    SelectionChip("全部", uiState.selectedCategoryId == null) { onCategoryFilterChanged(null) }
                    uiState.categories.take(50).forEach { category ->
                        SelectionChip(
                            label = category.pathName ?: category.name,
                            selected = uiState.selectedCategoryId == category.id,
                            onClick = { onCategoryFilterChanged(category.id) }
                        )
                    }
                }
            }

            item {
                FilterSection("主题") {
                    SelectionChip("全部", uiState.selectedThemeId == null) { onThemeFilterChanged(null) }
                    uiState.themes.take(40).forEach { theme ->
                        SelectionChip(
                            label = theme.name,
                            selected = uiState.selectedThemeId == theme.id,
                            onClick = { onThemeFilterChanged(theme.id) }
                        )
                    }
                }
            }

            item {
                FilterSection("状态") {
                    statusFilters.filter { it.value != null }.forEach { item ->
                        val status = item.value ?: return@forEach
                        SelectionChip(
                            label = item.label,
                            selected = status in selectedStatuses,
                            onClick = {
                                val next = if (status in selectedStatuses) {
                                    selectedStatuses - status
                                } else {
                                    selectedStatuses + status
                                }
                                onStatusSelectionChanged(next)
                            }
                        )
                    }
                    SelectionChip(
                        label = "全部状态",
                        selected = selectedStatuses.isEmpty(),
                        onClick = { onStatusSelectionChanged(emptySet()) }
                    )
                }
            }

            item {
                FilterSection("官谷 / 同人") {
                    officialFilters.forEach { item ->
                        SelectionChip(
                            label = item.label,
                            selected = uiState.officialFilter == item.value,
                            onClick = { onOfficialFilterChanged(item.value) }
                        )
                    }
                }
            }

            item {
                FilterSection("位置") {
                    SelectionChip("全部", uiState.selectedLocationId == null) { onLocationFilterChanged(null) }
                    uiState.locations.take(50).forEach { location ->
                        SelectionChip(
                            label = location.pathName ?: location.name,
                            selected = uiState.selectedLocationId == location.id,
                            onClick = { onLocationFilterChanged(location.id) }
                        )
                    }
                }
            }

            item {
                FilterSection("分组显示") {
                    groupByOptions.forEach { item ->
                        SelectionChip(
                            label = item.label,
                            selected = uiState.groupBy == item.value,
                            onClick = { onGroupByChanged(item.value) }
                        )
                    }
                }
            }

            if (uiState.isMetadataLoading) {
                item {
                    Text(
                        "基础数据加载中...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onRefreshMetadata, modifier = Modifier.weight(1f)) {
                        Text("刷新选项")
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("完成")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    PickGoodsCard(radius = 16.dp) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SelectionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = SurfaceGray.copy(alpha = 0.68f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = PurpleSecondary.copy(alpha = 0.16f),
            selectedLabelColor = PurpleSecondary
        ),
        label = {
            Text(
                label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

private fun currentStatusSet(uiState: GoodsListUiState): Set<String> {
    val status = uiState.statusFilter
    return when {
        !uiState.statusIn.isNullOrBlank() -> uiState.statusIn.split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
        !status.isNullOrBlank() -> setOf(status)
        else -> emptySet()
    }
}

private fun activeFilterSummary(uiState: GoodsListUiState): String {
    val pieces = mutableListOf<String>()
    uiState.ips.firstOrNull { it.id == uiState.selectedIpId }?.let { pieces += it.name }
    val selectedCharacters = uiState.characters.filter { it.id in uiState.selectedCharacterIds }
    when {
        selectedCharacters.size > 2 -> pieces += "${selectedCharacters.size} 个角色"
        selectedCharacters.isNotEmpty() -> pieces += selectedCharacters.joinToString(" / ") { it.name }
        uiState.selectedCharacterIds.isNotEmpty() -> pieces += "${uiState.selectedCharacterIds.size} 个角色"
        else -> uiState.characters.firstOrNull { it.id == uiState.selectedCharacterId }?.let { pieces += it.name }
    }
    uiState.categories.firstOrNull { it.id == uiState.selectedCategoryId }?.let { pieces += it.name }
    uiState.themes.firstOrNull { it.id == uiState.selectedThemeId }?.let { pieces += it.name }
    uiState.locations.firstOrNull { it.id == uiState.selectedLocationId }?.let { pieces += it.name }
    uiState.groupBy?.let { pieces += groupByOptions.firstOrNull { option -> option.value == it }?.label ?: it }
    if (uiState.viewMode == GoodsViewMode.SIMILAR_RANDOM) {
        pieces += "相似·${similarSeedStrategyOptions.firstOrNull { it.value == uiState.similarSeedStrategy }?.label ?: "均衡"}"
    }
    if (uiState.statusIn != null) {
        pieces += "多状态"
    } else if (uiState.statusFilter == null) {
        pieces += "全部状态"
    } else {
        val status = uiState.statusFilter.orEmpty()
        pieces += statusFilters.firstOrNull { it.value == status }?.label ?: status
    }
    uiState.officialFilter?.let { pieces += if (it) "官谷" else "同人" }
    return pieces.takeIf { it.isNotEmpty() }?.joinToString(" / ") ?: "默认显示在馆谷子"
}

private data class GoodsGroup(
    val label: String,
    val items: List<GoodsListItem>
)

private enum class GoodsDisplayDensity(val label: String) {
    Compact("紧凑"),
    Standard("标准"),
    Grid("宫格")
}

private val displayDensityOptions = listOf(
    GoodsDisplayDensity.Compact,
    GoodsDisplayDensity.Standard,
    GoodsDisplayDensity.Grid
)

private fun groupGoods(
    goods: List<GoodsListItem>,
    groupBy: String?
): List<GoodsGroup> {
    if (groupBy.isNullOrBlank()) {
        return listOf(GoodsGroup("", goods))
    }
    return goods
        .groupBy { goodsItem -> groupLabel(goodsItem, groupBy) }
        .map { (label, items) -> GoodsGroup(label, items) }
}

private fun groupLabel(goods: GoodsListItem, groupBy: String): String {
    return when (groupBy) {
        "ip" -> goods.ip.name
        "character" -> goods.characters
            .takeIf { it.isNotEmpty() }
            ?.joinToString("、") { it.name }
            ?: "未设置角色"
        "category" -> goods.category.pathName ?: goods.category.name
        "theme" -> goods.theme?.name ?: "无主题"
        else -> "其他"
    }
}

private data class FilterItem<T>(val label: String, val value: T)

private val statusFilters: List<FilterItem<String?>> = listOf(
    FilterItem("在馆", "in_cabinet"),
    FilterItem("全部", null),
    FilterItem("在外", "outdoor"),
    FilterItem("已出", "sold"),
    FilterItem("草稿", "draft")
)

private val officialFilters: List<FilterItem<Boolean?>> = listOf(
    FilterItem<Boolean?>("全部", null),
    FilterItem("官谷", true),
    FilterItem("同人", false)
)

private val groupByOptions: List<FilterItem<String?>> = listOf(
    FilterItem("不分组", null),
    FilterItem("按 IP", "ip"),
    FilterItem("按角色", "character"),
    FilterItem("按品类", "category"),
    FilterItem("按主题", "theme")
)

private val similarSeedStrategyOptions: List<FilterItem<String>> = listOf(
    FilterItem("均衡", "diverse"),
    FilterItem("热门", "popular"),
    FilterItem("最近", "recent")
)
