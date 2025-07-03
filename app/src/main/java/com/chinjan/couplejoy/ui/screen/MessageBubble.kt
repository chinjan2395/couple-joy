package com.chinjan.couplejoy.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun MessageBubble(initial: String, text: String, timestamp: String) {
    Row(
        modifier = Modifier
            .wrapContentWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Center
    ) {
        // Initial Circle (Left)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEA3AA)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Message Bubble (Right)
        Column(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}