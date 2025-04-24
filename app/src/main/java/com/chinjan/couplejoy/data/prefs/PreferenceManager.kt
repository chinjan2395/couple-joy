package com.chinjan.couplejoy.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.chinjan.couplejoy.utils.Constants
import androidx.core.content.edit

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    fun getRole(): String =
        prefs.getString(Constants.PREF_ROLE, Constants.PARTNER_A) ?: Constants.DEFAULT_PARTNER

    fun setRole(role: String) {
        prefs.edit { putString(Constants.PREF_ROLE, role) }
    }

    fun getCoupleId(): String? =
        prefs.getString(Constants.PREF_COUPLE_ID, null)

    fun setCoupleId(coupleId: String) {
        prefs.edit { putString(Constants.PREF_COUPLE_ID, coupleId) }
    }

    fun getUUID(): String? =
        prefs.getString(Constants.COLLECTION_UID, null)

    fun setUUID(uid: String?) {
        prefs.edit { putString(Constants.COLLECTION_UID, uid) }
    }

    fun hasLastDisplayedMessage(): Boolean =
        prefs.contains(Constants.PREF_LAST_DISPLAYED_MESSAGE)

    fun getLastDisplayedMessage(): String? =
        prefs.getString(Constants.PREF_LAST_DISPLAYED_MESSAGE, null)

    fun setLastDisplayedMessage(message: String) {
        prefs.edit { putString(Constants.PREF_LAST_DISPLAYED_MESSAGE, message) }
    }

    fun clear() {
        prefs.edit { clear() }
    }
}