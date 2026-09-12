package com.example.data.telegram

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TelegramService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun sendMessage(
        botToken: String,
        chatId: String,
        text: String
    ): TelegramResult = withContext(Dispatchers.IO) {
        if (botToken.isBlank() || chatId.isBlank()) {
            return@withContext TelegramResult.Error("Токен бота или Chat ID не указаны")
        }

        val cleanedToken = botToken.trim()
        val cleanedChatId = chatId.trim().removePrefix("@")

        val url = "https://api.telegram.org/bot$cleanedToken/sendMessage"
        val json = JSONObject().apply {
            put("chat_id", cleanedChatId)
            put("text", text)
            put("parse_mode", "HTML")
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Log.d("TelegramService", "Message sent successfully: $responseBody")
                TelegramResult.Success("Сообщение успешно отправлено в Telegram!")
            } else {
                Log.w("TelegramService", "Telegram API error: $responseBody")
                val description = try {
                    JSONObject(responseBody).optString("description", "Ошибка Telegram API")
                } catch (e: Exception) {
                    "Ошибка отправки: HTTP ${response.code}"
                }
                TelegramResult.Error(description)
            }
        } catch (e: Exception) {
            Log.e("TelegramService", "Exception sending Telegram message", e)
            TelegramResult.Error("Сетевая ошибка: ${e.localizedMessage ?: "не удалось связаться с сервером Telegram"}")
        }
    }

    fun openTelegramAppOrWeb(context: Context, text: String, username: String? = null) {
        try {
            val uri = if (!username.isNullOrBlank()) {
                val cleanUser = username.trim().removePrefix("@")
                Uri.parse("https://t.me/$cleanUser")
            } else {
                Uri.parse("https://t.me/share/url?url=${Uri.encode("https://rentch.app")}&text=${Uri.encode(text)}")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("TelegramService", "Failed to launch Telegram intent", e)
        }
    }
}

sealed class TelegramResult {
    data class Success(val message: String) : TelegramResult()
    data class Error(val errorMessage: String) : TelegramResult()
}
