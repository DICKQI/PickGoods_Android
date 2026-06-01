package com.pickgoods.app.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pickgoods.app.ui.theme.BorderGold
import com.pickgoods.app.ui.theme.Gold
import com.pickgoods.app.ui.theme.GoldLight
import com.pickgoods.app.ui.theme.GoldSoft
import com.pickgoods.app.ui.theme.Purple
import com.pickgoods.app.ui.theme.PurpleLight
import com.pickgoods.app.ui.theme.SurfaceGray
import com.pickgoods.app.ui.theme.White

@Stable
object PickGoodsMotion {
    const val Fast = 180
    const val Normal = 280
    const val Slow = 420
}

@Stable
object PickGoodsShape {
    val Card = RoundedCornerShape(20.dp)
    val Control = RoundedCornerShape(12.dp)
    val Pill = RoundedCornerShape(999.dp)
}

val ChampagneBrush = Brush.linearGradient(
    colors = listOf(Gold, GoldLight)
)

val LaserBrush = Brush.linearGradient(
    colors = listOf(Purple, PurpleLight)
)

val SoftPageBrush = Brush.verticalGradient(
    colors = listOf(White, SurfaceGray)
)

val SoftPanelBrush = Brush.linearGradient(
    colors = listOf(White, GoldSoft.copy(alpha = 0.78f), White)
)

@Composable
fun PickGoodsScreen(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SoftPageBrush),
        content = content
    )
}

@Composable
fun PickGoodsCard(
    modifier: Modifier = Modifier,
    radius: Dp = 20.dp,
    pressedScale: Float = 0.985f,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = tween(PickGoodsMotion.Fast, easing = FastOutSlowInEasing),
        label = "cardScale"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (pressed) 4.dp else 10.dp,
        animationSpec = tween(PickGoodsMotion.Fast, easing = FastOutSlowInEasing),
        label = "cardShadow"
    )
    val shape = RoundedCornerShape(radius)

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .shadow(
                shadowElevation,
                shape,
                ambientColor = Gold.copy(alpha = 0.08f),
                spotColor = Gold.copy(alpha = 0.12f)
            )
            .border(BorderStroke(1.dp, BorderGold.copy(alpha = 0.35f)), shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}

@Composable
fun GoldAccentLine(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Color.Transparent, Gold.copy(alpha = 0.55f), Color.Transparent)))
    )
}

@Composable
fun ShimmerBlock(
    modifier: Modifier = Modifier,
    radius: Dp = 14.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            SurfaceGray.copy(alpha = 0.75f),
            White.copy(alpha = 0.95f),
            SurfaceGray.copy(alpha = 0.75f)
        ),
        start = androidx.compose.ui.geometry.Offset(progress * 800f - 400f, 0f),
        end = androidx.compose.ui.geometry.Offset(progress * 800f, 0f)
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(radius))
            .background(brush)
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <S> PickGoodsAnimatedContent(
    targetState: S,
    modifier: Modifier = Modifier,
    transitionSpec: () -> ContentTransform = {
        (fadeIn(tween(PickGoodsMotion.Normal)) + slideInVertically(
            animationSpec = tween(PickGoodsMotion.Normal, easing = FastOutSlowInEasing),
            initialOffsetY = { it / 10 }
        )) togetherWith (fadeOut(tween(PickGoodsMotion.Fast)) + slideOutVertically(
            animationSpec = tween(PickGoodsMotion.Fast, easing = FastOutSlowInEasing),
            targetOffsetY = { -it / 18 }
        ))
    },
    content: @Composable (S) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = { transitionSpec() },
        modifier = modifier,
        label = "pickGoodsAnimatedContent",
        content = { state -> content(state) }
    )
}
