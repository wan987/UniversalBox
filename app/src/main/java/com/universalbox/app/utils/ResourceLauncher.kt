package com.universalbox.app.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.universalbox.app.domain.model.Resource
import com.universalbox.app.domain.model.ResourceType

/**
 * ResourceLauncher - 资源启动器
 * 
 * 封装不同类型资源的跳转逻辑：
 * 1. Web Link → Chrome Custom Tabs (优雅的内置浏览器体验)
 * 2. App Launch → 启动第三方应用
 * 3. Deep Link → 跳转到应用内特定页面
 * 4. Task/Memo → 不需要跳转，仅展示
 * 
 * 设计理念：
 * - 使用 Chrome Custom Tabs 而非系统浏览器，提供更好的用户体验
 * - 优雅的错误处理，避免应用崩溃
 * - 提供详细的失败反馈
 */
object ResourceLauncher {
    
    /**
     * 启动资源
     * @param context Android Context
     * @param resource 要启动的资源
     * @return LaunchResult 启动结果
     */
    fun launch(context: Context, resource: Resource): LaunchResult {
        return when (val type = resource.type) {
            is ResourceType.WebLink -> {
                launchWebLink(context, resource.url)
            }
            
            is ResourceType.AppLaunch -> {
                launchApp(context, type.packageName)
            }
            
            is ResourceType.DeepLink -> {
                launchDeepLink(context, type.uri, type.packageName)
            }
            
            is ResourceType.TaskMemo -> {
                // 任务类型不需要启动，由 UI 层处理展示
                LaunchResult.Success
            }
        }
    }
    
    /**
     * 启动网页 - 使用 Chrome Custom Tabs
     */
    private fun launchWebLink(context: Context, url: String): LaunchResult {
        return try {
            val uri = Uri.parse(url)
            
            // 优先使用 Chrome Custom Tabs，提供类似内置浏览器的体验
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setUrlBarHidingEnabled(true)
                .build()
            
            customTabsIntent.launchUrl(context, uri)
            LaunchResult.Success
            
        } catch (e: ActivityNotFoundException) {
            LaunchResult.Failure("未找到可以打开此链接的应用")
        } catch (e: Exception) {
            LaunchResult.Failure("打开链接失败: ${e.message}")
        }
    }
    
    /**
     * 启动第三方应用
     */
    private fun launchApp(context: Context, packageName: String): LaunchResult {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                LaunchResult.Success
            } else {
                LaunchResult.AppNotInstalled(packageName)
            }
            
        } catch (e: ActivityNotFoundException) {
            LaunchResult.AppNotInstalled(packageName)
        } catch (e: Exception) {
            LaunchResult.Failure("启动应用失败: ${e.message}")
        }
    }
    
    /**
     * 启动 Deep Link
     */
    private fun launchDeepLink(
        context: Context,
        deepLinkUri: String,
        packageName: String
    ): LaunchResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUri))
            intent.setPackage(packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            // 检查是否有应用可以处理这个 Intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                LaunchResult.Success
            } else {
                LaunchResult.AppNotInstalled(packageName)
            }
            
        } catch (e: ActivityNotFoundException) {
            LaunchResult.AppNotInstalled(packageName)
        } catch (e: Exception) {
            LaunchResult.Failure("打开 Deep Link 失败: ${e.message}")
        }
    }
    
    /**
     * 检查应用是否已安装
     */
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 打开应用商店页面（用于安装未安装的应用）
     */
    fun openAppStore(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // 如果没有应用商店，使用浏览器打开
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}

/**
 * 启动结果密封类
 */
sealed class LaunchResult {
    /** 成功启动 */
    data object Success : LaunchResult()
    
    /** 应用未安装 */
    data class AppNotInstalled(val packageName: String) : LaunchResult()
    
    /** 启动失败 */
    data class Failure(val message: String) : LaunchResult()
}

/**
 * 扩展函数：显示启动结果的 Toast
 */
fun LaunchResult.showToast(context: Context) {
    when (this) {
        is LaunchResult.Success -> {
            // 成功时不显示 Toast
        }
        is LaunchResult.AppNotInstalled -> {
            Toast.makeText(
                context,
                "应用未安装，是否前往应用商店下载？",
                Toast.LENGTH_LONG
            ).show()
        }
        is LaunchResult.Failure -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
