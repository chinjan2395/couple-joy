package com.chinjan.couplejoy.ui.screen

import android.content.Context
import android.widget.RemoteViews
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

@Composable
fun MessageScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var message by remember { mutableStateOf(TextFieldValue("")) }

    // Retrieve coupleId and role
    val prefs = context.getSharedPreferences("CoupleWidgetPrefs", Context.MODE_PRIVATE)
    val role = prefs.getString("partner_role", "Partner") ?: "Partner"
    val initial = role.takeLast(1).uppercase() // "PartnerA" → "A", "PartnerB" → "B"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF3F6), Color(0xFFFCE4EC))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PartnerHeader(initial)
                Spacer(modifier = Modifier.height(24.dp))
                MessageInput(
                    message = message,
                    onMessageChange = { message = it },
                    onSend = {
                        prefs.edit { putString("partner_message", message.text) }

                        val views = RemoteViews(context.packageName, R.layout.couple_widget)
                        views.setTextViewText(R.id.widgetMessage, message.text)

                        viewModel.sendMessage(message.text)
                        message = TextFieldValue("")
                    }
                )
            }
            ResetSection(resetSetup = {
                viewModel.resetSetup()
            })
        }
    }
}