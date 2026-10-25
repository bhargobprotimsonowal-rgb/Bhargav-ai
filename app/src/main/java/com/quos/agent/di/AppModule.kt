package com.quos.agent.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.quos.agent.data.local.NoteDao
import com.quos.agent.data.local.QuosDatabase
import com.quos.agent.data.local.TaskDao
import com.quos.agent.data.parser.CommandParser
import com.quos.agent.data.parser.DefaultCommandParser
import com.quos.agent.data.repository.QuosRepository
import com.quos.agent.data.scheduler.TaskScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QuosDatabase {
        return Room.databaseBuilder(
            context,
            QuosDatabase::class.java,
            "quos_agent.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTaskDao(database: QuosDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideNoteDao(database: QuosDatabase): NoteDao = database.noteDao()

    @Provides
    @Singleton
    fun provideRepository(taskDao: TaskDao, noteDao: NoteDao): QuosRepository =
        QuosRepository(taskDao, noteDao)

    @Provides
    @Singleton
    fun provideCommandParser(): CommandParser = DefaultCommandParser()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideTaskScheduler(
        @ApplicationContext context: Context,
        workManager: WorkManager,
        repository: QuosRepository
    ): TaskScheduler = TaskScheduler(context, workManager, repository)
}
