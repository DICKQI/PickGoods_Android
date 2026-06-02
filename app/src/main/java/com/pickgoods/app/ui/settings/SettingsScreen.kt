package com.pickgoods.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pickgoods.app.ui.common.MobileHeaderCard
import com.pickgoods.app.ui.common.MobileInfoTile
import com.pickgoods.app.ui.common.MobileSectionHeader
import com.pickgoods.app.ui.common.PickGoodsBackTopBar
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.PurpleSecondary
import com.pickgoods.app.ui.theme.TextLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("后端地址已保存。若修改了地址，请重启应用或重新登录以生效。")
            viewModel.clearSaveSuccess()
        }
    }

    if (uiState.showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLogoutConfirm() },
            title = { Text("退出登录") },
            text = { Text("确定要退出登录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.logout()
                    onLogout()
                }) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLogoutConfirm() }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PickGoodsBackTopBar(
                title = "设置",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        PickGoodsScreen(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
            MobileHeaderCard(
                title = "应用设置",
                subtitle = "服务器连接、账号权限与调试信息"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MobileInfoTile(
                    label = "后端",
                    value = if (uiState.baseUrl.isBlank()) "未配置" else "已配置",
                    subtitle = uiState.baseUrl,
                    accent = Gold,
                    modifier = Modifier.weight(1f)
                )
                MobileInfoTile(
                    label = "账号",
                    value = uiState.username ?: "未登录",
                    subtitle = roleLabel(uiState.role),
                    accent = PurpleSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
            MobileSectionHeader(
                title = "连接配置",
                subtitle = "修改地址后重新登录可刷新网络实例",
                accent = PurpleSecondary
            )
            // 1. 后端地址配置卡片
            PickGoodsCard(radius = 16.dp) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("后端服务配置", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.baseUrl,
                        onValueChange = { viewModel.onUrlChanged(it) },
                        label = { Text("后端 API 地址") },
                        placeholder = { Text("例如：http://192.168.1.100:8000") },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                        isError = uiState.urlError != null,
                        supportingText = {
                            if (uiState.urlError != null) {
                                Text(uiState.urlError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = PickGoodsShape.Control
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.resetToDefault() },
                            enabled = uiState.baseUrl != uiState.defaultBaseUrl,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("恢复默认")
                        }
                        Button(
                            onClick = { viewModel.saveUrl() },
                            enabled = !uiState.isSaving,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            if (uiState.isSaving) {
                                Text("保存中...")
                            } else {
                                Text("保存")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = "修改后需重启应用或重新登录以生效",
                            fontSize = 12.sp,
                            color = TextLight
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingValueRow("当前后端地址", uiState.baseUrl)
                        SettingValueRow("默认后端地址", uiState.defaultBaseUrl, muted = true)
                    }
                }
            }

            MobileSectionHeader(
                title = "使用提示",
                subtitle = "按运行环境选择后端地址",
                accent = Gold
            )
            // 2. 使用说明卡片
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MobileInfoTile(
                        label = "模拟器",
                        value = "10.0.2.2",
                        subtitle = "访问宿主机",
                        accent = Gold,
                        modifier = Modifier.weight(1f)
                    )
                    MobileInfoTile(
                        label = "真机",
                        value = "局域网 IP",
                        subtitle = "同一网络",
                        accent = PurpleSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MobileInfoTile(
                        label = "格式",
                        value = "http(s)",
                        subtitle = "含协议端口",
                        accent = PurpleSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    MobileInfoTile(
                        label = "生效",
                        value = "重新登录",
                        subtitle = "刷新连接",
                        accent = Gold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. 账号信息卡片（仅登录后显示）
            if (uiState.username != null) {
                MobileSectionHeader(
                    title = "账号",
                    subtitle = "权限与退出登录",
                    accent = PurpleSecondary
                )
                PickGoodsCard(radius = 16.dp) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("账号与个人信息", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            InfoRow("用户名", uiState.username ?: "—")
                            InfoRow("角色", roleLabel(uiState.role))
                            InfoRow("用户 ID", uiState.userId?.toString() ?: "—")
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (uiState.isAdmin)
                                "管理员可管理 IP、品类、角色、主题等公共元数据，并可查看所有用户数据。"
                            else
                                "普通用户只能访问和修改自己的谷子、展柜、主题与收纳位置；公共元数据为只读。",
                            fontSize = 13.sp,
                            color = TextLight,
                            lineHeight = 18.sp
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.isAdmin) {
                                Button(
                                    onClick = onNavigateToAdmin,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                    Text("管理后台")
                                }
                            }
                            OutlinedButton(
                                onClick = { viewModel.refreshUserInfo() },
                                enabled = !uiState.isRefreshing,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Text(if (uiState.isRefreshing) "刷新中..." else "刷新信息")
                            }
                            OutlinedButton(
                                onClick = { viewModel.showLogoutConfirm() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Text("退出登录")
                            }
                        }
                    }
                }
            } else {
                // 未登录提示
                PickGoodsCard(radius = 16.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = TextLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "登录后可在此查看账号信息与退出登录。",
                            fontSize = 14.sp,
                            color = TextLight
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "$label：",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SettingValueRow(label: String, value: String, muted: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = if (muted) TextLight else MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun roleLabel(role: String?): String {
    return when (role?.lowercase()) {
        "admin" -> "管理员"
        null -> "—"
        else -> "普通用户"
    }
}
