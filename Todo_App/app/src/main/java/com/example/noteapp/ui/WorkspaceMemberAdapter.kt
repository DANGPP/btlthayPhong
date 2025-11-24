package com.example.noteapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.databinding.ItemMemberBinding
import com.example.noteapp.model.WorkspaceMember

class WorkspaceMemberAdapter(
    private val onRoleChangeClick: (WorkspaceMember) -> Unit,
    private val onRemoveClick: (WorkspaceMember) -> Unit
) : ListAdapter<WorkspaceMember, WorkspaceMemberAdapter.MemberViewHolder>(MemberDiffCallback()) {
    
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
