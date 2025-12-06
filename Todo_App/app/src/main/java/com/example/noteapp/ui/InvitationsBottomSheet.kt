package com.example.noteapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.adapter.WorkspaceInvitationAdapter
import com.example.noteapp.databinding.DialogInvitationsBinding
import com.example.noteapp.model.WorkspaceRole
import com.example.noteapp.viewmodel.WorkspaceViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

/**
 * Bottom sheet dialog hiển thị danh sách lời mời workspace
 */
class InvitationsBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: DialogInvitationsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: WorkspaceViewModel
    private lateinit var adapter: WorkspaceInvitationAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogInvitationsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(
            requireActivity(),
            WorkspaceViewModel.WorkspaceViewModelFactory(requireContext())
        )[WorkspaceViewModel::class.java]
        
        setupRecyclerView()
        observeViewModel()
        
        // Load invitations
        val userEmail = com.example.noteapp.auth.SessionManager(requireContext()).getUserEmail()
        if (userEmail != null) {
            viewModel.loadPendingInvitations(userEmail)
        }
    }
    
    private fun setupRecyclerView() {
        adapter = WorkspaceInvitationAdapter(
            onAccept = { invitation ->
                viewModel.acceptInvitation(invitation.id)
            },
            onReject = { invitation ->
                viewModel.rejectInvitation(invitation.id)
            }
        )
        
        binding.recyclerViewInvitations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@InvitationsBottomSheet.adapter
        }
    }
    
    private fun observeViewModel() {
        viewModel.invitations.observe(viewLifecycleOwner) { invitations ->
            if (invitations.isEmpty()) {
                binding.recyclerViewInvitations.visibility = View.GONE
                binding.emptyView?.visibility = View.VISIBLE
            } else {
                binding.recyclerViewInvitations.visibility = View.VISIBLE
                binding.emptyView?.visibility = View.GONE
                adapter.submitList(invitations)
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.operationSuccess.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearOperationSuccess()
                // Reload invitations after accept/reject
                val userEmail = com.example.noteapp.auth.SessionManager(requireContext()).getUserEmail()
                if (userEmail != null) {
                    viewModel.loadPendingInvitations(userEmail)
                }
            }
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        const val TAG = "InvitationsBottomSheet"
    }
}
