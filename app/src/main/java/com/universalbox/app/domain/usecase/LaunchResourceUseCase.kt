package com.universalbox.app.domain.usecase

import com.universalbox.app.domain.model.Resource
import com.universalbox.app.domain.repository.ResourceRepository

/**
 * LaunchResource - 启动资源并记录使用
 * 
 * 这个用例封装了"打开资源"的完整业务逻辑：
 * 1. 记录使用权重
 * 2. 返回资源信息供 UI 处理跳转
 */
class LaunchResourceUseCase(
    private val resourceRepository: ResourceRepository
) {
    /**
     * 执行用例
     * @param resourceId 资源 ID
     * @return LaunchResult 启动结果
     */
    suspend operator fun invoke(resourceId: Long): LaunchResult {
        // 1. 获取资源信息
        val resource = resourceRepository.getResourceById(resourceId)
            ?: return LaunchResult.ResourceNotFound
        
        // 2. 增加使用权重（后台异步）
        try {
            resourceRepository.incrementUsageWeight(resourceId)
        } catch (e: Exception) {
            // 即使权重更新失败，也不影响启动
            e.printStackTrace()
        }
        
        // 3. 返回资源信息供 UI 层处理跳转
        return LaunchResult.Success(resource)
    }
}

/**
 * 启动结果密封类
 */
sealed class LaunchResult {
    data class Success(val resource: Resource) : LaunchResult()
    data object ResourceNotFound : LaunchResult()
}
