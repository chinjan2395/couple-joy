package com.chinjan.couplejoy.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.chinjan.couplejoy.data.FirebaseRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val prefs = context.getSharedPreferences("CoupleWidgetPrefs", Context.MODE_PRIVATE)

    private val repository = FirebaseRepository(context) // 🔥 Inject repository

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
        val uid = Firebase.auth.currentUser?.uid

        if (coupleId != null && role != null && uid != null) {
            repository.deleteIfOwner(coupleId, role, uid) {
                prefs.edit { clear() }
                _isSetupDone.value = false
            }
        } else {
            prefs.edit { clear() }
            _isSetupDone.value = false
        }
    }

    fun sendMessage(message: String) {
        val coupleId = prefs.getString("couple_id", null)
        val role = prefs.getString("partner_role", null)
        if (!coupleId.isNullOrEmpty() && !role.isNullOrEmpty()) {
            repository.sendMessage(coupleId, role, message)
        }
    }

    fun setupPartner(
        coupleId: String,
        selectedRole: String,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = getUUId(context)
        val repository = FirebaseRepository(context)

        repository.verifyAndSetPartnerRole(
            coupleId = coupleId,
            partnerId = selectedRole,
            uid = uid,
            onSuccess = {
                Log.d(
                    "Firebase",
                    " Role is free or same device $uid is re-setting up"
                )
                // Save to prefs
                val prefs = context.getSharedPreferences("CoupleWidgetPrefs", Context.MODE_PRIVATE)
                prefs.edit {
                    putString("couple_id", coupleId.trim())
                    putString("partner_role", selectedRole)
                }
                _isSetupDone.value = true
                onSuccess()
            },
            onFailure = { error ->
                Log.d(
                    "Firebase",
                    "error: $error"
                )
                onError(error)
            }
        )
    }

    fun getUUId(context: Context): String? {
        val prefs = context.getSharedPreferences("CoupleWidgetPrefs", Context.MODE_PRIVATE)
        var id = prefs.getString("uid", null)
        if (id == null) {
            id = Firebase.auth.currentUser?.uid
            prefs.edit { putString("uid", id) }
            return id
        }
        return id
    }
}