package com.pickgoods.app.ui.metadata

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.pickgoods.app.data.model.BgmCharacter
import com.pickgoods.app.data.model.BgmSubject
import com.pickgoods.app.data.model.Character
import com.pickgoods.app.data.model.IP
import com.pickgoods.app.data.util.ImageCaptureUtils
import com.pickgoods.app.ui.common.ChoiceChipFlow
import com.pickgoods.app.ui.common.CompactActionButton
import com.pickgoods.app.ui.common.DeleteConfirmDialog
import com.pickgoods.app.ui.common.EmptyMessage
import com.pickgoods.app.ui.common.ErrorMessage
import com.pickgoods.app.ui.common.MobileHeaderCard
import com.pickgoods.app.ui.common.MobileFormSheet
import com.pickgoods.app.ui.common.MobileInfoTile
import com.pickgoods.app.ui.common.MobileSectionHeader
import com.pickgoods.app.ui.common.PickGoodsAnimatedContent
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.common.PickGoodsTopBar
import com.pickgoods.app.ui.common.SearchField
import com.pickgoods.app.ui.common.SimpleListCard
import com.pickgoods.app.ui.common.SmallChoiceChip
import com.pickgoods.app.ui.goods.components.resolveImageUrl
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.GoldSoft
import com.pickgoods.app.ui.theme.PurpleSecondary
import com.pickgoods.app.ui.theme.PurpleSoft
import com.pickgoods.app.ui.theme.White

@Composable
fun IPCharacterScreen(
    onSettingsClick: () -> Unit,
    viewModel: MetadataViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var activeTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            PickGoodsTopBar(
                title = "IP作品与角色",
                onSettingsClick = onSettingsClick,
                onRefreshClick = viewModel::refresh
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PickGoodsScreen(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                MetadataTabBar(
                    tabs = listOf("IP作品", "角色"),
                    activeTab = activeTab,
                    onTabSelected = { activeTab = it }
                )
                PickGoodsAnimatedContent(targetState = activeTab, modifier = Modifier.weight(1f)) { tab ->
                    if (tab == 0) {
                        IPListTab(
                            state = state,
                            viewModel = viewModel,
                            onOpenCharactersTab = { activeTab = 1 }
                        )
                    } else {
                        CharacterListTab(state = state, viewModel = viewModel)
                    }
                }
            }
        }
    }

    if (state.isBgmDialogOpen) {
        BgmImportDialog(
            state = state,
            viewModel = viewModel
        )
    }
}

@Composable
private fun MetadataTabBar(
    tabs: List<String>,
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    PickGoodsCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp),
        radius = 14.dp
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = activeTab == index
                val containerColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    label = "metadataTabContainer"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "metadataTabContent"
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
                            .padding(horizontal = 8.dp, vertical = 7.dp),
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
private fun IPListTab(
    state: MetadataUiState,
    viewModel: MetadataViewModel,
    onOpenCharactersTab: () -> Unit
) {
    var editing by remember { mutableStateOf<IP?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<IP?>(null) }
    val orderedIps = remember(state.ips) {
        state.ips.sortedWith(compareBy<IP> { it.order }.thenBy { it.id })
    }
    val totalCharacterCount = remember(state.ips, state.characters) {
        if (state.ips.any { it.characterCount != null }) {
            state.ips.sumOf { it.characterCount ?: 0 }
        } else {
            state.characters.size
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            MobileHeaderCard(
                title = "IP作品",
                subtitle = "${state.ips.size} 个作品",
                trailing = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CompactActionButton(
                            label = "BGM",
                            emphasized = false,
                            onClick = viewModel::openBgmImport
                        )
                        CompactActionButton(label = "新增", onClick = { showCreate = true })
                    }
                }
            ) {
                SearchField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchChanged,
                    placeholder = "搜索 IP、关键词..."
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        SmallChoiceChip(
                            label = "全部类型",
                            selected = state.ipSubjectTypeFilter == null,
                            onClick = { viewModel.setIpSubjectTypeFilter(null) }
                        )
                    }
                    items(ipSubjectTypeOptions) { type ->
                        SmallChoiceChip(
                            label = subjectTypeLabel(type),
                            selected = state.ipSubjectTypeFilter == type,
                            onClick = { viewModel.setIpSubjectTypeFilter(type) }
                        )
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MobileInfoTile(
                    label = "作品",
                    value = state.ips.size.toString(),
                    subtitle = "IP 总数",
                    accent = Gold,
                    modifier = Modifier.weight(1f)
                )
                MobileInfoTile(
                    label = "角色",
                    value = totalCharacterCount.toString(),
                    subtitle = "可绑定角色",
                    accent = PurpleSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (orderedIps.isNotEmpty()) {
            item {
                IPPreviewRail(
                    ips = orderedIps.take(14),
                    selectedId = state.selectedIpForCharacters?.id,
                    onSelect = viewModel::loadIPCharacters
                )
            }
        }
        state.selectedIpForCharacters?.let { ip ->
            item {
                IPCharactersSpotlight(
                    ip = ip,
                    characters = state.ipCharacters,
                    baseUrl = state.baseUrl,
                    isLoading = state.isIpCharactersLoading,
                    onOpenCharactersTab = {
                        viewModel.setCharacterIpFilter(ip.id)
                        onOpenCharactersTab()
                    },
                    onClear = viewModel::clearIPCharacters
                )
            }
        }
        item {
            MobileSectionHeader(
                title = "作品排序",
                subtitle = "上移/下移会同步后端展示顺序",
                accent = PurpleSecondary
            )
        }
        when {
            state.isLoading && state.ips.isEmpty() -> item { LoadingBox() }
            state.error != null && state.ips.isEmpty() -> item { ErrorMessage(state.error, viewModel::refresh) }
            state.ips.isEmpty() -> item { EmptyMessage("暂无 IP 作品") }
            else -> itemsIndexed(orderedIps, key = { _, item -> item.id }) { index, ip ->
                val keywords = ip.keywords?.joinToString(" / ") { it.value }.orEmpty()
                SimpleListCard(
                    title = ip.name,
                    subtitle = keywords.ifBlank { "角色 ${ip.characterCount ?: 0} 个" },
                    meta = ip.subjectType?.let { subjectTypeLabel(it) },
                    onClick = { viewModel.loadIPCharacters(ip) },
                    onEdit = { editing = ip },
                    onDelete = { deleteTarget = ip },
                    leading = {
                        IPTypeBadge(ip.subjectType)
                    },
                    trailing = {
                        SortButtons(
                            canMoveUp = index > 0 && !state.isSaving,
                            canMoveDown = index < orderedIps.lastIndex && !state.isSaving,
                            onMoveUp = { viewModel.moveIP(ip.id, -1) },
                            onMoveDown = { viewModel.moveIP(ip.id, 1) }
                        )
                    }
                )
            }
        }
    }

    if (showCreate || editing != null) {
        IPEditDialog(
            ip = editing,
            onDismiss = {
                showCreate = false
                editing = null
            },
            onConfirm = { name, keywords, type ->
                viewModel.saveIP(editing, name, keywords, type)
                showCreate = false
                editing = null
            }
        )
    }
    deleteTarget?.let { ip ->
        DeleteConfirmDialog(
            title = "删除 IP",
            text = "确定删除「${ip.name}」吗？",
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.deleteIP(ip.id)
                deleteTarget = null
            }
        )
    }
}

@Composable
private fun CharacterListTab(state: MetadataUiState, viewModel: MetadataViewModel) {
    var editing by remember { mutableStateOf<Character?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Character?>(null) }
    val selectedIp = state.ips.firstOrNull { it.id == state.characterIpFilterId }

    LazyColumn(
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            MobileHeaderCard(
                title = "角色列表",
                subtitle = selectedIp?.let { "${it.name} · ${state.characters.size} 个角色" }
                    ?: "${state.characters.size} 个角色",
                trailing = {
                    CompactActionButton(label = "新增", onClick = { showCreate = true })
                }
            ) {
                SearchField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchChanged,
                    placeholder = "搜索角色、所属 IP..."
                )
                if (state.ips.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            SmallChoiceChip(
                                label = "全部 IP",
                                selected = state.characterIpFilterId == null,
                                onClick = { viewModel.setCharacterIpFilter(null) }
                            )
                        }
                        items(state.ips.take(36), key = { it.id }) { ip ->
                            SmallChoiceChip(
                                label = ip.name,
                                selected = ip.id == selectedIp?.id,
                                onClick = { viewModel.setCharacterIpFilter(ip.id) }
                            )
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MobileInfoTile(
                    label = "角色",
                    value = state.characters.size.toString(),
                    subtitle = selectedIp?.name ?: "当前列表",
                    accent = Gold,
                    modifier = Modifier.weight(1f)
                )
                MobileInfoTile(
                    label = "作品",
                    value = state.ips.size.toString(),
                    subtitle = "可选归属",
                    accent = PurpleSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (state.characters.isNotEmpty()) {
            item {
                CharacterPreviewRail(
                    characters = state.characters.take(18),
                    baseUrl = state.baseUrl
                )
            }
        }
        item {
            MobileSectionHeader(
                title = "角色条目",
                subtitle = "头像、性别与所属 IP 会用于筛选和表单联动",
                accent = PurpleSecondary
            )
        }
        when {
            state.isLoading && state.characters.isEmpty() -> item { LoadingBox() }
            state.error != null && state.characters.isEmpty() -> item { ErrorMessage(state.error, viewModel::refresh) }
            state.characters.isEmpty() -> item { EmptyMessage("暂无角色") }
            else -> items(state.characters, key = { it.id }) { character ->
                SimpleListCard(
                    title = character.name,
                    subtitle = character.ip.name,
                    meta = genderLabel(character.gender),
                    onEdit = { editing = character },
                    onDelete = { deleteTarget = character },
                    leading = {
                        CharacterAvatar(character, state.baseUrl)
                    }
                )
            }
        }
    }

    if (showCreate || editing != null) {
        CharacterEditDialog(
            character = editing,
            ips = state.ips,
            baseUrl = state.baseUrl,
            onDismiss = {
                showCreate = false
                editing = null
            },
            onConfirm = { name, ipId, gender, avatar, avatarUri ->
                viewModel.saveCharacter(editing, name, ipId, gender, avatar, avatarUri)
                showCreate = false
                editing = null
            }
        )
    }
    deleteTarget?.let { character ->
        DeleteConfirmDialog(
            title = "删除角色",
            text = "确定删除「${character.name}」吗？",
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.deleteCharacter(character.id)
                deleteTarget = null
            }
        )
    }
}

@Composable
private fun IPPreviewRail(
    ips: List<IP>,
    selectedId: Int?,
    onSelect: (IP) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MobileSectionHeader(
            title = "作品速览",
            subtitle = "按展示顺序预览作品和角色数量",
            accent = Gold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(ips, key = { it.id }) { ip ->
                IPPreviewCard(
                    ip = ip,
                    selected = ip.id == selectedId,
                    onClick = { onSelect(ip) }
                )
            }
        }
    }
}

@Composable
private fun IPPreviewCard(
    ip: IP,
    selected: Boolean,
    onClick: () -> Unit
) {
    PickGoodsCard(
        modifier = Modifier.width(208.dp),
        radius = 16.dp,
        borderColor = if (selected) Gold.copy(alpha = 0.72f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subjectTypeLabel(ip.subjectType ?: 0),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(7.dp),
                    shape = PickGoodsShape.Pill,
                    color = White.copy(alpha = 0.88f)
                ) {
                    Text(
                        text = "${ip.characterCount ?: 0} 角",
                        color = Gold,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = ip.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = ip.keywords?.take(2)?.joinToString(" / ") { it.value }
                    ?: "角色 ${ip.characterCount ?: 0} 个",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun IPCharactersSpotlight(
    ip: IP,
    characters: List<Character>,
    baseUrl: String,
    isLoading: Boolean,
    onOpenCharactersTab: () -> Unit,
    onClear: () -> Unit
) {
    PickGoodsCard(radius = 16.dp) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(White, PurpleSoft.copy(alpha = 0.46f), White)))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ip.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isLoading) "正在加载角色..." else "共 ${characters.size} 个角色",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onClear, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "收起角色预览")
                }
            }

            when {
                isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 2.dp)
                }
                characters.isEmpty() -> Text(
                    text = "这个 IP 下还没有角色",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                else -> LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(characters.take(18), key = { it.id }) { character ->
                        CharacterPreviewCard(character = character, baseUrl = baseUrl)
                    }
                }
            }

            TextButton(
                onClick = onOpenCharactersTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("管理这个 IP 的角色")
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun IPTypeBadge(subjectType: Int?) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = subjectType?.let { subjectTypeLabel(it).take(2) } ?: "IP",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CharacterPreviewRail(
    characters: List<Character>,
    baseUrl: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MobileSectionHeader(
            title = "角色速览",
            subtitle = "头像优先展示，便于核对导入结果",
            accent = Gold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(characters, key = { it.id }) { character ->
                CharacterPreviewCard(character = character, baseUrl = baseUrl)
            }
        }
    }
}

@Composable
private fun CharacterPreviewCard(character: Character, baseUrl: String) {
    PickGoodsCard(
        modifier = Modifier.width(178.dp),
        radius = 16.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            CharacterAvatar(character = character, baseUrl = baseUrl, size = 92.dp)
            Text(
                text = character.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Surface(shape = PickGoodsShape.Pill, color = PurpleSoft) {
                Text(
                    text = character.ip.name,
                    color = PurpleSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun CharacterAvatar(
    character: Character,
    baseUrl: String,
    size: androidx.compose.ui.unit.Dp = 48.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
        contentAlignment = Alignment.Center
    ) {
        if (!character.avatar.isNullOrBlank()) {
            AsyncImage(
                model = resolveImageUrl(character.avatar, baseUrl),
                contentDescription = character.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(character.name.take(1), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SortButtons(
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
        IconButton(
            onClick = onMoveUp,
            enabled = canMoveUp,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = "上移")
        }
        IconButton(
            onClick = onMoveDown,
            enabled = canMoveDown,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "下移")
        }
    }
}

@Composable
private fun BgmImportDialog(
    state: MetadataUiState,
    viewModel: MetadataViewModel
) {
    val busy = state.bgmStep in setOf(
        BgmImportStep.Searching,
        BgmImportStep.LoadingCharacters,
        BgmImportStep.Importing
    )
    val confirmText = when (state.bgmStep) {
        BgmImportStep.Search -> "搜索"
        BgmImportStep.Results -> "导入 (${state.bgmSelectedCharacterIndexes.size})"
        BgmImportStep.Imported -> "完成"
        else -> "处理中"
    }
    val confirmEnabled = when (state.bgmStep) {
        BgmImportStep.Search -> state.bgmSearchQuery.isNotBlank()
        BgmImportStep.Results -> state.bgmSelectedCharacterIndexes.isNotEmpty()
        BgmImportStep.Imported -> true
        else -> false
    }

    MobileFormSheet(
        title = "从 Bangumi 导入角色",
        subtitle = "先搜索作品，再选择需要导入的角色",
        confirmText = confirmText,
        confirmEnabled = confirmEnabled,
        isBusy = busy,
        onDismiss = viewModel::closeBgmImport,
        onConfirm = {
            when (state.bgmStep) {
                BgmImportStep.Search -> viewModel.searchBgmSubjects()
                BgmImportStep.Results -> viewModel.importSelectedBgmCharacters()
                BgmImportStep.Imported -> viewModel.closeBgmImport()
                else -> Unit
            }
        }
    ) {
        state.error?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        when (state.bgmStep) {
            BgmImportStep.Search -> BgmSearchStep(state, viewModel)
            BgmImportStep.Searching -> BgmLoadingStep("正在搜索 Bangumi...")
            BgmImportStep.Subjects -> BgmSubjectsStep(state, viewModel)
            BgmImportStep.LoadingCharacters -> BgmLoadingStep("正在获取角色列表...")
            BgmImportStep.Results -> BgmCharactersStep(state, viewModel)
            BgmImportStep.Importing -> BgmLoadingStep("正在导入角色...")
            BgmImportStep.Imported -> BgmImportedStep(state)
        }
        when (state.bgmStep) {
            BgmImportStep.Subjects -> TextButton(
                onClick = { viewModel.resetBgmImport(keepOpen = true) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("返回搜索")
            }
            BgmImportStep.Results -> TextButton(
                onClick = { viewModel.searchBgmSubjects() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("重选作品")
            }
            else -> Unit
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BgmSearchStep(
    state: MetadataUiState,
    viewModel: MetadataViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = state.bgmSearchQuery,
            onValueChange = viewModel::updateBgmSearchQuery,
            label = { Text("作品关键词") },
            placeholder = { Text("例如：崩坏、孤独摇滚") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = PickGoodsShape.Control
        )
        Text("作品类型", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            bgmSubjectTypes.forEach { item ->
                FilterChip(
                    selected = state.bgmSubjectType == item.value,
                    onClick = { viewModel.updateBgmSubjectType(item.value) },
                    label = { Text(item.label) }
                )
            }
        }
        Text(
            text = "会先搜索作品列表，再选择具体作品导入角色。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        TextButton(
            onClick = viewModel::searchBgmCharactersDirect,
            enabled = state.bgmSearchQuery.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("直接按作品名获取角色")
        }
    }
}

@Composable
private fun BgmSubjectsStep(
    state: MetadataUiState,
    viewModel: MetadataViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "找到 ${state.bgmSubjects.size} 个相关作品",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.bgmSubjects.take(30).forEach { subject ->
                BgmSubjectRow(
                    subject = subject,
                    onClick = { viewModel.selectBgmSubject(subject) }
                )
            }
        }
    }
}

@Composable
private fun BgmSubjectRow(
    subject: BgmSubject,
    onClick: () -> Unit
) {
    PickGoodsCard(radius = 16.dp, onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(9.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
                contentAlignment = Alignment.Center
            ) {
                if (!subject.image.isNullOrBlank()) {
                    AsyncImage(
                        model = subject.image,
                        contentDescription = subject.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("番", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = subject.nameCn?.takeIf { it.isNotBlank() } ?: subject.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subject.name.takeIf { it != subject.nameCn } ?: subject.typeName ?: "作品",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(shape = PickGoodsShape.Pill, color = GoldSoft) {
                    Text(
                        text = subject.typeName ?: "作品",
                        color = Gold,
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
private fun BgmCharactersStep(
    state: MetadataUiState,
    viewModel: MetadataViewModel
) {
    val keyword = state.bgmCharacterKeyword.trim()
    val filtered = state.bgmCharacters.mapIndexed { index, character -> index to character }
        .filter { (_, character) ->
            keyword.isBlank() || character.name.contains(keyword, ignoreCase = true)
        }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "${state.bgmSubjectName} · ${state.bgmCharacters.size} 个角色",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = viewModel::selectAllBgmCharacters) { Text("全选") }
            TextButton(onClick = viewModel::clearBgmCharacterSelection) { Text("取消全选") }
        }
        OutlinedTextField(
            value = state.bgmCharacterKeyword,
            onValueChange = viewModel::updateBgmCharacterKeyword,
            label = { Text("筛选角色") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = PickGoodsShape.Control
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filtered.take(80).forEach { (index, character) ->
                BgmCharacterRow(
                    character = character,
                    selected = index in state.bgmSelectedCharacterIndexes,
                    onClick = { viewModel.toggleBgmCharacter(index) }
                )
            }
        }
    }
}

@Composable
private fun BgmCharacterRow(
    character: BgmCharacter,
    selected: Boolean,
    onClick: () -> Unit
) {
    PickGoodsCard(
        radius = 16.dp,
        borderColor = if (selected) Gold.copy(alpha = 0.72f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft))),
                contentAlignment = Alignment.Center
            ) {
                if (!character.avatar.isNullOrBlank()) {
                    AsyncImage(
                        model = character.avatar,
                        contentDescription = character.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(character.name.take(1), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    character.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    character.relation ?: "角色",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            FilterChip(
                selected = selected,
                onClick = onClick,
                label = { Text(if (selected) "已选" else "选择") }
            )
        }
    }
}

@Composable
private fun BgmLoadingStep(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().padding(24.dp)
    ) {
        CircularProgressIndicator()
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BgmImportedStep(state: MetadataUiState) {
    val result = state.bgmImportResult
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("导入完成", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("成功创建：${result?.created ?: 0} 个角色")
        Text("已存在跳过：${result?.skipped ?: 0} 个角色")
        result?.details?.take(6)?.forEach { detail ->
            Text(
                text = "${detail.characterName} · ${bgmImportStatusLabel(detail.status)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun IPEditDialog(
    ip: IP?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int?) -> Unit
) {
    var name by remember(ip?.id) { mutableStateOf(ip?.name.orEmpty()) }
    var keywords by remember(ip?.id) { mutableStateOf(ip?.keywords?.joinToString(", ") { it.value }.orEmpty()) }
    var subjectType by remember(ip?.id) { mutableStateOf(ip?.subjectType) }

    MobileFormSheet(
        title = if (ip == null) "新增 IP" else "编辑 IP",
        subtitle = "关键词会用于搜索和 Bangumi 导入匹配",
        confirmEnabled = name.isNotBlank(),
        onDismiss = onDismiss,
        onConfirm = { onConfirm(name.trim(), keywords.trim(), subjectType) }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("IP 名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = PickGoodsShape.Control
        )
        OutlinedTextField(
            value = keywords,
            onValueChange = { keywords = it },
            label = { Text("关键词，用逗号分隔") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            shape = PickGoodsShape.Control
        )
        ChoiceChipFlow(
            title = "作品类型",
            options = ipSubjectTypeOptions,
            selected = subjectType,
            label = { subjectTypeLabel(it) },
            onSelected = { subjectType = it },
            emptyLabel = "不设置"
        )
    }
}

@Composable
private fun CharacterEditDialog(
    character: Character?,
    ips: List<IP>,
    baseUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, String?, String?) -> Unit
) {
    var name by remember(character?.id) { mutableStateOf(character?.name.orEmpty()) }
    var selectedIpId by remember(character?.id, ips) { mutableStateOf(character?.ipId ?: character?.ip?.id ?: ips.firstOrNull()?.id) }
    var avatar by remember(character?.id) { mutableStateOf(character?.avatar.orEmpty()) }
    var selectedAvatarUri by remember(character?.id) { mutableStateOf<String?>(null) }
    var pendingAvatarCameraUri by remember(character?.id) { mutableStateOf<Uri?>(null) }
    var gender by remember(character?.id) { mutableStateOf(character?.gender ?: "other") }
    val context = LocalContext.current
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedAvatarUri = uri?.toString()
        if (uri != null) avatar = ""
    }
    val avatarCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingAvatarCameraUri?.let { uri ->
                selectedAvatarUri = uri.toString()
                avatar = ""
            }
        }
        pendingAvatarCameraUri = null
    }
    val avatarPreview = selectedAvatarUri ?: resolveImageUrl(avatar, baseUrl)
    val selectedIp = ips.firstOrNull { it.id == selectedIpId }

    MobileFormSheet(
        title = if (character == null) "新增角色" else "编辑角色",
        subtitle = selectedIp?.name ?: "选择所属 IP 后再保存角色",
        confirmEnabled = name.isNotBlank() && selectedIpId != null,
        onDismiss = onDismiss,
        onConfirm = {
            selectedIpId?.let { ipId ->
                onConfirm(name.trim(), ipId, gender, avatar.trim(), selectedAvatarUri)
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (avatarPreview != null) {
                    AsyncImage(
                        model = avatarPreview,
                        contentDescription = "角色头像",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = name.take(1).ifBlank { "角" },
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            avatarPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
                        Text(if (avatarPreview == null) "相册" else "更换")
                    }
                    TextButton(
                        onClick = {
                            val uri = ImageCaptureUtils.createCaptureUri(context)
                            pendingAvatarCameraUri = uri
                            avatarCameraLauncher.launch(uri)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                        Text("拍照")
                    }
                }
                if (selectedAvatarUri != null) {
                    TextButton(onClick = { selectedAvatarUri = null }) {
                        Icon(Icons.Outlined.Close, contentDescription = null)
                        Text("清除选择")
                    }
                }
            }
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("角色名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = PickGoodsShape.Control
        )
        ChoiceChipFlow(
            title = "所属 IP",
            options = ips,
            selected = selectedIp,
            label = { it.name },
            onSelected = { selectedIpId = it?.id },
            maxItems = 80
        )
        ChoiceChipFlow(
            title = "角色性别",
            options = genderOptions,
            selected = genderOptions.firstOrNull { it.value == gender },
            label = { it.label },
            onSelected = { selected -> selected?.let { gender = it.value } }
        )
        OutlinedTextField(
            value = avatar,
            onValueChange = {
                avatar = it
                selectedAvatarUri = null
            },
            label = { Text("头像 URL（可选）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = PickGoodsShape.Control
        )
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

private fun genderLabel(gender: String?): String {
    return when (gender) {
        "male" -> "男"
        "female" -> "女"
        else -> "其他"
    }
}

private fun subjectTypeLabel(type: Int): String {
    return when (type) {
        1 -> "书籍"
        2 -> "动画"
        3 -> "音乐"
        4 -> "游戏"
        6 -> "三次元"
        else -> "类型 $type"
    }
}

private data class BgmSubjectTypeItem(val label: String, val value: Int?)

private val bgmSubjectTypes = listOf(
    BgmSubjectTypeItem("全部", null),
    BgmSubjectTypeItem("动画", 2),
    BgmSubjectTypeItem("游戏", 4),
    BgmSubjectTypeItem("书籍", 1),
    BgmSubjectTypeItem("三次元", 6)
)

private val ipSubjectTypeOptions = listOf(2, 4, 1, 3, 6)

private data class GenderOption(val label: String, val value: String)

private val genderOptions = listOf(
    GenderOption("男", "male"),
    GenderOption("女", "female"),
    GenderOption("其他", "other")
)

private fun bgmImportStatusLabel(status: String): String {
    return when (status) {
        "created" -> "已创建"
        "already_exists" -> "已存在"
        "error" -> "失败"
        else -> status
    }
}
