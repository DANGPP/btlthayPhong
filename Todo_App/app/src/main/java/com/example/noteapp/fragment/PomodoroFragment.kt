package com.example.noteapp.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.noteapp.auth.AuthRepositoryImpl
import com.example.noteapp.auth.SessionManager
import kotlinx.coroutines.launch
import com.example.noteapp.R
import com.example.noteapp.databinding.FragmentPomodoroBinding
import com.example.noteapp.model.*
import com.example.noteapp.viewmodel.PomodoroViewModel
import com.example.noteapp.viewmodel.TimerState
import java.util.concurrent.TimeUnit

class PomodoroFragment : Fragment() {

    private var _binding: FragmentPomodoroBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PomodoroViewModel by viewModels {
        PomodoroViewModel.PomodoroViewModelFactory(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPomodoroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        // Timer State Observer
        viewModel.timerState.observe(viewLifecycleOwner) { state ->
            updateTimerControls(state)
        }

        // Time Remaining Observer
        viewModel.timeRemaining.observe(viewLifecycleOwner) { timeRemaining ->
            updateTimerDisplay(timeRemaining)
        }

        // Progress Observer
        viewModel.progress.observe(viewLifecycleOwner) { progress ->
            binding.progressTimer.progress = progress
        }

        // Session Type Observer
        viewModel.currentSessionType.observe(viewLifecycleOwner) { sessionType ->
            updateSessionTypeDisplay(sessionType)
        }

        // Settings Observer
        viewModel.settings.observe(viewLifecycleOwner) { settings ->
            updateSettingsDisplay(settings)
        }

        // Stats Observer
        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            updateStatsDisplay(stats)
        }

        // Linked Todo Observer
        viewModel.linkedTodo.observe(viewLifecycleOwner) { todo ->
            updateLinkedTodoDisplay(todo)
        }

        // Error Observer
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        // Start/Pause Button
        binding.btnStartPause.setOnClickListener {
            when (viewModel.timerState.value) {
                TimerState.STOPPED, TimerState.COMPLETED -> {
                    viewModel.startTimer()
                }
                TimerState.RUNNING -> {
                    viewModel.pauseTimer()
                }
                TimerState.PAUSED -> {
                    viewModel.resumeTimer()
                }
                null -> {
                    viewModel.startTimer()
                }
            }
        }

        // Reset Button
        binding.btnReset.setOnClickListener {
            if (viewModel.timerState.value == TimerState.RUNNING || 
                viewModel.timerState.value == TimerState.PAUSED) {
                showResetConfirmationDialog()
            } else {
                viewModel.resetTimer()
            }
        }

        // Work Duration Button
        binding.btnWorkDuration.setOnClickListener {
            showDurationPickerDialog(PomodoroSessionType.WORK)
        }

        // Break Duration Button
        binding.btnBreakDuration.setOnClickListener {
            showDurationPickerDialog(PomodoroSessionType.SHORT_BREAK)
        }

        // Auto Start Breaks Switch
        binding.switchAutoStartBreaks.setOnCheckedChangeListener { _, isChecked ->
            val currentSettings = viewModel.settings.value ?: PomodoroSettings()
            viewModel.updateSettings(currentSettings.copy(autoStartBreaks = isChecked))
        }

        // Unlink Todo Button
        binding.btnUnlinkTodo.setOnClickListener {
            viewModel.unlinkTodo()
        }

        // Session Type Click (for manual switching when stopped)
        binding.tvSessionType.setOnClickListener {
            if (viewModel.timerState.value == TimerState.STOPPED || 
                viewModel.timerState.value == TimerState.COMPLETED) {
                showSessionTypeDialog()
            }
        }
    }

    private fun updateTimerControls(state: TimerState) {
        when (state) {
            TimerState.STOPPED, TimerState.COMPLETED -> {
                binding.btnStartPause.text = "Start"
                binding.btnStartPause.setIconResource(R.drawable.ic_play)
                binding.btnStartPause.setBackgroundColor(resources.getColor(android.R.color.holo_green_light, null))
                binding.btnReset.isEnabled = true
            }
            TimerState.RUNNING -> {
                binding.btnStartPause.text = "Pause"
                binding.btnStartPause.setIconResource(R.drawable.ic_pause)
                binding.btnStartPause.setBackgroundColor(resources.getColor(android.R.color.holo_orange_light, null))
                binding.btnReset.isEnabled = true
            }
            TimerState.PAUSED -> {
                binding.btnStartPause.text = "Resume"
                binding.btnStartPause.setIconResource(R.drawable.ic_play)
                binding.btnStartPause.setBackgroundColor(resources.getColor(android.R.color.holo_green_light, null))
                binding.btnReset.isEnabled = true
            }
        }
    }

    private fun updateTimerDisplay(timeRemaining: Long) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeRemaining)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeRemaining) % 60
        binding.tvTimerDisplay.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateSessionTypeDisplay(sessionType: PomodoroSessionType) {
        binding.tvSessionType.text = sessionType.displayName
        
        // Update colors based on session type
        val color = when (sessionType) {
            PomodoroSessionType.WORK -> "#FF6B6B"
            PomodoroSessionType.SHORT_BREAK -> "#4ECDC4"
            PomodoroSessionType.LONG_BREAK -> "#45B7D1"
        }
        binding.tvSessionType.setTextColor(android.graphics.Color.parseColor(color))
        
        // Update progress bar color
        updateProgressBarColor(sessionType)
    }

    private fun updateProgressBarColor(sessionType: PomodoroSessionType) {
        // This would require custom drawable or programmatic color change
        // For now, we'll keep the default red color from the XML
    }

    private fun updateSettingsDisplay(settings: PomodoroSettings) {
        val workMinutes = TimeUnit.MILLISECONDS.toMinutes(settings.workDuration)
        val breakMinutes = TimeUnit.MILLISECONDS.toMinutes(settings.shortBreakDuration)
        
        binding.btnWorkDuration.text = "${workMinutes} min"
        binding.btnBreakDuration.text = "${breakMinutes} min"
        binding.switchAutoStartBreaks.isChecked = settings.autoStartBreaks
    }

    private fun updateStatsDisplay(stats: PomodoroStats) {
        binding.tvCompletedSessions.text = stats.completedSessions.toString()
        binding.tvCurrentCycle.text = "${stats.sessionsInCurrentCycle}/${viewModel.settings.value?.sessionsUntilLongBreak ?: 4}"
        
        val totalHours = TimeUnit.MILLISECONDS.toHours(stats.totalFocusTime)
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(stats.totalFocusTime) % 60
        binding.tvTotalTime.text = "${totalHours}h ${totalMinutes}m"
    }

    private fun updateLinkedTodoDisplay(todo: ToDo?) {
        if (todo != null) {
            binding.cardLinkedTodo.visibility = View.VISIBLE
            binding.tvLinkedTodoTitle.text = todo.title
            binding.tvLinkedTodoDescription.text = todo.description.ifEmpty { "No description" }
        } else {
            binding.cardLinkedTodo.visibility = View.GONE
        }
    }

    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Timer")
            .setMessage("Are you sure you want to reset the current session? Your progress will be lost.")
            .setPositiveButton("Reset") { _, _ ->
                viewModel.resetTimer()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDurationPickerDialog(sessionType: PomodoroSessionType) {
        val currentSettings = viewModel.settings.value ?: PomodoroSettings()
        val currentDuration = when (sessionType) {
            PomodoroSessionType.WORK -> TimeUnit.MILLISECONDS.toMinutes(currentSettings.workDuration).toInt()
            PomodoroSessionType.SHORT_BREAK -> TimeUnit.MILLISECONDS.toMinutes(currentSettings.shortBreakDuration).toInt()
            PomodoroSessionType.LONG_BREAK -> TimeUnit.MILLISECONDS.toMinutes(currentSettings.longBreakDuration).toInt()
        }

        val durations = arrayOf("5 min", "10 min", "15 min", "20 min", "25 min", "30 min", "45 min", "60 min")
        val durationValues = arrayOf(5, 10, 15, 20, 25, 30, 45, 60)
        val selectedIndex = durationValues.indexOf(currentDuration).takeIf { it >= 0 } ?: 4 // Default to 25 min

        AlertDialog.Builder(requireContext())
            .setTitle("Select ${sessionType.displayName} Duration")
            .setSingleChoiceItems(durations, selectedIndex) { dialog, which ->
                val newDurationMinutes = durationValues[which]
                val newDurationMillis = TimeUnit.MINUTES.toMillis(newDurationMinutes.toLong())
                
                val newSettings = when (sessionType) {
                    PomodoroSessionType.WORK -> currentSettings.copy(workDuration = newDurationMillis)
                    PomodoroSessionType.SHORT_BREAK -> currentSettings.copy(shortBreakDuration = newDurationMillis)
                    PomodoroSessionType.LONG_BREAK -> currentSettings.copy(longBreakDuration = newDurationMillis)
                }
                
                viewModel.updateSettings(newSettings)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSessionTypeDialog() {
        val sessionTypes = arrayOf(
            PomodoroSessionType.WORK.displayName,
            PomodoroSessionType.SHORT_BREAK.displayName,
            PomodoroSessionType.LONG_BREAK.displayName
        )
        
        val currentType = viewModel.currentSessionType.value ?: PomodoroSessionType.WORK
        val selectedIndex = when (currentType) {
            PomodoroSessionType.WORK -> 0
            PomodoroSessionType.SHORT_BREAK -> 1
            PomodoroSessionType.LONG_BREAK -> 2
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Select Session Type")
            .setSingleChoiceItems(sessionTypes, selectedIndex) { dialog, which ->
                val newSessionType = when (which) {
                    0 -> PomodoroSessionType.WORK
                    1 -> PomodoroSessionType.SHORT_BREAK
                    2 -> PomodoroSessionType.LONG_BREAK
                    else -> PomodoroSessionType.WORK
                }
                
                viewModel.switchSessionType(newSessionType)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Method to link a todo from other fragments
    fun linkTodo(todo: ToDo) {
        viewModel.linkTodo(todo)
        Toast.makeText(requireContext(), "Todo linked to Pomodoro timer", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                handleLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleLogout() {
        lifecycleScope.launch {
            val repo = AuthRepositoryImpl(requireContext())
            val sessionManager = SessionManager(requireContext())
            val success = repo.logout()
            if (success) {
                sessionManager.clearSession()
                Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_pomodoroFragment_to_loginFragment)
            } else {
                Toast.makeText(requireContext(), "Logout failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
