package com.openplaud.app.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openplaud.app.LocalApi
import com.openplaud.app.LocalPrefs
import com.openplaud.app.ui.theme.PlaudColors
import com.openplaud.app.ui.theme.PlaudTheme

@Composable
fun SetupScreen(onConfigured: () -> Unit) {
    val api = LocalApi.current
    val prefs = LocalPrefs.current
    val viewModel: SetupViewModel = viewModel(factory = SetupViewModelFactory(prefs, api))
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isReady) {
        if (uiState.isReady) {
            Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show()
            onConfigured()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = PlaudColors.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Plaud", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PlaudColors.primary)
                Text("Connect your OpenPlaud server", fontSize = 14.sp, color = PlaudColors.textSecondary)
                Divider(color = PlaudTheme.colors.surfaceVariant)

                OutlinedTextField(
                    value = uiState.serverUrl,
                    onValueChange = viewModel::onServerUrlChange,
                    label = { Text("Server URL") },
                    placeholder = { Text("http://134.175.249.19") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )

                OutlinedTextField(
                    value = uiState.apiKey,
                    onValueChange = viewModel::onApiKeyChange,
                    label = { Text("API Key") },
                    placeholder = { Text("op_...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                if (uiState.error != null) Text(uiState.error!!, color = PlaudColors.error, fontSize = 13.sp)
                if (uiState.isLoading) CircularProgressIndicator(color = PlaudColors.primary, modifier = Modifier.size(24.dp))

                Button(
                    onClick = viewModel::connect,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PlaudColors.primary),
                    enabled = uiState.serverUrl.isNotBlank() && uiState.apiKey.isNotBlank() && !uiState.isLoading
                ) { Text("Connect", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }

                Text("Get your API key from OpenPlaud Settings → API Keys", fontSize = 12.sp, color = PlaudColors.textSecondary)
            }
        }
    }
}
