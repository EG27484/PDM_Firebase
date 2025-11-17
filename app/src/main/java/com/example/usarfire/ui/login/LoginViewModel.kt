package com.example.usarfire.ui.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

data class LoginState(
    var email: String? = null,
    var password: String? = null,
    var error: String? = null,
    var isLoading: Boolean? = null
)

class LoginViewModel : ViewModel() {

    var uiState = mutableStateOf(LoginState())
        private set

    private val auth: FirebaseAuth = Firebase.auth

    fun setEmail(email: String) {
        uiState.value = uiState.value.copy(email = email)
    }

    fun setPassword(password: String) {
        uiState.value = uiState.value.copy(password = password)
    }

    fun login(onLoginSuccess: () -> Unit) {
        // Validação dos campos
        if (uiState.value.email.isNullOrEmpty()) {
            uiState.value = uiState.value.copy(
                error = "Email é obrigatório",
                isLoading = false
            )
            return
        }

        if (uiState.value.password.isNullOrEmpty()) {
            uiState.value = uiState.value.copy(
                error = "Password é obrigatória",
                isLoading = false
            )
            return
        }

        // Iniciar loading
        uiState.value = uiState.value.copy(isLoading = true, error = null)

        // Fazer login com Firebase
        auth.signInWithEmailAndPassword(
            uiState.value.email!!,
            uiState.value.password!!
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Login bem-sucedido
                uiState.value = uiState.value.copy(
                    error = null,
                    isLoading = false
                )
                onLoginSuccess()
            } else {
                // Login falhou
                val errorMessage = when (task.exception?.message) {
                    "The email address is badly formatted." -> "Email inválido"
                    "There is no user record corresponding to this identifier. The user may have been deleted." -> "Utilizador não encontrado"
                    "The password is invalid or the user does not have a password." -> "Password incorreta"
                    else -> task.exception?.message ?: "Erro ao fazer login"
                }

                uiState.value = uiState.value.copy(
                    error = errorMessage,
                    isLoading = false
                )
            }
        }
    }

    fun register(onRegisterSuccess: () -> Unit) {
        // Validação dos campos
        if (uiState.value.email.isNullOrEmpty()) {
            uiState.value = uiState.value.copy(
                error = "Email é obrigatório",
                isLoading = false
            )
            return
        }

        if (uiState.value.password.isNullOrEmpty()) {
            uiState.value = uiState.value.copy(
                error = "Password é obrigatória",
                isLoading = false
            )
            return
        }

        if ((uiState.value.password?.length ?: 0) < 6) {
            uiState.value = uiState.value.copy(
                error = "Password deve ter pelo menos 6 caracteres",
                isLoading = false
            )
            return
        }

        // Iniciar loading
        uiState.value = uiState.value.copy(isLoading = true, error = null)

        // Criar conta com Firebase
        auth.createUserWithEmailAndPassword(
            uiState.value.email!!,
            uiState.value.password!!
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Registo bem-sucedido
                uiState.value = uiState.value.copy(
                    error = null,
                    isLoading = false
                )
                onRegisterSuccess()
            } else {
                // Registo falhou
                val errorMessage = when (task.exception?.message) {
                    "The email address is already in use by another account." -> "Este email já está registado"
                    "The email address is badly formatted." -> "Email inválido"
                    else -> task.exception?.message ?: "Erro ao criar conta"
                }

                uiState.value = uiState.value.copy(
                    error = errorMessage,
                    isLoading = false
                )
            }
        }
    }
}