package com.example.myapplication.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.data.Task
import com.example.myapplication.databinding.FragmentTasksBinding
import com.example.myapplication.databinding.DialogTaskBinding
import com.example.myapplication.ui.adapter.TaskAdapter
import com.example.myapplication.viewmodel.TaskViewModel
import com.example.myapplication.viewmodel.TaskViewModelFactory
import com.example.myapplication.data.TaskDatabase
import com.example.myapplication.repository.TaskRepository
import com.example.myapplication.repository.ProfileRepository
import com.example.myapplication.repository.HistoryRepository
import com.example.myapplication.repository.AchievementRepository

class TasksFragment : Fragment() {

    // Binding gives us type-safe access to all views in the layout
    // The underscore version can be null, the non-underscore version cannot
    // This pattern is recommended for Fragments to avoid memory leaks
    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    // activityViewModels() instead of viewModels() means this ViewModel
    // is shared across all fragments in the same Activity
    // This is crucial because it means all your fragments can access
    // the same data and stay in sync with each other
    private val viewModel: TaskViewModel by activityViewModels {
        // Here we create the ViewModel with all its dependencies
        // This only happens once - subsequent fragments will reuse the same instance
        val database = TaskDatabase.getDatabase(requireContext())
        TaskViewModelFactory(
            TaskRepository(database.taskDao()),
            ProfileRepository(database.userProfileDao()),
            HistoryRepository(database.taskHistoryDao()),
            AchievementRepository(database.achievementDao())
        )
    }

    private lateinit var adapter: TaskAdapter

    // onCreateView is where we inflate (create) our layout
    // Think of it like setting up the visual structure of this screen
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    // onViewCreated is called right after the view is created
    // This is where we set up everything that interacts with the UI
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeTasks()
    }

    // This sets up the RecyclerView that displays your list of tasks
    // Notice how the adapter gets callbacks for item clicks and long clicks
    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onItemClick = { task ->
                // When user taps a task, show the edit dialog
                showTaskDialog(task)
            },
            onLongItemClick = { task ->
                // When user long-presses a task, show delete confirmation
                showDeleteConfirmation(task)
            }
        )

        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter
    }

    // This is where the magic happens - we observe LiveData from the ViewModel
    // Whenever the task list changes in the database, this observer gets notified
    // and automatically updates the UI. No manual refresh needed!
    private fun observeTasks() {
        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            // Update the adapter with the new list of tasks
            adapter.updateTasks(tasks)

            // Show or hide the empty state based on whether we have tasks
            // This gives users helpful feedback when their list is empty
            if(tasks.isEmpty()) {
                binding.rvTasks.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            } else {
                binding.rvTasks.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
            }
        }
    }

    // Set up the floating action button to add new tasks
    private fun setupClickListeners() {
        binding.btnAddTask.setOnClickListener {
            showTaskDialog() // No task parameter means we're creating a new one
        }
    }

    // This dialog serves double duty - it can create new tasks or edit existing ones
    // The existingTask parameter determines which mode we're in
    private fun showTaskDialog(existingTask: Task? = null) {
        // Inflate the dialog layout
        val dialogBinding = DialogTaskBinding.inflate(layoutInflater)

        // Create the AlertDialog with our custom layout
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // If we're editing an existing task, populate the form with its data
        existingTask?.let { task ->
            dialogBinding.etTitle.setText(task.title)
            dialogBinding.etDescription.setText(task.description)
            dialogBinding.cbCompleted.isChecked = task.isCompleted

            // Select the appropriate priority radio button
            // This gives users visual feedback about the task's current priority
            when(task.priority) {
                1L -> dialogBinding.rbLow.isChecked = true
                2L -> dialogBinding.rbMedium.isChecked = true
                3L -> dialogBinding.rbHigh.isChecked = true
            }

            // Change button text to indicate we're updating, not creating
            dialogBinding.btnSave.text = "Update"
        }

        // Handle the cancel button - just dismiss the dialog
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Handle the save button - this is where the real work happens
        dialogBinding.btnSave.setOnClickListener {
            // Get and validate the title
            val title = dialogBinding.etTitle.text.toString().trim()
            val description = dialogBinding.etDescription.text.toString().trim()

            // Validation - ensure the user entered a title
            if (title.isEmpty()) {
                dialogBinding.etTitle.error = "Title is required"
                return@setOnClickListener
            }

            // Figure out which priority was selected
            // We check which radio button is selected and map it to our priority values
            val priority = when(dialogBinding.rgPriority.checkedRadioButtonId) {
                R.id.rbLow -> 1
                R.id.rbMedium -> 2
                R.id.rbHigh -> 3
                else -> 1 // Default to low if somehow nothing is selected
            }

            val isCompleted = dialogBinding.cbCompleted.isChecked

            // Create or update the task
            // If existingTask is not null, we copy it with updated values
            // If it's null, we create a brand new Task object
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

            // Now here's the important part about the new gamification system
            // Instead of just updating/inserting the task, we need to check
            // if the task is being marked as completed for the first time
            if (existingTask != null) {
                // If task was just marked complete and wasn't before
                if (task.isCompleted && !existingTask.isCompleted) {
                    // Use the special completeTask method which awards EXP
                    // and creates a history entry
                    viewModel.completeTask(task)
                } else {
                    // Otherwise just do a normal update
                    viewModel.updateTask(task)
                }
            } else {
                // For new tasks, just insert them normally
                viewModel.insertTask(task)
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    // Show a confirmation dialog before deleting a task
    // This prevents accidental deletions and is good UX practice
    private fun showDeleteConfirmation(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task? This will apply an EXP penalty if the task is not completed.")
            .setPositiveButton("Delete") { _, _ ->
                // Use the ViewModel's deleteTask which handles EXP penalties
                // and history tracking automatically
                viewModel.deleteTask(task)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // This is crucial for avoiding memory leaks
    // When the Fragment's view is destroyed, we set the binding to null
    // This allows garbage collection to clean up the views properly
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
