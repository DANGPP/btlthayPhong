# Hướng Dẫn Hoàn Tất Nâng Cấp Workspace

## ✅ Đã Hoàn Thành

1. ✅ **UserSelectionAdapter** - Adapter hiển thị danh sách users có checkbox
2. ✅ **Dialog mời thành viên nâng cấp** - Tìm kiếm và chọn nhiều users
3. ✅ **Search users** - Tìm kiếm theo email trong AppwriteRepository
4. ✅ **WorkspaceViewModel nâng cấp** - Thêm loadAvailableUsers(), searchUsers(), inviteMultipleUsers()
5. ✅ **WorkspaceDetailsFragment nâng cấp** - Dùng dialog mới
6. ✅ **WorkspaceMemberAdapter nâng cấp** - Thêm onClick để xem tasks của member
7. ✅ **MemberTasksFragment** - Fragment hiển thị tasks của từng member

---

## 🔧 Bước Tiếp Theo: Cập Nhật Navigation

Bạn cần thêm vào file `nav_graph.xml`:

```xml
<!-- Thêm fragment mới -->
<fragment
    android:id="@+id/memberTasksFragment"
    android:name="com.example.noteapp.ui.MemberTasksFragment"
    android:label="Member Tasks"
    tools:layout="@layout/fragment_member_tasks" >
    <argument
        android:name="workspace_id"
        app:argType="string" />
    <argument
        android:name="user_id"
        app:argType="string" />
    <argument
        android:name="user_name"
        app:argType="string" />
</fragment>

<!-- Thêm action trong workspaceDetailsFragment hoặc workspaceBoardFragment -->
<!-- Tìm fragment có id="@+id/workspaceDetailsFragment" và thêm action này bên trong: -->
<action
    android:id="@+id/action_workspaceDetails_to_memberTasks"
    app:destination="@id/memberTasksFragment" />
```

### Vị trí chính xác:
Mở file: `app/src/main/res/navigation/nav_graph.xml`

Tìm đoạn:
```xml
<fragment
    android:id="@+id/workspaceDetailsFragment"
    ...>
    <!-- Các action hiện tại -->
    <action ... />
    
    <!-- THÊM ACTION MỚI Ở ĐÂY -->
    <action
        android:id="@+id/action_workspaceDetails_to_memberTasks"
        app:destination="@id/memberTasksFragment" />
</fragment>
```

Và thêm fragment mới ở cuối file (trước tag `</navigation>`):
```xml
    <fragment
        android:id="@+id/memberTasksFragment"
        android:name="com.example.noteapp.ui.MemberTasksFragment"
        android:label="Member Tasks"
        tools:layout="@layout/fragment_member_tasks" >
        <argument
            android:name="workspace_id"
            app:argType="string" />
        <argument
            android:name="user_id"
            app:argType="string" />
        <argument
            android:name="user_name"
            app:argType="string" />
    </fragment>
</navigation>
```

---

## 📝 Tính Năng Mới

### 1. Mời Thành Viên - Nâng Cấp
**Trước:**
- Chỉ nhập email thủ công
- Phải biết chính xác email

**Sau:**
- ✅ Hiển thị danh sách tất cả users trong hệ thống
- ✅ Tìm kiếm users theo email (real-time)
- ✅ Chọn nhiều users cùng lúc (checkbox)
- ✅ Hiển thị số lượng đã chọn
- ✅ Gửi lời mời cho nhiều người cùng lúc

### 2. Xem Tasks Của Thành Viên
**Trước:**
- Chỉ xem tất cả tasks trong workspace

**Sau:**
- ✅ Click vào member trong danh sách
- ✅ Xem tất cả tasks do member đó tạo
- ✅ Board view riêng cho từng member
- ✅ Có thể update status tasks

---

## 🎯 Cách Sử Dụng

### Mời Thành Viên Mới:
1. Vào Workspace Details
2. Click "Mời Thành Viên"
3. **Tìm kiếm:** Nhập email vào ô search
4. **Chọn:** Click vào users muốn mời (có thể chọn nhiều)
5. **Vai trò:** Chọn Admin/Editor/Viewer
6. Click "Gửi Lời Mời"

### Xem Tasks Của Thành Viên:
1. Vào Workspace Details
2. Trong danh sách "Thành Viên", **click vào member bất kỳ**
3. Màn hình mới hiển thị tất cả tasks của member đó
4. Có thể xem theo board columns (Todo, In Progress, Review, Done)

---

## 🐛 Nếu Gặp Lỗi

### Lỗi: "Không tìm thấy người dùng"
**Nguyên nhân:** Collection `users` chưa có data
**Giải pháp:** 
- Đăng ký vài tài khoản test
- Mỗi user đăng ký sẽ tự động tạo document trong `users` collection

### Lỗi: "Unknown attribute: email"
**Nguyên nhân:** Collection `users` chưa có attribute `email` với index
**Giải pháp:**
1. Vào Appwrite Console → Database → Collection `users`
2. Vào tab **Indexes**
3. Tạo index mới:
   - Key: `idx_email`
   - Type: Fulltext
   - Attribute: `email`

### Lỗi Navigation
**Nguyên nhân:** Chưa thêm fragment vào nav_graph.xml
**Giải pháp:** Làm theo hướng dẫn ở trên

---

## ✨ Các File Đã Tạo

1. `UserSelectionAdapter.kt` - Adapter cho danh sách users
2. `item_user_selection.xml` - Layout cho mỗi user item
3. `dialog_invite_member_enhanced.xml` - Dialog mời thành viên nâng cấp
4. `MemberTasksFragment.kt` - Fragment xem tasks của member
5. `fragment_member_tasks.xml` - Layout cho member tasks
6. Đã update: `AppwriteRepository.kt`, `WorkspaceViewModel.kt`, `WorkspaceDetailsFragment.kt`, `WorkspaceMemberAdapter.kt`

---

## 🚀 Test Tính Năng

1. **Test mời thành viên:**
   - Tạo 2-3 tài khoản test
   - Login vào tài khoản 1
   - Tạo workspace
   - Mời tài khoản 2 và 3
   - Check search có hoạt động không

2. **Test xem tasks của member:**
   - Login tài khoản 2
   - Tạo vài tasks trong workspace
   - Login lại tài khoản 1 (owner)
   - Click vào member 2 trong danh sách
   - Kiểm tra hiển thị tasks của member 2

---

Sau khi thêm navigation vào `nav_graph.xml`, build lại project và test thử! 🎉
