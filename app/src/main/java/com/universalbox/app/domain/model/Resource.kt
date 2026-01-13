package com.universalbox.app.domain.model

/**
 * Resource - Domain 层的核心资源模型
 * 
 * 这是业务逻辑层使用的纯 Kotlin 类，独立于数据库实现
 * Data Layer 的 FavoriteItem 会转换为这个模型
 */
data class Resource(
    val id: Long = 0,
    val title: String,
    val url: String,  // 通用资源定位符（可以是 URL、包名、Deep Link 等）
    val description: String = "",
    val type: ResourceType,
    val imageUrl: String = "",
    val siteName: String = "",
    val source: String = "",           // 归因来源，如 Chrome、抖音
    val featureTag: String = "",       // 主要特征标签，如 视频/文章/学习资料
    val tags: List<String> = emptyList(),
    val category: ResourceCategory = ResourceCategory.OTHER,
    val usageWeight: Int = 0,
    val createTime: Long = System.currentTimeMillis()
) {
    /**
     * 是否为网页类型
     */
    fun isWeb(): Boolean = type is ResourceType.WebLink
    
    /**
     * 是否为应用跳转
     */
    fun isApp(): Boolean = type is ResourceType.AppLaunch || type is ResourceType.DeepLink
    
    /**
     * 是否为任务类型
     */
    fun isTask(): Boolean = type is ResourceType.TaskMemo
    
    /**
     * 获取包名（如果是 App 相关类型）
     */
    fun getPackageName(): String? {
        return when (type) {
            is ResourceType.AppLaunch -> type.packageName
            is ResourceType.DeepLink -> type.packageName
            else -> null
        }
    }
    
    /**
     * 获取 Deep Link URI（如果是 Deep Link 类型）
     */
    fun getDeepLinkUri(): String? {
        return when (type) {
            is ResourceType.DeepLink -> type.uri
            else -> null
        }
    }
}
