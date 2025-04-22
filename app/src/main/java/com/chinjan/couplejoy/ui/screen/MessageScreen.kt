package com.chinjan.couplejoy.ui.screen

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chinjan.couplejoy.viewmodel.MainViewModel
import com.chinjan.couplejoy.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun MessageScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var message by remember { mutableStateOf(TextFieldValue("")) }

    // Retrieve coupleId and role
    val prefs = context.getSharedPreferences("CoupleWidgetPrefs", Context.MODE_PRIVATE)
    val role = prefs.getString("partner_role", "Partner") ?: "Partner"
    val initial = role.takeLast(1).uppercase() // "PartnerA" → "A", "PartnerB" → "B"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFF3F6)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // 🧍‍♂️ Partner Initial Circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEA3AA).copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "💌 Couple Widget",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Send a sweet message to your partner",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("Type something lovely...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFEA3AA),
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        prefs.edit { putString("partner_message", message.text) }

                        val views = RemoteViews(context.packageName, R.layout.couple_widget)
                        views.setTextViewText(R.id.widgetMessage, message.text)

                        sendMessageToFirebase(context, message.text)
                        message = TextFieldValue("")
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEA3AA))
                ) {
                    Text("Send")
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Want to start fresh?",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.resetSetup() },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                ) {
                    Text("Reset Setup")
                }
            }
        }
    }
}

fun sendMessageToFirebase(context: Context, message: String) {
    val prefs = context.getSharedPreferences("CoupleWidgetPrefs", Context.MODE_PRIVATE)
    val partner = prefs.getString("partner_role", "partnerA") ?: "partnerA"
    val coupleId = prefs.getString("couple_id", "default") ?: "default"

    val db = Firebase.firestore
    val messageData = hashMapOf(
        "message" to message,
        "timestamp" to FieldValue.serverTimestamp(),
        "device" to "${Build.MANUFACTURER} ${Build.MODEL}"
    )

    db.collection("couples")
        .document(coupleId)
        .collection("messages")
        .document(partner)
        .set(messageData, SetOptions.merge())
        .addOnSuccessListener {
            Log.d("Firebase", "Message sent successfully!")
            Toast.makeText(context, "Message sent!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Log.w("Firebase", "Error writing message", e)
            Toast.makeText(context, "Failed to send", Toast.LENGTH_SHORT).show()
        }
}