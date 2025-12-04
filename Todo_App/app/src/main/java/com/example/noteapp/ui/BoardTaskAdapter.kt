package com.example.noteapp.ui

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.databinding.ItemBoardTaskBinding
import com.example.noteapp.model.BoardColumn
import com.example.noteapp.model.WorkspaceTask

class BoardTaskAdapter(
    private val column: BoardColumn,
    private val onTaskClick: (WorkspaceTask) -> Unit,
    private val onStatusChange: (WorkspaceTask, BoardColumn) -> Unit,
    private val onTaskDragStart: ((RecyclerView.ViewHolder) -> Unit)? = null
) : ListAdapter<WorkspaceTask, BoardTaskAdapter.TaskViewHolder>(TaskDiffCallback()) {
    
    private var itemTouchHelper: ItemTouchHelper? = null
    
    fun attachItemTouchHelper(touchHelper: ItemTouchHelper) {
        this.itemTouchHelper = touchHelper
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemBoardTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class TaskViewHolder(
        private val binding: ItemBoardTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(task: WorkspaceTask) {
            binding.apply {
                textViewTaskTitle.text = task.title
                textViewTaskDescription.text = task.description?.ifEmpty { "Không có mô tả" } ?: "Không có mô tả"
                textViewTaskCategory.text = task.category ?: "Chung"
                
                // Priority badge
                val priorityColor = when(task.priority) {
                    com.example.noteapp.model.TodoPriority.URGENT -> R.color.priority_urgent
                    com.example.noteapp.model.TodoPriority.HIGH -> R.color.priority_high
                    com.example.noteapp.model.TodoPriority.MEDIUM -> R.color.priority_medium
                    com.example.noteapp.model.TodoPriority.LOW -> R.color.priority_low
                }
                textViewTaskPriority.text = task.priority.displayName
                textViewTaskPriority.setBackgroundColor(
                    ContextCompat.getColor(root.context, priorityColor)
                )
                
                // Assignee count
                if (task.assignedTo.isNotEmpty()) {
                    textViewAssigneeCount.text = "${task.assignedTo.size} người"
                    textViewAssigneeCount.visibility = android.view.View.VISIBLE
                } else {
                    textViewAssigneeCount.visibility = android.view.View.GONE
                }
                
                // Due time
                if (task.dueTime != null) {
                    textViewDueTime.text = "Hạn: ${task.dueTime}"
                    textViewDueTime.visibility = android.view.View.VISIBLE
                } else {
                    textViewDueTime.visibility = android.view.View.GONE
                }
                
                root.setOnClickListener {
                    onTaskClick(task)
                }
                
                root.setOnLongClickListener {
                    // Show dialog to change status
                    showChangeStatusDialog(task)
                    true
                }
                
                // Enable drag on long press
                root.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        onTaskDragStart?.invoke(this@TaskViewHolder)
                    }
                    false
                }
            }
        }
        
        private fun showChangeStatusDialog(task: WorkspaceTask) {
            val columns = BoardColumn.values()
            val columnNames = columns.map { it.displayName }.toTypedArray()
            val currentIndex = columns.indexOf(column)
            
            androidx.appcompat.app.AlertDialog.Builder(binding.root.context)
                .setTitle("Chuyển Công Việc Đến")
                .setSingleChoiceItems(columnNames, currentIndex) { dialog, which ->
                    val newColumn = columns[which]
                    if (newColumn != column) {
                        onStatusChange(task, newColumn)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }
    
    private class TaskDiffCallback : DiffUtil.ItemCallback<WorkspaceTask>() {
        override fun areItemsTheSame(oldItem: WorkspaceTask, newItem: WorkspaceTask): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: WorkspaceTask, newItem: WorkspaceTask): Boolean {
            return oldItem == newItem
        }
    }
}
