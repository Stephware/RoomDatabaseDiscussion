package com.example.room_database_discussion.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(
    entities = [Student::class],
    version = 2,
    exportSchema = false
)
abstract class StudentDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao

    companion object {

        @Volatile
        private var INSTANCE: StudentDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(
                connection: SQLiteConnection
            ) {
                connection.execSQL(
                    """
                    ALTER TABLE students
                    ADD COLUMN age INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )
            }
        }

        fun getData(context: Context): StudentDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudentDatabase::class.java,
                    "student_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
