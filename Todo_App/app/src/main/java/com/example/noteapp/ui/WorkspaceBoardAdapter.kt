package com.example.noteapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.databinding.ItemBoardColumnBinding
import com.example.noteapp.model.BoardColumn
import com.example.noteapp.model.WorkspaceTask

class WorkspaceBoardAdapter(
    private val onTaskClick: (WorkspaceTask) -> Unit,
    private val onStatusChange: (WorkspaceTask, BoardColumn) -> Unit
) : RecyclerView.Adapter<WorkspaceBoardAdapter.ColumnViewHolder>() {
    
    private val columns = BoardColumn.values().toList()
    private var tasks = listOf<WorkspaceTask>()
    
    fun updateTasks(newTasks: List<WorkspaceTask>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColumnViewHolder {
        val binding = ItemBoardColumnBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ColumnViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ColumnViewHolder, position: Int) {
        holder.bind(columns[position])
    }
    
    override fun getItemCount() = columns.size
    
    inner class ColumnViewHolder(
        private val binding: ItemBoardColumnBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private lateinit var taskAdapter: BoardTaskAdapter
        
        fun bind(column: BoardColumn) {
            binding.textViewColumnTitle.text = column.displayName
            
            // Filter tasks for this column
            val columnTasks = tasks.filter { task ->
                column.status.contains(task.status)
            }
            
            binding.textViewTaskCount.text = "${columnTasks.size}"
            
            // Setup tasks RecyclerView
            taskAdapter = BoardTaskAdapter(
                column = column,
                onTaskClick = onTaskClick,
                onStatusChange = { task, newColumn ->
                    onStatusChange(task, newColumn)
                }
            )
            
            binding.recyclerViewTasks.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = taskAdapter
                
                // Enable drag and drop within column
                val callback = object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
                    androidx.recyclerview.widget.ItemTouchHelper.UP or 
                    androidx.recyclerview.widget.ItemTouchHelper.DOWN,
                    0
                ) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        return true
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        // Not used
                    }
                    
                    override fun isLongPressDragEnabled(): Boolean = true
                }
                
                val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(callback)
                itemTouchHelper.attachToRecyclerView(this)
            }
            
            taskAdapter.submitList(columnTasks)
        }
    }
}
