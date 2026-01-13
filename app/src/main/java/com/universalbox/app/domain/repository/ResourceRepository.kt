package com.universalbox.app.domain.repository

import com.universalbox.app.domain.model.Resource
import com.universalbox.app.domain.model.ResourceCategory
import com.universalbox.app.domain.model.ResourceType
import kotlinx.coroutines.flow.Flow

/**
 * ResourceRepository - Domain 层的资源仓库接口
 * 
 * 依赖倒置原则：Domain 层定义接口，Data 层实现接口
 * 这样 Domain 层不依赖任何具体实现
 */
interface ResourceRepository {
    /**
     * 获取所有资源的 Flow
     */
    fun getAllResources(): Flow<List<Resource>>
    
    /**
     * 根据分类获取资源
     */
    fun getResourcesByCategory(category: ResourceCategory): Flow<List<Resource>>
    
    /**
     * 根据类型获取资源
     */
    fun getResourcesByType(type: ResourceType): Flow<List<Resource>>
    
    /**
     * 获取使用频率最高的资源
     * @param limit 限制返回数量
     */
    fun getMostUsedResources(limit: Int = 10): Flow<List<Resource>>
    
    /**
     * 搜索资源
     * @param query 搜索关键词（搜索标题、描述、标签）
     */
    fun searchResources(query: String): Flow<List<Resource>>
    
    /**
     * 根据 ID 获取资源
     */
    suspend fun getResourceById(id: Long): Resource?
    
    /**
     * 插入资源
     */
    suspend fun insertResource(resource: Resource): Long
    
    /**
     * 更新资源
     */
    suspend fun updateResource(resource: Resource)
    
    /**
     * 删除资源
     */
    suspend fun deleteResource(resource: Resource)
    
    /**
     * 增加使用频率权重
     */
    suspend fun incrementUsageWeight(id: Long)
}
