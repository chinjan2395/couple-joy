package com.chinjan.couplejoy

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chinjan.couplejoy.ui.screen.CoupleSetupScreen
import com.chinjan.couplejoy.ui.screen.MessageScreen
import com.chinjan.couplejoy.ui.screen.SignInScreen
import com.chinjan.couplejoy.viewmodel.AuthViewModel
import com.chinjan.couplejoy.viewmodel.MainViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)

    private val REQUEST_NOTIFICATION_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        Log.d("Auth", "Signed in using GoogleSignInOptions" + FirebaseAuth.getInstance().currentUser)

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

                val context = LocalContext.current
                val viewModel: MainViewModel = viewModel()
                val isSetupDone = viewModel.isSetupDone.collectAsState()
                val authViewModel: AuthViewModel = viewModel()
                val currentUser by authViewModel.currentUser.collectAsState()

                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    authViewModel.handleSignInResult(result.data)
                }

                if (isSetupDone.value) {
                    MessageScreen()
                } else {
                    if (currentUser == null) {
                        SignInScreen(
                            onSignInClicked = {
                                Log.d("SignInScreen", "signInIntent")
                                val signInIntent = authViewModel.getGoogleSignInClient(context).signInIntent
                                launcher.launch(signInIntent)
                            }
                        )
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
    }
}
