package io.mhssn.colorpicker

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SimpleRingColorPicker(
    modifier: Modifier = Modifier,
    colorWidth: Dp = 20.dp,
    tracksCount: Int = 5,
    sectorsCount: Int = 24,
    onPickedColor: (Color) -> Unit
) {
    val density = LocalDensity.current
    val colorWidthPx = remember {
        with(density) { colorWidth.toPx() }
    }
    val selectColorWidth = remember {
        with(density) { colorWidthPx + 5.dp.toPx() }
    }
    var pickerLocation by remember {
        mutableStateOf(IntOffset(0, 0))
    }
    var radius by remember {
        mutableStateOf(0f)
    }
    Box(modifier = modifier) {
        Canvas(modifier = Modifier
            .size(280.dp)
            .aspectRatio(1f)
            .onSizeChanged {
                radius = it.width / 2f
            }
            .pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        val length = getLength(it.x, it.y, radius)
                        val offset = radius - colorWidthPx * tracksCount
                        val trackProgress =
                            ((length - offset) / (radius - offset)).coerceIn(0f, 1f)
                        val progress = ((Math.toDegrees(
                            atan2(
                                it.y - radius,
                                it.x - radius
                            ).toDouble()
                        ) + 360) % 360) / 360f
                        pickerLocation =
                            IntOffset(
                                (sectorsCount * progress)
                                    .roundToInt()
                                    .coerceIn(0, sectorsCount),
                                ((tracksCount.toFloat()) * (1 - trackProgress))
                                    .roundToInt()
                                    .coerceIn(0, tracksCount - 1)
                            )
                        onPickedColor(
                            getColorAt(
                                pickerLocation.x,
                                pickerLocation.y,
                                sectorsCount,
                                tracksCount
                            )
                        )
                    }
                }
                return@pointerInteropFilter true
            }) {
            repeat(tracksCount) { track ->
                repeat(sectorsCount) {
                    val degree = 360f / sectorsCount * it
                    drawArc(
                        getColorAt(it, track, sectorsCount, tracksCount),
                        degree,
                        360f / sectorsCount,
                        false,
                        topLeft = Offset(
                            track * colorWidthPx + colorWidthPx / 2 + selectColorWidth / 2,
                            track * colorWidthPx + colorWidthPx / 2 + selectColorWidth / 2
                        ),
                        size = Size(
                            size.width - (track * colorWidthPx * 2) - colorWidthPx - selectColorWidth,
                            size.height - (track * colorWidthPx * 2) - colorWidthPx - selectColorWidth
                        ),
                        style = Stroke(colorWidthPx)
                    )
                }
            }
        }
    }

}

private fun getLength(x: Float, y: Float, radius: Float): Float {
    return sqrt((x - radius) * (x - radius) + (y - radius) * (y - radius))
}

private fun Int.darken(darkness: Float): Int {
    return (this - this * darkness).roundToInt()
}

internal enum class ColorRange {
    RedToYellow,
    YellowToGreen,
    GreenToCyan,
    CyanToBlue,
    BlueToPurple,
    PurpleToRed
}

private fun calculateRangeProgress(progress: Double): Pair<Double, ColorRange> {
    val range: ColorRange
    return progress * 6 - when {
        progress < 1f / 6 -> {
            range = ColorRange.RedToYellow
            0
        }

        progress < 2f / 6 -> {
            range = ColorRange.YellowToGreen
            1
        }

        progress < 3f / 6 -> {
            range = ColorRange.GreenToCyan
            2
        }

        progress < 4f / 6 -> {
            range = ColorRange.CyanToBlue
            3
        }

        progress < 5f / 6 -> {
            range = ColorRange.BlueToPurple
            4
        }

        else -> {
            range = ColorRange.PurpleToRed
            5
        }
    } to range
}

private fun getColorAt(
    pickerLocationX: Int,
    pickerLocationY: Int,
    sectorsCount: Int,
    tracksCount: Int
): Color {
    val progress = pickerLocationX.toFloat() / sectorsCount.toFloat()
    val deepProgress = (pickerLocationY.toFloat() / tracksCount.toFloat()).coerceIn(0f, 1f)
    val (rangeProgress, range) = calculateRangeProgress(progress.toDouble())
    val red: Int
    val green: Int
    val blue: Int
    val dark: Float = 0.5f * deepProgress
    when (range) {
        ColorRange.RedToYellow -> {
            red = 255
            green = (255f * rangeProgress)
                .roundToInt()
            blue = 0.0
                .roundToInt()
        }

        ColorRange.YellowToGreen -> {
            red = (255 * (1 - rangeProgress))
                .roundToInt()
            green = 255
            blue = 0.0
                .roundToInt()
        }

        ColorRange.GreenToCyan -> {
            red = 0.0
                .roundToInt()
            green = 255
            blue = (255 * rangeProgress)
                .roundToInt()
        }

        ColorRange.CyanToBlue -> {
            red = 0.0
                .roundToInt()
            green = (255 * (1 - rangeProgress))
                .roundToInt()
            blue = 255
        }

        ColorRange.BlueToPurple -> {
            red = (255 * rangeProgress)
                .roundToInt()
            green = 0.0
                .roundToInt()
            blue = 255
        }

        ColorRange.PurpleToRed -> {
            red = 255
            green = 0.0
                .roundToInt()
            blue = (255 * (1 - rangeProgress))
                .roundToInt()
        }
    }
    return Color(
        red.darken(dark),
        green.darken(dark),
        blue.darken(dark)
    )
}

