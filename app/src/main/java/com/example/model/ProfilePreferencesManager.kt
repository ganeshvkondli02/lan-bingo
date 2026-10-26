package com.example.model

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.ui.theme.AppTheme
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ProfilePreferencesManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("bingo_profile_prefs", Context.MODE_PRIVATE)

    fun getPlayerId(): String {
        var id = prefs.getString("player_id", null)
        if (id == null) {
            id = UUID.randomUUID().toString().take(8)
            prefs.edit().putString("player_id", id).apply()
        }
        return id
    }

    fun getPlayerName(): String {
        var name = prefs.getString("player_name", null)
        if (name.isNullOrBlank()) {
            name = "Player ${kotlin.random.Random.nextInt(100, 999)}"
            prefs.edit().putString("player_name", name).apply()
        }
        return name
    }

    fun savePlayerName(name: String) {
        val trimmed = name.trim().ifEmpty { "Player" }
        prefs.edit().putString("player_name", trimmed).apply()
    }

    fun getAvatarIndex(): Int {
        return prefs.getInt("avatar_index", 0)
    }

    fun saveAvatarIndex(index: Int) {
        prefs.edit().putInt("avatar_index", index).apply()
    }

    fun getCustomAvatarPath(): String? {
        val path = prefs.getString("custom_avatar_path", null)
        if (path != null && File(path).exists()) {
            return path
        }
        return null
    }

    fun saveCustomAvatarFromUri(uri: Uri): String? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (bitmap != null) {
                // Delete old custom avatars
                context.filesDir.listFiles()?.filter { it.name.startsWith("custom_avatar_") }?.forEach { it.delete() }

                val avatarFile = File(context.filesDir, "custom_avatar_${System.currentTimeMillis()}.jpg")
                val out = FileOutputStream(avatarFile)

                val maxDimension = 512
                val scaled = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                    val scaleFactor = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
                    Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * scaleFactor).toInt().coerceAtLeast(1),
                        (bitmap.height * scaleFactor).toInt().coerceAtLeast(1),
                        true
                    )
                } else {
                    bitmap
                }

                scaled.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()

                val path = avatarFile.absolutePath
                prefs.edit().putString("custom_avatar_path", path).apply()
                return path
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun clearCustomAvatar() {
        prefs.edit().remove("custom_avatar_path").apply()
        context.filesDir.listFiles()?.filter { it.name.startsWith("custom_avatar_") }?.forEach { it.delete() }
    }

    fun getTheme(): AppTheme {
        val themeName = prefs.getString("app_theme", null)
        if (themeName != null) {
            try {
                return AppTheme.valueOf(themeName)
            } catch (_: Exception) {
                return AppTheme.values().firstOrNull { it.id == themeName } ?: AppTheme.VIBRANT_LIME
            }
        }
        return AppTheme.VIBRANT_LIME
    }

    fun saveTheme(theme: AppTheme) {
        prefs.edit().putString("app_theme", theme.name).apply()
    }

    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean("is_sound_enabled", true)
    }

    fun saveSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_sound_enabled", enabled).apply()
    }

    fun isHapticsEnabled(): Boolean {
        return prefs.getBoolean("is_haptics_enabled", true)
    }

    fun saveHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_haptics_enabled", enabled).apply()
    }
}
