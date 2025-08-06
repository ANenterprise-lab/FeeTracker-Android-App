package com.aaloke.feetracker

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfManager {

    fun generateReceipt(context: Context, student: Student, payment: Payment) {
        try {
            // Create a new PDF document using Android's native classes
            val pdfDocument = PdfDocument()

            // Define the page properties (A4 size)
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            // --- Set up different paint styles for our text ---
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 24f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val subtitlePaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 14f
                textAlign = Paint.Align.CENTER
            }
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
            }
            val boldTextPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                isFakeBoldText = true
            }
            val amountPaint = Paint().apply {
                color = Color.BLACK
                textSize = 20f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }

            // --- Start drawing the content onto the PDF canvas ---
            val pageW = canvas.width
            var yPos = 80f // Start from the top

            // Header
            canvas.drawText("Fee Receipt", (pageW / 2).toFloat(), yPos, titlePaint)
            yPos += 20f
            val institutionName = SettingsManager.getInstitutionName(context)
            canvas.drawText(institutionName, (pageW / 2).toFloat(), yPos, subtitlePaint)
            yPos += 80f // Add a large space

            // Details section
            val dateFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
            canvas.drawText("Student Name:", 50f, yPos, boldTextPaint)
            canvas.drawText(student.name, 150f, yPos, textPaint)
            yPos += 20f
            canvas.drawText("Class:", 50f, yPos, boldTextPaint)
            canvas.drawText(student.className, 150f, yPos, textPaint)
            yPos += 20f
            canvas.drawText("Payment Date:", 50f, yPos, boldTextPaint)
            canvas.drawText(dateFormat.format(payment.paymentDate), 150f, yPos, textPaint)
            yPos += 20f
            canvas.drawText("Payment For:", 50f, yPos, boldTextPaint)
            canvas.drawText("${payment.month} ${payment.year}", 150f, yPos, textPaint)
            yPos += 60f

            // Amount Paid
            val amountText = "Amount Paid: ₹${"%.2f".format(payment.amount)}"
            canvas.drawText(amountText, (pageW - 50).toFloat(), yPos, amountPaint)

            // Finish writing to the page
            pdfDocument.finishPage(page)

            // Save the file to the app's cache directory
            val fileName = "Receipt_${student.name.replace(" ", "_")}_${payment.id}.pdf"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use {
                pdfDocument.writeTo(it)
            }

            // Close the document and share it
            pdfDocument.close()
            sharePdf(context, file)

        } catch (e: Exception) {
            Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Fee Receipt for ${file.nameWithoutExtension}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Receipt"))
    }
}