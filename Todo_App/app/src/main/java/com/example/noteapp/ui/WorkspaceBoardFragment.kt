package com.example.noteapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.adapter.MemberAvatarAdapter
import com.example.noteapp.databinding.FragmentWorkspaceBoardBinding
import com.example.noteapp.model.BoardColumn
import com.example.noteapp.model.WorkspaceTask
import com.example.noteapp.viewmodel.WorkspaceTaskViewModel
import com.example.noteapp.viewmodel.WorkspaceViewModel
import com.example.noteapp.auth.SessionManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class WorkspaceBoardFragment : Fragment() {
    private var _binding: FragmentWorkspaceBoardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var taskViewModel: WorkspaceTaskViewModel
    private lateinit var workspaceViewModel: WorkspaceViewModel
    private lateinit var boardAdapter: WorkspaceBoardAdapter
    private lateinit var memberAdapter: MemberAvatarAdapter
    private var workspaceId: String? = null
    private var selectedUserId: String? = null // null means "All"
    
    companion object {
        private const val ARG_WORKSPACE_ID = "workspace_id"
        
        fun newInstance(workspaceId: String): WorkspaceBoardFragment {
            val fragment = WorkspaceBoardFragment()
            val args = Bundle()
            args.putString(ARG_WORKSPACE_ID, workspaceId)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workspaceId = arguments?.getString(ARG_WORKSPACE_ID)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkspaceBoardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val taskFactory = WorkspaceTaskViewModel.WorkspaceTaskViewModelFactory(requireContext())
        taskViewModel = ViewModelProvider(this, taskFactory)[WorkspaceTaskViewModel::class.java]
        
        val workspaceFactory = WorkspaceViewModel.WorkspaceViewModelFactory(requireContext())
        workspaceViewModel = ViewModelProvider(this, workspaceFactory)[WorkspaceViewModel::class.java]
        
        setupToolbar()
        setupMembersRecyclerView()
        setupBoardRecyclerView()
        setupListeners()
        setupObservers()
        loadData()
    }
    
    private fun setupToolbar() {
        binding.toolbar.title = "Bảng Công Việc"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupMembersRecyclerView() {
        memberAdapter = MemberAvatarAdapter { member ->
            // If "All" is clicked, show all tasks
            if (member.userId == "all") {
                selectedUserId = null
                filterTasks()
                Snackbar.make(binding.root, "Hiển thị tất cả tasks", Snackbar.LENGTH_SHORT).show()
            } else {
                // Toggle filter: if already selected, show all; otherwise filter by this member
                selectedUserId = if (selectedUserId == member.userId) null else member.userId
                filterTasks()
                
                // Update UI to show selection
                val message = if (selectedUserId == null) 
                    "Hiển thị tất cả tasks" 
                else 
                    "Hiển thị tasks của ${member.userEmail}"
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            }
        }
        binding.recyclerViewMembers.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = memberAdapter
        }
    }
    
    private fun setupListeners() {
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }
        
        binding.buttonAddFirstTask.setOnClickListener {
            showAddTaskDialog()
        }
    }
    
    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(com.example.noteapp.R.layout.dialog_add_board_task, null)
        val titleInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.example.noteapp.R.id.editTextTaskTitle)
        val descInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.example.noteapp.R.id.editTextTaskDescription)
        val categoryInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.example.noteapp.R.id.editTextTaskCategory)
        val priorityGroup = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(com.example.noteapp.R.id.chipGroupPriority)
        val statusGroup = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(com.example.noteapp.R.id.chipGroupStatus)
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Thêm Công Việc Mới")
            .setView(dialogView)
            .setPositiveButton("Tạo", null)
            .setNegativeButton("Hủy", null)
            .create()
        
        dialog.show()
        
        // Override positive button to prevent auto-dismiss
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val title = titleInput.text.toString().trim()
            val description = descInput.text.toString().trim()
            val category = categoryInput.text.toString().trim().ifEmpty { "Chung" }
            
            // Validate title
            if (title.isEmpty()) {
                titleInput.error = "Vui lòng nhập tiêu đề"
                return@setOnClickListener
            }
            
            if (workspaceId == null) {
                Snackbar.make(binding.root, "Lỗi: Không tìm thấy workspace", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val priority = when (priorityGroup.checkedChipId) {
                com.example.noteapp.R.id.chipHigh -> com.example.noteapp.model.TodoPriority.HIGH
                com.example.noteapp.R.id.chipMedium -> com.example.noteapp.model.TodoPriority.MEDIUM
                com.example.noteapp.R.id.chipLow -> com.example.noteapp.model.TodoPriority.LOW
                else -> com.example.noteapp.model.TodoPriority.MEDIUM
            }
            
            val status = when (statusGroup.checkedChipId) {
                com.example.noteapp.R.id.chipStatusInProgress -> com.example.noteapp.model.TodoStatus.IN_PROGRESS
                com.example.noteapp.R.id.chipStatusInReview -> com.example.noteapp.model.TodoStatus.IN_REVIEW
                com.example.noteapp.R.id.chipStatusDone -> com.example.noteapp.model.TodoStatus.DONE
                else -> com.example.noteapp.model.TodoStatus.TODO
            }
            
            val sessionManager = SessionManager(requireContext())
            val userId = sessionManager.getCurrentUserId()
            
            if (userId.isNullOrEmpty()) {
                Snackbar.make(binding.root, "Lỗi: Bạn chưa đăng nhập", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val newTask = com.example.noteapp.model.WorkspaceTask(
                title = title,
                description = description,
                workspaceId = workspaceId!!,
                createdBy = userId,
                status = status,
                priority = priority,
                category = category,
                assignedTo = emptyList()
            )
            
            taskViewModel.createTask(newTask)
            dialog.dismiss()
        }
    }
    
    private fun loadData() {
        workspaceId?.let { 
            workspaceViewModel.selectWorkspace(it)
            taskViewModel.loadWorkspaceTasks(it)
        }
    }
    
    private fun setupBoardRecyclerView() {
        boardAdapter = WorkspaceBoardAdapter(
            onTaskClick = { task ->
                // Open task details dialog
                showTaskDetailDialog(task)
            },
            onStatusChange = { task, newColumn ->
                // Update task status
                val newStatus = when(newColumn) {
                    BoardColumn.TODO -> com.example.noteapp.model.TodoStatus.TODO
                    BoardColumn.IN_PROGRESS -> com.example.noteapp.model.TodoStatus.IN_PROGRESS
                    BoardColumn.IN_REVIEW -> com.example.noteapp.model.TodoStatus.IN_REVIEW
                    BoardColumn.DONE -> com.example.noteapp.model.TodoStatus.DONE
                }
                workspaceId?.let { wsId ->
                    taskViewModel.updateTaskStatus(task.id, wsId, newStatus)
                }
                Snackbar.make(binding.root, "Đã chuyển đến ${newColumn.displayName}", Snackbar.LENGTH_SHORT).show()
            }
        )
        
        binding.recyclerViewBoard.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = boardAdapter
        }
        
        // Enable drag and drop
        setupDragAndDrop()
    }
    
    private fun setupDragAndDrop() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // We handle this in BoardTaskAdapter
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Not used
            }
            
            override fun isLongPressDragEnabled(): Boolean = true
        })
        
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewBoard)
    }
    
    private fun setupObservers() {
        // Observe tasks
        taskViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            filterTasks()
        }
        
        // Observe loading state
        taskViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide loading indicator if needed
        }
        
        // Observe errors
        taskViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                taskViewModel.clearError()
            }
        }
        
        // Observe operation success
        taskViewModel.operationSuccess.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                taskViewModel.clearOperationSuccess()
            }
        }
        
        // Observe workspace members
        workspaceViewModel.members.observe(viewLifecycleOwner) { members ->
            // Add "All" member at the beginning
            val allMember = com.example.noteapp.model.WorkspaceMember(
                id = "all",
                workspaceId = workspaceId ?: "",
                userId = "all",
                userEmail = "All",
                role = com.example.noteapp.model.WorkspaceRole.VIEWER,
                joinedTime = ""
            )
            
            val membersWithAll = listOf(allMember) + members
            memberAdapter.submitList(membersWithAll)
            
            if (members.isEmpty()) {
                binding.cardMembers.visibility = View.GONE
            } else {
                binding.cardMembers.visibility = View.VISIBLE
            }
        }
        
        // Observe workspace details
        workspaceViewModel.currentWorkspace.observe(viewLifecycleOwner) { workspace ->
            workspace?.let {
                binding.toolbar.title = "${it.name} - Bảng"
            }
        }
    }
    
    private fun filterTasks() {
        val allTasks = taskViewModel.tasks.value ?: emptyList()
        val filteredTasks = if (selectedUserId == null) {
            allTasks // Show all tasks
        } else {
            allTasks.filter { task -> 
                task.assignedTo.contains(selectedUserId) || task.createdBy == selectedUserId
            }
        }
        
        boardAdapter.updateTasks(filteredTasks)
        
        if (filteredTasks.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.recyclerViewBoard.visibility = View.GONE
            binding.fabAddTask.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.recyclerViewBoard.visibility = View.VISIBLE
            binding.fabAddTask.visibility = View.VISIBLE
        }
    }
    
    private fun showTaskDetailDialog(task: WorkspaceTask) {
        val options = arrayOf("Xem chi tiết", "Sửa", "Xóa")
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(task.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showTaskInfo(task)
                    1 -> showEditTaskDialog(task)
                    2 -> confirmDeleteTask(task)
                }
            }
            .show()
    }
    
    private fun showTaskInfo(task: WorkspaceTask) {
        val createdDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(task.createdAt))
        
        val info = buildString {
            append("Tiêu đề: ${task.title}\n\n")
            append("Mô tả: ${task.description}\n\n")
            append("Danh mục: ${task.category}\n\n")
            append("Trạng thái: ${task.status.displayName}\n\n")
            append("Độ ưu tiên: ${task.priority.displayName}\n\n")
            append("Thời gian tạo: $createdDate")
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Chi tiết Task")
            .setMessage(info)
            .setPositiveButton("Đóng", null)
            .show()
    }
    
    private fun showEditTaskDialog(task: WorkspaceTask) {
        val dialogView = layoutInflater.inflate(com.example.noteapp.R.layout.dialog_add_board_task, null)
        val titleInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.example.noteapp.R.id.editTextTaskTitle)
        val descInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.example.noteapp.R.id.editTextTaskDescription)
        val categoryInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.example.noteapp.R.id.editTextTaskCategory)
        val priorityGroup = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(com.example.noteapp.R.id.chipGroupPriority)
        val statusGroup = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(com.example.noteapp.R.id.chipGroupStatus)
        
        // Pre-fill with existing values
        titleInput.setText(task.title)
        descInput.setText(task.description)
        categoryInput.setText(task.category)
        
        // Select current priority
        when (task.priority) {
            com.example.noteapp.model.TodoPriority.LOW -> priorityGroup.check(com.example.noteapp.R.id.chipLow)
            com.example.noteapp.model.TodoPriority.MEDIUM -> priorityGroup.check(com.example.noteapp.R.id.chipMedium)
            com.example.noteapp.model.TodoPriority.HIGH -> priorityGroup.check(com.example.noteapp.R.id.chipHigh)
        }
        
        // Select current status
        when (task.status) {
            com.example.noteapp.model.TodoStatus.TODO -> statusGroup.check(com.example.noteapp.R.id.chipStatusTodo)
            com.example.noteapp.model.TodoStatus.IN_PROGRESS -> statusGroup.check(com.example.noteapp.R.id.chipStatusInProgress)
            com.example.noteapp.model.TodoStatus.IN_REVIEW -> statusGroup.check(com.example.noteapp.R.id.chipStatusInReview)
            com.example.noteapp.model.TodoStatus.DONE -> statusGroup.check(com.example.noteapp.R.id.chipStatusDone)
            com.example.noteapp.model.TodoStatus.COMPLETED -> statusGroup.check(com.example.noteapp.R.id.chipStatusDone)
            com.example.noteapp.model.TodoStatus.CANCELLED -> statusGroup.check(com.example.noteapp.R.id.chipStatusTodo)
            com.example.noteapp.model.TodoStatus.ON_HOLD -> statusGroup.check(com.example.noteapp.R.id.chipStatusInProgress)
        }
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sửa Task")
            .setView(dialogView)
            .setPositiveButton("Lưu", null)
            .setNegativeButton("Hủy", null)
            .create()
        
        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = titleInput.text.toString().trim()
                val description = descInput.text.toString().trim()
                val category = categoryInput.text.toString().trim()
                
                if (title.isEmpty()) {
                    Snackbar.make(binding.root, "Vui lòng nhập tiêu đề", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                val priority = when (priorityGroup.checkedChipId) {
                    com.example.noteapp.R.id.chipLow -> com.example.noteapp.model.TodoPriority.LOW
                    com.example.noteapp.R.id.chipMedium -> com.example.noteapp.model.TodoPriority.MEDIUM
                    com.example.noteapp.R.id.chipHigh -> com.example.noteapp.model.TodoPriority.HIGH
                    else -> com.example.noteapp.model.TodoPriority.MEDIUM
                }
                val status = when (statusGroup.checkedChipId) {
                    com.example.noteapp.R.id.chipStatusTodo -> com.example.noteapp.model.TodoStatus.TODO
                    com.example.noteapp.R.id.chipStatusInProgress -> com.example.noteapp.model.TodoStatus.IN_PROGRESS
                    com.example.noteapp.R.id.chipStatusInReview -> com.example.noteapp.model.TodoStatus.IN_REVIEW
                    com.example.noteapp.R.id.chipStatusDone -> com.example.noteapp.model.TodoStatus.DONE
                    else -> com.example.noteapp.model.TodoStatus.TODO
                }
                
                val updatedTask = task.copy(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    status = status
                )
                taskViewModel.updateTask(updatedTask)
                
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }
    
    private fun confirmDeleteTask(task: WorkspaceTask) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xóa Task")
            .setMessage("Bạn có chắc chắn muốn xóa task \"${task.title}\"?")
            .setPositiveButton("Xóa") { _, _ ->
                workspaceId?.let { wsId ->
                    taskViewModel.deleteTask(task.id, wsId)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
