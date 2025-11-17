package com.example.usarfire.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.usarfire.Models.Task
import com.example.usarfire.ui.theme.UsarFireTheme

@Composable
fun TaskViewCell(
    modifier: Modifier = Modifier,
    task: Task,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    Card ( modifier = modifier
        .fillMaxWidth()
        .padding(8.dp)
        .clickable{
            // Permite clicar na célula inteira para uma ação, como editar
            onClick()
        }){
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                task.title ?: "Tarefa Sem Título",
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                textDecoration = if (task.isCompleted == true) TextDecoration.LineThrough else null // Risca o texto se a tarefa estiver completa
            )

            // Checkbox para marcar a tarefa como concluída
            Checkbox(
                checked = task.isCompleted ?: false,
                onCheckedChange = {
                    onCheckedChange(it)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskViewCellPreview() {

    UsarFireTheme {
        TaskViewCell(
            task = Task(
                title = "Fazer Compras",
                isCompleted = false
            ),
            onClick = {},
            onCheckedChange = {}
        )
    }
}