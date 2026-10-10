
package com.example.room_database_discussion.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.room_database_discussion.data.Student
import com.example.room_database_discussion.viewmodel.StudentFormState
import com.example.room_database_discussion.viewmodel.StudentViewModel
import kotlinx.coroutines.launch

@Composable
fun StudentScreen(viewModel: StudentViewModel) {

    val students by viewModel.students.collectAsStateWithLifecycle()
    val studentCount by viewModel.studentCount.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val courseFilter by viewModel.courseFilter.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    var showForm by rememberSaveable {
        mutableStateOf(false)
    }

    var saveRequested by remember {
        mutableStateOf(false)
    }

    var studentToDelete by remember {
        mutableStateOf<Student?>(null)
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val colors = MaterialTheme.colorScheme

    val courses = listOf(
        "All Courses",
        "BSCpE",
        "BSCS",
        "BSEMC"
    )

    // Hide form after a successful save
    LaunchedEffect(formState) {
        if (saveRequested && formState == StudentFormState()) {
            showForm = false
            saveRequested = false
        }
    }

    Scaffold(
        containerColor = colors.background
    ) { innerPadding ->

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 20.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Header and Add button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Student Management",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onBackground
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "Manage student records",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.cancelEditing()
                            saveRequested = false
                            showForm = true
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+ Add")
                    }
                }
            }

            // Total student count
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "TOTAL STUDENTS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onPrimaryContainer
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = studentCount.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )

                        Text(
                            text = "Registered student records",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onPrimaryContainer
                        )
                    }
                }
            }

            // Add/Edit form
            if (showForm) {
                item {
                    StudentFormCard(
                        state = formState,
                        viewModel = viewModel,

                        onSave = {
                            saveRequested = true
                            viewModel.saveStudent()
                        },

                        onCancel = {
                            viewModel.cancelEditing()
                            saveRequested = false
                            showForm = false
                        }
                    )
                }
            }

            // Search students
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Search Students",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::setSearchQuery,
                        placeholder = {
                            Text("Enter student name")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = cleanTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Course filters
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Filter by Course",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        courses.forEach { course ->

                            FilterChip(
                                selected = courseFilter == course,

                                onClick = {
                                    viewModel.setCourseFilter(course)
                                },

                                label = {
                                    Text(course)
                                },

                                shape = RoundedCornerShape(10.dp),

                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White,
                                    labelColor = colors.onSurface,
                                    selectedContainerColor = colors.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Student list header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Student Directory",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )

                    Text(
                        text = "${students.size} shown",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            // Student cards
            if (students.isEmpty()) {

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        border = BorderStroke(
                            1.dp,
                            colors.outline
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No students found",
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = "Add a student or change your filters.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }

            } else {

                items(
                    items = students,
                    key = { it.id }
                ) { student ->

                    StudentCard(
                        student = student,

                        onEdit = {
                            viewModel.startEditing(student)
                            saveRequested = false
                            showForm = true

                            scope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },

                        onDelete = {
                            studentToDelete = student
                        }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    studentToDelete?.let { student ->

        AlertDialog(
            onDismissRequest = {
                studentToDelete = null
            },

            containerColor = Color.White,

            title = {
                Text("Delete Student")
            },

            text = {
                Text(
                    "Are you sure you want to delete ${student.name}?"
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteStudent(student)

                        if (formState.editingStudentId == student.id) {
                            showForm = false
                        }

                        studentToDelete = null
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = colors.error
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        studentToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}


// Add and Edit Student Form
@Composable
private fun StudentFormCard(
    state: StudentFormState,
    viewModel: StudentViewModel,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {

    val colors = MaterialTheme.colorScheme

    var courseExpanded by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            colors.outline
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = if (state.editingStudentId == null) {
                    "Add New Student"
                } else {
                    "Edit Student"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Name
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Full Name") },
                isError = state.nameError != null,
                enabled = !state.isSaving,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = cleanTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            FormError(state.nameError)

            // Email
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email Address") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                isError = state.emailError != null,
                enabled = !state.isSaving,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = cleanTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            FormError(state.emailError)

            // Course
            Text(
                text = "Course",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )

            Box {
                OutlinedButton(
                    onClick = {
                        courseExpanded = true
                    },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (state.courseError != null) {
                            colors.error
                        } else {
                            colors.outline
                        }
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = colors.onSurface
                    )
                ) {
                    Text(
                        text = state.course.ifBlank {
                            "Select Course"
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Text("⌄")
                }

                DropdownMenu(
                    expanded = courseExpanded,
                    onDismissRequest = {
                        courseExpanded = false
                    },
                    containerColor = Color.White
                ) {
                    listOf(
                        "BSCpE",
                        "BSCS",
                        "BSEMC"
                    ).forEach { course ->

                        DropdownMenuItem(
                            text = { Text(course) },

                            onClick = {
                                viewModel.onCourseChange(course)
                                courseExpanded = false
                            }
                        )
                    }
                }
            }

            FormError(state.courseError)

            // Age
            OutlinedTextField(
                value = state.age,
                onValueChange = viewModel::onAgeChange,
                label = { Text("Age (Optional)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                isError = state.ageError != null,
                enabled = !state.isSaving,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = cleanTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            FormError(state.ageError)
            FormError(state.generalError)

            // Form action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !state.isSaving,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = onSave,
                    enabled = !state.isSaving,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = if (state.editingStudentId == null) {
                                "Add Student"
                            } else {
                                "Save Changes"
                            }
                        )
                    }
                }
            }
        }
    }
}


// Individual Student Card
@Composable
private fun StudentCard(
    student: Student,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            colors.outline
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Student name and email
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            colors.primaryContainer,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name
                            .trim()
                            .firstOrNull()
                            ?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = student.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Course and age
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Surface(
                    color = colors.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = student.course,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onPrimaryContainer
                    )
                }

                Text(
                    text = if (student.age == 0) {
                        "Age: Not specified"
                    } else {
                        "Age: ${student.age}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            HorizontalDivider(
                color = colors.outlineVariant
            )

            // Edit and Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Edit")
                }

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.error
                    ),
                    border = BorderStroke(
                        1.dp,
                        colors.error
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}


// Form Validation Messages
@Composable
private fun FormError(message: String?) {

    if (message != null) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}


// Consistent White Text Fields
@Composable
private fun cleanTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,

        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,

        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,

        cursorColor = MaterialTheme.colorScheme.primary
    )
