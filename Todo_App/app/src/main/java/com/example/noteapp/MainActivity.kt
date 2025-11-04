package com.example.noteapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.noteapp.appwrite.AppwriteConfig
import com.example.noteapp.databinding.ActivityMainBinding
import com.example.noteapp.fragment.*
import com.jakewharton.threetenabp.AndroidThreeTen
import io.appwrite.services.Account
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure immersive mode
        setupImmersiveMode()

        // Initialize date/time
        AndroidThreeTen.init(this)

        // Initialize Appwrite client
        AppwriteConfig.init(this)

        // Test login Appwrite (⚠️ đổi email/password đúng với user Appwrite)
//        lifecycleScope.launch {
//            try {
//                val client = AppwriteConfig.getClient(this@MainActivity)
//                val account = Account(client)
//                account.createEmailPasswordSession("dangthbm2k4@gmail.com", "Dang@@211204")
//                val user = account.get()
//                Log.d("AuthService", "✅ Đăng nhập thành công: ${user.name} (${user.email})")
//            } catch (e: Exception) {
//                Log.e("AuthService", "❌ Lỗi đăng nhập: ${e.message}")
//            }
//        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(CalendarFragment())
        }
    }

    private fun setupImmersiveMode() {
        supportActionBar?.hide()
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
                R.id.nav_statistics -> { loadFragment(StatisticsFragment()); true }
                R.id.nav_profile -> { loadFragment(ProfileFragment()); true }
                R.id.nav_calendar -> { loadFragment(CalendarFragment()); true }
                R.id.nav_pomodoro -> { loadFragment(PomodoroFragment()); true }
                R.id.nav_ai_schedule -> { loadFragment(SmartScheduleFragment()); true }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_calendar
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
