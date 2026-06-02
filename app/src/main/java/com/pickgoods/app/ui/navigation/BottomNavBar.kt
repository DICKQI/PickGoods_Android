package com.pickgoods.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import com.pickgoods.app.ui.theme.TextLight
import com.pickgoods.app.ui.theme.White

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem("showcase", "云展柜", Icons.Filled.GridView, Icons.Outlined.GridView),
        BottomNavItem("location", "位置", Icons.Filled.Folder, Icons.Outlined.Folder),
        BottomNavItem("ip_character", "IP角色", Icons.Filled.Bookmarks, Icons.Outlined.Bookmarks),
        BottomNavItem("category", "品类", Icons.Filled.Category, Icons.Outlined.Category),
        BottomNavItem("theme", "主题", Icons.Filled.Apps, Icons.Outlined.Apps)
    )

    Column(
        modifier = modifier
            .background(White)
            .navigationBarsPadding()
    ) {
        GoldAccentLine(
            modifier = Modifier
                .height(1.dp)
                .padding(horizontal = 26.dp)
        )
        NavigationBar(
            containerColor = White,
            tonalElevation = 8.dp,
            modifier = Modifier.height(68.dp)
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.10f else 1f,
                    animationSpec = tween(PickGoodsMotion.Fast),
                    label = "bottomNavIconScale"
                )
                NavigationBarItem(
                    selected = selected,
                    onClick = { onItemClick(item.route) },
                    icon = {
                        Icon(
                            if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            modifier = Modifier.graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                        )
                    },
                    label = {
                        NavLabel(text = item.label, selected = selected)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Gold,
                        selectedTextColor = Gold,
                        indicatorColor = GoldPrimaryContainer,
                        unselectedIconColor = TextLight,
                        unselectedTextColor = TextLight
                    )
                )
            }
        }
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
