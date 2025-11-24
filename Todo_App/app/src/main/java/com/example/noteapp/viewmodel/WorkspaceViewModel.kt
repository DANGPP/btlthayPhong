package com.example.noteapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.noteapp.auth.SessionManager
import com.example.noteapp.model.*
import com.example.noteapp.repository.WorkspaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WorkspaceViewModel(private val context: Context) : ViewModel() {
    private val repository = WorkspaceRepository(context)
    private val sessionManager = SessionManager(context)
    
    private val _workspaces = MutableLiveData<List<Workspace>>()
    val workspaces: LiveData<List<Workspace>> = _workspaces
    
    private val _currentWorkspace = MutableLiveData<Workspace?>()
    val currentWorkspace: LiveData<Workspace?> = _currentWorkspace
    
    private val _members = MutableLiveData<List<WorkspaceMember>>()
    val members: LiveData<List<WorkspaceMember>> = _members
    
    private val _invitations = MutableLiveData<List<WorkspaceInvitation>>()
    val invitations: LiveData<List<WorkspaceInvitation>> = _invitations
    
    private val _userRole = MutableLiveData<WorkspaceRole?>()
    val userRole: LiveData<WorkspaceRole?> = _userRole
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _operationSuccess = MutableLiveData<String?>()
    val operationSuccess: LiveData<String?> = _operationSuccess
    
    fun loadWorkspaces() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val userId = sessionManager.getCurrentUserId()
        if (userId != null) {
            val result = repository.getWorkspacesByUserId(userId)
            _workspaces.postValue(result)
        }
        _isLoading.postValue(false)
    }
    
    fun selectWorkspace(workspaceId: String) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val workspace = repository.getWorkspaceById(workspaceId)
        _currentWorkspace.postValue(workspace)
        
        // Load members
        loadMembers(workspaceId)
        
        // Get user role
        val userId = sessionManager.getCurrentUserId()
        if (userId != null) {
            val role = repository.getMemberRole(workspaceId, userId)
            _userRole.postValue(role)
        }
        _isLoading.postValue(false)
    }
    
    fun createWorkspace(name: String, description: String) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            _error.postValue("User not authenticated")
            _isLoading.postValue(false)
            return@launch
        }
        
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val workspace = Workspace(
            name = name,
            description = description,
            ownerId = userId,
            createdTime = sdf.format(Date())
        )
        
        val result = repository.createWorkspace(workspace)
        if (result != null) {
            // Add owner as ADMIN member
            val ownerMember = WorkspaceMember(
                workspaceId = result.id,
                userId = userId,
                userEmail = sessionManager.getUserEmail() ?: "",
                role = WorkspaceRole.ADMIN,
                joinedTime = sdf.format(Date()),
                invitedBy = userId
            )
            repository.addMember(ownerMember)
            
            _operationSuccess.postValue("Workspace created successfully")
            loadWorkspaces()
        } else {
            _error.postValue("Failed to create workspace")
        }
        _isLoading.postValue(false)
    }
    
    fun updateWorkspace(workspaceId: String, name: String, description: String) = 
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            val current = _currentWorkspace.value
            if (current != null) {
                val updated = current.copy(name = name, description = description)
                val result = repository.updateWorkspace(workspaceId, updated)
                if (result != null) {
                    _currentWorkspace.postValue(result)
                    _operationSuccess.postValue("Workspace updated")
                } else {
                    _error.postValue("Failed to update workspace")
                }
            }
            _isLoading.postValue(false)
        }
    
    fun deleteWorkspace(workspaceId: String) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val success = repository.deleteWorkspace(workspaceId)
        if (success) {
            _operationSuccess.postValue("Workspace deleted")
            loadWorkspaces()
        } else {
            _error.postValue("Failed to delete workspace")
        }
        _isLoading.postValue(false)
    }
    
    private fun loadMembers(workspaceId: String) = viewModelScope.launch(Dispatchers.IO) {
        val result = repository.getWorkspaceMembers(workspaceId)
        _members.postValue(result)
    }
    
    fun inviteMember(workspaceId: String, email: String, role: WorkspaceRole) = 
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            val userId = sessionManager.getCurrentUserId()
            val workspace = _currentWorkspace.value
            
            if (userId == null || workspace == null) {
                _error.postValue("Invalid state")
                _isLoading.postValue(false)
                return@launch
            }
            
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val invitation = WorkspaceInvitation(
                workspaceId = workspaceId,
                workspaceName = workspace.name,
                invitedEmail = email,
                invitedBy = userId,
                role = role,
                createdTime = sdf.format(Date())
            )
            
            val result = repository.createInvitation(invitation)
            if (result != null) {
                _operationSuccess.postValue("Invitation sent to $email")
            } else {
                _error.postValue("Failed to send invitation")
            }
            _isLoading.postValue(false)
        }
    
    fun loadPendingInvitations(userEmail: String) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val result = repository.getPendingInvitations(userEmail)
        _invitations.postValue(result)
        _isLoading.postValue(false)
    }
    
    fun acceptInvitation(invitation: WorkspaceInvitation) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val userId = sessionManager.getCurrentUserId()
        
        if (userId == null) {
            _error.postValue("User not authenticated")
            _isLoading.postValue(false)
            return@launch
        }
        
        // Update invitation status
        val updated = repository.updateInvitationStatus(invitation.id, InvitationStatus.ACCEPTED)
        if (updated) {
            // Add as member
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val member = WorkspaceMember(
                workspaceId = invitation.workspaceId,
                userId = userId,
                userEmail = invitation.invitedEmail,
                role = invitation.role,
                joinedTime = sdf.format(Date()),
                invitedBy = invitation.invitedBy
            )
            
            repository.addMember(member)
            _operationSuccess.postValue("Joined workspace: ${invitation.workspaceName}")
            loadWorkspaces()
        } else {
            _error.postValue("Failed to accept invitation")
        }
        _isLoading.postValue(false)
    }
    
    fun declineInvitation(invitationId: String) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val success = repository.updateInvitationStatus(invitationId, InvitationStatus.DECLINED)
        if (success) {
            _operationSuccess.postValue("Invitation declined")
        }
        _isLoading.postValue(false)
    }
    
    fun updateMemberRole(memberId: String, newRole: WorkspaceRole) = 
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            val success = repository.updateMemberRole(memberId, newRole)
            if (success) {
                _operationSuccess.postValue("Member role updated")
                _currentWorkspace.value?.id?.let { loadMembers(it) }
            } else {
                _error.postValue("Failed to update role")
            }
            _isLoading.postValue(false)
        }
    
    fun removeMember(memberId: String) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val success = repository.removeMember(memberId)
        if (success) {
            _operationSuccess.postValue("Member removed")
            _currentWorkspace.value?.id?.let { loadMembers(it) }
        } else {
            _error.postValue("Failed to remove member")
        }
        _isLoading.postValue(false)
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearOperationSuccess() {
        _operationSuccess.value = null
    }
    
    class WorkspaceViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WorkspaceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WorkspaceViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
