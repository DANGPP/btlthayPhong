package com.example.noteapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.databinding.ItemTodoPreviewBinding
import com.example.noteapp.model.ToDo
import com.example.noteapp.model.TodoPriority
import java.text.SimpleDateFormat
import java.util.*

class TodoPreviewAdapter(
    private var todos: List<ToDo> = emptyList(),
    private val onEditClick: (ToDo, Int) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<TodoPreviewAdapter.TodoPreviewViewHolder>() {

    fun updateTodos(newTodos: List<ToDo>) {
        todos = newTodos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoPreviewViewHolder {
        val binding = ItemTodoPreviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TodoPreviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoPreviewViewHolder, position: Int) {
        holder.bind(todos[position])
    }

    override fun getItemCount(): Int = todos.size

    inner class TodoPreviewViewHolder(
        private val binding: ItemTodoPreviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(todo: ToDo) {
            binding.apply {
                tvPreviewTitle.text = todo.title
                
                // Show description if available
                if (todo.description.isNotBlank()) {
                    tvPreviewDescription.text = todo.description
                    tvPreviewDescription.visibility = View.VISIBLE
                } else {
                    tvPreviewDescription.visibility = View.GONE
                }

                // Priority
                tvPreviewPriority.text = todo.priority.displayName
                tvPreviewPriority.setBackgroundResource(getPriorityBackground(todo.priority))

                // Category
                tvPreviewCategory.text = todo.category

                // Duration
                if (todo.estimatedDuration > 0) {
                    val hours = todo.estimatedDuration / 60
                    val minutes = todo.estimatedDuration % 60
                    tvPreviewDuration.text = when {
                        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                        hours > 0 -> "${hours}h"
                        else -> "${minutes}m"
                    }
                    tvPreviewDuration.visibility = View.VISIBLE
                } else {
                    tvPreviewDuration.visibility = View.GONE
                }

                // Due time
                if (!todo.dueTime.isNullOrBlank()) {
                    tvPreviewDueTime.text = formatDueTime(todo.dueTime!!)
                    tvPreviewDueTime.visibility = View.VISIBLE
                } else {
                    tvPreviewDueTime.visibility = View.GONE
                }

                // Click listener for editing
                root.setOnClickListener {
                    onEditClick(todo, adapterPosition)
                }
            }
        }

        private fun getPriorityBackground(priority: TodoPriority): Int {
            return when (priority) {
                TodoPriority.LOW -> R.drawable.priority_low_background
                TodoPriority.MEDIUM -> R.drawable.priority_medium_background
                TodoPriority.HIGH -> R.drawable.priority_high_background
                TodoPriority.URGENT -> R.drawable.priority_urgent_background
            }
        }

        private fun formatDueTime(dueTime: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
                val date = inputFormat.parse(dueTime)
                date?.let { outputFormat.format(it) } ?: dueTime
            } catch (e: Exception) {
                // If parsing fails, try to show a readable version
                dueTime.replace("T", " ").replace("Z", "")
            }
        }
    }
}
