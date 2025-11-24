package com.example.noteapp.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.noteapp.R
import com.example.noteapp.auth.AuthRepositoryImpl
import kotlinx.coroutines.launch


class SplashFragment : Fragment() {

    // don't keep a long-lived reference to context-bound services

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Handler(Looper.myLooper()!!).postDelayed(
            {
                checkAuthenticationAndNavigate()
            },3000
        )

        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    private fun checkAuthenticationAndNavigate() {
        lifecycleScope.launch {
            val repo = AuthRepositoryImpl(requireContext())
            if (repo.isLoggedIn()) {
                // User is logged in, navigate to calendar
                findNavController().navigate(R.id.action_splashFragment_to_calendarFragment)
            } else {
                // User is not logged in, navigate to login
                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
            }
        }
    }

}