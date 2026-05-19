package com.example.ds_safer.ui.screens.scenario

// TODO: 영상 파일 추가 필요
//  경로: app/src/main/res/raw/scenario3_fire.mp4
//  파일명 규칙: 소문자, 숫자, 언더바만 허용 (한글·대문자·하이픈·공백 금지)
//  파일을 넣은 뒤 Android Studio에서 Sync Project → 앱 재빌드 하세요.

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.ds_safer.ui.theme.OnSafeColor
import com.example.ds_safer.ui.theme.OnSafeScreenBrush

@Composable
fun ScenarioVideoScreen(
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current

    // R.raw.scenario3_fire 직접 참조 대신 getIdentifier 사용 →
    // mp4 파일이 없어도 빌드 에러 없이 컴파일됨.
    // 파일을 app/src/main/res/raw/scenario3_fire.mp4 에 넣으면 자동으로 재생됨.
    val rawResId by remember {
        mutableStateOf(
            context.resources.getIdentifier("scenario3_fire", "raw", context.packageName)
        )
    }

    val player = remember(rawResId) {
        if (rawResId == 0) return@remember null
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse("android.resource://${context.packageName}/$rawResId")
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(player) {
        onDispose {
            player?.release()
        }
    }

    Scaffold(
        containerColor = OnSafeColor.BgTop,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OnSafeScreenBrush)
                .padding(padding)
                .systemBarsPadding(),
        ) {
            // 상단 헤더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = OnSafeColor.TextPrimary,
                    )
                }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "시나리오 3",
                        color = OnSafeColor.Red,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "화재 상황 영상 시연",
                        color = OnSafeColor.TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (rawResId == 0 || player == null) {
                // 영상 파일이 없을 때 안내 화면
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = OnSafeColor.Red,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "영상 파일이 없습니다",
                            color = OnSafeColor.TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "아래 경로에 영상 파일을 추가하세요:\napp/src/main/res/raw/scenario3_fire.mp4",
                            color = OnSafeColor.TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                // 영상 플레이어
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            useController = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }
    }
}
