# Hướng Dẫn Cấu Hình Appwrite

## Bước 1: Lấy Thông Tin Project

1. Đăng nhập vào [Appwrite Console](https://cloud.appwrite.io)
2. Mở project của bạn (hoặc tạo project mới)
3. Vào **Settings** → Copy **Project ID**

## Bước 2: Tạo Database và Collections

### Tạo Database
1. Vào **Databases** → Click **Create database**
2. Đặt tên: `todo_app_db` (hoặc tên bạn muốn)
3. Copy **Database ID**

### Tạo Collection: Users
1. Trong database vừa tạo, click **Create collection**
2. Đặt tên: `users`
3. Copy **Collection ID** (dạng: `6908cdf2000c4f89e55d`)
4. Thêm các attributes:
   - `name` (String, size: 255, required)
   - `email` (String, size: 255, required)
   - `createdAt` (String, size: 100)
   - `updatedAt` (String, size: 100)

### Tạo Collection: Todos
1. Click **Create collection**
2. Đặt tên: `todos`
3. Copy **Collection ID**
4. Thêm các attributes:
   - `title` (String, size: 500, required)
   - `description` (String, size: 5000)
   - `createdTime` (String, size: 100, required)
   - `dueTime` (String, size: 100)
   - `completedDate` (String, size: 100)
   - `userId` (String, size: 100, required)
   - `status` (String, size: 50, default: "to_do")
   - `priority` (String, size: 50, default: "medium")
   - `category` (String, size: 100, default: "General")
   - `reminderTime` (String, size: 100)
   - `estimatedDuration` (Integer, default: 0)
   - `actualDuration` (Integer, default: 0)

5. **Cấu hình Indexes** (quan trọng cho search):
   - Index 1: Key `userId_index`, Attribute: `userId`, Type: `key`
   - Index 2: Key `status_index`, Attribute: `status`, Type: `key`
   - Index 3: Key `priority_index`, Attribute: `priority`, Type: `key`

6. **Cấu hình Permissions**:
   - Read: `Any`
   - Create: `Any`
   - Update: `Any`
   - Delete: `Any`

### Tạo Collection: Notes
1. Click **Create collection**
2. Đặt tên: `notes`
3. Copy **Collection ID**
4. Thêm các attributes:
   - `title` (String, size: 500, required)
   - `description` (String, size: 10000)
   - `createdTime` (String, size: 100, required)
   - `userId` (String, size: 100, required)

5. **Cấu hình Permissions** tương tự như Todos

### Tạo Collection: Workspaces (Cho tính năng chia sẻ)
1. Click **Create collection**
2. Đặt tên: `workspaces`
3. Copy **Collection ID**
4. Thêm các attributes:
   - `name` (String, size: 255, required)
   - `description` (String, size: 1000)
   - `ownerId` (String, size: 100, required)
   - `createdTime` (String, size: 100, required)

5. **Cấu hình Indexes**:
   - Index: Key `ownerId_index`, Attribute: `ownerId`, Type: `key`

6. **Cấu hình Permissions** tương tự như Todos

### Tạo Collection: Workspace Members
1. Click **Create collection**
2. Đặt tên: `workspace_members`
3. Copy **Collection ID**
4. Thêm các attributes:
   - `workspaceId` (String, size: 100, required)
   - `userId` (String, size: 100, required)
   - `userEmail` (String, size: 255, required)
   - `role` (String, size: 50, required) // ADMIN, EDITOR, VIEWER
   - `joinedTime` (String, size: 100, required)
   - `invitedBy` (String, size: 100)

5. **Cấu hình Indexes**:
   - Index 1: Key `workspaceId_index`, Attribute: `workspaceId`, Type: `key`
   - Index 2: Key `userId_index`, Attribute: `userId`, Type: `key`

6. **Cấu hình Permissions** tương tự như Todos

### Tạo Collection: Workspace Invitations
1. Click **Create collection**
2. Đặt tên: `workspace_invitations`
3. Copy **Collection ID**
4. Thêm các attributes:
   - `workspaceId` (String, size: 100, required)
   - `workspaceName` (String, size: 255, required)
   - `invitedEmail` (String, size: 255, required)
   - `invitedBy` (String, size: 100, required)
   - `role` (String, size: 50, required) // ADMIN, EDITOR, VIEWER
   - `status` (String, size: 50, default: "PENDING") // PENDING, ACCEPTED, DECLINED, CANCELLED
   - `createdTime` (String, size: 100, required)

5. **Cấu hình Indexes**:
   - Index 1: Key `invitedEmail_index`, Attribute: `invitedEmail`, Type: `key`
   - Index 2: Key `status_index`, Attribute: `status`, Type: `key`

6. **Cấu hình Permissions** tương tự như Todos

## Bước 3: Cấu Hình trong Code

Mở file `AppwriteConfig.kt` và thay thế các giá trị:

```kotlin
object AppwriteConfig {
    private const val ENDPOINT = "https://sgp.cloud.appwrite.io/v1"
    private const val PROJECT_ID = "YOUR_PROJECT_ID_HERE" 
    const val DATABASE_ID = "YOUR_DATABASE_ID_HERE"
    const val USER_COLLECTION_ID = "YOUR_USERS_COLLECTION_ID_HERE"
    const val NOTE_COLLECTION_ID = "YOUR_NOTES_COLLECTION_ID_HERE"
    const val TODO_COLLECTION_ID = "YOUR_TODOS_COLLECTION_ID_HERE"
    
    // Workspace Collections (cho tính năng chia sẻ)
    const val WORKSPACE_COLLECTION_ID = "YOUR_WORKSPACES_COLLECTION_ID_HERE"
    const val WORKSPACE_MEMBER_COLLECTION_ID = "YOUR_WORKSPACE_MEMBERS_COLLECTION_ID_HERE"
    const val WORKSPACE_INVITATION_COLLECTION_ID = "YOUR_WORKSPACE_INVITATIONS_COLLECTION_ID_HERE"
}
```

## Bước 4: Cấu Hình Auth

1. Vào **Auth** → **Settings**
2. Bật **Email/Password** authentication
3. (Tùy chọn) Tắt email verification để test nhanh hơn:
   - Vào **Security** → Tắt **Email verification**

## Bước 5: Cập nhật Todos Collection cho Workspace

Thêm các attributes sau vào collection `todos` để hỗ trợ chia sẻ:
- `workspaceId` (String, size: 100) - nullable
- `assignedTo` (String, size: 100) - nullable 
- `createdBy` (String, size: 100, required)

**Cấu hình Indexes**:
- Index: Key `workspaceId_index`, Attribute: `workspaceId`, Type: `key`

## Bước 6: Test

### Test Basic Features
1. Build và chạy app
2. Thử đăng ký tài khoản mới
3. Đăng nhập
4. Tạo todo mới
5. Kiểm tra trong Appwrite Console xem data đã được lưu chưa

### Test Workspace Features (Chia sẻ giống Jira)
1. Vào menu → **Workspaces**
2. Tạo workspace mới
3. Click vào workspace → Click **Invite Member**
4. Nhập email của user khác và chọn role (Admin/Editor/Viewer)
5. User được mời sẽ thấy invitation trong menu → **Invitations**
6. Accept invitation
7. Tạo todo và assign cho workspace
8. Kiểm tra permissions:
   - **ADMIN**: Full access (thêm/sửa/xóa tasks, mời members, quản lý workspace)
   - **EDITOR**: Tạo/sửa/xóa tasks, xem members
   - **VIEWER**: Chỉ xem tasks, không được edit

## Lưu Ý Quan Trọng

- ⚠️ **Collection IDs** là chuỗi dài (VD: `6908ce0d001a9bad8c83`), KHÔNG phải tên collection
- ⚠️ Đảm bảo cấu hình **Permissions** đúng, nếu không sẽ bị lỗi 401/403
- ⚠️ Nếu dùng production, nên cấu hình permissions chặt chẽ hơn (user chỉ được access data của mình)
- ⚠️ Callback URL trong AndroidManifest.xml phải khớp với Project ID

## Troubleshooting

### Lỗi 404 - Collection not found
→ Kiểm tra lại Collection IDs trong `AppwriteConfig.kt`

### Lỗi 401/403 - Unauthorized
→ Kiểm tra lại Permissions của Collection

### Không đăng nhập được
→ Kiểm tra Auth settings, đảm bảo Email/Password đã được bật

### Không tạo được todo
→ Kiểm tra:
1. User đã login chưa (có session chưa)
2. Collection attributes đã đúng chưa
3. Permissions của collection
