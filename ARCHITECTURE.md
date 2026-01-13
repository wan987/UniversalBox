# UniversalBox Architecture Guide
# "个人数字生活路由" 架构设计文档

## 📋 目录
1. [架构概览](#架构概览)
2. [核心设计决策](#核心设计决策)
3. [层级说明](#层级说明)
4. [数据流向](#数据流向)
5. [使用指南](#使用指南)
6. [最佳实践](#最佳实践)

---

## 🏗️ 架构概览

UniversalBox 采用 **Clean Architecture + MVVM** 架构模式，分为三个核心层：

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│   (UI / Compose / ViewModel)            │
└──────────────┬──────────────────────────┘
               │ depends on
┌──────────────▼──────────────────────────┐
│          Domain Layer                   │
│  (Use Cases / Models / Repository Interface) │
└──────────────┬──────────────────────────┘
               │ implements
┌──────────────▼──────────────────────────┐
│           Data Layer                    │
│  (Repository Impl / DAO / Room)         │
└─────────────────────────────────────────┘
```

### 核心原则
- **依赖倒置 (Dependency Inversion)**: Domain 层定义接口，Data 层实现
- **单一职责 (Single Responsibility)**: 每个类/模块只负责一件事
- **开闭原则 (Open/Closed)**: 对扩展开放，对修改关闭

---

## 🎯 核心设计决策

### 1️⃣ 为什么使用密封类 (Sealed Class) 表示 ResourceType？

```kotlin
sealed class ResourceType {
    data object WebLink : ResourceType()
    data class AppLaunch(val packageName: String) : ResourceType()
    data class DeepLink(val packageName: String, val uri: String) : ResourceType()
    data object TaskMemo : ResourceType()
}
```

**理由：**
- ✅ **类型安全**: 编译时检查，避免字符串拼写错误
- ✅ **when 表达式完整性**: 编译器强制处理所有分支
- ✅ **携带参数**: AppLaunch 可以直接携带 packageName
- ✅ **可扩展**: 新增类型无需修改现有代码

**对比字符串常量：**
```kotlin
// ❌ 字符串方案：容易出错，无类型安全
when (resource.type) {
    "APP_LAUNCJ" -> // 拼写错误，运行时才发现
    ...
}

// ✅ 密封类方案：编译时保证正确
when (resource.type) {
    is ResourceType.AppLaunch -> // 有智能提示，类型安全
    ...
}
```

### 2️⃣ 为什么使用枚举 (Enum) 表示 ResourceCategory？

```kotlin
enum class ResourceCategory(
    val displayName: String,
    val icon: String,
    val color: Long
) {
    STUDY("学习", "school", 0xFF4CAF50),
    WORK("工作", "work", 0xFF2196F3),
    ...
}
```

**理由：**
- ✅ **固定集合**: 分类是预定义的，不需要动态扩展
- ✅ **遍历方便**: `ResourceCategory.values()` 获取所有分类
- ✅ **性能更好**: 枚举比密封类更轻量
- ✅ **携带元数据**: 每个枚举可以携带显示名称、图标、颜色等

### 3️⃣ 为什么需要 Domain Layer 和 Data Layer 分离？

**传统方案（不推荐）：**
```kotlin
// ViewModel 直接使用 Room Entity
class MyViewModel(private val dao: FavoriteDao) {
    val favorites = dao.getAllFavorites()  // ❌ ViewModel 依赖具体实现
}
```

**Clean Architecture 方案（推荐）：**
```kotlin
// ViewModel 依赖抽象接口
class MyViewModel(private val repository: ResourceRepository) {
    val resources = repository.getAllResources()  // ✅ 依赖抽象
}
```

**好处：**
- ✅ **可测试性**: 可以轻松 Mock Repository 进行单元测试
- ✅ **独立性**: Domain 层可以独立于任何框架（Room/Retrofit 等）
- ✅ **灵活性**: 可以随时更换数据源（从 Room 换到 DataStore）而不影响业务逻辑

### 4️⃣ 为什么使用 Chrome Custom Tabs 而非系统浏览器？

```kotlin
// 使用 Chrome Custom Tabs
val customTabsIntent = CustomTabsIntent.Builder()
    .setShowTitle(true)
    .setUrlBarHidingEnabled(true)
    .build()
customTabsIntent.launchUrl(context, uri)
```

**好处：**
- ✅ **更好的用户体验**: 页面在 App 内打开，无需切换应用
- ✅ **保持上下文**: 用户可以快速返回 App
- ✅ **预加载**: 支持页面预加载，速度更快
- ✅ **自定义样式**: 可以设置工具栏颜色、动画等

---

## 📦 层级说明

### Domain Layer (域层)

**位置**: `com.universalbox.app.domain`

**职责**: 定义业务逻辑和规则，独立于任何框架

**包含：**

#### 1. Models (领域模型)
```kotlin
// domain/model/Resource.kt
data class Resource(
    val id: Long,
    val title: String,
    val type: ResourceType,
    val category: ResourceCategory,
    ...
)
```

#### 2. Repository Interfaces (仓库接口)
```kotlin
// domain/repository/ResourceRepository.kt
interface ResourceRepository {
    fun getAllResources(): Flow<List<Resource>>
    suspend fun insertResource(resource: Resource): Long
    ...
}
```

#### 3. Use Cases (用例)
```kotlin
// domain/usecase/GetCurrentRecommendationsUseCase.kt
class GetCurrentRecommendationsUseCase(
    private val scheduleRepository: ScheduleRepository,
    private val resourceRepository: ResourceRepository
) {
    operator fun invoke(): Flow<RecommendationResult> {
        // 封装完整的业务逻辑
    }
}
```

**设计理念：**
- Use Case 封装一个完整的业务场景
- 一个 Use Case 只做一件事
- 可以组合多个 Repository

### Data Layer (数据层)

**位置**: `com.universalbox.app.data`

**职责**: 实现数据访问，对接外部数据源 (Room/Network)

**包含：**

#### 1. Room Entities (数据库实体)
```kotlin
// data/model/FavoriteItem.kt
@Entity(tableName = "favorites")
data class FavoriteItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String,  // 存储为字符串类型代码
    ...
)
```

#### 2. DAO (数据访问对象)
```kotlin
// data/local/FavoriteDao.kt
@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteItem>>
    ...
}
```

#### 3. Repository Implementations (仓库实现)
```kotlin
// data/repository/ResourceRepositoryImpl.kt
class ResourceRepositoryImpl(
    private val favoriteDao: FavoriteDao
) : ResourceRepository {
    override fun getAllResources(): Flow<List<Resource>> {
        return favoriteDao.getAllFavorites().map { items ->
            items.map { it.toDomain() }  // 转换为 Domain Model
        }
    }
}
```

#### 4. Mappers (映射器)
```kotlin
// data/mapper/ResourceMapper.kt
fun FavoriteItem.toDomain(): Resource { ... }
fun Resource.toData(): FavoriteItem { ... }
```

**关键点：**
- Room Entity 存储为字符串/整数等基本类型
- 通过 Mapper 转换为 Domain Model
- Domain Layer 永远不知道 Room 的存在

### Presentation Layer (表现层)

**位置**: `com.universalbox.app.ui`

**职责**: UI 展示和用户交互

**包含：**

#### 1. ViewModel
```kotlin
// ui/viewmodel/DashboardViewModel.kt
class DashboardViewModel(
    private val getCurrentRecommendations: GetCurrentRecommendationsUseCase,
    private val launchResource: LaunchResourceUseCase,
    ...
) : ViewModel() {
    // 只依赖 Use Cases，不直接依赖 Repository
}
```

#### 2. Compose UI
```kotlin
// ui/screens/DashboardScreen.kt
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.recommendationState.collectAsState()
    // UI 逻辑
}
```

---

## 🔄 数据流向

### 读取数据流

```
User Action (点击按钮)
    ↓
Compose UI (收集 StateFlow)
    ↓
ViewModel (调用 Use Case)
    ↓
Use Case (组合业务逻辑)
    ↓
Repository Interface (Domain 层接口)
    ↓
Repository Impl (Data 层实现)
    ↓
DAO (Room 查询)
    ↓
Mapper (Entity → Domain Model)
    ↓
Flow<List<Resource>> (回传到 ViewModel)
    ↓
StateFlow<State> (UI 自动更新)
```

### 写入数据流

```
User Action (添加资源)
    ↓
ViewModel.addResource(resource)
    ↓
Use Case (验证业务规则)
    ↓
Repository.insertResource(resource)
    ↓
Mapper (Domain Model → Entity)
    ↓
DAO.insert(favoriteItem)
    ↓
Room 数据库
    ↓
Flow 自动发射新数据
    ↓
UI 自动刷新
```

---

## 📖 使用指南

### 如何添加新功能：添加"分享到朋友圈"

#### Step 1: 扩展 ResourceType (如果需要)
```kotlin
// domain/model/ResourceType.kt
sealed class ResourceType {
    ...
    data class ShareToMoments(val imageUrl: String) : ResourceType()
}
```

#### Step 2: 创建 Use Case
```kotlin
// domain/usecase/ShareToMomentsUseCase.kt
class ShareToMomentsUseCase(
    private val resourceRepository: ResourceRepository
) {
    suspend operator fun invoke(resourceId: Long): ShareResult {
        val resource = resourceRepository.getResourceById(resourceId)
        // 业务逻辑：生成分享内容
        return ShareResult.Success(shareUrl)
    }
}
```

#### Step 3: 在 ViewModel 中使用
```kotlin
// ui/viewmodel/DetailViewModel.kt
class DetailViewModel(
    private val shareToMoments: ShareToMomentsUseCase
) : ViewModel() {
    fun onShareClick(resourceId: Long) {
        viewModelScope.launch {
            val result = shareToMoments(resourceId)
            // 处理结果
        }
    }
}
```

#### Step 4: 在 UI 中调用
```kotlin
// ui/screens/DetailScreen.kt
Button(onClick = { viewModel.onShareClick(resourceId) }) {
    Text("分享")
}
```

### 如何切换数据源：从 Room 到 DataStore

由于使用了依赖倒置，切换数据源非常简单：

```kotlin
// 1. 创建新的 Repository 实现
class ResourceDataStoreRepository(
    private val dataStore: DataStore<Preferences>
) : ResourceRepository {
    override fun getAllResources(): Flow<List<Resource>> {
        // 使用 DataStore 实现
    }
}

// 2. 在依赖注入时替换实现（不影响任何其他代码）
val resourceRepository: ResourceRepository = ResourceDataStoreRepository(dataStore)
```

---

## ✅ 最佳实践

### 1️⃣ ViewModel 不应该知道 Room

❌ **错误示例：**
```kotlin
class MyViewModel(private val dao: FavoriteDao) {
    val favorites = dao.getAllFavorites()  // ViewModel 依赖 Room
}
```

✅ **正确示例：**
```kotlin
class MyViewModel(private val getResources: GetAllResourcesUseCase) {
    val resources = getResources()  // 依赖抽象用例
}
```

### 2️⃣ Use Case 应该是可重用的

❌ **错误示例：**
```kotlin
class GetHomeScreenDataUseCase { ... }  // 太具体，只能用于首页
```

✅ **正确示例：**
```kotlin
class GetResourcesByCategoryUseCase { ... }  // 通用，多个页面可用
```

### 3️⃣ Domain Model 不应该依赖 Android 框架

❌ **错误示例：**
```kotlin
data class Resource(
    val bitmap: Bitmap  // ❌ Bitmap 是 Android 框架类
)
```

✅ **正确示例：**
```kotlin
data class Resource(
    val imageUrl: String  // ✅ 纯 Kotlin，可以在任何地方运行
)
```

### 4️⃣ 使用 Mapper 在层之间转换

```kotlin
// ✅ 每一层都有自己的 Model
FavoriteItem (Room Entity)  →  Resource (Domain Model)
TimeSchedule (Room Entity)  →  Schedule (Domain Model)
```

### 5️⃣ 错误处理使用密封类

```kotlin
sealed class LaunchResult {
    data object Success : LaunchResult()
    data class AppNotInstalled(val packageName: String) : LaunchResult()
    data class Failure(val message: String) : LaunchResult()
}

// 使用时强制处理所有情况
when (result) {
    is LaunchResult.Success -> { ... }
    is LaunchResult.AppNotInstalled -> { ... }
    is LaunchResult.Failure -> { ... }
}
```

---

## 🎓 进阶话题

### 依赖注入 (未来可选)

如果项目变大，建议使用 Hilt/Koin：

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideResourceRepository(
        dao: FavoriteDao
    ): ResourceRepository {
        return ResourceRepositoryImpl(dao)
    }
}
```

### 测试策略

Clean Architecture 的一大优势是易于测试：

```kotlin
// Domain Layer 测试（不需要 Android 环境）
class GetCurrentRecommendationsUseCaseTest {
    @Test
    fun `should return study resources during study time`() {
        // 使用 Mock Repository，纯 Kotlin 单元测试
        val mockRepo = MockResourceRepository()
        val useCase = GetCurrentRecommendationsUseCase(mockRepo)
        
        val result = useCase()
        assertEquals(ResourceCategory.STUDY, result.currentCategory)
    }
}
```

---

## 📚 总结

**核心思想：**
1. **分层清晰**: Domain / Data / Presentation 各司其职
2. **依赖倒置**: 高层模块不依赖低层模块，都依赖抽象
3. **类型安全**: 使用密封类和枚举替代字符串常量
4. **可测试性**: Domain 层纯 Kotlin，易于单元测试
5. **可维护性**: 修改数据库不影响业务逻辑，修改 UI 不影响数据层

**当你要做修改时，问自己：**
- 我在修改哪一层？
- 这个改动会影响其他层吗？
- 如果影响，是否违反了依赖规则？

**记住：依赖只能单向流动**
```
Presentation → Domain → Data
(UI 可以依赖 Use Cases，但 Use Cases 不能依赖 UI)
```

---

**Made with ❤️ for UniversalBox - Personal Digital Life Router**
