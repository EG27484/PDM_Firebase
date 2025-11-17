package com.example.usarfire.Models


data class Task (
    // ID do documento no Firestore
    var docId  : String? = null,
    // O nome da tarefa (ex: "Ligar ao cliente")
    var title  : String? = null,
    // Estado da tarefa: true se concluída, false se pendente
    var isCompleted : Boolean? = false,
    // ID do utilizador que criou a tarefa (para filtragem)
    var userId : String? = null
)