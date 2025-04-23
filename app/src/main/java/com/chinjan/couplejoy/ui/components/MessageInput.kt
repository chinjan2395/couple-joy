package com.chinjan.couplejoy.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MessageInput(
    message: TextFieldValue, onMessageChange: (TextFieldValue) -> Unit, onSend: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    var showCheck by remember { mutableStateOf(false) }

    val checkAlpha by animateFloatAsState(if (showCheck) 1f else 0f, label = "Check Alpha")
    val checkScale by animateFloatAsState(if (showCheck) 1.4f else 0.5f, label = "Check Scale")

    val isMessageValid = message.text.isNotBlank()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                placeholder = { Text("Type something lovely...") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFD9BB0), unfocusedBorderColor = Color.LightGray
                ),
                shape = RoundedCornerShape(20.dp)
            )

            if (checkAlpha > 0f) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Message Sent",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-12).dp, y = (-20).dp)
                        .graphicsLayer {
                            scaleX = checkScale
                            scaleY = checkScale
                            alpha = checkAlpha
                        }
                        .size(15.dp),
                    )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (message.text.isBlank()) {
                    return@Button
                }
                scope.launch {
                    scale.animateTo(0.95f)
                    scale.animateTo(1f)

                    // Send message logic
                    onSend()

                    // Show check animation
                    showCheck = true
                    delay(1000)
                    showCheck = false
                }
            },
            enabled = isMessageValid,
            modifier = Modifier.scale(scale.value),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFD9BB0))
        ) {
            Text(
                "Send",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}