# Hướng Dẫn Tạo Collection workspace_tasks Trong Appwrite

## Bước 1: Truy cập Appwrite Console
1. Mở trình duyệt và truy cập: https://cloud.appwrite.io/console
2. Đăng nhập vào tài khoản của bạn
3. Chọn project: `6908ccdf00223cfe80cd`
4. Chọn database: `6908cde40006b4bbd549`

## Bước 2: Tạo Collection Mới
1. Click vào tab **"Databases"** ở menu bên trái
2. Chọn database hiện tại
3. Click nút **"Create Collection"**
4. Nhập Collection ID: `workspace_tasks`
5. Nhập Collection Name: `Workspace Tasks`
6. Click **"Create"**

## Bước 3: Cấu Hình Permissions
Trong màn hình collection settings:
1. Click tab **"Settings"**
2. Scroll xuống **"Permissions"**
3. Thêm các permissions sau:
   - **Read access**: Role: `users` (Tất cả users đã đăng nhập có thể đọc)
   - **Create access**: Role: `users` (Tất cả users đã đăng nhập có thể tạo)
   - **Update access**: Role: `users` (Tất cả users đã đăng nhập có thể cập nhật)
   - **Delete access**: Role: `users` (Tất cả users đã đăng nhập có thể xóa)

## Bước 4: Tạo Attributes (Thuộc Tính)
Click tab **"Attributes"** và thêm các attributes sau:

### 1. title (String - Required)
- Attribute Key: `title`
- Type: `String`
- Size: `255`
- Required: ✅ Yes
- Array: ❌ No
- Default: (empty)
- Click **"Create"**

### 2. description (String - Optional)
- Attribute Key: `description`
- Type: `String`
- Size: `5000`
- Required: ❌ No
- Array: ❌ No
- Default: (empty)
- Click **"Create"**

### 3. workspaceId (String - Required)
- Attribute Key: `workspaceId`
- Type: `String`
- Size: `255`
- Required: ✅ Yes
- Array: ❌ No
- Default: (empty)
- Click **"Create"**

### 4. createdBy (String - Required)
- Attribute Key: `createdBy`
- Type: `String`
- Size: `255`
- Required: ✅ Yes
- Array: ❌ No
- Default: (empty)
- Click **"Create"**

### 5. assignedTo (String Array - Optional)
- Attribute Key: `assignedTo`
- Type: `String`
- Size: `255`
- Required: ❌ No
- Array: ✅ Yes
- Default: (empty)
- Click **"Create"**

### 6. status (String - Optional)
- Attribute Key: `status`
- Type: `String`
- Size: `50`
- Required: ❌ No
- Array: ❌ No
- Default: `TODO`
- Click **"Create"**

### 7. priority (String - Optional)
- Attribute Key: `priority`
- Type: `String`
- Size: `50`
- Required: ❌ No
- Array: ❌ No
- Default: `MEDIUM`
- Click **"Create"**

### 8. category (String - Optional)
- Attribute Key: `category`
- Type: `String`
- Size: `100`
- Required: ❌ No
- Array: ❌ No
- Default: (empty)
- Click **"Create"**

### 9. dueDate (String - Optional)
- Attribute Key: `dueDate`
- Type: `String`
- Size: `50`
- Required: ❌ No
- Array: ❌ No
- Default: (empty)
- Click **"Create"**

### 10. dueTime (String - Optional)
- Attribute Key: `dueTime`
- Type: `String`
- Size: `50`
- Required: ❌ No
- Array: ❌ No
- Default: (empty)
- Click **"Create"**

### 11. estimatedHours (Integer - Optional)
- Attribute Key: `estimatedHours`
- Type: `Integer`
- Required: ❌ No
- Array: ❌ No
- Min: `0`
- Max: `1000`
- Default: `0`
- Click **"Create"**

### 12. actualHours (Integer - Optional)
- Attribute Key: `actualHours`
- Type: `Integer`
- Required: ❌ No
- Array: ❌ No
- Min: `0`
- Max: `1000`
- Default: `0`
- Click **"Create"**

### 13. createdAt (String - Optional)
- Attribute Key: `createdAt`
- Type: `String`
- Size: `50`
- Required: ❌ No
- Array: ❌ No
- Default: (empty)
- Click **"Create"**

### 14. updatedAt (String - Optional)
- Attribute Key: `updatedAt`
- Type: `String`
- Size: `50`
- Required: ❌ No
- Array: ❌ No
- Default: (empty)
- Click **"Create"**

## Bước 5: Tạo Indexes (Chỉ Mục) Để Tối Ưu Truy Vấn
Click tab **"Indexes"** và thêm các indexes sau:

### Index 1: workspaceId
- Key: `workspace_id_index`
- Type: `Key`
- Attributes: `workspaceId` (ASC)
- Click **"Create"**

### Index 2: status
- Key: `status_index`
- Type: `Key`
- Attributes: `status` (ASC)
- Click **"Create"**

### Index 3: createdBy
- Key: `created_by_index`
- Type: `Key`
- Attributes: `createdBy` (ASC)
- Click **"Create"**

### Index 4: Composite (workspaceId + status)
- Key: `workspace_status_index`
- Type: `Key`
- Attributes: `workspaceId` (ASC), `status` (ASC)
- Click **"Create"**

## Bước 6: Kiểm Tra Cấu Hình
1. Đảm bảo collection ID chính xác là: `workspace_tasks`
2. Kiểm tra tất cả 14 attributes đã được tạo
3. Kiểm tra permissions đã được cấu hình đúng
4. Kiểm tra các indexes đã được tạo

## Bước 7: Test Trong App
1. Build và chạy ứng dụng Android
2. Tạo hoặc mở một workspace
3. Click "Xem Bảng" để mở board view
4. Click nút ➕ để thêm task mới
5. Điền thông tin và click "Tạo"
6. Kiểm tra task xuất hiện trong cột tương ứng

## Lưu Ý Quan Trọng

### Phân Biệt workspace_tasks và todos
- **todos**: Collection cho task cá nhân của người dùng
- **workspace_tasks**: Collection cho task trong workspace (nhiều người cùng làm việc)
- Hai collection này hoàn toàn độc lập và không ảnh hưởng lẫn nhau

### Giá Trị Status Hợp Lệ
- `TODO` - Cần làm
- `IN_PROGRESS` - Đang làm
- `IN_REVIEW` - Đang review
- `DONE` - Hoàn thành
- `COMPLETED` - Đã hoàn thành (optional)
- `ON_HOLD` - Tạm dừng (optional)
- `CANCELLED` - Đã hủy (optional)

### Giá Trị Priority Hợp Lệ
- `LOW` - Thấp
- `MEDIUM` - Trung bình
- `HIGH` - Cao
- `URGENT` - Khẩn cấp

## Troubleshooting

### Lỗi: Collection not found
- Kiểm tra Collection ID phải chính xác là `workspace_tasks`
- Kiểm tra database ID trong AppwriteConfig.kt

### Lỗi: Missing required attribute
- Đảm bảo title, workspaceId, createdBy được điền khi tạo task
- Kiểm tra required attributes đã được đánh dấu đúng

### Task không xuất hiện
1. Kiểm tra permissions đã cấu hình đúng
2. Kiểm tra workspaceId có khớp với workspace hiện tại
3. Mở Appwrite Console để xem documents đã được tạo chưa
4. Check logs trong Android Studio (Logcat)

### Cải thiện Performance
- Các indexes đã tạo sẽ giúp truy vấn nhanh hơn
- Nếu có nhiều tasks, có thể thêm pagination
- Sử dụng workspaceId filter trong mọi query

## Hoàn Tất
Sau khi hoàn thành các bước trên, ứng dụng sẽ có hệ thống quản lý task workspace hoàn chỉnh, tách biệt hoàn toàn với task cá nhân!
