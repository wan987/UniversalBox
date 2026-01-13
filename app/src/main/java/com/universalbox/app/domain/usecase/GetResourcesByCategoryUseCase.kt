package com.universalbox.app.domain.usecase

import com.universalbox.app.domain.model.Resource
import com.universalbox.app.domain.model.ResourceCategory
import com.universalbox.app.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * GetResourcesByCategory - 根据分类获取资源
 * 
 * 这是 "Library Zone" 的核心逻辑
 * 用户可以手动筛选查看所有分类的资源，不受时间限制
 */
class GetResourcesByCategoryUseCase(
    private val resourceRepository: ResourceRepository
) {
    /**
     * 执行用例
     * @param category 资源分类，null 表示显示所有
     * @param sortBy 排序方式
     * @return Flow<List<Resource>> 资源列表
     */
    operator fun invoke(
        category: ResourceCategory? = null,
        sortBy: SortOption = SortOption.BY_CREATE_TIME
    ): Flow<List<Resource>> {
        val resourcesFlow = if (category != null) {
            resourceRepository.getResourcesByCategory(category)
        } else {
            resourceRepository.getAllResources()
        }
        
        return resourcesFlow.map { resources ->
            when (sortBy) {
                SortOption.BY_CREATE_TIME -> resources.sortedByDescending { it.createTime }
                SortOption.BY_USAGE -> resources.sortedByDescending { it.usageWeight }
                SortOption.BY_TITLE -> resources.sortedBy { it.title }
            }
        }
    }
}

/**
 * 排序选项
 */
enum class SortOption {
    BY_CREATE_TIME,  // 按创建时间
    BY_USAGE,        // 按使用频率
    BY_TITLE         // 按标题字母序
}
