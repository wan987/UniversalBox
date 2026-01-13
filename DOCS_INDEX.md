# UniversalBox - 文档索引

欢迎来到 UniversalBox "个人数字生活路由" 项目！

---

## 📚 文档导航

### 🚀 快速开始
**从这里开始！** 5 分钟快速了解新架构

👉 [QUICK_START.md](QUICK_START.md)

---

### 📖 核心文档

#### 1️⃣ 架构设计指南 ⭐⭐⭐
**深入理解 Clean Architecture 设计决策**

👉 [ARCHITECTURE.md](ARCHITECTURE.md)

**内容包括：**
- 架构概览（三层架构图）
- 核心设计决策（为什么用密封类？为什么用枚举？）
- 层级说明（Domain / Data / Presentation）
- 数据流向（完整的数据流图）
- 使用指南（如何添加新功能）
- 最佳实践（ViewModel 应该依赖什么？）

**适合：** 想深入理解架构的开发者
**阅读时间：** 30 分钟

---

#### 2️⃣ 迁移指南
**从旧架构平滑迁移到新架构**

👉 [MIGRATION_GUIDE.md](MIGRATION_GUIDE.md)

**内容包括：**
- 架构对比（新旧架构差异）
- 需要更新的文件（BaseApplication.kt、build.gradle.kts 等）
- 具体迁移步骤（Step by Step）
- 注意事项（数据兼容性、ViewModel Factory）
- 迁移优先级（高/中/低）
- 验证清单

**适合：** 需要迁移现有代码的开发者
**阅读时间：** 15 分钟

---

#### 3️⃣ 项目结构说明
**完整的目录结构和文件说明**

👉 [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)

**内容包括：**
- 完整目录树
- 核心文件说明
- 数据流示例
- 依赖关系图
- 关键设计原则
- 快速定位指南

**适合：** 想快速找到特定文件的开发者
**阅读时间：** 10 分钟

---

#### 4️⃣ 重构完成报告
**了解本次重构的所有工作**

👉 [REFACTORING_SUMMARY.md](REFACTORING_SUMMARY.md)

**内容包括：**
- 已完成的工作清单
- 核心设计决策回顾
- 架构优势说明
- 实现的核心功能
- 下一步建议

**适合：** 项目管理者、想了解整体进展的开发者
**阅读时间：** 15 分钟

---

## 🎯 推荐阅读路径

### 新手开发者
```
1. QUICK_START.md (5 min)
   ↓
2. PROJECT_STRUCTURE.md (10 min)
   ↓
3. ARCHITECTURE.md (30 min)
```

### 需要迁移代码
```
1. MIGRATION_GUIDE.md (15 min)
   ↓
2. ARCHITECTURE.md (重点阅读"核心设计决策") (15 min)
   ↓
3. 开始迁移
```

### 项目管理者/架构师
```
1. REFACTORING_SUMMARY.md (15 min)
   ↓
2. ARCHITECTURE.md (30 min)
   ↓
3. 评估是否需要调整架构
```

---

## 📁 关键代码位置

### Domain Layer (业务逻辑)
```
app/src/main/java/com/universalbox/app/domain/
├── model/                  # 领域模型
│   ├── ResourceType.kt     # 资源类型（密封类）
│   ├── ResourceCategory.kt # 资源分类（枚举）
│   ├── Resource.kt         # 资源领域模型
│   └── Schedule.kt         # 时间表领域模型
│
├── repository/             # 仓库接口
│   ├── ResourceRepository.kt
│   └── ScheduleRepository.kt
│
└── usecase/                # 业务用例
    ├── GetCurrentRecommendationsUseCase.kt
    ├── LaunchResourceUseCase.kt
    ├── GetResourcesByCategoryUseCase.kt
    ├── SearchResourcesUseCase.kt
    └── ManageSchedulesUseCase.kt
```

### Data Layer (数据访问)
```
app/src/main/java/com/universalbox/app/data/
├── model/                  # Room Entity
│   ├── FavoriteItem.kt
│   └── TimeSchedule.kt
│
├── mapper/                 # 数据映射器
│   ├── ResourceMapper.kt
│   └── ScheduleMapper.kt
│
└── repository/             # 仓库实现
    ├── ResourceRepositoryImpl.kt
    └── ScheduleRepositoryImpl.kt
```

### Presentation Layer (UI)
```
app/src/main/java/com/universalbox/app/ui/
├── viewmodel/
│   └── DashboardViewModel.kt  # 示例 ViewModel
│
└── screens/
    ├── dashboard/DashboardScreen.kt
    ├── home/HomeScreen.kt
    └── detail/DetailScreen.kt
```

### Utils
```
app/src/main/java/com/universalbox/app/utils/
├── ResourceLauncher.kt     # 资源启动器
└── UrlParser.kt            # URL 解析器
```

---

## 🔍 快速搜索

### 想找...

#### 如何定义资源类型？
→ `domain/model/ResourceType.kt`

#### 如何启动一个 App？
→ `utils/ResourceLauncher.kt`

#### 如何获取推荐资源？
→ `domain/usecase/GetCurrentRecommendationsUseCase.kt`

#### 如何在 ViewModel 中使用？
→ `ui/viewmodel/DashboardViewModel.kt`

#### 如何转换数据库实体？
→ `data/mapper/ResourceMapper.kt`

---

## 💡 核心概念速查

| 概念 | 说明 | 文件 |
|------|------|------|
| **密封类** | 类型安全的资源类型 | ResourceType.kt |
| **枚举** | 固定的资源分类 | ResourceCategory.kt |
| **Use Case** | 封装一个业务场景 | usecase/*.kt |
| **Mapper** | Entity ↔ Domain Model | mapper/*.kt |
| **依赖倒置** | Domain 定义接口，Data 实现 | repository/*.kt |
| **Soft Guidance** | 柔性引导，不强制限制 | GetCurrentRecommendationsUseCase.kt |

---

## 🎓 学习资源

### 推荐阅读
- [Clean Architecture - Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Android MVVM Guide](https://developer.android.com/topic/architecture)
- [Kotlin Sealed Classes](https://kotlinlang.org/docs/sealed-classes.html)

### 视频教程
- [Clean Architecture in Android](https://www.youtube.com/results?search_query=clean+architecture+android)
- [MVVM Pattern Explained](https://www.youtube.com/results?search_query=android+mvvm)

---

## 🆘 获取帮助

### 遇到问题？

1. **编译错误**
   - 检查 [MIGRATION_GUIDE.md](MIGRATION_GUIDE.md) 的"注意事项"

2. **不理解架构**
   - 阅读 [ARCHITECTURE.md](ARCHITECTURE.md) 的"核心设计决策"

3. **不知道如何使用**
   - 查看 [QUICK_START.md](QUICK_START.md) 的"常见使用场景"

4. **找不到文件**
   - 查看 [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) 的"快速定位指南"

---

## 📈 版本历史

### v2.0 - Clean Architecture 重构 (2026-01-06)
- ✅ 引入三层架构（Domain / Data / Presentation）
- ✅ 使用密封类表示资源类型
- ✅ 使用枚举表示资源分类
- ✅ 创建 5 个核心 Use Cases
- ✅ 实现 ResourceLauncher（Chrome Custom Tabs）
- ✅ 完善文档系统（4 篇核心文档）

### v1.0 - 初始版本
- 基础收藏功能
- 自动抓取网页元数据
- 标签系统
- CRUD 操作

---

## 🚀 快速链接

- 🏠 [项目首页](README.md)
- 📖 [快速开始](QUICK_START.md)
- 🏗️ [架构设计](ARCHITECTURE.md)
- 🔄 [迁移指南](MIGRATION_GUIDE.md)
- 📁 [项目结构](PROJECT_STRUCTURE.md)
- 📝 [重构报告](REFACTORING_SUMMARY.md)

---

**Made with ❤️ for UniversalBox**
