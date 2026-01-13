package com.universalbox.app.ui.screens.qrcode

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.universalbox.app.ui.components.IOSTopBar
import com.universalbox.app.ui.components.IOSCard
import com.universalbox.app.ui.components.IOSTextField
import com.universalbox.app.ui.theme.AppTheme

/**
 * 二维码工坊 - iOS Style
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("📷 扫描", "✏️ 制码")

    Scaffold(
        containerColor = AppTheme.Colors.AppBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // iOS 顶部栏
            IOSTopBar(
                title = "二维码工坊",
                onBack = onBack
            )
            
            // Tab 切换
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = AppTheme.Colors.AppBackground,
                contentColor = AppTheme.Colors.PrimaryColor
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = AppTheme.Typography.Body,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab 内容
            when (selectedTab) {
                0 -> ScanTab(context, view)
                1 -> GenerateTab(context)
            }
        }
    }
}

/**
 * Tab 1: 扫描二维码
 */
@Composable
fun ScanTab(context: Context, view: android.view.View) {
    var scanResult by remember { mutableStateOf<String?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }

    // 相机权限请求
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "需要相机权限才能扫描二维码", Toast.LENGTH_SHORT).show()
        }
    }

    // 二维码扫描启动器
    val scanLauncher = rememberLauncherForActivityResult(
        ScanContract()
    ) { result ->
        if (result.contents != null) {
            scanResult = result.contents
            showResultDialog = true
            // 震动反馈
            view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppTheme.Spacing.Medium),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.Spacing.Large)
        ) {
            // 扫描图标
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = AppTheme.Colors.PrimaryColor
            )

            Text(
                text = "点击按钮开始扫描",
                style = AppTheme.Typography.Headline,
                fontWeight = FontWeight.Medium,
                color = AppTheme.Colors.TextPrimary
            )

            // 扫描按钮
            Button(
                onClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    val options = ScanOptions().apply {
                        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        setPrompt("将二维码放入扫描框内")
                        setBeepEnabled(false)
                        setOrientationLocked(true)
                    }
                    scanLauncher.launch(options)
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.Colors.PrimaryColor
                ),
                shape = RoundedCornerShape(AppTheme.Shapes.ButtonShape.topStart)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(AppTheme.Spacing.Small))
                Text(
                    text = "开始扫描",
                    style = AppTheme.Typography.Body,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // 扫描结果对话框
    if (showResultDialog && scanResult != null) {
        ScanResultDialog(
            result = scanResult!!,
            onDismiss = { showResultDialog = false },
            context = context
        )
    }
}

/**
 * 扫描结果对话框
 */
@Composable
fun ScanResultDialog(
    result: String,
    onDismiss: () -> Unit,
    context: Context
) {
    val isUrl = result.startsWith("http://") || result.startsWith("https://")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "扫描结果",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = result,
                    fontSize = 14.sp,
                    color = AppTheme.Colors.TextPrimary
                )
                if (isUrl) {
                    Text(
                        text = "检测到网址链接",
                        fontSize = 12.sp,
                        color = AppTheme.Colors.PrimaryColor
                    )
                }
            }
        },
        confirmButton = {
            if (isUrl) {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result))
                    context.startActivity(intent)
                    onDismiss()
                }) {
                    Text("打开链接")
                }
            } else {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("QR Code", result))
                    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }) {
                    Text("复制文本")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * Tab 2: 生成二维码
 */
@Composable
fun GenerateTab(context: Context) {
    var generateMode by remember { mutableIntStateOf(0) } // 0: 文本/链接, 1: WiFi
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(AppTheme.Spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(AppTheme.Spacing.Medium)
    ) {
        // 模式切换
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.Spacing.Small)
        ) {
            FilterChip(
                selected = generateMode == 0,
                onClick = { generateMode = 0 },
                label = { Text("文本/链接") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppTheme.Colors.PrimaryColor,
                    selectedLabelColor = Color.White
                )
            )

            FilterChip(
                selected = generateMode == 1,
                onClick = { generateMode = 1 },
                label = { Text("WiFi 分享") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppTheme.Colors.PrimaryColor,
                    selectedLabelColor = Color.White
                )
            )
        }

        // 根据模式显示不同内容
        when (generateMode) {
            0 -> TextGenerateMode(context)
            1 -> WiFiGenerateMode(context)
        }
    }
}

/**
 * 模式 A: 文本/链接生成 - iOS Style
 */
@Composable
fun TextGenerateMode(context: Context) {
    var inputText by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // 实时生成二维码
    LaunchedEffect(inputText) {
        if (inputText.isNotEmpty()) {
            qrBitmap = generateQRCode(inputText, 512)
        } else {
            qrBitmap = null
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(AppTheme.Spacing.Medium)
    ) {
        // 输入框 - iOS 风格
        IOSTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = "输入文本或链接",
            placeholder = "https://example.com",
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        // 二维码展示
        AnimatedVisibility(visible = qrBitmap != null) {
            QRCodePolaroidCard(bitmap = qrBitmap, subtitle = "扫码查看内容")
        }
    }
}

/**
 * 模式 B: WiFi 分享生成 - iOS Style
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiFiGenerateMode(context: Context) {
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var encryptionType by remember { mutableStateOf("WPA") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var expanded by remember { mutableStateOf(false) }

    // 生成 WiFi 二维码
    fun generateWiFiQR() {
        if (ssid.isNotEmpty()) {
            val wifiString = buildString {
                append("WIFI:S:$ssid;")
                append("T:$encryptionType;")
                if (password.isNotEmpty() && encryptionType != "nopass") {
                    append("P:$password;")
                }
                append(";")
            }
            qrBitmap = generateQRCode(wifiString, 512)
        } else {
            qrBitmap = null
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(AppTheme.Spacing.Medium)
    ) {
        // WiFi 名称
        IOSTextField(
            value = ssid,
            onValueChange = {
                ssid = it
                generateWiFiQR()
            },
            label = "WiFi 名称 (SSID)",
            placeholder = "我的WiFi",
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    Icons.Default.Wifi,
                    contentDescription = null,
                    tint = AppTheme.Colors.TextSecondary
                )
            }
        )

        // 加密方式下拉菜单
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            IOSTextField(
                value = when (encryptionType) {
                    "WPA" -> "WPA/WPA2"
                    "nopass" -> "无密码"
                    else -> encryptionType
                },
                onValueChange = {},
                readOnly = true,
                label = "加密方式",
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("WPA/WPA2") },
                    onClick = {
                        encryptionType = "WPA"
                        expanded = false
                        generateWiFiQR()
                    }
                )
                DropdownMenuItem(
                    text = { Text("无密码") },
                    onClick = {
                        encryptionType = "nopass"
                        expanded = false
                        generateWiFiQR()
                    }
                )
            }
        }

        // WiFi 密码
        if (encryptionType != "nopass") {
            IOSTextField(
                value = password,
                onValueChange = {
                    password = it
                    generateWiFiQR()
                },
                label = "WiFi 密码",
                placeholder = "输入密码",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = AppTheme.Colors.TextSecondary
                    )
                }
            )
        }

        // 二维码展示
        AnimatedVisibility(visible = qrBitmap != null) {
            QRCodePolaroidCard(
                bitmap = qrBitmap,
                subtitle = "扫码自动连接 WiFi"
            )
        }
    }
}

/**
 * 拍立得风格的二维码卡片 - iOS Style
 */
@Composable
fun QRCodePolaroidCard(
    bitmap: Bitmap?,
    subtitle: String
) {
    IOSCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.Spacing.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.Spacing.Medium)
        ) {
            // 二维码图片
            if (bitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color.White)
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // 底部文字（拍立得风格）
            Text(
                text = subtitle,
                style = AppTheme.Typography.Body,
                color = AppTheme.Colors.TextSecondary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 生成二维码的工具函数
 */
fun generateQRCode(content: String, size: Int): Bitmap? {
    return try {
        val hints = hashMapOf<EncodeHintType, Any>()
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        hints[EncodeHintType.MARGIN] = 1

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
