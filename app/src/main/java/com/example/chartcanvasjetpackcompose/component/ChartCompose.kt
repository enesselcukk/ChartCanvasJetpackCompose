addpackage com.example.chartcanvasjetpackcompose.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chartcanvasjetpackcompose.data.ChartData
import kotlin.math.pow


@Composable
fun CryptoChart(
    data: List<ChartData> = listOf(
        ChartData(30000f, "7 MAY"),
        ChartData(44000f, "30 MAY"),
        ChartData(35000f, "23 JUN"),
        ChartData(44023.90f, "16 JUN"),
        ChartData(94023.90f, "10 JUN")
    )
) {
    var selectedPoint by remember { mutableStateOf<ChartData?>(null) }
    val animationProgress = remember { Animatable(0f) }

    // Initial animation when the chart is first displayed
    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 2000,
                easing = FastOutSlowInEasing
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
                .align(Alignment.Center)
                .pointerInput(Unit) {
                    // Handle touch events to show price indicator
                    detectTapGestures { offset ->
                        val points = data.mapIndexed { index, chartData ->
                            val x = (size.width * index) / (data.size - 1)
                            val y = size.height - (chartData.value / data.maxOf { it.value }) * size.height
                            Offset(x.toFloat(), y) to chartData
                        }
                        
                        // Find the nearest point to touch location
                        selectedPoint = points.minByOrNull { (point) ->
                            (point.x - offset.x).pow(2) + (point.y - offset.y).pow(2)
                        }?.second
                    }
                }
        ) {
            // Calculate points for the chart
            val points = data.mapIndexed { index, chartData ->
                Offset(
                    x = (size.width * index) / (data.size - 1),
                    y = size.height - (chartData.value / data.maxOf { it.value }) * size.height
                )
            }

            // Draw grid lines
            val gridLines = 5
            for (i in 0..gridLines) {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(0f, size.height * i / gridLines),
                    end = Offset(size.width, size.height * i / gridLines),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (points.isNotEmpty()) {
                // Create animated points for bottom-up animation
                val animatedPoints = points.mapIndexed { index, point ->
                    Offset(
                        x = point.x,
                        y = size.height - ((size.height - point.y) * animationProgress.value)
                    )
                }

                // Draw area fill with gradient
                val fillPath = Path().apply {
                    moveTo(animatedPoints.first().x, size.height)
                    lineTo(animatedPoints.first().x, animatedPoints.first().y)
                    animatedPoints.forEach { lineTo(it.x, it.y) }
                    lineTo(animatedPoints.last().x, size.height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.7f * animationProgress.value),
                            Color(0xFFFFD700).copy(alpha = 0.0f)
                        ),
                        startY = 0f,
                        endY = size.height
                    )
                )

                // Draw the line chart
                val linePath = Path().apply {
                    moveTo(animatedPoints.first().x, animatedPoints.first().y)
                    animatedPoints.forEach { lineTo(it.x, it.y) }
                }

                drawPath(
                    path = linePath,
                    color = Color(0xFFFFD700),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw normal points
                animatedPoints.forEach { point ->
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }

                // Draw selected point and price indicator
                selectedPoint?.let { selected ->
                    val index = data.indexOf(selected)
                    val point = animatedPoints[index]
                    
                    // Price indicator box dimensions
                    val boxWidth = 120.dp.toPx()
                    val boxHeight = 60.dp.toPx()
                    val boxPadding = 8.dp.toPx()
                    
                    // Keep box x position within screen bounds
                    val boxX = (point.x - boxWidth / 2).coerceIn(
                        minimumValue = 0f,
                        maximumValue = size.width - boxWidth
                    )
                    
                    // Calculate box y position
                    val boxY = if (point.y <= boxHeight + boxPadding + 16.dp.toPx()) {
                        // Show below point if not enough space above
                        point.y + boxPadding + 8.dp.toPx()
                    } else {
                        // Show above point if there's enough space
                        point.y - boxHeight - boxPadding - 8.dp.toPx()
                    }
                    
                    val boxTopLeft = Offset(boxX, boxY)
                    
                    // Draw price indicator box
                    drawRoundRect(
                        color = Color(0xFF2A2A2A),
                        topLeft = boxTopLeft,
                        size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )
                    
                    // Draw price text
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "$${String.format("%.2f", selected.value)}",
                            boxTopLeft.x + boxPadding,
                            boxTopLeft.y + boxHeight/2 + 6.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = Color(0xFFFFD700).toArgb()
                                textSize = 16.sp.toPx()
                                isFakeBoldText = true
                                isAntiAlias = true
                            }
                        )
                    }
                    
                    // Draw highlighted point
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = 8.dp.toPx(),
                        center = point
                    )
                }
            }
        }
    }
}