package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.PdfNoteItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.UUID

class PdfStorageRepository(private val context: Context) {

    private val pdfsDir: File = File(context.filesDir, "pdfs").apply { mkdirs() }
    private val cacheDir: File = File(context.cacheDir, "pdf_cache").apply { mkdirs() }
    private val prefs = context.getSharedPreferences("vyas_pdf_store", Context.MODE_PRIVATE)

    fun getPdfsForUnit(subject: String, unit: String): List<PdfNoteItem> {
        val key = "$subject-$unit"
        val defaults = TimetableData.defaultUnit1Pdfs[key].orEmpty()
        val uploaded = loadUploadedPdfs().filter { it.subject == subject && it.unit == unit }
        return defaults + uploaded
    }

    fun importPdfFromUri(uri: Uri, subject: String, unit: String): PdfNoteItem? {
        val displayName = resolveDisplayName(uri) ?: "Uploaded_Notes_${System.currentTimeMillis()}.pdf"
        val safeName = displayName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val uniqueFileName = "${UUID.randomUUID()}_$safeName"
        val targetFile = File(pdfsDir, uniqueFileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return null

        val item = PdfNoteItem(
            id = UUID.randomUUID().toString(),
            name = displayName,
            subject = subject,
            unit = unit,
            localFileName = uniqueFileName
        )

        val current = loadUploadedPdfs().toMutableList()
        current.add(item)
        saveUploadedPdfs(current)
        return item
    }

    fun getOrCreateReadablePdfFile(item: PdfNoteItem): File {
        if (item.localFileName != null) {
            val file = File(pdfsDir, item.localFileName)
            if (file.exists()) return file
        }
        val safeName = item.name.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val generatedFile = File(cacheDir, safeName)
        generatedFile.writeBytes(buildValidPdfBytes(item.name, item.subject, item.unit))
        return generatedFile
    }

    fun exportPdfToUri(item: PdfNoteItem, destinationUri: Uri): Boolean {
        return try {
            val sourceFile = getOrCreateReadablePdfFile(item)
            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } != null
        } catch (_: Exception) {
            false
        }
    }

    private fun resolveDisplayName(uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = cursor.getString(idx)
            }
        }
        return name
    }

    private fun loadUploadedPdfs(): List<PdfNoteItem> {
        val raw = prefs.getString(KEY_UPLOADED_PDFS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val fileName = obj.optString("localFileName", "")
                    if (fileName.isNotEmpty() && File(pdfsDir, fileName).exists()) {
                        add(
                            PdfNoteItem(
                                id = obj.getString("id"),
                                name = obj.getString("name"),
                                subject = obj.getString("subject"),
                                unit = obj.getString("unit"),
                                localFileName = fileName
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveUploadedPdfs(items: List<PdfNoteItem>) {
        val arr = JSONArray()
        items.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("subject", item.subject)
                    put("unit", item.unit)
                    put("localFileName", item.localFileName ?: "")
                }
            )
        }
        prefs.edit().putString(KEY_UPLOADED_PDFS, arr.toString()).apply()
    }

    private fun escapePdfText(text: String): String =
        text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")

    /**
     * Builds a valid PDF 1.4 document with exact byte-offset xref table
     * so Android's native PdfRenderer renders it cleanly.
     */
    private fun buildValidPdfBytes(title: String, subject: String, unit: String): ByteArray {
        val safeTitle = escapePdfText(title)
        val safeSubject = escapePdfText(subject)
        val safeUnit = escapePdfText(unit)

        val streamContent = buildString {
            append("BT\n")
            append("/F1 20 Tf\n")
            append("50 720 Td\n")
            append("(SSBT COET - Computer Engineering) Tj\n")
            append("/F1 15 Tf\n")
            append("0 -35 Td\n")
            append("(Subject: $safeSubject | $safeUnit) Tj\n")
            append("/F1 12 Tf\n")
            append("0 -30 Td\n")
            append("(Document: $safeTitle) Tj\n")
            append("0 -25 Td\n")
            append("(Official First Year Engineering Study Notes) Tj\n")
            append("0 -20 Td\n")
            append("(-----------------------------------------------------) Tj\n")
            append("0 -25 Td\n")
            append("(Unit Contents and Syllabus topics are ready for study.) Tj\n")
            append("ET")
        }
        val streamLength = streamContent.toByteArray(StandardCharsets.US_ASCII).size

        val obj1 = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n"
        val obj2 = "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n"
        val obj3 = "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n"
        val obj4 = "4 0 obj\n<< /Length $streamLength >>\nstream\n$streamContent\nendstream\nendobj\n"
        val obj5 = "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n"

        val out = ByteArrayOutputStream()
        fun writeAscii(s: String) {
            out.write(s.toByteArray(StandardCharsets.US_ASCII))
        }

        writeAscii("%PDF-1.4\n")
        val offsets = mutableListOf<Int>()
        listOf(obj1, obj2, obj3, obj4, obj5).forEach { obj ->
            offsets.add(out.size())
            writeAscii(obj)
        }
        val xrefStart = out.size()
        writeAscii("xref\n0 6\n")
        writeAscii("0000000000 65535 f \n")
        offsets.forEach { offset ->
            writeAscii(String.format("%010d 00000 n \n", offset))
        }
        writeAscii("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n$xrefStart\n%%EOF")
        return out.toByteArray()
    }

    companion object {
        private const val KEY_UPLOADED_PDFS = "uploaded_pdfs"
    }
}
