package com.example.noteapp.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.noteapp.R
import com.example.noteapp.databinding.FragmentBottomDialogBinding
import com.example.noteapp.model.ToDo
import com.example.noteapp.model.TodoPriority
import com.example.noteapp.model.TodoStatus
import com.example.noteapp.viewmodel.ToDoViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BottomDialogFragment(private val toDo: ToDo?) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentBottomDialogBinding
    private var selectedDueDate: String? = null
    private var selectedDueTime: String? = null
    private var selectedReminderTime: String? = null
    private var selectedPriority: TodoPriority = TodoPriority.MEDIUM
    private var selectedStatus: TodoStatus = TodoStatus.TODO

    private val toDoViewModel: ToDoViewModel by lazy {
        ViewModelProvider(requireActivity(), ToDoViewModel.ToDoViewModelFactory(requireContext()))[ToDoViewModel::class.java]
    }

    companion object {
        private const val TAG = "BottomDialogFragment"
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "BottomDialogFragment using ViewModel instance: ${toDoViewModel.hashCode()}")
        
        setupUI()
        setupSpinners()
        setupClickListeners()
        setupObservers()
        
        if (toDo != null) {
            populateFieldsForEdit()
        }
    }
    
    private fun setupUI() {
        if (toDo == null) {
            binding.txtTodoTitle.text = "New Task"
        } else {
            binding.txtTodoTitle.text = "Edit Task"
        }
    }
    
    private fun setupSpinners() {
        // Priority Spinner
        val priorityAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            TodoPriority.values().map { it.displayName }
        )
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = priorityAdapter
        
        binding.spinnerPriority.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPriority = TodoPriority.values()[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Status Spinner
        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            TodoStatus.values().map { it.displayName }
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = statusAdapter
        
        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedStatus = TodoStatus.values()[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSelectDueDate.setOnClickListener { showDatePicker() }
        binding.btnSelectDueTime.setOnClickListener { showTimePicker() }
        binding.btnSelectReminderTime.setOnClickListener { showReminderTimePicker() }
        binding.btnSaveTask.setOnClickListener { saveToDo() }
        binding.btnCancel.setOnClickListener { dismiss() }
        
        // Enable/disable reminder time button based on checkbox
        binding.checkboxReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.btnSelectReminderTime.isEnabled = isChecked
        }
    }
    
    private fun setupObservers() {
        // Observe errors and show toast
        toDoViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
                toDoViewModel.clearError()
            }
        }

        // Observe operation success
        toDoViewModel.operationSuccess.observe(viewLifecycleOwner) { success ->
            Log.d(TAG, "operationSuccess observer triggered with value: $success")
            if (success == true) {
                Log.d(TAG, "Operation successful, showing toast and dismissing dialog")
                Toast.makeText(context, "Todo saved successfully!", Toast.LENGTH_SHORT).show()
                toDoViewModel.clearOperationSuccess()
                dismiss()
            }
        }

        // Observe redirect to login
        toDoViewModel.shouldRedirectToLogin.observe(viewLifecycleOwner) { shouldRedirect ->
            if (shouldRedirect) {
                Toast.makeText(context, "Please login to continue", Toast.LENGTH_SHORT).show()
                toDoViewModel.clearRedirectToLogin()
                dismiss()
            }
        }
    }
    
    private fun populateFieldsForEdit() {
        toDo?.let { todo ->
            binding.edtTaskName.setText(todo.title)
            binding.edtDescription.setText(todo.description)
            binding.edtCategory.setText(todo.category)
            
            // Set priority spinner
            val priorityPosition = TodoPriority.values().indexOf(todo.priority)
            binding.spinnerPriority.setSelection(priorityPosition)
            
            // Set status spinner
            val statusPosition = TodoStatus.values().indexOf(todo.status)
            binding.spinnerStatus.setSelection(statusPosition)
            
            // Set due time if available
            todo.dueTime?.let { dueTime ->
                selectedDueDate = dueTime.split(" ").getOrNull(0)
                selectedDueTime = dueTime.split(" ").getOrNull(1)
                updateDueTimeDisplay()
            }
            
            // Set estimated duration
            if (todo.estimatedDuration > 0) {
                val hours = todo.estimatedDuration / 60
                val minutes = todo.estimatedDuration % 60
                binding.edtDurationHours.setText(hours.toString())
                binding.edtDurationMinutes.setText(minutes.toString())
            }
            
            // Set reminder
            todo.reminderTime?.let {
                binding.checkboxReminder.isChecked = true
                selectedReminderTime = it
            }
        }
    }


    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDueDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)
                updateDueTimeDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedDueTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                updateDueTimeDisplay()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }
    
    private fun showReminderTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedReminderTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                Toast.makeText(context, "Reminder set for $selectedReminderTime", Toast.LENGTH_SHORT).show()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }
    
    private fun updateDueTimeDisplay() {
        val dueTimeText = when {
            selectedDueDate != null && selectedDueTime != null -> "Due: $selectedDueDate $selectedDueTime"
            selectedDueDate != null -> "Due: $selectedDueDate"
            selectedDueTime != null -> "Due: $selectedDueTime"
            else -> null
        }
        
        if (dueTimeText != null) {
            binding.txtSelectedDueTime.text = dueTimeText
            binding.txtSelectedDueTime.visibility = View.VISIBLE
        } else {
            binding.txtSelectedDueTime.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun saveToDo() {
        Log.d(TAG, "saveToDo called")
        
        val taskName = binding.edtTaskName.text.toString().trim()
        if (taskName.isEmpty()) {
            Toast.makeText(context, "Please enter the task name", Toast.LENGTH_SHORT).show()
            return
        }
        
        val description = binding.edtDescription.text.toString().trim()
        val category = binding.edtCategory.text.toString().trim().ifEmpty { "General" }
        
        // Calculate estimated duration in minutes
        val hours = binding.edtDurationHours.text.toString().toIntOrNull() ?: 0
        val minutes = binding.edtDurationMinutes.text.toString().toIntOrNull() ?: 0
        val estimatedDuration = (hours * 60) + minutes
        
        // Combine due date and time
        val dueDateTime = when {
            selectedDueDate != null && selectedDueTime != null -> "$selectedDueDate $selectedDueTime"
            selectedDueDate != null -> selectedDueDate
            else -> null
        }
        
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val createdTime = sdf.format(Date())
        
        if (toDo == null) {
            // Creating new todo
            Log.d(TAG, "Creating new todo")
            val newToDo = ToDo(
                title = taskName,
                description = description,
                createdTime = createdTime,
                dueTime = dueDateTime,
                completedDate = null,
                userId = "", // Will be set by ViewModel
                status = selectedStatus,
                priority = selectedPriority,
                category = category,
                reminderTime = if (binding.checkboxReminder.isChecked) selectedReminderTime else null,
                estimatedDuration = estimatedDuration,
                actualDuration = 0
            )
            Log.d(TAG, "Calling toDoViewModel.insertTodo...")
            toDoViewModel.insertTodo(newToDo)
        } else {
            // Updating existing todo
            Log.d(TAG, "Updating existing todo: ${toDo.id}")
            val editToDo = toDo.copy(
                title = taskName,
                description = description,
                dueTime = dueDateTime,
                status = selectedStatus,
                priority = selectedPriority,
                category = category,
                reminderTime = if (binding.checkboxReminder.isChecked) selectedReminderTime else null,
                estimatedDuration = estimatedDuration
            )
            Log.d(TAG, "Updated todo object: $editToDo")
            toDoViewModel.updateTodo(editToDo)
        }
    }
}

