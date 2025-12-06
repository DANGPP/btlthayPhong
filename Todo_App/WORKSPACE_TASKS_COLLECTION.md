# Tạo Collection `workspace_tasks` trong Appwrite

## ⚠️ QUAN TRỌNG: Bạn thiếu collection này!

Theo code và ảnh màn hình, bạn đã có:
- ✅ `workspaces`
- ✅ `workspace_members`
- ✅ `workspace_invitations`

Nhưng **THIẾU** collection `workspace_tasks` để lưu các task trong workspace.

---

## 🔧 Các bước tạo Collection

### 1. Vào Appwrite Console
- Truy cập: https://cloud.appwrite.io
- Chọn project: **TODOAPP** (ID: `6908ccdf00223cfe80cd`)
- Vào **Database** → Database ID: `6908cde40006b4bbd549`

### 2. Tạo Collection mới
- Click **"Create Collection"**
- Collection Name: `workspace_tasks`
- Collection ID: Để Appwrite tự tạo (hoặc nhập custom)
- Click **Create**

### 3. Thêm các Attributes (Fields)

#### **String Attributes:**

| Attribute Key | Type | Size | Required | Default | Indexed |
|--------------|------|------|----------|---------|---------|
| `title` | String | 500 | ✅ Yes | - | No |
| `description` | String | 5000 | ❌ No | NULL | No |
| `workspaceId` | String | 100 | ✅ Yes | - | ✅ Yes |
| `createdBy` | String | 100 | ✅ Yes | - | ✅ Yes |
| `status` | String | 50 | ✅ Yes | to_do | ✅ Yes |
| `priority` | String | 50 | ✅ Yes | medium | No |
| `category` | String | 100 | ❌ No | Chung | No |
| `dueDate` | String | 50 | ❌ No | NULL | No |
| `dueTime` | String | 50 | ❌ No | NULL | No |

#### **Array Attribute:**

| Attribute Key | Type | Size | Required | Default |
|--------------|------|------|----------|---------|
| `assignedTo` | String Array | 100 per item | ❌ No | [] |

#### **Integer Attributes:**

| Attribute Key | Type | Min | Max | Required | Default |
|--------------|------|-----|-----|----------|---------|
| `estimatedHours` | Integer | 0 | 1000 | ❌ No | 0 |
| `actualHours` | Integer | 0 | 1000 | ❌ No | 0 |
| `createdAt` | Integer | - | - | ✅ Yes | - |
| `updatedAt` | Integer | - | - | ✅ Yes | - |

---

## 4. Cấu hình Permissions

Vào tab **Settings** của collection:

### Document Security:
- **Create**: `users` (chỉ user đã login mới tạo được)
- **Read**: `users` (chỉ user đã login mới đọc được)
- **Update**: `users`
- **Delete**: `users`

### Collection Permissions:
- Enable **Read** cho `users`

---

## 5. Tạo Indexes (Optional nhưng khuyến nghị)

Vào tab **Indexes** → **Create Index**:

### Index 1: Tìm task theo workspace
- **Index Key**: `idx_workspace`
- **Type**: Key
- **Attributes**: `workspaceId` (ASC)

### Index 2: Tìm task theo status
- **Index Key**: `idx_status`
- **Type**: Key
- **Attributes**: `status` (ASC)

### Index 3: Tìm task theo creator
- **Index Key**: `idx_creator`
- **Type**: Key
- **Attributes**: `createdBy` (ASC)

---

## 6. Copy Collection ID

Sau khi tạo xong:
1. Vào collection `workspace_tasks`
2. Copy **Collection ID** (dạng: `67a1b2c3d4e5f6g7h8i9`)
3. Paste vào file `AppwriteConfig.kt`:

```kotlin
const val WORKSPACE_TASK_COLLECTION_ID = "PASTE_COLLECTION_ID_HERE"
```

---

## 📝 Enum Values cần nhớ

### Status (giá trị lưu trong DB):
- `"to_do"` - Cần làm
- `"in_progress"` - Đang làm
- `"in_review"` - Đang review
- `"done"` - Hoàn thành
- `"completed"` - Hoàn thành
- `"cancelled"` - Hủy
- `"on_hold"` - Tạm dừng

### Priority:
- `"low"` - Thấp
- `"medium"` - Trung bình
- `"high"` - Cao
- `"urgent"` - Khẩn cấp

---

## ✅ Kiểm tra sau khi tạo

1. Vào **Rows** tab của collection
2. Thử tạo 1 document test thủ công
3. Nếu tạo được → Collection đã setup đúng
4. Xóa document test đi
5. Chạy app và thử tạo task từ app

---

## 🐛 Nếu vẫn lỗi

Kiểm tra:
1. Collection ID trong `AppwriteConfig.kt` có đúng không
2. Permissions có cho phép user create không
3. Tất cả required fields có được gửi đúng không
4. Check logs trong Android Studio: View → Tool Windows → Logcat
5. Search lỗi "AppwriteException" hoặc "createTask"
