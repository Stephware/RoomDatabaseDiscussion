package com.example.room_database_discussion.data

import kotlinx.coroutines.flow.Flow

class StudentRepository(private val studentDao: StudentDao) {
    val students: Flow<List<Student>>
        get() = studentDao.getAllStudents()

    suspend fun insertStudents(student: Student){
        studentDao.insertStudent(student)
    }

    suspend fun deleteStudent(student: Student){
        studentDao.deleteStudent(student)
    }
}