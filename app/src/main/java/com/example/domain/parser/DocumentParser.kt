package com.example.domain.parser

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream

class DocumentParser(private val context: Context) {

    init {
        PDFBoxResourceLoader.init(context)
    }

    fun extractTextFromUri(uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri)
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (mimeType == "application/pdf" || uri.path?.endsWith(".pdf") == true) {
                val pdfDocument = PDDocument.load(inputStream)
                val stripper = PDFTextStripper()
                val text = stripper.getText(pdfDocument)
                pdfDocument.close()
                text
            } else {
                // Assume text/plain or similar
                inputStream?.bufferedReader()?.use { it.readText() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
