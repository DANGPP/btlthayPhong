package com.example.noteapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.databinding.FragmentMemberTasksBinding
import com.example.noteapp.model.BoardColumn
import com.example.noteapp.viewmodel.WorkspaceTaskViewModel
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment hiển thị tasks của 1 member cụ thể trong workspace
 */
class MemberTasksFragment : Fragment() {
    private var _binding: FragmentMemberTasksBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var taskViewModel: WorkspaceTaskViewModel
    private lateinit var boardAdapter: WorkspaceBoardAdapter
    
    private var workspaceId: String? = null
    private var userId: String? = null
    private var userName: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workspaceId = arguments?.getString("workspace_id")
        userId = arguments?.getString("user_id")
        userName = arguments?.getString("user_name")
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemberTasksBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val factory = WorkspaceTaskViewModel.WorkspaceTaskViewModelFactory(requireContext())
        taskViewModel = ViewModelProvider(this, factory)[WorkspaceTaskViewModel::class.java]
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        loadData()
    }
    
    private fun setupToolbar() {
        binding.toolbar.title = "Tasks của $userName"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupRecyclerView() {
        boardAdapter = WorkspaceBoardAdapter(
            onTaskClick = { task ->
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
            }
        )
        
        binding.recyclerViewMemberTasks.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = boardAdapter
        }
    }
    
    private fun setupObservers() {
        taskViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            if (tasks.isEmpty()) {
                binding.textEmptyTasks.visibility = View.VISIBLE
                binding.recyclerViewMemberTasks.visibility = View.GONE
            } else {
                binding.textEmptyTasks.visibility = View.GONE
                binding.recyclerViewMemberTasks.visibility = View.VISIBLE
                boardAdapter.updateTasks(tasks)
            }
            
            // Update count
            binding.textTaskCount.text = "Tổng: ${tasks.size} tasks"
        }
        
        taskViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        taskViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                taskViewModel.clearError()
            }
        }
        
        taskViewModel.operationSuccess.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                taskViewModel.clearOperationSuccess()
            }
        }
    }
    
    private fun loadData() {
        if (workspaceId != null && userId != null) {
            taskViewModel.loadMyTasks(workspaceId!!, userId!!)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
