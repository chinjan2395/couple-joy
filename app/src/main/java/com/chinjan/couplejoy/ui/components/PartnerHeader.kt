package com.chinjan.couplejoy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chinjan.couplejoy.R

@Composable
fun PartnerHeader(ownerInitial: String, partnerInitial: String, lastMessage: Message?, timestampText: String, coupleId: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEA3AA).copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Couple ID: ${coupleId}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        if ((lastMessage != null && lastMessage.message.isNotBlank()) && timestampText.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MessageBubble(initial = partnerInitial, text = lastMessage.message.toString(), timestamp = timestampText)
            }
            /*Text(
                text = timestampText,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        /*Spacer(modifier = Modifier.height(8.dp))*/

        /*Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = Color(0xFF333333)
        )*/

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.message_screen_title),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )
    }
}