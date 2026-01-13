package com.universalbox.app

import android.app.Application

/**
 * UniversalBox 应用程序类
 * 用于全局初始化和配置
 */
class UniversalBoxApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // 在这里可以进行全局初始化，例如：
        // - 数据库初始化
        // - 日志框架初始化
        // - 第三方SDK初始化
    }
}
