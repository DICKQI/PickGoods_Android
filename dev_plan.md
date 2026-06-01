# 📱 拾谷 PickGoods — Android 原生 App 开发计划

> 基于 Django DRF 后端 API + Vue 前端 UI/UX 的完整移植方案

---

## 一、项目概览

### 1.1 项目背景

后端「拾谷」(PickGoods) 是一个二次元谷子（周边）数字化管理平台，支持谷子的 CRUD、多维分类（IP/角色/品类/主题）、物理位置树管理、展柜展示、统计看板等功能。目前已有一个适配移动端的 Vue Web 前端。

### 1.2 开发目标

开发一个**原生 Android App**（Kotlin + Jetpack Compose），对接现有 Django DRF REST API，复刻 Vue 前端的全部功能模块，并充分利用 Android 原生能力提升体验（离线缓存、推送通知、相机拍照直接上传、文件管理等）。

### 1.3 技术选型

| 层面   | 技术栈                                                | 说明                       |
| ---- | -------------------------------------------------- | ------------------------ |
| 语言   | **Kotlin**                                         | 官方推荐，空安全、协程              |
| UI   | **Jetpack Compose**                                | 声明式 UI，与 Vue 的组件化思路一致    |
| 网络   | **Retrofit + OkHttp + Kotlin Serialization/ Gson** | REST API 调用              |
| 图片   | **Coil**                                           | Compose 原生图片加载，支持内存/磁盘缓存 |
| 状态管理 | **ViewModel + StateFlow**                          | 替代 Vue Pinia Store       |
| 导航   | **Compose Navigation**                             | 替代 Vue Router            |
| 本地存储 | **Room** (离线缓存) + **DataStore** (偏好设置 + Token)     |                          |
| DI   | **Hilt**                                           | 依赖注入                     |
| 图片选择 | **ActivityResult Contracts**                       | 相机/相册选图                  |
| 图表   | **Vico** 或自定义 Compose Canvas                       | 统计看板                     |
| 构建   | **Gradle Kotlin DSL**                              |                          |

---

## 二、API 端点映射清单

以下列出 Android App 需要对接的全部后端 REST API 端点。

### 2.1 认证模块

| 方法     | 端点                    | 用途       | 对应前端文件                      |
| ------ | --------------------- | -------- | --------------------------- |
| POST   | `/api/auth/register/` | 注册       | `auth.ts: register()`       |
| POST   | `/api/auth/login/`    | 登录       | `auth.ts: login()`          |
| GET    | `/api/auth/me/`       | 获取当前用户信息 | `auth.ts: getCurrentUser()` |
| DELETE | `/api/auth/logout/`   | 登出       | `auth.ts: logout()`         |

### 2.2 谷子模块

| 方法     | 端点                                              | 用途                | 对应前端文件                                  |
| ------ | ----------------------------------------------- | ----------------- | --------------------------------------- |
| GET    | `/api/goods/`                                   | 谷子列表（分页+筛选+搜索+分组） | `goods.ts: getGoodsList()`              |
| GET    | `/api/goods/{id}/`                              | 谷子详情              | `goods.ts: getGoodsDetail()`            |
| POST   | `/api/goods/`                                   | 创建谷子              | `goods.ts: createGoods()`               |
| PUT    | `/api/goods/{id}/`                              | 更新谷子              | `goods.ts: updateGoods()`               |
| POST   | `/api/goods/{id}/upload-main-photo/`            | 上传/更新主图           | `goods.ts: uploadMainPhoto()`           |
| POST   | `/api/goods/{id}/upload-additional-photos/`     | 上传/更新附加图          | `goods.ts: uploadAdditionalPhotos()`    |
| DELETE | `/api/goods/{id}/additional-photos/{photo_id}/` | 删除单张附加图           | `goods.ts: deleteAdditionalPhoto()`     |
| DELETE | `/api/goods/{id}/additional-photos/`            | 批量删除附加图           | `goods.ts: deleteAdditionalPhotos()`    |
| POST   | `/api/goods/{id}/move/`                         | 移动排序              | `goods.ts: moveGoods()`                 |
| GET    | `/api/goods/stats/`                             | 统计数据              | `goods.ts: getGoodsStats()`             |
| GET    | `/api/goods/similar-random/`                    | 相似随机推荐            | `goods.ts: getSimilarRandomGoodsList()` |

### 2.3 IP 作品模块

| 方法                   | 端点                             | 用途        |
| -------------------- | ------------------------------ | --------- |
| GET                  | `/api/ips/`                    | IP 列表     |
| POST                 | `/api/ips/`                    | 创建 IP     |
| POST                 | `/api/ips/batch-update-order/` | 批量更新排序    |
| GET/PUT/PATCH/DELETE | `/api/ips/{id}/`               | IP CRUD   |
| GET                  | `/api/ips/{id}/characters/`    | IP 下的角色列表 |

### 2.4 角色模块

| 方法                   | 端点                      | 用途             |
| -------------------- | ----------------------- | -------------- |
| GET                  | `/api/characters/`      | 角色列表（可按 IP 筛选） |
| POST                 | `/api/characters/`      | 创建角色           |
| GET/PUT/PATCH/DELETE | `/api/characters/{id}/` | 角色 CRUD        |

### 2.5 品类模块

| 方法                   | 端点                                      | 用途             |
| -------------------- | --------------------------------------- | -------------- |
| GET                  | `/api/categories/`                      | 品类列表（树形数据，含 children 嵌套） |
| GET                  | `/api/categories/tree/`                 | 品类扁平树（客户端 buildTree 组装） |
| POST                 | `/api/categories/`                      | 创建品类           |
| POST                 | `/api/categories/batch-update-order/`   | 批量更新排序         |
| GET/PUT/PATCH/DELETE | `/api/categories/{id}/`                 | 品类 CRUD        |

### 2.6 主题模块

| 方法                   | 端点                                        | 用途            |
| -------------------- | ----------------------------------------- | ------------- |
| GET                  | `/api/themes/`                            | 主题列表          |
| POST                 | `/api/themes/`                            | 创建主题          |
| GET/PUT/PATCH/DELETE | `/api/themes/{id}/`                       | 主题 CRUD       |
| POST                 | `/api/themes/{id}/upload-images/`          | 上传主题附加图片      |
| DELETE               | `/api/themes/{id}/images/{photo_id}/`      | 删除单张主题图片      |
| DELETE               | `/api/themes/{id}/images/`                 | 批量删除主题图片      |

### 2.7 展柜模块

| 方法                   | 端点                                        | 用途      |
| -------------------- | ----------------------------------------- | ------- |
| GET/POST             | `/api/showcases/`                         | 展柜列表/创建 |
| GET/PUT/PATCH/DELETE | `/api/showcases/{id}/`                    | 展柜 CRUD |
| GET                  | `/api/showcases/public/`                  | 公开展柜    |
| GET                  | `/api/showcases/private/`                 | 私有展柜    |
| POST                 | `/api/showcases/{id}/upload-cover-image/` | 上传封面    |
| GET                  | `/api/showcases/{id}/goods/`              | 展柜中谷子列表 |
| POST                 | `/api/showcases/{id}/add-goods/`          | 添加谷子到展柜 |
| POST                 | `/api/showcases/{id}/remove-goods/`       | 从展柜移除谷子 |
| POST                 | `/api/showcases/{id}/move-goods/`         | 展柜内谷子排序 |

### 2.8 位置模块

| 方法                   | 端点                                | 用途                            |
| -------------------- | --------------------------------- | ----------------------------- |
| GET                  | `/api/location/tree/`             | 位置树                           |
| GET/POST             | `/api/location/nodes/`            | 位置节点列表/创建                     |
| GET/PUT/PATCH/DELETE | `/api/location/nodes/{id}/`       | 节点 CRUD                       |
| GET                  | `/api/location/nodes/{id}/goods/` | 节点下的谷子列表（支持 `?include_children=true` 递归查询子节点下所有谷子） |

### 2.9 后台管理（管理员）

| 方法        | 端点                       | 用途      |
| --------- | ------------------------ | ------- |
| GET       | `/api/admin/users/`      | 用户列表    |
| POST      | `/api/admin/users/`      | 创建用户    |
| GET/PATCH | `/api/admin/users/{id}/` | 用户详情/更新 |
| GET       | `/api/admin/roles/`      | 角色列表    |

### 2.10 BGM（Bangumi）集成

| 方法   | 端点                               | 用途             |
| ---- | -------------------------------- | -------------- |
| POST | `/api/bgm/search-characters/`    | 搜索角色（按 IP 名称搜索） |
| POST | `/api/bgm/create-characters/`    | 从 Bangumi 导入角色 |
| POST | `/api/bgm/search-subjects/`      | 两步搜索第 1 步：搜索作品   |
| POST | `/api/bgm/get-characters-by-id/` | 两步搜索第 2 步：获取作品下角色 |

---

## 三、数据模型（Android 端）

对应 Kotlin data class，与后端 JSON 字段一一映射。

```kotlin
// Auth
data class AuthTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)

data class UserInfo(
    val id: Int,
    val username: String,
    @SerializedName("role") val role: String // "User" | "Admin"
)

// IP & Character
data class IP(
    val id: Int,
    val name: String,
    @SerializedName("short_name") val shortName: String? = null,
    val keywords: List<IPKeyword>? = null,
    @SerializedName("subject_type") val subjectType: Int? = null,
    @SerializedName("character_count") val characterCount: Int? = null
)

data class IPKeyword(val id: Int, val value: String)

data class Character(
    val id: Int,
    val name: String,
    val ip: IP,
    val avatar: String? = null,
    val gender: String // "male" | "female" | "other"
)

// Category (tree structure)
data class Category(
    val id: Int,
    val name: String,
    val parent: Int? = null,
    @SerializedName("path_name") val pathName: String,
    @SerializedName("color_tag") val colorTag: String? = null,
    val order: Int = 0,
    val children: List<Category>? = null
)

// Theme
data class Theme(
    val id: Int,
    val name: String,
    val description: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    val images: List<ThemeImage>? = null
)

data class ThemeImage(
    val id: Int,
    val image: String,
    val label: String? = null
)

// Goods (core)
enum class GoodsStatus {
    @SerializedName("draft") DRAFT,
    @SerializedName("in_cabinet") IN_CABINET,
    @SerializedName("outdoor") OUTDOOR,
    @SerializedName("sold") SOLD
}

data class GoodsListItem(
    val id: String, // UUID
    val name: String,
    val ip: IP,
    val characters: List<Character>,
    val category: Category,
    val theme: Theme? = null,
    @SerializedName("location_path") val locationPath: String,
    @SerializedName("main_photo") val mainPhoto: String? = null,
    val status: GoodsStatus,
    val quantity: Int,
    @SerializedName("is_official") val isOfficial: Boolean = true,
    val user: UserRef? = null,
    @SerializedName("user_id") val userId: Int? = null
)

data class GoodsDetail(
    val id: String,
    val name: String,
    val ip: IP,
    val characters: List<Character>,
    val category: Category,
    val theme: Theme? = null,
    @SerializedName("location_path") val locationPath: String,
    @SerializedName("main_photo") val mainPhoto: String? = null,
    val status: GoodsStatus,
    val quantity: Int,
    @SerializedName("is_official") val isOfficial: Boolean = true,
    val user: UserRef? = null,
    @SerializedName("user_id") val userId: Int? = null,
    val location: Int? = null,
    val price: String? = null,
    @SerializedName("purchase_date") val purchaseDate: String? = null,
    val notes: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("additional_photos") val additionalPhotos: List<GuziImage> = emptyList()
)

data class GuziImage(
    val id: Int,
    val image: String,
    val label: String? = null
)

// Pagination
data class PaginatedResponse<T>(
    val count: Int,
    val page: Int,
    @SerializedName("page_size") val pageSize: Int,
    val next: Int? = null,
    val previous: Int? = null,
    val results: List<T>
)

// Location / StorageNode
data class StorageNode(
    val id: Int,
    val name: String,
    val parent: Int? = null,
    @SerializedName("path_name") val pathName: String,
    val image: String? = null,
    val description: String? = null,
    val order: Int = 0
)

// Showcase & ShowcaseGoods
data class Showcase(
    val id: String, // UUID
    val name: String,
    val description: String? = null,
    @SerializedName("cover_image") val coverImage: String? = null,
    val order: Long = 0,
    @SerializedName("is_public") val isPublic: Boolean = true,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("goods_count") val goodsCount: Int? = null,
    @SerializedName("preview_photos") val previewPhotos: List<String>? = null  // 前4张谷子主图，用于马赛克预览
)

data class ShowcaseDetail(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerializedName("cover_image") val coverImage: String? = null,
    val order: Long = 0,
    @SerializedName("is_public") val isPublic: Boolean = true,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("goods_count") val goodsCount: Int? = null,
    @SerializedName("preview_photos") val previewPhotos: List<String>? = null,
    @SerializedName("showcase_goods") val showcaseGoods: List<ShowcaseGoods>? = null
)

data class ShowcaseGoods(
    val id: String, // UUID
    @SerializedName("goods_id") val goodsId: String,
    val goods: GoodsListItem,
    val order: Long = 0,
    val notes: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

// User reference (used in goods list/detail)
data class UserRef(
    val id: Int,
    val username: String
)

// Goods search/filter params
data class GoodsSearchParams(
    val ip: Int? = null,
    val character: Int? = null,
    @SerializedName("characters__in") val charactersIn: String? = null, // comma-separated IDs
    val category: Int? = null,
    val theme: Int? = null,
    val status: String? = null, // draft | in_cabinet | outdoor | sold
    @SerializedName("status__in") val statusIn: String? = null, // comma-separated, e.g. "in_cabinet,sold"
    @SerializedName("is_official") val isOfficial: Boolean? = null,
    val location: Int? = null,
    val search: String? = null,
    val page: Int = 1,
    @SerializedName("page_size") val pageSize: Int = 18,
    @SerializedName("group_by") val groupBy: String? = null // ip | character | category | theme
)

// Goods creation params (with duplicate merge strategy)
data class GoodsCreateParams(
    val name: String,
    @SerializedName("ip_id") val ipId: Int,
    @SerializedName("character_ids") val characterIds: List<Int>,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("theme_id") val themeId: Int? = null,
    val location: Int? = null,
    val status: String = "draft", // draft | in_cabinet | outdoor | sold
    val quantity: Int = 1,
    val price: String? = null,
    @SerializedName("purchase_date") val purchaseDate: String? = null,
    @SerializedName("is_official") val isOfficial: Boolean = true,
    val notes: String? = null,
    @SerializedName("merge_strategy") val mergeStrategy: String = "auto", // auto | new | merge
    @SerializedName("merge_target_id") val mergeTargetId: String? = null // UUID, required when merge_strategy=merge
)

// Duplicate candidate from 409 response
data class GoodsDuplicateCandidate(
    val id: String,
    val name: String,
    val ip: IP,
    val characters: List<Character>,
    @SerializedName("main_photo_url") val mainPhotoUrl: String? = null,
    val quantity: Int,
    @SerializedName("created_at") val createdAt: String
)

data class DuplicateConflictResponse(
    val code: String, // "goods_duplicate"
    val detail: String,
    val candidates: List<GoodsDuplicateCandidate>
)

// Stats
data class StatsOverview(
    val total: Int,
    @SerializedName("total_value") val totalValue: String? = null,
    @SerializedName("completeness") val completeness: Double? = null
)

data class DistributionItem(
    val label: String,
    val count: Int
)

data class TopRankItem(
    val label: String,
    val count: Int
)

data class TrendItem(
    val label: String,
    val count: Int
)

data class StatsResponse(
    val overview: StatsOverview? = null,
    val distribution: Map<String, List<DistributionItem>>? = null,
    @SerializedName("top_ranks") val topRanks: Map<String, List<TopRankItem>>? = null,
    val trends: Map<String, List<TrendItem>>? = null
)
```

---

## 四、页面与导航结构

### 4.1 导航架构

```
App
├── AuthFlow (未登录)
│   ├── LoginScreen
│   └── RegisterScreen
│
└── MainFlow (已登录，底部导航)
    ├── BottomNavBar (5 tabs)
    │   ├── Tab 0: 云展柜 (ShowcaseFlow)
    │   ├── Tab 1: 位置管理 (LocationFlow)
    │   ├── Tab 2: IP与角色 (IPCharacterFlow)
    │   ├── Tab 3: 品类管理 (CategoryFlow)
    │   └── Tab 4: 主题管理 (ThemeFlow)
    │
    ├── Settings (从顶部导航进入)
    ├── AdminFlow (管理员入口)
    │   ├── AdminDashboard
    │   ├── AdminUserManagement
    │   └── AdminGoodsManagement
    │
    └── GoodsSubPages (从云展柜进入)
        ├── GoodsDetailScreen
        ├── GoodsFormScreen (新增/编辑)
        ├── GoodsDraftsScreen
        └── GoodsStatsScreen (统计看板)
```

### 4.2 云展柜主页（ShowcaseFlow）

对应前端 `CloudShowcase.vue`，使用 `HorizontalPager` 或 `TabRow` 实现三 Tab 切换：

| Tab  | 页面                     | 说明                |
| ---- | ---------------------- | ----------------- |
| 展柜   | `ShowcaseListScreen`   | 展柜管理（列表+网格+预览）    |
| 谷仓   | `GoodsListScreen`      | 谷子列表（筛选+搜索+分组+分页） |
| 统计看板 | `StatsDashboardScreen` | 统计图表              |

**谷仓子页面**：

- 顶部：SearchBar（搜索框）
- 折叠式 FilterPanel（IP/角色/品类/主题/状态/官非筛选）
- 内容区域：LazyVerticalGrid（商品卡片网格）
- 底部：分页控制器

### 4.3 谷子详情（GoodsDetailScreen）

对应前端 `GoodsDrawer.vue` + 详情功能：

- 顶部：主图（可放大查看）
- 信息区：名称、IP、角色、品类、主题、位置、状态、数量、价格、入手时间、官非标记
- 附加图片：横向滚动的图片列表
- 备注文本
- 操作栏：编辑、删除、移动排序

### 4.4 谷子表单（GoodsFormScreen）

对应前端 `GoodsForm.vue`，组件化程度最高：

- 名称输入
- IP选择器（搜索+下拉）
- 角色选择器（多选，按 IP 过滤）
- 品类选择器（树形选择）
- 主题选择器
- 状态选择器
- 位置选择器（树形选择）
- 主图上传（相机/相册，裁剪）
- 附加图片管理（多图上传，标签编辑，删除）
- 价格、数量、入手日期
- 官非开关
- 备注
- 草稿/保存/合并去重逻辑

### 4.5 位置管理（LocationFlow）

对应前端 `LocationManagement.vue`：

- 树形展示（可折叠展开）
- 增删改节点
- 点击节点查看该位置下的谷子列表

### 4.6 IP与角色管理（IPCharacterFlow）

对应前端 `IPCharacterManagement.vue`：

- 双栏/分段：IP 列表 ↔ 角色列表
- IP CRUD（名称、关键词、作品类型、排序）
- 角色 CRUD（名称、性别、头像、关联 IP）
- Bangumi 导入（搜索作品→获取角色→批量创建）

### 4.7 品类管理（CategoryFlow）

对应前端 `CategoryManagement.vue`：

- 树形展示
- 增删改节点（名称、颜色标签、排序）

### 4.8 主题管理（ThemeFlow）

对应前端 `ThemeManagement.vue`：

- 主题列表
- CRUD（名称、描述、附加图片）

### 4.9 后台管理（AdminFlow）

对应前端 admin 目录：

- 用户管理（列表、创建、编辑角色、启用/禁用）
- 谷子管理（管理员全局查看）

---

## 五、开发阶段与里程碑

### Phase 1：项目基建（预估 3-5 天）

| 任务                                                          | 产出                  |
| ----------------------------------------------------------- | ------------------- |
| 创建 Android 项目（Kotlin + Compose + Hilt）                      | 工程骨架                |
| 配置 Gradle 依赖（Retrofit, Room, Coil, Navigation, DataStore 等） | `build.gradle.kts`  |
| 搭建网络层（Retrofit 接口定义 + OkHttp 拦截器自动附加 Token）                 | `api/` 包            |
| 实现 Token 管理（DataStore 存储 + 自动刷新）                            | `auth/TokenManager` |
| 实现通用分页加载组件                                                  | `PagingLoadable`    |
| 配置 Navigation + Bottom Nav 框架                               | 底部五 Tab 空壳可切换       |
| 搭建全局主题（日间/夜间模式）                                             | `theme/`            |

### Phase 2：认证模块（预估 1-2 天）

| 任务                 | 优先级 |
| ------------------ | --- |
| 登录页面 UI + 逻辑       | P0  |
| 注册页面 UI + 逻辑       | P0  |
| Token 持久化 + 拦截器附加  | P0  |
| App 启动自动检查登录态 + 跳转 | P0  |
| 退出登录               | P0  |
| 设置页面（用户信息 + 服务器地址配置 + 深色模式） | P0  |

### Phase 3：核心模块 — 谷子（预估 5-7 天）

| 任务                           | 优先级 | 说明 |
| ---------------------------- | --- | --- |
| 谷子列表页（LazyVerticalGrid + 卡片） | P0  | `GoodsListScreen` + `GoodsCard` 组件 |
| 搜索栏（OutlinedTextField + 300ms 防抖） | P0  | 绑定 `_searchQuery` StateFlow，自动触发列表刷新 |
| 筛选面板 `FilterSheet`（ModalBottomSheet） | P0  | IP/角色多选/品类树/主题/状态多选/官非/位置树/分组/展示模式 |
| 多选状态筛选（`status__in`）          | P0  | 用 `FilterChip` + `FlowRow`，多选时传 `status__in`，单选传 `status` |
| `group_by` 分组展示               | P0  | 选择分组字段后列表按组头分段排列，每组内显示谷子卡片 |
| 分页加载（底部页码 + 上拉加载更多）          | P0  | `PaginationBar` 组件 + 手动分页（非 Paging3） |
| 谷子详情页（图片懒加载、附加图片轮播）          | P0  | `GoodsDetailScreen`：主图放大查看 + `HorizontalPager` 附加图轮播 |
| 谷子表单 `GoodsFormScreen`（新建/编辑）           | P0  | 见 `15.1` 创建/编辑流程 |
| IP选择器（搜索+下拉）                | P0  | `ExposedDropdownMenu` + 异步搜索 |
| 角色多选选择器（按 IP 过滤）            | P0  | 选中 IP 后加载该 IP 角色列表，`FlowRow` + `FilterChip` 多选 |
| 品类/位置树形选择器                 | P0  | `TreePickerDialog`：层级导航（面包屑），逐级进入 |
| 主题选择器                       | P0  | `ExposedDropdownMenu` |
| 状态/官非开关                     | P0  | `SegmentedButton` / `Switch` |
| 图片选择（相册）+ 主图上传              | P0  | `ActivityResultContracts.PickVisualMedia` → 压缩 → Retrofit Multipart |
| 附加图片管理（多图上传 + 标签编辑 + 删除）     | P0  | 复用 `UploadRequest` 模式，支持 `photo_ids` 替换已有图片 |
| 相机拍照直接上传                     | P1  | `ActivityResultContracts.TakePicture` + FileProvider |
| 图片裁剪（类似前端 ImageCropper）      | P1  | 自定义 Compose Canvas 或集成 ucrop 库 |
| 草稿箱页面 `GoodsDraftsScreen`     | P1  | 筛选 status=draft，复用 `GoodsCard` |
| 谷子排序（移至前/移至后）                | P1  | 调用 `POST /api/goods/{id}/move/`，长按弹出上下文菜单 |
| 去重合并交互流程（409 响应处理）           | P0  | 见下方详细描述 |
| 相似随机展示页面 `SimilarRandomScreen` | P2  | `GET /api/goods/similar-random/`，不分页，交错排列 |

#### 去重合并交互流程（409 Conflict）

```
创建谷子 POST /api/goods/ { merge_strategy: "auto" }
├── 200 → 创建成功，跳转详情页
├── 400 → 显示表单验证错误
└── 409 DuplicateConflictResponse
    ├── 弹窗展示候选谷子列表（名称、缩略图、数量、创建时间）
    ├── 用户选择：
    │   ├── [取消] → 关闭弹窗，保留表单数据
    │   ├── [作为新谷子创建] → 重新 POST，merge_strategy="new"
    │   └── [合并到选中谷子] → 重新 POST，merge_strategy="merge", merge_target_id=candidate.id
    └── 合并成功 → quantity 累加，跳转到合并后的谷子详情

### Phase 4：数据字典模块（预估 3-4 天）

| 模块  | 任务                       | 优先级 |
| --- | ------------------------ | --- |
| IP  | 列表 + CRUD                | P0  |
| IP  | Bangumi 搜索作品 + 导入角色      | P1  |
| 角色  | 列表（按 IP 筛选）+ CRUD + 头像上传 | P0  |
| 品类  | 树形列表 + CRUD              | P0  |
| 主题  | 列表 + CRUD + 附加图片         | P0  |
| 位置  | 树形列表 + CRUD + 节点下谷子列表    | P0  |

### Phase 5：展柜模块（预估 3-4 天）

| 任务                | 优先级 |
| ----------------- | --- |
| 展柜列表页（网格/列表切换）    | P0  |
| 展柜详情（谷子网格）        | P0  |
| 展柜 CRUD + 封面上传    | P0  |
| 展柜内谷子管理（添加/移除/排序） | P1  |
| 展柜预制视图（文件夹/马赛克预览） | P2  |

### Phase 6：统计看板（预估 2-3 天）

| 任务                          | 优先级 |
| --------------------------- | --- |
| 概览卡片（总数、总金额、信息完整度）          | P0  |
| 分布图（状态/官非/作品类型）—— 饼图        | P0  |
| Top N 排行（品类/IP/角色/位置）—— 柱状图 | P0  |
| 趋势图（入手/创建时间）—— 折线图          | P1  |

### Phase 7：后台管理（预估 2 天）

| 任务        | 优先级 |
| --------- | --- |
| 用户列表（管理员） | P1  |
| 用户创建/编辑   | P1  |
| 管理员全局谷子列表 | P2  |

### Phase 8：增强功能（预估 3-5 天）

| 任务        | 说明                     | 优先级 |
| --------- | ---------------------- | --- |
| Room 离线缓存 | 谷子列表/数据字典本地缓存，弱网可用     | P1  |
| 图片本地缓存    | Coil 磁盘缓存已自带           | P0  |
| 深色模式      | Compose Material3 原生支持 | P1  |
| 分享功能      | 谷子/展柜截图分享              | P2  |
| 桌面 Widget | 快捷查看/搜索                | P3  |
| 指纹/面容锁    | 保护敏感数据                 | P2  |
| 批量操作      | 批量修改状态/位置等             | P3  |
| 数据导出/备份   | CSV / JSON 导出          | P3  |

---

## 六、Android 特有设计要点

### 6.1 Token 管理

```
┌─────────────────────────┐
│     App Launch          │
│  DataStore.read(token)  │
│       ↓                 │
│  token 存在?            │
│  ├── No → LoginScreen   │
│  └── Yes → Validate     │
│       ↓                 │
│  GET /api/auth/me/      │
│  ├── 200 → MainFlow     │
│  └── 401 → LoginScreen  │
└─────────────────────────┘
```

- OkHttp 全局拦截器自动附加 `Authorization: Bearer <token>`
- 401 响应时清 Token 并跳转登录

### 6.2 图片加载策略

- 后端返回的 `main_photo` / `additional_photos` 等字段为相对路径（如 `/media/goods/main/xxx.jpg`）
- Android 端需配置 **Base URL**（在设置中可配置服务器地址 + 端口）
- Coil 加载时拼接完整 URL：`${baseUrl}${imagePath}`
- 列表页使用 `size=medium` 缩略图尺寸，详情页使用原图

### 6.3 树形数据适配

后端返回的品类/位置为树形结构（`children` 嵌套），Android 端有两种展示方案：

1. **Compose LazyColumn + 缩进**：类似前端 el-tree 的交互
2. **底部弹出 Picker 层级选择**：类似文件选择器，逐层进入

推荐方案：列表页用方案 1，表单选择用方案 2。

### 6.4 图片上传

- 前端使用 `FormData` 上传，Android 端 Retrofit 同样支持 `@Multipart`
- 主图上传用 `@Part main_photo: MultipartBody.Part`
- 附加图批量上传用 `@Part List<MultipartBody.Part> additional_photos`
- 图片压缩策略（参考后端的 `compress_image` 逻辑，控制在 300KB 以内）

### 6.5 分页 & 下拉刷新

- 使用 `LazyVerticalGrid` + `Paging3` 或手动分页
- 下拉刷新用 Compose Material3 的 `pullRefresh`
- 分页参数：`page`（从 1 开始）、`page_size`（默认 18）

### 6.6 搜索优化

- 搜索框输入使用 `debounce`（300ms 防抖）
- 前端搜索参数复用：`search` 关键词、`ip`、`character`、`characters__in`、`category`(树形)、`location`(树形)、`theme`、`status`、`status__in`、`is_official`、`group_by`

---

## 七、项目目录结构建议

```
app/src/main/java/com/pickgoods/app/
├── App.kt                          # Application + Hilt
├── MainActivity.kt                 # Single Activity
│
├── data/
│   ├── api/                        # Retrofit 接口
│   │   ├── AuthApi.kt
│   │   ├── GoodsApi.kt
│   │   ├── IPApi.kt
│   │   ├── CharacterApi.kt
│   │   ├── CategoryApi.kt
│   │   ├── ThemeApi.kt
│   │   ├── ShowcaseApi.kt
│   │   ├── LocationApi.kt
│   │   └── AdminApi.kt
│   ├── model/                      # 数据类
│   │   ├── AuthModels.kt
│   │   ├── GoodsModels.kt
│   │   ├── DictModels.kt          # IP/Character/Category/Theme
│   │   ├── LocationModels.kt
│   │   ├── ShowcaseModels.kt
│   │   └── AdminModels.kt
│   ├── repository/                 # 数据仓库
│   │   ├── AuthRepository.kt
│   │   ├── GoodsRepository.kt
│   │   ├── DictRepository.kt
│   │   ├── LocationRepository.kt
│   │   ├── ShowcaseRepository.kt
│   │   └── AdminRepository.kt
│   ├── local/                      # Room + DataStore
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   ├── entity/
│   │   └── TokenManager.kt
│   └── network/                    # OkHttp 配置
│       ├── ApiClient.kt
│       ├── AuthInterceptor.kt
│       └── ApiResult.kt            # 统一封装
│
├── di/                             # Hilt 模块
│   ├── NetworkModule.kt
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
│
├── ui/
│   ├── navigation/
│   │   ├── AppNavGraph.kt
│   │   └── BottomNavBar.kt
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   └── AuthViewModel.kt
│   ├── showcase/
│   │   ├── ShowcaseScreen.kt       # 云展柜主页（3 Tab）
│   │   ├── ShowcaseDetailScreen.kt
│   │   ├── ShowcaseViewModel.kt
│   │   └── components/
│   ├── goods/
│   │   ├── GoodsListScreen.kt
│   │   ├── GoodsDetailScreen.kt
│   │   ├── GoodsFormScreen.kt
│   │   ├── GoodsDraftsScreen.kt
│   │   ├── GoodsViewModel.kt
│   │   └── components/
│   │       ├── GoodsCard.kt
│   │       ├── FilterSheet.kt
│   │       ├── SearchBar.kt
│   │       └── ImageCropper.kt
│   ├── stats/
│   │   ├── StatsDashboardScreen.kt
│   │   └── StatsViewModel.kt
│   ├── location/
│   │   ├── LocationScreen.kt
│   │   ├── LocationViewModel.kt
│   │   └── components/
│   ├── ipcharacter/
│   │   ├── IPCharacterScreen.kt
│   │   ├── IPCharacterViewModel.kt
│   │   └── components/
│   ├── category/
│   │   ├── CategoryScreen.kt
│   │   ├── CategoryViewModel.kt
│   │   └── components/
│   ├── theme/
│   │   ├── ThemeScreen.kt
│   │   ├── ThemeViewModel.kt
│   │   └── components/
│   ├── admin/
│   │   ├── AdminScreen.kt
│   │   ├── AdminViewModel.kt
│   │   └── components/
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   └── SettingsViewModel.kt
│   └── common/                     # 可复用组件
│       ├── LoadingIndicator.kt
│       ├── ErrorView.kt
│       ├── EmptyView.kt
│       ├── ConfirmDialog.kt
│       ├── TreePickerDialog.kt     # 树形选择器
│       └── ImagePicker.kt
│
└── util/
    ├── DateUtils.kt
    ├── ImageUtils.kt
    └── DebounceUtils.kt
```

---

## 八、关键 Compose 组件对照

| Vue 组件                       | Compose 等价实现                                       |
| ---------------------------- | -------------------------------------------------- |
| `MobileBottomNav.vue`        | `BottomNavBar` (Scaffold + NavigationBar)          |
| `Layout.vue`                 | `Scaffold` + `TopAppBar` + `NavigationBar`         |
| `GoodsCard.vue`              | `GoodsCard` (Card + AsyncImage + Column)           |
| `SearchBar.vue`              | `SearchBar` (OutlinedTextField + debounce)         |
| `FilterPanel.vue`            | `FilterSheet` (ModalBottomSheet + Chip/FilterChip) |
| `GoodsDrawer.vue`            | `GoodsDetailScreen` (Scaffold + LazyColumn)        |
| `FilterPanel.vue` 中的 el-tree | `TreePickerDialog` (自定义递归 LazyColumn)              |
| `StatsDashboard.vue`         | `StatsDashboardScreen` (Vico ColumnChart/PieChart) |
| `ImageCropper.vue`           | `ImageCropper` (Compose Canvas + 手势缩放/裁剪)          |
| 展柜视图组件                       | `LazyVerticalGrid` + StaggeredGrid 组合              |

---

## 九、ViewModel + StateFlow 状态管理模式

Android 端统一使用 `ViewModel + StateFlow` 替代 Vue Pinia Store。

### 9.1 统一的 UiState 封装

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val code: Int? = null) : UiState<Nothing>()
}
```

每个 ViewModel 暴露 `StateFlow<UiState<T>>` 供 Compose 收集，UI 层通过 `when` 分支渲染 Loading/Error/Success。

### 9.2 ViewModel 设计模板

```kotlin
@HiltViewModel
class GoodsViewModel @Inject constructor(
    private val goodsRepo: GoodsRepository,
    private val dictRepo: DictRepository
) : ViewModel() {

    // 列表状态
    private val _listState = MutableStateFlow<UiState<PaginatedResponse<GoodsListItem>>>(UiState.Loading)
    val listState: StateFlow<UiState<PaginatedResponse<GoodsListItem>>> = _listState.asStateFlow()

    // 筛选条件（StateFlow 驱动 UI 联动）
    private val _filters = MutableStateFlow(GoodsSearchParams())
    val filters: StateFlow<GoodsSearchParams> = _filters.asStateFlow()

    // 防抖搜索
    private val _searchQuery = MutableStateFlow("")

    init {
        // 当筛选条件变化时自动触发列表加载
        viewModelScope.launch {
            _filters.debounce(300).collect { loadList() }
        }
        viewModelScope.launch {
            _searchQuery.debounce(300).collect { query ->
                _filters.update { it.copy(search = query.ifBlank { null }) }
            }
        }
    }

    fun loadList() { ... }
    fun updateFilter(transform: (GoodsSearchParams) -> GoodsSearchParams) { ... }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
}
```

### 9.3 Pinia Store ↔ ViewModel 对照

| Vue Pinia Store        | Android ViewModel           | 说明                        |
| ---------------------- | --------------------------- | ------------------------- |
| `useAuthStore`         | `AuthViewModel`             | Token 管理由 DataStore 独立完成    |
| `useGuziStore`         | `GoodsViewModel`            | 列表 + 筛选 + 分页               |
| `useLocationStore`     | `LocationViewModel`         | 位置树 + 节点 CRUD              |
| `useMetadataStore`     | 各 ViewModel 内嵌 `DictRepository` | IP/角色/品类/主题缓存，不必建独立 Store |
| `useShowcaseStore`     | `ShowcaseViewModel`         | 展柜列表 + 详情 + 谷子管理          |
| localStorage 元数据缓存   | `Room` DB                   | 离线持久化，非内存缓存               |

### 9.4 Compose 中的状态收集

```kotlin
@Composable
fun GoodsListScreen(viewModel: GoodsViewModel = hiltViewModel()) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()

    when (val state = listState) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Error -> ErrorView(state.message, onRetry = { viewModel.loadList() })
        is UiState.Success -> {
            LazyVerticalGrid(...) {
                items(state.data.results) { goods ->
                    GoodsCard(goods)
                }
            }
            // 底部加载更多 / 分页控制器
            PaginationBar(
                page = state.data.page,
                totalPages = state.data.count.ceilDiv(state.data.pageSize),
                onPageChange = { viewModel.setPage(it) }
            )
        }
    }
}
```

---

## 十、网络错误处理策略

### 10.1 OkHttp 拦截器统一处理

```kotlin
class ErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        when (response.code) {
            401 -> { /* 清 Token → 跳转登录 */ }
            403 -> { /* 封装到 Response body，UI 层展示"无权限" */ }
            409 -> { /* 透传，由 GoodsForm 处理去重弹窗 */ }
            429 -> { /* 提示"操作太频繁" */ }
        }
        return response
    }
}
```

### 10.2 各状态码处理策略

| 状态码 | 含义       | 处理方式                                  |
| --- | -------- | ------------------------------------- |
| 200 | 成功       | 正常解析                                  |
| 400 | 请求参数错误   | 显示 `detail` 字段信息                      |
| 401 | 未认证      | DataStore 清 Token → `navigation.navigate(Login)` |
| 403 | 无权限      | Snackbar / Toast "无权限访问"               |
| 409 | 冲突（谷子重复） | 透传 response body → 弹出去重候选列表            |
| 429 | 限流       | Toast "搜索太快，请稍后再试"                     |
| 5xx | 服务器错误    | Toast "服务器繁忙，请稍后重试" + 重试按钮             |

### 10.3 Repository 层统一封装

```kotlin
// ApiResult.kt — 统一网络响应封装
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class HttpError(val code: Int, val message: String, val rawBody: String? = null) : ApiResult<Nothing>()
    data class NetworkError(val exception: Throwable) : ApiResult<Nothing>()
}

// Repository 中统一 try-catch
suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (e: HttpException) {
        val body = e.response()?.errorBody()?.string()
        ApiResult.HttpError(e.code(), e.message(), body)
    } catch (e: IOException) {
        ApiResult.NetworkError(e)
    }
}
```

---

## 十一、图片上传压缩管线

### 11.1 对齐后端压缩策略

后端 `compress_image` 限制输出最大 **300KB**、固定 JPEG 格式、quality 从 85 递减。Android 端需在客户端进行预压缩：

```kotlin
// ImageUtils.kt
fun compressImage(context: Context, uri: Uri, maxSizeKB: Int = 300): File {
    // 1. 读取原始 Bitmap
    val inputStream = context.contentResolver.openInputStream(uri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    inputStream?.close()

    // 2. 渐进压缩：quality 从 85 → 10，若仍超限则缩小尺寸（每次 -10%，最低 30%）
    var quality = 85
    var bitmap = originalBitmap
    val outputFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")

    while (quality >= 10) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        if (baos.size() / 1024 <= maxSizeKB) {
            outputFile.writeBytes(baos.toByteArray())
            return outputFile
        }
        quality -= 5
        if (quality < 20) {
            val scale = maxOf(0.3f, (maxSizeKB.toFloat() * 1024 / baos.size()))
            val newW = (bitmap.width * scale).toInt()
            val newH = (bitmap.height * scale).toInt()
            bitmap = Bitmap.createScaledBitmap(originalBitmap, newW, newH, true)
        }
    }

    // 兜底：最低质量 + 缩小到 30% 尺寸
    val finalW = (originalBitmap.width * 0.3f).toInt()
    val finalH = (originalBitmap.height * 0.3f).toInt()
    val finalBitmap = Bitmap.createScaledBitmap(originalBitmap, finalW, finalH, true)
    val baos = ByteArrayOutputStream()
    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos)
    outputFile.writeBytes(baos.toByteArray())

    // 清理
    if (bitmap != originalBitmap) bitmap.recycle()
    finalBitmap.recycle()
    originalBitmap.recycle()

    return outputFile
}
```

### 11.2 列表缩略图 vs 详情原图

| 场景   | 策略                                    | 说明                            |
| ---- | ------------------------------------- | ----------------------------- |
| 列表卡片 | Coil 加载时请求 `size=medium` 缩略图（若后端支持）   | 减少内存占用，提升滑动流畅度               |
| 详情页  | 原图 + Coil `diskCacheStrategy(DiskCacheStrategy.ALL)` | 支持双击放大                     |
| 附加图片 | 小图预览 + 点击全屏（`HorizontalPager` 轮播）      | Coil `subSamplingScale` 自动处理 |

### 11.3 Retrofit Multipart 上传

```kotlin
// GoodsApi.kt
@Multipart
@POST("api/goods/{id}/upload-main-photo/")
suspend fun uploadMainPhoto(
    @Path("id") id: String,
    @Part mainPhoto: MultipartBody.Part
): GoodsDetail

@Multipart
@POST("api/goods/{id}/upload-additional-photos/")
suspend fun uploadAdditionalPhotos(
    @Path("id") id: String,
    @Part additionalPhotos: List<MultipartBody.Part>,
    @Part("photo_ids") photoIds: List<Int>? = null,  // 替换已有图片时传
    @Part("label") label: RequestBody? = null
): List<GuziImage>
```

### 11.4 Coil 图片 URL 拼接

```kotlin
// Coil ImageLoader 配置
@Singleton
@Provides
fun provideImageLoader(
    @ApplicationContext context: Context,
    tokenManager: TokenManager
): ImageLoader {
    return ImageLoader.Builder(context)
        .diskCache { DiskCache.Builder().directory(File(context.cacheDir, "coil_cache")).build() }
        .components {
            // 自定义 Mapper：相对路径 → 完整 URL
            add(FullUrlMapper { baseUrl -> "$baseUrl$it" })
        }
        .build()
}

// 使用：后端返回相对路径 "/media/goods/main/xxx.jpg"
// Coil 自动拼接为 "http://192.168.1.100:8000/media/goods/main/xxx.jpg"
AsyncImage(
    model = goods.mainPhoto,  // 相对路径
    contentDescription = goods.name,
    modifier = Modifier.fillMaxSize()
)
```

---

## 十二、筛选状态管理

### 12.1 筛选参数枚举

```kotlin
data class GoodsFilterState(
    val ip: IP? = null,
    val character: Character? = null,
    val characterIds: Set<Int> = emptySet(),   // 多选角色 → characters__in
    val category: Category? = null,
    val theme: Theme? = null,
    val statuses: Set<GoodsStatus> = setOf(GoodsStatus.IN_CABINET), // 多选状态
    val isOfficial: Boolean? = null,           // null=全部, true=官谷, false=同人
    val location: StorageNode? = null,
    val search: String = "",
    val groupBy: GroupByOption? = null,        // IP | CHARACTER | CATEGORY | THEME
    val viewMode: ViewMode = ViewMode.STANDARD  // STANDARD | SIMILAR_RANDOM
)

enum class GroupByOption { IP, CHARACTER, CATEGORY, THEME }
enum class ViewMode { STANDARD, SIMILAR_RANDOM }
```

### 12.2 筛选状态 → API 参数映射

```kotlin
fun GoodsFilterState.toSearchParams(page: Int = 1): GoodsSearchParams {
    return GoodsSearchParams(
        ip = ip?.id,
        character = character?.id,
        charactersIn = characterIds.takeIf { it.isNotEmpty() }?.joinToString(","),
        category = category?.id,
        theme = theme?.id,
        status = statuses.singleOrNull()?.name?.lowercase(),  // 单选时用 status
        statusIn = statuses.takeIf { it.size > 1 }?.joinToString(",") { it.name.lowercase() },  // 多选时用 status__in
        isOfficial = isOfficial,
        location = location?.id,
        search = search.ifBlank { null },
        page = page,
        groupBy = groupBy?.name?.lowercase()
    )
}
```

### 12.3 筛选联动规则

| 操作              | 联动行为                      |
| --------------- | ------------------------- |
| 切换 IP           | 清空角色选择 + 重新加载该 IP 下的角色列表  |
| 切换 group_by    | switch to STANDARD view   |
| 切换 viewMode    | 切换到 SIMILAR_RANDOM 时清空 group_by |
| 任意筛选条件变更        | 重置到 page=1                |
| 搜索输入（300ms 防抖） | 重置到 page=1                |

### 12.4 FilterSheet 组件设计

```
FilterSheet (ModalBottomSheet)
├── IP 选择器 (ExposedDropdownMenu)
├── 角色多选 (FlowRow + FilterChip, 按选中 IP 过滤)
├── 品类树选择器 (TreePickerDialog → 层级导航)
├── 主题选择器 (ExposedDropdownMenu)
├── 状态多选 (FlowRow + FilterChip: 在仓/在外/已出)
├── 官非开关 (Switch: 全部 ↔ 官谷)
├── 位置树选择器 (TreePickerDialog)
├── 分组方式 (SegmentedButton: 不分组/IP/角色/品类/主题)
├── 展示模式 (Switch: 标准 ↔ 随机相似)
├── 重置按钮 (TextButton)
└── 应用按钮 (Button → dismiss + 触发搜索)
```

### 12.5 ViewMode 切换影响

| ViewMode        | API 端点                          | 说明                    |
| --------------- | -------------------------------- | --------------------- |
| `STANDARD`      | `GET /api/goods/`                | 标准分页列表，支持 group_by 分组 |
| `SIMILAR_RANDOM` | `GET /api/goods/similar-random/` | 相似度随机推荐，不分页，不支持分组    |

---

## 十三、Gradle 核心依赖清单

```kotlin
// build.gradle.kts (app)
dependencies {
    // Compose BOM — 统一管理版本
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)

    // Compose UI
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Lifecycle + ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Hilt (DI)
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")

    // Coil (图片加载)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Room (离线缓存)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore (偏好设置 + Token)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Vico (图表)
    implementation("com.patrykandpatrick.vico:compose-m3:1.14.0")

    // CameraX (拍照)
    implementation("androidx.camera:camera-camera2:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    implementation("androidx.camera:camera-view:1.3.3")

    // 图片裁剪库 (ucrop 或自定义 Compose Canvas)
    // implementation("com.github.yalantis:ucrop:2.2.8")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("io.mockk:mockk:1.13.11")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

### 关键版本要求

| 依赖              | 最低版本  | 说明                 |
| --------------- | ----- | ------------------ |
| `minSdk`        | 26    | Android 8.0+       |
| `targetSdk`     | 34    | Android 14         |
| `compileSdk`    | 34    |                    |
| Kotlin          | 2.0+  | 需 KSP 替代 kapt（性能更好） |
| AGP             | 8.4+  | Gradle 8.7+        |

---

## 十四、测试策略

### 14.1 测试金字塔

| 层级        | 工具                     | 覆盖内容                           | 目标覆盖率 |
| --------- | ---------------------- | ------------------------------ | ----- |
| 单元测试      | JUnit4 + MockK         | ViewModel 逻辑、Repository 数据转换、工具函数 | ≥ 70% |
| 集成测试      | OkHttp MockWebServer   | API 请求/响应解析、拦截器逻辑、Token 刷新      | 核心全量  |
| UI 组件测试   | Compose UI Test        | 关键组件（GoodsCard、FilterSheet）渲染和交互  | 关键路径  |
| E2E 测试    | (可选)                   | 完整业务流程（注册→创建谷子→展柜管理）            | P1     |

### 14.2 单元测试示例

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class GoodsViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val goodsRepo = mockk<GoodsRepository>()
    private val dictRepo = mockk<DictRepository>()
    private lateinit var viewModel: GoodsViewModel

    @Before
    fun setup() {
        viewModel = GoodsViewModel(goodsRepo, dictRepo)
    }

    @Test
    fun `loadList emits Success with mock data`() = runTest {
        val mockResponse = PaginatedResponse(count = 1, page = 1, pageSize = 18, results = listOf(...))
        coEvery { goodsRepo.getList(any()) } returns mockResponse

        viewModel.loadList()
        advanceUntilIdle()

        val state = viewModel.listState.value
        assertTrue(state is UiState.Success)
        assertEquals(1, (state as UiState.Success).data.results.size)
    }

    @Test
    fun `filters reset to page 1 on status change`() = runTest {
        viewModel.updateFilter { it.copy(statuses = setOf(GoodsStatus.SOLD)) }

        assertEquals(1, viewModel.filters.value.page)
    }
}
```

### 14.3 API 集成测试

```kotlin
class GoodsApiTest {
    private lateinit var mockServer: MockWebServer
    private lateinit var api: GoodsApi

    @Before
    fun setup() {
        mockServer = MockWebServer()
        api = Retrofit.Builder()
            .baseUrl(mockServer.url("/"))
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GoodsApi::class.java)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun `getGoodsList returns paginated response`() = runTest {
        mockServer.enqueue(MockResponse()
            .setBody("""{"count":42,"page":1,"page_size":18,"results":[]}""")
            .setResponseCode(200))

        val result = api.getGoodsList(page = 1, pageSize = 18)
        assertEquals(42, result.count)
    }
}
```

---

## 十五、关键场景流程

### 15.1 创建/编辑谷子流程

```
GoodsFormScreen
├── 填写基础信息
│   ├── 名称
│   ├── IP → 搜索+选择（弹窗Picker）
│   ├── 角色 → 按IP过滤+多选
│   ├── 品类 → 树形选择器
│   ├── 主题 → 下拉选择
│   ├── 位置 → 树形选择器
│   └── 状态、数量、价格、日期、官非开关
├── 图片管理
│   ├── 主图 → 拍照/相册 → 裁剪(可选) → 上传
│   └── 附加图 → 多选 → 上传 + 标签编辑
├── 备注
│
└── 保存
    ├── 草稿 → POST status=draft
    └── 发布 → POST → 409(重复检测)
        ├── 合并 → PUT (quantity +=)
        └── 强制新建 → POST merge_strategy=new
```

### 15.2 离线缓存策略

| 数据             | 策略        | 说明                |
| -------------- | --------- | ----------------- |
| Token          | DataStore | 持久化，App 重启保留      |
| 谷子列表           | Room      | 缓存最近 3 页，下拉刷新时更新  |
| IP/角色/品类/主题/位置 | Room      | 首次加载后缓存，每次打开时后台刷新 |
| 图片             | Coil 磁盘缓存 | 自动 LRU 缓存，无需额外处理  |
| 用户设置           | DataStore | 服务器地址、主题偏好等       |

---

## 十六、风险与注意事项

1. **后端地址可配置**：Android App 需要提供设置界面让用户输入后端服务器地址（IP + 端口），因为开发环境可能用 `192.168.x.x`，生产环境用域名。

2. **图片 URL 拼接**：后端返回的图片字段是相对路径（如 `goods/main/xxx.jpg`），Android 端需要根据 `MEDIA_URL` 配置拼接完整 URL。建议后端在 settings 中设置 `MEDIA_URL = '/media/'`，Android 拼接为 `${baseUrl}/media/goods/main/xxx.jpg`。

3. **UUID 主键处理**：`Goods` 和 `Showcase` 使用 UUID 作为主键，API 请求时以字符串形式传递。

4. **SQLite 兼容性**：后端部分统计数据使用了 TruncDate 等数据库函数，这些在前端不感知。Android 端直接请求 API 获取统计数据即可。

5. **权限要求**：相机权限（`CAMERA`）、存储权限（`READ_MEDIA_IMAGES` Android 13+），使用 `rememberLauncherForActivityResult` 运行时申请。

6. **网络状态监听**：使用 `ConnectivityManager` 监听网络变化，离线时提示用户并使用缓存数据。

7. **Token 过期处理**：后端 JWT Token 包含 `expires_in`，Android 端可以在过期前主动刷新，或在收到 401 时静默跳转登录页。

---

## 十七、交付物清单

| 交付物                        | 说明                       |
| -------------------------- | ------------------------ |
| 完整 Android 项目源码            | Kotlin + Jetpack Compose |
| `gradle.properties` + 密钥配置 | 签名配置                     |
| 网络层 API 接口定义               | Retrofit 接口 + 数据类        |
| APK（debug / release）       | 可安装包                     |
| 服务器地址配置文档                  | 用户需知                     |
| 截图 / 录屏                    | 功能演示                     |

---

# 
