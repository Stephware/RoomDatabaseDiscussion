package com.example.room_database_discussion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.room_database_discussion.data.Student
import com.example.room_database_discussion.data.StudentDatabase
import com.example.room_database_discussion.data.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudentViewModel(application: Application): AndroidViewModel(application) {
    private val database = StudentDatabase.getData(application)
    private val repository = StudentRepository(database.studentDao())
    private val _students = MutableStateFlow<List<Student>>(emptyList())

    val students: StateFlow<List<Student>> = _students.asStateFlow()

    init{
        viewModelScope.launch{
            repository.students.collect { studentList ->
                _students.value = studentList
            }
        }
    }

    fun addStudent(name: String, email: String, course: String){
        viewModelScope.launch {
            val student = Student(
                name = name,
                email = email,
                course = course
            )

            repository.insertStudents(student)
        }
    }

    fun deleteStudent(student: Student){
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }
}