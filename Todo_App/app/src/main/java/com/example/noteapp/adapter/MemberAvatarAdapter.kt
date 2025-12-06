package com.example.noteapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.databinding.ItemMemberAvatarBinding
import com.example.noteapp.model.WorkspaceMember

class MemberAvatarAdapter(
    private val onMemberClick: (WorkspaceMember) -> Unit = {}
) : ListAdapter<WorkspaceMember, MemberAvatarAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMemberAvatarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onMemberClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemMemberAvatarBinding,
        private val onMemberClick: (WorkspaceMember) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(member: WorkspaceMember) {
            // Get initials from email
            val initials = getInitials(member.userEmail)
            binding.textAvatarInitials.text = initials
            
            // Generate color from email
            val color = generateColorFromString(member.userEmail)
            binding.textAvatarInitials.setBackgroundColor(color)
            
            // Show name or email
            binding.textMemberName.text = member.userEmail.substringBefore("@")
            
            // Set click listener
            binding.root.setOnClickListener {
                onMemberClick(member)
            }
        }

        private fun getInitials(email: String): String {
            val name = email.substringBefore("@")
            return if (name.length >= 2) {
                name.substring(0, 2).uppercase()
            } else {
                name.uppercase()
            }
        }

        private fun generateColorFromString(str: String): Int {
            val colors = listOf(
                "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A",
                "#98D8C8", "#6C5CE7", "#A29BFE", "#FD79A8",
                "#FDCB6E", "#00B894", "#0984E3", "#6C5CE7"
            )
            val index = kotlin.math.abs(str.hashCode()) % colors.size
            return Color.parseColor(colors[index])
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<WorkspaceMember>() {
        override fun areItemsTheSame(oldItem: WorkspaceMember, newItem: WorkspaceMember): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WorkspaceMember, newItem: WorkspaceMember): Boolean {
            return oldItem == newItem
        }
    }
}
