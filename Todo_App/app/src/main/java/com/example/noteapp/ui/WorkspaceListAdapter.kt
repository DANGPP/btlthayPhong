package com.example.noteapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.databinding.ItemWorkspaceBinding
import com.example.noteapp.model.Workspace

class WorkspaceListAdapter(
    private val onWorkspaceClick: (Workspace) -> Unit,
    private val onDeleteClick: (Workspace) -> Unit
) : ListAdapter<Workspace, WorkspaceListAdapter.WorkspaceViewHolder>(WorkspaceDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkspaceViewHolder {
        val binding = ItemWorkspaceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WorkspaceViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: WorkspaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class WorkspaceViewHolder(
        private val binding: ItemWorkspaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(workspace: Workspace) {
            binding.apply {
                textViewWorkspaceName.text = workspace.name
                textViewWorkspaceDescription.text = workspace.description.ifEmpty { "No description" }
                textViewMemberCount.text = "Workspace" // Will show member count after loading members
                textViewCreatedTime.text = workspace.createdTime
                
                root.setOnClickListener {
                    onWorkspaceClick(workspace)
                }
                
                buttonDeleteWorkspace.setOnClickListener {
                    onDeleteClick(workspace)
                }
            }
        }
    }
    
    private class WorkspaceDiffCallback : DiffUtil.ItemCallback<Workspace>() {
        override fun areItemsTheSame(oldItem: Workspace, newItem: Workspace): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Workspace, newItem: Workspace): Boolean {
            return oldItem == newItem
        }
    }
}
