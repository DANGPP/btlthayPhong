package com.example.noteapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.noteapp.databinding.FragmentNotificationSettingsBinding
import com.example.noteapp.model.NotificationSettings
import com.example.noteapp.repository.NotificationRepository

class NotificationSettingsFragment : Fragment() {
    
    private var _binding: FragmentNotificationSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var notificationRepository: NotificationRepository
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        notificationRepository = NotificationRepository(requireContext())
        
        setupUI()
        loadCurrentSettings()
    }
    
    private fun setupUI() {
        binding.btnSaveSettings.setOnClickListener {
            saveSettings()
        }
    }
    
    private fun loadCurrentSettings() {
        val settings = notificationRepository.getNotificationSettings()
        
        binding.apply {
            switchReminders.isChecked = settings.enableReminders
            switchDeadlines.isChecked = settings.enableDeadlineAlerts
            switchOverdue.isChecked = settings.enableOverdueAlerts
            switchDailySummary.isChecked = settings.enableDailySummary
            switchWeeklySummary.isChecked = settings.enableWeeklySummary
            
            etReminderMinutes.setText(settings.reminderMinutesBefore.toString())
            etDeadlineHours.setText(settings.deadlineHoursBefore.toString())
        }
    }
    
    private fun saveSettings() {
        try {
            val reminderMinutes = binding.etReminderMinutes.text.toString().toIntOrNull() ?: 60
            val deadlineHours = binding.etDeadlineHours.text.toString().toIntOrNull() ?: 24
            
            val settings = NotificationSettings(
                enableReminders = binding.switchReminders.isChecked,
                enableDeadlineAlerts = binding.switchDeadlines.isChecked,
                enableOverdueAlerts = binding.switchOverdue.isChecked,
                enableDailySummary = binding.switchDailySummary.isChecked,
                enableWeeklySummary = binding.switchWeeklySummary.isChecked,
                reminderMinutesBefore = reminderMinutes,
                deadlineHoursBefore = deadlineHours
            )
            
            notificationRepository.saveNotificationSettings(settings)
            
            Toast.makeText(requireContext(), "Settings saved successfully", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error saving settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
