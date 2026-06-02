package com.pickgoods.app.ui.goods

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.ui.common.DeleteConfirmDialog
import com.pickgoods.app.ui.common.EmptyMessage
import com.pickgoods.app.ui.common.ErrorMessage
import com.pickgoods.app.ui.common.MobileHeaderCard
import com.pickgoods.app.ui.common.MobileInfoTile
import com.pickgoods.app.ui.common.MobileSectionHeader
import com.pickgoods.app.ui.common.PickGoodsBackTopBar
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.goods.components.GoodsCard
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.PurpleSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoodsDraftsScreen(
    onBack: () -> Unit,
    onEditDraft: (String) -> Unit,
    viewModel: GoodsDraftsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<GoodsListItem?>(null) }

    Scaffold(
        topBar = {
            PickGoodsBackTopBar(
                title = "草稿箱",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { viewModel.refresh() }, enabled = !state.isLoading) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "刷新草稿")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PickGoodsScreen(modifier = Modifier.padding(paddingValues)) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(190.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    MobileHeaderCard(
                        title = "草稿箱",
                        subtitle = "状态为草稿的谷子会保存在这里，可继续编辑后发布"
                    )
                }
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        MobileInfoTile(
                            label = "草稿",
                            value = state.totalCount.toString(),
                            subtitle = "待完善",
                            accent = Gold,
                            modifier = Modifier.weight(1f)
                        )
                        MobileInfoTile(
                            label = "页码",
                            value = "${state.page}/${state.totalPages}",
                            subtitle = "每页 ${state.pageSize}",
                            accent = PurpleSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                state.error?.let { error ->
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        ErrorMessage(error, onRetry = { viewModel.refresh() })
                    }
                }

                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    MobileSectionHeader(
                        title = "继续编辑",
                        subtitle = "点击卡片或编辑按钮进入表单",
                        accent = PurpleSecondary
                    )
                }

                when {
                    state.isLoading && state.drafts.isEmpty() -> {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    state.drafts.isEmpty() -> {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                            EmptyMessage("暂无草稿")
                        }
                    }
                    else -> {
                        items(state.drafts, key = { it.id }) { draft ->
                            DraftCard(
                                draft = draft,
                                baseUrl = state.baseUrl,
                                canDelete = !state.isDeleting,
                                onEdit = { onEditDraft(draft.id) },
                                onDelete = { deleteTarget = draft }
                            )
                        }
                    }
                }

                if (state.totalPages > 1) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        DraftPaginationBar(
                            page = state.page,
                            totalPages = state.totalPages,
                            onPageChanged = viewModel::refresh
                        )
                    }
                }
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    deleteTarget?.let { draft ->
        DeleteConfirmDialog(
            title = "删除草稿",
            text = "确定删除「${draft.name}」吗？",
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.deleteDraft(draft.id)
                deleteTarget = null
            }
        )
    }
}

@Composable
private fun DraftCard(
    draft: GoodsListItem,
    baseUrl: String,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box {
        GoodsCard(
            goods = draft,
            baseUrl = baseUrl,
            onClick = onEdit
        )
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            shape = PickGoodsShape.Pill,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.width(36.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "编辑草稿",
                        tint = PurpleSecondary
                    )
                }
                IconButton(
                    onClick = onDelete,
                    enabled = canDelete,
                    modifier = Modifier.width(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "删除草稿",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
            shape = PickGoodsShape.Pill,
            color = Gold.copy(alpha = 0.16f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.NoteAlt,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.height(14.dp)
                )
                Text(
                    text = "草稿",
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DraftPaginationBar(
    page: Int,
    totalPages: Int,
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
                onClick = { onPageChanged((page - 1).coerceAtLeast(1)) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = null)
                Text("上一页", maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                    text = "草稿分页",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(
                enabled = page < totalPages,
                onClick = { onPageChanged((page + 1).coerceAtMost(totalPages)) },
                modifier = Modifier.weight(1f)
            ) {
                Text("下一页", maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}
