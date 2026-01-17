package com.example.myapplication.repository

import androidx.lifecycle.LiveData
import com.example.myapplication.data.Task
import com.example.myapplication.data.TaskDao

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks()

    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task){
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun getTaskById(taskId: Long): Task {
        return taskDao.getTaskById(taskId)
    }

}


