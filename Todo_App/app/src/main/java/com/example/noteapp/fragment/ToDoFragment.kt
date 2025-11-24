package com.example.noteapp.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.auth.AuthRepositoryImpl
import com.example.noteapp.auth.SessionManager
import kotlinx.coroutines.launch
import com.example.noteapp.R
import com.example.noteapp.adapter.ToDoAdapter
import com.example.noteapp.databinding.FragmentToDoBinding
import com.example.noteapp.model.ToDo
import com.example.noteapp.viewmodel.ToDoViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Timer
import kotlin.concurrent.timerTask

class ToDoFragment : Fragment() {
    private lateinit var binding: FragmentToDoBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var adapter: ToDoAdapter
    private var isLinearLayout:Boolean = false
    private val toDoViewModel: ToDoViewModel by lazy {
        val vm = ViewModelProvider(requireActivity(), ToDoViewModel.ToDoViewModelFactory(requireContext()))[ToDoViewModel::class.java]
        Log.d("ToDoFragment", "ToDoFragment using ViewModel instance: ${vm.hashCode()}")
        vm
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentToDoBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initControls()

    }


    @SuppressLint("NotifyDataSetChanged")
    private fun initControls() {
        adapter = ToDoAdapter(this.requireContext(), editToDo,completedToDo)
        recyclerView = binding.recyclerview
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerview.layoutManager = linearLayoutManager
        gridLayoutManager = GridLayoutManager(context,2)
        binding.recyclerview.adapter = adapter

        // Observe todos data
        toDoViewModel.allTodos.observe(viewLifecycleOwner, Observer {
            Log.d("ToDoFragment","initControls: $it")
            adapter.setToDo(it)
        })

        // Observe loading state
        toDoViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            // You can show/hide loading indicator here if needed
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        // Observe errors
        toDoViewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
                toDoViewModel.clearError()
            }
        })

        // Observe operation success
        toDoViewModel.operationSuccess.observe(viewLifecycleOwner, Observer { success ->
            if (success == true) {
                // Operation completed successfully - no need to clear here
                // BottomDialogFragment will handle clearing after dismiss
            }
        })

        // Observe redirect to login
        toDoViewModel.shouldRedirectToLogin.observe(viewLifecycleOwner, Observer { shouldRedirect ->
            if (shouldRedirect) {
                // Navigate to login screen
                findNavController().navigate(R.id.action_toDoFragment_to_loginFragment)
                toDoViewModel.clearRedirectToLogin()
            }
        })

        // Load todos when fragment is created
        toDoViewModel.loadAllTodos()

        parentFragmentManager.setFragmentResultListener("toDoSort",viewLifecycleOwner) { key, bundle ->
            val isAscending = bundle.getBoolean("bundleKey")

            if(isAscending){
                toDoViewModel.loadAllTodosSortedByCreatedTimeASC()
            }
            else{
                toDoViewModel.loadAllTodosSortedByCreatedTimeDESC()
            }
        }

    }


    private val editToDo: (ToDo) ->Unit =  {
        BottomDialogFragment(it).show(parentFragmentManager,"Edit Task")
    }

    private val completedToDo: (ToDo) ->Unit =  {
        if (it.isCompleted) {
            toDoViewModel.markTodoIncomplete(it)
        } else {
            toDoViewModel.markTodoCompleted(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_workspaces -> {
                findNavController().navigate(R.id.workspaceFragment)
                true
            }
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
                findNavController().navigate(R.id.action_toDoFragment_to_loginFragment)
            } else {
                Toast.makeText(requireContext(), "Logout failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

}