package com.example.noteapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.model.User

/**
 * Adapter để hiển thị danh sách users có thể mời vào workspace
 */
class UserSelectionAdapter(
    private val onUserSelected: (User, Boolean) -> Unit
) : ListAdapter<User, UserSelectionAdapter.UserViewHolder>(UserDiffCallback()) {
    
    private val selectedUsers = mutableSetOf<String>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_selection, parent, false)
        return UserViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user, selectedUsers.contains(user.id))
    }
    
    fun getSelectedUsers(): List<User> {
        return currentList.filter { selectedUsers.contains(it.id) }
    }
    
    fun clearSelection() {
        selectedUsers.clear()
        notifyDataSetChanged()
    }
    
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.textUserName)
        private val textEmail: TextView = itemView.findViewById(R.id.textUserEmail)
        private val checkbox: CheckBox = itemView.findViewById(R.id.checkboxUser)
        
        fun bind(user: User, isSelected: Boolean) {
            textName.text = user.name
            textEmail.text = user.email
            checkbox.isChecked = isSelected
            
            itemView.setOnClickListener {
                val newCheckedState = !checkbox.isChecked
                checkbox.isChecked = newCheckedState
                
                if (newCheckedState) {
                    selectedUsers.add(user.id)
                } else {
                    selectedUsers.remove(user.id)
                }
                
                onUserSelected(user, newCheckedState)
            }
            
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedUsers.add(user.id)
                } else {
                    selectedUsers.remove(user.id)
                }
                onUserSelected(user, isChecked)
            }
        }
    }
    
    private class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
