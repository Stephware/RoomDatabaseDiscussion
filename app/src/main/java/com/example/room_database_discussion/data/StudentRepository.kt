package com.example.room_database_discussion.data

import kotlinx.coroutines.flow.Flow

class StudentRepository(
    private val studentDao: StudentDao
) {

    val students: Flow<List<Student>> =
        studentDao.getAllStudents()

    val studentCount: Flow<Int> =
        studentDao.getStudentCount()

    suspend fun insertStudent(student: Student) {
        studentDao.insertStudent(student)
    }

    suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
    }

    fun searchStudents(
        search: String
    ): Flow<List<Student>> {
        return studentDao.searchStudents(search)
    }

    fun filterStudentsByCourse(
        course: String
    ): Flow<List<Student>> {
        return studentDao.filterStudentsByCourse(course)
    }

    fun searchStudentsByCourse(
        search: String,
        course: String
    ): Flow<List<Student>> {
        return studentDao.searchStudentsByCourse(
            search,
            course
        )
    }
}
