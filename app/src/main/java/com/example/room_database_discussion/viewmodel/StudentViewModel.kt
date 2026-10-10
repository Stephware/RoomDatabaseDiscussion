package com.example.room_database_discussion.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.room_database_discussion.data.Student
import com.example.room_database_discussion.data.StudentDatabase
import com.example.room_database_discussion.data.StudentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Form state for adding and editing students
data class StudentFormState(
    val name: String = "",
    val email: String = "",
    val course: String = "",
    val age: String = "",
    val editingStudentId: Int? = null,
    val nameError: String? = null,
    val emailError: String? = null,
    val courseError: String? = null,
    val ageError: String? = null,
    val generalError: String? = null,
    val isSaving: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class StudentViewModel(
    application: Application
) : AndroidViewModel(application) {

    companion object {
        const val ALL_COURSES = "All Courses"
    }

    private val repository = StudentRepository(
        StudentDatabase.getData(application).studentDao()
    )

    // Private search state
    private val _searchQuery = MutableStateFlow("")

    val searchQuery: StateFlow<String> =
        _searchQuery.asStateFlow()

    // Private course filter state
    private val _courseFilter = MutableStateFlow(ALL_COURSES)

    val courseFilter: StateFlow<String> =
        _courseFilter.asStateFlow()

    // Private form state
    private val _formState = MutableStateFlow(StudentFormState())

    val formState: StateFlow<StudentFormState> =
        _formState.asStateFlow()

    // Reactive student list
    val students: StateFlow<List<Student>> =
        combine(
            _searchQuery,
            _courseFilter
        ) { search, course ->
            Pair(search.trim(), course)
        }.flatMapLatest { (search, course) ->

            when {
                search.isEmpty() && course == ALL_COURSES ->
                    repository.students

                search.isNotEmpty() && course == ALL_COURSES ->
                    repository.searchStudents(search)

                search.isEmpty() ->
                    repository.filterStudentsByCourse(course)

                else ->
                    repository.searchStudentsByCourse(
                        search,
                        course
                    )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Count derived from Room database
    val studentCount: StateFlow<Int> =
        repository.studentCount.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Update search input
    fun setSearchQuery(value: String) {
        _searchQuery.value = value
    }

    // Update selected course filter
    fun setCourseFilter(value: String) {
        _courseFilter.value = value
    }

    // Form field changes
    fun onNameChange(value: String) {
        _formState.update {
            it.copy(
                name = value,
                nameError = null
            )
        }
    }

    fun onEmailChange(value: String) {
        _formState.update {
            it.copy(
                email = value,
                emailError = null
            )
        }
    }

    fun onCourseChange(value: String) {
        _formState.update {
            it.copy(
                course = value,
                courseError = null
            )
        }
    }

    fun onAgeChange(value: String) {
        _formState.update {
            it.copy(
                age = value,
                ageError = null
            )
        }
    }

    // Fill the form with a selected student's information
    fun startEditing(student: Student) {

        if (_formState.value.isSaving) return

        _formState.value = StudentFormState(
            name = student.name,
            email = student.email,
            course = student.course,
            age = if (student.age == 0) {
                ""
            } else {
                student.age.toString()
            },
            editingStudentId = student.id
        )
    }

    // Clear the editing state
    fun cancelEditing() {

        if (!_formState.value.isSaving) {
            _formState.value = StudentFormState()
        }
    }

    // Validate and save
    fun saveStudent() {

        val form = _formState.value

        if (form.isSaving) return

        val name = form.name.trim()
        val email = form.email.trim()
        val course = form.course.trim()
        val ageText = form.age.trim()
        val ageNumber = ageText.toIntOrNull()

        val validated = form.copy(

            nameError = if (name.isEmpty()) {
                "Name is required"
            } else null,

            emailError = when {
                email.isEmpty() ->
                    "Email is required"

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    "Enter a valid email address"

                else -> null
            },

            courseError = if (course.isEmpty()) {
                "Please select a course"
            } else null,

            ageError = when {
                ageText.isEmpty() -> null

                ageNumber == null ||
                        ageNumber < 0 ||
                        ageNumber > 120 ->
                    "Enter a valid age from 0 to 120"

                else -> null
            },

            generalError = null
        )

        _formState.value = validated

        // Stop when validation fails
        val hasErrors = listOf(
            validated.nameError,
            validated.emailError,
            validated.courseError,
            validated.ageError
        ).any { it != null }

        if (hasErrors) return

        // Preserve the primary key when editing
        val student = Student(
            id = form.editingStudentId ?: 0,
            name = name,
            email = email,
            course = course,
            age = ageNumber ?: 0
        )

        _formState.update {
            it.copy(isSaving = true)
        }

        if (form.editingStudentId == null) {
            addStudent(student)
        } else {
            updateStudent(student)
        }
    }

    // Add new student
    fun addStudent(student: Student) {

        viewModelScope.launch {

            try {
                repository.insertStudent(student)

                _formState.value = StudentFormState()

            } catch (e: Exception) {

                _formState.update {
                    it.copy(
                        isSaving = false,
                        generalError = "Unable to add student"
                    )
                }
            }
        }
    }

    // Update existing student
    fun updateStudent(student: Student) {

        viewModelScope.launch {

            try {
                repository.updateStudent(student)

                _formState.value = StudentFormState()

            } catch (e: Exception) {

                _formState.update {
                    it.copy(
                        isSaving = false,
                        generalError = "Unable to update student"
                    )
                }
            }
        }
    }

    // Delete student
    fun deleteStudent(student: Student) {

        if (_formState.value.editingStudentId == student.id) {
            cancelEditing()
        }

        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }
}
