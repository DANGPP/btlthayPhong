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
        memberAdapter = MemberAvatarAdapter()
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
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Thêm Công Việc Mới")
            .setView(dialogView)
            .setPositiveButton("Tạo") { _, _ ->
                val title = titleInput.text.toString().trim()
                val description = descInput.text.toString().trim()
                val category = categoryInput.text.toString().trim().ifEmpty { "Chung" }
                
                val priority = when (priorityGroup.checkedChipId) {
                    com.example.noteapp.R.id.chipUrgent -> com.example.noteapp.model.TodoPriority.URGENT
                    com.example.noteapp.R.id.chipHigh -> com.example.noteapp.model.TodoPriority.HIGH
                    com.example.noteapp.R.id.chipMedium -> com.example.noteapp.model.TodoPriority.MEDIUM
                    else -> com.example.noteapp.model.TodoPriority.LOW
                }
                
                val status = when (statusGroup.checkedChipId) {
                    com.example.noteapp.R.id.chipStatusInProgress -> com.example.noteapp.model.TodoStatus.IN_PROGRESS
                    com.example.noteapp.R.id.chipStatusInReview -> com.example.noteapp.model.TodoStatus.IN_REVIEW
                    com.example.noteapp.R.id.chipStatusDone -> com.example.noteapp.model.TodoStatus.DONE
                    else -> com.example.noteapp.model.TodoStatus.TODO
                }
                
                if (title.isNotEmpty() && workspaceId != null) {
                    val sessionManager = SessionManager(requireContext())
                    val userId = sessionManager.getCurrentUserId()
                    
                    val newTask = com.example.noteapp.model.WorkspaceTask(
                        title = title,
                        description = description,
                        workspaceId = workspaceId!!,
                        createdBy = userId ?: "",
                        status = status,
                        priority = priority,
                        category = category,
                        assignedTo = emptyList()
                    )
                    
                    taskViewModel.createTask(newTask)
                } else {
                    Snackbar.make(binding.root, "Vui lòng nhập tiêu đề", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
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
                // Open task details
                Snackbar.make(binding.root, "Task: ${task.title}", Snackbar.LENGTH_SHORT).show()
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
            boardAdapter.updateTasks(tasks)
            
            if (tasks.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerViewBoard.visibility = View.GONE
                binding.fabAddTask.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerViewBoard.visibility = View.VISIBLE
                binding.fabAddTask.visibility = View.VISIBLE
            }
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
            memberAdapter.submitList(members)
            
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
