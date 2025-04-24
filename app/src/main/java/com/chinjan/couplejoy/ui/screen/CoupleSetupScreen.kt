package com.chinjan.couplejoy.ui.screen

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chinjan.couplejoy.R
import com.chinjan.couplejoy.utils.Constants
import com.chinjan.couplejoy.viewmodel.MainViewModel

@Composable
fun CoupleSetupScreen(viewModel: MainViewModel = viewModel(), onContinue: () -> Unit) {
    val context = LocalContext.current
    var coupleId by remember { mutableStateOf("testing") }
    var selectedRole by remember { mutableStateOf(Constants.PARTNER_A) }
    var setupCompleteText = stringResource(R.string.setup_complete)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.enter_couple_code),
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = coupleId,
            onValueChange = { coupleId = it },
            label = { Text(stringResource(R.string.couple_id)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(stringResource(R.string.select_partner_role), style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RoleOption(stringResource(R.string.partner_a), Constants.PARTNER_A, selectedRole) { selectedRole = it }
            RoleOption(stringResource(R.string.partner_b), Constants.PARTNER_B, selectedRole) { selectedRole = it }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                Log.d("Firebase", "onClick selectedRole: $selectedRole, couple: $coupleId")
                if (coupleId.isNotEmpty() && selectedRole.isNotEmpty()) {
                    if (false) {
                        return@Button
                    }

                    viewModel.setupPartner(
                        coupleId = coupleId,
                        selectedRole = selectedRole,
                        context = context,
                        onSuccess = {
                            Toast.makeText(context, setupCompleteText, Toast.LENGTH_SHORT).show()
                            onContinue()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.continue_button_text), color = Color.White)
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