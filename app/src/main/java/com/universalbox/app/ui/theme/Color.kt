package com.universalbox.app.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Gradients
val HomeBrush = Brush.linearGradient(
	colors = listOf(
		Color(0xFFEFD5FF),
		Color(0xFF515ADA)
	),
	start = Offset.Zero,
	end = Offset(1200f, 1200f)
)

val CollectionBrush = Brush.linearGradient(
	colors = listOf(
		Color(0xFF00C9FF),
		Color(0xFF92FE9D)
	),
	start = Offset.Zero,
	end = Offset(1200f, 1200f)
)

val ToolsBrush = Brush.linearGradient(
	colors = listOf(
		Color(0xFFE3FFE7),
		Color(0xFFD9E7FF)
	),
	start = Offset.Zero,
	end = Offset(1200f, 1200f)
)

val NotebookBrush = Brush.linearGradient(
	colors = listOf(
		Color(0xFFE3FFE7),
		Color(0xFFD9E7FF)
	),
	start = Offset.Zero,
	end = Offset(Float.POSITIVE_INFINITY, 0f)
)

val ScheduleBrush = Brush.linearGradient(
	colors = listOf(
		Color(0xFFFFF1EB),
		Color(0xFFACE0F9)
	),
	start = Offset.Zero,
	end = Offset(1200f, 1200f)
)