package com.example.usarfire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.usarfire.ui.login.LoginView
import com.example.usarfire.ui.tasks.TasksView
import com.example.usarfire.ui.theme.UsarFireTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            UsarFireTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Ecrã de Login
                        composable("login") {
                            LoginView(navController)
                        }

                        // Ecrã principal com lista de tarefas
                        composable("home") {
                            TasksView(navController)
                        }
                    }
                }
            }

            // Verificar se já existe um utilizador autenticado
            LaunchedEffect(Unit) {
                val currentUser = Firebase.auth.currentUser
                if (currentUser != null) {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        }
    }
}