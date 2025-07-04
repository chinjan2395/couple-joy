package com.chinjan.couplejoy.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.chinjan.couplejoy.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(FirebaseAuth.getInstance().currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val auth = FirebaseAuth.getInstance()

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun handleSignInResult(intent: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        _currentUser.value = auth.currentUser
                    } else {
                        Log.e("Auth", "Firebase sign-in failed", result.exception)
                    }
                }
        } catch (e: ApiException) {
            Log.e("Auth", "Google sign-in failed", e)
        }
    }
}