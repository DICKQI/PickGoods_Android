package com.pickgoods.app.ui.metadata

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.pickgoods.app.ui.common.AddButton
import com.pickgoods.app.ui.common.DeleteConfirmDialog
import com.pickgoods.app.ui.common.EmptyMessage
import com.pickgoods.app.ui.common.ErrorMessage
import com.pickgoods.app.ui.common.PickGoodsAnimatedContent
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.common.PickGoodsTopBar
import com.pickgoods.app.ui.common.SearchField
import com.pickgoods.app.ui.common.SimpleListCard

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
                        IPListTab(state = state, viewModel = viewModel)
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
private fun IPListTab(state: MetadataUiState, viewModel: MetadataViewModel) {
    var editing by remember { mutableStateOf<IP?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<IP?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PickGoodsCard(radius = 18.dp) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("IP作品", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        TextButton(onClick = viewModel::openBgmImport) {
                            Text("BGM导入")
                        }
                        AddButton(onClick = { showCreate = true })
                    }
                    SearchField(
                        value = state.searchQuery,
                        onValueChange = viewModel::onSearchChanged,
                        placeholder = "搜索 IP、关键词..."
                    )
                }
            }
        }
        when {
            state.isLoading && state.ips.isEmpty() -> item { LoadingBox() }
            state.error != null && state.ips.isEmpty() -> item { ErrorMessage(state.error, viewModel::refresh) }
            state.ips.isEmpty() -> item { EmptyMessage("暂无 IP 作品") }
            else -> items(state.ips, key = { it.id }) { ip ->
                val keywords = ip.keywords?.joinToString(" / ") { it.value }.orEmpty()
                SimpleListCard(
                    title = ip.name,
                    subtitle = keywords.ifBlank { "角色 ${ip.characterCount ?: 0} 个" },
                    meta = ip.subjectType?.let { subjectTypeLabel(it) },
                    onEdit = { editing = ip },
                    onDelete = { deleteTarget = ip },
                    leading = {
                        Text("IP", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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

    LazyColumn(
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PickGoodsCard(radius = 18.dp) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("角色列表", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        AddButton(onClick = { showCreate = true })
                    }
                    SearchField(
                        value = state.searchQuery,
                        onValueChange = viewModel::onSearchChanged,
                        placeholder = "搜索角色、所属 IP..."
                    )
                }
            }
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
                        CharacterAvatar(character)
                    }
                )
            }
        }
    }

    if (showCreate || editing != null) {
        CharacterEditDialog(
            character = editing,
            ips = state.ips,
            onDismiss = {
                showCreate = false
                editing = null
            },
            onConfirm = { name, ipId, gender, avatar ->
                viewModel.saveCharacter(editing, name, ipId, gender, avatar)
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
private fun CharacterAvatar(character: Character) {
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape),
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
}

@Composable
private fun BgmImportDialog(
    state: MetadataUiState,
    viewModel: MetadataViewModel
) {
    AlertDialog(
        onDismissRequest = viewModel::closeBgmImport,
        title = { Text("从 Bangumi 导入角色") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
            }
        },
        confirmButton = {
            when (state.bgmStep) {
                BgmImportStep.Search -> {
                    TextButton(
                        enabled = state.bgmSearchQuery.isNotBlank(),
                        onClick = viewModel::searchBgmSubjects
                    ) {
                        Text("搜索")
                    }
                }
                BgmImportStep.Results -> {
                    TextButton(
                        enabled = state.bgmSelectedCharacterIndexes.isNotEmpty(),
                        onClick = viewModel::importSelectedBgmCharacters
                    ) {
                        Text("确认导入 (${state.bgmSelectedCharacterIndexes.size})")
                    }
                }
                BgmImportStep.Imported -> {
                    TextButton(onClick = viewModel::closeBgmImport) {
                        Text("完成")
                    }
                }
                else -> Unit
            }
        },
        dismissButton = {
            when (state.bgmStep) {
                BgmImportStep.Subjects -> TextButton(onClick = { viewModel.resetBgmImport(keepOpen = true) }) { Text("返回搜索") }
                BgmImportStep.Results -> TextButton(onClick = { viewModel.searchBgmSubjects() }) { Text("重选作品") }
                BgmImportStep.Search, BgmImportStep.Imported -> TextButton(onClick = viewModel::closeBgmImport) { Text("取消") }
                else -> Unit
            }
        }
    )
}

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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
        LazyColumn(
            modifier = Modifier.heightIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.bgmSubjects, key = { it.id }) { subject ->
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
    SimpleListCard(
        title = subject.nameCn?.takeIf { it.isNotBlank() } ?: subject.name,
        subtitle = subject.name.takeIf { it != subject.nameCn } ?: subject.typeName,
        meta = subject.typeName,
        onClick = onClick,
        leading = {
            Box(modifier = Modifier.size(56.dp).clip(PickGoodsShape.Control), contentAlignment = Alignment.Center) {
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
        }
    )
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
        LazyColumn(
            modifier = Modifier.heightIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.first }) { (index, character) ->
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
    PickGoodsCard(radius = 14.dp, onClick = onClick) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
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
                Text(character.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
    var typeText by remember(ip?.id) { mutableStateOf(ip?.subjectType?.toString().orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ip == null) "新增 IP" else "编辑 IP") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("IP 名称") }, singleLine = true)
                OutlinedTextField(value = keywords, onValueChange = { keywords = it }, label = { Text("关键词，用逗号分隔") })
                OutlinedTextField(
                    value = typeText,
                    onValueChange = { typeText = it.filter(Char::isDigit) },
                    label = { Text("作品类型") },
                    supportingText = { Text("2 动画 / 4 游戏 / 6 三次元等，可留空") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onConfirm(name.trim(), keywords.trim(), typeText.toIntOrNull()) }) {
                Text("保存")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun CharacterEditDialog(
    character: Character?,
    ips: List<IP>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, String?) -> Unit
) {
    var name by remember(character?.id) { mutableStateOf(character?.name.orEmpty()) }
    var ipIdText by remember(character?.id) { mutableStateOf((character?.ipId ?: character?.ip?.id ?: ips.firstOrNull()?.id)?.toString().orEmpty()) }
    var avatar by remember(character?.id) { mutableStateOf(character?.avatar.orEmpty()) }
    var gender by remember(character?.id) { mutableStateOf(character?.gender ?: "other") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (character == null) "新增角色" else "编辑角色") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("角色名称") }, singleLine = true)
                OutlinedTextField(
                    value = ipIdText,
                    onValueChange = { ipIdText = it.filter(Char::isDigit) },
                    label = { Text("所属 IP ID") },
                    supportingText = {
                        val candidates = ips.take(4).joinToString(" / ") { "${it.id}:${it.name}" }
                        if (candidates.isNotBlank()) Text("可选：$candidates")
                    },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("male" to "男", "female" to "女", "other" to "其他").forEach { item ->
                        FilterChip(selected = gender == item.first, onClick = { gender = item.first }, label = { Text(item.second) })
                    }
                }
                OutlinedTextField(value = avatar, onValueChange = { avatar = it }, label = { Text("头像 URL（可选）") })
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && ipIdText.toIntOrNull() != null,
                onClick = { onConfirm(name.trim(), ipIdText.toInt(), gender, avatar.trim()) }
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
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

private fun bgmImportStatusLabel(status: String): String {
    return when (status) {
        "created" -> "已创建"
        "already_exists" -> "已存在"
        "error" -> "失败"
        else -> status
    }
}
