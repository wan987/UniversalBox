package com.universalbox.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.universalbox.app.domain.model.Resource
import com.universalbox.app.domain.model.ResourceType
import com.universalbox.app.domain.model.ResourceCategory
import com.universalbox.app.domain.repository.ResourceRepository
import com.universalbox.app.utils.UrlParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: ResourceRepository) : ViewModel() {

    // 1. 原始数据流（所有收藏）
    private val allFavoritesRaw = repository.getAllResources()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. 当前选中的标签（null 表示显示全部）
    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag

    // 2.1 当前选中的来源（null 表示全部）
    private val _selectedSource = MutableStateFlow<String?>(null)
    val selectedSource: StateFlow<String?> = _selectedSource

    // 已处理的分享链接，防止重复入库
    private val processedSharedUrls = MutableStateFlow<Set<String>>(emptySet())

    // 3. 筛选后的收藏列表（根据选中的标签）
    val allFavorites: StateFlow<List<Resource>> = combine(
        allFavoritesRaw,
        _selectedTag,
        _selectedSource
    ) { favorites, tag, source ->
        favorites.filter { item ->
            val matchTag = tag == null || item.tags.contains(tag) || item.featureTag == tag
            val matchSource = source == null || item.source.equals(source, ignoreCase = true)
            matchTag && matchSource
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 4. 所有标签列表（从所有收藏中提取）
    val allTags: StateFlow<List<String>> = allFavoritesRaw
        .map { favorites ->
            favorites
                .flatMap { it.tags + it.featureTag }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 4.1 所有来源列表
    val allSources: StateFlow<List<String>> = allFavoritesRaw
        .map { favorites ->
            favorites.mapNotNull { it.source.takeIf { src -> src.isNotBlank() } }
                .distinct()
                .sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 加载状态（用于显示加载指示器）
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 5. 选择标签（用于筛选）
    fun selectTag(tag: String?) {
        _selectedTag.value = tag
    }

    fun selectSource(source: String?) {
        _selectedSource.value = source
    }

    /**
     * 处理来自分享的 URL，只处理一次
     */
    fun consumeSharedUrl(url: String) {
        if (url.isBlank()) return
        if (processedSharedUrls.value.contains(url)) return
        processedSharedUrls.value = processedSharedUrls.value + url
        addFavoriteFromUrl(url)
    }

    // 2. 提供给 UI 调用的方法：添加收藏（自动抓取网页信息）
    fun addFavoriteFromUrl(url: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // 1. 验证 URL
                if (!UrlParser.isValidUrl(url)) {
                    // URL 无效，直接保存原始链接
                    repository.insertResource(
                        Resource(
                            title = "无效链接",
                            url = url,
                            description = "这不是一个有效的网页链接",
                            source = "未知来源",
                            featureTag = "其他",
                            type = ResourceType.WebLink,
                            category = ResourceCategory.OTHER
                        )
                    )
                    return@launch
                }

                // 2. 抓取网页信息
                val webInfo = UrlParser.fetchWebPageInfo(url)

                // 3. 保存到数据库（包含自动推荐的标签）
                repository.insertResource(
                    Resource(
                        title = webInfo.title.ifBlank { "无标题" },
                        url = webInfo.url,
                        description = webInfo.description,
                        imageUrl = webInfo.imageUrl,
                        siteName = webInfo.siteName,
                        source = webInfo.source,
                        featureTag = webInfo.featureTag,
                        tags = webInfo.suggestedTags, // 已经是 List<String>
                        type = ResourceType.WebLink,
                        category = ResourceCategory.OTHER
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                // 发生错误时仍然保存，但标记为失败
                repository.insertResource(
                    Resource(
                        title = "抓取失败",
                        url = url,
                        description = "无法获取网页信息: ${e.message}",
                        source = "未知来源",
                        featureTag = "其他",
                        type = ResourceType.WebLink,
                        category = ResourceCategory.OTHER
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 3. 兼容旧方法：手动指定标题和 URL（用于测试或手动输入）
    fun addFavorite(title: String, url: String) {
        viewModelScope.launch {
            repository.insertResource(
                Resource(
                    title = title,
                    url = url,
                    source = "手动添加",
                    featureTag = "其他",
                    type = ResourceType.WebLink,
                    category = ResourceCategory.OTHER
                )
            )
        }
    }

    // 4. 提供给 UI 调用的方法：删除收藏
    fun deleteFavorite(item: Resource) {
        viewModelScope.launch {
            repository.deleteResource(item)
        }
    }
}

// ★ 这是工厂类 (Factory)
// 因为我们的 ViewModel 需要传参数 (repository)，系统默认不知道怎么传，所以需要写这个工厂
class HomeViewModelFactory(private val repository: ResourceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}