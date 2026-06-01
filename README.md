# PickGoods Android

PickGoods 是一个面向「谷子 / 周边」收藏管理的 Android 原生应用，基于 Kotlin 与 Jetpack Compose 开发，当前代码对接 PickGoods 后端 REST API，支持账号认证、谷子管理、云展柜、收纳位置、IP / 角色 / 品类 / 主题等元数据管理，以及图片上传。

## 功能概览

- 账号认证：登录、注册、登出、Token 持久化、自动检查登录状态。
- 后端配置：可在登录页或设置页修改 API Base URL，默认地址为 `http://10.0.2.2:8000`。
- 谷子管理：列表、搜索、筛选、分页、详情、新增、编辑、删除、排序、主图与附加图片上传。
- 云展柜：公开 / 私有展柜列表，创建、编辑、删除、封面上传，向展柜添加 / 移除 / 移动谷子。
- 统计看板：展示谷子总数、总数量、估值、定位情况、状态分布、官方 / 非官方分布、IP 与品类排行。
- 收纳位置：位置节点管理，并可查看指定位置下的谷子。
- 元数据管理：IP、角色、品类、主题的增删改查，主题图片上传与标签维护。
- Bangumi 辅助导入：支持搜索条目、拉取角色，并创建 IP / 角色数据。
- 图片处理：使用 Android Photo Picker 选择图片，上传前压缩，使用 Coil 加载远程图片。

## 技术栈

| 层面 | 技术 |
| --- | --- |
| 语言 | Kotlin |
| UI | Jetpack Compose、Material 3 |
| 导航 | Navigation Compose |
| 状态管理 | ViewModel、StateFlow |
| 网络 | Retrofit、OkHttp、Gson |
| 依赖注入 | Hilt |
| 本地配置 | DataStore Preferences |
| 图片 | Coil、Android Photo Picker |
| 构建 | Gradle Kotlin DSL、Android Gradle Plugin |

当前工程参数：

- `applicationId`: `com.pickgoods.app`
- `minSdk`: 29
- `compileSdk`: 36
- `targetSdk`: 36
- Java / Kotlin JVM target: 11
- Gradle Wrapper: 8.13
- Android Gradle Plugin: 8.13.2
- Kotlin: 2.0.21

## 项目结构

```text
PickGoods/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/pickgoods/app/
│       │   ├── App.kt                    # Hilt Application
│       │   ├── MainActivity.kt           # Compose 入口
│       │   ├── data/
│       │   │   ├── api/                  # Retrofit API 定义
│       │   │   ├── local/                # DataStore Token / Base URL 管理
│       │   │   ├── model/                # 请求与响应数据模型
│       │   │   ├── network/              # Retrofit / OkHttp / 拦截器
│       │   │   ├── repository/           # 数据仓库
│       │   │   └── util/                 # 图片压缩等工具
│       │   ├── di/                       # Hilt 模块
│       │   └── ui/
│       │       ├── auth/                 # 登录 / 注册
│       │       ├── goods/                # 谷子列表、详情、表单
│       │       ├── location/             # 位置管理
│       │       ├── metadata/             # IP、角色、品类、主题
│       │       ├── navigation/           # 路由与底部导航
│       │       ├── settings/             # 设置页
│       │       ├── showcase/             # 云展柜与统计
│       │       └── theme/                # Compose 主题
│       └── res/
├── gradle/libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── dev_plan.md
```

## 运行项目

### 1. 准备环境

- 安装 Android Studio。
- 使用 Android Studio 打开本项目根目录。
- 确认 `local.properties` 中存在本机 Android SDK 路径，例如：

```properties
sdk.dir=C\:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk
```

`local.properties` 是本机配置文件，不应提交到 Git。

### 2. 启动后端

应用需要 PickGoods 后端 API。模拟器访问宿主机后端时，默认使用：

```text
http://10.0.2.2:8000
```

如果使用真机调试，请把后端地址改成电脑在局域网中的 IP，例如：

```text
http://192.168.1.100:8000
```

可以在登录页展开「服务器地址配置」，或登录后进入设置页修改后端地址。

### 3. 构建与安装

Windows PowerShell：

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

macOS / Linux：

```bash
./gradlew assembleDebug
./gradlew installDebug
```

也可以直接在 Android Studio 中选择 `app` 配置并点击 Run。

## 常用开发命令

```powershell
# 构建 Debug APK
.\gradlew.bat assembleDebug

# 运行本地单元测试
.\gradlew.bat testDebugUnitTest

# 运行连接设备 / 模拟器上的 Android 测试
.\gradlew.bat connectedDebugAndroidTest

# 清理构建产物
.\gradlew.bat clean
```

## 后端 API 对接

应用通过 Retrofit 定义 API，并由 `DynamicBaseUrlInterceptor` 将占位地址 `http://pickgoods.local/` 动态替换为 DataStore 中保存的真实后端地址。

主要接口模块：

| 模块 | 文件 | 主要路径 |
| --- | --- | --- |
| 认证 | `AuthApi.kt` | `/api/auth/login/`、`/api/auth/register/`、`/api/auth/me/`、`/api/auth/logout/` |
| 谷子 | `GoodsApi.kt` | `/api/goods/`、`/api/goods/{id}/`、`/api/goods/stats/`、图片上传接口 |
| 云展柜 | `ShowcaseApi.kt` | `/api/showcases/`、`/api/showcases/public/`、`/api/showcases/private/`、展柜谷子管理接口 |
| 位置 | `LocationApi.kt` | `/api/location/tree/`、`/api/location/nodes/` |
| 元数据 | `MetadataApi.kt` | `/api/ips/`、`/api/characters/`、`/api/categories/`、`/api/themes/`、`/api/bgm/*` |

认证 Token 会保存在 DataStore 中，请求时由 `AuthInterceptor` 自动添加：

```text
Authorization: Bearer <access_token>
```

如果后端返回 `401`，应用会清空本地 Token。

## 注意事项

- `AndroidManifest.xml` 当前允许明文 HTTP：`android:usesCleartextTraffic="true"`，便于本地调试。生产环境建议使用 HTTPS 并收紧网络安全配置。
- 图片上传前会压缩到较小体积，临时文件写入应用缓存目录。
- `.gitignore` 已忽略 Gradle / Kotlin 缓存、构建产物、本机配置、签名文件与常见系统文件。
- `Room` 与 `Vico` 依赖已加入工程，但当前核心数据流主要来自远程 API 与 DataStore；后续可继续扩展离线缓存和图表展示。

## 入口文件

- 应用入口：`app/src/main/java/com/pickgoods/app/MainActivity.kt`
- 路由入口：`app/src/main/java/com/pickgoods/app/ui/navigation/AppNavGraph.kt`
- 网络配置：`app/src/main/java/com/pickgoods/app/data/network/NetworkModule.kt`
- 后端地址管理：`app/src/main/java/com/pickgoods/app/data/local/TokenManager.kt`
- 主功能页：`app/src/main/java/com/pickgoods/app/ui/showcase/CloudShowcaseScreen.kt`
