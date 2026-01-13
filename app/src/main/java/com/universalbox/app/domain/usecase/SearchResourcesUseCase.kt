package com.universalbox.app.domain.usecase

import com.universalbox.app.domain.model.Resource
import com.universalbox.app.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow

/**
 * SearchResources - 搜索资源
 * 
 * 支持按标题、描述、标签搜索
 */
class SearchResourcesUseCase(
    private val resourceRepository: ResourceRepository
) {
    /**
     * 执行用例
     * @param query 搜索关键词
     * @return Flow<List<Resource>> 搜索结果
     */
    operator fun invoke(query: String): Flow<List<Resource>> {
        return if (query.isBlank()) {
            resourceRepository.getAllResources()
        } else {
            resourceRepository.searchResources(query)
        }
    }
}
