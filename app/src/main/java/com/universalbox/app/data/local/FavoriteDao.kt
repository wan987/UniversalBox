package com.universalbox.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.universalbox.app.data.model.FavoriteItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    // 1. 插入数据 (如果冲突就替换)
    @Insert
    suspend fun insertFavorite(item: FavoriteItem): Long

    // 2. 删除数据
    @Delete
    suspend fun deleteFavorite(item: FavoriteItem)

    // 3. 更新数据 (新增)
    @Update
    suspend fun updateFavorite(item: FavoriteItem)

    // 4. 查询所有 (按时间倒序，新的在前面)
    // Flow 是 Kotlin 的黑科技，数据一变，界面会自动刷新，不用手动查
    @Query("SELECT * FROM favorites ORDER BY createTime DESC")
    fun getAllFavorites(): Flow<List<FavoriteItem>>

    // 5. 根据ID查询单个收藏 (新增)
    @Query("SELECT * FROM favorites WHERE id = :id")
    suspend fun getFavoriteById(id: Long): FavoriteItem?

    // 6. 根据分类查询收藏
    @Query("SELECT * FROM favorites WHERE category = :category ORDER BY usageWeight DESC, createTime DESC")
    fun getFavoritesByCategory(category: String): Flow<List<FavoriteItem>>

    // 7. 查询最常用的收藏 (按使用频率排序)
    @Query("SELECT * FROM favorites ORDER BY usageWeight DESC, createTime DESC LIMIT :limit")
    fun getMostUsedFavorites(limit: Int = 10): Flow<List<FavoriteItem>>

    // 8. 根据类型查询
    @Query("SELECT * FROM favorites WHERE type = :type ORDER BY createTime DESC")
    fun getFavoritesByType(type: String): Flow<List<FavoriteItem>>

    // 9. 搜索收藏 (标题或描述包含关键词)
    @Query("SELECT * FROM favorites WHERE title LIKE '%' || :keyword || '%' OR description LIKE '%' || :keyword || '%' ORDER BY usageWeight DESC")
    fun searchFavorites(keyword: String): Flow<List<FavoriteItem>>
}