package com.universalbox.app.ui.screens.tools

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.universalbox.app.ui.components.bouncyClick
import com.universalbox.app.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * DecisionMakerScreen - 幸运大转盘
 * 
 * 帮助用户做决定的趣味工具
 * 特性：
 * - 彩色转盘可视化
 * - 物理衰减动画
 * - 触觉反馈
 * - 自定义选项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisionMakerScreen(
    onBack: () -> Unit
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    
    // 选项列表
    var options by remember {
        mutableStateOf(listOf("外卖", "食堂", "减肥", "自己做"))
    }
    var newOption by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    
    // 大标题状态
    var questionTitle by remember { mutableStateOf("今天吃什么？") }
    var showEditTitleDialog by remember { mutableStateOf(false) }
    
    // 转盘状态
    var rotation by remember { mutableStateOf(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    
    // 颜色列表
    val colors = listOf(
        Color(0xFFFF6B6B), // 红
        Color(0xFF4ECDC4), // 青
        Color(0xFFFFBE0B), // 橙
        Color(0xFF95E1D3), // 薄荷绿
        Color(0xFFF38181), // 粉红
        Color(0xFFAA96DA), // 紫
        Color(0xFFFCBF49), // 黄
        Color(0xFF06BEE1)  // 蓝
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("帮我决定") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "添加选项")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.Colors.AppBackground)
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 艺术字大标题（可点击编辑）
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .bouncyClick(onClick = { showEditTitleDialog = true }),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    AppTheme.Colors.PrimaryColor,
                                    Color(0xFF8B5CF6),
                                    Color(0xFFEC4899)
                                )
                            )
                        )
                        .padding(vertical = 24.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = questionTitle,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp,
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (result != null) "✨ 结果：$result ✨" else "点击 GO 开始转动",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (result != null) AppTheme.Colors.PrimaryColor else Color.Gray
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 转盘
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // 转盘指示器 (顶部三角形)
                Canvas(
                    modifier = Modifier
                        .size(40.dp)
                        .offset(y = (-150).dp)
                ) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width / 2, size.height)
                        lineTo(0f, 0f)
                        lineTo(size.width, 0f)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFFFF6B6B)
                    )
                }
                
                // 转盘本体
                SpinWheel(
                    options = options,
                    colors = colors,
                    rotation = rotation,
                    modifier = Modifier.size(280.dp)
                )
                
                // 中心 GO 按钮
                Button(
                    onClick = {
                        if (!isSpinning && options.isNotEmpty()) {
                            isSpinning = true
                            result = null
                            
                            scope.launch {
                                // 震动反馈
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                
                                // 随机目标角度 (5-10 圈 + 随机偏移)
                                val spins = (5..10).random()
                                val randomOffset = (0..360).random().toFloat()
                                val targetRotation = rotation + (spins * 360f) + randomOffset
                                
                                // 衰减动画
                                val startRotation = rotation
                                val duration = 3000L // 3秒
                                val startTime = System.currentTimeMillis()
                                
                                while (System.currentTimeMillis() - startTime < duration) {
                                    val elapsed = System.currentTimeMillis() - startTime
                                    val progress = elapsed / duration.toFloat()
                                    
                                    // 使用 ease-out 曲线模拟摩擦力
                                    val easeOut = 1 - (1 - progress) * (1 - progress) * (1 - progress)
                                    rotation = startRotation + (targetRotation - startRotation) * easeOut
                                    
                                    // 速度越快，震动越频繁
                                    if (progress < 0.7f && elapsed % 100 < 50) {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    }
                                    
                                    delay(16) // 60fps
                                }
                                
                                rotation = targetRotation % 360f
                                
                                // 计算结果
                                val anglePerOption = 360f / options.size
                                val adjustedRotation = (360f - (rotation % 360f) + 90f) % 360f
                                val selectedIndex = (adjustedRotation / anglePerOption).toInt() % options.size
                                result = options[selectedIndex]
                                
                                // 结果震动
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                
                                isSpinning = false
                            }
                        }
                    },
                    enabled = !isSpinning && options.isNotEmpty(),
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = AppTheme.Colors.PrimaryColor
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Text(
                        text = if (isSpinning) "⏳" else "GO",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 选项列表
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "当前选项 (${options.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (options.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "还没有选项，点击 + 添加",
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(options) { index, option ->
                                OptionItem(
                                    option = option,
                                    color = colors[index % colors.size],
                                    onDelete = {
                                        if (options.size > 1) {
                                            options = options.filterIndexed { i, _ -> i != index }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 添加选项对话框
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加新选项") },
            text = {
                OutlinedTextField(
                    value = newOption,
                    onValueChange = { newOption = it },
                    label = { Text("选项名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newOption.isNotBlank() && options.size < 8) {
                            options = options + newOption.trim()
                            newOption = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    newOption = ""
                    showAddDialog = false 
                }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 编辑标题对话框
    if (showEditTitleDialog) {
        var editingTitle by remember { mutableStateOf(questionTitle) }
        
        AlertDialog(
            onDismissRequest = { showEditTitleDialog = false },
            title = { 
                Text(
                    "编辑问题",
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                OutlinedTextField(
                    value = editingTitle,
                    onValueChange = { editingTitle = it },
                    label = { Text("输入你的问题") },
                    placeholder = { Text("例如：今天吃什么？") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editingTitle.isNotBlank()) {
                            questionTitle = editingTitle.trim()
                            showEditTitleDialog = false
                        }
                    }
                ) {
                    Text("确定", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTitleDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 转盘组件
 */
@Composable
private fun SpinWheel(
    options: List<String>,
    colors: List<Color>,
    rotation: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .rotate(rotation)
            .border(4.dp, Color.White, CircleShape)
            .clip(CircleShape)
    ) {
        if (options.isEmpty()) return@Canvas
        
        val anglePerOption = 360f / options.size
        
        options.forEachIndexed { index, option ->
            val startAngle = anglePerOption * index
            val color = colors[index % colors.size]
            
            // 绘制扇形
            drawArc(
                color = color,
                startAngle = startAngle - 90f,
                sweepAngle = anglePerOption,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            
            // 绘制分割线
            drawArc(
                color = Color.White,
                startAngle = startAngle - 90f,
                sweepAngle = anglePerOption,
                useCenter = true,
                style = Stroke(width = 3f),
                size = Size(size.width, size.height)
            )
        }
        
        // 中心圆圈装饰
        drawCircle(
            color = Color.White,
            radius = size.minDimension * 0.15f / 2,
            center = center
        )
    }
}

/**
 * 选项卡片
 */
@Composable
private fun OptionItem(
    option: String,
    color: Color,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClick(onClick = {}),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color, CircleShape)
                )
                
                Text(
                    text = option,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
