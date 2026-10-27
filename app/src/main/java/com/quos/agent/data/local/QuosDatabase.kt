package com.quos.agent.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.quos.agent.data.model.NoteEntity
import com.quos.agent.data.model.TaskEntity

@Database(
    entities = [TaskEntity::class, NoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class QuosDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
}
