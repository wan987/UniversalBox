package com.universalbox.app.domain.usecase

import com.universalbox.app.domain.model.Resource
import com.universalbox.app.domain.model.ResourceCategory
import com.universalbox.app.domain.repository.ResourceRepository
import com.universalbox.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * GetCurrentRecommendations - 获取当前时间的推荐资源
 * 
 * 这是 "Dynamic Zone" 的核心逻辑
 * 实现 "Soft Guidance" 理念：根据当前时间推荐，但不强制限制
 */
class GetCurrentRecommendationsUseCase(
    private val scheduleRepository: ScheduleRepository,
    private val resourceRepository: ResourceRepository
) {
    /**
     * 执行用例
     * @return Flow<RecommendationResult> 包含推荐分类和资源列表
     */
    operator fun invoke(): Flow<RecommendationResult> {
        return combine(
            scheduleRepository.getAllSchedules(),
            resourceRepository.getAllResources()
        ) { schedules, allResources ->
            // 1. 获取当前推荐的分类
            val recommendedCategory = scheduleRepository.getCurrentRecommendedCategory()
            
            // 2. 根据分类筛选资源
            val recommendedResources = if (recommendedCategory != null) {
                allResources
                    .filter { it.category == recommendedCategory }
                    .sortedByDescending { it.usageWeight }  // 按使用频率排序
                    .take(6)  // 取前 6 个
            } else {
                // 如果没有匹配的时间段，显示最常用的资源
                allResources
                    .sortedByDescending { it.usageWeight }
                    .take(6)
            }
            
            RecommendationResult(
                currentCategory = recommendedCategory,
                recommendedResources = recommendedResources,
                contextMessage = generateContextMessage(recommendedCategory)
            )
        }
    }
    
    /**
     * 生成上下文提示信息
     */
    private fun generateContextMessage(category: ResourceCategory?): String {
        return when (category) {
            ResourceCategory.STUDY -> "📚 现在是学习时间，专注于知识成长"
            ResourceCategory.WORK -> "💼 工作时段，高效完成任务"
            ResourceCategory.ENTERTAINMENT -> "🎮 休闲时光，享受生活"
            ResourceCategory.TOOL -> "🔧 工具时间，提升效率"
            ResourceCategory.LIFE -> "🏠 生活时段，照顾日常"
            else -> "✨ 探索你的数字世界"
        }
    }
}

/**
 * 推荐结果数据类
 */
data class RecommendationResult(
    val currentCategory: ResourceCategory?,
    val recommendedResources: List<Resource>,
    val contextMessage: String
)
