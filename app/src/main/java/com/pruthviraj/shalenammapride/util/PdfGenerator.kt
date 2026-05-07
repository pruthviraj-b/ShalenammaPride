package com.pruthviraj.shalenammapride.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.pruthviraj.shalenammapride.AppNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {
    suspend fun generateSchoolReport(context: Context, meals: Int, stars: Int, feedback: Int) {
        withContext(Dispatchers.IO) {
            try {
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
                val page = pdfDocument.startPage(pageInfo)
                val canvas: Canvas = page.canvas
                val paint = Paint()

                // Background
                paint.color = Color.WHITE
                canvas.drawPaint(paint)

                // Header Background
                paint.color = Color.parseColor("#111827")
                canvas.drawRect(0f, 0f, 595f, 150f, paint)

                // Title
                paint.color = Color.WHITE
                paint.textSize = 36f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("SHALE-NAMMA PRIDE", 297.5f, 70f, paint)

                // Subtitle
                paint.textSize = 18f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                val date = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
                canvas.drawText("Official School Engagement Report - $date", 297.5f, 110f, paint)

                // Content Text Setup
                paint.color = Color.parseColor("#111827")
                paint.textAlign = Paint.Align.LEFT
                
                // Section Title
                paint.textSize = 24f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("Activity Overview", 50f, 220f, paint)

                // Stats Draw Function
                fun drawStatBox(label: String, value: String, x: Float, y: Float) {
                    paint.color = Color.parseColor("#F3F4F6")
                    paint.style = Paint.Style.FILL
                    canvas.drawRoundRect(x, y, x + 150f, y + 100f, 10f, 10f, paint)
                    
                    paint.color = Color.parseColor("#6B7280")
                    paint.textSize = 14f
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText(label, x + 75f, y + 35f, paint)
                    
                    paint.color = Color.parseColor("#111827")
                    paint.textSize = 32f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText(value, x + 75f, y + 80f, paint)
                }

                paint.textAlign = Paint.Align.LEFT
                drawStatBox("Mid-Day Meals", meals.toString(), 50f, 260f)
                drawStatBox("Student Stars", stars.toString(), 222.5f, 260f)
                drawStatBox("Feedback", feedback.toString(), 395f, 260f)

                // Footer signature
                paint.color = Color.parseColor("#6B7280")
                paint.textSize = 14f
                paint.textAlign = Paint.Align.CENTER
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                canvas.drawText("Generated automatically by Shale-Namma Admin Portal", 297.5f, 800f, paint)

                pdfDocument.finishPage(page)

                // Save PDF to Downloads
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, "ShaleNamma_Report_${System.currentTimeMillis()}.pdf")
                
                pdfDocument.writeTo(FileOutputStream(file))
                pdfDocument.close()

                withContext(Dispatchers.Main) {
                    AppNotificationManager.trigger("📄", "PDF Saved to Downloads!")
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val msg = e.message ?: "Unknown error"
                    AppNotificationManager.trigger("❌", "PDF Failed: $msg")
                }
            }
        }
    }
}
