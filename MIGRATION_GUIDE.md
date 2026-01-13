# UniversalBox 架构迁移指南
# 从旧架构迁移到 Clean Architecture

## 📋 迁移概览

本指南帮助你将现有代码从旧架构平滑迁移到新的 Clean Architecture。

**迁移策略：渐进式重构**
- ✅ 新旧架构可以共存
- ✅ 逐步迁移，不影响现有功能
- ✅ 先迁移数据层，再迁移业务层，最后迁移 UI 层

---

## 🔄 架构对比

### 旧架构
```
ViewModel → Repository (直接使用 Room Entity)
    ↓
FavoriteDao → FavoriteItem (Room Entity)
```

### 新架构
```
ViewModel → Use Case (业务逻辑)
    ↓
Repository Interface (Domain 层)
    ↓
Repository Impl (Data 层) → Mapper → DAO
    ↓
Room Entity
```

---

## 📦 需要更新的文件

### 1️⃣ build.gradle.kts (添加 Chrome Custom Tabs)

在 `app/build.gradle.kts` 的 `dependencies` 块中添加：

```kotlin
dependencies {
    // 现有依赖...
    
    // Chrome Custom Tabs - 用于优雅地打开网页
    implementation("androidx.browser:browser:1.7.0")
}
```

### 2️⃣ BaseApplication.kt - 更新依赖注入

**现状：**
```kotlin
class BaseApplication : Application() {
    private val database by lazy { AppDatabase.getInstance(this) }
    val favoriteRepository by lazy { FavoriteRepository(database.favoriteDao()) }
    val timeScheduleRepository by lazy { TimeScheduleRepository(database.timeScheduleDao()) }
}
```

**需要更新为：**
```kotlin
package com.universalbox.app

import android.app.Application
import com.universalbox.app.data.local.AppDatabase
import com.universalbox.app.data.repository.ResourceRepositoryImpl
import com.universalbox.app.data.repository.ScheduleRepositoryImpl
import com.universalbox.app.domain.repository.ResourceRepository
import com.universalbox.app.domain.repository.ScheduleRepository
import com.universalbox.app.domain.usecase.*

class BaseApplication : Application() {
    // 数据库
    private val database by lazy { AppDatabase.getInstance(this) }
    
    // Repositories (Data Layer 实现)
    val resourceRepository: ResourceRepository by lazy {
        ResourceRepositoryImpl(database.favoriteDao())
    }
    
    val scheduleRepository: ScheduleRepository by lazy {
        ScheduleRepositoryImpl(database.timeScheduleDao())
    }
    
    // Use Cases (Domain Layer)
    val getCurrentRecommendations by lazy {
        GetCurrentRecommendationsUseCase(scheduleRepository, resourceRepository)
    }
    
    val launchResource by lazy {
        LaunchResourceUseCase(resourceRepository)
    }
    
    val getResourcesByCategory by lazy {
        GetResourcesByCategoryUseCase(resourceRepository)
    }
    
    val searchResources by lazy {
        SearchResourcesUseCase(resourceRepository)
    }
    
    val manageSchedules by lazy {
        ManageSchedulesUseCase(scheduleRepository)
    }
    
    override fun onCreate() {
        super.onCreate()
        // 初始化默认时间表
        // Note: 这应该在后台线程执行
        // 可以使用 WorkManager 或在首次启动时执行
    }
}
```

### 3️⃣ FavoriteItem.kt - 兼容新旧架构

当前的 `FavoriteItem.kt` 可以保持不变（作为 Room Entity），但需要确保字段映射正确：

```kotlin
// data/model/FavoriteItem.kt
@Entity(tableName = "favorites")
data class FavoriteItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val description: String = "",
    val type: String = "WEB_LINK",  // ✅ 使用新的类型代码
    val imageUrl: String = "",
    val siteName: String = "",
    val tags: String = "",
    val packageName: String? = null,
    val category: String = "工具",
    val usageWeight: Int = 0,
    val createTime: Long = System.currentTimeMillis()
) {
    // 保持现有的工具方法
    fun getTagList(): List<String> = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }
}
```

**注意：** 旧数据的 `type` 字段可能是 "URL" / "APP" / "TASK"，新架构使用 "WEB_LINK" / "APP_LAUNCH" / "TASK_MEMO"。Mapper 已经做了兼容处理。

### 4️⃣ 现有 ViewModel 的迁移示例

#### 旧代码 (HomeViewModel.kt)
```kotlin
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as BaseApplication).favoriteRepository
    val allFavorites = repository.allFavorites.asStateFlow()
    
    fun addFavorite(item: FavoriteItem) {
        viewModelScope.launch {
            repository.insert(item)
        }
    }
}
```

#### 新代码 (使用 Use Cases)
```kotlin
class HomeViewModel(
    private val getAllResources: GetResourcesByCategoryUseCase,
    private val searchResources: SearchResourcesUseCase,
    private val launchResource: LaunchResourceUseCase,
    application: Application
) : AndroidViewModel(application) {

    private val _resources = MutableStateFlow<List<Resource>>(emptyList())
    val resources: StateFlow<List<Resource>> = _resources.asStateFlow()
    
    init {
        loadResources()
    }
    
    private fun loadResources() {
        viewModelScope.launch {
            getAllResources(category = null).collect { list ->
                _resources.value = list
            }
        }
    }
    
    fun filterByCategory(category: ResourceCategory) {
        viewModelScope.launch {
            getAllResources(category = category).collect { list ->
                _resources.value = list
            }
        }
    }
    
    fun onResourceClick(resourceId: Long, context: Context) {
        viewModelScope.launch {
            when (val result = launchResource(resourceId)) {
                is LaunchResult.Success -> {
                    // 使用 ResourceLauncher 启动
                    ResourceLauncher.launch(context, result.resource)
                }
                is LaunchResult.ResourceNotFound -> {
                    // 显示错误提示
                }
            }
        }
    }
}
```

---

## 🔧 具体迁移步骤

### Step 1: 添加依赖 (5分钟)

1. 打开 `app/build.gradle.kts`
2. 添加 Chrome Custom Tabs 依赖：
   ```kotlin
   implementation("androidx.browser:browser:1.7.0")
   ```
3. 点击 "Sync Now"

### Step 2: 验证新架构文件 (已完成)

确认以下文件已创建：
- ✅ `domain/model/ResourceType.kt`
- ✅ `domain/model/ResourceCategory.kt`
- ✅ `domain/model/Resource.kt`
- ✅ `domain/model/Schedule.kt`
- ✅ `domain/repository/ResourceRepository.kt`
- ✅ `domain/repository/ScheduleRepository.kt`
- ✅ `domain/usecase/GetCurrentRecommendationsUseCase.kt`
- ✅ `domain/usecase/LaunchResourceUseCase.kt`
- ✅ `domain/usecase/GetResourcesByCategoryUseCase.kt`
- ✅ `domain/usecase/SearchResourcesUseCase.kt`
- ✅ `domain/usecase/ManageSchedulesUseCase.kt`
- ✅ `data/mapper/ResourceMapper.kt`
- ✅ `data/mapper/ScheduleMapper.kt`
- ✅ `data/repository/ResourceRepositoryImpl.kt` (替代旧的 FavoriteRepository)
- ✅ `data/repository/ScheduleRepositoryImpl.kt` (替代旧的 TimeScheduleRepository)
- ✅ `utils/ResourceLauncher.kt`

### Step 3: 更新 BaseApplication.kt (10分钟)

使用上面提供的新版本代码替换现有的 `BaseApplication.kt`。

### Step 4: 迁移现有 UI (可选，按需迁移)

**策略：新功能使用新架构，旧功能逐步迁移**

#### 示例：迁移 DashboardScreen

**旧代码：**
```kotlin
@Composable
fun DashboardScreen(
    onNavigateToCollection: () -> Unit,
    ...
) {
    // 直接使用 Repository
}
```

**新代码：**
```kotlin
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BaseApplication
                DashboardViewModel(
                    getCurrentRecommendations = app.getCurrentRecommendations,
                    getResourcesByCategory = app.getResourcesByCategory,
                    launchResource = app.launchResource,
                    searchResources = app.searchResources
                )
            }
        }
    )
) {
    val recommendationState by viewModel.recommendationState.collectAsState()
    val libraryState by viewModel.libraryState.collectAsState()
    
    // Dynamic Zone
    when (val state = recommendationState) {
        is RecommendationState.Success -> {
            DynamicZone(
                result = state.result,
                onResourceClick = { id -> viewModel.onResourceClick(id) { resource ->
                    // 处理资源启动
                }}
            )
        }
        ...
    }
    
    // Library Zone
    when (val state = libraryState) {
        is LibraryState.Success -> {
            LibraryZone(resources = state.resources)
        }
        ...
    }
}
```

---

## ⚠️ 注意事项

### 1️⃣ 数据兼容性

旧数据的 `type` 字段使用的是 "URL" / "APP" / "TASK"，新架构使用 "WEB_LINK" / "APP_LAUNCH" / "TASK_MEMO"。

**解决方案：** Mapper 已经做了兼容处理
```kotlin
// ResourceMapper.kt 中的兼容代码
private fun mapTypeStringToResourceType(...): ResourceType {
    return when (typeCode) {
        "WEB_LINK" -> ResourceType.WebLink
        "APP_LAUNCH" -> ResourceType.AppLaunch(packageName ?: "")
        // 兼容旧数据 ↓
        "URL" -> ResourceType.WebLink
        "APP" -> ResourceType.AppLaunch(packageName ?: "")
        "TASK" -> ResourceType.TaskMemo
        else -> ResourceType.WebLink
    }
}
```

### 2️⃣ 数据库迁移

如果需要统一 `type` 字段的格式，可以创建一个迁移：

```kotlin
// AppDatabase.kt
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 统一类型字段格式
        database.execSQL("""
            UPDATE favorites
            SET type = CASE type
                WHEN 'URL' THEN 'WEB_LINK'
                WHEN 'APP' THEN 'APP_LAUNCH'
                WHEN 'TASK' THEN 'TASK_MEMO'
                ELSE type
            END
        """)
    }
}
```

### 3️⃣ ViewModel Factory

新的 ViewModel 需要传入 Use Cases，建议使用 ViewModelProvider.Factory：

```kotlin
// 简单方式（Jetpack Compose）
val viewModel: DashboardViewModel = viewModel(
    factory = viewModelFactory {
        initializer {
            val app = this[APPLICATION_KEY] as BaseApplication
            DashboardViewModel(
                getCurrentRecommendations = app.getCurrentRecommendations,
                ...
            )
        }
    }
)
```

---

## 🎯 迁移优先级

### 高优先级 (立即迁移)
1. ✅ **BaseApplication.kt** - 依赖注入中心
2. ✅ **添加 Chrome Custom Tabs 依赖**

### 中优先级 (按需迁移)
3. **DashboardScreen** - 展示新架构的核心功能
4. **DetailScreen** - 使用 ResourceLauncher 启动资源

### 低优先级 (逐步迁移)
5. **HomeScreen** - 使用新的 Use Cases
6. **其他现有 Screen** - 保持现有实现，新功能使用新架构

---

## ✅ 验证迁移成功

运行以下检查确保迁移成功：

### 1. 编译检查
```bash
# PowerShell
./gradlew build
```

### 2. 功能测试
- [ ] 打开 App，首页能正常显示资源列表
- [ ] 点击网页链接，能使用 Chrome Custom Tabs 打开
- [ ] 点击 App 跳转，能正常启动第三方应用
- [ ] 添加新资源，能正常保存
- [ ] 搜索功能正常工作

### 3. 架构检查
- [ ] ViewModel 不直接依赖 DAO
- [ ] ViewModel 只依赖 Use Cases
- [ ] Domain 层不依赖 Android Framework

---

## 📚 常见问题

### Q: 旧的 FavoriteRepository 和新的 ResourceRepositoryImpl 可以共存吗？

A: 可以！迁移期间两者可以共存。旧代码继续使用 `FavoriteRepository`，新代码使用 `ResourceRepositoryImpl`。完全迁移后删除旧的 Repository。

### Q: 我需要重新设计数据库吗？

A: 不需要！现有的 Room Entity（FavoriteItem, TimeSchedule）保持不变，只是添加了 Mapper 层进行转换。

### Q: 如果我不想用 Chrome Custom Tabs，可以用系统浏览器吗？

A: 可以！在 `ResourceLauncher.kt` 中修改 `launchWebLink` 方法：
```kotlin
private fun launchWebLink(context: Context, url: String): LaunchResult {
    return try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
        LaunchResult.Success
    } catch (e: Exception) {
        LaunchResult.Failure("打开链接失败")
    }
}
```

### Q: 密封类和枚举的性能差异大吗？

A: 对于 App 这种规模，性能差异可以忽略不计。密封类提供的类型安全和可维护性收益远大于微小的性能开销。

---

## 🎓 下一步

完成迁移后，你可以：

1. **阅读 ARCHITECTURE.md** - 深入理解架构设计
2. **实现 Dynamic Dashboard** - 使用 `GetCurrentRecommendationsUseCase` 实现时间感知推荐
3. **添加更多 Use Cases** - 例如批量导入、导出等功能
4. **引入依赖注入** - 使用 Hilt/Koin 简化依赖管理
5. **编写单元测试** - Domain 层可以轻松进行单元测试

---

**Good luck with the migration! 🚀**
