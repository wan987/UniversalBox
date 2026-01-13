package com.universalbox.app.ui.screens.home

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.universalbox.app.data.local.AppDatabase
import com.universalbox.app.data.repository.ResourceRepositoryImpl

/**
 * 添加资源的 ModalBottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddResourceSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AddResourceViewModel = viewModel(
        factory = AddResourceViewModelFactory(
            repository = ResourceRepositoryImpl(
                favoriteDao = AppDatabase.getDatabase(context).favoriteDao()
            ),
            context = context.applicationContext
        )
    )
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("添加链接", "添加应用")
    
    val installedApps by viewModel.installedApps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // 加载已安装应用列表
    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps()
    }

    // 显示错误消息
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            // 可以在这里显示 Snackbar
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "添加资源",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }

            // Tab 选项卡
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                        icon = {
                            Icon(
                                imageVector = if (index == 0) Icons.Default.Link else Icons.Default.Apps,
                                contentDescription = title
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab 内容
            when (selectedTab) {
                0 -> AddLinkTab(
                    isLoading = isLoading,
                    onAddLink = { url, title ->
                        viewModel.addLinkFromUrl(url, title) {
                            onDismiss()
                        }
                    }
                )
                1 -> AddAppTab(
                    apps = installedApps,
                    isLoading = isLoading,
                    onAddApp = { app ->
                        viewModel.addApp(app) {
                            onDismiss()
                        }
                    }
                )
            }
        }
    }
}

/**
 * 添加链接的 Tab
 */
@Composable
fun AddLinkTab(
    isLoading: Boolean,
    onAddLink: (url: String, title: String?) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        // URL 输入框
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("网址") },
            placeholder = { Text("https://example.com") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 标题输入框（可选）
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("标题（可选）") },
            placeholder = { Text("留空将自动抓取网页标题") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 添加按钮
        Button(
            onClick = {
                if (url.isNotBlank()) {
                    onAddLink(url.trim(), title.takeIf { it.isNotBlank() }?.trim())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = url.isNotBlank() && !isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("添加链接", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 提示文本
        Text(
            text = "将自动抓取网页的标题、描述和图标",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

/**
 * 添加应用的 Tab
 */
@Composable
fun AddAppTab(
    apps: List<InstalledApp>,
    isLoading: Boolean,
    onAddApp: (InstalledApp) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // 过滤应用列表
    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isBlank()) {
            apps
        } else {
            apps.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 搜索框
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("搜索应用") },
            placeholder = { Text("输入应用名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 应用列表
        if (isLoading && apps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("正在加载应用列表...", color = Color.Gray)
                }
            }
        } else if (filteredApps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "没有找到应用" else "没有匹配的应用",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps) { app ->
                    AppListItem(
                        app = app,
                        onClick = { onAddApp(app) }
                    )
                }
            }
        }
    }
}

/**
 * 应用列表项
 */
@Composable
fun AppListItem(
    app: InstalledApp,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 应用图标
            app.icon?.let { drawable ->
                val bitmap: Bitmap = drawable.toBitmap()
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = app.appName,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } ?: run {
                // 默认图标占位符
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Apps,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 应用信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = app.packageName,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}
