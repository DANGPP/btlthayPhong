package com.example.noteapp.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.adapter.NoteAdapter
import com.example.noteapp.databinding.FragmentHomeBinding
import com.example.noteapp.databinding.FragmentNoteBinding
import com.example.noteapp.model.Note
import com.example.noteapp.utils.SwipeToDeleteCallBack
import com.example.noteapp.viewmodel.NoteViewModel
import com.google.android.material.snackbar.Snackbar

class NoteFragment : Fragment() {
    private lateinit var binding: FragmentNoteBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var adapter: NoteAdapter

    private val noteViewModel: NoteViewModel by lazy {
        ViewModelProvider(this, NoteViewModel.NoteViewModelFactory(requireContext()))[NoteViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNoteBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initControls()
        val swipeToDeleteCallBack = object : SwipeToDeleteCallBack(){
            @SuppressLint("ResourceAsColor")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val note = adapter.mDiffer.currentList[position]
                noteViewModel.deleteNote(note)

                val snackBar = Snackbar.make(view, "Item Deleted", Snackbar.LENGTH_SHORT)
                snackBar.setAction("UNDO"){
                    noteViewModel.insertNote(note)
                }.show()

            }
        }
        ItemTouchHelper(swipeToDeleteCallBack).apply {
            attachToRecyclerView(binding.recyclerview)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initControls() {
        adapter = NoteAdapter(this.requireContext(), onItemClick)
        recyclerView = binding.recyclerview
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerview.layoutManager = linearLayoutManager
        gridLayoutManager = GridLayoutManager(context,2)
        binding.recyclerview.adapter = adapter
        noteViewModel.getAllNote().observe(viewLifecycleOwner, Observer {
            adapter.setNotes(it)
        })
        parentFragmentManager.setFragmentResultListener("noteSort",viewLifecycleOwner) { key, bundle ->
            val isAscending = bundle.getBoolean("bundleKey")

            if(isAscending){
                noteViewModel.getAllSortedCreatedTimeASC().observe(viewLifecycleOwner, Observer {
                    adapter.setNotes(it)
                })

            }
            else{
                noteViewModel.getAllSortedCreatedTimeDESC().observe(viewLifecycleOwner, Observer {
                    adapter.setNotes(it)
                })

            }

        }
        parentFragmentManager.setFragmentResultListener("noteLayoutManager", viewLifecycleOwner){key,bundle->
             val isLinearLayout = bundle.getBoolean("bundleKey")

            if(isLinearLayout)
                recyclerView.layoutManager = gridLayoutManager
            else
                recyclerView.layoutManager = linearLayoutManager
           adapter.notifyDataSetChanged()
        }

    }
    private val onItemClick: (Note) ->Unit = {
        val direction = HomeFragmentDirections.actionHomeFragmentToShowNoteFragment(it)
        findNavController().navigate(direction)
    }



}