package com.example.noteapp.fragment

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.auth.AuthRepositoryImpl
import com.example.noteapp.auth.SessionManager
import kotlinx.coroutines.launch
import com.example.noteapp.R
import com.example.noteapp.adapter.CalendarTodoAdapter
import com.example.noteapp.adapter.TimeSlotAdapter
import com.example.noteapp.adapter.DayTaskAdapter
import com.example.noteapp.adapter.TimelineScheduleAdapter
import com.example.noteapp.databinding.FragmentCalendarBinding
import com.example.noteapp.model.ToDo
import com.example.noteapp.model.TodoStatus
import com.example.noteapp.model.TimeSlot
import com.example.noteapp.viewmodel.CalendarViewModel
import com.example.noteapp.viewmodel.WorkspaceViewModel
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    private lateinit var binding: FragmentCalendarBinding
    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var timeSlotAdapter: TimeSlotAdapter
    private lateinit var allDayTasksAdapter: CalendarTodoAdapter
    private lateinit var dayTaskAdapter: DayTaskAdapter
    private lateinit var timelineScheduleAdapter: TimelineScheduleAdapter
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    enum class ViewMode {
        DAY, WEEK
    }

    private var currentViewMode = ViewMode.DAY

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val monthYearFormatter = SimpleDateFormat("MMMM, dd", Locale.getDefault())
    private val dayMonthFormatter = SimpleDateFormat("MMMM, dd", Locale.getDefault())

    private var currentWeekStart: Calendar = Calendar.getInstance().apply {
        // Set to today first
        time = Calendar.getInstance().time
        // Then set to the start of the week (Sunday)
        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    }
    private var allTimeSlots: List<TimeSlot> = TimeSlot.generateTimeSlots()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ensure we start with today's date and week
        selectedDate = Calendar.getInstance()
        currentWeekStart = getWeekStart(selectedDate)

        setupUserName()
        setupViewModel()
        setupClickListeners()
        setupRecyclerView()
        observeData()
        updateUI()

        // Load initial data for today
        loadTodosForDate(selectedDate)
    }

    private fun setupUserName() {
        val sessionManager = SessionManager(requireContext())
        val userName = sessionManager.getUserName() ?: "User"
        binding.textUserName.text = userName
    }

    private fun setupViewModel() {
        calendarViewModel = ViewModelProvider(
            this,
            CalendarViewModel.CalendarViewModelFactory(requireContext())
        )[CalendarViewModel::class.java]
    }

    private fun setupClickListeners() {
        // View mode toggle buttons

        // Week navigation buttons
        binding.btnPreviousWeek.setOnClickListener { navigateWeek(-1) }
        binding.btnNextWeek.setOnClickListener { navigateWeek(1) }

        binding.btnWeekView.setOnClickListener {
            setViewMode(ViewMode.WEEK)
        }

        binding.btnDayView.setOnClickListener {
            setViewMode(ViewMode.DAY)
        }

        binding.fabAddTask.setOnClickListener {
            com.example.noteapp.fragment.BottomDialogFragment(null)
                .show(parentFragmentManager, "New Task")
        }

        // Setup day click listeners
        setupDayClickListeners()
    }

    private fun setupDayClickListeners() {
        val dayIds = arrayOf(
            binding.day1, binding.day2, binding.day3,
            binding.day4, binding.day5, binding.day6, binding.day7
        )

        dayIds.forEachIndexed { index, dayLayout ->
            dayLayout.setOnClickListener {
                selectDay(index)
            }
        }
    }

    private fun setupRecyclerView() {
        timeSlotAdapter = TimeSlotAdapter(requireContext()) { todo ->
            editTodo(todo)
        }
        
        allDayTasksAdapter = CalendarTodoAdapter(requireContext(), { todo ->
            editTodo(todo)
        }, { todo ->
            toggleTodoStatus(todo)
        })
        binding.recyclerViewAllDayTasks.apply {
            adapter = allDayTasksAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // Setup day timeline adapter
        dayTaskAdapter = DayTaskAdapter(
            context = requireContext(),
            onTaskClick = { todo ->
                editTodo(todo)
            },
            onStatusToggle = { todo ->
                // Handle status toggle - cycle through statuses
                val newStatus = when (todo.status) {
                    TodoStatus.TODO -> TodoStatus.IN_PROGRESS
                    TodoStatus.IN_PROGRESS -> TodoStatus.IN_REVIEW
                    TodoStatus.IN_REVIEW -> TodoStatus.DONE
                    TodoStatus.COMPLETED -> TodoStatus.TODO
                    TodoStatus.DONE -> TodoStatus.TODO
                    TodoStatus.CANCELLED -> TodoStatus.TODO
                    TodoStatus.ON_HOLD -> TodoStatus.IN_PROGRESS
                }
                calendarViewModel.updateTodoStatus(todo, newStatus)
            }
        )
        binding.recyclerViewDayTasks?.apply {
            adapter = dayTaskAdapter
            layoutManager = LinearLayoutManager(context)
        }
        
        // Setup modern timeline schedule adapter
        timelineScheduleAdapter = TimelineScheduleAdapter(requireContext()) { todo ->
            editTodo(todo)
        }
        binding.recyclerViewTimelineSchedule.apply {
            adapter = timelineScheduleAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setViewMode(viewMode: ViewMode) {
        currentViewMode = viewMode
        
        // When switching to day view, always show today's tasks
        if (viewMode == ViewMode.DAY) {
            selectedDate = Calendar.getInstance() // Reset to today
            currentWeekStart = getWeekStart(selectedDate) // Update week start to include today
            
            // Immediately load today's tasks
            loadTodosForDate(selectedDate)
        }
        
        updateToggleButtons()
        updateUI()
    }

    private fun updateToggleButtons() {
        // Reset all buttons
        binding.btnDayView.setBackgroundResource(R.drawable.bg_toggle_button)
        binding.btnWeekView.setBackgroundResource(R.drawable.bg_toggle_button)

        binding.btnDayView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.text_secondary
            )
        )
        binding.btnWeekView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.text_secondary
            )
        )

        // Highlight selected button
        when (currentViewMode) {
            ViewMode.DAY -> {
                binding.btnDayView.setBackgroundResource(R.drawable.bg_toggle_button_selected)
                binding.btnDayView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        android.R.color.white
                    )
                )
            }

            ViewMode.WEEK -> {
                binding.btnWeekView.setBackgroundResource(R.drawable.bg_toggle_button_selected)
                binding.btnWeekView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        android.R.color.white
                    )
                )
            }
        }
    }

    private fun observeData() {
        calendarViewModel.todosForDate.observe(viewLifecycleOwner) { todos ->
            updateTimeSlots(todos)
            updateTaskCount(todos.size)
        }

        calendarViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        calendarViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
                calendarViewModel.clearError()
            }
        }
    }

    private fun updateTimeSlots(todos: List<ToDo>) {
        // Debug logging
        android.util.Log.d("CalendarFragment", "Updating time slots with ${todos.size} todos")

        when (currentViewMode) {
            ViewMode.DAY -> {
                // Day view: Always filter for today's tasks
                val todayString = dateFormatter.format(Calendar.getInstance().time)
                val todaysTask = todos.filter { todo ->
                    val createdDate = extractDateFromDateTime(todo.createdTime)
                    val dueDate = extractDateFromDateTime(todo.dueTime)
                    val reminderDate = extractDateFromDateTime(todo.reminderTime)
                    
                    createdDate == todayString || dueDate == todayString || reminderDate == todayString
                }
                dayTaskAdapter.submitList(todaysTask)
                
                // Update the header to show "Today" instead of generic text
                binding.textSelectedDayTasks?.text = "Tasks for Today (${todaysTask.size})"
            }

            ViewMode.WEEK -> {
                // Week view: Separate timed tasks from all-day tasks
                val (todosWithTime, allDayTodos) = todos.partition { hasSpecificTime(it) }

                // Always keep header visible
                binding.textAllDayHeader.visibility = View.VISIBLE
                
                // Handle all-day tasks
                if (allDayTodos.isNotEmpty()) {
                    binding.recyclerViewAllDayTasks.visibility = View.VISIBLE
                    allDayTasksAdapter.submitList(allDayTodos)
                    binding.textAllDayHeader.text = "All Day (${allDayTodos.size})"
                } else {
                    // Keep RecyclerView space but show empty list
                    binding.recyclerViewAllDayTasks.visibility = View.VISIBLE
                    allDayTasksAdapter.submitList(emptyList())
                    binding.textAllDayHeader.text = "All Day - No task today"
                }

                // Update modern timeline schedule with timed tasks
                timelineScheduleAdapter.updateTasks(todosWithTime)

                // Update week view to show event indicators
                updateWeekViewWithEvents(todos)
            }
        }
    }

    private fun hasSpecificTime(todo: ToDo): Boolean {
        val timeString = when {
            !todo.reminderTime.isNullOrEmpty() -> todo.reminderTime
            !todo.dueTime.isNullOrEmpty() -> todo.dueTime
            else -> todo.createdTime
        }

        // Check if the time string contains actual time (HH:mm format)
        return timeString?.contains(" ") == true &&
                timeString.split(" ").size >= 2 &&
                timeString.split(" ")[1].contains(":")
    }

    private fun extractHourFromTodo(todo: ToDo): Int {
        return try {
            // Priority order: reminderTime -> dueTime -> createdTime
            val timeString = when {
                !todo.reminderTime.isNullOrEmpty() -> todo.reminderTime
                !todo.dueTime.isNullOrEmpty() -> todo.dueTime
                else -> todo.createdTime
            }

            // Parse time from string (format: "dd/MM/yyyy HH:mm")
            if (timeString?.contains(" ") == true && timeString.split(" ").size >= 2) {
                val timePart = timeString.split(" ")[1] // Get "HH:mm" part
                val hour = timePart.split(":")[0].toInt()
                hour
            } else {
                // Return -1 for tasks without specific time (will be filtered out)
                -1
            }
        } catch (e: Exception) {
            -1 // Invalid time
        }
    }

    private fun formatHourToDisplayTime(hour: Int): String {
        return when {
            hour == 0 -> "12 AM"
            hour < 12 -> "$hour AM"
            hour == 12 -> "12 PM"
            else -> "${hour - 12} PM"
        }
    }

    private fun extractHourFromTimeString(timeString: String?): Int? {
        if (timeString.isNullOrEmpty()) return null

        return try {
            // Check if time string contains time part (HH:mm)
            if (timeString.contains(" ") && timeString.split(" ").size >= 2) {
                val timePart = timeString.split(" ")[1] // Get "HH:mm" part
                val hour = timePart.split(":")[0].toInt()
                hour.coerceIn(0, 23)
            } else {
                // No time specified, return null to use default
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun selectDay(dayIndex: Int) {
        val newDate = Calendar.getInstance()
        newDate.time = currentWeekStart.time
        newDate.add(Calendar.DAY_OF_MONTH, dayIndex)

        selectedDate = newDate
        updateWeekView()
        loadTodosForDate(selectedDate)
    }

    private fun loadTodosForDate(date: Calendar) {
        val dateString = dateFormatter.format(date.time)
        android.util.Log.d("CalendarFragment", "Loading todos for date: $dateString")

        // Also try loading with today's date format for debugging
        val today = Calendar.getInstance()
        val todayString = dateFormatter.format(today.time)
        android.util.Log.d("CalendarFragment", "Today is: $todayString")

        calendarViewModel.loadTodosForDate(dateString)
    }

    private fun updateUI() {
        when (currentViewMode) {
            ViewMode.DAY -> {
                binding.dayViewContainer.visibility = View.VISIBLE
                binding.weekViewContainer.visibility = View.GONE
                binding.weekCalendarLayout.visibility = View.VISIBLE
                binding.weekNavigation.visibility = View.GONE
                updateWeekView()
            }

            ViewMode.WEEK -> {
                binding.dayViewContainer.visibility = View.GONE
                binding.weekViewContainer.visibility = View.VISIBLE
                binding.weekCalendarLayout.visibility = View.VISIBLE
                binding.weekNavigation.visibility = View.VISIBLE
                updateWeekView()
                updateWeekRangeText()
            }
        }
        loadTodosForDate(selectedDate)
    }
    
    private fun updateTaskCount(count: Int) {
        // Task count display removed from new design
        // binding.textTaskCount.text = "$count task${if (count != 1) "s" else ""}" today"
    }

    private fun updateWeekView() {
        val dayLayouts = arrayOf(
            binding.day1, binding.day2, binding.day3,
            binding.day4, binding.day5, binding.day6, binding.day7
        )

        val tempDate = Calendar.getInstance()
        tempDate.time = currentWeekStart.time
        
        // Get current date for highlighting today
        val today = Calendar.getInstance()

        dayLayouts.forEachIndexed { index, dayLayout ->
            val dayNumber = tempDate.get(Calendar.DAY_OF_MONTH)
            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(tempDate.time)

            val dayNameText = dayLayout.getChildAt(0) as TextView
            val dayNumberText = dayLayout.getChildAt(1) as TextView
            
            // Check if this day layout has a third child (indicator dot)
            val hasIndicator = dayLayout.childCount > 2
            val indicatorDot = if (hasIndicator) {
                dayLayout.getChildAt(2) as View
            } else {
                // Create indicator dot if it doesn't exist
                View(context).apply {
                    layoutParams = ViewGroup.LayoutParams(6, 6)
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle)
                    visibility = View.GONE
                    dayLayout.addView(this)
                }
            }

            dayNameText.text = dayName.uppercase()
            dayNumberText.text = dayNumber.toString()
            
            // Reset all styling
            dayLayout.background = null
            dayNumberText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            dayNameText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            indicatorDot.visibility = View.GONE

            // Highlight selected day
            if (isSameDay(tempDate, selectedDate)) {
                dayLayout.setBackgroundResource(R.drawable.bg_selected_day)
                dayNumberText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                dayNameText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                
                // Show white indicator for selected day
                indicatorDot.visibility = View.VISIBLE
                indicatorDot.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.white))
            } 
            // Highlight current day with a special background
            else if (isSameDay(tempDate, today)) {
                // Use a different style for current day (not selected)
                dayNumberText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_current_day)
                dayNumberText.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
                dayNumberText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                dayNumberText.gravity = Gravity.CENTER
                
                // Make day name bold for today
                dayNameText.setTypeface(dayNameText.typeface, Typeface.BOLD)
            }

            tempDate.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun updateWeekViewWithEvents(todos: List<ToDo>) {
        val dayLayouts = arrayOf(
            binding.day1, binding.day2, binding.day3,
            binding.day4, binding.day5, binding.day6, binding.day7
        )

        val tempDate = Calendar.getInstance()
        tempDate.time = currentWeekStart.time

        dayLayouts.forEachIndexed { index, dayLayout ->
            val dateString = dateFormatter.format(tempDate.time)

            // Check if this date has any todos
            val hasTodos = todos.any { todo ->
                val createdDate = extractDateFromDateTime(todo.createdTime)
                val dueDate = extractDateFromDateTime(todo.dueTime)
                val reminderDate = extractDateFromDateTime(todo.reminderTime)

                createdDate == dateString || dueDate == dateString || reminderDate == dateString
            }

            // Add event indicator (small dot) if there are tasks
            if (hasTodos && !isSameDay(tempDate, selectedDate)) {
                // Add a small indicator for events
                dayLayout.alpha = 1.0f
                // You could add a small dot or change background slightly
            } else if (!isSameDay(tempDate, selectedDate)) {
                dayLayout.alpha = 0.7f
            }

            tempDate.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun extractDateFromDateTime(dateTimeString: String?): String? {
        if (dateTimeString == null) return null
        return try {
            if (dateTimeString.matches(Regex("\\d{2}/\\d{2}/\\d{4}$"))) {
                dateTimeString
            } else {
                dateTimeString.split(" ")[0]
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun updateYearView() {
        // Year view removed - using Month view instead
        currentWeekStart = getMonthStart(selectedDate)
        updateWeekView()
    }

    private fun getMonthStart(date: Calendar): Calendar {
        val monthStart = Calendar.getInstance()
        monthStart.time = date.time
        monthStart.set(Calendar.DAY_OF_MONTH, 1)
        return monthStart
    }

    private fun getYearStart(date: Calendar): Calendar {
        val yearStart = Calendar.getInstance()
        yearStart.time = date.time
        yearStart.set(Calendar.DAY_OF_YEAR, 1)
        return yearStart
    }

    private fun navigateDate(direction: Int) {
        when (currentViewMode) {
            ViewMode.DAY -> {
                selectedDate.add(Calendar.DAY_OF_MONTH, direction)
                currentWeekStart = getWeekStart(selectedDate)
            }

            ViewMode.WEEK -> {
                selectedDate.add(Calendar.WEEK_OF_YEAR, direction)
                currentWeekStart = getWeekStart(selectedDate)
            }
        }
        updateUI()
    }

    private fun navigateWeek(direction: Int) {
        currentWeekStart.add(Calendar.WEEK_OF_YEAR, direction)
        selectedDate.time = currentWeekStart.time
        updateUI()
        updateWeekRangeText()
    }

    private fun updateWeekRangeText() {
        val weekStart = Calendar.getInstance().apply { time = currentWeekStart.time }
        val weekEnd = Calendar.getInstance().apply {
            time = currentWeekStart.time
            add(Calendar.DAY_OF_YEAR, 6)
        }

        val startFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val endFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        val rangeText =
            "${startFormat.format(weekStart.time)} - ${endFormat.format(weekEnd.time)}"
        binding.textWeekRange.text = rangeText
    }

    private fun navigateNext() {
        navigateDate(1)
    }

    private fun navigatePrevious() {
        navigateDate(-1)
    }

    private fun getWeekStart(date: Calendar): Calendar {
        val weekStart = Calendar.getInstance()
        weekStart.time = date.time
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        return weekStart
    }

    private fun isSameDay(date1: Calendar, date2: Calendar): Boolean {
        return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR)
    }

    private fun editTodo(todo: ToDo) {
        com.example.noteapp.fragment.BottomDialogFragment(todo)
            .show(parentFragmentManager, "Edit Task")
    }

    private fun toggleTodoStatus(todo: ToDo) {
        when (todo.status) {
            com.example.noteapp.model.TodoStatus.TODO -> {
                calendarViewModel.updateTodoStatus(
                    todo,
                    com.example.noteapp.model.TodoStatus.IN_PROGRESS
                )
            }

            com.example.noteapp.model.TodoStatus.IN_PROGRESS -> {
                calendarViewModel.updateTodoStatus(
                    todo,
                    com.example.noteapp.model.TodoStatus.COMPLETED
                )
            }

            com.example.noteapp.model.TodoStatus.COMPLETED -> {
                calendarViewModel.updateTodoStatus(
                    todo,
                    com.example.noteapp.model.TodoStatus.TODO
                )
            }

            else -> {
                calendarViewModel.updateTodoStatus(
                    todo,
                    com.example.noteapp.model.TodoStatus.TODO
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        
        // Setup notification badge
        val notificationItem = menu.findItem(R.id.action_notifications)
        notificationItem?.actionView?.let { actionView ->
            val badge = actionView.findViewById<com.example.noteapp.ui.NotificationBadgeView>(R.id.notification_badge)
            val icon = actionView.findViewById<android.widget.ImageView>(R.id.notification_icon)
            
            // Load invitation count
            val workspaceViewModel = ViewModelProvider(
                requireActivity(),
                WorkspaceViewModel.WorkspaceViewModelFactory(requireContext())
            )[WorkspaceViewModel::class.java]
            
            val userEmail = SessionManager(requireContext()).getUserEmail()
            if (userEmail != null) {
                workspaceViewModel.loadPendingInvitations(userEmail)
                workspaceViewModel.invitations.observe(viewLifecycleOwner) { invitations ->
                    val count = invitations.size
                    badge?.setCount(count)
                }
            }
            
            // Click listener
            actionView.setOnClickListener {
                com.example.noteapp.ui.InvitationsBottomSheet().show(
                    parentFragmentManager,
                    com.example.noteapp.ui.InvitationsBottomSheet.TAG
                )
            }
        }
        
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifications -> {
                com.example.noteapp.ui.InvitationsBottomSheet().show(
                    parentFragmentManager,
                    com.example.noteapp.ui.InvitationsBottomSheet.TAG
                )
                true
            }
            R.id.action_profile -> {
                findNavController().navigate(R.id.action_calendarFragment_to_profileEditFragment)
                true
            }
            R.id.action_logout -> {
                handleLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleLogout() {
        lifecycleScope.launch {
            val authManager = com.example.noteapp.auth.CustomAuthManager(requireContext())
            val success = authManager.logout()
            if (success) {
                Toast.makeText(requireContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_calendarFragment_to_loginFragment)
            } else {
                Toast.makeText(requireContext(), "Đăng xuất thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
