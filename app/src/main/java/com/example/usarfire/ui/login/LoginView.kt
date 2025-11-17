package com.example.usarfire.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.usarfire.ui.theme.Purple40
import com.example.usarfire.ui.theme.UsarFireTheme

private val compose: Any

@Composable
fun LoginView(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val viewModel: LoginViewModel = viewModel()
    val uiState = viewModel.uiState.value

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ícone da app
        Image(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp),
            colorFilter = ColorFilter.tint(Purple40)
        )

        Text(
            text = "Lista de Tarefas",
            modifier = Modifier.padding(8.dp),
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )

        // Campo de email
        TextField(
            modifier = Modifier.padding(8.dp),
            value = uiState.email ?: "",
            onValueChange = {
                viewModel.setEmail(it)
            },
            label = { Text("Email") },
            enabled = uiState.isLoading != true
        )

        // Campo de password
        TextField(
            modifier = Modifier.padding(8.dp),
            value = uiState.password ?: "",
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = {
                viewModel.setPassword(it)
            },
            label = { Text("Password") },
            enabled = uiState.isLoading != true
        )

        // Mensagem de erro
        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                modifier = Modifier.padding(8.dp),
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        // Loading indicator
        if (uiState.isLoading == true) {
            CircularProgressIndicator(
                modifier = Modifier.padding(8.dp)
            )
        } else {
            // Botão de Login
            Button(
                onClick = {
                    viewModel.login(onLoginSuccess = {
                        navController.navigate("home")
                    })
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    "Entrar",
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Botão de Registo
            Button(
                onClick = {
                    viewModel.register(onRegisterSuccess = {
                        navController.navigate("home")
                    })
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    "Criar Conta",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginViewPreview() {
    UsarFireTheme {
        LoginView(navController = rememberNavController())
    }
}