package com.pickgoods.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.LocalImageLoader
import com.pickgoods.app.di.ImageLoaderEntryPoint
import com.pickgoods.app.ui.auth.AuthViewModel
import com.pickgoods.app.ui.auth.LoginScreen
import com.pickgoods.app.ui.goods.GoodsDetailScreen
import com.pickgoods.app.ui.goods.GoodsFormScreen
import com.pickgoods.app.ui.location.LocationScreen
import com.pickgoods.app.ui.metadata.CategoryScreen
import com.pickgoods.app.ui.metadata.IPCharacterScreen
import com.pickgoods.app.ui.metadata.ThemeScreen
import com.pickgoods.app.ui.settings.SettingsScreen
import com.pickgoods.app.ui.showcase.CloudShowcaseScreen
import dagger.hilt.android.EntryPointAccessors

object Routes {
    const val LOGIN = "login"
    const val SHOWCASE = "showcase"
    const val LOCATION = "location"
    const val IP_CHARACTER = "ip_character"
    const val CATEGORY = "category"
    const val THEME = "theme"
    const val SETTINGS = "settings"
    const val GOODS_NEW = "goods_new"
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

    val context = LocalContext.current
    val imageLoader = remember {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ImageLoaderEntryPoint::class.java
        )
        entryPoint.imageLoader()
    }

    CompositionLocalProvider(LocalImageLoader provides imageLoader) {
        val snackbarHostState = remember { SnackbarHostState() }

        val startDestination = if (authState.isLoggedIn) Routes.SHOWCASE else Routes.LOGIN

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val showBottomBar = currentRoute in listOf(
            Routes.SHOWCASE, Routes.LOCATION, Routes.IP_CHARACTER, Routes.CATEGORY, Routes.THEME
        )

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
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
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
                        onSettingsClick = { navController.navigate(Routes.SETTINGS) }
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
                        onLogout = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToAdmin = {
                            navController.navigate(Routes.IP_CHARACTER)
                        }
                    )
                }
                composable(Routes.GOODS_NEW) {
                    GoodsFormScreen(
                        goodsId = null,
                        onBack = { navController.popBackStack() },
                        onSaved = { goodsId ->
                            navController.navigate(Routes.goodsDetail(goodsId)) {
                                popUpTo(Routes.GOODS_NEW) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.GOODS_DETAIL) { backStackEntry ->
                    val goodsId = backStackEntry.arguments?.getString("goodsId") ?: return@composable
                    GoodsDetailScreen(
                        goodsId = goodsId,
                        onBack = { navController.popBackStack() },
                        onEdit = { id -> navController.navigate(Routes.goodsEdit(id)) }
                    )
                }
                composable(Routes.GOODS_EDIT) { backStackEntry ->
                    val goodsId = backStackEntry.arguments?.getString("goodsId") ?: return@composable
                    GoodsFormScreen(
                        goodsId = goodsId,
                        onBack = { navController.popBackStack() },
                        onSaved = { savedId ->
                            navController.navigate(Routes.goodsDetail(savedId)) {
                                popUpTo(Routes.GOODS_EDIT) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
