package com.example.noteapp.repository

import android.content.Context
import android.util.Log
import com.example.noteapp.appwrite.AppwriteConfig
import com.example.noteapp.model.*
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Document
import io.appwrite.services.Databases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkspaceRepository(private val context: Context) {
    private val databases: Databases = AppwriteConfig.getDatabases(context)
    
    companion object {
        private const val TAG = "WorkspaceRepository"
    }
    
    // ==================== WORKSPACE CRUD ====================
    
    suspend fun createWorkspace(workspace: Workspace): Workspace? {
        return withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "name" to workspace.name,
                    "description" to workspace.description,
                    "ownerId" to workspace.ownerId,
                    "createdTime" to workspace.createdTime
                )
                
                val document = databases.createDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.WORKSPACE_COLLECTION_ID,
                    documentId = ID.unique(),
                    data = data
                )
                
                Log.d(TAG, "Workspace created: ${document.id}")
                documentToWorkspace(document)
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to create workspace: ${e.message}")
                null
            }
        }
    }
    
    suspend fun getWorkspaceById(workspaceId: String): Workspace? {
        return withContext(Dispatchers.IO) {
            try {
                val document = databases.getDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.WORKSPACE_COLLECTION_ID,
                    documentId = workspaceId
                )
                documentToWorkspace(document)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get workspace: ${e.message}")
                null
            }
        }
    }
    
    suspend fun getWorkspacesByUserId(userId: String): List<Workspace> {
        return withContext(Dispatchers.IO) {
            try {
                // Get workspaces where user is owner
                val ownedDocs = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.WORKSPACE_COLLECTION_ID,
                    queries = listOf(Query.equal("ownerId", userId))
                )
                
                // Get workspaces where user is member via workspace_members collection
                val memberRecords = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.WORKSPACE_MEMBER_COLLECTION_ID,
                    queries = listOf(Query.equal("userId", userId))
                )
                
                val workspaces = mutableListOf<Workspace>()
                ownedDocs.documents.mapNotNullTo(workspaces) { documentToWorkspace(it) }
                
                // Get workspaces from member records
                memberRecords.documents.forEach { memberDoc ->
                    val workspaceId = memberDoc.data["workspaceId"] as? String
                    workspaceId?.let { id ->
                        getWorkspaceById(id)?.let { workspace ->
                            workspaces.add(workspace)
                        }
                    }
                }
                
                workspaces.distinctBy { it.id }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get workspaces: ${e.message}")
                emptyList()
            }
        }
    }
    
    suspend fun updateWorkspace(workspaceId: String, workspace: Workspace): Workspace? {
        return withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "name" to workspace.name,
                    "description" to workspace.description
                )
                
                val document = databases.updateDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.WORKSPACE_COLLECTION_ID,
                    documentId = workspaceId,
                    data = data
                )
                
                documentToWorkspace(document)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update workspace: ${e.message}")
                null
            }
        }
    }
    
    suspend fun deleteWorkspace(workspaceId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                databases.deleteDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.WORKSPACE_COLLECTION_ID,
                    documentId = workspaceId
                )
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete workspace: ${e.message}")
                false
            }
        }
    }
    
    // ==================== WORKSPACE MEMBERS ====================
    
    suspend fun addMember(member: WorkspaceMember): WorkspaceMember? {
        return withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "workspaceId" to member.workspaceId,
                    "userId" to member.userId,
                    "userName" to member.userName,
                    "userEmail" to member.userEmail,
                    "role" to member.role.value,
                    "joinedTime" to member.joinedTime,
                    "invitedBy" to member.invitedBy
                )
                
                val document = databases.createDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.WORKSPACE_MEMBER_COLLECTION_ID,
                    documentId = ID.unique(),
                    data = data
                )
                
                Log.d(TAG, "Member added: ${document.id}")
                documentToWorkspaceMember(document)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add member: ${e.message}")
                null
            }
        }
    }
    
    suspend fun getWorkspaceMembers(workspaceId: String): List<WorkspaceMember> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.WORKSPACE_MEMBER_COLLECTION_ID,
                    queries = listOf(Query.equal("workspaceId", workspaceId))
                )
                
                documents.documents.mapNotNull { documentToWorkspaceMember(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get members: ${e.message}")
                emptyList()
            }
        }
    }
    
    suspend fun getMemberRole(workspaceId: String, userId: String): WorkspaceRole? {
        return withContext(Dispatchers.IO) {
            try {
                val documents = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.WORKSPACE_MEMBER_COLLECTION_ID,
                    queries = listOf(
                        Query.equal("workspaceId", workspaceId),
                        Query.equal("userId", userId)
                    )
                )
                
                documents.documents.firstOrNull()?.let { documentToWorkspaceMember(it)?.role }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get member role: ${e.message}")
                null
            }
        }
    }
    
    suspend fun updateMemberRole(memberId: String, newRole: WorkspaceRole): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                databases.updateDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.WORKSPACE_MEMBER_COLLECTION_ID,
                    documentId = memberId,
                    data = mapOf("role" to newRole.value)
                )
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update member role: ${e.message}")
                false
            }
        }
    }
    
    suspend fun removeMember(memberId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                databases.deleteDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.WORKSPACE_MEMBER_COLLECTION_ID,
                    documentId = memberId
                )
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove member: ${e.message}")
                false
            }
        }
    }
    
    // ==================== INVITATIONS ====================
    
    suspend fun createInvitation(invitation: WorkspaceInvitation): WorkspaceInvitation? {
        return withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "workspaceId" to invitation.workspaceId,
                    "workspaceName" to invitation.workspaceName,
                    "invitedEmail" to invitation.invitedEmail,
                    "invitedBy" to invitation.invitedBy,
                    "role" to invitation.role.value,
                    "status" to invitation.status.value,
                    "createdTime" to invitation.createdTime
                )
                
                val document = databases.createDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.WORKSPACE_INVITATION_COLLECTION_ID,
                    documentId = ID.unique(),
                    data = data
                )
                
                Log.d(TAG, "Invitation created: ${document.id}")
                documentToInvitation(document)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create invitation: ${e.message}")
                null
            }
        }
    }
    
    suspend fun getPendingInvitations(userEmail: String): List<WorkspaceInvitation> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.WORKSPACE_INVITATION_COLLECTION_ID,
                    queries = listOf(
                        Query.equal("invitedEmail", userEmail),
                        Query.equal("status", "pending")
                    )
                )
                
                documents.documents.mapNotNull { documentToInvitation(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get invitations: ${e.message}")
                emptyList()
            }
        }
    }
    
    suspend fun updateInvitationStatus(invitationId: String, status: InvitationStatus): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                databases.updateDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.WORKSPACE_INVITATION_COLLECTION_ID,
                    documentId = invitationId,
                    data = mapOf("status" to status.value)
                )
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update invitation: ${e.message}")
                false
            }
        }
    }
    
    // ==================== HELPER FUNCTIONS ====================
    
    private fun documentToWorkspace(document: Document<Map<String, Any>>): Workspace? {
        return try {
            Workspace(
                id = document.id,
                name = document.data["name"] as? String ?: "",
                description = document.data["description"] as? String ?: "",
                ownerId = document.data["ownerId"] as? String ?: "",
                createdTime = document.data["createdTime"] as? String ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error converting document to workspace: ${e.message}")
            null
        }
    }
    
    private fun documentToWorkspaceMember(document: Document<Map<String, Any>>): WorkspaceMember? {
        return try {
            WorkspaceMember(
                id = document.id,
                workspaceId = document.data["workspaceId"] as? String ?: "",
                userId = document.data["userId"] as? String ?: "",
                userName = document.data["userName"] as? String ?: "",
                userEmail = document.data["userEmail"] as? String ?: "",
                role = WorkspaceRole.fromValue(document.data["role"] as? String ?: "viewer"),
                joinedTime = document.data["joinedTime"] as? String ?: "",
                invitedBy = document.data["invitedBy"] as? String ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error converting document to member: ${e.message}")
            null
        }
    }
    
    private fun documentToInvitation(document: Document<Map<String, Any>>): WorkspaceInvitation? {
        return try {
            WorkspaceInvitation(
                id = document.id,
                workspaceId = document.data["workspaceId"] as? String ?: "",
                workspaceName = document.data["workspaceName"] as? String ?: "",
                invitedEmail = document.data["invitedEmail"] as? String ?: "",
                invitedBy = document.data["invitedBy"] as? String ?: "",
                role = WorkspaceRole.fromValue(document.data["role"] as? String ?: "viewer"),
                status = InvitationStatus.fromValue(document.data["status"] as? String ?: "pending"),
                createdTime = document.data["createdTime"] as? String ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error converting document to invitation: ${e.message}")
            null
        }
    }
}
