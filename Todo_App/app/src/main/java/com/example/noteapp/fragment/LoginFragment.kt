package com.example.noteapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.noteapp.R
import com.example.noteapp.appwrite.AuthResult
import com.example.noteapp.appwrite.AuthService
import com.example.noteapp.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var authService: AuthService
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        authService = AuthService(requireContext())
        
        setupClickListeners()
        checkIfAlreadyLoggedIn()
    }
    
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }
        
        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        binding.tvError.visibility = View.GONE
        
        when {
            email.isEmpty() -> {
                showError("Please enter your email")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError("Please enter a valid email address")
                return false
            }
            password.isEmpty() -> {
                showError("Please enter your password")
                return false
            }
            password.length < 6 -> {
                showError("Password must be at least 6 characters")
                return false
            }
        }
        
        return true
    }
    
    private fun loginUser(email: String, password: String) {
        showLoading(true)
        Log.e("LoginFragment", "Attempting login for email: $email")
        lifecycleScope.launch {
            when (val result = authService.login(email, password)) {
                is AuthResult.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        "Welcome back, ${result.user.name}!",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Navigate to main app
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
                is AuthResult.Error -> {
                    showLoading(false)
                    showError(result.message)
                }
            }
        }
    }
    
    private fun checkIfAlreadyLoggedIn() {
        lifecycleScope.launch {
            if (authService.isLoggedIn()) {
                // User is already logged in, navigate to main app
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
        binding.etEmail.isEnabled = !show
        binding.etPassword.isEnabled = !show
    }
    
    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
