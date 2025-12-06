package com.example.noteapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.model.WorkspaceInvitation

/**
 * Adapter hiển thị danh sách lời mời workspace
 */
class WorkspaceInvitationAdapter(
    private val onAccept: (WorkspaceInvitation) -> Unit,
    private val onReject: (WorkspaceInvitation) -> Unit
) : ListAdapter<WorkspaceInvitation, WorkspaceInvitationAdapter.InvitationViewHolder>(InvitationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workspace_invitation, parent, false)
        return InvitationViewHolder(view, onAccept, onReject)
    }

    override fun onBindViewHolder(holder: InvitationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class InvitationViewHolder(
        itemView: View,
        private val onAccept: (WorkspaceInvitation) -> Unit,
        private val onReject: (WorkspaceInvitation) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val workspaceName: TextView = itemView.findViewById(R.id.textWorkspaceName)
        private val inviterEmail: TextView = itemView.findViewById(R.id.textInviterEmail)
        private val role: TextView = itemView.findViewById(R.id.textRole)
        private val invitedTime: TextView = itemView.findViewById(R.id.textInvitedTime)
        private val btnAccept: Button = itemView.findViewById(R.id.btnAccept)
        private val btnReject: Button = itemView.findViewById(R.id.btnReject)

        fun bind(invitation: WorkspaceInvitation) {
            workspaceName.text = invitation.workspaceName
            inviterEmail.text = "Từ: ${invitation.invitedBy}"
            role.text = "Vai trò: ${invitation.role.displayName}"
            invitedTime.text = invitation.createdTime

            btnAccept.setOnClickListener { onAccept(invitation) }
            btnReject.setOnClickListener { onReject(invitation) }
        }
    }

    class InvitationDiffCallback : DiffUtil.ItemCallback<WorkspaceInvitation>() {
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
