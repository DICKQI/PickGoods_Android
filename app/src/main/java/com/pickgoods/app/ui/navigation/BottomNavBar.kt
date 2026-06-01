package com.pickgoods.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pickgoods.app.ui.common.GoldAccentLine
import com.pickgoods.app.ui.common.PickGoodsMotion
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
        BottomNavItem("location", "位置管理", Icons.Filled.Folder, Icons.Outlined.Folder),
        BottomNavItem("ip_character", "IP与角色", Icons.Filled.Bookmarks, Icons.Outlined.Bookmarks),
        BottomNavItem("category", "品类管理", Icons.Filled.Category, Icons.Outlined.Category),
        BottomNavItem("theme", "主题管理", Icons.Filled.Apps, Icons.Outlined.Apps)
    )

    Column(
        modifier = modifier.background(White)
    ) {
        GoldAccentLine(
            modifier = Modifier
                .height(1.dp)
                .padding(horizontal = 26.dp)
        )
        NavigationBar(
            containerColor = White,
            tonalElevation = 8.dp
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.14f else 1f,
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
                    label = { Text(item.label) },
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
