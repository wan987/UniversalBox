package com.universalbox.app.ui.screens.notebook

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotebookViewModel : ViewModel() {
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    private val _notes = mutableStateListOf<NoteItem>()
    val notes: List<NoteItem> get() = _notes

    var editingId by mutableStateOf<Long?>(null)
        private set

    private fun nowText(): String = formatter.format(Date())

    fun startNewNote() {
        val note = NoteItem(
            id = System.currentTimeMillis(),
            title = "",
            content = "",
            colorSpans = emptyList(),
            images = emptyList(),
            tables = emptyList(),
            updatedAt = nowText()
        )
        _notes.add(0, note)
        editingId = note.id
    }

    fun openNote(id: Long) {
        editingId = id
    }

    fun getNote(id: Long): NoteItem? = _notes.firstOrNull { it.id == id }

    fun updateNote(
        id: Long,
        title: String,
        content: String,
        colorSpans: List<ColorSpan>,
        images: List<String>,
        tables: List<NoteTable>
    ) {
        val index = _notes.indexOfFirst { it.id == id }
        if (index >= 0) {
            _notes[index] = _notes[index].copy(
                title = title,
                content = content,
                colorSpans = colorSpans,
                images = images,
                tables = tables,
                updatedAt = nowText()
            )
        }
    }

    fun deleteNote(id: Long) {
        _notes.removeAll { it.id == id }
        if (editingId == id) {
            editingId = null
        }
    }

    fun deleteIfEmpty(id: Long) {
        val index = _notes.indexOfFirst { it.id == id }
        if (index >= 0) {
            val note = _notes[index]
            if (note.title.isBlank() && note.content.isBlank() &&
                note.images.isEmpty() && note.tables.isEmpty()
            ) {
                _notes.removeAt(index)
                if (editingId == id) {
                    editingId = null
                }
            }
        }
    }

    fun exitEditor() {
        editingId = null
    }

    // ===== Images =====
    fun addImages(id: Long, newImages: List<String>) {
        val index = _notes.indexOfFirst { it.id == id }
        if (index >= 0 && newImages.isNotEmpty()) {
            val updated = _notes[index].copy(images = _notes[index].images + newImages, updatedAt = nowText())
            _notes[index] = updated
        }
    }

    fun removeImage(id: Long, uri: String) {
        val index = _notes.indexOfFirst { it.id == id }
        if (index >= 0) {
            val updated = _notes[index].copy(images = _notes[index].images - uri, updatedAt = nowText())
            _notes[index] = updated
        }
    }

    // ===== Tables =====
    fun addTable(id: Long) {
        val index = _notes.indexOfFirst { it.id == id }
        if (index >= 0) {
            val table = NoteTable(
                id = System.currentTimeMillis(),
                columns = 2,
                rows = List(2) { List(2) { "" } }
            )
            _notes[index] = _notes[index].copy(tables = _notes[index].tables + table, updatedAt = nowText())
        }
    }

    fun updateTableCell(noteId: Long, tableId: Long, row: Int, col: Int, value: String) {
        val noteIndex = _notes.indexOfFirst { it.id == noteId }
        if (noteIndex < 0) return
        val tables = _notes[noteIndex].tables.map { table ->
            if (table.id != tableId) table else {
                val newRows = table.rows.toMutableList()
                if (row in newRows.indices && col in 0 until table.columns) {
                    val newRow = newRows[row].toMutableList()
                    // ensure row has enough columns
                    while (newRow.size < table.columns) newRow.add("")
                    newRow[col] = value
                    newRows[row] = newRow
                }
                table.copy(rows = newRows)
            }
        }
        _notes[noteIndex] = _notes[noteIndex].copy(tables = tables, updatedAt = nowText())
    }

    fun addTableRow(noteId: Long, tableId: Long) {
        val noteIndex = _notes.indexOfFirst { it.id == noteId }
        if (noteIndex < 0) return
        val tables = _notes[noteIndex].tables.map { table ->
            if (table.id != tableId) table else {
                val newRow = List(table.columns) { "" }
                table.copy(rows = table.rows + listOf(newRow))
            }
        }
        _notes[noteIndex] = _notes[noteIndex].copy(tables = tables, updatedAt = nowText())
    }

    fun addTableColumn(noteId: Long, tableId: Long) {
        val noteIndex = _notes.indexOfFirst { it.id == noteId }
        if (noteIndex < 0) return
        val tables = _notes[noteIndex].tables.map { table ->
            if (table.id != tableId) table else {
                val newColumns = (table.columns + 1).coerceAtMost(5)
                if (newColumns == table.columns) return@map table
                val newRows = table.rows.map { row ->
                    val r = row.toMutableList()
                    while (r.size < newColumns) r.add("")
                    r
                }
                table.copy(columns = newColumns, rows = newRows)
            }
        }
        _notes[noteIndex] = _notes[noteIndex].copy(tables = tables, updatedAt = nowText())
    }

    fun deleteTable(noteId: Long, tableId: Long) {
        val noteIndex = _notes.indexOfFirst { it.id == noteId }
        if (noteIndex < 0) return
        val tables = _notes[noteIndex].tables.filterNot { it.id == tableId }
        _notes[noteIndex] = _notes[noteIndex].copy(tables = tables, updatedAt = nowText())
    }
}

// Shared model
data class NoteItem(
    val id: Long,
    val title: String,
    val content: String,
    val colorSpans: List<ColorSpan>,
    val images: List<String>,
    val tables: List<NoteTable>,
    val updatedAt: String
)

data class ColorSpan(
    val start: Int,
    val end: Int,
    val color: Long
)

data class NoteTable(
    val id: Long,
    val columns: Int,
    val rows: List<List<String>>
)
