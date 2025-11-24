package com.example.noteapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.noteapp.appwrite.AppwriteConfig
import com.example.noteapp.appwrite.AppwriteValidator
import com.example.noteapp.databinding.ActivityMainBinding
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure immersive mode
        setupImmersiveMode()

        // Initialize date/time
        AndroidThreeTen.init(this)

        // Initialize Appwrite client
        AppwriteConfig.init(this)
        
        // Validate Appwrite configuration (only in debug mode)
        if (BuildConfig.DEBUG) {
            lifecycleScope.launch {
                AppwriteValidator(this@MainActivity).quickValidate()
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)

        // Setup Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupBottomNavigation()
        
        // Hide/show bottom navigation and toolbar based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment,
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.addNoteFragment,
                R.id.editNoteFragment,
                R.id.showNoteFragment,
                R.id.searchViewFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                    binding.toolbar.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.toolbar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_todo -> {
                    navController.navigate(R.id.toDoFragment)
                    true
                }
                R.id.nav_calendar -> {
                    navController.navigate(R.id.calendarFragment)
                    true
                }
                R.id.nav_pomodoro -> {
                    navController.navigate(R.id.pomodoroFragment)
                    true
                }
                R.id.nav_ai_schedule -> {
                    navController.navigate(R.id.smartScheduleFragment)
                    true
                }
                R.id.nav_statistics -> {
                    navController.navigate(R.id.statisticsFragment)
                    true
                }
                else -> false
            }
        }
    }
}
