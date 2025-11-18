package com.example.usarfire.models

// Enum para as categorias disponíveis
enum class TaskCategory(val displayName: String, val colorHex: String) {
    PERSONAL("Pessoal", "#4CAF50"),      // Verde
    WORK("Trabalho", "#2196F3"),         // Azul
    SHOPPING("Compras", "#FF9800"),      // Laranja
    HEALTH("Saúde", "#F44336"),          // Vermelho
    STUDY("Estudos", "#9C27B0"),         // Roxo
    HOME("Casa", "#795548"),              // Castanho
    OTHER("Outros", "#607D8B");          // Cinza-azulado

    companion object {
        fun fromString(value: String?): TaskCategory {
            return entries.find { it.name == value } ?: OTHER
        }
    }
}

data class Task(
    // ID do documento no Firestore
    var docId: String? = null,

    // O nome da tarefa (ex: "Ligar ao cliente")
    var title: String? = null,

    // Estado da tarefa: true se concluída, false se pendente
    // IMPORTANTE: usar var (não val) para permitir que o Firestore faça o set
    var isCompleted: Boolean = false,

    // ID do utilizador que criou a tarefa (para filtragem)
    var userId: String? = null,

    // Categoria da tarefa (como String para compatibilidade com Firestore)
    var category: String? = TaskCategory.OTHER.name,

    // Data de criação (timestamp)
    var createdAt: Long = System.currentTimeMillis(),

    // Data de conclusão (timestamp) - null se não concluída
    var completedAt: Long? = null
) {
    // Construtor sem argumentos necessário para o Firestore
    constructor() : this(
        docId = null,
        title = null,
        isCompleted = false,
        userId = null,
        category = TaskCategory.OTHER.name,
        createdAt = System.currentTimeMillis(),
        completedAt = null
    )

    // Função helper para obter a categoria como enum
    fun getCategoryEnum(): TaskCategory {
        return TaskCategory.fromString(category)
    }
}