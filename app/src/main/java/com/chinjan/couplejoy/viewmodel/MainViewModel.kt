package com.chinjan.couplejoy.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chinjan.couplejoy.data.FirebaseRepository
import com.chinjan.couplejoy.data.model.Message
import com.chinjan.couplejoy.data.prefs.PreferenceManager
import com.chinjan.couplejoy.utils.Constants
import com.chinjan.couplejoy.utils.CoupleWidgetHelper.formatRelativeTime
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    val prefs by lazy { PreferenceManager(context) }

    private val repository = FirebaseRepository(context) // 🔥 Inject repository
    internal val _lastReceivedMessage = MutableStateFlow<Message?>(null)
    private val _relativeTime = MutableStateFlow("")
    private var tickerJob: Job? = null

    private val _isSetupDone = MutableStateFlow(isSetupComplete())
    val isSetupDone = _isSetupDone.asStateFlow()

    val coupleId = prefs.getCoupleId()
    val relativeTime: StateFlow<String> = _relativeTime

    init {
        val coupleId = prefs.getCoupleId()
        val role = prefs.getRole()
        _isSetupDone.value = !coupleId.isNullOrEmpty() && !role.isNullOrEmpty()
    }

    fun isSetupComplete(): Boolean {
        return prefs.getCoupleId()?.isNotEmpty() == true &&
                prefs.getRole().isNotEmpty()
    }

    fun markSetupComplete() {
        _isSetupDone.value = true
    }

    fun resetSetup() {
        val coupleId = prefs.getCoupleId()
        val role = prefs.getRole()
        val uid = Firebase.auth.currentUser?.uid

        if (coupleId != null && role != null && uid != null) {
            repository.deleteIfOwner(coupleId, role, uid) {
                prefs.clear()
                _isSetupDone.value = false
            }
        } else {
            prefs.clear()
            _isSetupDone.value = false
        }
    }

    fun sendMessage(message: String) {
        val coupleId = prefs.getCoupleId()
        val role = prefs.getRole()
        if (coupleId?.isNotEmpty() == true && role.isNotEmpty()) {
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
        val uid = getUUId()
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
                prefs.setCoupleId(coupleId.trim())
                prefs.setRole(selectedRole)
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

    fun getUUId(): String? {
        var id = prefs.getUUID()
        if (id == null) {
            id = Firebase.auth.currentUser?.uid
            prefs.setUUID(id)
            return id
        }
        return id
    }

    fun observeLatestMessage(coupleId: String, role: String) {
        val oppositeRole = if (role == Constants.PARTNER_A) Constants.PARTNER_B else Constants.PARTNER_A

        repository.messageDocument(coupleId, oppositeRole)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val message = snapshot.toObject(Message::class.java)
                Log.d("MessageScreen", "Snapshot: ${snapshot?.data}")
                Log.d("MessageScreen", "Parsed: $message")
                if (message != null) {
                    _lastReceivedMessage.value = message
                    startRelativeTimeTicker(message.timestamp)
                }
            }
    }

    fun startRelativeTimeTicker(timestamp: Timestamp) {
        tickerJob?.cancel() // cancel existing
        tickerJob = viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val diff = now - timestamp.toDate().time
                _relativeTime.value = formatRelativeTime(diff)
                delay(if (diff < 60_000) 1000L else 60_000L) // every sec under 1 min, else per min
            }
        }
    }
}