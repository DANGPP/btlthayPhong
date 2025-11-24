package com.example.noteapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.databinding.ItemInvitationBinding
import com.example.noteapp.model.WorkspaceInvitation

class InvitationAdapter(
    private val onAcceptClick: (WorkspaceInvitation) -> Unit,
    private val onDeclineClick: (WorkspaceInvitation) -> Unit
) : ListAdapter<WorkspaceInvitation, InvitationAdapter.InvitationViewHolder>(InvitationDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationViewHolder {
        val binding = ItemInvitationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InvitationViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: InvitationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class InvitationViewHolder(
        private val binding: ItemInvitationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(invitation: WorkspaceInvitation) {
            binding.apply {
                textViewWorkspaceName.text = invitation.workspaceName
                textViewInvitationRole.text = "Role: ${invitation.role.name}"
                textViewInvitationTime.text = invitation.createdTime
                
                // Set role color
                val roleColor = ContextCompat.getColor(root.context, invitation.role.color)
                textViewInvitationRole.setTextColor(roleColor)
                
                buttonAccept.setOnClickListener {
                    onAcceptClick(invitation)
                }
                
                buttonDecline.setOnClickListener {
                    onDeclineClick(invitation)
                }
            }
        }
    }
    
    private class InvitationDiffCallback : DiffUtil.ItemCallback<WorkspaceInvitation>() {
        override fun areItemsTheSame(
            oldItem: WorkspaceInvitation,
            newItem: WorkspaceInvitation
        ): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(
            oldItem: WorkspaceInvitation,
            newItem: WorkspaceInvitation
        ): Boolean {
            return oldItem == newItem
        }
    }
}
