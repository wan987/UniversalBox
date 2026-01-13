package com.universalbox.app.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.universalbox.app.domain.model.Resource
import com.universalbox.app.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val itemId: Long,
    private val repository: ResourceRepository
) : ViewModel() {

    private val _favoriteItem = MutableStateFlow<Resource?>(null)
    val favoriteItem: StateFlow<Resource?> = _favoriteItem

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadFavoriteItem()
    }

    private fun loadFavoriteItem() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _favoriteItem.value = repository.getResourceById(itemId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFavorite(item: Resource) {
        viewModelScope.launch {
            // 防御性编程：确保 ID 有效，避免意外插入新记录
            if (item.id > 0) {
                repository.updateResource(item)
            } else {
                // 记录错误：不应该用 ID=0 的对象来更新
                android.util.Log.e("DetailViewModel", "尝试更新 ID=0 的收藏，操作被拒绝：${item.title}")
            }
        }
    }

    fun deleteFavorite(item: Resource) {
        viewModelScope.launch {
            repository.deleteResource(item)
        }
    }
}

class DetailViewModelFactory(
    private val itemId: Long,
    private val repository: ResourceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(itemId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
