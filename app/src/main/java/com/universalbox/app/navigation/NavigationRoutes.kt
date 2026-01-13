package com.universalbox.app.navigation

/**
 * 导航路由定义
 * 定义应用中所有的导航路由常量
 */
object NavigationRoutes {
    const val DASHBOARD = "dashboard"           // 首页
    const val COLLECTION = "collection"         // 我的收藏
    const val COLLECTION_DETAIL = "collection/{itemId}"  // 收藏详情
    const val POMODORO = "pomodoro"            // 番茄钟
    const val OCR = "ocr"                      // OCR识别
    const val QRCODE = "qrcode"                // 二维码工具
    const val ZEN_CLOCK = "zen_clock"          // 全屏时钟
    const val DECISION_MAKER = "decision_maker" // 帮我决定
    const val NOTEBOOK = "notebook"             // 我的笔记本

    /**
     * 构建收藏详情路由
     */
    fun collectionDetail(itemId: Long): String {
        return "collection/$itemId"
    }
}
