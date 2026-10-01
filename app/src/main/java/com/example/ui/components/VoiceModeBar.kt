package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.SplitSection
import com.example.domain.SpeechTargetField
import com.example.ui.theme.PosAmber
import com.example.ui.theme.PosCardBorder
import com.example.ui.theme.PosCardDark
import com.example.ui.theme.PosGreenOnline
import com.example.ui.theme.PosRedActive
import com.example.ui.theme.PosRedPulse
import com.example.ui.theme.PosTextMuted
import com.example.ui.theme.PosTextPrimary
import com.example.ui.theme.PosTextSecondary
import com.example.ui.theme.SplitLeftGreen
import com.example.ui.theme.SplitRightBlue

@Composable
fun VoiceModeBar(
    isListening: Boolean,
    listeningTarget: SpeechTargetField,
    audioRms: Float,
    targetSection: SplitSection,
    autoMerge: Boolean,
    lastSpokenText: String?,
    onStartListening: (SpeechTargetField) -> Unit,
    onStopListening: () -> Unit,
    onToggleTargetSection: () -> Unit,
    onToggleAutoMerge: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micPulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PosCardDark, RoundedCornerShape(12.dp))
            .border(1.dp, if (isListening) PosRedPulse else PosCardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp)
            .testTag("voice_mode_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Master Push-To-Talk Mic Button (Hold to talk, release to add)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(52.dp)
                ) {
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .scale(pulseScale + (audioRms / 20f))
                                .background(PosRedPulse.copy(alpha = 0.35f), CircleShape)
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    if (isListening) listOf(PosRedPulse, PosRedActive)
                                    else listOf(Color(0xFF3B82F6), SplitRightBlue)
                                )
                            )
                            // Zero-latency Push-to-Talk Gesture: Down = Start, Up = Stop
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    onStartListening(SpeechTargetField.MASTER)
                                    waitForUpOrCancellation()
                                    onStopListening()
                                }
                            }
                            .testTag("master_mic_button")
                    ) {
                        Icon(
                            painter = painterResource(id = if (isListening) R.drawable.ic_mic else R.drawable.ic_mic_none),
                            contentDescription = "المايك الشامل بالضغط المطول",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    text = if (isListening) "اترك للإضافة" else "استمر بالضغط",
                    color = if (isListening) PosRedPulse else PosTextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Voice Hint / Spoken Text Display
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF030712), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                if (isListening) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(PosRedActive, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تحدث الآن... (مثال: 5 بر)",
                            color = PosRedPulse,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else if (!lastSpokenText.isNullOrEmpty()) {
                    Text(
                        text = "تم التمييز: \"$lastSpokenText\"",
                        color = PosAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                } else {
                    Text(
                        text = "اضغط مطولاً وتحدث ثم ارفع إصبعك",
                        color = PosTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Auto-Merge Duplicates Toggle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (autoMerge) PosGreenOnline.copy(alpha = 0.2f) else Color(0xFF1E293B))
                    .border(
                        1.dp,
                        if (autoMerge) PosGreenOnline else PosCardBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onToggleAutoMerge() }
                    .padding(horizontal = 7.dp, vertical = 6.dp)
                    .testTag("auto_merge_toggle")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_merge),
                        contentDescription = "دمج المكرر",
                        tint = if (autoMerge) PosGreenOnline else PosTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (autoMerge) "دمج" else "مفرد",
                        color = if (autoMerge) PosGreenOnline else PosTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(5.dp))

            // Target Switcher Button: [أيسر 🟢] or [أيمن 🔷]
            val isRight = targetSection == SplitSection.RIGHT
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isRight) SplitRightBlue.copy(alpha = 0.25f)
                        else SplitLeftGreen.copy(alpha = 0.25f)
                    )
                    .border(
                        1.dp,
                        if (isRight) SplitRightBlue else SplitLeftGreen,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onToggleTargetSection() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("target_switcher_button")
            ) {
                Text(
                    text = if (isRight) "أيمن 🔷" else "أيسر 🟢",
                    color = if (isRight) Color(0xFF93C5FD) else Color(0xFFA7F3D0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
