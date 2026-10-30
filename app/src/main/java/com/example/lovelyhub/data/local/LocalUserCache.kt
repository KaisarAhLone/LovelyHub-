package com.example.lovelyhub.data.local

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream

class LocalUserCache(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("lovelyhub_user_cache", Context.MODE_PRIVATE)

    private fun getCurrentUid(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"
    }

    fun saveProfilePhotoFile(uid: String = "", uri: Uri): String {
        val targetUid = uid.ifBlank { getCurrentUid() }
        return try {
            val file = File(context.filesDir, "profile_photo_$targetUid.jpg")
            val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            outputStream.flush()
            inputStream.close()
            outputStream.close()

            val path = file.absolutePath
            prefs.edit()
                .putString("cached_photo_path_$targetUid", path)
                .putString("last_cached_photo_path", path)
                .commit()
            path
        } catch (_: Exception) {
            ""
        }
    }

    fun saveProfilePhotoBase64(uid: String = "", base64Str: String) {
        val targetUid = uid.ifBlank { getCurrentUid() }
        if (base64Str.isBlank()) return
        try {
            val file = File(context.filesDir, "profile_photo_$targetUid.b64")
            file.writeText(base64Str)
            val path = file.absolutePath
            prefs.edit()
                .putString("cached_photo_path_$targetUid", path)
                .putString("last_cached_photo_path", path)
                .commit()
        } catch (_: Exception) {}
    }

    fun getProfilePhoto(uid: String = ""): String? {
        val targetUid = uid.ifBlank { getCurrentUid() }

        val jpgFile = File(context.filesDir, "profile_photo_$targetUid.jpg")
        if (jpgFile.exists() && jpgFile.length() > 0) {
            return jpgFile.absolutePath
        }

        val b64File = File(context.filesDir, "profile_photo_$targetUid.b64")
        if (b64File.exists() && b64File.length() > 0) {
            try {
                val txt = b64File.readText().trim()
                if (txt.isNotBlank()) return txt
            } catch (_: Exception) {}
        }

        val savedPath = prefs.getString("cached_photo_path_$targetUid", null)
            ?: prefs.getString("last_cached_photo_path", null)
        if (!savedPath.isNullOrBlank()) {
            val f = File(savedPath)
            if (f.exists() && f.length() > 0) {
                if (savedPath.endsWith(".b64")) {
                    try { return f.readText().trim() } catch (_: Exception) {}
                } else {
                    return f.absolutePath
                }
            }
        }

        val defaultJpg = File(context.filesDir, "profile_photo_default_user.jpg")
        if (defaultJpg.exists() && defaultJpg.length() > 0) {
            return defaultJpg.absolutePath
        }

        val defaultB64 = File(context.filesDir, "profile_photo_default_user.b64")
        if (defaultB64.exists() && defaultB64.length() > 0) {
            try { return defaultB64.readText().trim() } catch (_: Exception) {}
        }

        return null
    }

    fun removeProfilePhoto(uid: String = "") {
        val targetUid = uid.ifBlank { getCurrentUid() }
        try {
            File(context.filesDir, "profile_photo_$targetUid.jpg").delete()
            File(context.filesDir, "profile_photo_$targetUid.b64").delete()
            File(context.filesDir, "profile_photo_default_user.jpg").delete()
            File(context.filesDir, "profile_photo_default_user.b64").delete()
        } catch (_: Exception) {}
        prefs.edit()
            .remove("cached_photo_path_$targetUid")
            .remove("last_cached_photo_path")
            .commit()
    }

    fun hasAcceptedDisclaimer(uid: String = ""): Boolean {
        val targetUid = uid.ifBlank { getCurrentUid() }
        return prefs.getBoolean("accepted_disclaimer_$targetUid", false)
    }

    fun setAcceptedDisclaimer(accepted: Boolean, uid: String = "") {
        val targetUid = uid.ifBlank { getCurrentUid() }
        prefs.edit().putBoolean("accepted_disclaimer_$targetUid", accepted).commit()
    }

    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean("has_completed_onboarding_v5", false)
    }

    fun setCompletedOnboarding(completed: Boolean) {
        prefs.edit().putBoolean("has_completed_onboarding_v5", completed).commit()
    }
}
