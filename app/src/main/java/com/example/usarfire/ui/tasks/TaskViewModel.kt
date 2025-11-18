package com.example.usarfire.ui.tasks

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.usarfire.models.Task
import com.example.usarfire.models.TaskCategory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import android.util.Log

data class TaskState(
    var tasks: List<Task> = emptyList(),
    var filteredTasks: List<Task> = emptyList(),
    var error: String? = null,
    var isLoading: Boolean? = null,
    var selectedCategory: TaskCategory? = null,
    var showCompletedTasks: Boolean = true
)

class TaskViewModel : ViewModel() {

    var uiState = mutableStateOf(TaskState())
        private set

    private val db = Firebase.firestore
    private val userId = Firebase.auth.currentUser?.uid

    fun fetchTasks() {
        if (userId.isNullOrEmpty()) {
            uiState.value = uiState.value.copy(
                error = "Utilizador não autenticado",
                isLoading = false
            )
            return
        }

        uiState.value = uiState.value.copy(isLoading = true)

        db
            .collection("tasks")
            .whereEqualTo("userId", userId!!)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { result, error ->
                if (error != null) {
                    Log.e("TaskViewModel", "Error fetching tasks: ${error.message}")
                    uiState.value = uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                    return@addSnapshotListener
                }

                val tasks = result?.documents?.mapNotNull { document ->
                    try {
                        val task = Task(
                            docId = document.id,
                            title = document.getString("title"),
                            isCompleted = document.getBoolean("isCompleted") ?: false,
                            userId = document.getString("userId"),
                            category = document.getString("category") ?: TaskCategory.OTHER.name,
                            createdAt = document.getLong("createdAt") ?: System.currentTimeMillis(),
                            completedAt = document.getLong("completedAt")
                        )
                        Log.d("TaskViewModel", "Fetched task: ${task.title}, isCompleted: ${task.isCompleted}")
                        task
                    } catch (e: Exception) {
                        Log.e("TaskViewModel", "Error parsing task: ${e.message}")
                        null
                    }
                } ?: emptyList()

                Log.d("TaskViewModel", "Total tasks fetched: ${tasks.size}")
                uiState.value = uiState.value.copy(
                    tasks = tasks,
                    error = null,
                    isLoading = false
                )

                applyFilters()
            }
    }

    fun addTask(title: String, category: TaskCategory = TaskCategory.OTHER) {
        if (userId.isNullOrEmpty() || title.isBlank()) return

        val newTask = hashMapOf(
            "title" to title.trim(),
            "isCompleted" to false,
            "userId" to userId,
            "category" to category.name,
            "createdAt" to System.currentTimeMillis(),
            "completedAt" to null
        )

        db.collection("tasks")
            .add(newTask)
            .addOnSuccessListener {
                Log.d("TaskViewModel", "Task added successfully")
            }
            .addOnFailureListener { e ->
                Log.e("TaskViewModel", "Error adding task: ${e.message}")
                uiState.value = uiState.value.copy(
                    error = "Erro ao adicionar tarefa: ${e.message}"
                )
            }
    }

    fun toggleTaskCompletion(docId: String, isCompleted: Boolean) {
        Log.d("TaskViewModel", "toggleTaskCompletion called - docId: $docId, isCompleted: $isCompleted")

        val timestamp = System.currentTimeMillis()

        // Atualização otimista: atualiza o UI imediatamente
        val updatedTasks = uiState.value.tasks.map { task ->
            if (task.docId == docId) {
                Log.d("TaskViewModel", "Updating task: ${task.title} from ${task.isCompleted} to $isCompleted")
                task.copy(
                    isCompleted = isCompleted,
                    completedAt = if (isCompleted) timestamp else null
                )
            } else {
                task
            }
        }

        Log.d("TaskViewModel", "Updated tasks count: ${updatedTasks.size}")
        uiState.value = uiState.value.copy(tasks = updatedTasks)
        applyFilters()

        // Atualiza no Firestore em background
        val updates = hashMapOf<String, Any?>(
            "isCompleted" to isCompleted,
            "completedAt" to if (isCompleted) timestamp else null
        )

        db.collection("tasks")
            .document(docId)
            .update(updates)
            .addOnSuccessListener {
                Log.d("TaskViewModel", "Firestore update successful for docId: $docId")
            }
            .addOnFailureListener { e ->
                Log.e("TaskViewModel", "Firestore update failed: ${e.message}")
                // Em caso de erro, reverte a mudança local
                val revertedTasks = uiState.value.tasks.map { task ->
                    if (task.docId == docId) {
                        task.copy(
                            isCompleted = !isCompleted,
                            completedAt = null
                        )
                    } else {
                        task
                    }
                }
                uiState.value = uiState.value.copy(
                    tasks = revertedTasks,
                    error = "Erro ao atualizar tarefa: ${e.message}"
                )
                applyFilters()
            }
    }

    fun deleteTask(docId: String) {
        if (userId.isNullOrEmpty()) return

        db.collection("tasks")
            .document(docId)
            .delete()
            .addOnFailureListener { e ->
                uiState.value = uiState.value.copy(
                    error = "Erro ao eliminar tarefa: ${e.message}"
                )
            }
    }

    fun updateTask(docId: String, title: String, category: TaskCategory) {
        if (userId.isNullOrEmpty() || title.isBlank()) return

        val updates = hashMapOf<String, Any>(
            "title" to title.trim(),
            "category" to category.name
        )

        db.collection("tasks")
            .document(docId)
            .update(updates)
            .addOnFailureListener { e ->
                uiState.value = uiState.value.copy(
                    error = "Erro ao atualizar tarefa: ${e.message}"
                )
            }
    }

    fun setSelectedCategory(category: TaskCategory?) {
        uiState.value = uiState.value.copy(selectedCategory = category)
        applyFilters()
    }

    fun toggleShowCompleted() {
        uiState.value = uiState.value.copy(
            showCompletedTasks = !uiState.value.showCompletedTasks
        )
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = uiState.value.tasks

        // Filtrar por categoria se selecionada
        uiState.value.selectedCategory?.let { category ->
            filtered = filtered.filter { task ->
                task.getCategoryEnum() == category
            }
        }

        // Filtrar tarefas completadas se necessário
        if (!uiState.value.showCompletedTasks) {
            filtered = filtered.filter { task ->
                task.isCompleted != true
            }
        }

        uiState.value = uiState.value.copy(filteredTasks = filtered)
    }

    fun getTaskStats(): TaskStats {
        val total = uiState.value.tasks.size
        val completed = uiState.value.tasks.count { it.isCompleted == true }
        val pending = total - completed

        val byCategory = TaskCategory.entries.map { category ->
            val categoryTasks = uiState.value.tasks.filter {
                it.getCategoryEnum() == category
            }
            CategoryStats(
                category = category,
                total = categoryTasks.size,
                completed = categoryTasks.count { it.isCompleted == true }
            )
        }.filter { it.total > 0 }

        return TaskStats(
            total = total,
            completed = completed,
            pending = pending,
            byCategory = byCategory
        )
    }
}

data class TaskStats(
    val total: Int,
    val completed: Int,
    val pending: Int,
    val byCategory: List<CategoryStats>
)

data class CategoryStats(
    val category: TaskCategory,
    val total: Int,
    val completed: Int
)