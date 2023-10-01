package com.example.geodo;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TaskSharedPreferences {
    private String login = ParseUser.getCurrentUser().getUsername();
    private final String NOTE_PREFS = "note_prefs"+login;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public TaskSharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(NOTE_PREFS, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // save a list of notes
    public void saveNotes(List<Model> notes) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Model note : notes) {
            String jsonNote = gson.toJson(note);
            editor.putString(note.getId(), jsonNote);
        }
        editor.apply();
    }

    public void saveNote(Model note) {
        String jsonNote = gson.toJson(note);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(note.getId(), jsonNote);
        editor.apply();
    }

    public Model getNoteById(String id) {
        String jsonNote = sharedPreferences.getString(id, "");
        if (jsonNote.isEmpty()) {
            return null;
        }
        return gson.fromJson(jsonNote, Model.class);
    }

    public List<Model> getAllNotes() {
        Map<String, ?> allEntries = sharedPreferences.getAll();
        List<Model> notes = new ArrayList<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String jsonNote = entry.getValue().toString();
            Model note = gson.fromJson(jsonNote, Model.class);
            notes.add(note);
        }
        return notes;
    }

    public void deleteNoteById(String id) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(id);
        editor.apply();
    }

}
