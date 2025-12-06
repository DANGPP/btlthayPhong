package com.example.noteapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.noteapp.R
import com.example.noteapp.auth.SessionManager
import com.example.noteapp.databinding.FragmentProfileEditBinding
import com.example.noteapp.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class ProfileEditFragment : Fragment() {
    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager
    private var selectedAvatarUri: Uri? = null
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedAvatarUri = result.data?.data
            selectedAvatarUri?.let { uri ->
                binding.imageViewAvatar.setImageURI(uri)
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        val factory = AuthViewModel.AuthViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        
        setupToolbar()
        loadUserInfo()
        setupListeners()
        setupObservers()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun loadUserInfo() {
        val email = sessionManager.getUserEmail()
        val name = sessionManager.getUserName()
        
        binding.etEmail.setText(email)
        binding.etName.setText(name)
    }
    
    private fun setupListeners() {
        binding.fabChangeAvatar.setOnClickListener {
            openImagePicker()
        }
        
        binding.btnSave.setOnClickListener {
            saveChanges()
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }
    
    private fun saveChanges() {
        val name = binding.etName.text.toString().trim()
        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmNewPassword.text.toString()
        
        // Validate name
        if (name.isEmpty()) {
            Snackbar.make(binding.root, "Vui lòng nhập tên", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        // Update name if changed
        val currentName = sessionManager.getUserName()
        if (name != currentName) {
            updateUserName(name)
        }
        
        // Update password if provided
        if (currentPassword.isNotEmpty() || newPassword.isNotEmpty() || confirmPassword.isNotEmpty()) {
            if (currentPassword.isEmpty()) {
                Snackbar.make(binding.root, "Vui lòng nhập mật khẩu hiện tại", Snackbar.LENGTH_SHORT).show()
                return
            }
            
            if (newPassword.isEmpty()) {
                Snackbar.make(binding.root, "Vui lòng nhập mật khẩu mới", Snackbar.LENGTH_SHORT).show()
                return
            }
            
            if (newPassword.length < 6) {
                Snackbar.make(binding.root, "Mật khẩu mới phải có ít nhất 6 ký tự", Snackbar.LENGTH_SHORT).show()
                return
            }
            
            if (newPassword != confirmPassword) {
                Snackbar.make(binding.root, "Mật khẩu xác nhận không khớp", Snackbar.LENGTH_SHORT).show()
                return
            }
            
            updatePassword(currentPassword, newPassword)
        } else {
            // Only name changed
            Snackbar.make(binding.root, "Đã lưu thay đổi", Snackbar.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
        
        // TODO: Handle avatar upload when implemented
        if (selectedAvatarUri != null) {
            Snackbar.make(binding.root, "Chức năng đổi avatar đang được phát triển", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    private fun updateUserName(newName: String) {
        val userId = sessionManager.getCurrentUserId() ?: return
        
        viewModel.updateUserName(userId, newName)
        sessionManager.saveUserName(newName)
    }
    
    private fun updatePassword(currentPassword: String, newPassword: String) {
        val userId = sessionManager.getCurrentUserId() ?: return
        viewModel.changePassword(userId, currentPassword, newPassword)
    }
    
    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
        
        viewModel.passwordChangeSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Snackbar.make(binding.root, "Đã đổi mật khẩu thành công", Snackbar.LENGTH_SHORT).show()
                
                // Clear password fields
                binding.etCurrentPassword.setText("")
                binding.etNewPassword.setText("")
                binding.etConfirmNewPassword.setText("")
                
                // Navigate back after short delay
                binding.root.postDelayed({
                    findNavController().navigateUp()
                }, 1500)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
