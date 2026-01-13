package com.universalbox.app.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.universalbox.app.domain.model.Resource
import com.universalbox.app.domain.model.ResourceType
import com.universalbox.app.domain.model.ResourceCategory
import com.universalbox.app.navigation.NavigationRoutes
import com.universalbox.app.ui.viewmodel.LibraryState
import com.universalbox.app.ui.viewmodel.RecommendationState
import com.universalbox.app.utils.ResourceLauncher
import com.universalbox.app.ui.components.bouncyClick
import com.universalbox.app.ui.components.strongBouncyClick
import com.universalbox.app.ui.theme.AppTheme
import com.universalbox.app.ui.theme.HomeBrush
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay
import java.util.Calendar

/**
 * 首页数据模型
 */
data class DashboardItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color,      // 图标颜色
    val backgroundColor: Color, // 背景颜色（浅色泡泡）
    val route: String,
    val enabled: Boolean = true
)

/**
 * 首页 Dashboard - 升级版（整合动态推荐）
 */
@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    
    // 使用 Mock 数据（不依赖 ViewModel）
    val recommendationState = RecommendationState.Loading
    val libraryState = LibraryState.Loading
    
    // ====== 精美的 Mock 数据 ======
    val mockResources = remember {
        listOf(
            Resource(
                id = -1,
                title = "【动画推荐】葬送的芙莉莲",
                url = "https://www.bilibili.com/video/BV1example",
                description = "史诗级奇幻冒险动画，豆瓣9.5分神作",
                imageUrl = "https://i0.hdslb.com/bfs/archive/placeholder.jpg",
                type = ResourceType.WebLink,
                category = ResourceCategory.ENTERTAINMENT,
                tags = listOf("动画", "B站", "娱乐")
            ),
            Resource(
                id = -2,
                title = "多邻国 - 学习语言",
                url = "com.duolingo",
                description = "全球最受欢迎的语言学习 App",
                type = ResourceType.AppLaunch("com.duolingo"),
                category = ResourceCategory.STUDY,
                tags = listOf("学习", "语言", "应用")
            ),
            Resource(
                id = -3,
                title = "高等数学复习大纲",
                url = "",
                description = "第一章：极限与连续 | 第二章：导数与微分",
                type = ResourceType.TaskMemo,
                category = ResourceCategory.STUDY,
                tags = listOf("学习", "数学", "待办")
            )
        )
    }
    
    // 核心功能 - 升级配色方案
    val coreFeatures = listOf(
        DashboardItem(
            id = "collection",
            title = "我的收藏",
            subtitle = "你的数字记忆",
            icon = Icons.Default.Favorite,
            iconColor = Color(0xFFFF2D55),       // 鲜艳红色
            backgroundColor = Color(0xFFFFE5EA),  // 浅红背景
            route = "collection"
        ),
        DashboardItem(
            id = "notebook",
            title = "我的笔记本",
            subtitle = "轻量速记",
            icon = Icons.Default.EditNote,
            iconColor = Color(0xFF42A5F5),       // 蓝色
            backgroundColor = Color(0xFFE3F2FD),  // 浅蓝背景
            route = NavigationRoutes.NOTEBOOK
        )
    )



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBrush)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero 卡片 - 带入场动画
            item {
                AnimatedCard(index = 0) {
                    when (val state = recommendationState) {
                        is RecommendationState.Success -> {
                            HeroCard(
                                contextMessage = state.result.contextMessage,
                                categoryName = state.result.currentCategory?.displayName
                            )
                        }
                        else -> {
                            HeroCard()
                        }
                    }
                }
            }

            // 动态推荐区（真实数据或 Mock 数据）
            when (val state = recommendationState) {
                is RecommendationState.Success -> {
                    if (state.result.recommendedResources.isNotEmpty()) {
                        item {
                            SectionTitle(
                                title = "为你推荐",
                                subtitle = state.result.currentCategory?.displayName ?: "当前时段"
                            )
                        }

                        itemsIndexed(state.result.recommendedResources.take(4)) { index, resource ->
                            AnimatedCard(index = index + 1) {
                                RecommendedResourceCard(
                                    resource = resource,
                                    onClick = {
                                        ResourceLauncher.launch(context, resource)
                                    }
                                )
                            }
                        }
                    } else {
                        // 如果没有真实数据，显示 Mock 数据
                        item {
                            SectionTitle(
                                title = "精选内容",
                                subtitle = "演示数据"
                            )
                        }
                        
                        itemsIndexed(mockResources) { index, resource ->
                            AnimatedCard(index = index + 1) {
                                RecommendedResourceCard(
                                    resource = resource,
                                    onClick = {
                                        // Mock 数据不执行实际操作
                                    }
                                )
                            }
                        }
                    }
                }
                else -> {
                    // 加载中或失败，也显示 Mock 数据
                    item {
                        SectionTitle(
                            title = "精选内容",
                            subtitle = "演示数据"
                        )
                    }
                    
                    itemsIndexed(mockResources) { index, resource ->
                        AnimatedCard(index = index + 1) {
                            RecommendedResourceCard(
                                resource = resource,
                                onClick = { }
                            )
                        }
                    }
                }
            }

            // 核心功能区
            item {
                SectionTitle(title = "核心功能")
            }

            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(360.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false
                ) {
                    items(coreFeatures.size) { index ->
                        AnimatedCard(index = index + 10) {
                            DashboardCard(
                                item = coreFeatures[index],
                                onClick = { onNavigate(coreFeatures[index].route) }
                            )
                        }
                    }
                }
            }

            // 底部留白
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

/**
 * 带入场动画的卡片包装器
 */
@Composable
fun AnimatedCard(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay((index * 50L).coerceAtMost(300L))
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 400, easing = EaseOut)
        ) + slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = tween(durationMillis = 400, easing = EaseOutCubic)
        )
    ) {
        content()
    }
}

/**
 * Hero 卡片 - 顶部欢迎卡片（线性渐变+白色文字+阴影+Lottie动画）
 */
@Composable
fun HeroCard(
    contextMessage: String? = null,
    categoryName: String? = null
) {
    val greetings = listOf(
        "人生得意须尽欢，莫使金樽空对月。",
        "路漫漫其修远兮，吾将上下而求索。",
        "海内存知己，天涯若比邻。",
        "长风破浪会有时，直挂云帆济沧海。",
        "不积跬步，无以至千里。",
        "千里之行，始于足下。",
        "业精于勤，荒于嬉；行成于思，毁于随。"
    )
    
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val timeGreeting = when (currentHour) {
        in 0..5 -> "深夜好"
        in 6..11 -> "早上好"
        in 12..13 -> "中午好"
        in 14..18 -> "下午好"
        else -> "晚上好"
    }

    val randomQuote = remember { greetings.random() }
    val displayMessage = contextMessage ?: randomQuote

    // Lottie 动画
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(com.universalbox.app.R.raw.anim_space_man)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp) // 增加高度以容纳动画
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = AppTheme.Colors.PrimaryColor.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AppTheme.Colors.PrimaryColor,  // 使用主题色
                            Color(0xFF8B5CF6)   // 淡紫色（右下）
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧文字
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeGreeting,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (categoryName != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = categoryName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = displayMessage,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 20.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 右侧 Lottie 动画
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(100.dp)
                )
            }
        }
    }
}

/**
 * 推荐资源卡片 - 优化圆角 + 果冻按钮效果
 */
@Composable
fun RecommendedResourceCard(
    resource: Resource,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClick(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 封面图片
            if (resource.imageUrl.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = resource.imageUrl,
                    contentDescription = "资源封面",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AppTheme.Colors.InputBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AppTheme.Colors.InputBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppTheme.Colors.InputBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            resource.isApp() -> Icons.Default.Apps
                            resource.isTask() -> Icons.Default.Task
                            else -> Icons.Default.Link
                        },
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }

            // 资源信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = resource.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.Colors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (resource.description.isNotBlank()) {
                    Text(
                        text = resource.description,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // 显示使用次数
                if (resource.usageWeight > 0) {
                    Text(
                        text = "使用 ${resource.usageWeight} 次",
                        fontSize = 11.sp,
                        color = Color(0xFF007AFF)
                    )
                }
            }

            // 箭头
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Section 标题组件
 */
@Composable
fun SectionTitle(
    title: String,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.Colors.TextPrimary
        )
        if (subtitle != null) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF007AFF).copy(alpha = 0.1f)
            ) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF007AFF),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Dashboard 卡片组件 - 彩色图标 + 浅色背景泡泡 + 果冻按钮效果
 */
@Composable
fun DashboardCard(
    item: DashboardItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .bouncyClick(
                enabled = item.enabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.enabled) Color.White else Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 左上角图标 - 彩色图标 + 浅色背景泡泡
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (item.enabled) item.backgroundColor else AppTheme.Colors.InputBackground
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (item.enabled) item.iconColor else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // 左下角文字
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.enabled) AppTheme.Colors.TextPrimary else Color.Gray
                )
                Text(
                    text = if (item.enabled) item.subtitle else "开发中",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
