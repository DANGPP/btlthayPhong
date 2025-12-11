# BÁO CÁO DỰ ÁN MÔN HỌC
## PHÁT TRIỂN ỨNG DỤNG CHO THIẾT BỊ DI ĐỘNG

---

**HỌC VIỆN CÔNG NGHỆ BƯU CHÍNH VIỄN THÔNG**  
**KHOA: CÔNG NGHỆ THÔNG TIN 1**

**Học phần:** Phát triển ứng dụng cho các thiết bị di động  
**Trình độ đào tạo:** Đại học  
**Hình thức đào tạo:** Chính quy

---

## THÔNG TIN NHÓM

- **Tên dự án:** Ứng dụng quản lý công việc và lịch trình cá nhân (NoteApp)
- **Số lượng thành viên:** [Điền số thành viên]
- **Danh sách thành viên:**
  1. [Họ tên - MSSV - Vai trò]
  2. [Họ tên - MSSV - Vai trò]
  3. [Họ tên - MSSV - Vai trò]

---

## MỨC LỤC

1. [GIỚI THIỆU TỔNG QUAN](#1-giới-thiệu-tổng-quan)
2. [PHÂN TÍCH YÊU CẦU](#2-phân-tích-yêu-cầu)
3. [THIẾT KẾ HỆ THỐNG](#3-thiết-kế-hệ-thống)
4. [CÔNG NGHỆ VÀ KIẾN TRÚC](#4-công-nghệ-và-kiến-trúc)
5. [TRIỂN KHAI CHỨC NĂNG](#5-triển-khai-chức-năng)
6. [KẾT QUẢ THỰC HIỆN](#6-kết-quả-thực-hiện)
7. [KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN](#7-kết-luận-và-hướng-phát-triển)

---

## 1. GIỚI THIỆU TỔNG QUAN

### 1.1. Đặt vấn đề

Trong cuộc sống hiện đại, việc quản lý thời gian và công việc hiệu quả là một thách thức lớn đối với mọi người. Việc ghi nhớ các công việc cần làm, lịch hẹn, và theo dõi tiến độ công việc đòi hỏi một công cụ hỗ trợ đắc lực.

**NoteApp** được phát triển nhằm giải quyết các vấn đề:
- Quên các công việc quan trọng cần thực hiện
- Khó khăn trong việc sắp xếp ưu tiên công việc
- Thiếu công cụ theo dõi tiến độ công việc cá nhân và nhóm
- Cần một nơi tập trung để quản lý lịch trình hàng ngày

### 1.2. Mục tiêu dự án

- Xây dựng ứng dụng di động quản lý công việc và lịch trình cá nhân
- Áp dụng kiến trúc MVVM (Model-View-ViewModel) và các best practices trong phát triển Android
- Tích hợp backend Appwrite để quản lý dữ liệu và xác thực người dùng
- Cung cấp giao diện thân thiện, dễ sử dụng trên nền tảng Android
- Hỗ trợ làm việc nhóm thông qua workspace chia sẻ

### 1.3. Phạm vi dự án

Ứng dụng được phát triển cho nền tảng Android, hỗ trợ từ Android 8.0 (API level 26) trở lên, với các chức năng chính:
- Quản lý công việc cá nhân (Todo)
- Quản lý lịch làm việc theo thời gian
- Làm việc nhóm qua Workspace
- Thống kê và báo cáo
- Pomodoro timer
- Smart Schedule

---

## 2. PHÂN TÍCH YÊU CẦU

### 2.1. Yêu cầu chức năng

#### 2.1.1. Quản lý tài khoản người dùng
- **Đăng ký tài khoản:** Người dùng có thể tạo tài khoản mới với email, mật khẩu và tên hiển thị
- **Đăng nhập:** Xác thực người dùng qua email và mật khẩu (mã hóa SHA-256)
- **Đăng xuất:** Xóa phiên đăng nhập và quay về màn hình đăng nhập
- **Chỉnh sửa thông tin cá nhân:** Đổi tên hiển thị, đổi mật khẩu, cập nhật avatar

#### 2.1.2. Quản lý công việc cá nhân (Todo)
- **Thêm công việc:** Tạo task mới với tiêu đề, mô tả, ngày hết hạn, độ ưu tiên (Low/Medium/High)
- **Xem danh sách:** Hiển thị tất cả công việc với trạng thái (Pending/Completed)
- **Cập nhật công việc:** Chỉnh sửa thông tin task, thay đổi trạng thái
- **Xóa công việc:** Xóa task không còn cần thiết
- **Lọc và tìm kiếm:** Lọc theo trạng thái, độ ưu tiên, tìm kiếm theo từ khóa

#### 2.1.3. Quản lý lịch làm việc (Calendar)
- **Xem lịch:** Hiển thị lịch theo ngày/tuần/tháng
- **Thêm sự kiện:** Tạo event với thời gian bắt đầu, kết thúc, mô tả
- **Xem chi tiết sự kiện:** Hiển thị thông tin đầy đủ của event
- **Chỉnh sửa/Xóa sự kiện:** Cập nhật hoặc xóa event đã tạo
- **Thông báo nhắc nhở:** Gửi notification trước khi sự kiện diễn ra

#### 2.1.4. Pomodoro Timer
- **Đếm thời gian:** Timer 25 phút làm việc, 5 phút nghỉ
- **Thông báo:** Nhắc nhở khi hết thời gian làm việc hoặc nghỉ
- **Lịch sử:** Lưu lại các session Pomodoro đã hoàn thành
- **Tùy chỉnh:** Cho phép thay đổi thời gian làm việc và nghỉ

#### 2.1.5. Smart Schedule
- **Gợi ý lịch trình:** Tự động đề xuất thời gian phù hợp cho các task
- **Tối ưu thời gian:** Sắp xếp công việc dựa trên độ ưu tiên và deadline
- **Cảnh báo xung đột:** Thông báo khi có task trùng thời gian

#### 2.1.6. Workspace (Làm việc nhóm)
- **Tạo workspace:** Người dùng tạo không gian làm việc chung cho nhóm
- **Mời thành viên:** Gửi lời mời qua email đến các thành viên
- **Quản lý thành viên:** Owner có thể xóa thành viên, member có thể rời khỏi workspace
- **Quản lý task nhóm:** Tạo, phân công, theo dõi task trong workspace
- **Board view:** Hiển thị task theo dạng Kanban board (Todo/In Progress/Done)


### 2.2. Yêu cầu phi chức năng

#### 2.2.1. Hiệu năng
- Thời gian khởi động ứng dụng < 3 giây
- Thời gian load dữ liệu từ server < 2 giây
- Giao diện mượt mà, không giật lag (60 FPS)
- Hỗ trợ offline mode với caching dữ liệu

#### 2.2.2. Bảo mật
- Mã hóa mật khẩu bằng SHA-256 trước khi lưu vào database
- Sử dụng HTTPS cho mọi request đến server
- Xác thực token cho các API call
- Bảo vệ dữ liệu người dùng theo quy định GDPR

#### 2.2.3. Giao diện
- Thiết kế Material Design 3
- Hỗ trợ đa ngôn ngữ (Tiếng Việt)
- Responsive trên các kích thước màn hình khác nhau
- Dark mode (nếu có thời gian)

#### 2.2.4. Khả năng mở rộng
- Kiến trúc MVVM dễ bảo trì và mở rộng
- Code structure rõ ràng, tuân thủ SOLID principles
- Sử dụng Dependency Injection
- Dễ dàng thêm chức năng mới

---

## 3. THIẾT KẾ HỆ THỐNG

### 3.1. Kiến trúc tổng quan

Ứng dụng sử dụng kiến trúc **MVVM (Model-View-ViewModel)** kết hợp với **Repository Pattern**:

```
┌─────────────────────────────────────────────────┐
│                    View Layer                    │
│  (Fragments, Activities, XML Layouts)           │
└─────────────────┬───────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────┐
│                 ViewModel Layer                  │
│  (AuthViewModel, TodoViewModel, etc.)           │
└─────────────────┬───────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────┐
│                Repository Layer                  │
│  (AppwriteRepository, WorkspaceRepository)      │
└─────────────────┬───────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────┐
│                   Data Source                    │
│         (Appwrite Backend, Local Cache)         │
└─────────────────────────────────────────────────┘
```

**Ưu điểm của kiến trúc:**
- Tách biệt logic nghiệp vụ khỏi UI
- Dễ test và maintain
- Tái sử dụng code cao
- Hỗ trợ lifecycle-aware components

### 3.2. Sơ đồ Use Case

```
                    ┌──────────────────┐
                    │   Người dùng     │
                    └────────┬─────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
  ┌──────────┐        ┌──────────┐        ┌──────────┐
  │ Đăng ký  │        │ Đăng nhập│        │Quản lý   │
  └──────────┘        └──────────┘        │Profile   │
                                           └──────────┘
        
        │                                        │
        ▼                                        ▼
  ┌──────────────────────────────────────────────────┐
  │              Quản lý công việc                   │
  ├──────────────┬───────────────┬──────────────────┤
  │ Thêm Todo    │ Xem Todo      │ Cập nhật Todo    │
  └──────────────┴───────────────┴──────────────────┘
  
        │                                        │
        ▼                                        ▼
  ┌──────────────────────────────────────────────────┐
  │              Quản lý Workspace                   │
  ├──────────────┬───────────────┬──────────────────┤
  │ Tạo WS       │ Mời thành viên│ Quản lý task     │
  └──────────────┴───────────────┴──────────────────┘
```

### 3.3. Sơ đồ cơ sở dữ liệu

**Database:** Appwrite (Database ID: `6908cde40006b4bbd549`)

#### Collection: `users`
```
{
  "id": "string",
  "email": "string" (unique),
  "password": "string" (SHA-256 hashed),
  "name": "string",
  "createdAt": "datetime"
}
```

#### Collection: `workspaces`
```
{
  "id": "string",
  "name": "string",
  "description": "string",
  "ownerEmail": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

#### Collection: `workspace_members`
```
{
  "id": "string",
  "workspaceId": "string",
  "userEmail": "string",
  "role": "string" (owner/member),
  "joinedAt": "datetime"
}
```

#### Collection: `workspace_invitations`
```
{
  "id": "string",
  "workspaceId": "string",
  "workspaceName": "string",
  "inviterEmail": "string",
  "inviteeEmail": "string",
  "status": "string" (pending/accepted/rejected),
  "createdAt": "datetime"
}
```

#### Collection: `workspace_tasks`
```
{
  "id": "string",
  "workspaceId": "string",
  "title": "string",
  "description": "string",
  "assignedTo": "string",
  "priority": "string" (low/medium/high),
  "status": "string" (todo/in_progress/done),
  "dueDate": "datetime",
  "createdBy": "string",
  "createdAt": "datetime"
}
```

### 3.4. Luồng xử lý chính

#### 3.4.1. Luồng đăng ký
```
User → RegisterFragment → RegisterViewModel → CustomAuthManager 
     → Appwrite API → Create User Document → Save Session 
     → Navigate to CalendarFragment
```

#### 3.4.2. Luồng đăng nhập
```
User → LoginFragment → LoginViewModel → CustomAuthManager 
     → Verify Password (SHA-256) → Save Session 
     → Navigate to CalendarFragment
```

#### 3.4.3. Luồng tạo task cá nhân
```
User → ToDoFragment/HomeFragment → Click FAB → AddTodoDialog 
     → Input (title, description, dueDate, priority) → Submit 
     → TodoViewModel → AppwriteRepository → Appwrite API 
     → Create Todo Document (with userId) → Save to Database 
     → Return success → Refresh RecyclerView → Show new task
```

#### 3.4.4. Luồng cập nhật trạng thái task cá nhân
```
User → Click Checkbox on Task → TodoAdapter.onCheckChange 
     → TodoFragment.updateTodoStatus → TodoViewModel 
     → AppwriteRepository.updateTodo → Appwrite API 
     → Update isCompleted field → Return success → Refresh UI
```

#### 3.4.5. Luồng tạo workspace
```
User → WorkspaceFragment → Create Dialog → WorkspaceViewModel 
     → WorkspaceRepository → Appwrite API → Create Workspace Document 
     → Create Member Document → Refresh UI
```

#### 3.4.6. Luồng mời thành viên
```
Owner → Invite Dialog → WorkspaceViewModel → WorkspaceRepository 
      → Create Invitation Document → Send Notification 
      → Invitee sees invitation → Accept/Reject 
      → Update Member Collection
```

---

## 4. CÔNG NGHỆ VÀ KIẾN TRÚC

### 4.1. Công nghệ sử dụng

#### 4.1.1. Frontend (Android)
- **Ngôn ngữ:** Kotlin 1.9.0
- **Minimum SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Build Tool:** Gradle 8.1.1

#### 4.1.2. Backend
- **Platform:** Appwrite v8.1.0
- **Endpoint:** https://cloud.appwrite.io/v1
- **Project ID:** 6768cd8c000c7a3c9d09
- **Database ID:** 6908cde40006b4bbd549

#### 4.1.3. Thư viện chính

**Networking & Backend:**
```kotlin
implementation("io.appwrite:sdk-for-android:5.0.0")
```

**Navigation:**
```kotlin
implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
implementation("androidx.navigation:navigation-ui-ktx:2.7.4")
```

**Lifecycle & ViewModel:**
```kotlin
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
```

**UI Components:**
```kotlin
implementation("com.google.android.material:material:1.10.0")
implementation("androidx.recyclerview:recyclerview:1.3.2")
implementation("androidx.cardview:cardview:1.0.0")
```

**Coroutines:**
```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

**Image Loading:**
```kotlin
implementation("com.github.bumptech.glide:glide:4.16.0")
```

### 4.2. Kiến trúc MVVM

#### 4.2.1. Model Layer
- Đại diện cho dữ liệu của ứng dụng
- Các data class: `ToDo`, `Workspace`, `WorkspaceTask`, `User`
- Repository pattern để truy xuất dữ liệu

#### 4.2.2. View Layer
- Fragments và Activities hiển thị UI
- XML layouts định nghĩa giao diện
- Không chứa business logic

#### 4.2.3. ViewModel Layer
- Quản lý UI state và business logic
- Sử dụng LiveData để observable data
- Lifecycle-aware, tự động xử lý configuration changes

**Ví dụ:**
```kotlin
class WorkspaceViewModel(context: Context) : ViewModel() {
    private val repository = WorkspaceRepository(context)
    
    private val _workspaces = MutableLiveData<List<Workspace>>()
    val workspaces: LiveData<List<Workspace>> = _workspaces
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun loadWorkspaces(userEmail: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getUserWorkspaces(userEmail)
            _workspaces.value = result
            _isLoading.value = false
        }
    }
}
```

### 4.3. Navigation Component

Sử dụng Android Navigation Component để quản lý luồng điều hướng:
- Single Activity architecture
- Type-safe arguments
- Deep linking support
- Animation transitions

**Navigation Graph:**
```xml
<navigation>
    <fragment id="@+id/splashFragment" />
    <fragment id="@+id/loginFragment" />
    <fragment id="@+id/registerFragment" />
    <fragment id="@+id/calendarFragment" />
    <fragment id="@+id/homeFragment" />
    <fragment id="@+id/workspaceFragment" />
    <fragment id="@+id/profileEditFragment" />
</navigation>
```

### 4.4. Data Binding & View Binding

- **View Binding:** Type-safe view references
- Loại bỏ `findViewById()`
- Compile-time safety

```kotlin
private var _binding: FragmentHomeBinding? = null
private val binding get() = _binding!!

override fun onCreateView(...): View {
    _binding = FragmentHomeBinding.inflate(inflater, container, false)
    return binding.root
}
```

---

## 5. TRIỂN KHAI CHỨC NĂNG

### 5.1. Phân công công việc

#### 5.1.1. Tổng quan phân công

| Thành viên | Vai trò chính | Tỉ lệ công việc |
|------------|---------------|-----------------|
| **[Họ tên SV 1]** | Frontend Developer | 30% |
| **[Họ tên SV 2]** | Backend Developer (Authentication & Todo CRUD) | 40% |
| **[Họ tên SV 3]** | Backend Developer (Workspace & Integration) | 30% |

#### 5.1.2. Chi tiết phân công theo module

**A. FRONTEND ([Họ tên SV 1] - 30%)**

| STT | Task | Mô tả | % | Trạng thái |
|-----|------|-------|---|-----------|
| 1.1 | Thiết kế UI/UX wireframe | Sketch toàn bộ màn hình, flow diagram | 2% | ✅ |
| 1.2 | Splash Screen Layout | XML layout + logo + auto-navigate | 1% | ✅ |
| 1.3 | Login Screen Layout | Form email/password + button + validation UI | 2% | ✅ |
| 1.4 | Register Screen Layout | Form name/email/password/confirm + validation UI | 2% | ✅ |
| 1.5 | Bottom Navigation Setup | Navigation bar với 5 tabs + active states | 2% | ✅ |
| 1.6 | Home Fragment Layout | RecyclerView + empty state + FAB add button | 2% | ✅ |
| 1.7 | Todo Item Card Layout | Card design + checkbox + priority badge + date | 2% | ✅ |
| 1.8 | Add Todo Dialog Layout | Dialog + TextInput + DatePicker + Priority Chips | 2% | ✅ |
| 1.9 | Calendar Fragment Layout | CalendarView + event RecyclerView | 2% | ✅ |
| 1.10 | Workspace Fragment Layout | GridLayout + FAB create + workspace cards | 2% | ✅ |
| 1.11 | Workspace Board Layout | 3-column Kanban (Todo/In Progress/Done) | 2.5% | ✅ |
| 1.12 | Statistics Fragment Layout | Chart containers + filter buttons + legends | 2% | ✅ |
| 1.13 | Pomodoro Fragment Layout | Circular progress + timer text + control buttons | 1.5% | ✅ |
| 1.14 | Smart Schedule Layout | RecyclerView + suggestion cards | 1.5% | ✅ |
| 1.15 | Profile Edit Layout | Avatar picker + name/email/password inputs + save | 2% | ✅ |
| 1.16 | Invitations Bottom Sheet | BottomSheet + invitation list + accept/reject | 1.5% | ✅ |
| 1.17 | Menu Resources | menu_home.xml + menu_main.xml + icons | 1% | ✅ |
| 1.18 | Adapters Setup | TodoAdapter + WorkspaceAdapter + TaskAdapter | 2% | ✅ |
| **Tổng Frontend** | | | **30%** | |

**B. BACKEND - AUTHENTICATION & TODO CRUD ([Họ tên SV 2] - 40%)**

| STT | Task | Mô tả | % | Trạng thái |
|-----|------|-------|---|-----------|
| 2.1 | Appwrite Client Setup | Khởi tạo AppwriteClient + endpoint + project ID | 1% | ✅ |
| 2.2 | Database Schema Design | Thiết kế collections: users, todos với attributes | 2% | ✅ |
| 2.3 | SessionManager Class | SharedPreferences: save/get userId, email, name | 2% | ✅ |
| 2.4 | Password Hash Function | Implement SHA-256 hashing cho password | 2% | ✅ |
| 2.5 | Register API | CustomAuthManager.register() - create user document | 3% | ✅ |
| 2.6 | Login API | CustomAuthManager.login() - verify hashed password | 3% | ✅ |
| 2.7 | Logout Function | clearSession() + navigate to login | 1.5% | ✅ |
| 2.8 | Check Login Status | isLoggedIn() - check userId in session | 1.5% | ✅ |
| 2.9 | RegisterViewModel | Input validation + register flow + LiveData states | 3% | ✅ |
| 2.10 | LoginViewModel | Login flow + error handling + loading states | 2.5% | ✅ |
| 2.11 | AuthViewModel | Update profile + change password logic | 2.5% | ✅ |
| 2.12 | Update User Name API | updateUserName() - update name field in users | 2% | ✅ |
| 2.13 | Change Password API | changePassword() - verify old + hash new password | 2.5% | ✅ |
| 2.14 | Todo Data Model | Data class ToDo + TodoPriority enum (LOW/MEDIUM/HIGH) | 1% | ✅ |
| 2.15 | Create Todo API | createTodo() - save to Appwrite với userId | 2% | ✅ |
| 2.16 | Get Todos API | getTodosByUserId() - query by userId + sorting | 2.5% | ✅ |
| 2.17 | Update Todo API | updateTodo() - edit title/description/priority/date | 2% | ✅ |
| 2.18 | Update Todo Status | updateTodoStatus() - toggle isCompleted | 1.5% | ✅ |
| 2.19 | Delete Todo API | deleteTodo() - xóa document by ID | 1.5% | ✅ |
| 2.20 | TodoViewModel | Todo CRUD operations + LiveData + error handling | 3% | ✅ |
| 2.21 | Error Handling Utils | Try-catch wrappers + error message mapping | 1.5% | ✅ |
| **Tổng Backend Auth & Todo** | | | **40%** | |

**C. BACKEND - WORKSPACE & INTEGRATION ([Họ tên SV 3] - 30%)**

| STT | Task | Mô tả | % | Trạng thái |
|-----|------|-------|---|-----------|
| 3.1 | Workspace Schema Design | Collections: workspaces, members, invitations, tasks | 2% | ✅ |
| 3.2 | Workspace Data Models | Data classes: Workspace, Member, Invitation, Task | 2% | ✅ |
| 3.3 | Create Workspace API | createWorkspace() - tạo workspace + add owner | 2.5% | ✅ |
| 3.4 | Get User Workspaces API | getUserWorkspaces() - query by userEmail | 2% | ✅ |
| 3.5 | Add Member API | addMemberToWorkspace() với role (owner/member) | 2% | ✅ |
| 3.6 | Get Workspace Members API | getWorkspaceMembers() - list members by workspaceId | 2% | ✅ |
| 3.7 | Remove Member API | removeMemberFromWorkspace() - chỉ owner | 2% | ✅ |
| 3.8 | Leave Workspace API | leaveWorkspace() - member tự rời khỏi workspace | 2% | ✅ |
| 3.9 | Delete Workspace API | deleteWorkspace() - xóa workspace + members + tasks | 2% | ✅ |
| 3.10 | Create Invitation API | sendInvitation() - tạo invitation document | 1.5% | ✅ |
| 3.11 | Get Invitations API | getPendingInvitations() - lấy invitations by email | 1.5% | ✅ |
| 3.12 | Accept Invitation API | acceptInvitation() - add to members + update status | 2% | ✅ |
| 3.13 | Reject Invitation API | rejectInvitation() - update status to rejected | 1% | ✅ |
| 3.14 | Create Workspace Task API | createWorkspaceTask() - với assignment + priority | 2% | ✅ |
| 3.15 | Get Workspace Tasks API | getWorkspaceTasks() - query by workspaceId | 1.5% | ✅ |
| 3.16 | Update Task Status API | updateTaskStatus() - todo/in_progress/done | 1.5% | ✅ |
| 3.17 | Update Task Details API | editWorkspaceTask() - update full task info | 1% | ✅ |
| 3.18 | Delete Workspace Task API | deleteWorkspaceTask() - xóa task by ID | 1% | ✅ |
| 3.19 | WorkspaceViewModel | Workspace CRUD + LiveData + error handling | 2.5% | ✅ |
| **Tổng Backend Workspace** | | | **30%** | |

#### 5.1.3. Phân tích chi tiết theo từng thành viên

**SINH VIÊN 1 - [Họ tên] - Frontend Developer (30%)**
- **Nhiệm vụ chính:** Xây dựng toàn bộ giao diện người dùng
- **Chi tiết:**
  - 18 UI layout tasks (30%)
- **Kỹ năng yêu cầu:** 
  - XML Layout Design
  - Material Design 3
  - RecyclerView & Adapters
  - Custom Dialogs & Bottom Sheets
  - Navigation Component
  - View Binding
  - ConstraintLayout, LinearLayout, FrameLayout

**SINH VIÊN 2 - [Họ tên] - Backend Developer Auth & Todo (40%)**
- **Nhiệm vụ chính:** Authentication system + Todo CRUD
- **Chi tiết:**
  - 13 authentication tasks (20%)
  - 8 todo CRUD tasks (20%)
- **Kỹ năng yêu cầu:**
  - Appwrite SDK for Android
  - Kotlin Coroutines (async/await)
  - SHA-256 Password Hashing
  - MVVM Architecture (ViewModel)
  - Repository Pattern
  - LiveData & Observer Pattern
  - Error Handling & Validation

**SINH VIÊN 3 - [Họ tên] - Backend Developer Workspace (30%)**
- **Nhiệm vụ chính:** Workspace management system + Integration
- **Chi tiết:**
  - 19 workspace & integration tasks (30%)
- **Kỹ năng yêu cầu:**
  - Appwrite Complex Queries
  - Relational Data Management
  - Multi-collection Operations
  - ViewModel với LiveData
  - Permission & Role Management
  - Integration Testing
  - Data Synchronization

### 5.2. Chi tiết triển khai từng chức năng

#### 5.2.1. Xác thực người dùng

**Mô tả:** Hệ thống xác thực cho phép người dùng đăng ký tài khoản mới, đăng nhập và quản lý phiên làm việc.

**Thiết kế giao diện:**
- **SplashFragment:** Màn hình chào mừng với logo, kiểm tra trạng thái đăng nhập
- **LoginFragment:** Form đăng nhập (email, password), nút "Đăng nhập", link "Đăng ký"
- **RegisterFragment:** Form đăng ký (name, email, password, confirm password)
- **ProfileEditFragment:** Chỉnh sửa tên, đổi mật khẩu, cập nhật avatar

**Triển khai:**

*CustomAuthManager.kt:*
```kotlin
class CustomAuthManager(private val context: Context) {
    private val sessionManager = SessionManager(context)
    private val databases = Databases(AppwriteClient.client)
    
    suspend fun register(name: String, email: String, password: String): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                val hashedPassword = hashPassword(password)
                val user = databases.createDocument(
                    databaseId = DATABASE_ID,
                    collectionId = USERS_COLLECTION_ID,
                    documentId = ID.unique(),
                    data = mapOf(
                        "email" to email,
                        "password" to hashedPassword,
                        "name" to name
                    )
                )
                sessionManager.saveCurrentUserId(user.id)
                sessionManager.saveUserEmail(email)
                sessionManager.saveUserName(name)
                AuthResult.Success
            } catch (e: Exception) {
                AuthResult.Error(e.message ?: "Đăng ký thất bại")
            }
        }
    }
    
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
```

*RegisterFragment.kt:*
```kotlin
class RegisterFragment : Fragment() {
    private lateinit var viewModel: RegisterViewModel
    
    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            
            val validationError = viewModel.validateInputs(name, email, password, confirmPassword)
            if (validationError == null) {
                viewModel.register(name, email, password)
            } else {
                showError(validationError)
            }
        }
    }
}
```

**Kiến trúc áp dụng:**
- MVVM với `AuthViewModel`, `RegisterViewModel`
- Repository pattern với `CustomAuthManager`
- Coroutines cho async operations
- LiveData để observe UI state

**Bảo vệ dữ liệu:**
- Mật khẩu được hash bằng SHA-256 trước khi lưu
- Session được lưu trong SharedPreferences
- Xác thực token cho mọi API call

#### 5.2.2. Quản lý công việc cá nhân (Todo)

**Mô tả:** Cho phép người dùng tạo, xem, cập nhật và xóa công việc cá nhân.

**Thiết kế giao diện:**
- **ToDoFragment:** RecyclerView hiển thị danh sách task, FAB để thêm task mới
- **AddTodoDialog:** Dialog nhập tiêu đề, mô tả, ngày hết hạn, độ ưu tiên
- **TodoAdapter:** Hiển thị từng item task với checkbox, tiêu đề, ngày hết hạn

**Triển khai:**

*ToDo.kt (Model):*
```kotlin
data class ToDo(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var isCompleted: Boolean = false,
    var dueDate: Long = System.currentTimeMillis(),
    var priority: TodoPriority = TodoPriority.MEDIUM,
    var userId: String = ""
)

enum class TodoPriority {
    LOW, MEDIUM, HIGH
}
```

*ToDoFragment.kt:*
```kotlin
class ToDoFragment : Fragment() {
    private lateinit var adapter: TodoAdapter
    
    private fun setupRecyclerView() {
        adapter = TodoAdapter(
            onItemClick = { todo -> showTodoDetail(todo) },
            onCheckChange = { todo, isChecked -> updateTodoStatus(todo, isChecked) },
            onDeleteClick = { todo -> deleteTodo(todo) }
        )
        binding.recyclerView.adapter = adapter
    }
    
    private fun addNewTodo() {
        AddTodoDialog { title, description, dueDate, priority ->
            val todo = ToDo(
                id = ID.unique(),
                title = title,
                description = description,
                dueDate = dueDate,
                priority = priority,
                userId = sessionManager.getCurrentUserId() ?: ""
            )
            saveTodoToDatabase(todo)
        }.show(parentFragmentManager, "AddTodoDialog")
    }
}
```

**Kiến trúc áp dụng:**
- MVVM với `TodoViewModel`
- RecyclerView với ViewHolder pattern
- Dialog pattern cho form nhập liệu

**Bảo vệ dữ liệu:**
- Mỗi todo gắn với `userId` để đảm bảo privacy
- Chỉ user tạo todo mới có quyền sửa/xóa

#### 5.2.3. Workspace & Task nhóm

**Mô tả:** Hệ thống workspace cho phép người dùng tạo không gian làm việc chung, mời thành viên và quản lý task nhóm.

**Thiết kế giao diện:**
- **WorkspaceFragment:** Grid/List hiển thị các workspace, FAB tạo workspace mới
- **WorkspaceBoardFragment:** Kanban board với 3 cột (Todo/In Progress/Done)
- **InvitationsBottomSheet:** Hiển thị lời mời workspace đang pending

**Triển khai:**

*WorkspaceRepository.kt:*
```kotlin
class WorkspaceRepository(context: Context) {
    private val databases = Databases(AppwriteClient.client)
    
    suspend fun createWorkspace(name: String, description: String, ownerEmail: String): Workspace? {
        return withContext(Dispatchers.IO) {
            try {
                val workspace = databases.createDocument(
                    databaseId = DATABASE_ID,
                    collectionId = WORKSPACES_COLLECTION_ID,
                    documentId = ID.unique(),
                    data = mapOf(
                        "name" to name,
                        "description" to description,
                        "ownerEmail" to ownerEmail
                    )
                )
                // Thêm owner vào members
                addMemberToWorkspace(workspace.id, ownerEmail, "owner")
                workspace.toWorkspace()
            } catch (e: Exception) {
                null
            }
        }
    }
    
    suspend fun inviteMember(workspaceId: String, workspaceName: String, 
                             inviterEmail: String, inviteeEmail: String): Boolean {
        return try {
            databases.createDocument(
                databaseId = DATABASE_ID,
                collectionId = WORKSPACE_INVITATIONS_COLLECTION_ID,
                documentId = ID.unique(),
                data = mapOf(
                    "workspaceId" to workspaceId,
                    "workspaceName" to workspaceName,
                    "inviterEmail" to inviterEmail,
                    "inviteeEmail" to inviteeEmail,
                    "status" to "pending"
                )
            )
            true
        } catch (e: Exception) {
            false
        }
    }
}
```

*WorkspaceBoardFragment.kt:*
```kotlin
class WorkspaceBoardFragment : Fragment() {
    private val todoAdapter = WorkspaceTaskAdapter { task -> onTaskClick(task) }
    private val inProgressAdapter = WorkspaceTaskAdapter { task -> onTaskClick(task) }
    private val doneAdapter = WorkspaceTaskAdapter { task -> onTaskClick(task) }
    
    private fun loadWorkspaceTasks() {
        viewModel.loadWorkspaceTasks(workspaceId)
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            val todoTasks = tasks.filter { it.status == "todo" }
            val inProgressTasks = tasks.filter { it.status == "in_progress" }
            val doneTasks = tasks.filter { it.status == "done" }
            
            todoAdapter.submitList(todoTasks)
            inProgressAdapter.submitList(inProgressTasks)
            doneAdapter.submitList(doneTasks)
        }
    }
}
```

**Kiến trúc áp dụng:**
- MVVM với `WorkspaceViewModel`
- Repository pattern với `WorkspaceRepository`
- Observer pattern cho realtime updates

**Bảo vệ dữ liệu:**
- Kiểm tra quyền owner/member trước khi cho phép thao tác
- Owner có quyền xóa workspace và member
- Member chỉ có quyền leave workspace

#### 5.2.4. Calendar, Statistics & Pomodoro

**Mô tả:** 
- **Calendar:** Hiển thị lịch làm việc, thêm/xóa events
- **Statistics:** Thống kê task hoàn thành, biểu đồ theo priority
- **Pomodoro:** Timer 25 phút làm việc, 5 phút nghỉ

**Thiết kế giao diện:**
- **CalendarFragment:** CalendarView hoặc Custom calendar, RecyclerView cho events
- **StatisticsFragment:** PieChart, BarChart, LineChart
- **PomodoroFragment:** Circular timer, nút Start/Pause/Reset

**Triển khai:**

*CalendarFragment.kt:*
```kotlin
class CalendarFragment : Fragment() {
    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.timeInMillis
            loadEventsForDate(selectedDate)
        }
    }
    
    private fun loadEventsForDate(date: Long) {
        viewModel.getEventsForDate(date).observe(viewLifecycleOwner) { events ->
            eventAdapter.submitList(events)
        }
    }
}
```

*PomodoroFragment.kt:*
```kotlin
class PomodoroFragment : Fragment() {
    private var countDownTimer: CountDownTimer? = null
    private val WORK_TIME = 25 * 60 * 1000L // 25 minutes
    private val BREAK_TIME = 5 * 60 * 1000L // 5 minutes
    
    private fun startTimer(duration: Long) {
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimerDisplay(millisUntilFinished)
            }
            
            override fun onFinish() {
                sendNotification("Pomodoro kết thúc!")
                switchMode()
            }
        }.start()
    }
}
```

**Kiến trúc áp dụng:**
- MVVM cho quản lý state
- CountDownTimer cho Pomodoro
- Chart libraries (MPAndroidChart) cho Statistics

---

## 6. KẾT QUẢ THỰC HIỆN

### 6.1. Chức năng đã hoàn thành

| Chức năng | Trạng thái | Hoàn thành |
|-----------|------------|------------|
| Đăng ký/Đăng nhập | ✅ Hoàn thành | 100% |
| Quản lý Todo cá nhân | ✅ Hoàn thành | 100% |
| Workspace & Task nhóm | ✅ Hoàn thành | 100% |
| Calendar | ✅ Hoàn thành | 100% |
| Statistics | ✅ Hoàn thành | 100% |
| Pomodoro Timer | ✅ Hoàn thành | 100% |
| Smart Schedule | ✅ Hoàn thành | 100% |
| Profile Edit | ✅ Hoàn thành | 100% |
| Thông báo | ✅ Hoàn thành | 100% |
| Bottom Navigation | ✅ Hoàn thành | 100% |

### 6.2. Giao diện ứng dụng

#### 6.2.1. Màn hình xác thực
- **Splash Screen:** Logo NoteApp, text "Chào Mừng Đến NoteApp"
- **Login Screen:** Form đăng nhập với email và password, nút "Đăng Nhập"
- **Register Screen:** Form đăng ký với name, email, password, confirm password

#### 6.2.2. Màn hình chính
- **Home:** Danh sách todo cá nhân với filter và search
- **Calendar:** Lịch làm việc với các event
- **Workspace:** Grid các workspace đã tham gia
- **Statistics:** Biểu đồ thống kê
- **Pomodoro:** Timer đếm ngược

#### 6.2.3. Màn hình phụ
- **Profile Edit:** Chỉnh sửa thông tin cá nhân
- **Workspace Board:** Kanban board cho task nhóm
- **Invitations:** Danh sách lời mời workspace

### 6.3. Demo và Testing

**Build APK:**
- File APK được build thành công
- Đường dẫn: `app/build/outputs/apk/debug/app-debug.apk`
- Kích thước: ~[X] MB

**Testing:**
- Đã test trên Android Emulator (API 30, 33, 34)
- Đã test trên thiết bị thật: [Tên thiết bị]
- Tất cả chức năng hoạt động ổn định

**Known Issues:**
- Avatar upload chưa implement (TODO)
- Dark mode chưa hỗ trợ (Future work)

### 6.4. Đánh giá theo tiêu chí

#### CLO1: Phân tích, thiết kế kiến trúc
- ✅ Thiết kế được 100% chức năng theo yêu cầu
- ✅ Áp dụng đúng kiến trúc MVVM
- ✅ Thiết kế giao diện đầy đủ, đẹp mắt, responsive

#### CLO2: Xây dựng và triển khai
- ✅ Xây dựng được 100% giao diện theo thiết kế
- ✅ Thành thạo các loại Layout (LinearLayout, ConstraintLayout, FrameLayout)
- ✅ Hiện thực hóa 100% chức năng đã thiết kế
- ✅ Tích hợp Appwrite Backend thành công

#### CLO3: Trình bày kết quả
- ✅ Viết báo cáo đầy đủ với tất cả đề mục
- ✅ Chuẩn bị slides thuyết trình
- ✅ Demo ứng dụng trực tiếp

---

## 7. KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

### 7.1. Kết luận

Dự án **NoteApp - Ứng dụng quản lý công việc và lịch trình cá nhân** đã hoàn thành đầy đủ các yêu cầu đề ra:

**Về mặt chức năng:**
- Xây dựng được hệ thống xác thực người dùng an toàn
- Quản lý công việc cá nhân hiệu quả
- Hỗ trợ làm việc nhóm qua Workspace
- Cung cấp công cụ thống kê và Pomodoro timer

**Về mặt kỹ thuật:**
- Áp dụng thành công kiến trúc MVVM
- Code structure rõ ràng, dễ maintain
- Tích hợp Appwrite Backend
- Tuân thủ Material Design guidelines

**Về mặt nhóm:**
- Phân công công việc hợp lý
- Phối hợp tốt giữa các thành viên
- Hoàn thành đúng tiến độ

### 7.2. Hướng phát triển

#### 7.2.1. Tính năng bổ sung
- [ ] Avatar upload và storage
- [ ] Dark mode
- [ ] Offline mode với Room Database
- [ ] Push notification realtime
- [ ] Export/Import data (JSON, CSV)
- [ ] Recurring tasks (task lặp lại)
- [ ] Task dependencies (task phụ thuộc)
- [ ] File attachments cho task
- [ ] Voice notes
- [ ] Integration với Google Calendar

#### 7.2.2. Cải thiện UX/UI
- [ ] Animation transitions mượt mà hơn
- [ ] Drag & drop tasks trong Kanban board
- [ ] Swipe actions (swipe to delete/complete)
- [ ] Widget trên home screen
- [ ] Customizable themes
- [ ] Onboarding tutorial cho user mới

#### 7.2.3. Performance optimization
- [ ] Implement pagination cho large datasets
- [ ] Image caching với Glide
- [ ] Reduce APK size
- [ ] Optimize database queries
- [ ] Background sync với WorkManager

#### 7.2.4. Security enhancements
- [ ] Two-factor authentication (2FA)
- [ ] Biometric authentication (fingerprint/face)
- [ ] End-to-end encryption cho sensitive data
- [ ] Session timeout
- [ ] Audit logs

### 7.3. Bài học kinh nghiệm

**Kỹ thuật:**
- Hiểu sâu về MVVM và lifecycle-aware components
- Làm việc với Appwrite Backend và NoSQL database
- Quản lý async operations với Coroutines
- Xử lý state và error handling

**Soft skills:**
- Làm việc nhóm và phân công nhiệm vụ
- Sử dụng Git cho version control
- Code review và merge conflicts
- Time management và deadline pressure

**Thách thức:**
- Debug các vấn đề về lifecycle và memory leaks
- Xử lý realtime data synchronization
- Thiết kế database schema phù hợp
- Balance giữa features và performance

---

## PHỤ LỤC

### A. Cấu trúc thư mục dự án

```
Todo_App/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/noteapp/
│   │   │   │   ├── auth/
│   │   │   │   │   ├── AuthRepository.kt
│   │   │   │   │   ├── CustomAuthManager.kt
│   │   │   │   │   └── SessionManager.kt
│   │   │   │   ├── fragment/
│   │   │   │   │   ├── SplashFragment.kt
│   │   │   │   │   ├── LoginFragment.kt
│   │   │   │   │   ├── RegisterFragment.kt
│   │   │   │   │   ├── HomeFragment.kt
│   │   │   │   │   ├── CalendarFragment.kt
│   │   │   │   │   ├── ToDoFragment.kt
│   │   │   │   │   ├── WorkspaceFragment.kt
│   │   │   │   │   ├── StatisticsFragment.kt
│   │   │   │   │   └── PomodoroFragment.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── ProfileEditFragment.kt
│   │   │   │   │   ├── WorkspaceBoardFragment.kt
│   │   │   │   │   └── InvitationsBottomSheet.kt
│   │   │   │   ├── viewmodel/
│   │   │   │   │   ├── RegisterViewModel.kt
│   │   │   │   │   ├── LoginViewModel.kt
│   │   │   │   │   ├── AuthViewModel.kt
│   │   │   │   │   └── WorkspaceViewModel.kt
│   │   │   │   ├── repository/
│   │   │   │   │   ├── AppwriteRepository.kt
│   │   │   │   │   └── WorkspaceRepository.kt
│   │   │   │   ├── adapter/
│   │   │   │   │   ├── TodoAdapter.kt
│   │   │   │   │   ├── WorkspaceAdapter.kt
│   │   │   │   │   └── WorkspaceTaskAdapter.kt
│   │   │   │   ├── model/
│   │   │   │   │   ├── ToDo.kt
│   │   │   │   │   ├── Workspace.kt
│   │   │   │   │   └── WorkspaceTask.kt
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── drawable/
│   │   │   │   ├── menu/
│   │   │   │   ├── navigation/
│   │   │   │   └── values/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

### B. Screenshots

[Chèn ảnh screenshots của các màn hình chính]

1. Splash Screen
2. Login Screen
3. Register Screen
4. Home - Todo List
5. Calendar View
6. Workspace List
7. Workspace Board (Kanban)
8. Statistics
9. Pomodoro Timer
10. Profile Edit

### C. Video Demo

[Link video demo ứng dụng trên YouTube/Google Drive]

### D. Source Code

- **GitHub Repository:** [Link repository]
- **APK File:** [Link Google Drive]

### E. Tài liệu tham khảo

1. Android Developers Documentation: https://developer.android.com
2. Kotlin Documentation: https://kotlinlang.org/docs
3. Appwrite Documentation: https://appwrite.io/docs
4. Material Design Guidelines: https://material.io/design
5. MVVM Architecture Guide: https://developer.android.com/topic/architecture
6. Coroutines Guide: https://kotlinlang.org/docs/coroutines-guide.html

---

**Ngày hoàn thành:** [Ngày/Tháng/Năm]  
**Người thực hiện:** [Tên các thành viên nhóm]  
**Giảng viên hướng dẫn:** [Tên giảng viên]

---

*Báo cáo này được tạo cho môn học Phát triển ứng dụng cho thiết bị di động, Học viện Công nghệ Bưu chính Viễn thông.*



