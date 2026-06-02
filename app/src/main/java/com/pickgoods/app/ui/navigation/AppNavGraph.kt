package com.pickgoods.app.ui.navigation

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pickgoods.app.ui.admin.AdminScreen
import com.pickgoods.app.ui.auth.AuthViewModel
import com.pickgoods.app.ui.auth.LoginScreen
import com.pickgoods.app.ui.goods.GoodsDetailScreen
import com.pickgoods.app.ui.goods.GoodsDraftsScreen
import com.pickgoods.app.ui.goods.GoodsFormScreen
import com.pickgoods.app.ui.location.LocationScreen
import com.pickgoods.app.ui.metadata.CategoryScreen
import com.pickgoods.app.ui.metadata.IPCharacterScreen
import com.pickgoods.app.ui.metadata.ThemeScreen
import com.pickgoods.app.ui.settings.SettingsScreen
import com.pickgoods.app.ui.showcase.CloudShowcaseScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.GoldSoft

object Routes {
    const val LOGIN = "login"
    const val SHOWCASE = "showcase"
    const val LOCATION = "location"
    const val IP_CHARACTER = "ip_character"
    const val CATEGORY = "category"
    const val THEME = "theme"
    const val SETTINGS = "settings"
    const val ADMIN = "admin"
    const val GOODS_NEW = "goods_new"
    const val GOODS_DRAFTS = "goods_drafts"
    const val GOODS_DETAIL = "goods_detail/{goodsId}"
    const val GOODS_EDIT = "goods_edit/{goodsId}"

    fun goodsDetail(goodsId: String) = "goods_detail/$goodsId"
    fun goodsEdit(goodsId: String) = "goods_edit/$goodsId"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    if (!authState.isInitialized) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    run {
        val snackbarHostState = remember { SnackbarHostState() }

        val startDestination = if (authState.isLoggedIn) Routes.SHOWCASE else Routes.LOGIN

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val isNetworkAvailable = rememberNetworkAvailable()

        val showBottomBar = currentRoute in listOf(
            Routes.SHOWCASE, Routes.LOCATION, Routes.IP_CHARACTER, Routes.CATEGORY, Routes.THEME
        )
        val showOfflineBanner = authState.isLoggedIn &&
            currentRoute != Routes.LOGIN &&
            !isNetworkAvailable

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    BottomNavBar(
                        currentRoute = currentRoute,
                        onItemClick = { route ->
                            navController.navigate(route) {
                                popUpTo(Routes.SHOWCASE) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Routes.LOGIN) {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(Routes.SHOWCASE) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Routes.SHOWCASE) {
                        CloudShowcaseScreen(
                            onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                            onGoodsClick = { goodsId -> navController.navigate(Routes.goodsDetail(goodsId)) },
                            onCreateGoods = { navController.navigate(Routes.GOODS_NEW) }
                        )
                    }
                    composable(Routes.LOCATION) {
                        LocationScreen(
                            onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                            onGoodsClick = { goodsId -> navController.navigate(Routes.goodsDetail(goodsId)) }
                        )
                    }
                    composable(Routes.IP_CHARACTER) {
                        IPCharacterScreen(
                            onSettingsClick = { navController.navigate(Routes.SETTINGS) }
                        )
                    }
                    composable(Routes.CATEGORY) {
                        CategoryScreen(
                            onSettingsClick = { navController.navigate(Routes.SETTINGS) }
                        )
                    }
                    composable(Routes.THEME) {
                        ThemeScreen(
                            onSettingsClick = { navController.navigate(Routes.SETTINGS) }
                        )
                    }
                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onLogout = {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateToAdmin = {
                                navController.navigate(Routes.ADMIN)
                            }
                        )
                    }
                    composable(Routes.ADMIN) {
                        AdminScreen(
                            onBack = { navController.popBackStack() },
                            onGoodsClick = { goodsId -> navController.navigate(Routes.goodsDetail(goodsId)) },
                            onCreateGoods = { navController.navigate(Routes.GOODS_NEW) },
                            onEditGoods = { goodsId -> navController.navigate(Routes.goodsEdit(goodsId)) },
                            onNavigateIp = { navController.navigate(Routes.IP_CHARACTER) },
                            onNavigateCategory = { navController.navigate(Routes.CATEGORY) },
                            onNavigateTheme = { navController.navigate(Routes.THEME) }
                        )
                    }
                    composable(Routes.GOODS_NEW) {
                        GoodsFormScreen(
                            goodsId = null,
                            onBack = { navController.popBackStack() },
                            onDraftsClick = { navController.navigate(Routes.GOODS_DRAFTS) },
                            onSaved = { goodsId ->
                                navController.navigate(Routes.goodsDetail(goodsId)) {
                                    popUpTo(Routes.GOODS_NEW) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Routes.GOODS_DRAFTS) {
                        GoodsDraftsScreen(
                            onBack = { navController.popBackStack() },
                            onEditDraft = { goodsId -> navController.navigate(Routes.goodsEdit(goodsId)) }
                        )
                    }
                    composable(Routes.GOODS_DETAIL) { backStackEntry ->
                        val goodsId = backStackEntry.arguments?.getString("goodsId") ?: return@composable
                        GoodsDetailScreen(
                            goodsId = goodsId,
                            onBack = { navController.popBackStack() },
                            onEdit = { id -> navController.navigate(Routes.goodsEdit(id)) },
                            onGoodsClick = { id -> navController.navigate(Routes.goodsDetail(id)) }
                        )
                    }
                    composable(Routes.GOODS_EDIT) { backStackEntry ->
                        val goodsId = backStackEntry.arguments?.getString("goodsId") ?: return@composable
                        GoodsFormScreen(
                            goodsId = goodsId,
                            onBack = { navController.popBackStack() },
                            onDraftsClick = { navController.navigate(Routes.GOODS_DRAFTS) },
                            onSaved = { savedId ->
                                navController.navigate(Routes.goodsDetail(savedId)) {
                                    popUpTo(Routes.GOODS_EDIT) { inclusive = true }
                                }
                            }
                        )
                    }
                }
                AnimatedVisibility(
                    visible = showOfflineBanner,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    OfflineCacheBanner()
                }
            }
        }
    }
}

@Composable
private fun OfflineCacheBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PickGoodsShape.Pill,
        color = GoldSoft.copy(alpha = 0.96f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Gold
            )
            Text(
                text = "离线模式，正在优先显示最近缓存",
                color = Gold,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun rememberNetworkAvailable(): Boolean {
    val context = LocalContext.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    var isAvailable by remember {
        mutableStateOf(context.isNetworkCurrentlyAvailable())
    }
    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        fun updateAvailability(value: Boolean) {
            mainHandler.post {
                isAvailable = value
            }
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                updateAvailability(true)
            }

            override fun onLost(network: Network) {
                updateAvailability(context.isNetworkCurrentlyAvailable())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                updateAvailability(
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                )
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
    return isAvailable
}

private fun Context.isNetworkCurrentlyAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
