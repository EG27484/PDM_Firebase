package com.example.usarfire.ui.tasks

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.usarfire.models.TaskCategory
import com.example.usarfire.ui.theme.UsarFireTheme
import androidx.core.graphics.toColorInt

@Composable
fun CategoryFilterChips(
    selectedCategory: TaskCategory?,
    onCategorySelected: (TaskCategory?) -> Unit,
    taskCounts: Map<TaskCategory, Int> = emptyMap(),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chip "Todas"
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = {
                Text(
                    text = "Todas",
                    fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Normal
                )
            },
            leadingIcon = if (selectedCategory == null) {
                {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else null
        )

        // Chips para cada categoria
        TaskCategory.entries.forEach { category ->
            val count = taskCounts[category] ?: 0
            if (count > 0) { // Só mostrar categorias com tarefas
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        onCategorySelected(
                            if (selectedCategory == category) null else category
                        )
                    },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = category.displayName,
                                fontWeight = if (selectedCategory == category)
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal
                            )
                            // Badge com contagem
                            if (count > 0) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = Color(category.colorHex.toColorInt())
                                        .copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = count.toString(),
                                        modifier = Modifier.padding(horizontal = 6.dp),
                                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                        color = Color(category.colorHex.toColorInt())
                                    )
                                }
                            }
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = getCategoryIcon(category),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (selectedCategory == category)
                                MaterialTheme.colorScheme.primary
                            else
                                Color(category.colorHex.toColorInt())
                        )
                    }
                )
            }
        }
    }
}

// Função para obter ícone por categoria
private fun getCategoryIcon(category: TaskCategory): ImageVector {
    return when (category) {
        TaskCategory.PERSONAL -> Icons.Default.Person
        TaskCategory.WORK -> Icons.Default.Work
        TaskCategory.SHOPPING -> Icons.Default.ShoppingCart
        TaskCategory.HEALTH -> Icons.Default.Favorite
        TaskCategory.STUDY -> Icons.Default.School
        TaskCategory.HOME -> Icons.Default.Home
        TaskCategory.OTHER -> Icons.Default.MoreHoriz
    }
}

@Composable
fun TaskStatsCard(
    stats: TaskStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Resumo das Tarefas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Estatísticas gerais
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total",
                    value = stats.total.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    label = "Concluídas",
                    value = stats.completed.toString(),
                    color = Color(0xFF4CAF50)
                )
                StatItem(
                    label = "Pendentes",
                    value = stats.pending.toString(),
                    color = Color(0xFFFF9800)
                )
            }

            // Progresso
            if (stats.total > 0) {
                Spacer(modifier = Modifier.height(12.dp))

                val progress = stats.completed.toFloat() / stats.total

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF4CAF50),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "${(progress * 100).toInt()}% concluído",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryFilterChipsPreview() {
    UsarFireTheme {
        Column {
            CategoryFilterChips(
                selectedCategory = TaskCategory.WORK,
                onCategorySelected = {},
                taskCounts = mapOf(
                    TaskCategory.PERSONAL to 5,
                    TaskCategory.WORK to 3,
                    TaskCategory.SHOPPING to 2
                )
            )

            TaskStatsCard(
                stats = TaskStats(
                    total = 10,
                    completed = 6,
                    pending = 4,
                    byCategory = emptyList()
                )
            )
        }
    }
}