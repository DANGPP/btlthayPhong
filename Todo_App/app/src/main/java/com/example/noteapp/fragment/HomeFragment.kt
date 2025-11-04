package com.example.noteapp.fragment





import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.noteapp.R
import com.example.noteapp.adapter.NoteAdapter
import com.example.noteapp.adapter.PagerAdapter
import com.example.noteapp.adapter.ToDoAdapter
import com.example.noteapp.appwrite.AuthService
import com.example.noteapp.databinding.FragmentHomeBinding
import com.example.noteapp.model.Note
import com.example.noteapp.model.ToDo
import com.example.noteapp.viewmodel.NoteViewModel
import com.example.noteapp.viewmodel.ToDoViewModel
import kotlinx.coroutines.launch
import com.google.android.material.tabs.TabLayout
import java.util.Timer
import kotlin.concurrent.timerTask
import org.owasp.encoder.Encode


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var toolBar: androidx.appcompat.widget.Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var toDoFragment: ToDoFragment
    private  var linearLayoutBoolean: Boolean = false
    private var isAscending: Boolean = true
    private var showFabButton : Boolean = false
    private lateinit var authService: AuthService

    private val noteViewModel: NoteViewModel by lazy {
        ViewModelProvider(this, NoteViewModel.NoteViewModelFactory(requireContext()))[NoteViewModel::class.java]
    }
    
    private val toDoViewModel: ToDoViewModel by lazy {
        val vm = ViewModelProvider(requireActivity(), ToDoViewModel.ToDoViewModelFactory(requireContext()))[ToDoViewModel::class.java]
        Log.d("HomeFragment", "HomeFragment using ViewModel instance: ${vm.hashCode()}")
        vm
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        authService = AuthService(requireContext())
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home,menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        initControls()
        //toolBar
        toolBar = binding.toolbarHome
        (requireActivity() as AppCompatActivity?)!!.setSupportActionBar(toolBar)
        //
        tabLayout = binding.tabLayout
        viewPager2 = binding.viewPager2
        pagerAdapter = PagerAdapter(childFragmentManager,lifecycle)
        tabLayout.addTab(tabLayout.newTab().setText("Note"))
        tabLayout.addTab(tabLayout.newTab().setText("Todo"))
        tabLayout.addTab(tabLayout.newTab().setText("Calendar"))
        tabLayout.addTab(tabLayout.newTab().setText("Pomodoro"))
        tabLayout.addTab(tabLayout.newTab().setText("AI Schedule"))
        tabLayout.addTab(tabLayout.newTab().setText("Stats"))
        toDoFragment = ToDoFragment()
        val fragmentManager: FragmentManager = childFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(0,toDoFragment)
        fragmentTransaction.commit()

        viewPager2.adapter = pagerAdapter
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    viewPager2.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })
        return binding.root
    }


    private fun initControls() {

        binding.fab.setOnClickListener{
            if(!showFabButton){
                binding.fabNote.visibility = View.VISIBLE
                binding.fabTodo.visibility = View.VISIBLE
            }
            else{
                binding.fabNote.visibility = View.INVISIBLE
                binding.fabTodo.visibility = View.INVISIBLE
            }
            showFabButton = !showFabButton
        }
        binding.fabNote.setOnClickListener{
            findNavController().navigate(com.example.noteapp.R.id.action_homeFragment_to_addNoteFragment)
            showFabButton = !showFabButton
            binding.fabNote.visibility = View.INVISIBLE
            binding.fabTodo.visibility = View.INVISIBLE
        }
        binding.fabTodo.setOnClickListener {
            BottomDialogFragment(null).show(childFragmentManager,"New Task")
            showFabButton = !showFabButton
            binding.fabNote.visibility = View.INVISIBLE
            binding.fabTodo.visibility = View.INVISIBLE
        }
        val originalString = ""
        val encodedString = Encode.forJava(originalString)

        Log.d("Encoded String", encodedString)

    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       when (item.itemId){
           R.id.search -> {
               val direction = HomeFragmentDirections.actionHomeFragmentToSearchViewFragment()
               findNavController().navigate(direction)
           }
           R.id.sort -> {
               isAscending = !isAscending
              // Toast.makeText(context,isAscending.toString(), Toast.LENGTH_SHORT).show()
               childFragmentManager.setFragmentResult("noteSort", bundleOf("bundleKey" to isAscending))
               childFragmentManager.setFragmentResult("toDoSort", bundleOf("bundleKey" to isAscending))
               }
           R.id.custom -> {
                linearLayoutBoolean = !linearLayoutBoolean
              // Toast.makeText(context,linearLayoutBoolean.toString(), Toast.LENGTH_SHORT).show()
               childFragmentManager.setFragmentResult("noteLayoutManager", bundleOf("bundleKey" to linearLayoutBoolean))
           }
           R.id.logout -> {
               handleLogout()
           }

       }

        return true
    }
    
    private fun handleLogout() {
        lifecycleScope.launch {
            val success = authService.logout()
            if (success) {
                Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
            } else {
                Toast.makeText(requireContext(), "Logout failed", Toast.LENGTH_SHORT).show()
            }
        }
    }


}