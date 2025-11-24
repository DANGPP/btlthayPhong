# Todo App - Ứng Dụng Quản Lý Công Việc

Ứng dụng Android quản lý công việc (Todo) với tính năng đăng nhập, đăng ký và đồng bộ hóa dữ liệu qua Appwrite.

## ✨ Tính Năng

- ✅ **Đăng nhập / Đăng ký** với Appwrite Authentication
- ✅ **Quản lý Todo** (Tạo, Sửa, Xóa, Hoàn thành)
- ✅ **Phân loại theo Priority** (Low, Medium, High, Urgent)
- ✅ **Phân loại theo Status** (To Do, In Progress, Completed, On Hold, Cancelled)
- ✅ **Nhắc nhở** (Reminder)
- ✅ **Ước lượng thời gian** (Estimated Duration)
- ✅ **Ghi chú** (Notes)
- ✅ **Calendar View**
- ✅ **Pomodoro Timer**
- ✅ **AI Schedule** (Smart scheduling)
- ✅ **Thống kê** (Statistics)

## 🚀 Cài Đặt

### 1. Yêu Cầu
- Android Studio (phiên bản mới nhất)
- Android SDK 24 trở lên
- Tài khoản Appwrite (miễn phí tại [cloud.appwrite.io](https://cloud.appwrite.io))

### 2. Cấu Hình Appwrite

**Đọc file [APPWRITE_SETUP.md](APPWRITE_SETUP.md) để biết chi tiết**

Tóm tắt:
1. Tạo project trên Appwrite
2. Tạo database và 3 collections: `users`, `todos`, `notes`
3. Copy các IDs vào file `AppwriteConfig.kt`

### 3. Build và Chạy

```bash
# Clone repository
git clone <repository-url>

# Mở project trong Android Studio
# File -> Open -> Chọn folder Todo_App

# Sync Gradle
# Build -> Make Project

# Chạy app
# Run -> Run 'app'
```

## 📱 Hướng Dẫn Sử Dụng

### Đăng Ký Tài Khoản Mới

1. Mở app, màn hình Splash sẽ tự động chuyển sang màn hình Login
2. Click "Don't have an account? Sign Up"
3. Nhập thông tin:
   - **Full Name**: Tên đầy đủ (ít nhất 2 ký tự)
   - **Email**: Địa chỉ email hợp lệ
   - **Password**: Mật khẩu (ít nhất 8 ký tự, có chữ hoa, chữ thường và số)
   - **Confirm Password**: Nhập lại mật khẩu
4. Click "Sign Up"
5. Sau khi đăng ký thành công, bạn sẽ tự động đăng nhập và chuyển đến màn hình Home

### Đăng Nhập

1. Nhập email và password
2. Click "Sign In"
3. Nếu thành công, bạn sẽ được chuyển đến màn hình Home

### Tạo Todo Mới

1. Ở màn hình Home, chuyển sang tab "Todo"
2. Click nút FAB (nút tròn màu xanh ở góc dưới phải)
3. Chọn "Add Todo"
4. Nhập thông tin:
   - **Task Name**: Tên công việc (bắt buộc)
   - **Description**: Mô tả chi tiết
   - **Category**: Phân loại (VD: Work, Personal, Study)
   - **Priority**: Mức độ ưu tiên (Low, Medium, High, Urgent)
   - **Status**: Trạng thái (To Do, In Progress, ...)
   - **Due Date/Time**: Ngày và giờ hết hạn
   - **Estimated Duration**: Thời gian ước tính (giờ và phút)
   - **Reminder**: Bật để nhận nhắc nhở
5. Click "Save"

### Sửa Todo

1. Click vào todo cần sửa trong danh sách
2. Dialog sẽ hiện lên với thông tin hiện tại
3. Thay đổi thông tin cần thiết
4. Click "Save"

### Hoàn Thành Todo

1. Click vào checkbox bên trái của todo
2. Todo sẽ được đánh dấu là "Completed"

### Xóa Todo

1. Vuốt todo sang trái hoặc phải
2. Xác nhận xóa

### Sắp Xếp

1. Click menu 3 chấm ở góc trên phải
2. Chọn "Sort"
3. Danh sách sẽ sắp xếp theo thời gian tạo (tăng dần/giảm dần)

### Đăng Xuất

1. Click menu 3 chấm ở góc trên phải
2. Chọn "Logout"
3. Bạn sẽ được đăng xuất và quay về màn hình Login

## 🛠️ Công Nghệ Sử Dụng

- **Kotlin** - Ngôn ngữ lập trình
- **Android Jetpack** - Architecture Components
  - Navigation Component
  - LiveData & ViewModel
  - ViewBinding
- **Appwrite** - Backend as a Service
  - Authentication
  - Database (NoSQL)
- **Material Design** - UI Components
- **Coroutines** - Async programming

## 📁 Cấu Trúc Project

```
app/src/main/java/com/example/noteapp/
├── appwrite/          # Appwrite services
│   ├── AppwriteConfig.kt
│   ├── AuthService.kt
│   └── AppwriteRepository.kt
├── auth/              # Authentication logic
│   ├── AuthRepository.kt
│   └── SessionManager.kt
├── fragment/          # UI Fragments
│   ├── LoginFragment.kt
│   ├── RegisterFragment.kt
│   ├── HomeFragment.kt
│   ├── ToDoFragment.kt
│   └── BottomDialogFragment.kt
├── viewmodel/         # ViewModels
│   ├── LoginViewModel.kt
│   ├── RegisterViewModel.kt
│   └── ToDoViewModel.kt
├── model/             # Data models
│   ├── User.kt
│   ├── ToDo.kt
│   └── Note.kt
└── MainActivity.kt    # Main activity
```

## ❗ Lưu Ý Quan Trọng

1. **Appwrite Configuration**: Bạn PHẢI cấu hình Appwrite theo hướng dẫn trong `APPWRITE_SETUP.md` trước khi chạy app.

2. **Collection IDs**: Collection IDs trong `AppwriteConfig.kt` là chuỗi ID thực tế từ Appwrite console, KHÔNG phải tên collection.

3. **Permissions**: Đảm bảo cấu hình permissions đúng cho các collections trong Appwrite console.

4. **Internet Permission**: App cần kết nối internet để đồng bộ với Appwrite.

## 🐛 Troubleshooting

### App không đăng nhập được
- Kiểm tra kết nối internet
- Kiểm tra Project ID và Endpoint trong `AppwriteConfig.kt`
- Kiểm tra Auth settings trong Appwrite console

### Không tạo được todo
- Kiểm tra user đã đăng nhập chưa
- Kiểm tra Collection IDs trong `AppwriteConfig.kt`
- Kiểm tra permissions của todo collection
- Xem logs trong Logcat (filter: "AppwriteRepository", "ToDoViewModel")

### App crash khi mở
- Clean và rebuild project
- Kiểm tra dependencies trong `build.gradle.kts`
- Xem crash logs trong Logcat

## 📝 License

This project is for educational purposes.

## 👨‍💻 Author

Sinh viên lớp 2025 - Thầy Phong

## 📞 Support

Nếu gặp vấn đề, hãy:
1. Đọc kỹ file `APPWRITE_SETUP.md`
2. Kiểm tra logs trong Android Studio
3. Kiểm tra Appwrite console xem có lỗi gì không
