package com.example.geodo;

import static java.lang.String.valueOf;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static List<Model> preferencesModelList;
    static ArrayList<String> notesTitles = new ArrayList<>();
    static ArrayList<String> notesDescriptions = new ArrayList<>();
    static ArrayAdapter arrayAdapter;
    Model note;
    TaskSharedPreferences preferences;
    public String noteId;
    public GridView gridView;
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] REQUIRED_PERMISSIONS = { Manifest.permission.READ_EXTERNAL_STORAGE };
    FloatingActionButton fabAdd;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.add_note){
            Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
            startActivity(intent);
            return true;
        }
        else if(item.getItemId() == R.id.settings){
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if(item.getItemId() == R.id.log_out){
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        // Start over due note service
        Intent serviceIntent = new Intent(this, CheckNoteDueDate.class);
        startService(serviceIntent);

        preferences = new TaskSharedPreferences(this);
        noteId = valueOf(preferences.getAllNotes().size());

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        String loginName = ParseUser.getCurrentUser().getUsername();

        preferencesModelList = preferences.getAllNotes();

        notesTitles.clear();
        notesDescriptions.clear();
        for(Model note : preferencesModelList){
            if(note == null) {
                break;
            }
            Back4AppHelper back4AppHelper = new Back4AppHelper();
            back4AppHelper.updateNoteInBack4App(note);
            String newNote = note.getTitle()+"\n"+"\n"+note.getDescription();
            notesTitles.add(newNote);
            notesDescriptions.add(note.getDescription());
        }

        gridView = (GridView) findViewById(R.id.gridView);
        arrayAdapter = new ArrayAdapter(this, R.layout.list_item, notesTitles);
        gridView.setAdapter(arrayAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                System.out.print("Main activity in onclick Note ID: " + i);
                intent.putExtra("noteID" , i);
                startActivity(intent);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                final int noteToDelete = i;

                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Are you sure?")
                        .setMessage("Do you want to delete this note?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Delete note from preferences
                                notesTitles.remove(noteToDelete);
                                notesDescriptions.remove(noteToDelete);
                                arrayAdapter.notifyDataSetChanged();
                                preferences.deleteNoteById(valueOf(noteToDelete));

                                // Delete note from Back4App
                                Back4AppHelper back4AppHelper = new Back4AppHelper();
                                back4AppHelper.deleteNoteOnBack4App(valueOf(noteToDelete));
                                List<Model> models = preferences.getAllNotes();
                                for(Model m : models){
                                    // turn string to int
                                    int id = Integer.parseInt(m.getId());
                                    if(id > noteToDelete){
                                        // decrement id
                                        id--;
                                        m.setId(valueOf(id));
                                        preferences.saveNote(m);
                                    }
                                    System.out.println("Note ID: " + m.getId());
                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

                return true;
            }
        });

        // Add note button
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                startActivity(intent);
            }
        });
    }

    //  Everytime the app is resumed, the notes are uploaded and updated
    public void uploadNotes(){
        List<Model> modelList = preferences.getAllNotes();
        for(Model b : modelList){
            Model c = b;
            ParseObject firstObject = new ParseObject("Notes");
            firstObject.put("Id", c.getId());
            firstObject.put("user", c.getUserName());
            firstObject.put("title", c.getTitle());
            firstObject.put("description", c.getDescription());
            firstObject.put("latitude", c.getLatitude().toString());
            firstObject.put("longitude", c.getLongitude().toString());
            firstObject.put("date", c.getDueDate());
            firstObject.put("image", c.getImage());

            firstObject.saveInBackground(e -> {
                if(e != null){
                    Toast.makeText(this, "Fail: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    

}