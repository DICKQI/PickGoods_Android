package com.pickgoods.app.ui.admin

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.pickgoods.app.data.model.AdminRole
import com.pickgoods.app.data.model.AdminUser
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.ui.common.ChoiceChipFlow
import com.pickgoods.app.ui.common.CompactActionButton
import com.pickgoods.app.ui.common.DeleteConfirmDialog
import com.pickgoods.app.ui.common.EmptyMessage
import com.pickgoods.app.ui.common.ErrorMessage
import com.pickgoods.app.ui.common.MobileFormSheet
import com.pickgoods.app.ui.common.MobileHeaderCard
import com.pickgoods.app.ui.common.MobileInfoTile
import com.pickgoods.app.ui.common.MobileSectionHeader
import com.pickgoods.app.ui.common.PickGoodsBackTopBar
import com.pickgoods.app.ui.common.PickGoodsAnimatedContent
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.common.SearchField
import com.pickgoods.app.ui.common.SimpleListCard
import com.pickgoods.app.ui.goods.components.resolveImageUrl
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.GoldSoft
import com.pickgoods.app.ui.theme.PurpleSecondary
import com.pickgoods.app.ui.theme.PurpleSoft
import com.pickgoods.app.ui.theme.SurfaceGray
import com.pickgoods.app.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    onGoodsClick: (String) -> Unit,
    onCreateGoods: () -> Unit,
    onEditGoods: (String) -> Unit,
    onNavigateIp: () -> Unit,
    onNavigateCategory: () -> Unit,
    onNavigateTheme: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var activeTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            PickGoodsBackTopBar(
                title = "管理后台",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "刷新")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PickGoodsScreen(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                AdminTabBar(
                    tabs = listOf("总览", "用户", "谷子"),
                    activeTab = activeTab,
                    onTabSelected = { activeTab = it }
                )
                PickGoodsAnimatedContent(targetState = activeTab, modifier = Modifier.weight(1f)) { tab ->
                    if (tab == 0) {
                        AdminOverviewTab(
                            state = state,
                            onUsersClick = { activeTab = 1 },
                            onGoodsClick = { activeTab = 2 },
                            onNavigateIp = onNavigateIp,
                            onNavigateCategory = onNavigateCategory,
                            onNavigateTheme = onNavigateTheme
                        )
                    } else if (tab == 1) {
                        AdminUsersTab(
                            state = state,
                            onRefresh = { viewModel.loadUsers(state.usersPage) },
                            onLoadUserDetail = viewModel::loadUserDetail,
                            onClearUserDetail = viewModel::clearUserDetail,
                            onSaveUser = viewModel::saveUser,
                            onToggleActive = viewModel::toggleUserActive,
                            onPageChanged = viewModel::loadUsers
                        )
                    } else {
                        AdminGoodsTab(
                            state = state,
                            onSearchChanged = viewModel::updateGoodsSearch,
                            onSearch = viewModel::searchGoods,
                            onUserFilterChanged = viewModel::setGoodsUserFilter,
                            onStatusChanged = viewModel::setGoodsStatusFilter,
                            onIpFilterChanged = viewModel::setGoodsIpFilter,
                            onCharacterFilterChanged = viewModel::setGoodsCharacterFilter,
                            onCategoryFilterChanged = viewModel::setGoodsCategoryFilter,
                            onThemeFilterChanged = viewModel::setGoodsThemeFilter,
                            onLocationFilterChanged = viewModel::setGoodsLocationFilter,
                            onOfficialFilterChanged = viewModel::setGoodsOfficialFilter,
                            onResetFilters = viewModel::resetGoodsFilters,
                            onPageChanged = viewModel::loadGoods,
                            onGoodsClick = onGoodsClick,
                            onCreateGoods = onCreateGoods,
                            onEditGoods = onEditGoods,
                            onDeleteGoods = viewModel::deleteGoods,
                            onMoveGoods = viewModel::moveGoods
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminOverviewTab(
    state: AdminUiState,
    onUsersClick: () -> Unit,
    onGoodsClick: () -> Unit,
    onNavigateIp: () -> Unit,
    onNavigateCategory: () -> Unit,
    onNavigateTheme: () -> Unit
) {
    val shortcuts = listOf(
        AdminShortcut("用户管理", "账号、角色、启停", Icons.Outlined.Group, onUsersClick),
        AdminShortcut("全站谷子", "筛选、编辑、排序", Icons.Outlined.Inventory2, onGoodsClick),
        AdminShortcut("IP 与角色", "作品和 BGM 导入", Icons.Outlined.Bookmarks, onNavigateIp),
        AdminShortcut("品类管理", "层级、颜色、排序", Icons.Outlined.Category, onNavigateCategory),
        AdminShortcut("主题管理", "主题集合与图片", Icons.Outlined.Apps, onNavigateTheme)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            MobileHeaderCard(
                title = "后台总览",
                subtitle = "账号、全站谷子与公共元数据入口"
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AdminMetricCard(
                    label = "账号",
                    value = state.usersTotalCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    label = "谷子",
                    value = state.goodsTotalCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AdminMetricCard(
                    label = "角色",
                    value = state.roles.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    label = "停用",
                    value = state.users.count { !it.isActive }.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            MobileSectionHeader(
                title = "常用管理",
                subtitle = "进入高频后台维护页面",
                accent = PurpleSecondary
            )
        }
        items(shortcuts.chunked(2)) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { shortcut ->
                    AdminShortcutCard(
                        shortcut = shortcut,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private data class AdminShortcut(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun AdminShortcutCard(
    shortcut: AdminShortcut,
    modifier: Modifier = Modifier
) {
    PickGoodsCard(modifier = modifier.height(112.dp), radius = 16.dp, onClick = shortcut.onClick) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = PickGoodsShape.Control,
                color = PurpleSecondary.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(shortcut.icon, contentDescription = null, tint = PurpleSecondary, modifier = Modifier.size(22.dp))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = shortcut.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = shortcut.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AdminMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    PickGoodsCard(modifier = modifier, radius = 16.dp) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = value,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdminShortcutRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    PickGoodsCard(radius = 16.dp, onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = PickGoodsShape.Control,
                color = PurpleSecondary.copy(alpha = 0.12f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(icon, contentDescription = null, tint = PurpleSecondary)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AdminTabBar(
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
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = activeTab == index
                val containerColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    label = "adminTabContainer"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "adminTabContent"
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
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = contentColor,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminUsersTab(
    state: AdminUiState,
    onRefresh: () -> Unit,
    onLoadUserDetail: (AdminUser) -> Unit,
    onClearUserDetail: () -> Unit,
    onSaveUser: (AdminUser?, String, String, Int?, Boolean) -> Unit,
    onToggleActive: (AdminUser) -> Unit,
    onPageChanged: (Int) -> Unit
) {
    var showCreate by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<AdminUser?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            MobileHeaderCard(
                title = "用户管理",
                subtitle = "共 ${state.usersTotalCount} 个账号",
                trailing = {
                    CompactActionButton(
                        label = "新增",
                        icon = Icons.Outlined.Add,
                        onClick = { showCreate = true }
                    )
                }
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminMetricCard(
                        label = "当前页",
                        value = state.users.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        label = "启用",
                        value = state.users.count { it.isActive }.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
                AdminMetricCard(
                    label = "角色",
                    value = state.roles.size.toString(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            MobileSectionHeader(
                title = "账号列表",
                subtitle = "点击卡片可编辑账号资料",
                accent = PurpleSecondary
            )
        }
        state.usersError?.let { message ->
            item { ErrorMessage(message, onRefresh) }
        }
        when {
            state.isUsersLoading && state.users.isEmpty() -> item { LoadingBox() }
            state.users.isEmpty() -> item { EmptyMessage("暂无用户数据") }
            else -> items(state.users, key = { it.id }) { user ->
                AdminUserCard(
                    user = user,
                    isBusy = state.isSavingUser,
                    onEdit = {
                        editingUser = user
                        onLoadUserDetail(user)
                    },
                    onToggleActive = { onToggleActive(user) }
                )
            }
        }
        if (state.usersTotalPages > 1) {
            item {
                AdminPaginationBar(
                    page = state.usersPage,
                    totalPages = state.usersTotalPages,
                    totalCount = state.usersTotalCount,
                    onPageChanged = onPageChanged
                )
            }
        }
    }

    if (showCreate || editingUser != null) {
        val sheetUser = editingUser?.let { editing ->
            state.activeUserDetail?.takeIf { it.id == editing.id } ?: editing
        }
        AdminUserSheet(
            user = sheetUser,
            roles = state.roles,
            isBusy = state.isSavingUser || state.isRolesLoading,
            isDetailLoading = state.isUserDetailLoading,
            onDismiss = {
                showCreate = false
                editingUser = null
                onClearUserDetail()
            },
            onConfirm = { username, password, roleId, isActive ->
                onSaveUser(sheetUser, username, password, roleId, isActive)
                if (!state.isSavingUser) {
                    showCreate = false
                    editingUser = null
                    onClearUserDetail()
                }
            }
        )
    }
}

@Composable
private fun AdminUserCard(
    user: AdminUser,
    isBusy: Boolean,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit
) {
    SimpleListCard(
        title = user.username,
        subtitle = "ID ${user.id} · ${formatShortDate(user.createdAt)}",
        meta = roleLabel(user.role),
        onClick = onEdit,
        onEdit = onEdit,
        leading = {
            UserAvatar(user.username, user.isActive)
        },
        trailing = {
            TextButton(onClick = onToggleActive, enabled = !isBusy) {
                Text(if (user.isActive) "停用" else "启用")
            }
        }
    )
}

@Composable
private fun UserAvatar(username: String, isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    if (isActive) listOf(GoldSoft, PurpleSoft) else listOf(White, MaterialTheme.colorScheme.surfaceVariant)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = username.take(1).uppercase(),
            color = if (isActive) PurpleSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AdminUserSheet(
    user: AdminUser?,
    roles: List<AdminRole>,
    isBusy: Boolean,
    isDetailLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int?, Boolean) -> Unit
) {
    var username by remember(user?.id) { mutableStateOf(user?.username.orEmpty()) }
    var password by remember(user?.id) { mutableStateOf("") }
    var roleId by remember(user?.id, roles) {
        mutableStateOf(user?.role?.id ?: roles.firstOrNull { it.name == "User" }?.id ?: roles.firstOrNull()?.id)
    }
    var isActive by remember(user?.id) { mutableStateOf(user?.isActive ?: true) }
    val selectedRole = roles.firstOrNull { it.id == roleId }

    MobileFormSheet(
        title = if (user == null) "新增用户" else "编辑用户",
        subtitle = if (user == null) "创建账号并分配角色" else "调整角色、状态或重置密码",
        confirmText = if (user == null) "创建用户" else "保存更改",
        confirmEnabled = username.isNotBlank() && roleId != null && (user != null || password.length >= 6),
        isBusy = isBusy,
        onDismiss = onDismiss,
        onConfirm = { onConfirm(username.trim(), password, roleId, isActive) }
    ) {
        if (isDetailLoading) {
            Text(
                text = "正在同步用户详情...",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium
            )
        }
        user?.let {
            UserDetailSummary(user = it)
        }
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            enabled = user == null,
            label = { Text("用户名") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = PickGoodsShape.Control
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(if (user == null) "密码" else "新密码（留空不修改）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = PickGoodsShape.Control
        )
        ChoiceChipFlow(
            title = "角色",
            options = roles,
            selected = selectedRole,
            label = { roleLabel(it) },
            onSelected = { roleId = it?.id }
        )
        if (user != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("账号启用", modifier = Modifier.weight(1f))
                Switch(checked = isActive, onCheckedChange = { isActive = it })
            }
        }
    }
}

@Composable
private fun UserDetailSummary(user: AdminUser) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            MobileInfoTile(
                label = "用户ID",
                value = user.id.toString(),
                subtitle = formatShortDate(user.createdAt),
                accent = Gold,
                modifier = Modifier.weight(1f)
            )
            MobileInfoTile(
                label = "状态",
                value = if (user.isActive) "启用" else "停用",
                subtitle = user.role?.let { roleLabel(it) } ?: "未分配角色",
                accent = if (user.isActive) PurpleSecondary else MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = "最近更新：${formatShortDate(user.updatedAt)}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AdminGoodsTab(
    state: AdminUiState,
    onSearchChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onUserFilterChanged: (Int?) -> Unit,
    onStatusChanged: (String?) -> Unit,
    onIpFilterChanged: (Int?) -> Unit,
    onCharacterFilterChanged: (Int?) -> Unit,
    onCategoryFilterChanged: (Int?) -> Unit,
    onThemeFilterChanged: (Int?) -> Unit,
    onLocationFilterChanged: (Int?) -> Unit,
    onOfficialFilterChanged: (Boolean?) -> Unit,
    onResetFilters: () -> Unit,
    onPageChanged: (Int) -> Unit,
    onGoodsClick: (String) -> Unit,
    onCreateGoods: () -> Unit,
    onEditGoods: (String) -> Unit,
    onDeleteGoods: (String) -> Unit,
    onMoveGoods: (String, String, String) -> Unit
) {
    var deleteTarget by remember { mutableStateOf<GoodsListItem?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            MobileHeaderCard(
                title = "全站谷子",
                subtitle = "共 ${state.goodsTotalCount} 件，支持全维度筛选和排序",
                trailing = {
                    CompactActionButton(label = "新增", onClick = onCreateGoods)
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SearchField(
                        value = state.goodsSearch,
                        onValueChange = onSearchChanged,
                        placeholder = "搜索谷子名称...",
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onSearch) { Text("搜索") }
                }
            }
        }
        item {
            AdminGoodsFilterSummary(
                state = state,
                onOpenFilters = { showFilterSheet = true },
                onReset = onResetFilters
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminMetricCard(
                        label = "总数",
                        value = state.goodsTotalCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        label = "本页",
                        value = state.goods.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
                AdminMetricCard(
                    label = "页码",
                    value = "${state.goodsPage}/${state.goodsTotalPages.coerceAtLeast(1)}",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            MobileSectionHeader(
                title = "谷子列表",
                subtitle = "支持排序、编辑和删除",
                accent = Gold
            )
        }
        state.goodsError?.let { message ->
            item { ErrorMessage(message, onSearch) }
        }
        when {
            state.isGoodsLoading && state.goods.isEmpty() -> item { LoadingBox() }
            state.goods.isEmpty() -> item { EmptyMessage("暂无谷子数据") }
            else -> itemsIndexed(state.goods, key = { _, item -> item.id }) { index, goods ->
                val previous = state.goods.getOrNull(index - 1)
                val next = state.goods.getOrNull(index + 1)
                AdminGoodsRow(
                    goods = goods,
                    baseUrl = state.baseUrl,
                    canMutate = !state.isGoodsMutating,
                    onClick = { onGoodsClick(goods.id) },
                    onEdit = { onEditGoods(goods.id) },
                    onDelete = { deleteTarget = goods },
                    onMoveUp = previous?.let { anchor ->
                        { onMoveGoods(goods.id, anchor.id, "before") }
                    },
                    onMoveDown = next?.let { anchor ->
                        { onMoveGoods(goods.id, anchor.id, "after") }
                    }
                )
            }
        }
        if (state.goodsTotalPages > 1) {
            item {
                AdminPaginationBar(
                    page = state.goodsPage,
                    totalPages = state.goodsTotalPages,
                    totalCount = state.goodsTotalCount,
                    onPageChanged = onPageChanged
                )
            }
        }
    }

    deleteTarget?.let { goods ->
        DeleteConfirmDialog(
            title = "删除谷子",
            text = "确定删除「${goods.name}」吗？",
            onDismiss = { deleteTarget = null },
            onConfirm = {
                onDeleteGoods(goods.id)
                deleteTarget = null
            }
        )
    }

    if (showFilterSheet) {
        AdminGoodsFilterSheet(
            state = state,
            onDismiss = { showFilterSheet = false },
            onUserFilterChanged = onUserFilterChanged,
            onStatusChanged = onStatusChanged,
            onIpFilterChanged = onIpFilterChanged,
            onCharacterFilterChanged = onCharacterFilterChanged,
            onCategoryFilterChanged = onCategoryFilterChanged,
            onThemeFilterChanged = onThemeFilterChanged,
            onLocationFilterChanged = onLocationFilterChanged,
            onOfficialFilterChanged = onOfficialFilterChanged,
            onResetFilters = onResetFilters
        )
    }
}

@Composable
private fun AdminGoodsFilterSummary(
    state: AdminUiState,
    onOpenFilters: () -> Unit,
    onReset: () -> Unit
) {
    val summary = adminGoodsFilterSummary(state)
    PickGoodsCard(radius = 16.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = null,
                    tint = PurpleSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "筛选条件",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = onReset,
                    enabled = summary != "全部谷子",
                    modifier = Modifier.weight(1f)
                ) {
                    Text("重置")
                }
                CompactActionButton(
                    label = "筛选",
                    icon = Icons.Outlined.Tune,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenFilters
                )
            }
        }
    }
}

@Composable
private fun AdminGoodsFilterSheet(
    state: AdminUiState,
    onDismiss: () -> Unit,
    onUserFilterChanged: (Int?) -> Unit,
    onStatusChanged: (String?) -> Unit,
    onIpFilterChanged: (Int?) -> Unit,
    onCharacterFilterChanged: (Int?) -> Unit,
    onCategoryFilterChanged: (Int?) -> Unit,
    onThemeFilterChanged: (Int?) -> Unit,
    onLocationFilterChanged: (Int?) -> Unit,
    onOfficialFilterChanged: (Boolean?) -> Unit,
    onResetFilters: () -> Unit
) {
    val filteredCharacters = remember(state.characters, state.goodsIpId) {
        state.characters.filter { character ->
            state.goodsIpId == null || character.ip.id == state.goodsIpId || character.ipId == state.goodsIpId
        }
    }

    MobileFormSheet(
        title = "筛选全站谷子",
        subtitle = "按用户、作品、角色、品类、主题、位置与官非精确收束",
        confirmText = "完成",
        isBusy = state.isGoodsMetadataLoading,
        onDismiss = onDismiss,
        onConfirm = onDismiss
    ) {
        ChoiceChipFlow(
            title = "归属用户",
            options = state.users,
            selected = state.users.firstOrNull { it.id == state.goodsUserId },
            label = { it.username },
            onSelected = { onUserFilterChanged(it?.id) },
            emptyLabel = "全部用户",
            maxItems = 40
        )
        ChoiceChipFlow(
            title = "状态",
            options = adminStatusOptions,
            selected = adminStatusOptions.firstOrNull { it.value == state.goodsStatus },
            label = { it.label },
            onSelected = { onStatusChanged(it?.value) },
            emptyLabel = "全部状态"
        )
        ChoiceChipFlow(
            title = "官谷 / 同人",
            options = adminOfficialOptions,
            selected = adminOfficialOptions.firstOrNull { it.value == state.goodsOfficial },
            label = { it.label },
            onSelected = { onOfficialFilterChanged(it?.value) },
            emptyLabel = "全部"
        )
        ChoiceChipFlow(
            title = "IP 作品",
            options = state.ips,
            selected = state.ips.firstOrNull { it.id == state.goodsIpId },
            label = { it.name },
            onSelected = { onIpFilterChanged(it?.id) },
            emptyLabel = "全部 IP",
            maxItems = 60
        )
        ChoiceChipFlow(
            title = "角色",
            options = filteredCharacters,
            selected = filteredCharacters.firstOrNull { character ->
                character.id in state.goodsCharacterIds ||
                    (state.goodsCharacterIds.isEmpty() && character.id == state.goodsCharacterId)
            },
            label = { it.name },
            onSelected = { onCharacterFilterChanged(it?.id) },
            emptyLabel = "全部角色",
            maxItems = 70,
            selectedPredicate = { character ->
                character.id in state.goodsCharacterIds ||
                    (state.goodsCharacterIds.isEmpty() && character.id == state.goodsCharacterId)
            }
        )
        ChoiceChipFlow(
            title = "品类",
            options = state.categories,
            selected = state.categories.firstOrNull { it.id == state.goodsCategoryId },
            label = { it.pathName ?: it.name },
            onSelected = { onCategoryFilterChanged(it?.id) },
            emptyLabel = "全部品类",
            maxItems = 70
        )
        ChoiceChipFlow(
            title = "主题",
            options = state.themes,
            selected = state.themes.firstOrNull { it.id == state.goodsThemeId },
            label = { it.name },
            onSelected = { onThemeFilterChanged(it?.id) },
            emptyLabel = "全部主题",
            maxItems = 60
        )
        ChoiceChipFlow(
            title = "位置",
            options = state.locations,
            selected = state.locations.firstOrNull { it.id == state.goodsLocationId },
            label = { it.pathName ?: it.name },
            onSelected = { onLocationFilterChanged(it?.id) },
            emptyLabel = "全部位置",
            maxItems = 70
        )
        TextButton(onClick = onResetFilters, modifier = Modifier.fillMaxWidth()) {
            Text("清空全部筛选")
        }
    }
}

@Composable
private fun AdminGoodsRow(
    goods: GoodsListItem,
    baseUrl: String,
    canMutate: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    val image = resolveImageUrl(goods.mainPhoto, baseUrl)
    PickGoodsCard(radius = 16.dp, onClick = onClick) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(256.dp)
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
                    Icon(Icons.Outlined.Image, contentDescription = null, tint = Gold, modifier = Modifier.size(36.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.06f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.38f)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MiniMetaChip(
                        text = statusLabel(goods.status),
                        color = Gold
                    )
                    MiniMetaChip(
                        text = if (goods.isOfficial) "官谷" else "同人",
                        color = if (goods.isOfficial) Gold else PurpleSecondary
                    )
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = PickGoodsShape.Pill,
                    color = White.copy(alpha = 0.9f)
                ) {
                    AdminGoodsActionGrid(
                        canMutate = canMutate,
                        onEdit = onEdit,
                        onDelete = onDelete,
                        onMoveUp = onMoveUp,
                        onMoveDown = onMoveDown
                    )
                }
                if (goods.quantity > 1) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
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
            }

            Column(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                        val characters = goods.characters.take(2).joinToString("、") { it.name }
                        if (characters.isNotBlank()) {
                            append(" · ")
                            append(characters)
                        }
                        goods.user?.username?.let {
                            append(" · ")
                            append(it)
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    MiniMetaChip(
                        text = goods.category.name,
                        color = Gold,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    goods.locationPath?.split('/')?.lastOrNull()?.takeIf { it.isNotBlank() }?.let { location ->
                        MiniMetaChip(
                            text = location,
                            color = PurpleSecondary,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniMetaChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, shape = PickGoodsShape.Pill, color = color.copy(alpha = 0.12f)) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun AdminGoodsActionGrid(
    canMutate: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            AdminMiniIconButton(
                enabled = canMutate,
                onClick = onEdit,
                tint = PurpleSecondary
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(17.dp))
            }
            AdminMiniIconButton(
                enabled = canMutate,
                onClick = onDelete,
                tint = MaterialTheme.colorScheme.error
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(17.dp))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            AdminMiniIconButton(
                enabled = canMutate && onMoveUp != null,
                onClick = { onMoveUp?.invoke() },
                tint = PurpleSecondary
            ) {
                Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(17.dp))
            }
            AdminMiniIconButton(
                enabled = canMutate && onMoveDown != null,
                onClick = { onMoveDown?.invoke() },
                tint = PurpleSecondary
            ) {
                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun AdminMiniIconButton(
    enabled: Boolean,
    onClick: () -> Unit,
    tint: Color,
    icon: @Composable () -> Unit
) {
    Surface(
        shape = PickGoodsShape.Pill,
        color = if (enabled) tint.copy(alpha = 0.12f) else SurfaceGray.copy(alpha = 0.72f),
        contentColor = if (enabled) tint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon()
            }
        }
    }
}

@Composable
private fun GoodsThumb(goods: GoodsListItem, baseUrl: String) {
    val image = resolveImageUrl(goods.mainPhoto, baseUrl)
    Box(
        modifier = Modifier
            .size(92.dp)
            .clip(RoundedCornerShape(13.dp))
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
            Icon(Icons.Outlined.Image, contentDescription = null, tint = Gold)
        }
    }
}

@Composable
private fun AdminPaginationBar(
    page: Int,
    totalPages: Int,
    totalCount: Int,
    onPageChanged: (Int) -> Unit
) {
    PickGoodsCard(radius = 16.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                enabled = page > 1,
                onClick = { onPageChanged((page - 1).coerceAtLeast(1)) }
            ) {
                Text("上一页")
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$page / $totalPages",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "共 $totalCount 条",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(
                enabled = page < totalPages,
                onClick = { onPageChanged((page + 1).coerceAtMost(totalPages)) }
            ) {
                Text("下一页")
            }
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

private data class AdminStatusOption(val label: String, val value: String)

private data class AdminOfficialOption(val label: String, val value: Boolean)

private val adminStatusOptions = listOf(
    AdminStatusOption("在馆", "in_cabinet"),
    AdminStatusOption("在外", "outdoor"),
    AdminStatusOption("已出", "sold"),
    AdminStatusOption("草稿", "draft")
)

private val adminOfficialOptions = listOf(
    AdminOfficialOption("官谷", true),
    AdminOfficialOption("同人", false)
)

private fun adminGoodsFilterSummary(state: AdminUiState): String {
    val pieces = mutableListOf<String>()
    state.users.firstOrNull { it.id == state.goodsUserId }?.let { pieces += "用户：${it.username}" }
    state.goodsStatus?.let { status -> pieces += statusLabel(status) }
    state.goodsOfficial?.let { pieces += if (it) "官谷" else "同人" }
    state.ips.firstOrNull { it.id == state.goodsIpId }?.let { pieces += it.name }
    val selectedCharacters = state.characters.filter { it.id in state.goodsCharacterIds }
    when {
        selectedCharacters.size > 2 -> pieces += "${selectedCharacters.size} 个角色"
        selectedCharacters.isNotEmpty() -> pieces += selectedCharacters.joinToString(" / ") { it.name }
        state.goodsCharacterIds.isNotEmpty() -> pieces += "${state.goodsCharacterIds.size} 个角色"
        else -> state.characters.firstOrNull { it.id == state.goodsCharacterId }?.let { pieces += it.name }
    }
    state.categories.firstOrNull { it.id == state.goodsCategoryId }?.let {
        pieces += (it.pathName ?: it.name)
    }
    state.themes.firstOrNull { it.id == state.goodsThemeId }?.let { pieces += it.name }
    state.locations.firstOrNull { it.id == state.goodsLocationId }?.let {
        pieces += (it.pathName ?: it.name)
    }
    return pieces.takeIf { it.isNotEmpty() }?.joinToString(" / ") ?: "全部谷子"
}

private fun roleLabel(role: AdminRole?): String {
    return when (role?.name?.lowercase()) {
        "admin" -> "管理员"
        "user" -> "普通用户"
        null -> "未分配"
        else -> role.name
    }
}

private fun statusLabel(status: String): String {
    return adminStatusOptions.firstOrNull { it.value == status }?.label ?: status
}

private fun formatShortDate(value: String?): String {
    if (value.isNullOrBlank()) return "未记录"
    return value.take(10)
}
