package com.example.usarfire.ui.tasks

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.example.usarfire.Models.Task

data class TaskState(
    var tasks: List<Task> = emptyList(),
    var error: String? = null,
    var isLoading: Boolean? = null
)

public class TaskViewModel : ViewModel() {

    var uiState = mutableStateOf(TaskState())
        private set

    val db = Firebase.firestore
    val userId = Firebase.auth.currentUser?.uid

    fun fetchTasks() {
        if (userId.isNullOrEmpty()) {
            uiState.value = uiState.value.copy(error = "Utilizador não autenticado", isLoading = false)
            return
        }

        uiState.value = uiState.value.copy(isLoading = true)

        // Escuta a coleção 'tasks' filtrando apenas pelas tarefas do utilizador atual
        db
            .collection("tasks")
            .whereEqualTo("userId", userId!!) // Filtro crucial
            .addSnapshotListener { result, error ->
                if (error != null) {
                    uiState.value = uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                    return@addSnapshotListener
                }

                val tasks = result?.documents?.mapNotNull { document ->
                    document.toObject(Task::class.java)?.apply {
                        docId = document.id // Garante que o ID do documento é guardado no modelo
                    }
                } ?: emptyList()

                uiState.value = uiState.value.copy(
                    tasks = tasks,
                    error = null,
                    isLoading = false
                )
            }
    }

    fun addTask(title: String) {
        if (userId.isNullOrEmpty() || title.isBlank()) return

        val newTask = Task(
            title = title,
            isCompleted = false,
            userId = userId
        )

        db.collection("tasks")
            .add(newTask)
            .addOnFailureListener { e ->
                uiState.value = uiState.value.copy(error = "Erro ao adicionar tarefa: ${e.message}")
            }
    }

    fun toggleTaskCompletion(docId: String, isCompleted: Boolean) {
        if (userId.isNullOrEmpty()) return

        // Atualiza o campo 'isCompleted' no documento
        db.collection("tasks")
            .document(docId)
            .update(mapOf("isCompleted" to isCompleted))
    }
}