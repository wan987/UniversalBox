package com.universalbox.app.ui.screens.home

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.universalbox.app.domain.model.Resource
import com.universalbox.app.domain.model.ResourceType
import com.universalbox.app.domain.model.ResourceCategory
import com.universalbox.app.domain.repository.ResourceRepository
import com.universalbox.app.utils.UrlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 已安装应用的数据类
 */
data class InstalledApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)

/**
 * AddResource 的 ViewModel
 * 负责处理添加链接和添加应用的业务逻辑
 */
class AddResourceViewModel(
    private val repository: ResourceRepository,
    private val context: Context
) : ViewModel() {

    // 已安装应用列表
    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 错误消息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * 加载已安装的应用列表
     */
    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val apps = withContext(Dispatchers.IO) {
                    val packageManager = context.packageManager
                    val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                    
                    packages
                        .filter { isUserApp(it) } // 过滤掉系统应用
                        .sortedBy { it.loadLabel(packageManager).toString() }
                        .map { appInfo ->
                            InstalledApp(
                                packageName = appInfo.packageName,
                                appName = appInfo.loadLabel(packageManager).toString(),
                                icon = try {
                                    appInfo.loadIcon(packageManager)
                                } catch (e: Exception) {
                                    null
                                }
                            )
                        }
                }
                _installedApps.value = apps
            } catch (e: Exception) {
                _errorMessage.value = "加载应用列表失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 判断是否为用户应用（非系统应用）
     */
    private fun isUserApp(appInfo: ApplicationInfo): Boolean {
        return (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
    }

    /**
     * 添加应用到数据库
     */
    fun addApp(app: InstalledApp, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val resource = Resource(
                    title = app.appName,
                    url = app.packageName, // 包名存储在 url 字段
                    description = "应用: ${app.appName}",
                    type = ResourceType.AppLaunch(packageName = app.packageName),
                    category = ResourceCategory.TOOL,
                    source = app.appName,
                    featureTag = "应用",
                    tags = listOf("应用", "工具", "应用分享")
                )
                
                repository.insertResource(resource)
                
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                _errorMessage.value = "保存失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 添加链接到数据库（从 URL 自动抓取信息）
     */
    fun addLinkFromUrl(url: String, title: String? = null, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // 验证 URL
                if (!UrlParser.isValidUrl(url)) {
                    _errorMessage.value = "请输入有效的网址"
                    _isLoading.value = false
                    return@launch
                }

                // 抓取网页信息
                val webInfo = withContext(Dispatchers.IO) {
                    UrlParser.fetchWebPageInfo(url)
                }

                // 保存到数据库
                val resource = Resource(
                    title = title?.takeIf { it.isNotBlank() } ?: webInfo.title,
                    url = url,
                    description = webInfo.description,
                    imageUrl = webInfo.imageUrl,
                    siteName = webInfo.siteName,
                    source = webInfo.source,
                    featureTag = webInfo.featureTag,
                    type = ResourceType.WebLink,
                    category = ResourceCategory.OTHER,
                    tags = webInfo.suggestedTags
                )

                repository.insertResource(resource)
                
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                _errorMessage.value = "保存失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

/**
 * AddResourceViewModel 的 Factory
 */
class AddResourceViewModelFactory(
    private val repository: ResourceRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddResourceViewModel::class.java)) {
            return AddResourceViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
