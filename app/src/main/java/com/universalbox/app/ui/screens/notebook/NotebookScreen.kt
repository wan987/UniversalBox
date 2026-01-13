package com.universalbox.app.ui.screens.notebook

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.universalbox.app.ui.theme.NotebookBrush

/**
 * 轻量笔记本：列表 + 全屏编辑
 * 数据暂存内存，后续可挂数据库/云同步。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookScreen(onBack: () -> Unit) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: NotebookViewModel = viewModel(activity)
    val notes = viewModel.notes
    val currentNote = viewModel.editingId?.let { id -> viewModel.getNote(id) }

    if (currentNote != null) {
        NotebookEditorScreen(
            note = currentNote,
            onBack = {
                viewModel.deleteIfEmpty(currentNote.id)
                viewModel.exitEditor()
            },
            onPersist = { title, content, spans, images, tables ->
                viewModel.updateNote(currentNote.id, title, content, spans, images, tables)
            },
            onDelete = {
                viewModel.deleteNote(currentNote.id)
                viewModel.exitEditor()
            },
            onAddImages = { uris -> viewModel.addImages(currentNote.id, uris) },
            onAddTable = { viewModel.addTable(currentNote.id) },
            onUpdateTableCell = { tableId, row, col, value ->
                viewModel.updateTableCell(currentNote.id, tableId, row, col, value)
            },
            onAddTableRow = { tableId -> viewModel.addTableRow(currentNote.id, tableId) },
            onAddTableColumn = { tableId -> viewModel.addTableColumn(currentNote.id, tableId) },
            onDeleteTable = { tableId -> viewModel.deleteTable(currentNote.id, tableId) }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("我的笔记本") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.startNewNote()
                        }) {
                            Icon(Icons.Filled.Add, contentDescription = "新增笔记")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0D47A1),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NotebookBrush)
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("还没有笔记，点右上角 + 添加", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NotebookBrush)
                        .padding(paddingValues),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { viewModel.openNote(note.id) },
                            onDelete = { viewModel.deleteNote(note.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotebookEditorScreen(
    note: NoteItem,
    onBack: () -> Unit,
    onPersist: (title: String, content: String, spans: List<ColorSpan>, images: List<String>, tables: List<NoteTable>) -> Unit,
    onDelete: () -> Unit,
    onAddImages: (List<String>) -> Unit,
    onAddTable: () -> Unit,
    onUpdateTableCell: (tableId: Long, row: Int, col: Int, value: String) -> Unit,
    onAddTableRow: (tableId: Long) -> Unit,
    onAddTableColumn: (tableId: Long) -> Unit,
    onDeleteTable: (tableId: Long) -> Unit
) {
    var title by remember(note.id) { mutableStateOf(note.title) }
    var textValue by remember(note.id) { mutableStateOf(TextFieldValue(note.content)) }
    var colorSpans by remember(note.id) { mutableStateOf(note.colorSpans) }
    var images by remember(note.id) { mutableStateOf(note.images) }
    var tables by remember(note.id) { mutableStateOf(note.tables) }
    var penColor by remember(note.id) { mutableStateOf(Color.Black) }

    val latestPersist by rememberUpdatedState(onPersist)

    LaunchedEffect(note.images) { images = note.images }
    LaunchedEffect(note.tables) { tables = note.tables }
    LaunchedEffect(note.colorSpans) { colorSpans = note.colorSpans }

    // Auto-save on each edit
    LaunchedEffect(title, textValue.text, colorSpans, images, tables) {
        latestPersist(title, textValue.text, colorSpans, images, tables)
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        val list = uris.mapNotNull { it.toString() }
        if (list.isNotEmpty()) {
            images = images + list
            onAddImages(list)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑笔记") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onDelete()
                        onBack()
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "删除")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D47A1),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("标题") },
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            )
            Text(
                text = "最后更新：${note.updatedAt}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))

            ActionRow(
                penColor = penColor,
                onPickImages = { imagePicker.launch("image/*") },
                onAddTable = onAddTable,
                onColorSelected = { color ->
                    applyColorToSelection(color, textValue, colorSpans) { updated ->
                        colorSpans = updated
                    }
                    penColor = color
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (images.isNotEmpty()) {
                ImageRow(images = images, onRemove = { uri ->
                    images = images - uri
                })
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (tables.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    tables.forEach { table ->
                        TableCard(
                            table = table,
                            onCellChange = { r, c, value -> onUpdateTableCell(table.id, r, c, value) },
                            onAddRow = { onAddTableRow(table.id) },
                            onAddColumn = { onAddTableColumn(table.id) },
                            onDelete = { onDeleteTable(table.id) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Rich text editor with color spans
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF7F7F7)
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    value = textValue,
                    onValueChange = { newValue ->
                        textValue = handleTextChange(textValue, newValue, penColor, colorSpans) { spans ->
                            colorSpans = spans
                        }
                    },
                    placeholder = { Text("开始记录，后续可加入图片、表格...") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    visualTransformation = coloredTransformation(colorSpans),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val firstLine = note.content.lineSequence().firstOrNull()?.take(80) ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = note.title.ifBlank { "无标题" },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "删除")
            }
        }
        if (firstLine.isNotBlank()) {
            Text(
                text = firstLine,
                fontSize = 14.sp,
                color = Color(0xFF424242),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(note.updatedAt, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun ActionRow(
    penColor: Color,
    onPickImages: () -> Unit,
    onAddTable: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color.Black,
        Color(0xFFD32F2F),
        Color(0xFF1976D2),
        Color(0xFF2E7D32),
        Color(0xFFF57C00),
        Color(0xFF8E24AA)
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPickImages,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Filled.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("添加图片")
        }
        Button(
            onClick = onAddTable,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Filled.GridOn, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("添加表格")
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            colors.forEach { color ->
                ColorSwatch(
                    color = color,
                    selected = penColor == color,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(color, shape = RoundedCornerShape(50))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Color.Black else Color.LightGray,
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() }
    )
}

@Composable
private fun ImageRow(images: List<String>, onRemove: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(images) { uri ->
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "图片",
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { onRemove(uri) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "删除图片", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun TableCard(
    table: NoteTable,
    onCellChange: (row: Int, col: Int, value: String) -> Unit,
    onAddRow: () -> Unit,
    onAddColumn: () -> Unit,
    onDelete: () -> Unit
) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("表格", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "删除表格") }
            }

            table.rows.forEachIndexed { rowIndex, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (0 until table.columns).forEach { colIndex ->
                        val cellValue = row.getOrNull(colIndex) ?: ""
                        TextField(
                            value = cellValue,
                            onValueChange = { onCellChange(rowIndex, colIndex, it) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedContainerColor = Color(0xFFFFFFFF)
                            )
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAddRow) {
                    Text("＋ 行")
                }
                Button(onClick = onAddColumn, enabled = table.columns < 5) {
                    Text("＋ 列 (${table.columns}/5)")
                }
            }
        }
    }
}

private fun applyColorToSelection(
    color: Color,
    value: TextFieldValue,
    spans: List<ColorSpan>,
    onUpdate: (List<ColorSpan>) -> Unit
) {
    val start = value.selection.start.coerceAtLeast(0)
    val end = value.selection.end.coerceAtLeast(0)
    if (start == end) return
    val orderedStart = minOf(start, end)
    val orderedEnd = maxOf(start, end)
    val newSpan = ColorSpan(orderedStart, orderedEnd, color.toArgb().toLong())
    onUpdate(mergeSpans(spans, newSpan))
}

private fun handleTextChange(
    oldValue: TextFieldValue,
    newValue: TextFieldValue,
    penColor: Color,
    spans: List<ColorSpan>,
    onSpanUpdate: (List<ColorSpan>) -> Unit
): TextFieldValue {
    val oldText = oldValue.text
    val newText = newValue.text
    if (oldText == newText) return newValue

    val diff = findDiff(oldText, newText)
    val delta = diff.newEnd - diff.oldEnd

    var updatedSpans = shiftSpans(spans, diff.start, diff.oldEnd, delta)

    // Add color span for inserted text when penColor is not default black
    if (delta > 0 && penColor != Color.Black) {
        val insertStart = diff.start
        val insertEnd = diff.start + (diff.newEnd - diff.start)
        val insertSpan = ColorSpan(insertStart, insertEnd, penColor.toArgb().toLong())
        updatedSpans = mergeSpans(updatedSpans, insertSpan)
    }

    onSpanUpdate(updatedSpans)
    return newValue
}

private data class DiffRange(val start: Int, val oldEnd: Int, val newEnd: Int)

private fun findDiff(oldText: String, newText: String): DiffRange {
    val prefix = oldText.commonPrefixWith(newText)
    var start = prefix.length
    var oldTail = oldText.length
    var newTail = newText.length
    while (oldTail > start && newTail > start && oldText[oldTail - 1] == newText[newTail - 1]) {
        oldTail--
        newTail--
    }
    return DiffRange(start = start, oldEnd = oldTail, newEnd = newTail)
}

private fun shiftSpans(spans: List<ColorSpan>, start: Int, oldEnd: Int, delta: Int): List<ColorSpan> {
    val result = mutableListOf<ColorSpan>()
    spans.forEach { span ->
        when {
            span.end <= start -> result.add(span)
            span.start >= oldEnd -> result.add(span.copy(start = span.start + delta, end = span.end + delta))
            span.start < start && span.end > oldEnd -> result.add(span.copy(end = span.end + delta))
            span.start < start -> result.add(span.copy(end = start))
            span.end > oldEnd -> result.add(span.copy(start = start + delta, end = span.end + delta))
            else -> { /* span fully removed by replacement */ }
        }
    }
    return result.filter { it.start < it.end }
}

private fun mergeSpans(existing: List<ColorSpan>, newSpan: ColorSpan): List<ColorSpan> {
    val merged = mutableListOf<ColorSpan>()
    var span = newSpan
    existing.sortedBy { it.start }.forEach { current ->
        if (current.color == span.color && current.end >= span.start && current.start <= span.end) {
            span = ColorSpan(
                start = minOf(current.start, span.start),
                end = maxOf(current.end, span.end),
                color = span.color
            )
        } else {
            merged.add(current)
        }
    }
    merged.add(span)
    return merged.sortedBy { it.start }
}

private fun coloredTransformation(spans: List<ColorSpan>): VisualTransformation = VisualTransformation { value ->
    val builder = buildAnnotatedString {
        append(value.text)
        spans.forEach { span ->
            val start = span.start.coerceAtLeast(0).coerceAtMost(value.text.length)
            val end = span.end.coerceAtMost(value.text.length)
            if (end > start) addStyle(SpanStyle(color = Color(span.color.toInt())), start, end)
        }
    }
    TransformedText(builder, OffsetMapping.Identity)
}
