package com.pickgoods.app.ui.goods.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pickgoods.app.data.model.GoodsListItem
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.theme.BorderGold
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.GoldSoft
import com.pickgoods.app.ui.theme.PurpleSecondary
import com.pickgoods.app.ui.theme.PurpleSoft
import com.pickgoods.app.ui.theme.TextLight
import com.pickgoods.app.ui.theme.TextLighter

@Composable
fun GoodsCard(
    goods: GoodsListItem,
    baseUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectable: Boolean = false,
    selected: Boolean = false,
    onSelect: (() -> Unit)? = null
) {
    val imageUrl = resolveImageUrl(goods.mainPhoto, baseUrl)

    PickGoodsCard(
        modifier = modifier,
        radius = 16.dp,
        borderColor = if (selected) Gold.copy(alpha = 0.82f) else BorderGold.copy(alpha = 0.35f),
        pressedScale = if (selectable) 0.975f else 0.985f,
        onClick = {
            if (selectable) {
                onSelect?.invoke()
            } else {
                onClick()
            }
        }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.82f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Brush.linearGradient(listOf(GoldSoft, PurpleSoft)))
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = goods.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = TextLighter,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(38.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.16f))
                            )
                        )
                )

                AttrBadge(
                    text = if (goods.isOfficial) "官谷" else "同人",
                    color = if (goods.isOfficial) Gold else PurpleSecondary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                )

                if (goods.quantity > 1) {
                    QuantityBadge(
                        text = "x${goods.quantity}",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    )
                }

                StatusBadge(
                    text = statusLabel(goods.status),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                )

                if (selected) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(BorderStroke(2.dp, Gold.copy(alpha = 0.78f)), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .background(Gold.copy(alpha = 0.08f))
                    )
                }

                if (selectable) {
                    SelectionBadge(
                        selected = selected,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = goods.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                MetaRow(label = "IP", value = goods.ip.name)
                MetaRow(
                    label = "角色",
                    value = goods.characters
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString("、") { it.name }
                        ?: "-"
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    CategoryTag(
                        text = goods.category.name,
                        color = goods.category.colorTag?.let { parseHexColor(it) } ?: Gold,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    goods.locationPath?.takeIf { it.isNotBlank() }?.let { path ->
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = TextLight,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = path.split('/').lastOrNull().orEmpty(),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextLight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionBadge(
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = PickGoodsShape.Pill,
        color = if (selected) Gold else Color.Black.copy(alpha = 0.36f),
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.9f)),
        shadowElevation = 3.dp
    ) {
        Box(
            modifier = Modifier.size(30.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "已选择",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(5.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f)
        ) {
            Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextLight,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .width(34.dp)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AttrBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.56f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.38f)),
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = Color.White.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.42f)),
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun QuantityBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = Color.Black.copy(alpha = 0.62f),
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun CategoryTag(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.22f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

private fun statusLabel(status: String): String {
    return when (status) {
        "in_cabinet" -> "在馆"
        "outdoor" -> "在外"
        "sold" -> "已出"
        "draft" -> "草稿"
        else -> status
    }
}

private fun parseHexColor(value: String): Color? {
    return runCatching { Color(android.graphics.Color.parseColor(value)) }.getOrNull()
}

fun resolveImageUrl(path: String?, baseUrl: String): Any? {
    if (path.isNullOrBlank()) return null
    if (path.startsWith("http://") || path.startsWith("https://")) return path
    return baseUrl.trimEnd('/') + "/" + path.trimStart('/')
}
