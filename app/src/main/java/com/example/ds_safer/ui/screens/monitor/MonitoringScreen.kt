package com.example.ds_safer.ui.screens.monitoring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ds_safer.ui.theme.OnSafeBottomBar
import com.example.ds_safer.ui.theme.OnSafeCard
import com.example.ds_safer.ui.theme.OnSafeColor
import com.example.ds_safer.ui.theme.OnSafeScreenBrush
import com.example.ds_safer.ui.theme.OnSafeSmallPill

@Composable
private fun SegmentTabs(
    items: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = OnSafeColor.Card,
        border = androidx.compose.foundation.BorderStroke(1.dp, OnSafeColor.Stroke)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            items.forEach { item ->
                val active = selected == item
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .background(
                            if (active) OnSafeColor.Blue else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelect(item) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        color = if (active) Color.White else OnSafeColor.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(50),
        color = if (selected) OnSafeColor.Blue else OnSafeColor.Card,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) OnSafeColor.Blue else OnSafeColor.Stroke
        )
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else OnSafeColor.TextSecondary,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun MonitoringCard(
    statusColor: Color,
    iconText: String,
    title: String,
    subtitle: String,
    status: String,
    leftValue: String,
    leftLabel: String,
    rightValue: String,
    rightLabel: String
) {
    OnSafeCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(50),
                color = statusColor.copy(alpha = 0.18f)
            ) {
                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconText,
                        color = statusColor,
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            color = OnSafeColor.TextPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = subtitle,
                            color = OnSafeColor.TextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = status,
                        color = statusColor,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    MetricText(
                        value = leftValue,
                        label = leftLabel,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .width(1.dp)
                            .background(OnSafeColor.Stroke)
                    )

                    MetricText(
                        value = rightValue,
                        label = rightLabel,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = OnSafeColor.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricText(
    value: String,
    label: String,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = OnSafeColor.TextPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
        )
        Text(
            text = label,
            color = OnSafeColor.TextSecondary,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
        )
    }
}