# UniversalBox - 快速开始指南

## 🎯 5 分钟上手新架构

这是一个最简洁的指南，帮你快速理解和使用新的 Clean Architecture。

---

## 📋 核心概念

### 三层架构

```
┌─────────────────┐
│   UI 层          │  ← 你看到的界面
│   (ViewModel)   │
└────────┬────────┘
         ↓ 调用
┌────────┴────────┐
│  业务逻辑层      │  ← 核心功能（推荐、搜索、启动）
│  (Use Cases)    │
└────────┬────────┘
         ↓ 调用
┌────────┴────────┐
│   数据层         │  ← 数据库操作
│  (Repository)   │
└─────────────────┘
```

### 核心文件（必看）

1. **ResourceType.kt** - 定义资源类型（网页/App/Deep Link/任务）
2. **ResourceCategory.kt** - 定义资源分类（学习/工作/娱乐/工具/生活）
3. **GetCurrentRecommendationsUseCase.kt** - 根据时间推荐资源
4. **ResourceLauncher.kt** - 启动资源（打开网页/启动App）
5. **BaseApplication.kt** - 依赖注入中心

---

## 🚀 如何使用新架构

### 场景 1: 在 ViewModel 中获取推荐资源

```kotlin
class DashboardViewModel(
    private val getCurrentRecommendations: GetCurrentRecommendationsUseCase
) : ViewModel() {

    val recommendations = getCurrentRecommendations()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
}
```

### 场景 2: 启动一个资源

```kotlin
// 在 ViewModel 中
fun onResourceClick(resourceId: Long, onSuccess: (Resource) -> Unit) {
    viewModelScope.launch {
        when (val result = launchResource(resourceId)) {
            is LaunchResult.Success -> onSuccess(result.resource)
            is LaunchResult.ResourceNotFound -> { /* 显示错误 */ }
        }
    }
}

// 在 Compose UI 中
val context = LocalContext.current
viewModel.onResourceClick(resourceId) { resource ->
    ResourceLauncher.launch(context, resource)
}
```

### 场景 3: 搜索资源

```kotlin
class SearchViewModel(
    private val searchResources: SearchResourcesUseCase
) : ViewModel() {

    fun search(query: String) {
        viewModelScope.launch {
            searchResources(query).collect { resources ->
                _searchResults.value = resources
            }
        }
    }
}
```

---

## 🔧 如何创建新资源

### 示例：添加一个 B站视频

```kotlin
// 1. 创建 Resource 对象
val bilibiliVideo = Resource(
    title = "安卓开发教程",
    url = "https://www.bilibili.com/video/BV1234567890",
    type = ResourceType.WebLink,
    category = ResourceCategory.STUDY,
    description = "零基础学 Android",
    tags = listOf("安卓", "教程", "编程")
)

// 2. 保存到数据库
viewModelScope.launch {
    resourceRepository.insertResource(bilibiliVideo)
}
```

### 示例：添加一个 App 快捷方式

```kotlin
val wechat = Resource(
    title = "微信",
    url = "com.tencent.mm",  // 包名
    type = ResourceType.AppLaunch("com.tencent.mm"),
    category = ResourceCategory.LIFE,
    description = "快速打开微信"
)

resourceRepository.insertResource(wechat)
```

### 示例：添加一个 Deep Link

```kotlin
val bilibiliCollection = Resource(
    title = "B站收藏夹",
    url = "bilibili://space/12345/favlist",
    type = ResourceType.DeepLink(
        packageName = "tv.danmaku.bili",
        uri = "bilibili://space/12345/favlist"
    ),
    category = ResourceCategory.ENTERTAINMENT,
    description = "直达我的收藏夹"
)

resourceRepository.insertResource(bilibiliCollection)
```

---

## 📱 Dynamic Dashboard 实现原理

### "Soft Guidance" 柔性引导

```kotlin
// Use Case 内部逻辑
fun invoke(): Flow<RecommendationResult> {
    return combine(schedules, resources) { schedules, allResources ->
        // 1. 获取当前时间的推荐分类
        val category = getCurrentCategory()  // 例如：现在是 "学习" 时间
        
        // 2. 筛选该分类的资源
        val recommended = allResources
            .filter { it.category == category }  // 只显示学习类资源
            .sortedByDescending { it.usageWeight }  // 按使用频率排序
            .take(6)  // 取前 6 个
        
        RecommendationResult(
            currentCategory = category,
            recommendedResources = recommended,
            contextMessage = "📚 现在是学习时间，专注于知识成长"
        )
    }
}
```

### UI 分区设计

```
┌─────────────────────────────────────┐
│  📚 现在是学习时间                    │  ← Context Message
├─────────────────────────────────────┤
│  [课堂笔记] [录音机] [在线课程]       │  ← Dynamic Zone (推荐区)
│  [学习计划] [单词本] [编程教程]       │     根据时间自动变化
├─────────────────────────────────────┤
│  所有资源 🔽                         │  ← Library Zone (资源库)
├─────────────────────────────────────┤
│  [学习] [工作] [娱乐] [工具] [生活]   │  ← 分类筛选
├─────────────────────────────────────┤
│  [所有资源列表...]                   │     用户可以手动查看所有内容
│  （包括娱乐内容，不强制隐藏）         │     ✅ Soft Guidance
└─────────────────────────────────────┘
```

---

## 🎨 如何自定义时间表

### 创建自定义时间表

```kotlin
val schedules = listOf(
    // 周一到周五：早上学习
    Schedule(
        dayOfWeek = DayOfWeek.MONDAY,
        startTime = LocalTime.of(8, 0),
        endTime = LocalTime.of(12, 0),
        recommendCategory = ResourceCategory.STUDY
    ),
    
    // 周末：全天娱乐
    Schedule(
        dayOfWeek = DayOfWeek.SATURDAY,
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(23, 0),
        recommendCategory = ResourceCategory.ENTERTAINMENT
    )
)

// 保存到数据库
viewModelScope.launch {
    manageSchedules.saveSchedules(schedules)
}
```

---

## 💡 常见使用场景

### 1️⃣ 启动资源时处理不同结果

```kotlin
val result = ResourceLauncher.launch(context, resource)

when (result) {
    is LaunchResult.Success -> {
        // 成功，什么都不做
    }
    
    is LaunchResult.AppNotInstalled -> {
        // 显示对话框：是否前往应用商店？
        AlertDialog(
            title = "应用未安装",
            confirmButton = {
                Button(onClick = {
                    ResourceLauncher.openAppStore(context, result.packageName)
                }) { Text("前往下载") }
            }
        )
    }
    
    is LaunchResult.Failure -> {
        // 显示 Toast
        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
    }
}
```

### 2️⃣ 按分类筛选资源

```kotlin
// ViewModel
fun filterByCategory(category: ResourceCategory) {
    viewModelScope.launch {
        getResourcesByCategory(
            category = category,
            sortBy = SortOption.BY_USAGE  // 按使用频率排序
        ).collect { resources ->
            _filteredResources.value = resources
        }
    }
}

// UI
Row {
    ResourceCategory.values().forEach { category ->
        FilterChip(
            selected = selectedCategory == category,
            onClick = { viewModel.filterByCategory(category) },
            label = { Text(category.displayName) }
        )
    }
}
```

### 3️⃣ 搜索资源

```kotlin
// ViewModel
fun search(query: String) {
    viewModelScope.launch {
        searchResources(query).collect { results ->
            _searchResults.value = results
        }
    }
}

// UI
SearchBar(
    query = searchQuery,
    onQueryChange = { viewModel.search(it) },
    placeholder = { Text("搜索资源...") }
)
```

---

## 🔑 关键设计决策速查

| 问题 | 方案 | 原因 |
|------|------|------|
| 如何表示资源类型？ | 密封类 `ResourceType` | 类型安全，编译时检查 |
| 如何表示资源分类？ | 枚举 `ResourceCategory` | 固定集合，携带元数据 |
| 如何打开网页？ | Chrome Custom Tabs | 更好的用户体验 |
| ViewModel 依赖什么？ | Use Cases | 不直接依赖 Repository |
| 如何在层之间转换数据？ | Mapper | Data Entity ↔ Domain Model |

---

## 📚 下一步

1. **阅读 ARCHITECTURE.md** - 深入理解架构设计
2. **阅读 MIGRATION_GUIDE.md** - 迁移现有代码
3. **查看 PROJECT_STRUCTURE.md** - 了解完整文件结构

---

## 🆘 快速调试

### 编译错误？

```bash
# 清理并重新构建
./gradlew clean build
```

### Use Case 注入失败？

检查 `BaseApplication.kt` 是否正确初始化所有 Use Cases：

```kotlin
val getCurrentRecommendations by lazy {
    GetCurrentRecommendationsUseCase(scheduleRepository, resourceRepository)
}
```

### ResourceLauncher 打不开链接？

检查是否添加了 Chrome Custom Tabs 依赖：

```kotlin
implementation("androidx.browser:browser:1.7.0")
```

---

**Happy Coding! 🎉**
