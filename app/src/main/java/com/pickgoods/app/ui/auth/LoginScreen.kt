package com.pickgoods.app.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pickgoods.app.data.local.TokenManager
import com.pickgoods.app.ui.common.GoldAccentLine
import com.pickgoods.app.ui.common.MobileHeaderCard
import com.pickgoods.app.ui.common.MobileInfoTile
import com.pickgoods.app.ui.common.PickGoodsCard
import com.pickgoods.app.ui.common.PickGoodsScreen
import com.pickgoods.app.ui.common.PickGoodsShape
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.PurpleSecondary
import com.pickgoods.app.ui.theme.TextLight

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var showServerConfig by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val passwordMismatch = isRegisterMode &&
        confirmPassword.isNotBlank() &&
        password != confirmPassword
    val canSubmit = username.isNotBlank() &&
        password.isNotBlank() &&
        !passwordMismatch &&
        (!isRegisterMode || confirmPassword.isNotBlank()) &&
        !uiState.isLoading

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PickGoodsScreen(modifier = Modifier.padding(paddingValues)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 10.dp, bottom = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                MobileHeaderCard(
                    title = "✦ 拾谷 PickGoods",
                    subtitle = if (isRegisterMode) "创建账号后开始管理你的谷子" else "欢迎回来，继续整理云展柜",
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MobileInfoTile(
                        label = "后端",
                        value = if (uiState.baseUrl.isBlank()) "未配置" else "已配置",
                        subtitle = uiState.baseUrl.ifBlank { TokenManager.DEFAULT_BASE_URL },
                        accent = Gold,
                        modifier = Modifier.weight(1f)
                    )
                    MobileInfoTile(
                        label = "模式",
                        value = if (isRegisterMode) "注册" else "登录",
                        subtitle = if (showServerConfig) "正在配置地址" else "账号认证",
                        accent = PurpleSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                PickGoodsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    radius = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AuthModeSwitch(
                            isRegisterMode = isRegisterMode,
                            onModeChanged = { register ->
                                isRegisterMode = register
                                confirmPassword = ""
                                viewModel.clearError()
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        GoldAccentLine(modifier = Modifier.height(1.dp))
                        Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("用户名") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                        shape = PickGoodsShape.Control
                    )

                        Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("密码") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = PickGoodsShape.Control
                    )

                        if (isRegisterMode) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("确认密码") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                isError = passwordMismatch,
                                supportingText = {
                                    if (passwordMismatch) {
                                        Text("两次密码不一致")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = PickGoodsShape.Control
                            )
                        }

                        if (uiState.error != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (isRegisterMode) {
                                viewModel.register(username, password)
                            } else {
                                viewModel.login(username, password)
                            }
                        },
                        enabled = canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = PickGoodsShape.Control,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onSecondary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isRegisterMode) "注册" else "登录",
                                fontSize = 16.sp
                            )
                        }
                    }

                    }
                }

            // 服务器地址配置（折叠式）
            TextButton(onClick = { showServerConfig = !showServerConfig }) {
                Text(
                    text = if (showServerConfig) "收起服务器配置 ▲" else "服务器地址配置 ▼",
                    fontSize = 13.sp,
                    color = TextLight
                )
            }

                if (showServerConfig) {
                    PickGoodsCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        radius = 16.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                        OutlinedTextField(
                            value = uiState.baseUrl,
                            onValueChange = { viewModel.onBaseUrlChanged(it) },
                            label = { Text("后端 API 地址") },
                            placeholder = { Text("http://192.168.1.100:8000") },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = PickGoodsShape.Control
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "模拟器默认 10.0.2.2:8000，真机用局域网 IP",
                                fontSize = 11.sp,
                                color = TextLight,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AuthModeSwitch(
    isRegisterMode: Boolean,
    onModeChanged: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PickGoodsShape.Pill,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f)
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AuthModeSegment(
                label = "登录",
                selected = !isRegisterMode,
                onClick = { onModeChanged(false) },
                modifier = Modifier.weight(1f)
            )
            AuthModeSegment(
                label = "注册",
                selected = isRegisterMode,
                onClick = { onModeChanged(true) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AuthModeSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .clip(PickGoodsShape.Pill)
            .clickable(onClick = onClick),
        shape = PickGoodsShape.Pill,
        color = if (selected) PurpleSecondary else Color.Transparent,
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = label,
                color = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
