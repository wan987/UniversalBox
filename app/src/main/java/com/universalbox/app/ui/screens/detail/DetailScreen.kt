package com.universalbox.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.platform.LocalContext
import com.universalbox.app.data.local.AppDatabase
import com.universalbox.app.data.repository.ResourceRepositoryImpl
import com.universalbox.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DetailViewModel = viewModel(
        factory = DetailViewModelFactory(
            itemId = itemId,
            repository = ResourceRepositoryImpl(
                favoriteDao = AppDatabase.getDatabase(context).favoriteDao()
            )
        )
    )

    val item by viewModel.favoriteItem.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 编辑状态的临时变量
    var editedTitle by remember { mutableStateOf("") }
    var editedDescription by remember { mutableStateOf("") }
    var editedTags by remember { mutableStateOf("") }

    // 当数据加载完成后，初始化编辑字段
    LaunchedEffect(item) {
        item?.let {
            editedTitle = it.title
            editedDescription = it.description
            editedTags = it.tags.joinToString(", ")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("收藏详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 删除按钮
                    IconButton(onClick = {
                        item?.let { viewModel.deleteFavorite(it) }
                        onBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Red)
                    }
                    // 保存按钮
                    IconButton(onClick = {
                        item?.let { currentItem ->
                            viewModel.updateFavorite(
                                currentItem.copy(
                                    id = currentItem.id, // 显式保留原始 ID，防止丢失
                                    title = editedTitle,
                                    description = editedDescription,
                                    tags = editedTags.split(",").map { tag -> tag.trim() }.filter { tag -> tag.isNotEmpty() }
                                )
                            )
                        }
                        onBack()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading || item == null) {
            // 加载中
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // 显示详情和编辑表单
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF2F2F7))
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 封面图片
                if (item!!.imageUrl.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        SubcomposeAsyncImage(
                            model = item!!.imageUrl,
                            contentDescription = "封面图",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(AppTheme.Colors.InputBackground),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("图片加载失败", color = Color.Gray)
                                }
                            }
                        )
                    }
                }

                // 网站信息
                if (item!!.siteName.isNotBlank()) {
                    Text(
                        text = item!!.siteName,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                // 标题编辑框
                OutlinedTextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    label = { Text("标题") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // 描述编辑框
                OutlinedTextField(
                    value = editedDescription,
                    onValueChange = { editedDescription = it },
                    label = { Text("描述") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // 标签编辑框
                OutlinedTextField(
                    value = editedTags,
                    onValueChange = { editedTags = it },
                    label = { Text("标签（逗号分隔）") },
                    placeholder = { Text("例如: 技术,学习,开源") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // URL（只读）
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "链接",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item!!.url,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 创建时间
                Text(
                    text = "创建时间: ${formatTimestamp(item!!.createTime)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// 时间格式化工具函数
private fun formatTimestamp(timestamp: Long): String {
    val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    return date.format(java.util.Date(timestamp))
}
