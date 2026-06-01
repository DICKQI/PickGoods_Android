package com.pickgoods.app.ui.goods

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.GoldSoft
import com.pickgoods.app.ui.theme.PurpleSecondary
import com.pickgoods.app.ui.theme.SurfaceGray
import com.pickgoods.app.ui.theme.White

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

    Scaffold(
        topBar = {
            if (showTopBar) {
                PickGoodsTopBar(
                    title = "✦ 拾谷 PickGoods",
                    onSettingsClick = onSettingsClick,
                    onRefreshClick = { viewModel.refreshGoods() }
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
                onResetFilters = viewModel::resetFilters,
                onRefreshMetadata = viewModel::refreshMetadata,
                onPageChanged = viewModel::setPage,
                onGoodsClick = onGoodsClick,
                onCreateClick = onCreateClick,
                onRetry = { viewModel.refreshGoods() }
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
    onResetFilters: () -> Unit = {},
    onRefreshMetadata: () -> Unit = {},
    onPageChanged: (Int) -> Unit = {},
    onGoodsClick: (String) -> Unit = {},
    onCreateClick: () -> Unit = {},
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    val showPagination = uiState.viewMode == GoodsViewMode.STANDARD && uiState.totalPages > 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PickGoodsCard(
            modifier = Modifier.fillMaxWidth(),
            radius = 18.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = { Text("搜索谷子名称、IP、角色...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = PickGoodsShape.Control,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.36f),
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    CompactToolbarButton(
                        label = "筛选",
                        icon = Icons.Outlined.Tune,
                        onClick = { showFilterSheet = true }
                    )
                    CompactToolbarButton(
                        label = "新增",
                        icon = Icons.Outlined.Add,
                        emphasized = true,
                        onClick = onCreateClick
                    )
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    item {
                        SummaryPill(
                            text = activeFilterSummary(uiState),
                            highlighted = uiState.groupBy != null || uiState.viewMode == GoodsViewMode.SIMILAR_RANDOM
                        )
                    }
                    items(statusFilters) { item ->
                        val selected = if (item.value == null) {
                            uiState.statusFilter == null && uiState.statusIn == null
                        } else {
                            uiState.statusFilter == item.value && uiState.statusIn == null
                        }
                        CompactFilterChip(
                            selected = selected,
                            onClick = { onStatusFilterChanged(item.value) },
                            label = item.label
                        )
                    }
                    items(officialFilters) { item ->
                        CompactFilterChip(
                            selected = uiState.officialFilter == item.value,
                            onClick = { onOfficialFilterChanged(item.value) },
                            label = if (item.value == null) "官非" else item.label
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val contentState = when {
                uiState.isLoading && uiState.goods.isEmpty() -> "loading"
                uiState.error != null && uiState.goods.isEmpty() -> "error"
                uiState.goods.isEmpty() -> "empty"
                else -> "list"
            }

            PickGoodsAnimatedContent(targetState = contentState, modifier = Modifier.fillMaxSize()) { state ->
                when (state) {
                    "loading" -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        contentPadding = PaddingValues(bottom = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(6) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ShimmerBlock(modifier = Modifier.fillMaxWidth().height(172.dp), radius = 16.dp)
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
                            bottomPadding = if (showPagination) 70.dp else 14.dp,
                            onGoodsClick = onGoodsClick
                        )
                    }
                }
            }

            if (showPagination) {
                GoodsPaginationDock(
                    page = uiState.page,
                    totalPages = uiState.totalPages,
                    totalCount = uiState.totalCount,
                    onPageChanged = onPageChanged,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                )
            }
        }
    }

    if (showFilterSheet) {
        GoodsFilterSheet(
            uiState = uiState,
            onDismiss = { showFilterSheet = false },
            onStatusSelectionChanged = onStatusSelectionChanged,
            onOfficialFilterChanged = onOfficialFilterChanged,
            onIpFilterChanged = onIpFilterChanged,
            onCharacterFilterChanged = onCharacterFilterChanged,
            onCategoryFilterChanged = onCategoryFilterChanged,
            onThemeFilterChanged = onThemeFilterChanged,
            onLocationFilterChanged = onLocationFilterChanged,
            onGroupByChanged = onGroupByChanged,
            onViewModeChanged = onViewModeChanged,
            onResetFilters = onResetFilters,
            onRefreshMetadata = onRefreshMetadata
        )
    }
}

@Composable
private fun GoodsGrid(
    uiState: GoodsListUiState,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onGoodsClick: (String) -> Unit
) {
    val groups = remember(uiState.goods, uiState.groupBy) {
        groupGoods(uiState.goods, uiState.groupBy)
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(bottom = bottomPadding),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (uiState.groupBy.isNullOrBlank()) {
            items(uiState.goods, key = { it.id }) { goods ->
                GoodsCard(goods = goods, baseUrl = uiState.baseUrl, onClick = { onGoodsClick(goods.id) })
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
                    GoodsCard(goods = goods, baseUrl = uiState.baseUrl, onClick = { onGoodsClick(goods.id) })
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
    PickGoodsCard(
        modifier = modifier,
        radius = 22.dp
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            White.copy(alpha = 0.96f),
                            SurfaceGray.copy(alpha = 0.92f),
                            White.copy(alpha = 0.96f)
                        )
                    )
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaginationIconButton(
                enabled = page > 1,
                onClick = { onPageChanged((page - 1).coerceAtLeast(1)) },
                icon = Icons.Filled.KeyboardArrowLeft,
                contentDescription = "上一页"
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "第 $page / $totalPages 页",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "共 $totalCount 件谷子",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PaginationIconButton(
                enabled = page < totalPages,
                onClick = { onPageChanged((page + 1).coerceAtMost(totalPages)) },
                icon = Icons.Filled.KeyboardArrowRight,
                contentDescription = "下一页"
            )
        }
    }
}

@Composable
private fun PaginationIconButton(
    enabled: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String
) {
    Surface(
        shape = PickGoodsShape.Pill,
        color = if (enabled) PurpleSecondary.copy(alpha = 0.14f) else SurfaceGray,
    ) {
        IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) PurpleSecondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
            )
        }
    }
}

@Composable
private fun CompactToolbarButton(
    label: String,
    icon: ImageVector,
    emphasized: Boolean = false,
    onClick: () -> Unit
) {
    val container = if (emphasized) PurpleSecondary else SurfaceGray.copy(alpha = 0.8f)
    val content = if (emphasized) White else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = PickGoodsShape.Pill,
        color = container,
        shadowElevation = if (emphasized) 2.dp else 0.dp,
        modifier = Modifier
            .height(44.dp)
            .clip(PickGoodsShape.Pill)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
            Text(
                text = label,
                color = content,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SummaryPill(
    text: String,
    highlighted: Boolean
) {
    Surface(
        modifier = Modifier.widthIn(max = 180.dp),
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
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
        modifier = Modifier.height(34.dp),
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
                    SelectionChip("全部", uiState.selectedCharacterId == null) { onCharacterFilterChanged(null) }
                    filteredCharacters.take(50).forEach { character ->
                        SelectionChip(
                            label = character.name,
                            selected = uiState.selectedCharacterId == character.id,
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
    uiState.characters.firstOrNull { it.id == uiState.selectedCharacterId }?.let { pieces += it.name }
    uiState.categories.firstOrNull { it.id == uiState.selectedCategoryId }?.let { pieces += it.name }
    uiState.themes.firstOrNull { it.id == uiState.selectedThemeId }?.let { pieces += it.name }
    uiState.locations.firstOrNull { it.id == uiState.selectedLocationId }?.let { pieces += it.name }
    uiState.groupBy?.let { pieces += groupByOptions.firstOrNull { option -> option.value == it }?.label ?: it }
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
