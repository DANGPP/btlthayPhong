package com.example.noteapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.databinding.ItemMemberBinding
import com.example.noteapp.model.WorkspaceMember
import com.example.noteapp.model.WorkspaceRole

class WorkspaceMemberAdapter(
    private val onMemberClick: (WorkspaceMember) -> Unit,
    private val onRoleChangeClick: (WorkspaceMember) -> Unit,
    private val onRemoveClick: (WorkspaceMember) -> Unit,
    private var currentUserRole: WorkspaceRole? = null
) : ListAdapter<WorkspaceMember, WorkspaceMemberAdapter.MemberViewHolder>(MemberDiffCallback()) {
    
    fun updateCurrentUserRole(role: WorkspaceRole?) {
        currentUserRole = role
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MemberViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class MemberViewHolder(
        private val binding: ItemMemberBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(member: WorkspaceMember) {
            binding.apply {
                textViewMemberEmail.text = member.userEmail
                textViewMemberRole.text = member.role.name
                textViewJoinedTime.text = "Joined: ${member.joinedTime}"
                
                // Set role color
                val roleColor = ContextCompat.getColor(root.context, member.role.color)
                textViewMemberRole.setTextColor(roleColor)
                
                // Click on whole item to view member's tasks
                root.setOnClickListener {
                    onMemberClick(member)
                }
                
                // Show/hide buttons based on current user's role
                when (currentUserRole) {
                    WorkspaceRole.ADMIN -> {
                        // Admin can edit and delete
                        buttonChangeRole.visibility = View.VISIBLE
                        buttonRemoveMember.visibility = View.VISIBLE
                    }
                    WorkspaceRole.EDITOR -> {
                        // Editor can only edit
                        buttonChangeRole.visibility = View.VISIBLE
                        buttonRemoveMember.visibility = View.GONE
                    }
                    WorkspaceRole.VIEWER, null -> {
                        // Viewer has no permissions
                        buttonChangeRole.visibility = View.GONE
                        buttonRemoveMember.visibility = View.GONE
                    }
                }
                
                buttonChangeRole.setOnClickListener {
                    onRoleChangeClick(member)
                }
                
                buttonRemoveMember.setOnClickListener {
                    onRemoveClick(member)
                }
            }
        }
    }
    
    private class MemberDiffCallback : DiffUtil.ItemCallback<WorkspaceMember>() {
        override fun areItemsTheSame(oldItem: WorkspaceMember, newItem: WorkspaceMember): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: WorkspaceMember, newItem: WorkspaceMember): Boolean {
            return oldItem == newItem
        }
    }
}
