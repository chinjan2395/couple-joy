package com.chinjan.couplejoy.ui.screen

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun CoupleSetupScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    var coupleId by remember { mutableStateOf("testing") }
    var selectedRole by remember { mutableStateOf("partnerA") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Enter Couple Code",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = coupleId,
            onValueChange = { coupleId = it },
            label = { Text("Couple ID") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Select Your Role", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RoleOption("Partner A", "partnerA", selectedRole) { selectedRole = it }
            RoleOption("Partner B", "partnerB", selectedRole) { selectedRole = it }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                Log.d("Firebase", "onClick selectedRole: $selectedRole, couple: $coupleId")
                if (coupleId.isNotEmpty() && selectedRole.isNotEmpty()) {
                    if (false) {
                        return@Button
                    }

                    val uid = getUUId(context)
                    val docRef = Firebase.firestore
                        .collection("couples")
                        .document(coupleId)
                        .collection("messages")
                        .document(selectedRole)

                    Log.d(
                        "Firebase",
                        "Setting up device:$uid for role:$selectedRole in couple id:$coupleId"
                    )

                    docRef
                        .get()
                        .addOnSuccessListener { doc ->
                            val existingUUId = doc.getString("uid")
                            Log.w("Firebase", "existingUUId: $existingUUId")
                            if (existingUUId != null && existingUUId != uid) {
                                Log.w(
                                    "Firebase",
                                    "This partner is already registered. Please choose the other."
                                )
                                Toast.makeText(
                                    context,
                                    "This partner is already registered. Please choose the other.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                // ✅ Role is free or same device is re-setting up
                                Log.d(
                                    "Firebase",
                                    " Role is free or same device $uid is re-setting up"
                                )
                                docRef.set(
                                    mapOf("uid" to uid),
                                    SetOptions.merge()
                                )
                                val prefs = context.getSharedPreferences(
                                    "CoupleWidgetPrefs",
                                    Context.MODE_PRIVATE
                                )
                                prefs.edit {
                                    putString("couple_id", coupleId.trim())
                                        .putString("partner_role", selectedRole)
                                        .apply()
                                }
                                onContinue() // navigate to main screen or widget preview
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firebase", "Error selecting couple id", e)
                            Toast.makeText(context, "Error selecting couple id", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue", color = Color.White)
        }
    }
}

@Composable
fun RoleOption(label: String, value: String, selected: String, onSelected: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onSelected(value) }
            .padding(8.dp)
    ) {
        RadioButton(
            selected = selected == value,
            onClick = { onSelected(value) },
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF7C4DFF)
            )
        )
        Text(label)
    }
}

fun getUUId(context: Context): String? {
    val prefs = context.getSharedPreferences("CoupleWidgetPrefs", Context.MODE_PRIVATE)
    var id = prefs.getString("uid", null)
    Log.d("Firebase", "id: $id")
    if (id == null) {
        id = Firebase.auth.currentUser?.uid
        Log.d("Firebase", "currentUser: $id")
        prefs.edit { putString("uid", id) }
        return id
    }
    return id
}