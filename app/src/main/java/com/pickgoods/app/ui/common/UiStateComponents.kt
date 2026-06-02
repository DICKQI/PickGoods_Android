package com.pickgoods.app.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.GoldSoft
import com.pickgoods.app.ui.theme.PurpleSoft
import com.pickgoods.app.ui.theme.PurpleSecondary
import com.pickgoods.app.ui.theme.PurpleOnSecondary
import com.pickgoods.app.ui.theme.SurfaceGray
import com.pickgoods.app.ui.theme.White

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp),
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

@Composable
fun MobileHeaderCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    PickGoodsCard(
        modifier = modifier.fillMaxWidth(),
        radius = 16.dp
    ) {
        Column(
            modifier = Modifier
                .background(SoftPanelBrush)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (trailing != null) {
                    trailing()
                }
            }
            GoldAccentLine(modifier = Modifier.height(1.dp))
            content()
        }
    }
}

@Composable
fun MobileInfoTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    PickGoodsCard(
        modifier = modifier.heightIn(min = 76.dp),
        radius = 16.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(White, accent.copy(alpha = 0.08f), White)
                    )
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(shape = PickGoodsShape.Pill, color = accent.copy(alpha = 0.12f)) {
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
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
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
fun MobileSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accent: Color = Gold,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 26.dp)
                .clip(PickGoodsShape.Pill)
                .background(accent)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun CompactActionButton(
    label: String,
    icon: ImageVector = Icons.Outlined.Add,
    emphasized: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val container = if (emphasized) PurpleSecondary else SurfaceGray.copy(alpha = 0.82f)
    val contentColor = if (emphasized) PurpleOnSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = PickGoodsShape.Pill,
        color = container,
        shadowElevation = if (emphasized) 2.dp else 0.dp,
        modifier = modifier
            .height(38.dp)
            .widthIn(min = 68.dp)
            .clip(PickGoodsShape.Pill)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileFormSheet(
    title: String,
    subtitle: String? = null,
    confirmText: String = "保存",
    confirmEnabled: Boolean = true,
    isBusy: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (isBusy) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                }
            }

            GoldAccentLine(modifier = Modifier.height(1.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 680.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }
                Button(
                    onClick = onConfirm,
                    enabled = confirmEnabled && !isBusy,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = PickGoodsShape.Pill,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleSecondary,
                        contentColor = PurpleOnSecondary
                    )
                ) {
                    Text(confirmText)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChoiceChipFlow(
    title: String,
    options: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelected: (T?) -> Unit,
    modifier: Modifier = Modifier,
    emptyLabel: String? = null,
    maxItems: Int = 60,
    selectedPredicate: ((T) -> Boolean)? = null
) {
    PickGoodsCard(modifier = modifier.fillMaxWidth(), radius = 16.dp) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (emptyLabel != null) {
                    SmallChoiceChip(
                        label = emptyLabel,
                        selected = selected == null,
                        onClick = { onSelected(null) }
                    )
                }
                options.take(maxItems).forEach { option ->
                    SmallChoiceChip(
                        label = label(option),
                        selected = selectedPredicate?.invoke(option) ?: (option == selected),
                        onClick = { onSelected(option) }
                    )
                }
            }
        }
    }
}

@Composable
fun SmallChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier.height(34.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = SurfaceGray.copy(alpha = 0.72f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = PurpleSecondary.copy(alpha = 0.16f),
            selectedLabelColor = PurpleSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
            selectedBorderColor = PurpleSecondary.copy(alpha = 0.24f)
        ),
        label = {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@Composable
fun ErrorMessage(message: String, onRetry: (() -> Unit)? = null) {
    PickGoodsCard(modifier = Modifier.fillMaxWidth(), radius = 16.dp) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(22.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            if (onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text("重试")
                }
            }
        }
    }
}

@Composable
fun EmptyMessage(text: String, modifier: Modifier = Modifier) {
    PickGoodsCard(modifier = modifier.fillMaxWidth(), radius = 16.dp) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(22.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.74f)
                )
            }
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SimpleListCard(
    title: String,
    subtitle: String? = null,
    meta: String? = null,
    onClick: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    PickGoodsCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp),
        radius = 16.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (leading != null) {
                leading()
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (meta != null) {
                        Surface(
                            shape = PickGoodsShape.Pill,
                            color = GoldSoft,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = meta,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (onEdit != null) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = "编辑", modifier = Modifier.size(19.dp))
                }
            }
            if (trailing != null) {
                trailing()
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = PickGoodsShape.Pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = PurpleSecondary,
            contentColor = PurpleOnSecondary
        )
    ) {
        Icon(Icons.Outlined.Add, contentDescription = null)
        Text("新增", modifier = Modifier.padding(start = 6.dp))
    }
}

@Composable
fun DeleteConfirmDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("删除", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
