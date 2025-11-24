package com.example.noteapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.databinding.DialogWorkspaceDetailsBinding
import com.example.noteapp.model.WorkspaceRole
import com.example.noteapp.viewmodel.WorkspaceViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.example.noteapp.R

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
    
    private fun setupObservers() {
        viewModel.currentWorkspace.observe(viewLifecycleOwner) { workspace ->
            workspace?.let {
                binding.textViewWorkspaceName.text = it.name
                binding.textViewWorkspaceDescription.text = it.description.ifEmpty { "No description" }
                
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
            binding.textViewMemberCount.text = "Members (${members.size})"
            
            if (members.isEmpty()) {
                binding.recyclerViewMembers.visibility = View.GONE
                binding.textViewEmptyMembers.visibility = View.VISIBLE
            } else {
                binding.recyclerViewMembers.visibility = View.VISIBLE
                binding.textViewEmptyMembers.visibility = View.GONE
            }
        }
        
        viewModel.userRole.observe(viewLifecycleOwner) { role ->
            // Show/hide admin actions based on role (if not owner)
            val workspace = viewModel.currentWorkspace.value
            val sessionManager = com.example.noteapp.auth.SessionManager(requireContext())
            val currentUserId = sessionManager.getCurrentUserId()
            val isOwner = workspace?.ownerId == currentUserId
            
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
        binding.buttonInviteMember.setOnClickListener {
            showInviteMemberDialog()
        }
        
        binding.buttonEditWorkspace.setOnClickListener {
            showEditWorkspaceDialog()
        }
    }
    
    private fun showInviteMemberDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_invite_member, null)
        val emailInput = dialogView.findViewById<TextInputEditText>(R.id.editTextInviteEmail)
        val roleGroup = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupRole)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Invite Member")
            .setMessage("Enter the exact email address the user registered with. They will see the invitation in Menu → Invitations.")
            .setView(dialogView)
            .setPositiveButton("Send Invitation") { _, _ ->
                val email = emailInput.text.toString().trim()
                val role = when (roleGroup.checkedChipId) {
                    R.id.chipAdmin -> WorkspaceRole.ADMIN
                    R.id.chipEditor -> WorkspaceRole.EDITOR
                    else -> WorkspaceRole.VIEWER
                }
                
                if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    workspaceId?.let { viewModel.inviteMember(it, email, role) }
                } else {
                    Snackbar.make(binding.root, "Invalid email", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEditWorkspaceDialog() {
        val workspace = viewModel.currentWorkspace.value ?: return
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_workspace, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.editTextWorkspaceName)
        val descInput = dialogView.findViewById<TextInputEditText>(R.id.editTextWorkspaceDescription)
        
        nameInput.setText(workspace.name)
        descInput.setText(workspace.description)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Workspace")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString().trim()
                val description = descInput.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    viewModel.updateWorkspace(workspace.id, name, description)
                } else {
                    Snackbar.make(binding.root, "Name required", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showChangeRoleDialog(memberId: String, currentRole: WorkspaceRole) {
        val roles = WorkspaceRole.values()
        val roleNames = roles.map { it.name }.toTypedArray()
        val currentIndex = roles.indexOf(currentRole)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Role")
            .setSingleChoiceItems(roleNames, currentIndex) { dialog, which ->
                viewModel.updateMemberRole(memberId, roles[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun confirmRemoveMember(memberId: String, userEmail: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove Member")
            .setMessage("Remove $userEmail from this workspace?")
            .setPositiveButton("Remove") { _, _ ->
                viewModel.removeMember(memberId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
