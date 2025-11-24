# ✅ CHECKLIST TRƯỚC KHI CHẠY APP

## 🔴 BẮT BUỘC - Làm theo thứ tự

### 1️⃣ Cấu Hình Appwrite Project

- [ ] Đăng nhập [Appwrite Console](https://cloud.appwrite.io)
- [ ] Tạo project mới (hoặc dùng project có sẵn)
- [ ] Copy **Project ID** (từ Settings)
- [ ] Bật **Email/Password Authentication** (Auth → Settings)

### 2️⃣ Tạo Database

- [ ] Vào **Databases** → Click **Create database**
- [ ] Đặt tên database (VD: `todo_app_db`)
- [ ] Copy **Database ID**

### 3️⃣ Tạo Collection: Users

- [ ] Click **Create collection** → Tên: `users`
- [ ] Copy **Collection ID** của users
- [ ] Thêm attributes:
  - [ ] `name` - String (255) - Required
  - [ ] `email` - String (255) - Required
  - [ ] `createdAt` - String (100)
  - [ ] `updatedAt` - String (100)
- [ ] Cấu hình **Permissions**:
  - [ ] Read: `Any`
  - [ ] Create: `Any`
  - [ ] Update: `Any`
  - [ ] Delete: `Any`

### 4️⃣ Tạo Collection: Todos

- [ ] Click **Create collection** → Tên: `todos`
- [ ] Copy **Collection ID** của todos
- [ ] Thêm attributes:
  - [ ] `title` - String (500) - Required
  - [ ] `description` - String (5000)
  - [ ] `createdTime` - String (100) - Required
  - [ ] `dueTime` - String (100)
  - [ ] `completedDate` - String (100)
  - [ ] `userId` - String (100) - Required
  - [ ] `status` - String (50) - Default: "to_do"
  - [ ] `priority` - String (50) - Default: "medium"
  - [ ] `category` - String (100) - Default: "General"
  - [ ] `reminderTime` - String (100)
  - [ ] `estimatedDuration` - Integer - Default: 0
  - [ ] `actualDuration` - Integer - Default: 0
- [ ] Tạo **Indexes**:
  - [ ] Index: `userId_index` → Attribute: `userId`
  - [ ] Index: `status_index` → Attribute: `status`
  - [ ] Index: `priority_index` → Attribute: `priority`
- [ ] Cấu hình **Permissions** (giống Users)

### 5️⃣ Tạo Collection: Notes

- [ ] Click **Create collection** → Tên: `notes`
- [ ] Copy **Collection ID** của notes
- [ ] Thêm attributes:
  - [ ] `title` - String (500) - Required
  - [ ] `description` - String (10000)
  - [ ] `createdTime` - String (100) - Required
  - [ ] `userId` - String (100) - Required
- [ ] Cấu hình **Permissions** (giống Users)

### 6️⃣ Cập Nhật Code

Mở file `app/src/main/java/com/example/noteapp/appwrite/AppwriteConfig.kt`

```kotlin
object AppwriteConfig {
    const val PROJECT_ID = "PASTE_YOUR_PROJECT_ID_HERE"
    const val DATABASE_ID = "PASTE_YOUR_DATABASE_ID_HERE"
    const val USER_COLLECTION_ID = "PASTE_YOUR_USERS_COLLECTION_ID_HERE"
    const val NOTE_COLLECTION_ID = "PASTE_YOUR_NOTES_COLLECTION_ID_HERE"
    const val TODO_COLLECTION_ID = "PASTE_YOUR_TODOS_COLLECTION_ID_HERE"
}
```

- [ ] Thay `PROJECT_ID` bằng ID bạn đã copy
- [ ] Thay `DATABASE_ID` bằng ID bạn đã copy
- [ ] Thay `USER_COLLECTION_ID` bằng ID của users collection
- [ ] Thay `NOTE_COLLECTION_ID` bằng ID của notes collection
- [ ] Thay `TODO_COLLECTION_ID` bằng ID của todos collection

### 7️⃣ Build và Run

- [ ] Mở project trong Android Studio
- [ ] Click **File** → **Sync Project with Gradle Files**
- [ ] Click **Build** → **Make Project**
- [ ] Đảm bảo không có lỗi compile
- [ ] Click **Run** → **Run 'app'**

### 8️⃣ Test App

- [ ] App khởi động thành công
- [ ] Màn hình Splash hiển thị
- [ ] Chuyển sang Login hoặc Home
- [ ] **Test Đăng Ký**:
  - [ ] Click "Sign Up"
  - [ ] Nhập thông tin đầy đủ
  - [ ] Click "Sign Up"
  - [ ] Tự động chuyển sang Home
- [ ] **Test Đăng Nhập**:
  - [ ] Logout ra
  - [ ] Login lại với tài khoản vừa tạo
  - [ ] Thành công vào Home
- [ ] **Test Tạo Todo**:
  - [ ] Click FAB → "Add Todo"
  - [ ] Nhập thông tin
  - [ ] Click "Save"
  - [ ] Todo xuất hiện trong danh sách
  - [ ] Vào Appwrite Console kiểm tra data

## 🟢 TÙY CHỌN - Nâng Cao

### Security (cho Production)

- [ ] Tắt email verification nếu chỉ test
- [ ] Cấu hình permissions theo user ID thay vì Any
- [ ] Thêm rate limiting
- [ ] Cấu hình CORS nếu cần

### Performance

- [ ] Thêm caching cho queries
- [ ] Optimize database indexes
- [ ] Monitor API usage trong Appwrite Console

## 🚨 Nếu Gặp Lỗi

### ❌ Lỗi 404 - Not Found
→ Collection ID sai, kiểm tra lại AppwriteConfig.kt

### ❌ Lỗi 401/403 - Unauthorized
→ Permissions chưa đúng, set All permissions = Any

### ❌ Lỗi Network
→ Kiểm tra internet, kiểm tra ENDPOINT url

### ❌ App crash
→ Xem Logcat với filter: "Appwrite", "ToDoViewModel", "AuthService"

---

## 📝 Quick Copy Template

Để tiện, bạn có thể copy template này vào notepad và điền IDs:

```
PROJECT_ID: _______________________________
DATABASE_ID: _______________________________
USER_COLLECTION_ID: _______________________________
TODO_COLLECTION_ID: _______________________________
NOTE_COLLECTION_ID: _______________________________
```

---

**✅ Hoàn thành checklist → App sẵn sàng sử dụng!**
