package com.universalbox.app.domain.model

/**
 * 资源分类 - 用于场景化推荐
 * 
 * 设计理由：使用枚举而非密封类
 * 1. 分类是固定的预定义集合
 * 2. 不需要携带额外参数
 * 3. 枚举提供更好的性能和便捷的遍历
 */
enum class ResourceCategory(
    val displayName: String,
    val icon: String,  // Material Icon 名称
    val color: Long    // Jetpack Compose Color value (0xFFRRGGBB)
) {
    STUDY(
        displayName = "学习",
        icon = "school",
        color = 0xFF4CAF50  // Green
    ),
    WORK(
        displayName = "工作",
        icon = "work",
        color = 0xFF2196F3  // Blue
    ),
    ENTERTAINMENT(
        displayName = "娱乐",
        icon = "sports_esports",
        color = 0xFFFF9800  // Orange
    ),
    TOOL(
        displayName = "工具",
        icon = "build",
        color = 0xFF9C27B0  // Purple
    ),
    LIFE(
        displayName = "生活",
        icon = "home",
        color = 0xFFF44336  // Red
    ),
    OTHER(
        displayName = "其他",
        icon = "more_horiz",
        color = 0xFF757575  // Grey
    );
    
    companion object {
        /**
         * 从字符串转换为枚举
         */
        fun fromString(value: String): ResourceCategory {
            return when (value) {
                "学习" -> STUDY
                "工作" -> WORK
                "娱乐" -> ENTERTAINMENT
                "工具" -> TOOL
                "生活" -> LIFE
                else -> OTHER
            }
        }
    }
}
