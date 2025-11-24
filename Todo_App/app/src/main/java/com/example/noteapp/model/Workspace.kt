package com.example.noteapp.model

import java.io.Serializable

/**
 * Workspace - Không gian làm việc chung (giống Project trong Jira)
 */
data class Workspace(
    var id: String = "",
    var name: String,
    var description: String = "",
    var ownerId: String, // User tạo workspace
    var createdTime: String
) : Serializable

/**
 * WorkspaceMember - Thành viên trong workspace với quyền
 */
data class WorkspaceMember(
    var id: String = "",
    var workspaceId: String,
    var userId: String,
    var userName: String = "",
    var userEmail: String = "",
    var role: WorkspaceRole = WorkspaceRole.VIEWER,
    var joinedTime: String,
    var invitedBy: String = ""
) : Serializable

/**
 * WorkspaceRole - Vai trò và quyền hạn
 */
enum class WorkspaceRole(val value: String, val displayName: String, val color: Int) {
    ADMIN("admin", "Admin", android.R.color.holo_red_dark),           // Toàn quyền
    EDITOR("editor", "Editor", android.R.color.holo_orange_dark),        // Xem, thêm, sửa, xóa tasks
    VIEWER("viewer", "Viewer", android.R.color.holo_green_dark);        // Chỉ xem
    
    companion object {
        fun fromValue(value: String): WorkspaceRole {
            return values().find { it.value == value } ?: VIEWER
        }
    }
    
    // Check permissions
    fun canView(): Boolean = true
    
    fun canCreate(): Boolean = this == ADMIN || this == EDITOR
    
    fun canEdit(): Boolean = this == ADMIN || this == EDITOR
    
    fun canDelete(): Boolean = this == ADMIN || this == EDITOR
    
    fun canInvite(): Boolean = this == ADMIN || this == EDITOR
    
    fun canManageMembers(): Boolean = this == ADMIN
}

/**
 * Invitation - Lời mời tham gia workspace
 */
data class WorkspaceInvitation(
    var id: String = "",
    var workspaceId: String,
    var workspaceName: String = "",
    var invitedEmail: String,
    var invitedBy: String,
    var role: WorkspaceRole = WorkspaceRole.VIEWER,
    var status: InvitationStatus = InvitationStatus.PENDING,
    var createdTime: String
) : Serializable

enum class InvitationStatus(val value: String, val displayName: String) {
    PENDING("pending", "Pending"),
    ACCEPTED("accepted", "Accepted"),
    DECLINED("declined", "Declined"),
    EXPIRED("expired", "Expired");
    
    companion object {
        fun fromValue(value: String): InvitationStatus {
            return values().find { it.value == value } ?: PENDING
        }
    }
}
