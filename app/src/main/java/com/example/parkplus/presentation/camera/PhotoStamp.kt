package com.example.parkplus.presentation.survey

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PhotoStamp {

    fun addStamp(
        original: Bitmap,
        parkName: String,
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        capturedAt: Long
    ): Bitmap {

        val bitmap = original.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)

        val date = SimpleDateFormat(
            "dd MMM yyyy, hh:mm:ss a",
            Locale.getDefault()
        ).format(Date(capturedAt))

        val lines = listOf(
            "Park: $parkName",
            "Latitude: %.6f".format(Locale.US, latitude),
            "Longitude: %.6f".format(Locale.US, longitude),
            "GPS accuracy: %.1f m".format(Locale.US, accuracy),
            "Captured: $date"
        )

        val padding = bitmap.width * 0.025f
        val textSize = (bitmap.width * 0.035f).coerceAtLeast(22f)
        val lineHeight = textSize * 1.45f
        val boxHeight = padding * 2 + lineHeight * lines.size

        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(190, 0, 0, 0)
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.textSize = textSize
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
        }

        val left = padding
        val top = bitmap.height - boxHeight - padding
        val right = bitmap.width - padding
        val bottom = bitmap.height - padding

        canvas.drawRoundRect(
            RectF(left, top, right, bottom),
            16f,
            16f,
            backgroundPaint
        )

        lines.forEachIndexed { index, line ->
            canvas.drawText(
                line,
                left + padding,
                top + padding + textSize + index * lineHeight,
                textPaint
            )
        }

        return bitmap
    }
}