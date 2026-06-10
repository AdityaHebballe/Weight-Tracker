package com.aditya.weighttracker.backup

import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DriveBackupClient(private val accessToken: String) {
    suspend fun uploadTextFile(name: String, mimeType: String, content: String) {
        withContext(Dispatchers.IO) {
            val existingId = findFileId(name)
            if (existingId == null) {
                createFile(name = name, mimeType = mimeType, content = content)
            } else {
                updateFile(fileId = existingId, name = name, mimeType = mimeType, content = content)
            }
        }
    }

    suspend fun downloadTextFile(name: String): String? =
        withContext(Dispatchers.IO) {
            val fileId = findFileId(name) ?: return@withContext null
            request(
                url = "$DRIVE_API/files/$fileId?alt=media",
                method = "GET",
            )
        }

    private fun findFileId(name: String): String? {
        val query = "name = '${name.replace("'", "\\'")}' and trashed = false"
        val url = "$DRIVE_API/files?spaces=appDataFolder&fields=files(id,name,modifiedTime)&q=${query.urlEncode()}"
        val response = request(url = url, method = "GET")
        val files = JSONObject(response).optJSONArray("files") ?: return null
        return files.takeIf { it.length() > 0 }?.getJSONObject(0)?.optString("id")?.takeIf { it.isNotBlank() }
    }

    private fun createFile(name: String, mimeType: String, content: String) {
        val metadata = JSONObject()
            .put("name", name)
            .put("parents", org.json.JSONArray().put("appDataFolder"))
            .put("mimeType", mimeType)
        multipartRequest(
            url = "$DRIVE_UPLOAD_API/files?uploadType=multipart&fields=id,name,modifiedTime",
            method = "POST",
            metadata = metadata.toString(),
            mimeType = mimeType,
            content = content,
        )
    }

    private fun updateFile(fileId: String, name: String, mimeType: String, content: String) {
        val metadata = JSONObject()
            .put("name", name)
            .put("mimeType", mimeType)
        multipartRequest(
            url = "$DRIVE_UPLOAD_API/files/$fileId?uploadType=multipart&fields=id,name,modifiedTime",
            method = "PATCH",
            metadata = metadata.toString(),
            mimeType = mimeType,
            content = content,
        )
    }

    private fun multipartRequest(url: String, method: String, metadata: String, mimeType: String, content: String) {
        val boundary = "weight-tracker-${System.currentTimeMillis()}"
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            doOutput = true
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
        }
        OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer ->
            writer.append("--$boundary\r\n")
            writer.append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            writer.append(metadata)
            writer.append("\r\n--$boundary\r\n")
            writer.append("Content-Type: $mimeType; charset=UTF-8\r\n\r\n")
            writer.append(content)
            writer.append("\r\n--$boundary--\r\n")
        }
        val code = connection.responseCode
        if (code !in 200..299) {
            throw IllegalStateException("Drive upload failed: HTTP $code ${connection.errorBody()}")
        }
        connection.disconnect()
    }

    private fun request(url: String, method: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Accept", "application/json,text/csv,text/plain")
        }
        val code = connection.responseCode
        val body = if (code in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            throw IllegalStateException("Drive request failed: HTTP $code ${connection.errorBody()}")
        }
        connection.disconnect()
        return body
    }

    private fun HttpURLConnection.errorBody(): String =
        errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()

    private fun String.urlEncode(): String =
        URLEncoder.encode(this, StandardCharsets.UTF_8.name())

    companion object {
        const val BACKUP_CSV_NAME = "weight-tracker-backup.csv"
        const val BACKUP_METADATA_NAME = "weight-tracker-metadata.json"
        private const val DRIVE_API = "https://www.googleapis.com/drive/v3"
        private const val DRIVE_UPLOAD_API = "https://www.googleapis.com/upload/drive/v3"
    }
}
