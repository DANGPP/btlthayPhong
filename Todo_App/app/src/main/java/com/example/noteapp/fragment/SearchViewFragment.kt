package com.example.noteapp.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.R
import com.example.noteapp.adapter.NoteAdapter
import com.example.noteapp.databinding.FragmentHomeBinding
import com.example.noteapp.databinding.FragmentSearchViewBinding
import com.example.noteapp.model.Note
import com.example.noteapp.viewmodel.NoteViewModel
import java.util.Locale


class SearchViewFragment : Fragment() {
    private lateinit var binding: FragmentSearchViewBinding
    private lateinit var adapter: NoteAdapter
    private lateinit var searchView : android.widget.SearchView

    private val noteViewModel: NoteViewModel by lazy {
        ViewModelProvider(this, NoteViewModel.NoteViewModelFactory(requireContext()))[NoteViewModel::class.java]
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchViewBinding.inflate(inflater,container,false)
        searchView = binding.searchView
        initControls()
        searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextChange(query: String?): Boolean {
                if(query != null){
                    filterList(query)
                }
                return true
            }

            @SuppressLint("FragmentLiveDataObserve")
            override fun onQueryTextSubmit(query: String?): Boolean {
               // filterList(query)
                if(query != null){
                    filterList(query)
                }
                return true
            }

        })
        return binding.root
    }

    private fun filterList(query : String?){
        if (query != null) {
            noteViewModel.searchDatabase(query)
        }
    }
    private fun initControls() {
        adapter = NoteAdapter(this@SearchViewFragment.requireContext(), onItemClick())
        binding.recyclerSearchView.setHasFixedSize(true)
        binding.recyclerSearchView.layoutManager = LinearLayoutManager(context)
        binding.recyclerSearchView.adapter = adapter
        
        // Observe search results
        noteViewModel.allNotes.observe(viewLifecycleOwner, Observer { notes ->
            adapter.setNotes(notes)
        })
        
        // Load initial notes
        noteViewModel.getAllNote()

    }

    private fun onItemClick(): (Note) -> Unit = {
        val direction = SearchViewFragmentDirections.actionSearchViewFragmentToShowNoteFragment(it)
        findNavController().navigate(direction)
    }

}