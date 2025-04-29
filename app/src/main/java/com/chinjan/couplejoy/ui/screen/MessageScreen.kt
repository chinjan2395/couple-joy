package com.chinjan.couplejoy.ui.screen

import android.widget.RemoteViews
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chinjan.couplejoy.viewmodel.MainViewModel
import com.chinjan.couplejoy.R
import com.chinjan.couplejoy.data.prefs.PreferenceManager
import com.chinjan.couplejoy.ui.components.MessageInput
import com.chinjan.couplejoy.ui.components.PartnerHeader
import com.chinjan.couplejoy.ui.components.ResetSection

@Composable
fun MessageScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var message by remember { mutableStateOf(TextFieldValue("")) }
    val relativeTime by viewModel.relativeTime.collectAsState()

    // Retrieve coupleId and role
    val prefs by lazy { PreferenceManager(context) }
    val role = prefs.getRole()

    val initial = role.takeLast(1).uppercase() // "PartnerA" → "A", "PartnerB" → "B"

    LaunchedEffect(Unit) {
        val coupleId = viewModel.coupleId ?: return@LaunchedEffect
        val role = PreferenceManager(context).getRole()
        viewModel.observeLatestMessage(coupleId, role)
    }

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
                PartnerHeader(initial, timestampText = relativeTime)
                Spacer(modifier = Modifier.height(24.dp))
                MessageInput(
                    message = message,
                    onMessageChange = { message = it },
                    onSend = {
                        //TODO find usage of partner_message
//                        prefs.edit { putString("partner_message", message.text) }

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