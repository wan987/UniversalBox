package com.universalbox.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalbox.app.domain.model.Resource
import com.universalbox.app.domain.model.ResourceCategory
import com.universalbox.app.domain.usecase.GetCurrentRecommendationsUseCase
import com.universalbox.app.domain.usecase.GetResourcesByCategoryUseCase
import com.universalbox.app.domain.usecase.LaunchResourceUseCase
import com.universalbox.app.domain.usecase.RecommendationResult
import com.universalbox.app.domain.usecase.SearchResourcesUseCase
import com.universalbox.app.domain.usecase.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * DashboardViewModel - 新架构下的 ViewModel 示例
 * 
 * 展示如何使用 Use Cases 和 Clean Architecture：
 * 1. ViewModel 只依赖 Domain 层的 Use Cases
 * 2. 不直接依赖 Repository 实现
 * 3. UI State 清晰，职责单一
 */
class DashboardViewModel(
    private val getCurrentRecommendations: GetCurrentRecommendationsUseCase,
    private val getResourcesByCategory: GetResourcesByCategoryUseCase,
    private val launchResource: LaunchResourceUseCase,
    private val searchResources: SearchResourcesUseCase
) : ViewModel() {

    // UI State - Dynamic Zone (当前推荐)
    private val _recommendationState = MutableStateFlow<RecommendationState>(RecommendationState.Loading)
    val recommendationState: StateFlow<RecommendationState> = _recommendationState.asStateFlow()
    
    // UI State - Library Zone (资源库)
    private val _libraryState = MutableStateFlow<LibraryState>(LibraryState.Loading)
    val libraryState: StateFlow<LibraryState> = _libraryState.asStateFlow()
    
    // 当前选中的分类
    private val _selectedCategory = MutableStateFlow<ResourceCategory?>(null)
    val selectedCategory: StateFlow<ResourceCategory?> = _selectedCategory.asStateFlow()
    
    // 搜索关键词
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadRecommendations()
        loadLibrary()
    }

    /**
     * 加载 Dynamic Zone 推荐内容
     */
    private fun loadRecommendations() {
        viewModelScope.launch {
            getCurrentRecommendations().collect { result ->
                _recommendationState.value = RecommendationState.Success(result)
            }
        }
    }

    /**
     * 加载 Library Zone 资源库
     */
    private fun loadLibrary() {
        viewModelScope.launch {
            getResourcesByCategory(
                category = _selectedCategory.value,
                sortBy = SortOption.BY_USAGE
            ).collect { resources ->
                _libraryState.value = LibraryState.Success(resources)
            }
        }
    }

    /**
     * 切换分类筛选
     */
    fun filterByCategory(category: ResourceCategory?) {
        _selectedCategory.value = category
        loadLibrary()
    }

    /**
     * 搜索资源
     */
    fun search(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            searchResources(query).collect { resources ->
                _libraryState.value = LibraryState.Success(resources)
            }
        }
    }

    /**
     * 启动资源
     * 
     * 注意：实际的 Intent 启动由 UI 层处理
     * ViewModel 只负责业务逻辑（记录使用权重）
     */
    fun onResourceClick(resourceId: Long, onLaunchSuccess: (Resource) -> Unit) {
        viewModelScope.launch {
            when (val result = launchResource(resourceId)) {
                is com.universalbox.app.domain.usecase.LaunchResult.Success -> {
                    onLaunchSuccess(result.resource)
                }
                is com.universalbox.app.domain.usecase.LaunchResult.ResourceNotFound -> {
                    // UI 层可以显示错误提示
                }
            }
        }
    }

    /**
     * 刷新推荐
     */
    fun refresh() {
        loadRecommendations()
        loadLibrary()
    }
}

/**
 * Dynamic Zone 推荐状态
 */
sealed class RecommendationState {
    data object Loading : RecommendationState()
    data class Success(val result: RecommendationResult) : RecommendationState()
    data class Error(val message: String) : RecommendationState()
}

/**
 * Library Zone 资源库状态
 */
sealed class LibraryState {
    data object Loading : LibraryState()
    data class Success(val resources: List<Resource>) : LibraryState()
    data class Error(val message: String) : LibraryState()
}
