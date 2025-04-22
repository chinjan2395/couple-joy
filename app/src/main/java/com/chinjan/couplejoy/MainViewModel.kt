package com.chinjan.couplejoy

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("CoupleWidgetPrefs", Context.MODE_PRIVATE)

    private val _isSetupDone = MutableStateFlow(isSetupComplete())
    val isSetupDone = _isSetupDone.asStateFlow()

    init {
        val coupleId = prefs.getString("couple_id", null)
        val role = prefs.getString("partner_role", null)
        _isSetupDone.value = !coupleId.isNullOrEmpty() && !role.isNullOrEmpty()
    }

    fun isSetupComplete(): Boolean {
        return !prefs.getString("couple_id", null).isNullOrEmpty() &&
                !prefs.getString("partner_role", null).isNullOrEmpty()
    }

    fun markSetupComplete() {
        _isSetupDone.value = true
    }

    fun resetSetup() {
        val coupleId = prefs.getString("couple_id", null)
        val role = prefs.getString("partner_role", null)
//        val context = getApplication<Application>().applicationContext
//        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val uid = Firebase.auth.currentUser?.uid

        if (coupleId != null && role != null) {
            Firebase.firestore
                .collection("couples")
                .document(coupleId)
                .collection("messages")
                .document(role)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.getString("uid") == uid) {
                        Firebase.firestore
                            .collection("couples")
                            .document(coupleId)
                            .collection("messages")
                            .document(role)
                            .delete()
                    }
                }
        }

        prefs.edit { clear() }
        _isSetupDone.value = false
    }
}