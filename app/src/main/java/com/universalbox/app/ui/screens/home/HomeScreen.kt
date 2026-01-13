package com.universalbox.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.universalbox.app.ui.theme.CollectionBrush
import com.universalbox.app.ui.theme.HomeBrush
import com.universalbox.app.data.local.AppDatabase
import com.universalbox.app.data.repository.ResourceRepositoryImpl
import com.universalbox.app.ui.components.FavoriteCard
import com.universalbox.app.utils.ResourceLauncher
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sharedUrl: String? = null,
    onNavigateToDetail: (Long) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    
    // 创建 Repository 和 ViewModel
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            ResourceRepositoryImpl(
                favoriteDao = AppDatabase.getDatabase(context).favoriteDao()
            )
        )
    )
    // 1. 监听数据库的数据变化 (StateFlow -> State)
    // 只要数据库一变，这个 list 就会自动刷新，界面也会跟着变
    val favoriteList by viewModel.allFavorites.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val allSources by viewModel.allSources.collectAsState()
    val selectedSource by viewModel.selectedSource.collectAsState()

    // 控制 AddResourceSheet 的显示状态
    var showAddResourceSheet by remember { mutableStateOf(false) }

// ★ 2. 自动保存逻辑
    // LaunchedEffect 意味着：只要 sharedUrl 不为空，这段代码就会执行一次
    LaunchedEffect(sharedUrl) {
        sharedUrl?.let { url ->
            viewModel.consumeSharedUrl(url)
        }
    }

    val navigationIcon: @Composable (() -> Unit)? = if (onBack == null) {
        null
    } else {
        {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的收藏") },
                navigationIcon = navigationIcon ?: {}
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddResourceSheet = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CollectionBrush)
                .padding(paddingValues)
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            
            // 顶部大标题（只在没有TopBar时显示）
            if (onBack == null) {
                Text(
                    text = "我的收藏",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(start = 20.dp, top = 60.dp, bottom = 10.dp)
                )
            } else {
                // 有TopBar时只需要小间距
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 来源筛选栏
            if (allSources.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedSource == null,
                            onClick = { viewModel.selectSource(null) },
                            label = { Text("全部来源") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    items(allSources) { source ->
                        FilterChip(
                            selected = selectedSource == source,
                            onClick = { viewModel.selectSource(source) },
                            label = { Text(source) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // 标签筛选栏（横向滚动的药丸按钮）
            if (allTags.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    // "全部" 按钮
                    item {
                        FilterChip(
                            selected = selectedTag == null,
                            onClick = { viewModel.selectTag(null) },
                            label = { Text("全部标签") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    // 各个标签按钮
                    items(allTags) { tag ->
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { viewModel.selectTag(tag) },
                            label = { Text(tag) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // 列表区域
            if (favoriteList.isEmpty()) {
                // 如果没数据，显示空状态
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "还没有收藏，点 + 号添加一个吧", color = Color.Gray)
                }
            } else {
                // 有数据，显示列表
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // items(list) 是 Compose 列表的神器，直接把 List 映射成界面
                    items(
                        items = favoriteList,
                        key = { it.id } // 使用 id 作为 key，提高性能
                    ) { item ->
                        // 删除确认对话框状态
                        var showDeleteDialog by remember { mutableStateOf(false) }
                        
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value: SwipeToDismissBoxValue ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    // 显示确认对话框而不是直接删除
                                    showDeleteDialog = true
                                    false // 不立即确认，等待用户确认
                                } else {
                                    false
                                }
                            },
                            // 增加滑动阈值，需要滑动更远才能触发
                            positionalThreshold = { distance -> distance * 0.75f }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                // 滑动时显示的红色删除背景
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Red, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = false, // 禁止右滑
                            enableDismissFromEndToStart = true   // 只允许左滑删除
                        ) {
                            // 传递完整的数据给卡片组件
                            FavoriteCard(
                                title = item.title,
                                description = item.description.ifBlank { item.url },
                                imageUrl = item.imageUrl,
                                siteName = item.siteName,
                                onClick = {
                                    // 点击直接启动资源
                                    ResourceLauncher.launch(context, item)
                                },
                                onEditClick = {
                                    // 编辑按钮跳转到详情页
                                    onNavigateToDetail(item.id)
                                }
                            )
                        }
                        
                        // 删除确认对话框
                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { 
                                    showDeleteDialog = false
                                },
                                title = { Text("确认删除") },
                                text = { Text("确定要删除 \"${item.title}\" 吗？此操作无法撤销。") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.deleteFavorite(item)
                                            showDeleteDialog = false
                                        }
                                    ) {
                                        Text("删除", color = Color.Red)
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { showDeleteDialog = false }
                                    ) {
                                        Text("取消")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // 显示 AddResourceSheet
        if (showAddResourceSheet) {
            AddResourceSheet(
                onDismiss = { showAddResourceSheet = false }
            )
        }
    }
}