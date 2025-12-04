package com.example.noteapp.adapter

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.databinding.TodoItemBinding
import com.example.noteapp.model.ToDo
import com.example.noteapp.model.TodoPriority
import com.example.noteapp.model.TodoStatus
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.random.Random


class ToDoAdapter(
    private val context: Context,
    private val editTodo: (ToDo) ->Unit,
    private val completedTodo: (ToDo) ->Unit
): RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {
    val DILL_CALLBACK: DiffUtil.ItemCallback<ToDo> = object : DiffUtil.ItemCallback<ToDo>(){
        override fun areItemsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
            return oldItem == newItem
        }

    }

    var mDiffer2: AsyncListDiffer<ToDo> = AsyncListDiffer<ToDo>(this, DILL_CALLBACK )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = TodoItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ToDoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mDiffer2.currentList.size
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.onBindToDo(mDiffer2.currentList[position])
    }
    fun setToDo (toDoList: List<ToDo>){
        Log.d("ToDoAdapter","setToDo: $toDoList")
        mDiffer2.submitList(toDoList)
    }

    inner class ToDoViewHolder(private val binding: TodoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun onBindToDo(toDo: ToDo) {
            binding.apply {
                // Set title and description
                txtTodoTitle.text = toDo.title
                
                if (toDo.description.isNotEmpty()) {
                    txtTodoDescription.text = toDo.description
                    txtTodoDescription.visibility = View.VISIBLE
                } else {
                    txtTodoDescription.visibility = View.GONE
                }
                
                // Set priority
                setPriority(toDo.priority)
                
                // Set status
                setStatus(toDo.status)
                
                // Set times
                txtCreatedTime.text = toDo.createdTime
                
                if (toDo.dueTime != null) {
                    txtDueTime.text = "Due: ${toDo.dueTime}"
                    dueTimeContainer.visibility = View.VISIBLE
                } else {
                    dueTimeContainer.visibility = View.GONE
                }
                
                // Set category
                txtCategory.text = toDo.category
                
                // Set duration if available
                if (toDo.estimatedDuration > 0) {
                    val hours = toDo.estimatedDuration / 60.0
                    txtDuration.text = "${String.format("%.1f", hours)}h"
                    txtDuration.visibility = View.VISIBLE
                } else {
                    txtDuration.visibility = View.GONE
                }
                
                // Set completion state
                setCompletionState(toDo.status == TodoStatus.COMPLETED)
                
                // Set click listeners
                root.setOnClickListener { editTodo(toDo) }
                btnAction.setOnClickListener { completedTodo(toDo) }
            }
        }
        
        private fun setPriority(priority: TodoPriority) {
            binding.priorityBadge.apply {
                text = priority.displayName.uppercase()
                
                val priorityColor = when (priority) {
                    TodoPriority.LOW -> R.color.priority_low
                    TodoPriority.MEDIUM -> R.color.priority_medium
                    TodoPriority.HIGH -> R.color.priority_high
                    TodoPriority.URGENT -> R.color.priority_urgent
                }
                
                setBackgroundColor(ContextCompat.getColor(context, priorityColor))
            }
        }
        
        private fun setStatus(status: TodoStatus) {
            binding.apply {
                statusText.text = status.displayName
                statusText.setTextColor(ContextCompat.getColor(context, status.colorRes))
                
                // Set status indicator color
                statusIndicator.setBackgroundColor(ContextCompat.getColor(context, status.colorRes))
                
                // Set status icon and background
                val (iconRes, actionText) = when (status) {
                    TodoStatus.TODO -> Pair(R.drawable.ic_play_arrow, "Start")
                    TodoStatus.IN_PROGRESS -> Pair(R.drawable.ic_check, "Complete")
                    TodoStatus.IN_REVIEW -> Pair(R.drawable.ic_check, "Done")
                    TodoStatus.COMPLETED -> Pair(R.drawable.ic_refresh, "Reopen")
                    TodoStatus.DONE -> Pair(R.drawable.ic_refresh, "Reopen")
                    TodoStatus.ON_HOLD -> Pair(R.drawable.ic_play_arrow, "Resume")
                    TodoStatus.CANCELLED -> Pair(R.drawable.ic_refresh, "Reopen")
                }
                
                statusIcon.setImageResource(iconRes)
                statusIcon.setBackgroundColor(ContextCompat.getColor(context, status.colorRes))
                
                btnAction.setImageResource(iconRes)
                btnAction.contentDescription = actionText
            }
        }
        
        private fun setCompletionState(isCompleted: Boolean) {
            binding.apply {
                val alpha = if (isCompleted) 0.6f else 1.0f
                txtTodoTitle.alpha = alpha
                txtTodoDescription.alpha = alpha
                
                if (isCompleted) {
                    txtTodoTitle.paintFlags = txtTodoTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    txtTodoTitle.paintFlags = txtTodoTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
        }
    }
    fun getRandomColor(): Int{
        val colors = ArrayList<Int>()
       // colors.add(R.color.random1)
        colors.add(R.color.random2)
        colors.add(R.color.random3)
       // colors.add(R.color.random4)
        colors.add(R.color.random5)
        colors.add(R.color.random6)

        val random  = Random.nextInt(colors.size)
        return colors[random]
    }
}