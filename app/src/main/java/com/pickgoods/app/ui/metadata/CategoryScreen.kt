package com.pickgoods.app.ui.metadata

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pickgoods.app.data.model.Category
import com.pickgoods.app.ui.common.CompactActionButton
import com.pickgoods.app.ui.common.DeleteConfirmDialog
import com.pickgoods.app.ui.common.EmptyMessage
import com.pickgoods.app.ui.common.ErrorMessage
import com.pickgoods.app.ui.common.ChoiceChipFlow
import com.pickgoods.app.ui.common.MobileHeaderCard
import com.pickgoods.app.ui.common.MobileInfoTile
import com.pickgoods.app.ui.common.MobileSectionHeader
import com.pickgoods.app.ui.common.MobileFormSheet
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.common.PickGoodsTopBar
import com.pickgoods.app.ui.common.SearchField
import com.pickgoods.app.ui.common.SimpleListCard
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.PurpleSecondary

@Composable
fun CategoryScreen(
    onSettingsClick: () -> Unit,
    viewModel: MetadataViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            PickGoodsTopBar(
                title = "品类管理",
                onSettingsClick = onSettingsClick,
                onRefreshClick = viewModel::refresh
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PickGoodsScreen(modifier = Modifier.padding(paddingValues)) {
            CategoryContent(
                state = state,
                onSearch = viewModel::onSearchChanged,
                onSave = viewModel::saveCategory,
                onDelete = viewModel::deleteCategory,
                onMove = viewModel::moveCategory,
                onRefresh = viewModel::refresh
            )
        }
    }
}

@Composable
fun CategoryContent(
    state: MetadataUiState,
    onSearch: (String) -> Unit,
    onSave: (Category?, String, Int?, String?, Int?) -> Unit,
    onDelete: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editing by remember { mutableStateOf<Category?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Category?>(null) }
    val sortedCategories = remember(state.categories) {
        flattenCategoryTree(state.categories)
    }
    val rootCount = remember(state.categories) { state.categories.count { it.parent == null } }
    val childCount = (state.categories.size - rootCount).coerceAtLeast(0)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            MobileHeaderCard(
                title = "品类管理",
                subtitle = "${state.categories.size} 个品类",
                trailing = {
                    CompactActionButton(label = "新增", onClick = { showCreate = true })
                }
            ) {
                SearchField(
                    value = state.searchQuery,
                    onValueChange = onSearch,
                    placeholder = "搜索品类...",
                    modifier = Modifier.height(48.dp)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MobileInfoTile(
                    label = "顶级",
                    value = rootCount.toString(),
                    subtitle = "主品类",
                    accent = Gold,
                    modifier = Modifier.weight(1f)
                )
                MobileInfoTile(
                    label = "子级",
                    value = childCount.toString(),
                    subtitle = "层级标签",
                    accent = PurpleSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (sortedCategories.isNotEmpty()) {
            item {
                CategoryQuickRail(categories = sortedCategories.take(18))
            }
        }
        item {
            MobileSectionHeader(
                title = "品类树",
                subtitle = "颜色与层级会影响筛选和统计展示",
                accent = PurpleSecondary
            )
        }
        when {
            state.isLoading && state.categories.isEmpty() -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null && state.categories.isEmpty() -> item { ErrorMessage(state.error, onRefresh) }
            state.categories.isEmpty() -> item { EmptyMessage("暂无品类") }
            else -> itemsIndexed(sortedCategories, key = { _, item -> item.id }) { _, category ->
                val siblings = state.categories
                    .filter { it.parent == category.parent }
                    .sortedWith(compareBy<Category> { it.order }.thenBy { it.id })
                val siblingIndex = siblings.indexOfFirst { it.id == category.id }
                SimpleListCard(
                    title = category.name,
                    subtitle = category.pathName ?: "ID ${category.id}",
                    onEdit = { editing = category },
                    onDelete = { deleteTarget = category },
                    leading = {
                        Spacer(modifier = Modifier.width((categoryDepth(category) * 12).dp))
                        ColorDot(category.colorTag)
                    },
                    trailing = {
                        CategorySortButtons(
                            canMoveUp = siblingIndex > 0 && !state.isSaving,
                            canMoveDown = siblingIndex >= 0 && siblingIndex < siblings.lastIndex && !state.isSaving,
                            onMoveUp = { onMove(category.id, -1) },
                            onMoveDown = { onMove(category.id, 1) }
                        )
                    }
                )
            }
        }
    }

    if (showCreate || editing != null) {
        CategoryEditDialog(
            category = editing,
            categories = state.categories,
            onDismiss = {
                showCreate = false
                editing = null
            },
            onConfirm = { name, parent, color, order ->
                onSave(editing, name, parent, color, order)
                showCreate = false
                editing = null
            }
        )
    }
    deleteTarget?.let { category ->
        DeleteConfirmDialog(
            title = "删除品类",
            text = "确定删除「${category.name}」吗？",
            onDismiss = { deleteTarget = null },
            onConfirm = {
                onDelete(category.id)
                deleteTarget = null
            }
        )
    }
}

@Composable
private fun CategoryQuickRail(categories: List<Category>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MobileSectionHeader(
            title = "颜色索引",
            subtitle = "先看主色与层级，再进入列表编辑",
            accent = Gold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(categories, key = { it.id }) { category ->
                CategoryQuickCard(category = category)
            }
        }
    }
}

@Composable
private fun CategoryQuickCard(category: Category) {
    val color = parseHexColor(category.colorTag) ?: Gold
    PickGoodsCard(
        modifier = Modifier.width(188.dp),
        radius = 16.dp,
        borderColor = color.copy(alpha = 0.34f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.18f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .align(Alignment.BottomCenter)
                        .background(color)
                )
            }
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = category.pathName ?: "顶级品类",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun flattenCategoryTree(categories: List<Category>): List<Category> {
    if (categories.isEmpty()) return emptyList()
    val byParent = categories.groupBy { it.parent }
    val result = mutableListOf<Category>()
    val seen = mutableSetOf<Int>()

    fun append(parentId: Int?) {
        byParent[parentId]
            .orEmpty()
            .sortedWith(compareBy<Category> { it.order }.thenBy { it.id })
            .forEach { category ->
                if (seen.add(category.id)) {
                    result += category
                    append(category.id)
                }
            }
    }

    append(null)
    categories
        .sortedWith(compareBy<Category> { categoryDepth(it) }.thenBy { it.order }.thenBy { it.id })
        .forEach { category ->
            if (seen.add(category.id)) {
                result += category
            }
        }
    return result
}

@Composable
private fun ColorDot(colorTag: String?) {
    val color = parseHexColor(colorTag) ?: MaterialTheme.colorScheme.secondary
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(color, CircleShape)
        )
    }
}

@Composable
private fun CategorySortButtons(
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
private fun CategoryEditDialog(
    category: Category?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int?, String?, Int?) -> Unit
) {
    var name by remember(category?.id) { mutableStateOf(category?.name.orEmpty()) }
    var parentId by remember(category?.id) { mutableStateOf(category?.parent) }
    var color by remember(category?.id) { mutableStateOf(category?.colorTag ?: "#D4AF37") }
    var orderText by remember(category?.id) { mutableStateOf(category?.order?.toString() ?: "0") }
    val parentOptions = remember(categories, category?.id) {
        categories
            .filter { it.id != category?.id }
            .sortedWith(compareBy<Category> { categoryDepth(it) }.thenBy { it.order }.thenBy { it.name })
    }
    val selectedParent = parentOptions.firstOrNull { it.id == parentId }

    MobileFormSheet(
        title = if (category == null) "新增品类" else "编辑品类",
        subtitle = "用层级和颜色快速整理谷子类型",
        confirmEnabled = name.isNotBlank(),
        onDismiss = onDismiss,
        onConfirm = { onConfirm(name.trim(), parentId, color.trim(), orderText.toIntOrNull()) }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("品类名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = PickGoodsShape.Control
        )
        ChoiceChipFlow(
            title = "父级品类",
            options = parentOptions,
            selected = selectedParent,
            label = { it.pathName ?: it.name },
            onSelected = { parentId = it?.id },
            emptyLabel = "顶级品类"
        )
        ChoiceChipFlow(
            title = "常用颜色",
            options = categoryColorOptions,
            selected = color,
            label = { it },
            onSelected = { selected -> selected?.let { color = it } }
        )
        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("颜色标签") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = PickGoodsShape.Control
        )
        OutlinedTextField(
            value = orderText,
            onValueChange = { orderText = it.filter(Char::isDigit) },
            label = { Text("排序") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = PickGoodsShape.Control
        )
    }
}

private fun categoryDepth(category: Category): Int = category.pathName?.count { it == '/' } ?: 0

private fun parseHexColor(value: String?): Color? {
    if (value.isNullOrBlank()) return null
    return runCatching { Color(android.graphics.Color.parseColor(value)) }.getOrNull()
}

private val categoryColorOptions = listOf(
    "#D4AF37",
    "#8B5CF6",
    "#EC4899",
    "#10B981",
    "#3B82F6",
    "#F97316"
)
