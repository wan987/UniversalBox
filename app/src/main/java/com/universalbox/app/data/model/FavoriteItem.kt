package com.universalbox.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.universalbox.app.domain.model.Resource
import com.universalbox.app.domain.model.ResourceType as DomainResourceType
import com.universalbox.app.domain.model.ResourceCategory as DomainResourceCategory

/**
 * 资源类型枚举
 */
object ResourceType {
    const val URL = "URL"      // 网页链接
    const val APP = "APP"      // 应用跳转
    const val TASK = "TASK"    // 纯任务
}

/**
 * 资源分类
 */
object ResourceCategory {
    const val STUDY = "学习"
    const val ENTERTAINMENT = "娱乐"
    const val TOOL = "工具"
    const val WORK = "工作"
    const val LIFE = "生活"
}

// @Entity 表示这就一张数据库表，表名叫 "favorites"
@Entity(tableName = "favorites")
data class FavoriteItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // 自增 ID，不用管
    val title: String,          // 标题
    val url: String,            // 链接
    val description: String = "", // 描述 (可选)
    val type: String = ResourceType.URL,  // 资源类型: URL, APP, TASK (新升级)
    val imageUrl: String = "",  // 封面图 URL
    val siteName: String = "",  // 网站名称
    val source: String = "",    // 归因来源（如 Chrome、抖音、微信）
    val featureTag: String = "",// 主特征标签（视频/文章/学习资料/工具等）
    val tags: String = "",      // 标签，逗号分隔，例："技术,学习,安卓"
    val packageName: String? = null,  // APP包名 (新增) - 当type=APP时使用
    val category: String = ResourceCategory.TOOL,  // 资源分类 (新增)
    val usageWeight: Int = 0,   // 使用频率权重 (新增)
    val createTime: Long = System.currentTimeMillis() // 创建时间，默认当前时间
) {
    // 工具方法：获取标签列表
    fun getTagList(): List<String> = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }
    
    // 工具方法：是否是APP类型
    fun isApp(): Boolean = type == ResourceType.APP
    
    // 工具方法：是否是URL类型
    fun isUrl(): Boolean = type == ResourceType.URL
    
    // 工具方法：是否是任务类型
    fun isTask(): Boolean = type == ResourceType.TASK
    
    // 转换为 Domain Resource 对象
    fun toResource(): Resource {
        val resourceType = when {
            isUrl() -> DomainResourceType.WebLink
            isApp() && packageName != null -> DomainResourceType.AppLaunch(packageName)
            isTask() -> DomainResourceType.TaskMemo
            else -> DomainResourceType.WebLink // 默认为网页链接
        }
        
        val domainCategory = when (category) {
            ResourceCategory.STUDY -> DomainResourceCategory.STUDY
            ResourceCategory.WORK -> DomainResourceCategory.WORK
            ResourceCategory.ENTERTAINMENT -> DomainResourceCategory.ENTERTAINMENT
            ResourceCategory.TOOL -> DomainResourceCategory.TOOL
            ResourceCategory.LIFE -> DomainResourceCategory.LIFE
            else -> DomainResourceCategory.OTHER
        }
        
        return Resource(
            id = id,
            title = title,
            description = description,
            url = url,
            type = resourceType,
            tags = (getTagList() + featureTag).filter { it.isNotBlank() },
            category = domainCategory,
            imageUrl = imageUrl,
            siteName = siteName,
            source = source,
            featureTag = featureTag,
            usageWeight = usageWeight,
            createTime = createTime
        )
    }
}