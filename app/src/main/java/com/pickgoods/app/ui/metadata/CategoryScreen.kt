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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pickgoods.app.data.model.Category
import com.pickgoods.app.ui.common.AddButton
import com.pickgoods.app.ui.common.DeleteConfirmDialog
import com.pickgoods.app.ui.common.EmptyMessage
import com.pickgoods.app.ui.common.ErrorMessage
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsTopBar
import com.pickgoods.app.ui.common.SearchField
import com.pickgoods.app.ui.common.SimpleListCard

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
                onRefresh = viewModel::refresh
            )
        }
    }
}

@Composable
private fun CategoryContent(
    state: MetadataUiState,
    onSearch: (String) -> Unit,
    onSave: (Category?, String, Int?, String?, Int?) -> Unit,
    onDelete: (Int) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editing by remember { mutableStateOf<Category?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Category?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("品类管理", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("配置谷子的种类，如吧唧、立牌、徽章", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                AddButton(onClick = { showCreate = true })
            }
        }
        item {
            PickGoodsCard(radius = 18.dp) {
                SearchField(
                    value = state.searchQuery,
                    onValueChange = onSearch,
                    placeholder = "搜索品类...",
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        when {
            state.isLoading && state.categories.isEmpty() -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null && state.categories.isEmpty() -> item { ErrorMessage(state.error, onRefresh) }
            state.categories.isEmpty() -> item { EmptyMessage("暂无品类") }
            else -> items(state.categories.sortedWith(compareBy<Category> { categoryDepth(it) }.thenBy { it.order }), key = { it.id }) { category ->
                SimpleListCard(
                    title = category.name,
                    subtitle = category.pathName ?: "ID ${category.id}",
                    onEdit = { editing = category },
                    onDelete = { deleteTarget = category },
                    leading = {
                        Spacer(modifier = Modifier.width((categoryDepth(category) * 12).dp))
                        ColorDot(category.colorTag)
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
private fun ColorDot(colorTag: String?) {
    val color = parseHexColor(colorTag) ?: MaterialTheme.colorScheme.secondary
    Box(
        modifier = Modifier
            .size(16.dp)
            .background(color, CircleShape)
    )
}

@Composable
private fun CategoryEditDialog(
    category: Category?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int?, String?, Int?) -> Unit
) {
    var name by remember(category?.id) { mutableStateOf(category?.name.orEmpty()) }
    var parentText by remember(category?.id) { mutableStateOf(category?.parent?.toString().orEmpty()) }
    var color by remember(category?.id) { mutableStateOf(category?.colorTag ?: "#D4AF37") }
    var orderText by remember(category?.id) { mutableStateOf(category?.order?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "新增品类" else "编辑品类") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("品类名称") }, singleLine = true)
                OutlinedTextField(
                    value = parentText,
                    onValueChange = { parentText = it.filter(Char::isDigit) },
                    label = { Text("父级品类 ID（可空）") },
                    supportingText = {
                        val candidates = categories.take(4).joinToString(" / ") { "${it.id}:${it.name}" }
                        if (candidates.isNotBlank()) Text("可选：$candidates")
                    },
                    singleLine = true
                )
                OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("颜色标签") }, singleLine = true)
                OutlinedTextField(value = orderText, onValueChange = { orderText = it.filter(Char::isDigit) }, label = { Text("排序") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim(), parentText.toIntOrNull(), color.trim(), orderText.toIntOrNull()) }
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

private fun categoryDepth(category: Category): Int = category.pathName?.count { it == '/' } ?: 0

private fun parseHexColor(value: String?): Color? {
    if (value.isNullOrBlank()) return null
    return runCatching { Color(android.graphics.Color.parseColor(value)) }.getOrNull()
}
