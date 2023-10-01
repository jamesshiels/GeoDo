package com.example.geodo;

import static com.parse.Parse.getApplicationContext;

import android.util.Log;

import com.parse.DeleteCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class Back4AppHelper {
    private static final String APPLICATION_ID = "s9VA28pb92as5jXxIRC16DwY4rX7qJvbptWB9T7d";
    private static final String CLIENT_KEY = "Dk8dwuQE49Uzz96CDdaCTHZmLxjS2a0vJaGzxQ79";
    private static final String BACK4APP_OBJECT_NAME = "Notes";

    private ParseObject noteObject;

    public Back4AppHelper() {
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId(APPLICATION_ID)
                .clientKey(CLIENT_KEY)
                .server("https://parseapi.back4app.com/")
                .build());

        noteObject = new ParseObject(BACK4APP_OBJECT_NAME);
    }

    // Update fields in back4app
    public void updateNoteInBack4App(Model model) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Notes");
        query.getInBackground(model.getId(), new com.parse.GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject noteObject, ParseException e) {
                if (e == null) {
                    System.out.println("Note found in Back4App");
                    noteObject.put("title", model.getTitle());
                    noteObject.put("description", model.getDescription());
                    noteObject.put("dueDate", model.getDueDate());
                    noteObject.put("latitude", model.getLatitude());
                    noteObject.put("longitude", model.getLongitude());
                    noteObject.put("image", model.getImage());
                    noteObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.i("UpdateNote", "Success");
                            } else {
                                Log.e("UpdateNoteError", e.getMessage());
                            }
                        }
                    });
                } else {
                    // Note not found, create new note
                    System.out.println("Note NOT found in Back4App");
                    ParseObject firstObject = new ParseObject("Notes");
                    firstObject.put("Id", model.getId());
                    firstObject.put("user", model.getUserName());
                    firstObject.put("title", model.getTitle());
                    firstObject.put("description", model.getDescription());
                    firstObject.put("latitude", model.getLatitude().toString());
                    firstObject.put("longitude", model.getLongitude().toString());
                    firstObject.put("date", model.getDueDate());
                    firstObject.put("image", model.getImage());

                    firstObject.saveInBackground(ex -> {
                        if(ex == null){
                            // Note was saved
                        }
                        else{
                            Log.e("Add Note Error: ", ex.getMessage());
                        }
                    });
                }
            }
        });
    }

    public void createOrUpdateNoteOnBack4App(final Model model) {
        noteObject.put("title", model.getTitle());
        noteObject.put("description", model.getDescription());
        if (model.getId() == null) {
            noteObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        model.setId(noteObject.getObjectId());
                    } else {
                        Log.e("CreateNoteError", e.getMessage());
                    }
                }
            });
        } else {
            noteObject.setObjectId(model.getId());
            noteObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e("UpdateNoteError", e.getMessage());
                    }
                }
            });
        }
    }

    public void deleteNoteOnBack4App(String noteId) {
        noteObject.setObjectId(noteId);
        noteObject.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e("DeleteNoteError", e.getMessage());
                }
            }
        });
    }
}
