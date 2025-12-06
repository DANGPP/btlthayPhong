package com.example.noteapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.databinding.DialogWorkspaceDetailsBinding
import com.example.noteapp.model.WorkspaceMember
import com.example.noteapp.model.WorkspaceRole
import com.example.noteapp.viewmodel.WorkspaceViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.example.noteapp.R
import kotlinx.coroutines.launch

class WorkspaceDetailsFragment : BottomSheetDialogFragment() {
    private var _binding: DialogWorkspaceDetailsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: WorkspaceViewModel
    private lateinit var memberAdapter: WorkspaceMemberAdapter
    private var workspaceId: String? = null
    
    companion object {
        private const val ARG_WORKSPACE_ID = "workspace_id"
        
        fun newInstance(workspaceId: String): WorkspaceDetailsFragment {
            val fragment = WorkspaceDetailsFragment()
            val args = Bundle()
            args.putString(ARG_WORKSPACE_ID, workspaceId)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workspaceId = arguments?.getString(ARG_WORKSPACE_ID)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogWorkspaceDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val factory = WorkspaceViewModel.WorkspaceViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[WorkspaceViewModel::class.java]
        
        setupRecyclerView()
        setupObservers()
        setupListeners()
        
        workspaceId?.let { viewModel.selectWorkspace(it) }
    }
    
    private fun setupRecyclerView() {
        memberAdapter = WorkspaceMemberAdapter(
            onMemberClick = { member ->
                viewMemberTasks(member)
            },
            onRoleChangeClick = { member ->
                showChangeRoleDialog(member.id, member.role)
            },
            onRemoveClick = { member ->
                confirmRemoveMember(member.id, member.userEmail)
            }
        )
        
        binding.recyclerViewMembers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = memberAdapter
        }
    }
    
    private fun viewMemberTasks(member: WorkspaceMember) {
        workspaceId?.let { wsId ->
            // Load user name from users collection
            lifecycleScope.launch {
                try {
                    val userName = viewModel.getUserName(member.userEmail) ?: member.userEmail
                    
                    val bundle = Bundle().apply {
                        putString("workspace_id", wsId)
                        putString("user_id", member.userId)
                        putString("user_name", userName)
                    }
                    
                    // Navigate using activity's NavController
                    androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.memberTasksFragment, bundle)
                    
                    dismiss()
                } catch (e: Exception) {
                    Snackbar.make(binding.root, "Không thể mở màn hình tasks: ${e.message}", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun setupObservers() {
        viewModel.currentWorkspace.observe(viewLifecycleOwner) { workspace ->
            workspace?.let {
                binding.textViewWorkspaceName.text = it.name
                binding.textViewWorkspaceDescription.text = it.description.ifEmpty { "Không có mô tả" }
                
                // Check if current user is owner - always show buttons for owner
                val sessionManager = com.example.noteapp.auth.SessionManager(requireContext())
                val currentUserId = sessionManager.getCurrentUserId()
                val isOwner = it.ownerId == currentUserId
                
                if (isOwner) {
                    binding.buttonInviteMember.visibility = View.VISIBLE
                    binding.buttonEditWorkspace.visibility = View.VISIBLE
                }
            }
        }
        
        viewModel.members.observe(viewLifecycleOwner) { members ->
            memberAdapter.submitList(members)
            binding.textViewMemberCount.text = "Thành Viên (${members.size})"
            
            if (members.isEmpty()) {
                binding.recyclerViewMembers.visibility = View.GONE
                binding.textViewEmptyMembers.visibility = View.VISIBLE
            } else {
                binding.recyclerViewMembers.visibility = View.VISIBLE
                binding.textViewEmptyMembers.visibility = View.GONE
            }
        }
        
        viewModel.userRole.observe(viewLifecycleOwner) { role ->
            // Check if current user is owner
            val workspace = viewModel.currentWorkspace.value
            val sessionManager = com.example.noteapp.auth.SessionManager(requireContext())
            val currentUserId = sessionManager.getCurrentUserId()
            val isOwner = workspace?.ownerId == currentUserId
            
            // Update adapter: owner always has ADMIN permissions
            val effectiveRole = if (isOwner) WorkspaceRole.ADMIN else role
            memberAdapter.updateCurrentUserRole(effectiveRole)
            
            // Show/hide admin actions based on role (if not owner)
            if (!isOwner) {
                val canManage = role?.canManageMembers() == true
                binding.buttonInviteMember.visibility = if (canManage) View.VISIBLE else View.GONE
                binding.buttonEditWorkspace.visibility = if (canManage) View.VISIBLE else View.GONE
            }
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
        
        viewModel.operationSuccess.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearOperationSuccess()
            }
        }
    }
    
    private fun setupListeners() {
        binding.buttonViewBoard.setOnClickListener {
            openBoardView()
        }
        
        binding.buttonInviteMember.setOnClickListener {
            showInviteMemberDialog()
        }
        
        binding.buttonEditWorkspace.setOnClickListener {
            showEditWorkspaceDialog()
        }
    }
    
    private fun openBoardView() {
        workspaceId?.let { id ->
            val bundle = Bundle().apply {
                putString("workspace_id", id)
            }
            findNavController().navigate(R.id.action_workspaceDetails_to_workspaceBoard, bundle)
            dismiss()
        }
    }
    
    private fun showInviteMemberDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_invite_member_enhanced, null)
        val searchInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextSearchUser)
        val recyclerView = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewUsers)
        val emptyText = dialogView.findViewById<android.widget.TextView>(R.id.textEmptyUsers)
        val selectedCountText = dialogView.findViewById<android.widget.TextView>(R.id.textSelectedCount)
        val roleGroup = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupRole)
        
        // Setup RecyclerView
        lateinit var userAdapter: com.example.noteapp.adapter.UserSelectionAdapter
        userAdapter = com.example.noteapp.adapter.UserSelectionAdapter { user, isSelected ->
            val selectedUsers = userAdapter.getSelectedUsers()
            selectedCountText.text = "Đã chọn: ${selectedUsers.size}"
        }
        
        recyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = userAdapter
        }
        
        // Load available users from database (filtered by workspace)
        workspaceId?.let { viewModel.loadAvailableUsers(it) }
        
        // Observe users list
        viewModel.availableUsers.observe(viewLifecycleOwner) { users ->
            if (users.isEmpty()) {
                recyclerView.visibility = android.view.View.GONE
                emptyText.visibility = android.view.View.VISIBLE
            } else {
                recyclerView.visibility = android.view.View.VISIBLE
                emptyText.visibility = android.view.View.GONE
                userAdapter.submitList(users)
            }
        }
        
        // Search functionality
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s.toString().trim()
                workspaceId?.let { viewModel.searchUsers(query, it) }
            }
        })
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Mời Thành Viên")
            .setView(dialogView)
            .setPositiveButton("Gửi Lời Mời", null)
            .setNegativeButton("Hủy", null)
            .create()
        
        dialog.show()
        
        // Override positive button to prevent auto-dismiss
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val selectedUsers = userAdapter.getSelectedUsers()
            
            if (selectedUsers.isEmpty()) {
                Snackbar.make(binding.root, "Vui lòng chọn ít nhất 1 người dùng", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val role = when (roleGroup.checkedChipId) {
                R.id.chipAdmin -> WorkspaceRole.ADMIN
                R.id.chipEditor -> WorkspaceRole.EDITOR
                else -> WorkspaceRole.VIEWER
            }
            
            workspaceId?.let { id ->
                viewModel.inviteMultipleUsers(id, selectedUsers, role)
            }
            
            dialog.dismiss()
        }
    }
    
    private fun showEditWorkspaceDialog() {
        val workspace = viewModel.currentWorkspace.value ?: return
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_workspace, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.editTextWorkspaceName)
        val descInput = dialogView.findViewById<TextInputEditText>(R.id.editTextWorkspaceDescription)
        
        nameInput.setText(workspace.name)
        descInput.setText(workspace.description)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Chỉnh Sửa Không Gian")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val name = nameInput.text.toString().trim()
                val description = descInput.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    viewModel.updateWorkspace(workspace.id, name, description)
                } else {
                    Snackbar.make(binding.root, "Vui lòng nhập tên", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun showChangeRoleDialog(memberId: String, currentRole: WorkspaceRole) {
        val roles = WorkspaceRole.values()
        val roleNames = roles.map { it.name }.toTypedArray()
        val currentIndex = roles.indexOf(currentRole)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Đổi Vai Trò")
            .setSingleChoiceItems(roleNames, currentIndex) { dialog, which ->
                viewModel.updateMemberRole(memberId, roles[which])
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun confirmRemoveMember(memberId: String, userEmail: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xóa Thành Viên")
            .setMessage("Xóa $userEmail khỏi không gian này?")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.removeMember(memberId)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
