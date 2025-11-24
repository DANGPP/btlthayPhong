# Hướng Dẫn Sử Dụng Tính Năng Workspace (Chia Sẻ Công Việc)

## Tổng Quan
Tính năng Workspace cho phép bạn chia sẻ và quản lý công việc theo nhóm, giống như Jira. Mỗi workspace có 3 loại quyền:

- **ADMIN** 🔴: Quyền cao nhất
  - Tạo/sửa/xóa tasks
  - Mời và xóa members
  - Chỉnh sửa thông tin workspace
  - Xóa workspace
  
- **EDITOR** 🟡: Quyền chỉnh sửa
  - Tạo/sửa/xóa tasks
  - Xem danh sách members
  - Không được mời/xóa members
  
- **VIEWER** 🟢: Chỉ xem
  - Xem tasks
  - Xem members
  - Không được tạo/sửa/xóa

## 1. Tạo Workspace Mới

1. Mở app → Menu (⋮) → **Workspaces**
2. Click nút **+** (FAB) ở góc dưới bên phải
3. Nhập thông tin:
   - **Workspace Name**: Tên dự án/nhóm (bắt buộc)
   - **Description**: Mô tả ngắn (tùy chọn)
4. Click **Create**
5. Bạn sẽ tự động trở thành **ADMIN** của workspace này

## 2. Mời Thành Viên

### Gửi lời mời
1. Vào **Workspaces** → Click vào workspace
2. Click **Invite Member**
3. Nhập:
   - **Email Address**: Email **chính xác** mà người đó đã đăng ký (VD: user@example.com)
   - **Select Role**: Chọn quyền (Admin/Editor/Viewer)
4. Click **Send Invitation**
5. Thông báo "Invitation sent to [email]" xuất hiện

⚠️ **LƯU Ý QUAN TRỌNG**:
- Email phải **khớp 100%** với email người dùng đã dùng để đăng ký
- Không có danh sách gợi ý - phải nhập thủ công
- Người được mời **không nhận notification push**, phải vào app để xem

### Chấp nhận lời mời
1. Người được mời mở app
2. Vào **Menu (⋮)** ở góc trên bên phải
3. Chọn **Workspaces** 
4. Màn hình Workspaces sẽ hiển thị thông báo nếu có invitation pending
5. Hoặc vào **Menu (⋮) → Invitations** để xem chi tiết
6. Click **Accept** để tham gia hoặc **Decline** để từ chối
7. Sau khi Accept, workspace sẽ hiện trong danh sách **Workspaces** của bạn

## 3. Quản Lý Thành Viên (Chỉ ADMIN)

### Xem danh sách members
1. Vào **Workspaces** → Click vào workspace
2. Cuộn xuống phần **Members**
3. Xem thông tin:
   - Email
   - Role (với màu sắc tương ứng)
   - Ngày tham gia

### Thay đổi role
1. Trong danh sách Members
2. Click icon **✏️ (Edit)** bên cạnh member
3. Chọn role mới trong dialog
4. Thay đổi có hiệu lực ngay lập tức

### Xóa thành viên
1. Trong danh sách Members
2. Click icon **🗑️ (Delete)** bên cạnh member
3. Xác nhận xóa
4. Member sẽ mất quyền truy cập vào workspace

## 4. Tạo Task Trong Workspace

### Cách 1: Từ Calendar
1. Vào tab **Calendar** 
2. Click nút **+** (FAB)
3. Điền thông tin task
4. **Quan trọng**: Chọn **Workspace** từ dropdown
5. (Tùy chọn) Chọn **Assign To** để giao việc cho member
6. Click **Add Todo**

### Cách 2: Từ Todo List
1. Vào tab **Todo**
2. Click **Add Todo** hoặc nút **+**
3. Điền thông tin và chọn workspace
4. Click **Save**

## 5. Xem Task Trong Workspace

### Lọc theo workspace
1. Vào tab **Todo** hoặc **Calendar**
2. Click icon **Filter** (🔍)
3. Chọn workspace muốn xem
4. Danh sách sẽ chỉ hiển thị tasks của workspace đó

### Xem tất cả tasks
1. Trong filter, chọn **All Workspaces** hoặc **My Tasks**
2. **My Tasks**: Chỉ tasks cá nhân (không có workspace)
3. **All Workspaces**: Tất cả tasks bạn có quyền xem

## 6. Chỉnh Sửa Workspace (Chỉ ADMIN)

1. Vào **Workspaces** → Click vào workspace
2. Click **Edit**
3. Thay đổi:
   - Workspace Name
   - Description
4. Click **Save**

## 7. Xóa Workspace (Chỉ ADMIN)

⚠️ **Cảnh báo**: Hành động này KHÔNG thể hoàn tác!

1. Vào **Workspaces**
2. Click icon **🗑️ (Delete)** bên cạnh workspace
3. Xác nhận xóa
4. Tất cả tasks trong workspace sẽ bị xóa
5. Tất cả members sẽ mất quyền truy cập

## 8. Tips & Tricks

### Tổ chức hiệu quả
- Tạo workspace riêng cho từng dự án
- Đặt tên workspace rõ ràng (VD: "Dự án Website 2025")
- Viết description để members hiểu rõ mục đích

### Phân quyền thông minh
- **ADMIN**: Chỉ cho Project Manager/Team Lead
- **EDITOR**: Cho members chính trong team
- **VIEWER**: Cho stakeholders/khách hàng muốn theo dõi tiến độ

### Quản lý tasks
- Luôn chọn workspace khi tạo task chung
- Sử dụng **Assign To** để phân công rõ ràng
- Tasks cá nhân không cần workspace

### Bảo mật
- Chỉ mời email tin cậy
- Thường xuyên review danh sách members
- Hạ quyền member không còn active xuống VIEWER
- Xóa members rời khỏi dự án

## 9. Troubleshooting

### Không thấy Invitations
- Kiểm tra email đăng ký có đúng không
- Refresh lại màn hình Invitations (Menu → Invitations)
- Yêu cầu người mời gửi lại invitation

### Không tạo được task trong workspace
- Kiểm tra role của bạn (phải là EDITOR hoặc ADMIN)
- VIEWER chỉ được xem, không được tạo/sửa

### Không thấy workspace sau khi Accept
- Quay lại màn hình Workspaces
- Pull to refresh
- Đăng xuất và đăng nhập lại

### Không mời được member
- Kiểm tra role của bạn (phải là ADMIN)
- Kiểm tra email có đúng format không
- Member phải có tài khoản trong app

## 10. Quy Trình Làm Việc Đề Xuất

### Setup ban đầu
1. ADMIN tạo workspace cho dự án
2. ADMIN mời tất cả members với role phù hợp
3. Members Accept invitations
4. ADMIN tạo các tasks ban đầu

### Làm việc hàng ngày
1. EDITOR/ADMIN tạo tasks mới
2. Assign tasks cho members cụ thể
3. Members cập nhật status của tasks
4. ADMIN theo dõi tiến độ chung

### Review định kỳ
1. ADMIN review danh sách tasks
2. Check members còn active không
3. Xóa/hạ quyền members không còn cần thiết
4. Cập nhật workspace description nếu cần

---

**Lưu ý**: Tính năng này yêu cầu kết nối Internet. Mọi thay đổi đều được sync real-time với Appwrite.
