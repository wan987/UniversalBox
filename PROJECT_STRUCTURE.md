# UniversalBox - Clean Architecture 项目结构

## 📁 完整目录结构

```
app/src/main/java/com/universalbox/app/
│
├── 📦 domain/                          # Domain Layer (业务逻辑层)
│   ├── model/                          # 领域模型
│   │   ├── Resource.kt                 # 资源领域模型
│   │   ├── ResourceType.kt             # 资源类型 (密封类)
│   │   ├── ResourceCategory.kt         # 资源分类 (枚举)
│   │   └── Schedule.kt                 # 时间表领域模型
│   │
│   ├── repository/                     # 仓库接口 (依赖倒置)
│   │   ├── ResourceRepository.kt       # 资源仓库接口
│   │   └── ScheduleRepository.kt       # 时间表仓库接口
│   │
│   └── usecase/                        # 用例 (业务场景)
│       ├── GetCurrentRecommendationsUseCase.kt  # 获取当前推荐
│       ├── LaunchResourceUseCase.kt             # 启动资源
│       ├── GetResourcesByCategoryUseCase.kt     # 按分类获取资源
│       ├── SearchResourcesUseCase.kt            # 搜索资源
│       └── ManageSchedulesUseCase.kt            # 管理时间表
│
├── 📦 data/                            # Data Layer (数据层)
│   ├── model/                          # 数据模型 (Room Entity)
│   │   ├── FavoriteItem.kt             # 收藏项 Entity
│   │   └── TimeSchedule.kt             # 时间表 Entity
│   │
│   ├── local/                          # 本地数据源
│   │   ├── AppDatabase.kt              # Room 数据库
│   │   ├── FavoriteDao.kt              # 收藏项 DAO
│   │   └── TimeScheduleDao.kt          # 时间表 DAO
│   │
│   ├── repository/                     # 仓库实现
│   │   ├── ResourceRepositoryImpl.kt   # 资源仓库实现
│   │   └── ScheduleRepositoryImpl.kt   # 时间表仓库实现
│   │
│   └── mapper/                         # 数据映射器
│       ├── ResourceMapper.kt           # FavoriteItem ↔ Resource
│       └── ScheduleMapper.kt           # TimeSchedule ↔ Schedule
│
├── 📦 ui/                              # Presentation Layer (表现层)
│   ├── viewmodel/                      # ViewModel
│   │   └── DashboardViewModel.kt       # Dashboard ViewModel (示例)
│   │
│   ├── screens/                        # Compose 屏幕
│   │   ├── dashboard/
│   │   │   └── DashboardScreen.kt      # 首页
│   │   ├── home/
│   │   │   └── HomeScreen.kt           # 收藏列表
│   │   └── detail/
│   │       └── DetailScreen.kt         # 详情页
│   │
│   └── components/                     # 可复用组件
│       └── FavoriteCard.kt
│
├── 📦 utils/                           # 工具类
│   ├── ResourceLauncher.kt             # 资源启动器
│   └── UrlParser.kt                    # URL 解析器
│
├── 📦 navigation/                      # 导航
│   └── NavigationRoutes.kt
│
├── BaseApplication.kt                  # Application (依赖注入中心)
└── MainActivity.kt                     # 主 Activity
```

---

## 🎯 核心文件说明

### Domain Layer (业务核心)

#### ResourceType.kt - 资源类型定义
```kotlin
sealed class ResourceType {
    data object WebLink : ResourceType()                     // 网页链接
    data class AppLaunch(val packageName: String)           // App 跳转
    data class DeepLink(val packageName: String, uri: String) // Deep Link
    data object TaskMemo : ResourceType()                   // 任务/备忘录
}
```

**用途：** 类型安全的资源类型表示，支持携带参数

#### ResourceCategory.kt - 资源分类
```kotlin
enum class ResourceCategory(val displayName: String, val icon: String, val color: Long) {
    STUDY("学习", "school", 0xFF4CAF50),
    WORK("工作", "work", 0xFF2196F3),
    ENTERTAINMENT("娱乐", "sports_esports", 0xFFFF9800),
    TOOL("工具", "build", 0xFF9C27B0),
    LIFE("生活", "home", 0xFFF44336),
    OTHER("其他", "more_horiz", 0xFF757575)
}
```

**用途：** 预定义的资源分类，每个分类带有显示名称、图标、颜色

#### Resource.kt - 领域资源模型
```kotlin
data class Resource(
    val id: Long,
    val title: String,
    val url: String,
    val type: ResourceType,
    val category: ResourceCategory,
    val usageWeight: Int,
    ...
)
```

**用途：** Domain 层使用的纯 Kotlin 资源模型，独立于数据库实现

#### Repository Interfaces
- **ResourceRepository.kt**: 定义资源仓库接口（CRUD + 查询）
- **ScheduleRepository.kt**: 定义时间表仓库接口

**用途：** 依赖倒置原则 - Domain 层定义接口，Data 层实现

#### Use Cases
每个 Use Case 封装一个完整的业务场景：

- **GetCurrentRecommendationsUseCase**: 根据当前时间推荐资源（Dynamic Zone 核心逻辑）
- **LaunchResourceUseCase**: 启动资源并记录使用权重
- **GetResourcesByCategoryUseCase**: 按分类获取资源（Library Zone 核心逻辑）
- **SearchResourcesUseCase**: 搜索资源
- **ManageSchedulesUseCase**: 管理时间表 CRUD

**用途：** 封装可重用的业务逻辑，ViewModel 只调用 Use Cases

---

### Data Layer (数据访问)

#### FavoriteItem.kt - Room Entity
```kotlin
@Entity(tableName = "favorites")
data class FavoriteItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String,              // 存储为字符串类型代码
    val packageName: String? = null,
    val category: String,          // 存储为中文字符串
    ...
)
```

**用途：** Room 数据库实体，使用基本类型存储

#### Mappers
- **ResourceMapper.kt**: FavoriteItem ↔ Resource 转换
- **ScheduleMapper.kt**: TimeSchedule ↔ Schedule 转换

**用途：** 在 Data Layer 和 Domain Layer 之间转换数据

#### Repository Implementations
- **ResourceRepositoryImpl.kt**: 实现 ResourceRepository 接口
- **ScheduleRepositoryImpl.kt**: 实现 ScheduleRepository 接口

**用途：** 实现 Domain 层定义的接口，对接 Room 数据库

---

### Presentation Layer (UI)

#### DashboardViewModel.kt (示例)
```kotlin
class DashboardViewModel(
    private val getCurrentRecommendations: GetCurrentRecommendationsUseCase,
    private val getResourcesByCategory: GetResourcesByCategoryUseCase,
    ...
) : ViewModel() {
    val recommendationState: StateFlow<RecommendationState>
    val libraryState: StateFlow<LibraryState>
}
```

**用途：** 展示如何在新架构下使用 Use Cases

---

### Utils

#### ResourceLauncher.kt - 资源启动器
```kotlin
object ResourceLauncher {
    fun launch(context: Context, resource: Resource): LaunchResult
    fun isAppInstalled(context: Context, packageName: String): Boolean
    fun openAppStore(context: Context, packageName: String)
}
```

**功能：**
- 网页：Chrome Custom Tabs
- App：启动第三方应用
- Deep Link：跳转到 App 特定页面
- 错误处理：优雅地处理未安装应用等情况

---

## 🔄 数据流示例

### 场景：用户点击资源卡片

```
1. User clicks on Resource Card
   ↓
2. DashboardScreen calls viewModel.onResourceClick(resourceId)
   ↓
3. DashboardViewModel calls launchResourceUseCase(resourceId)
   ↓
4. LaunchResourceUseCase:
   - 从 ResourceRepository 获取 Resource
   - 调用 resourceRepository.incrementUsageWeight(resourceId)
   - 返回 LaunchResult.Success(resource)
   ↓
5. ViewModel 回调 onLaunchSuccess(resource)
   ↓
6. DashboardScreen 调用 ResourceLauncher.launch(context, resource)
   ↓
7. ResourceLauncher 根据 ResourceType 执行：
   - WebLink → Chrome Custom Tabs
   - AppLaunch → startActivity
   - DeepLink → Intent with URI
   - TaskMemo → 不跳转，仅展示
   ↓
8. 资源成功打开 🎉
```

---

## 📊 依赖关系图

```
┌─────────────────────────────────────────┐
│         BaseApplication                 │
│  (依赖注入中心，创建所有 Use Cases)      │
└──────────────┬──────────────────────────┘
               │
               ├─→ ResourceRepositoryImpl ──→ FavoriteDao
               ├─→ ScheduleRepositoryImpl ──→ TimeScheduleDao
               │
               ├─→ GetCurrentRecommendationsUseCase
               ├─→ LaunchResourceUseCase
               ├─→ GetResourcesByCategoryUseCase
               └─→ SearchResourcesUseCase
                       ↑
                       │ injected into
                       │
               ┌───────┴───────┐
               │  ViewModel    │
               └───────┬───────┘
                       │
                       ↓
               ┌───────────────┐
               │  Compose UI   │
               └───────────────┘
```

---

## ✅ 关键设计原则

### 1. 依赖方向
```
Presentation → Domain → Data
(单向依赖，永远不能反向)
```

### 2. 不同层使用不同模型
- **Data Layer**: FavoriteItem (Room Entity)
- **Domain Layer**: Resource (Pure Kotlin)
- **Presentation Layer**: 直接使用 Resource

### 3. 接口在 Domain，实现在 Data
```kotlin
// domain/repository/ResourceRepository.kt (接口)
interface ResourceRepository { ... }

// data/repository/ResourceRepositoryImpl.kt (实现)
class ResourceRepositoryImpl : ResourceRepository { ... }
```

### 4. ViewModel 只依赖 Use Cases
```kotlin
// ✅ Good
class MyViewModel(
    private val getCurrentRecommendations: GetCurrentRecommendationsUseCase
)

// ❌ Bad
class MyViewModel(
    private val dao: FavoriteDao
)
```

---

## 🎯 快速定位文件

### 要添加新功能？
1. **定义 Domain Model**: `domain/model/`
2. **创建 Use Case**: `domain/usecase/`
3. **在 ViewModel 中使用**: `ui/viewmodel/`
4. **在 UI 中展示**: `ui/screens/`

### 要修改数据库？
1. **更新 Entity**: `data/model/FavoriteItem.kt`
2. **更新 DAO**: `data/local/FavoriteDao.kt`
3. **更新 Mapper**: `data/mapper/ResourceMapper.kt`
4. **创建 Migration**: `data/local/AppDatabase.kt`

### 要添加新的资源类型？
1. **扩展 ResourceType**: `domain/model/ResourceType.kt`
2. **更新 ResourceLauncher**: `utils/ResourceLauncher.kt`
3. **更新 Mapper**: `data/mapper/ResourceMapper.kt`

---

## 📚 相关文档

- **ARCHITECTURE.md**: 详细的架构设计说明和最佳实践
- **MIGRATION_GUIDE.md**: 从旧架构迁移到新架构的步骤
- **README.md**: 项目概述和快速开始

---

**Created with ❤️ for UniversalBox - Personal Digital Life Router**
