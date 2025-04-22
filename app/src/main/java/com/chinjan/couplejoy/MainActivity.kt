package com.chinjan.couplejoy

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chinjan.couplejoy.ui.screen.CoupleSetupScreen
import com.chinjan.couplejoy.ui.screen.MessageScreen
import com.chinjan.couplejoy.viewmodel.MainViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)

    private lateinit var googleSignInClient: GoogleSignInClient
    private val REQUEST_NOTIFICATION_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        Log.d("Auth", "Signed in using GoogleSignInOptions" + FirebaseAuth.getInstance().currentUser)

        // Sign in anonymously with Firebase Auth
        /*FirebaseAuth.getInstance().signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid
                    Log.d("Auth", "Signed in anonymously as $uid")
                } else {
                    Log.e("Auth", "Anonymous sign-in failed", task.exception)
                }
            }*/

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // From Firebase project settings
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // If not signed in, trigger sign-in

        if (FirebaseAuth.getInstance().currentUser == null) {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        // 🔐 Ask for POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }


        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFFFF3F6)
            ) {

                val viewModel: MainViewModel = viewModel()
                val isSetupDone = viewModel.isSetupDone.collectAsState()

                if (isSetupDone.value) {
                    MessageScreen()
                } else {
                    CoupleSetupScreen(
                        onContinue = {
                            viewModel.markSetupComplete()
                        }
                    )
                }
            }
        }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intent = result.data
        Log.d("Auth", "Sign-In result intent: $intent")  // Inspect the intent
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        val user = FirebaseAuth.getInstance().currentUser
                        Log.d("Auth", "Signed in as: ${user?.email} (${user?.uid})")
                    } else {
                        Log.e("Auth", "Firebase auth failed", authResult.exception)
                    }
                }
        } catch (e: ApiException) {
            Log.e("Auth", "Google Sign-In failed", e)
        }
    }
}
