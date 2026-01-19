package com.example.myapplication.ui.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.Task
import com.example.myapplication.databinding.ItemTaskBinding

class TaskAdapter(
    private var tasks: List<Task> = emptyList(),
    private val onItemClick: (Task) -> Unit,
    private val onLongItemClick: (Task) -> Unit,

): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {


    class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task, onItemClick: (Task) -> Unit, onLongItemClick: (Task) -> Unit){
            binding.tvTaskTitle.text = task.title
            binding.tvTaskDescription.text = task.description
            binding.tvPriority.text = task.getPriorityText()
            binding.tvStatus.text = if (task.isCompleted)"Completed" else "Pending"

            binding.viewPriorityIndicator.setBackgroundColor(task.getPriorityColor().toInt())

            val context = binding.root.context
            if(task.isCompleted){
                binding.tvTaskTitle.setTextColor(ContextCompat.getColor(context, R.color.gray))
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green))

            }else {
                binding.tvTaskTitle.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
            }

            binding.root.setOnLongClickListener {
                onLongItemClick(task)
                true
            }

            binding.root.setOnClickListener {
                onItemClick(task)
            }


        }


    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun getItemCount(): Int = tasks.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
            holder.bind(task, onItemClick, onLongItemClick)
    }

    fun updateTasks (NewTasks: List<Task>) {
        tasks = NewTasks
        notifyDataSetChanged()
    }



}