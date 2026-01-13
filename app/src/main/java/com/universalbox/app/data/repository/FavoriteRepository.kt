package com.universalbox.app.data.repository

import com.universalbox.app.data.local.FavoriteDao
import com.universalbox.app.data.mapper.toData
import com.universalbox.app.data.mapper.toDomain
import com.universalbox.app.domain.model.Resource
import com.universalbox.app.domain.model.ResourceCategory
import com.universalbox.app.domain.model.ResourceType
import com.universalbox.app.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * ResourceRepositoryImpl - ResourceRepository 接口的实现
 * 
 * 实现依赖倒置：
 * - Domain 层定义接口
 * - Data 层实现接口
 * - ViewModel 依赖 Domain 层接口，而非这个实现类
 */
class ResourceRepositoryImpl(
    private val favoriteDao: FavoriteDao
) : ResourceRepository {

    override fun getAllResources(): Flow<List<Resource>> {
        return favoriteDao.getAllFavorites().map { items ->
            items.map { it.toDomain() }
        }
    }

    override fun getResourcesByCategory(category: ResourceCategory): Flow<List<Resource>> {
        return favoriteDao.getFavoritesByCategory(category.displayName).map { items ->
            items.map { it.toDomain() }
        }
    }

    override fun getResourcesByType(type: ResourceType): Flow<List<Resource>> {
        val typeCode = ResourceType.toTypeCode(type)
        return favoriteDao.getFavoritesByType(typeCode).map { items ->
            items.map { it.toDomain() }
        }
    }

    override fun getMostUsedResources(limit: Int): Flow<List<Resource>> {
        return favoriteDao.getMostUsedFavorites(limit).map { items ->
            items.map { it.toDomain() }
        }
    }

    override fun searchResources(query: String): Flow<List<Resource>> {
        return favoriteDao.searchFavorites(query).map { items ->
            items.map { it.toDomain() }
        }
    }

    override suspend fun getResourceById(id: Long): Resource? {
        return favoriteDao.getFavoriteById(id)?.toDomain()
    }

    override suspend fun insertResource(resource: Resource): Long {
        return favoriteDao.insertFavorite(resource.toData())
    }

    override suspend fun updateResource(resource: Resource) {
        // 二次防护：确保 ID 有效
        if (resource.id > 0) {
            favoriteDao.updateFavorite(resource.toData())
        } else {
            android.util.Log.e("ResourceRepository", "updateResource 收到无效 ID：${resource.id}，标题：${resource.title}")
            throw IllegalArgumentException("无法更新 ID=0 的资源")
        }
    }

    override suspend fun deleteResource(resource: Resource) {
        favoriteDao.deleteFavorite(resource.toData())
    }

    override suspend fun incrementUsageWeight(id: Long) {
        val item = favoriteDao.getFavoriteById(id) ?: return
        val updatedItem = item.copy(usageWeight = item.usageWeight + 1)
        favoriteDao.updateFavorite(updatedItem)
    }
}