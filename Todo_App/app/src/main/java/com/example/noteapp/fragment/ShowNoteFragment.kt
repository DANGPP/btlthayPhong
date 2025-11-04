package com.example.noteapp.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.noteapp.R
import com.example.noteapp.databinding.FragmentShowNoteBinding
import com.example.noteapp.viewmodel.NoteViewModel


class ShowNoteFragment : Fragment() {

    private lateinit var binding: FragmentShowNoteBinding
    private val arg: ShowNoteFragmentArgs by navArgs()
//    private val noteViewModel: NoteViewModel by lazy {
//        ViewModelProvider (this, NoteViewModel.NoteViewModelFactory(this.activity)
//        )[NoteViewModel::class.java]
//
//    }
    private lateinit var toolBar:Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_show_note,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShowNoteBinding.inflate(inflater, container,false)
        //toolBar
        toolBar = binding.toolbarShowNote
        (requireActivity() as AppCompatActivity?)!!.setSupportActionBar(toolBar)
        //
        binding.txtShowTitle.text = arg.note.title
        binding.txtShowDescription.text = arg.note.description
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfig = AppBarConfiguration(navController.graph)
        toolBar.setupWithNavController(navController,appBarConfig)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.edit){
            val direction = ShowNoteFragmentDirections.actionShowNoteFragmentToEditNoteFragment(arg.note)
            findNavController().navigate(direction)
        }
        return true
    }
}