package com.example.myapplication.viewmodel

import com.example.myapplication.repository.TaskRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Task
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository): ViewModel() {

    val allTasks = repository.allTasks

    fun insertTask(task: Task) = viewModelScope.launch {
        repository.insertTask(task)
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        repository.updateTask(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
    }

    fun getTaskById(taskId : Long, onResult: (Task)->Unit) = viewModelScope.launch {
        val task = repository.getTaskById(taskId)
        onResult(task)
    }

}