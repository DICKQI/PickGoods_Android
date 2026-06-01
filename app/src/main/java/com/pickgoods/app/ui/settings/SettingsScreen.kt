package com.pickgoods.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Logout
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.common.PickGoodsTopBar
import com.pickgoods.app.ui.theme.TextLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
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
            PickGoodsTopBar(title = "设置")
        }
    ) { paddingValues ->
        PickGoodsScreen(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // 1. 后端地址配置卡片
            PickGoodsCard(radius = 18.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                        Row {
                            Text("当前后端地址：", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Text(
                                text = uiState.baseUrl,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row {
                            Text("默认后端地址：", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Text(
                                text = uiState.defaultBaseUrl,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextLight
                            )
                        }
                    }
                }
            }

            // 2. 使用说明卡片
            PickGoodsCard(radius = 18.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("使用说明", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        HelpItem("后端地址应为完整的 URL，包含协议（http:// 或 https://）和端口号")
                        HelpItem("示例：http://127.0.0.1:8000 或 https://api.example.com")
                        HelpItem("模拟器中使用 10.0.2.2 访问宿主机")
                        HelpItem("真机调试使用局域网 IP，如 192.168.1.100:8000")
                    }
                }
            }

            // 3. 账号信息卡片（仅登录后显示）
            if (uiState.username != null) {
                PickGoodsCard(radius = 18.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.isAdmin) {
                                Button(
                                    onClick = onNavigateToAdmin,
                                    modifier = Modifier.weight(1f)
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
                                modifier = Modifier.weight(1f)
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
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    Icons.Outlined.Logout,
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
                PickGoodsCard(radius = 18.dp) {
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
private fun HelpItem(text: String) {
    Text(
        text = "• $text",
        fontSize = 13.sp,
        color = TextLight,
        lineHeight = 20.sp
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(
            text = "$label：",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace
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
