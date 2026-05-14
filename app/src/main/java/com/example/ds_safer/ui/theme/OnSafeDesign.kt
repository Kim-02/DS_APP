package com.example.ds_safer.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object OnSafeColor {
    val BgTop = Color(0xFF07111D)
    val BgBottom = Color(0xFF0B1624)

    val Card = Color(0xFF101B2A)
    val CardSoft = Color(0xFF142235)
    val CardDark = Color(0xFF0D1724)

    val Stroke = Color(0xFF2C3B4F)
    val StrokeSoft = Color(0xFF1C2B3E)

    val Blue = Color(0xFF3478FF)
    val BlueDeep = Color(0xFF1F5FEF)

    val TextPrimary = Color(0xFFF3F6FF)
    val TextSecondary = Color(0xFF9BA7B7)
    val TextTertiary = Color(0xFF667284)

    val Green = Color(0xFF37C66B)
    val Orange = Color(0xFFFFA51E)
    val Red = Color(0xFFFF4F3F)
    val Gray = Color(0xFF697484)
}

val OnSafeScreenBrush = Brush.verticalGradient(
    colors = listOf(
        OnSafeColor.BgTop,
        OnSafeColor.BgBottom
    )
)

val OnSafeBlueBrush = Brush.horizontalGradient(
    colors = listOf(
        OnSafeColor.Blue,
        OnSafeColor.BlueDeep
    )
)

@Composable
fun OnSafeScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OnSafeScreenBrush)
            .systemBarsPadding()
            .padding(horizontal = 20.dp),
        content = content
    )
}

@Composable
fun OnSafeCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = OnSafeColor.Card,
        border = BorderStroke(
            width = if (selected) 1.6.dp else 1.dp,
            color = if (selected) OnSafeColor.Blue else OnSafeColor.Stroke
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun OnSafePrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = OnSafeColor.Blue,
            disabledContainerColor = OnSafeColor.Stroke,
            contentColor = Color.White,
            disabledContentColor = OnSafeColor.TextSecondary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun OnSafeOutlineButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, OnSafeColor.Blue),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = OnSafeColor.Blue
        )
    ) {
        Text(text)
    }
}

@Composable
fun OnSafeSectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = OnSafeColor.TextPrimary,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier
    )
}

@Composable
fun OnSafeStatusDot(
    color: Color,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun OnSafeSmallPill(
    text: String,
    color: Color = OnSafeColor.Blue
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun OnSafeBottomBar(
    selected: String,
    onHomeClick: () -> Unit,
    onFloorMapClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = OnSafeColor.CardDark,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selected == "홈",
            onClick = onHomeClick,
            icon = { Text("⌂") },
            label = { Text("홈") },
            colors = onSafeNavColors()
        )
        NavigationBarItem(
            selected = selected == "현장도",
            onClick = onFloorMapClick,
            icon = { Text("▣") },
            label = { Text("현장도") },
            colors = onSafeNavColors()
        )
        NavigationBarItem(
            selected = selected == "내정보",
            onClick = onProfileClick,
            icon = { Text("◯") },
            label = { Text("내정보") },
            colors = onSafeNavColors()
        )
    }
}

@Composable
private fun onSafeNavColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = OnSafeColor.Blue,
    selectedTextColor = OnSafeColor.TextPrimary,
    unselectedIconColor = OnSafeColor.TextSecondary,
    unselectedTextColor = OnSafeColor.TextSecondary,
    indicatorColor = OnSafeColor.Blue.copy(alpha = 0.12f)
)