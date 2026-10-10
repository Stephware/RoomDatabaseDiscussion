package com.example.room_database_discussion.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    // Display all students
    @Query("SELECT * FROM students ORDER BY name")
    fun getAllStudents(): Flow<List<Student>>

    // Add student
    @Insert
    suspend fun insertStudent(student: Student)

    // Update student
    @Update
    suspend fun updateStudent(student: Student)

    // Delete student
    @Delete
    suspend fun deleteStudent(student: Student)

    // Search by name
    @Query("""
        SELECT * FROM students
        WHERE name LIKE '%' || :search || '%'
        ORDER BY name
    """)
    fun searchStudents(
        search: String
    ): Flow<List<Student>>

    // Filter by course
    @Query("""
        SELECT * FROM students
        WHERE course = :course
        ORDER BY name
    """)
    fun filterStudentsByCourse(
        course: String
    ): Flow<List<Student>>

    // Search and filter together
    @Query("""
        SELECT * FROM students
        WHERE name LIKE '%' || :search || '%'
        AND course = :course
        ORDER BY name
    """)
    fun searchStudentsByCourse(
        search: String,
        course: String
    ): Flow<List<Student>>

    // Get the total student count
    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>
}
