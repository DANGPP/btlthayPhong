package com.example.noteapp.fragment

import android.os.Bundle
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
import com.example.noteapp.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var authService: AuthService
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        authService = AuthService(requireContext())
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            
            if (validateInput(name, email, password, confirmPassword)) {
                registerUser(name, email, password)
            }
        }
        
        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }
    
    private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        binding.tvError.visibility = View.GONE
        
        when {
            name.isEmpty() -> {
                showError("Please enter your full name")
                return false
            }
            name.length < 2 -> {
                showError("Name must be at least 2 characters")
                return false
            }
            email.isEmpty() -> {
                showError("Please enter your email")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError("Please enter a valid email address")
                return false
            }
            password.isEmpty() -> {
                showError("Please enter a password")
                return false
            }
            password.length < 8 -> {
                showError("Password must be at least 8 characters")
                return false
            }
            !password.matches(".*[A-Z].*".toRegex()) -> {
                showError("Password must contain at least one uppercase letter")
                return false
            }
            !password.matches(".*[a-z].*".toRegex()) -> {
                showError("Password must contain at least one lowercase letter")
                return false
            }
            !password.matches(".*\\d.*".toRegex()) -> {
                showError("Password must contain at least one number")
                return false
            }
            confirmPassword.isEmpty() -> {
                showError("Please confirm your password")
                return false
            }
            password != confirmPassword -> {
                showError("Passwords do not match")
                return false
            }
        }
        
        return true
    }
    
    private fun registerUser(name: String, email: String, password: String) {
        showLoading(true)
        
        lifecycleScope.launch {
            when (val result = authService.register(email, password, name)) {
                is AuthResult.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        "Account created successfully! Welcome, ${result.user.name}!",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Auto-login after successful registration
                    loginAfterRegistration(email, password)
                }
                is AuthResult.Error -> {
                    showLoading(false)
                    showError(result.message)
                }
            }
        }
    }
    
    private fun loginAfterRegistration(email: String, password: String) {
        lifecycleScope.launch {
            when (val result = authService.login(email, password)) {
                is AuthResult.Success -> {
                    // Navigate to main app
                    findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                }
                is AuthResult.Error -> {
                    // If auto-login fails, navigate to login screen
                    Toast.makeText(
                        requireContext(),
                        "Account created! Please sign in.",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                }
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !show
        binding.etName.isEnabled = !show
        binding.etEmail.isEnabled = !show
        binding.etPassword.isEnabled = !show
        binding.etConfirmPassword.isEnabled = !show
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
