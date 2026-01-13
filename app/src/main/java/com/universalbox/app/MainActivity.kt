package com.universalbox.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.universalbox.app.navigation.NavigationRoutes
import com.universalbox.app.ui.screens.dashboard.DashboardScreen
import com.universalbox.app.ui.screens.detail.DetailScreen
import com.universalbox.app.ui.screens.home.HomeScreen
import com.universalbox.app.ui.screens.ocr.OCRScreen
import com.universalbox.app.ui.screens.pomodoro.PomodoroScreen
import com.universalbox.app.ui.screens.qrcode.QRCodeScreen
import com.universalbox.app.ui.screens.splash.SplashScreen
import com.universalbox.app.ui.screens.tools.ZenClockScreen
import com.universalbox.app.ui.screens.tools.DecisionMakerScreen
import com.universalbox.app.ui.screens.tools.ToolsScreen
import com.universalbox.app.ui.screens.notebook.NotebookScreen
import com.universalbox.app.ui.screens.schedule.ScheduleScreen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.platform.LocalLayoutDirection
import com.universalbox.app.ui.theme.UniversalBoxTheme
import androidx.navigation.compose.currentBackStackEntryAsState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 检查是不是从"分享"菜单进来的
        val sharedText: String? =
            if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
                intent.getStringExtra(Intent.EXTRA_TEXT)
            } else {
                null
            }

        setContent {
            UniversalBoxTheme {
                // 创建导航控制器
                val homeNavController = rememberNavController()
                val toolsNavController = rememberNavController()

                // 是否显示启动页
                var showSplash by remember { mutableStateOf(sharedText == null) }

                if (showSplash) {
                    // 显示启动页动画
                    SplashScreen(
                        onSplashFinished = {
                            showSplash = false
                        }
                    )
                } else {
                    // 主界面 - 带底部导航栏
                    MainScreenWithBottomNav(
                        homeNavController = homeNavController,
                        toolsNavController = toolsNavController,
                        sharedText = sharedText
                    )
                }
            }
        }
    }
}

/**
 * 底部导航项
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home_tab",
        title = "首页",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    object Tools : BottomNavItem(
        route = "tools_tab",
        title = "工具",
        selectedIcon = Icons.Filled.Build,
        unselectedIcon = Icons.Outlined.Build
    )
    
    object Schedule : BottomNavItem(
        route = "schedule_tab",
        title = "日程",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange
    )
}

/**
 * 主界面 - 包含底部导航栏
 */
@Composable
fun MainScreenWithBottomNav(
    homeNavController: androidx.navigation.NavHostController,
    toolsNavController: androidx.navigation.NavHostController,
    sharedText: String?
) {
    val items = listOf(BottomNavItem.Home, BottomNavItem.Tools, BottomNavItem.Schedule)
    var selectedTab by remember { mutableIntStateOf(0) }
    val activeController = when (selectedTab) {
        0 -> homeNavController
        1 -> toolsNavController
        else -> null
    }
    val navBackStackEntry by (activeController ?: homeNavController).currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val hideBottomBar = currentRoute == NavigationRoutes.ZEN_CLOCK

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (!hideBottomBar) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val adjustedPadding = PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            top = paddingValues.calculateTopPadding(),
            end = paddingValues.calculateEndPadding(layoutDirection),
            bottom = 0.dp
        )

        Box(modifier = Modifier.padding(adjustedPadding)) {
            when (selectedTab) {
                0 -> {
                    // 首页 Tab
                    NavHost(
                        navController = homeNavController,
                        startDestination = if (sharedText != null) NavigationRoutes.COLLECTION else NavigationRoutes.DASHBOARD
                    ) {
                        // 首页 - Dashboard
                        composable(NavigationRoutes.DASHBOARD) {
                            DashboardScreen(
                                onNavigate = { route -> homeNavController.navigate(route) }
                            )
                        }

                        // 我的笔记本
                        composable(NavigationRoutes.NOTEBOOK) {
                            NotebookScreen(onBack = { homeNavController.navigateUp() })
                        }

                        // 我的收藏
                        composable(NavigationRoutes.COLLECTION) {
                            HomeScreen(
                                sharedUrl = sharedText,
                                onNavigateToDetail = { itemId ->
                                    homeNavController.navigate(NavigationRoutes.collectionDetail(itemId))
                                },
                                onBack = {
                                    if (homeNavController.previousBackStackEntry != null) {
                                        homeNavController.navigateUp()
                                    } else {
                                        homeNavController.navigate(NavigationRoutes.DASHBOARD) {
                                            popUpTo(NavigationRoutes.DASHBOARD) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                        }

                        // 收藏详情
                        composable(
                            route = NavigationRoutes.COLLECTION_DETAIL,
                            arguments = listOf(
                                navArgument("itemId") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
                            DetailScreen(
                                itemId = itemId,
                                onBack = { homeNavController.navigateUp() }
                            )
                        }
                    }
                }
                1 -> {
                    // 工具 Tab
                    NavHost(
                        navController = toolsNavController,
                        startDestination = "tools_main"
                    ) {
                        // 工具主页
                        composable("tools_main") {
                            ToolsScreen(
                                onNavigateToPomodoro = {
                                    toolsNavController.navigate(NavigationRoutes.POMODORO)
                                },
                                onNavigateToOCR = {
                                    toolsNavController.navigate(NavigationRoutes.OCR)
                                },
                                onNavigateToQRCode = {
                                    toolsNavController.navigate(NavigationRoutes.QRCODE)
                                },
                                onNavigateToZenClock = {
                                    toolsNavController.navigate(NavigationRoutes.ZEN_CLOCK)
                                },
                                onNavigateToDecisionMaker = {
                                    toolsNavController.navigate(NavigationRoutes.DECISION_MAKER)
                                }
                            )
                        }

                        // 番茄钟
                        composable(NavigationRoutes.POMODORO) {
                            PomodoroScreen(onBack = { toolsNavController.navigateUp() })
                        }

                        // OCR 文字识别
                        composable(NavigationRoutes.OCR) {
                            OCRScreen(onBack = { toolsNavController.navigateUp() })
                        }

                        // 二维码工坊
                        composable(NavigationRoutes.QRCODE) {
                            QRCodeScreen(onBack = { toolsNavController.navigateUp() })
                        }

                        // 禅意时钟
                        composable(NavigationRoutes.ZEN_CLOCK) {
                            ZenClockScreen(onBack = { toolsNavController.navigateUp() })
                        }

                        // 帮我决定
                        composable(NavigationRoutes.DECISION_MAKER) {
                            DecisionMakerScreen(onBack = { toolsNavController.navigateUp() })
                        }
                    }
                }
                2 -> {
                    // 日程 Tab
                    ScheduleScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "功能开发中，敬请期待...",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Gray
            )
        }
    }
}
