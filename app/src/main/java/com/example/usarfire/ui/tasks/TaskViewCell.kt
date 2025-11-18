package com.example.usarfire.ui.tasks

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.usarfire.models.Task
import com.example.usarfire.models.TaskCategory
import com.example.usarfire.ui.theme.UsarFireTheme
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.toColorInt
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskViewCell(
    modifier: Modifier = Modifier,
    task: Task,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val category = task.getCategoryEnum()
    val categoryColor = Color(category.colorHex.toColorInt())

    // Estado local para o checkbox
    val isChecked = task.isCompleted ?: false

    // Animação de opacidade para tarefas concluídas
    val alpha by animateFloatAsState(
        targetValue = if (isChecked) 0.6f else 1f,
        label = "alpha"
    )

    // Animação de cor do card
    val cardColor by animateColorAsState(
        targetValue = if (isChecked)
            MaterialTheme.colorScheme.surfaceVariant
        else
            MaterialTheme.colorScheme.surface,
        label = "cardColor"
    )

    // Estado para mostrar/esconder o botão de delete
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .alpha(alpha),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isChecked) 0.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra lateral colorida indicando a categoria
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(categoryColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Checkbox com log para debug
            Checkbox(
                checked = isChecked,
                onCheckedChange = { newValue ->
                    Log.d("TaskViewCell", "Checkbox clicked! Task: ${task.title}, Old: $isChecked, New: $newValue")
                    onCheckedChange(newValue)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = categoryColor,
                    uncheckedColor = categoryColor.copy(alpha = 0.6f)
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Conteúdo da tarefa
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Título da tarefa
                Text(
                    text = task.title ?: "Tarefa Sem Título",
                    fontSize = 16.sp,
                    fontWeight = if (isChecked) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (isChecked)
                        TextDecoration.LineThrough
                    else
                        TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Linha com categoria e data
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge da categoria
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = categoryColor.copy(alpha = 0.2f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = category.displayName,
                            fontSize = 12.sp,
                            color = categoryColor,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    // Data
                    Text(
                        text = formatDate(
                            if (isChecked && task.completedAt != null)
                                task.completedAt!!
                            else
                                task.createdAt ?: System.currentTimeMillis()
                        ),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Botão de eliminar
            IconButton(
                onClick = { showDeleteDialog = true }
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }

    // Dialog de confirmação para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Tarefa") },
            text = {
                Text("Tens a certeza que queres eliminar '${task.title}'?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Função auxiliar para formatar datas
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
    val today = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = timestamp }

    return when {
        isSameDay(today, date) -> "Hoje"
        isYesterday(today, date) -> "Ontem"
        else -> sdf.format(Date(timestamp))
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(today: Calendar, date: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = today.timeInMillis
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return isSameDay(yesterday, date)
}

@Preview(showBackground = true)
@Composable
fun TaskViewCellPreview() {
    UsarFireTheme {
        Column {
            TaskViewCell(
                task = Task(
                    title = "Reunião com equipa de desenvolvimento",
                    isCompleted = false,
                    category = TaskCategory.WORK.name
                ),
                onClick = {},
                onCheckedChange = {},
                onDelete = {}
            )

            TaskViewCell(
                task = Task(
                    title = "Comprar leite e pão",
                    isCompleted = true,
                    category = TaskCategory.SHOPPING.name
                ),
                onClick = {},
                onCheckedChange = {},
                onDelete = {}
            )
        }
    }
}