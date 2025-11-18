@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.usarfire.ui.tasks

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.usarfire.models.Task
import com.example.usarfire.models.TaskCategory
import com.example.usarfire.ui.theme.UsarFireTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.core.graphics.toColorInt
import androidx.compose.material3.ExposedDropdownMenuAnchorType



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksView(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val viewModel: TaskViewModel = viewModel()
    val uiState by viewModel.uiState

    // Estados locais
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var showStats by remember { mutableStateOf(false) }

    // Carregar tarefas quando o composable é criado
    LaunchedEffect(Unit) {
        viewModel.fetchTasks()
    }

    // Calcular contagens por categoria
    val taskCountsByCategory = remember(uiState.tasks) {
        uiState.tasks.groupBy { it.getCategoryEnum() }
            .mapValues { it.value.size }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("As Minhas Tarefas")
                        Firebase.auth.currentUser?.email?.let { email ->
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    // Toggle para mostrar/esconder concluídas
                    IconButton(onClick = { viewModel.toggleShowCompleted() }) {
                        Icon(
                            if (uiState.showCompletedTasks)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (uiState.showCompletedTasks)
                                "Esconder Concluídas"
                            else
                                "Mostrar Concluídas"
                        )
                    }

                    // Botão de estatísticas
                    IconButton(onClick = { showStats = !showStats }) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = "Estatísticas"
                        )
                    }

                    // Botão de logout
                    IconButton(onClick = {
                        Firebase.auth.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sair"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                text = { Text("Nova Tarefa") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filtros de categoria
            CategoryFilterChips(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.setSelectedCategory(it) },
                taskCounts = taskCountsByCategory
            )

            // Card de estatísticas (animado)
            AnimatedVisibility(
                visible = showStats,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                TaskStatsCard(
                    stats = viewModel.getTaskStats()
                )
            }

            // Conteúdo principal
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    // Estado de loading
                    uiState.isLoading == true -> {
                        CircularProgressIndicator()
                    }

                    // Estado de erro
                    uiState.error != null -> {
                        ErrorMessage(
                            message = uiState.error!!,
                            onRetry = { viewModel.fetchTasks() }
                        )
                    }

                    // Lista vazia
                    uiState.filteredTasks.isEmpty() -> {
                        EmptyTasksMessage(
                            hasFilter = uiState.selectedCategory != null || !uiState.showCompletedTasks
                        )
                    }

                    // Lista com tarefas
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            itemsIndexed(
                                items = uiState.filteredTasks,
                                key = { _, task -> task.docId ?: "" }
                            ) { index, task ->
                                this@Column.AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + slideInVertically(),
                                    exit = fadeOut() + slideOutHorizontally()
                                ) {
                                    TaskViewCell(
                                        task = task,
                                        onClick = {
                                            selectedTask = task
                                            showEditDialog = true
                                        },
                                        onCheckedChange = { isChecked ->
                                            viewModel.toggleTaskCompletion(
                                                docId = task.docId!!,
                                                isCompleted = isChecked
                                            )
                                        },
                                        onDelete = {
                                            viewModel.deleteTask(task.docId!!)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialog para adicionar nova tarefa
        if (showAddDialog) {
            AddEditTaskDialog(
                task = null,
                onDismiss = { showAddDialog = false },
                onSave = { title, category ->
                    viewModel.addTask(title, category)
                    showAddDialog = false
                }
            )
        }

        // Dialog para editar tarefa
        if (showEditDialog && selectedTask != null) {
            AddEditTaskDialog(
                task = selectedTask,
                onDismiss = {
                    showEditDialog = false
                    selectedTask = null
                },
                onSave = { title, category ->
                    viewModel.updateTask(
                        docId = selectedTask!!.docId!!,
                        title = title,
                        category = category
                    )
                    showEditDialog = false
                    selectedTask = null
                }
            )
        }
    }
}

@Composable
fun AddEditTaskDialog(
    task: Task?,
    onDismiss: () -> Unit,
    onSave: (String, TaskCategory) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var selectedCategory by remember {
        mutableStateOf(task?.getCategoryEnum() ?: TaskCategory.PERSONAL)
    }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (task == null) "Nova Tarefa" else "Editar Tarefa")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Campo de título
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título da tarefa") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )

                // Seletor de categoria
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.SecondaryEditable, enabled = true)
                        ,
                                colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(
                                selectedCategory.colorHex.toColorInt()
                            ),
                            focusedBorderColor = Color(
                                selectedCategory.colorHex.toColorInt()
                            )
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        TaskCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(
                                                    Color(category.colorHex.toColorInt()),
                                                    shape = MaterialTheme.shapes.small
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(category.displayName)
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, selectedCategory)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text(if (task == null) "Adicionar" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EmptyTasksMessage(hasFilter: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = if (hasFilter) Icons.Default.FilterList else Icons.Default.TaskAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (hasFilter)
                "Nenhuma tarefa encontrada com os filtros aplicados"
            else
                "Ainda não tens tarefas",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (hasFilter)
                "Tenta ajustar os filtros ou criar uma nova tarefa"
            else
                "Clica no botão '+' para adicionar a tua primeira tarefa",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ocorreu um erro",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Tentar Novamente")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TasksViewPreview() {
    UsarFireTheme {
        TasksView(navController = rememberNavController())
    }
}