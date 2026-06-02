package com.pickgoods.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pickgoods.app.ui.common.GoldAccentLine
import com.pickgoods.app.ui.common.PickGoodsMotion
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.GoldPrimaryContainer
import com.pickgoods.app.ui.theme.PurpleOnSecondary
import com.pickgoods.app.ui.theme.PurpleSecondary
import com.pickgoods.app.ui.theme.TextLight
import com.pickgoods.app.ui.theme.White

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val BottomNavBarAccentHeight = 1.dp
val BottomNavBarRouteHeight = 64.dp
val BottomNavBarContentHeight = BottomNavBarAccentHeight + BottomNavBarRouteHeight

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    onCreateGoodsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem("showcase", "云展柜", Icons.Filled.GridView, Icons.Outlined.GridView),
        BottomNavItem("location", "位置", Icons.Filled.Folder, Icons.Outlined.Folder),
        BottomNavItem("ip_character", "资料", Icons.Filled.Bookmarks, Icons.Outlined.Bookmarks),
        BottomNavItem("theme", "主题", Icons.Filled.Apps, Icons.Outlined.Apps)
    )
    val leftItems = items.take(2)
    val rightItems = items.drop(2)

    Column(
        modifier = modifier.background(White)
    ) {
        GoldAccentLine(
            modifier = Modifier
                .height(BottomNavBarAccentHeight)
                .padding(horizontal = 26.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BottomNavBarRouteHeight)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leftItems.forEach { item ->
                BottomNavRouteButton(
                    item = item,
                    selected = currentRoute == item.route,
                    onClick = { onItemClick(item.route) },
                    modifier = Modifier.weight(1f)
                )
            }
            CreateGoodsButton(
                onClick = onCreateGoodsClick,
                modifier = Modifier.weight(1f)
            )
            rightItems.forEach { item ->
                BottomNavRouteButton(
                    item = item,
                    selected = currentRoute == item.route,
                    onClick = { onItemClick(item.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .background(White)
        )
    }
}

@Composable
private fun BottomNavRouteButton(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.10f else 1f,
        animationSpec = tween(PickGoodsMotion.Fast),
        label = "bottomNavIconScale"
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Surface(
            shape = PickGoodsShape.Pill,
            color = if (selected) GoldPrimaryContainer else White
        ) {
            Box(
                modifier = Modifier
                    .size(width = 46.dp, height = 30.dp)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                    tint = if (selected) Gold else TextLight,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )
            }
        }
        NavLabel(text = item.label, selected = selected)
    }
}

@Composable
private fun CreateGoodsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Surface(
            shape = PickGoodsShape.Pill,
            color = PurpleSecondary,
            shadowElevation = 6.dp,
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "新建谷子",
                    tint = PurpleOnSecondary,
                    modifier = Modifier.size(23.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "新建",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            color = PurpleSecondary,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun NavLabel(text: String, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Surface(
            shape = PickGoodsShape.Pill,
            color = if (selected) Gold else White,
            modifier = Modifier.size(width = if (selected) 18.dp else 4.dp, height = 3.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth())
        }
    }
}
