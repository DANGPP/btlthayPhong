package com.example.noteapp.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.noteapp.R
import com.example.noteapp.auth.AuthRepositoryImpl
import com.example.noteapp.auth.SessionManager
import kotlinx.coroutines.launch
import com.example.noteapp.databinding.FragmentStatisticsBinding
import com.example.noteapp.viewmodel.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class StatisticsFragment : Fragment() {
    
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var statisticsViewModel: StatisticsViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        statisticsViewModel = ViewModelProvider(this, StatisticsViewModel.StatisticsViewModelFactory(requireContext()))[StatisticsViewModel::class.java]
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        // Setup refresh button
        binding.btnRefresh.setOnClickListener {
            statisticsViewModel.refreshStatistics()
        }
        
        // Setup chart
        setupChart()
    }
    
    private fun setupChart() {
        binding.chartDailyCompletions.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(false)
            
            // X-axis setup
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = 7
            }
            
            // Y-axis setup
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            
            legend.isEnabled = false
        }
    }
    
    private fun observeViewModel() {
        statisticsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        statisticsViewModel.todoStatistics.observe(viewLifecycleOwner) { todoStats ->
            updateTodoStatistics(todoStats)
        }
        
        statisticsViewModel.overallStatistics.observe(viewLifecycleOwner) { overallStats ->
            updateOverallStatistics(overallStats)
        }
        
        statisticsViewModel.error.observe(viewLifecycleOwner) { error ->
            // Handle error display
            // You could show a Snackbar or Toast here
        }
    }
    
    private fun updateTodoStatistics(todoStats: com.example.noteapp.model.TodoStatistics) {
        binding.apply {
            tvTotalTodos.text = todoStats.totalTodos.toString()
            tvCompletedTodos.text = todoStats.completedTodos.toString()
            
            val completionRate = todoStats.completionRate.roundToInt()
            tvCompletionRate.text = "$completionRate%"
            progressCompletion.progress = completionRate
            
            tvWeeklyCompleted.text = todoStats.weeklyCompletedTodos.toString()
            tvMonthlyCompleted.text = todoStats.monthlyCompletedTodos.toString()
        }
        
        // Update chart with daily completions
        updateDailyCompletionsChart(todoStats.dailyCompletions)
    }
    
    private fun updateOverallStatistics(overallStats: com.example.noteapp.model.OverallStatistics) {
        binding.apply {
            val focusScore = overallStats.focusScore.roundToInt()
            tvFocusScore.text = focusScore.toString()
            progressFocusScore.progress = focusScore
        }
    }
    
    private fun updateDailyCompletionsChart(dailyCompletions: Map<String, Int>) {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        
        // Sort by date and create entries
        val sortedEntries = dailyCompletions.toList().sortedBy { it.first }
        
        sortedEntries.forEachIndexed { index, (dateString, count) ->
            entries.add(BarEntry(index.toFloat(), count.toFloat()))
            
            try {
                val date = dateFormat.parse(dateString)
                labels.add(if (date != null) displayFormat.format(date) else dateString)
            } catch (e: Exception) {
                labels.add(dateString)
            }
        }
        
        if (entries.isNotEmpty()) {
            val dataSet = BarDataSet(entries, "Daily Completions").apply {
                color = Color.parseColor("#009688") // Teal color
                valueTextColor = Color.BLACK
                valueTextSize = 12f
            }
            
            val barData = BarData(dataSet)
            barData.barWidth = 0.8f
            
            binding.chartDailyCompletions.apply {
                data = barData
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                invalidate() // Refresh the chart
            }
        }
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
                findNavController().navigate(R.id.action_statisticsFragment_to_loginFragment)
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
