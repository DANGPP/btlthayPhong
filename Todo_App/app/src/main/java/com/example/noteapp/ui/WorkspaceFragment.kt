package com.example.noteapp.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.R
import com.example.noteapp.databinding.FragmentWorkspaceBinding
import com.example.noteapp.viewmodel.WorkspaceViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class WorkspaceFragment : Fragment() {
    private var _binding: FragmentWorkspaceBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: WorkspaceViewModel
    private lateinit var workspaceAdapter: WorkspaceListAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkspaceBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val factory = WorkspaceViewModel.WorkspaceViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[WorkspaceViewModel::class.java]
        
        setupRecyclerView()
        setupObservers()
        setupListeners()
        
        viewModel.loadWorkspaces()
        
        // Load pending invitations to check for new invites
        val sessionManager = com.example.noteapp.auth.SessionManager(requireContext())
        val userEmail = sessionManager.getUserEmail()
        userEmail?.let { viewModel.loadPendingInvitations(it) }
    }
    
    private fun setupRecyclerView() {
        workspaceAdapter = WorkspaceListAdapter(
            onWorkspaceClick = { workspace ->
                viewModel.selectWorkspace(workspace.id)
                showWorkspaceDetailsDialog(workspace.id)
            },
            onDeleteClick = { workspace ->
                confirmDeleteWorkspace(workspace.id, workspace.name)
            }
        )
        
        binding.recyclerViewWorkspaces.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = workspaceAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.workspaces.observe(viewLifecycleOwner) { workspaces ->
            workspaceAdapter.submitList(workspaces)
            binding.emptyView.visibility = if (workspaces.isEmpty()) View.VISIBLE else View.GONE
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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
        
        viewModel.invitations.observe(viewLifecycleOwner) { invitations ->
            if (invitations.isNotEmpty()) {
                Snackbar.make(
                    binding.root,
                    "Bạn có ${invitations.size} lời mời chờ xử lý. Kiểm tra menu → Lời Mời",
                    Snackbar.LENGTH_LONG
                ).setAction("Xem") {
                    showInvitationsDialog()
                }.show()
            }
        }
    }
    
    private fun setupListeners() {
        binding.fabAddWorkspace.setOnClickListener {
            showCreateWorkspaceDialog()
        }
    }
    
    private fun showCreateWorkspaceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_workspace, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.editTextWorkspaceName)
        val descInput = dialogView.findViewById<TextInputEditText>(R.id.editTextWorkspaceDescription)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Tạo Không Gian Làm Việc")
            .setView(dialogView)
            .setPositiveButton("Tạo") { _, _ ->
                val name = nameInput.text.toString().trim()
                val description = descInput.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    viewModel.createWorkspace(name, description)
                } else {
                    Snackbar.make(binding.root, "Vui lòng nhập tên", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun showWorkspaceDetailsDialog(workspaceId: String) {
        val fragment = WorkspaceDetailsFragment.newInstance(workspaceId)
        fragment.show(parentFragmentManager, "workspace_details")
    }
    
    private fun confirmDeleteWorkspace(workspaceId: String, workspaceName: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xóa Không Gian")
            .setMessage("Bạn có chắc chắn muốn xóa \"$workspaceName\"? Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteWorkspace(workspaceId)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_workspace, menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_invitations -> {
                showInvitationsDialog()
                true
            }
            R.id.action_refresh -> {
                viewModel.loadWorkspaces()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showInvitationsDialog() {
        val fragment = InvitationsFragment()
        fragment.show(parentFragmentManager, "invitations")
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
