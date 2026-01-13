package com.universalbox.app.ui.screens.ocr

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.universalbox.app.ui.components.strongBouncyClick
import com.universalbox.app.ui.components.IOSTopBar
import com.universalbox.app.ui.components.IOSCard
import com.universalbox.app.ui.theme.AppTheme
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

/**
 * OCR 文字识别页面 - iOS Style
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OCRScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var recognizedText by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    // 临时图片文件（用于拍照）
    val tempImageFile = remember {
        File(context.cacheDir, "ocr_temp_image.jpg")
    }
    val tempImageUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempImageFile
        )
    }
    
    // 拍照启动器
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = tempImageUri
            val bitmap = BitmapFactory.decodeFile(tempImageFile.absolutePath)
            selectedBitmap = bitmap
            recognizeText(context, bitmap, view) { text ->
                recognizedText = text
                isScanning = false
            }
            isScanning = true
        }
    }
    
    // 选择图片启动器
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            try {
                val bitmap = uriToBitmap(context, it)
                selectedBitmap = bitmap
                recognizeText(context, bitmap, view) { text ->
                    recognizedText = text
                    isScanning = false
                }
                isScanning = true
            } catch (e: IOException) {
                Toast.makeText(context, "无法加载图片", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // 相机权限请求
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(tempImageUri)
        } else {
            Toast.makeText(context, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 存储权限请求
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show()
        }
    }
    
    Scaffold(
        floatingActionButton = {
            if (recognizedText.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(recognizedText))
                        Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                    },
                    containerColor = AppTheme.Colors.PrimaryColor,
                    shape = RoundedCornerShape(AppTheme.Shapes.ButtonShape.topStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "复制",
                        tint = Color.White
                    )
                }
            }
        },
        containerColor = AppTheme.Colors.AppBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // iOS 风格顶部栏
            IOSTopBar(
                title = "OCR 文字识别",
                onBack = onBack
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppTheme.Spacing.Medium, vertical = AppTheme.Spacing.Small),
                verticalArrangement = Arrangement.spacedBy(AppTheme.Spacing.Medium)
            ) {
                // 预览区
                ImagePreviewSection(
                    bitmap = selectedBitmap,
                    isScanning = isScanning,
                    onClick = { showImageSourceDialog = true }
                )
                
                // 结果区
                RecognizedTextSection(
                    text = recognizedText,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    
    // 图片来源选择对话框
    if (showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showImageSourceDialog = false },
            onTakePhoto = {
                showImageSourceDialog = false
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onPickFromGallery = {
                showImageSourceDialog = false
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        )
    }
}

/**
 * 图片预览区 - iOS Card Style
 */
@Composable
fun ImagePreviewSection(
    bitmap: Bitmap?,
    isScanning: Boolean,
    onClick: () -> Unit
) {
    IOSCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "选中的图片",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                // 扫描中的遮罩
                if (isScanning) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(AppTheme.Spacing.Medium)
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "扫描中...",
                                style = AppTheme.Typography.Body,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                // 空状态
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppTheme.Spacing.Medium),
                    modifier = Modifier.padding(AppTheme.Spacing.ExtraLarge)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .border(
                                width = 2.dp,
                                color = AppTheme.Colors.TextSecondary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(AppTheme.Shapes.ButtonShape.topStart)
                            )
                            .clip(RoundedCornerShape(AppTheme.Shapes.ButtonShape.topStart))
                            .background(AppTheme.Colors.TextSecondary.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = AppTheme.Colors.TextSecondary.copy(alpha = 0.4f)
                        )
                    }
                    
                    Text(
                        text = "点击上传 / 拍照",
                        style = AppTheme.Typography.Body,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.Colors.TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * 识别结果区 - iOS Card Style
 */
@Composable
fun RecognizedTextSection(
    text: String,
    modifier: Modifier = Modifier
) {
    IOSCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppTheme.Spacing.Medium)
        ) {
            Text(
                text = "识别结果",
                style = AppTheme.Typography.Headline,
                fontWeight = FontWeight.Bold,
                color = AppTheme.Colors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(AppTheme.Spacing.Medium))
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = AppTheme.Colors.InputBackground,
                        shape = RoundedCornerShape(AppTheme.Shapes.InputShape.topStart)
                    )
                    .padding(AppTheme.Spacing.Medium)
            ) {
                if (text.isNotEmpty()) {
                    Text(
                        text = text,
                        style = AppTheme.Typography.Body,
                        color = AppTheme.Colors.TextPrimary,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                } else {
                    Text(
                        text = "识别到的文字将显示在这里",
                        style = AppTheme.Typography.Body,
                        color = AppTheme.Colors.TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * 图片来源选择对话框
 */
@Composable
fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择图片来源",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onTakePhoto,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("拍照", fontSize = 16.sp)
                }
                
                TextButton(
                    onClick = onPickFromGallery,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("从相册选择", fontSize = 16.sp)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 使用 ML Kit 识别图片中的文字
 */
fun recognizeText(
    context: Context,
    bitmap: Bitmap,
    view: android.view.View,
    onResult: (String) -> Unit
) {
    val image = InputImage.fromBitmap(bitmap, 0)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            val resultText = visionText.text
            if (resultText.isNotEmpty()) {
                // 震动反馈
                view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                onResult(resultText)
            } else {
                onResult("未识别到文字")
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "识别失败: ${e.message}", Toast.LENGTH_SHORT).show()
            onResult("")
        }
}

/**
 * 将 URI 转换为 Bitmap
 */
fun uriToBitmap(context: Context, uri: Uri): Bitmap {
    val inputStream = context.contentResolver.openInputStream(uri)
    return BitmapFactory.decodeStream(inputStream)
}
