package com.universalbox.app.data.mapper

import com.universalbox.app.data.model.FavoriteItem
import com.universalbox.app.domain.model.Resource
import com.universalbox.app.domain.model.ResourceCategory
import com.universalbox.app.domain.model.ResourceType

/**
 * Mapper - 在 Data Layer 和 Domain Layer 之间转换数据
 * 
 * 这是 Clean Architecture 的重要部分，确保：
 * 1. Domain 层不依赖 Room 数据库
 * 2. 数据库结构变化不影响业务逻辑
 */

/**
 * FavoriteItem (Room Entity) -> Resource (Domain Model)
 */
fun FavoriteItem.toDomain(): Resource {
    return Resource(
        id = this.id,
        title = this.title,
        url = this.url,
        description = this.description,
        type = mapTypeStringToResourceType(this.type, this.packageName, this.url),
        imageUrl = this.imageUrl,
        siteName = this.siteName,
        source = this.source,
        featureTag = this.featureTag,
        tags = (this.getTagList() + this.featureTag).filter { it.isNotBlank() },
        category = ResourceCategory.fromString(this.category),
        usageWeight = this.usageWeight,
        createTime = this.createTime
    )
}

/**
 * Resource (Domain Model) -> FavoriteItem (Room Entity)
 */
fun Resource.toData(): FavoriteItem {
    return FavoriteItem(
        id = this.id,
        title = this.title,
        url = this.url,
        description = this.description,
        type = ResourceType.toTypeCode(this.type),
        imageUrl = this.imageUrl,
        siteName = this.siteName,
        source = this.source,
        featureTag = this.featureTag,
        tags = this.tags.joinToString(","),
        packageName = this.getPackageName(),
        category = this.category.displayName,
        usageWeight = this.usageWeight,
        createTime = this.createTime
    )
}

/**
 * 将数据库存储的字符串类型映射为 ResourceType 密封类
 */
private fun mapTypeStringToResourceType(
    typeCode: String,
    packageName: String?,
    url: String
): ResourceType {
    return when (typeCode) {
        "WEB_LINK" -> ResourceType.WebLink
        "APP_LAUNCH" -> ResourceType.AppLaunch(packageName ?: "")
        "DEEP_LINK" -> {
            // Deep Link 需要解析 URL 获取 scheme
            ResourceType.DeepLink(packageName ?: "", url)
        }
        "TASK_MEMO" -> ResourceType.TaskMemo
        // 兼容旧数据
        "URL" -> ResourceType.WebLink
        "APP" -> ResourceType.AppLaunch(packageName ?: "")
        "TASK" -> ResourceType.TaskMemo
        else -> ResourceType.WebLink
    }
}
