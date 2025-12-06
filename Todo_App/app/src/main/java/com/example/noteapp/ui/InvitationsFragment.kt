package com.example.noteapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.databinding.DialogInvitationsBinding
import com.example.noteapp.viewmodel.WorkspaceViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.example.noteapp.auth.SessionManager

class InvitationsFragment : BottomSheetDialogFragment() {
    private var _binding: DialogInvitationsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: WorkspaceViewModel
    private lateinit var invitationAdapter: InvitationAdapter
    
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
        
        val factory = WorkspaceViewModel.WorkspaceViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[WorkspaceViewModel::class.java]
        
        setupRecyclerView()
        setupObservers()
        
        // Load invitations for current user
        val sessionManager = SessionManager(requireContext())
        val userEmail = sessionManager.getUserEmail()
        if (userEmail != null) {
            viewModel.loadPendingInvitations(userEmail)
        }
    }
    
    private fun setupRecyclerView() {
        invitationAdapter = InvitationAdapter(
            onAcceptClick = { invitation ->
                viewModel.acceptInvitation(invitation.id)
            },
            onDeclineClick = { invitation ->
                viewModel.rejectInvitation(invitation.id)
            }
        )
        
        binding.recyclerViewInvitations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = invitationAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.invitations.observe(viewLifecycleOwner) { invitations ->
            invitationAdapter.submitList(invitations)
            binding.emptyView.visibility = if (invitations.isEmpty()) View.VISIBLE else View.GONE
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
                dismiss()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
