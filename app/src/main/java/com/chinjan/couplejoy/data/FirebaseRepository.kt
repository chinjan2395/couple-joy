package com.chinjan.couplejoy.data

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source

class FirebaseRepository(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 🔐 Current user
    val currentUserId: String? get() = auth.currentUser?.uid

    // 🔧 Dynamic paths
    fun coupleDocument(coupleId: String) =
        firestore.collection(COLLECTION_COUPLES).document(coupleId)

    fun messageDocument(coupleId: String, partnerId: String) =
        coupleDocument(coupleId).collection(COLLECTION_MESSAGES).document(partnerId)

    fun deviceInfoDocument(coupleId: String, deviceId: String) =
        coupleDocument(coupleId).collection(COLLECTION_DEVICES).document(deviceId)

    // ✅ Save partner message
    fun sendMessage(coupleId: String, partnerId: String, message: String) =
        messageDocument(coupleId, partnerId).set(mapOf(
            "message" to message,
            "timestamp" to FieldValue.serverTimestamp(),
            "uid" to currentUserId,
            "device" to "${Build.MANUFACTURER} ${Build.MODEL}"
        ))

    // ✅ Get partner message
    fun getMessage(coupleId: String, partnerId: String, onSuccess: (String?) -> Unit) {
        messageDocument(coupleId, partnerId).get(Source.SERVER).addOnSuccessListener { document ->
                onSuccess(document.getString("message"))
            }
    }

    fun deleteIfOwner(coupleId: String, partnerId: String, uid: String, onSuccess: () -> Unit) {
        firestore.collection("couples").document(coupleId).collection("messages")
            .document(partnerId).get(Source.SERVER).addOnSuccessListener { document ->
                if (document.getString("uid") == uid) {
                    firestore.collection("couples").document(coupleId).collection("messages")
                        .document(partnerId).delete().addOnSuccessListener { onSuccess() }
                } else {
                    onSuccess() // still reset local prefs even if uid doesn't match
                }
            }.addOnFailureListener {
                onSuccess() // still allow reset if Firestore call fails
            }
    }

    fun verifyAndSetPartnerRole(
        coupleId: String,
        partnerId: String,
        uid: String?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        messageDocument(coupleId, partnerId).get().addOnSuccessListener { doc ->
                val existingUid = doc.getString("uid")
                if (existingUid != null && existingUid != uid) {
                    Log.w(
                        "Firebase",
                        "This partner is already registered. Please choose the other."
                    )
                    onFailure("This partner is already registered. Please choose the other.")
                } else {
                    messageDocument(coupleId, partnerId).set(mapOf("uid" to uid), SetOptions.merge())
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure("Failed to save partner info.") }
                }
            }
            .addOnFailureListener {
                onFailure("Error checking partner registration.")
            }
    }

    companion object {
        const val COLLECTION_COUPLES = "couples"
        const val COLLECTION_MESSAGES = "messages"
        const val COLLECTION_DEVICES = "devices"
    }
}