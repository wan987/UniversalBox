package com.universalbox.app.domain.model

/**
 * 资源类型 - 使用密封类实现类型安全
 * 
 * 设计理由：相比常量字符串，密封类提供：
 * 1. 编译时类型检查
 * 2. when 表达式的完整性检查
 * 3. 可携带额外参数（如 DeepLink 的 scheme）
 */
sealed class ResourceType {
    /** 网页链接 - 使用浏览器打开 */
    data object WebLink : ResourceType()
    
    /** App 跳转 - 启动第三方应用 */
    data class AppLaunch(val packageName: String) : ResourceType()
    
    /** Deep Link - 跳转到 App 特定页面 */
    data class DeepLink(
        val packageName: String,
        val uri: String  // 例如: bilibili://space/12345
    ) : ResourceType()
    
    /** 任务/备忘录 - 纯文本展示 */
    data object TaskMemo : ResourceType()
    
    companion object {
        /**
         * 从字符串类型代码转换为密封类
         * 用于数据库存储和反序列化
         */
        fun fromTypeCode(
            typeCode: String,
            packageName: String? = null,
            uri: String? = null
        ): ResourceType {
            return when (typeCode) {
                "WEB_LINK" -> WebLink
                "APP_LAUNCH" -> AppLaunch(packageName ?: "")
                "DEEP_LINK" -> DeepLink(packageName ?: "", uri ?: "")
                "TASK_MEMO" -> TaskMemo
                else -> WebLink  // 默认类型
            }
        }
        
        /**
         * 将密封类转换为字符串类型代码
         * 用于数据库存储
         */
        fun toTypeCode(type: ResourceType): String {
            return when (type) {
                is WebLink -> "WEB_LINK"
                is AppLaunch -> "APP_LAUNCH"
                is DeepLink -> "DEEP_LINK"
                is TaskMemo -> "TASK_MEMO"
            }
        }
    }
}
