package com.pickgoods.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickGoodsTopBar(
    title: String,
    onSettingsClick: (() -> Unit)? = null,
    onRefreshClick: (() -> Unit)? = null,
    compact: Boolean = false
) {
    val barHeight by animateDpAsState(
        targetValue = if (compact) 42.dp else 50.dp,
        animationSpec = tween(PickGoodsMotion.Fast, easing = FastOutSlowInEasing),
        label = "topBarHeight"
    )
    val horizontalPadding by animateDpAsState(
        targetValue = if (compact) 12.dp else 16.dp,
        animationSpec = tween(PickGoodsMotion.Fast, easing = FastOutSlowInEasing),
        label = "topBarHorizontalPadding"
    )
    val iconButtonSize by animateDpAsState(
        targetValue = if (compact) 34.dp else 38.dp,
        animationSpec = tween(PickGoodsMotion.Fast, easing = FastOutSlowInEasing),
        label = "topBarIconSize"
    )
    val accentPadding by animateDpAsState(
        targetValue = if (compact) 14.dp else 18.dp,
        animationSpec = tween(PickGoodsMotion.Fast, easing = FastOutSlowInEasing),
        label = "topBarAccentPadding"
    )
    val cleanTitle = title.removePrefix("✦ ").removePrefix("✦").trim()

    Column(
        modifier = Modifier
            .background(SoftPanelBrush)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (title.startsWith("✦")) {
                Text(
                    text = "✦",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 14.sp else 16.sp,
                    maxLines = 1
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = cleanTitle,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 16.sp else 18.sp,
                letterSpacing = 0.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onRefreshClick != null) {
                    TopBarActionButton(
                        icon = Icons.Outlined.Refresh,
                        contentDescription = "刷新",
                        size = iconButtonSize,
                        onClick = onRefreshClick
                    )
                }
                if (onSettingsClick != null) {
                    TopBarActionButton(
                        icon = Icons.Outlined.Settings,
                        contentDescription = "设置",
                        size = iconButtonSize,
                        onClick = onSettingsClick
                    )
                }
            }
        }
        GoldAccentLine(
            modifier = Modifier
                .height(1.dp)
                .padding(horizontal = accentPadding)
        )
    }
}

@Composable
private fun TopBarActionButton(
    icon: ImageVector,
    contentDescription: String,
    size: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(PickGoodsShape.Pill)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickGoodsBackTopBar(
    title: String,
    onBackClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(modifier = Modifier.background(SoftPanelBrush)) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
        GoldAccentLine(
            modifier = Modifier
                .height(1.dp)
                .padding(horizontal = 18.dp)
        )
    }
}
