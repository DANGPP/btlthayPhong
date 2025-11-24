# 🚀 HƯỚNG DẪN NHANH - BẮT ĐẦU TRONG 5 PHÚT

## Bước 1: Cấu Hình Appwrite (3 phút)

1. Vào https://cloud.appwrite.io
2. Tạo Project → Copy **Project ID**
3. Tạo Database → Copy **Database ID**
4. Tạo 3 Collections:
   - **users** (có attributes: name, email, createdAt, updatedAt)
   - **todos** (có attributes: title, description, createdTime, dueTime, completedDate, userId, status, priority, category, reminderTime, estimatedDuration, actualDuration)
   - **notes** (có attributes: title, description, createdTime, userId)
5. Copy **Collection ID** của mỗi collection
6. Set Permissions = `Any` cho tất cả (Read, Create, Update, Delete)

📖 **Chi tiết**: Xem file [CHECKLIST.md](CHECKLIST.md) hoặc [APPWRITE_SETUP.md](APPWRITE_SETUP.md)

## Bước 2: Cập Nhật Code (1 phút)

Mở file: `app/src/main/java/com/example/noteapp/appwrite/AppwriteConfig.kt`

Thay đổi:
```kotlin
const val PROJECT_ID = "PASTE_YOUR_PROJECT_ID"
const val DATABASE_ID = "PASTE_YOUR_DATABASE_ID"
const val USER_COLLECTION_ID = "PASTE_USERS_COLLECTION_ID"
const val TODO_COLLECTION_ID = "PASTE_TODOS_COLLECTION_ID"
const val NOTE_COLLECTION_ID = "PASTE_NOTES_COLLECTION_ID"
```

## Bước 3: Chạy App (1 phút)

```
1. Android Studio → Sync Project
2. Build → Make Project
3. Run → Run 'app'
```

## Test Nhanh

1. **Đăng ký**: Sign Up → Nhập thông tin → Thành công!
2. **Đăng nhập**: Login với tài khoản vừa tạo → Vào Home
3. **Tạo Todo**: FAB → Add Todo → Nhập task → Save → Hiển thị!

---

## ✅ Xong! App đã sẵn sàng

**Gặp lỗi?** → Đọc [CHANGELOG.md](CHANGELOG.md) phần "Troubleshooting"

**Cần hướng dẫn chi tiết?** → Đọc [README.md](README.md)

---

## 🎯 Các Tính Năng Chính

- ✅ Đăng nhập / Đăng ký
- ✅ Tạo, sửa, xóa Todo
- ✅ Priority (Low, Medium, High, Urgent)
- ✅ Status (To Do, In Progress, Completed, ...)
- ✅ Reminder & Due Time
- ✅ Calendar View
- ✅ Pomodoro Timer
- ✅ AI Schedule
- ✅ Statistics

---

**Made with ❤️ by Sinh viên lớp 2025**
