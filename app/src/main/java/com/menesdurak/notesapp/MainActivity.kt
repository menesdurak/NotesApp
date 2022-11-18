package com.menesdurak.notesapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Note
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.room.RoomDatabase
import com.menesdurak.notesapp.adapter.NotesAdapter
import com.menesdurak.notesapp.database.NoteDatabase
import com.menesdurak.notesapp.databinding.ActivityMainBinding
import com.menesdurak.notesapp.models.NoteViewModel

class MainActivity : AppCompatActivity(), NotesAdapter.NotesClickListener, PopupMenu.OnMenuItemClickListener{

    private lateinit var binding: ActivityMainBinding
    private lateinit var dataBase: RoomDatabase
    lateinit var viewModel: NoteViewModel
    lateinit var adapter: NotesAdapter
    lateinit var selectedNote: com.menesdurak.notesapp.models.Note

    private val updateNote = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == Activity.RESULT_OK) {
            val note = result.data?.getSerializableExtra("note") as?
                    com.menesdurak.notesapp.models.Note
            if(note != null) {
                viewModel.updateNote(note)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initializing UI
        initUI()

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider
                .AndroidViewModelFactory
                .getInstance(application)
        ).get(NoteViewModel::class.java)

        viewModel.allnotes.observe(this) { list ->
            list?.let {
                adapter.updateList(list)
            }
        }

        dataBase = NoteDatabase.getDatabase(this)

    }

    private fun initUI() {
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(
            2,
            LinearLayoutManager.VERTICAL
        )
        adapter = NotesAdapter(this, this)
        binding.recyclerView.adapter = adapter
        val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val note = result.data?.getSerializableExtra("note") as?
                        com.menesdurak.notesapp.models.Note
                if (note != null) {
                    viewModel.insertNote(note)
                }
            }

        }

        binding.fbAddNote.setOnClickListener {
            val intent = Intent(this, AddNote::class.java)
            getContent.launch(intent)
        }

        binding.searchView.setOnQueryTextListener(
            object: android.widget.SearchView.OnQueryTextListener
             {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText != null) {
                    adapter.filterList(newText)
                }
                return true
            }

        })
    }

    override fun onItemClicked(note: com.menesdurak.notesapp.models.Note) {
        val intent = Intent(this@MainActivity, AddNote::class.java)
        intent.putExtra("current_note", note)
        updateNote.launch(intent)
    }

    override fun onLongItemClicked(note: com.menesdurak.notesapp.models.Note, cardView: CardView) {
        selectedNote = note
        popUpDisplay(cardView)
    }

    private fun popUpDisplay(cardView: CardView) {
        val popup = PopupMenu(this, cardView)
        popup.setOnMenuItemClickListener(this@MainActivity)
        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.delete_note) {
            viewModel.deleteNote(selectedNote)
            return true
        }
        return false
    }
}