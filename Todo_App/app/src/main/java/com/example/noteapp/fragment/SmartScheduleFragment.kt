package com.example.noteapp.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.net.Uri
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.auth.AuthRepositoryImpl
import com.example.noteapp.auth.SessionManager
import kotlinx.coroutines.launch
import com.example.noteapp.R
import com.example.noteapp.adapter.TodoPreviewAdapter
import com.example.noteapp.databinding.FragmentSmartScheduleBinding
import com.example.noteapp.model.ToDo
import com.example.noteapp.viewmodel.SmartScheduleViewModel
import java.util.*

class SmartScheduleFragment : Fragment() {

    private var _binding: FragmentSmartScheduleBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SmartScheduleViewModel by viewModels()
    private lateinit var previewAdapter: TodoPreviewAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    
    private val speechRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            results?.firstOrNull()?.let { spokenText ->
                binding.etAIPrompt.setText(spokenText)
            }
        }
    }
    
    private val microphonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceRecognition()
        } else {
            Toast.makeText(context, "Microphone permission required for voice input", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSmartScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        setupTextWatcher()
        observeViewModel()
        
        // Check if API key is configured
        viewModel.checkApiConfiguration()
    }

    private fun setupRecyclerView() {
        previewAdapter = TodoPreviewAdapter { todo, position ->
            // Handle edit todo click
            showEditTodoDialog(todo, position)
        }
        
        binding.rvPreviewTodos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = previewAdapter
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Quick action buttons
            btnStudySession.setOnClickListener {
                etAIPrompt.setText("Schedule a 2-hour study session for tomorrow")
            }
            
            btnMeeting.setOnClickListener {
                etAIPrompt.setText("Schedule a 1-hour team meeting for next Monday at 10am")
            }
            
            btnExercise.setOnClickListener {
                etAIPrompt.setText("Schedule a 45-minute workout session for tomorrow morning")
            }
            
            // Voice input
            btnVoiceInput.setOnClickListener {
                checkMicrophonePermissionAndStart()
            }
            
            // Generate todos
            btnGenerateTodos.setOnClickListener {
                val prompt = etAIPrompt.text.toString().trim()
                if (prompt.isNotEmpty()) {
                    viewModel.generateTodos(prompt)
                }
            }
            
            // API configuration
            btnShowApiConfig.setOnClickListener {
                toggleApiConfigVisibility()
            }
            
            btnGetApiKey.setOnClickListener {
                openGeminiApiKeyWebsite()
            }
            
            btnSaveApiKey.setOnClickListener {
                saveApiConfiguration()
            }
            
            // Preview actions
            btnEditTasks.setOnClickListener {
                // Allow editing individual tasks
                Toast.makeText(context, "Tap on any task to edit it", Toast.LENGTH_SHORT).show()
            }
            
            btnConfirmTasks.setOnClickListener {
                viewModel.confirmAndCreateTodos()
            }
        }
    }

    private fun setupTextWatcher() {
        binding.etAIPrompt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.btnGenerateTodos.isEnabled = !s.isNullOrBlank()
            }
        })
    }

    private fun openGeminiApiKeyWebsite() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://makersuite.google.com/app/apikey"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open browser. Please visit: https://makersuite.google.com/app/apikey", Toast.LENGTH_LONG).show()
        }
    }

    private fun observeViewModel() {
        viewModel.apply {
            // Loading state
            isLoading.observe(viewLifecycleOwner, Observer { loading ->
                binding.layoutLoading.visibility = if (loading) View.VISIBLE else View.GONE
                binding.btnGenerateTodos.isEnabled = !loading && binding.etAIPrompt.text.toString().isNotBlank()
            })
            
            // Generated todos
            generatedTodos.observe(viewLifecycleOwner, Observer { todos ->
                if (todos.isNotEmpty()) {
                    previewAdapter.updateTodos(todos)
                    binding.cardPreview.visibility = View.VISIBLE
                    binding.tvTaskCount.text = "${todos.size} task${if (todos.size != 1) "s" else ""}"
                } else {
                    binding.cardPreview.visibility = View.GONE
                }
            })
            
            // Error messages
            errorMessage.observe(viewLifecycleOwner, Observer { error ->
                if (error.isNotBlank()) {
                    binding.tvErrorMessage.text = error
                    binding.cardError.visibility = View.VISIBLE
                } else {
                    binding.cardError.visibility = View.GONE
                }
            })
            
            // API configuration status
            hasApiKey.observe(viewLifecycleOwner, Observer { hasKey ->
                if (hasKey) {
                    binding.cardSetupInstructions.visibility = View.GONE
                    binding.btnGenerateTodos.isEnabled = binding.etAIPrompt.text.toString().isNotBlank()
                } else {
                    binding.cardSetupInstructions.visibility = View.VISIBLE
                    binding.btnGenerateTodos.isEnabled = false
                }
            })
            
            // Success message
            successMessage.observe(viewLifecycleOwner, Observer { message ->
                if (message.isNotBlank()) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    // Clear the form after successful creation
                    binding.etAIPrompt.setText("")
                    binding.cardPreview.visibility = View.GONE
                }
            })
        }
    }

    private fun checkMicrophonePermissionAndStart() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceRecognition()
            }
            else -> {
                microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell me what you want to schedule...")
        }
        
        try {
            speechRecognitionLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Speech recognition not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleApiConfigVisibility() {
        binding.cardApiConfig.visibility = if (binding.cardApiConfig.visibility == View.VISIBLE) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun saveApiConfiguration() {
        val apiKey = binding.etApiKey.text.toString().trim()
        
        if (apiKey.isBlank()) {
            Toast.makeText(context, "Please enter your Gemini API key", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModel.saveApiConfiguration(apiKey)
        
        binding.etApiKey.setText("")
        binding.cardApiConfig.visibility = View.GONE
        Toast.makeText(context, "Gemini API key saved successfully", Toast.LENGTH_SHORT).show()
    }

    private fun showEditTodoDialog(todo: ToDo, position: Int) {
        // For now, show a simple toast. In a full implementation, 
        // you would show a dialog to edit the todo
        Toast.makeText(
            context, 
            "Edit functionality would open a dialog for: ${todo.title}", 
            Toast.LENGTH_SHORT
        ).show()
        
        // TODO: Implement edit dialog similar to BottomDialogFragment
        // but for editing AI-generated todos before confirmation
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
                findNavController().navigate(R.id.action_smartScheduleFragment_to_loginFragment)
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
