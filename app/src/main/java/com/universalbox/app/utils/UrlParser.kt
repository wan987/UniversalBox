package com.universalbox.app.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * 网页元数据模型
 * 用于存储从网页抓取的信息
 */
data class WebPageInfo(
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val siteName: String = "",
    val url: String = "",
    val source: String = "",
    val featureTag: String = "",
    val suggestedTags: List<String> = emptyList() // 新增：推荐标签
)

/**
 * URL 解析工具类
 * 使用 Jsoup 库抓取网页的 Open Graph 元数据
 */
object UrlParser {

    /**
     * 从指定 URL 抓取网页信息
     * @param url 网页地址
     * @return WebPageInfo 包含标题、描述、封面图等信息
     */
    suspend fun fetchWebPageInfo(url: String): WebPageInfo = withContext(Dispatchers.IO) {
        try {
            // 1. 连接并获取网页内容
            val doc: Document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000) // 10秒超时
                .get()

            // 2. 优先获取 Open Graph 元数据（大部分网站都支持）
            val ogTitle = doc.select("meta[property=og:title]").attr("content")
            val ogDescription = doc.select("meta[property=og:description]").attr("content")
            val ogImage = doc.select("meta[property=og:image]").attr("content")
            val ogSiteName = doc.select("meta[property=og:site_name]").attr("content")

            // 3. 如果没有 OG 标签，尝试获取 Twitter Card 元数据
            val twitterTitle = doc.select("meta[name=twitter:title]").attr("content")
            val twitterDescription = doc.select("meta[name=twitter:description]").attr("content")
            val twitterImage = doc.select("meta[name=twitter:image]").attr("content")

            // 4. 最后尝试普通 HTML 标签
            val htmlTitle = doc.title()
            val htmlDescription = doc.select("meta[name=description]").attr("content")

            // 5. 按优先级组合结果（OG > Twitter > HTML）
            val finalTitle = ogTitle.ifBlank { twitterTitle.ifBlank { htmlTitle } }
            val finalDescription = ogDescription.ifBlank { twitterDescription.ifBlank { htmlDescription } }
            val finalImage = ogImage.ifBlank { twitterImage }
            val finalSiteName = ogSiteName.ifBlank {
                // 如果没有站点名称，从 URL 提取域名
                extractDomainFromUrl(url)
            }

            // 6. 处理相对路径的图片 URL
            val absoluteImageUrl = if (finalImage.isNotBlank() && !finalImage.startsWith("http")) {
                doc.baseUri() + finalImage
            } else {
                finalImage
            }

            val featureTag = detectFeatureTag(url, finalSiteName, finalDescription)
            val source = detectSource(url, finalSiteName)
            WebPageInfo(
                title = finalTitle.take(200), // 限制长度防止过长
                description = finalDescription.take(500),
                imageUrl = absoluteImageUrl,
                siteName = finalSiteName,
                url = url,
                source = source,
                featureTag = featureTag,
                suggestedTags = (suggestTags(url, finalSiteName) + featureTag).filter { it.isNotBlank() }.distinct() // 自动推荐标签
            )
        } catch (e: Exception) {
            // 如果抓取失败，返回默认信息
            e.printStackTrace()
            val fallbackSite = extractDomainFromUrl(url)
            val featureTag = detectFeatureTag(url, fallbackSite, "")
            WebPageInfo(
                title = "无法获取标题",
                description = "网页信息抓取失败",
                imageUrl = "",
                siteName = fallbackSite,
                url = url,
                source = fallbackSite,
                featureTag = featureTag,
                suggestedTags = (suggestTags(url, fallbackSite) + featureTag).filter { it.isNotBlank() }.distinct()
            )
        }
    }

    /**
     * 从 URL 提取域名作为站点名称
     * 例如：https://www.google.com/search?q=test -> google.com
     */
    private fun extractDomainFromUrl(url: String): String {
        return try {
            val regex = """https?://(?:www\.)?([^/]+)""".toRegex()
            regex.find(url)?.groupValues?.get(1) ?: url
        } catch (e: Exception) {
            url
        }
    }

    /**
     * 识别来源：优先站点名，否则域名
     */
    private fun detectSource(url: String, siteName: String): String {
        val domain = extractDomainFromUrl(url)
        return when {
            siteName.isNotBlank() -> siteName
            domain.isNotBlank() -> domain
            else -> "未知来源"
        }
    }

    /**
     * 验证 URL 是否有效
     */
    fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    /**
     * 根据 URL 和站点名自动推荐标签
     * 例如：bilibili.com -> 视频, github.com -> 技术
     */
    private fun suggestTags(url: String, siteName: String): List<String> {
        val tags = mutableListOf<String>()
        val lowerUrl = url.lowercase()
        val lowerSite = siteName.lowercase()

        fun add(vararg values: String) { tags.addAll(values) }

        when {
            // 视频/短视频
            lowerSite.contains("bilibili") || lowerSite.contains("b23.tv") -> add("视频", "娱乐")
            lowerSite.contains("youtube") || lowerSite.contains("youtu.be") -> add("视频", "国际")
            lowerSite.contains("douyin") || lowerSite.contains("tiktok") -> add("短视频", "娱乐")

            // 技术/编程
            lowerSite.contains("github") -> add("技术", "开源")
            lowerSite.contains("stackoverflow") -> add("技术", "问答")
            lowerSite.contains("csdn") || lowerSite.contains("juejin") || lowerSite.contains("cnblogs") -> add("技术", "博客")

            // 社交
            lowerSite.contains("twitter") || lowerSite.contains("x.com") -> add("社交", "资讯")
            lowerSite.contains("weibo") -> add("社交", "微博")
            lowerSite.contains("zhihu") -> add("知识", "问答")

            // 新闻资讯
            lowerSite.contains("news") || lowerSite.contains("xinhua") || lowerSite.contains("163") -> add("新闻", "资讯")

            // 购物
            lowerSite.contains("taobao") || lowerSite.contains("tmall") || lowerSite.contains("jd") -> add("购物", "电商")
            lowerSite.contains("amazon") -> add("购物", "国际")

            // 学习教育
            lowerSite.contains("coursera") || lowerSite.contains("udemy") || lowerSite.contains("edx") -> add("学习", "课程")
            lowerSite.contains("baidu") || lowerSite.contains("google") -> add("搜索", "工具")
        }

        // 根据 URL 关键词补充
        if (lowerUrl.contains("pdf")) add("文档")
        if (lowerUrl.contains("wiki")) add("百科")

        if (tags.isEmpty()) add("待分类")

        return tags.distinct() // 去重
    }

    private fun detectFeatureTag(url: String, siteName: String, description: String): String {
        val lowerUrl = url.lowercase()
        val lowerSite = siteName.lowercase()
        val lowerDesc = description.lowercase()

        return when {
            listOf(lowerSite, lowerUrl, lowerDesc).any { it.contains("video") || it.contains("bilibili") || it.contains("youtube") || it.contains("douyin") || it.contains("tiktok") } -> "视频"
            listOf(lowerSite, lowerDesc).any { it.contains("course") || it.contains("lesson") || it.contains("learn") || it.contains("study") } -> "学习资料"
            listOf(lowerUrl, lowerSite).any { it.contains("blog") || it.contains("juejin") || it.contains("csdn") || it.contains("medium") } -> "文章"
            lowerUrl.contains("pdf") || lowerDesc.contains("pdf") -> "文档"
            lowerSite.contains("shop") || lowerUrl.contains("item") || lowerSite.contains("taobao") || lowerSite.contains("jd") -> "购物"
            lowerSite.contains("news") || lowerDesc.contains("news") -> "新闻"
            lowerSite.contains("github") || lowerSite.contains("gitlab") -> "开源"
            lowerSite.contains("zhihu") || lowerSite.contains("stack") -> "问答"
            else -> "其他"
        }
    }
}
