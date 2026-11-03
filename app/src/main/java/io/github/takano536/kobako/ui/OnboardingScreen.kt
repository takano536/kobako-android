package io.github.takano536.kobako.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.R
import io.github.takano536.kobako.ui.theme.ThemeTokens

@Composable
fun OnboardingScreen(viewModel: AppViewModel) {
    var url by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var showToken by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("kobako", style = androidx.compose.material3.MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.onboarding_tagline), color = ThemeTokens.muted)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.server_url)) },
            placeholder = { Text("https://example.com") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.api_token)) },
            singleLine = true,
            visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                androidx.compose.material3.TextButton(onClick = { showToken = !showToken }) {
                    Text(stringResource(if (showToken) R.string.hide_token else R.string.show_token))
                }
            },
        )
        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = ThemeTokens.moneyNegative)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                viewModel.connect(url, token) { ok ->
                    haptic.performHapticFeedback(
                        if (ok) androidx.compose.ui.hapticfeedback.HapticFeedbackType.Confirm
                        else androidx.compose.ui.hapticfeedback.HapticFeedbackType.Reject,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = url.isNotBlank() && token.isNotBlank(),
        ) { Text(stringResource(R.string.connect_server)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = viewModel::enterDemo, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text(stringResource(R.string.demo_view))
        }
    }
}
