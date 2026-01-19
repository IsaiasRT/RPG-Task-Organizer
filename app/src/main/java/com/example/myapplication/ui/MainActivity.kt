package com.example.myapplication.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.activity.ComponentActivity
import androidx.appcompat.R
import com.example.myapplication.data.Task
import com.example.myapplication.databinding.DialogTaskBinding

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.TaskDatabase
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.repository.TaskRepository
import com.example.myapplication.ui.adapter.TaskAdapter
import com.example.myapplication.viewmodel.TaskViewModel
import com.example.myapplication.viewmodel.TaskViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeTasks()
    }

    private fun setupViewModel() {
        val database = TaskDatabase.getDatabase(this)
        val repository = TaskRepository(database.taskDao())

        val viewModelFactory = TaskViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[TaskViewModel::class.java]
    }

    private fun setupRecyclerView(){
        adapter = TaskAdapter(
            onItemClick = {task -> showTaskDialog(task)},
            onLongItemClick = {task -> showDeleteConfirmation(task) }
        )

        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter

    }

    private fun showDeleteConfirmation(task: Task){
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you really sure you want to delete this task?")
            .setPositiveButton("Delete"){_,_->
                viewModel.deleteTask(task)
            }
            .setNegativeButton("cancel", null)
            .show()
    }

    private fun observeTasks(){
        viewModel.allTasks.observe(this) {tasks ->
            adapter.updateTasks(tasks)
            if(tasks.isEmpty()){
                binding.rvTasks.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE

            }else{
                binding.rvTasks.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE

            }
        }
    }

    private fun setupClickListeners(){
        binding.btnAddTask.setOnClickListener {
            showTaskDialog()
        }
    }

    private fun showTaskDialog(existingTask: Task? = null){

        val dialogBinding = DialogTaskBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        existingTask?.let { task ->
            dialogBinding.etTitle.setText(task.title)
            dialogBinding.etDescription.setText(task.description)
            dialogBinding.cbCompleted.isChecked = task.isCompleted

            when(task.priority){
                1L -> dialogBinding.rbLow.isChecked = true
                2L -> dialogBinding.rbMedium.isChecked = true
                3L -> dialogBinding.rbHigh.isChecked = true
            }

            dialogBinding.btnSave.text = "Update"

        }

            dialogBinding.btnCancel.setOnClickListener{
                dialog.dismiss()
            }

            dialogBinding.btnSave.setOnClickListener{
                val title = dialogBinding.etTitle.text.toString().trim()
                val description = dialogBinding.etDescription.text.toString().trim()
                if (title.isEmpty()) {
                    dialogBinding.etTitle.error = "title is required"
                    return@setOnClickListener
                }

                val priority = when(dialogBinding.rgPriority.checkedRadioButtonId){
                    R.id.rbLow -> 1
                    R.id.rbMedium -> 2
                    R.id.rbHigh -> 3
                    else -> 1
                }
                val isCompleted = dialogBinding.cbCompleted.isChecked

                val task = existingTask?.copy(
                    title = title,
                    description = description,
                    priority = priority.toLong(),
                    isCompleted = isCompleted

                ) ?: Task(
                    title = title,
                    description = description,
                    priority = priority.toLong(),
                    isCompleted = isCompleted
                )

                if (existingTask !=null){
                    viewModel.updateTask(task)
                }else{
                    viewModel.insertTask(task)

                }

                dialog.dismiss()

            }
            dialog.show()


    }

}