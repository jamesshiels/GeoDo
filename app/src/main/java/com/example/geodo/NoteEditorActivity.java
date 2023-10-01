package com.example.geodo;

import static java.lang.String.valueOf;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geodo.ml.LiteModelObjectDetectionMobileObjectLabelerV11;
import com.parse.ParseUser;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class NoteEditorActivity extends AppCompatActivity {
    int noteID;
    int intentNoteID;
    private TaskSharedPreferences preferences;
    boolean editTitle, editDescription;
    private Model model;
    private EditText title;
    private EditText multiLine;
    ImageView imageView;
    public static List<String> labels;
    Button addImage;
    private String filePath;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.note_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.list_item, MainActivity.notesTitles);
        preferences = new TaskSharedPreferences(this);

        MainActivity.notesTitles.set(intentNoteID, title.getText().toString());
        MainActivity.notesDescriptions.set(intentNoteID, multiLine.getText().toString());
        MainActivity.arrayAdapter.notifyDataSetChanged();
        model.setTitle(title.getText().toString());
        model.setDescription(multiLine.getText().toString());
        model.setId(valueOf(intentNoteID));
        preferences.saveNote(model);

        if(item.getItemId() == R.id.delete_note){
                    final int noteToDelete = intentNoteID;
                    new AlertDialog.Builder(NoteEditorActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Are you sure?")
                            .setMessage("Do you want to delete this note?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Delete note from array lists
                                    MainActivity.notesTitles.remove(noteToDelete);
                                    MainActivity.notesDescriptions.remove(noteToDelete);
                                    arrayAdapter.notifyDataSetChanged();

                                    // Delete note from back4app list and later from database
                                    preferences.deleteNoteById(valueOf(noteToDelete));

                                    // Push user back up to Main Activity, else app will crash.
                                    Intent intent1 = new Intent(NoteEditorActivity.this, MainActivity.class);
                                    startActivity(intent1);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();

                    return true;
        }

        else if(item.getItemId() == R.id.create_geofence){

            Intent intent1 = new Intent(NoteEditorActivity.this, MapsActivity.class);
            System.out.println("Note editor in create geofence Note ID: " + intentNoteID);
            intent1.putExtra("noteID" , intentNoteID);
            startActivity(intent1);
            return true;
        }

        else if(item.getItemId() == R.id.set_date){
            // TODO: Add date picker
            @SuppressLint({"NewApi", "LocalSuppress"})
            DatePickerDialog datePickerDialog = new DatePickerDialog(this);
            datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(android.widget.DatePicker datePicker, int year, int month, int day) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, day);
                    Date date = calendar.getTime();
                    model.setDueDate(date);
                    preferences.saveNote(model);
                    System.out.println("Date set: " + date.toString());
                }
            });
            datePickerDialog.show();
            return true;
        }

        else if(item.getItemId() == R.id.set_picture){
            showImageOptions();
            // Call fragment and display List of labels

        }
        else if(item.getItemId() == R.id.save_note){
            Intent intent1 = new Intent(NoteEditorActivity.this, MainActivity.class);
            startActivity(intent1);
        }
        return false;
    }

    public String getPathFromUri(Uri uri) {
        String uriFilePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            uriFilePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return uriFilePath;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);
        preferences = new TaskSharedPreferences(this);
        model = new Model();
        imageView = findViewById(R.id.imageView);
        labels = new ArrayList<String>();
        addImage = findViewById(R.id.buttonAddImage);

        Intent intent = getIntent();
        intentNoteID = intent.getIntExtra("noteID", -1);
        System.out.println("Note editor intent note ID: " + intentNoteID);

        title = findViewById(R.id.editTextTextTitle);
        multiLine = findViewById(R.id.editTextMultiLine);

        // save image
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageOptions();
            }
        });

        // Note exists just call it
        if (intentNoteID != -1) {

            model = preferences.getNoteById(valueOf(intentNoteID));
            multiLine.setText(model.getDescription());
            title.setText(model.getTitle());
            filePath = model.getImage();
            if(filePath != null){
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                imageView.setImageBitmap(bitmap);
            }

        // Note does not exist, create new note
        }else{
            MainActivity.notesTitles.add("");
            MainActivity.notesDescriptions.add("");
            intentNoteID = MainActivity.notesTitles.size() -1;
            MainActivity.arrayAdapter.notifyDataSetChanged();
            model.setId(valueOf(intentNoteID));
            model.setObjectId(valueOf(intentNoteID));
            model.setUserName(ParseUser.getCurrentUser().getUsername());
            model.setImage(filePath);
            preferences.saveNote(model);

            // Update fields in back4app
//            ParseObject note = new ParseObject("Notes");
//            note.put("Id", valueOf(intentNoteID));
//            note.put("title", model.getTitle());
//            note.put("description", model.getDescription());
//            note.put("username", model.getUserName());
//            note.saveInBackground(new SaveCallback() {
//                @Override
//                public void done(ParseException e) {
//                    if(e == null){
//                        System.out.println("Note saved successfully");
//                    }else{
//                        Log.d("NoteEditorActivity", "Error while saving note: " + e.toString());
//                        System.out.println("Note save failed");
//                    }
//                }
//            });

        }

            title.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (editTitle) {
                        MainActivity.notesTitles.set(intentNoteID, String.valueOf(charSequence));
                        MainActivity.arrayAdapter.notifyDataSetChanged();

                        model.setTitle(String.valueOf(charSequence));
                        preferences.saveNote(model);
//                        ParseQuery<ParseObject> noteQuery = ParseQuery.getQuery("Notes");
//                        noteQuery.whereEqualTo("Id", valueOf(intentNoteID));
//                        noteQuery.getFirstInBackground(new GetCallback<ParseObject>() {
//                            @Override
//                            public void done(ParseObject object, ParseException e) {
//                                if(e == null && object != null) {
//                                    object.put("title", model.getTitle());
//                                    object.saveInBackground();
//                                }else{
//                                    System.out.println("Error while updating note: " + e.toString());
//                                }
//
//                            }
//                        });
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    // Close text watcher
                }
            });

            multiLine.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (editDescription) {
                        MainActivity.notesDescriptions.set(intentNoteID, String.valueOf(charSequence));
                        MainActivity.arrayAdapter.notifyDataSetChanged();

                        model.setDescription(String.valueOf(charSequence));
                        preferences.saveNote(model);
//                        ParseQuery<ParseObject> noteQuery = ParseQuery.getQuery("Notes");
//                        noteQuery.whereEqualTo("Id", valueOf(intentNoteID));
//                        noteQuery.getFirstInBackground(new GetCallback<ParseObject>() {
//                            @Override
//                            public void done(ParseObject object, ParseException e) {
//                                if(e == null && object != null) {
//                                    object.put("description", model.getDescription());
//                                    object.saveInBackground();
//                                }else{
//                                    System.out.println("Error while updating note: " + e.toString());
//                                }
//
//                            }
//                        });
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    // Close text watcher

                }
            });

            // Set focus change listener
            title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    editTitle = b;
                }
            });
            multiLine.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    editDescription = b;
                }
            });
    }

    // Show image options
    private void showImageOptions(){
        final String[] options = {"Take a picture", "Choose from gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i){
                    case 0:
                        // Take a picture
                        capturePictureFromCamera();
                        break;
                    case 1:
                        // Choose from gallery
                        choosePictureFromGallery();
                        break;
                }
            }
        });
        builder.show();
    }

    // Capture picture from camera
    private void capturePictureFromCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            cameraResultLauncher.launch(intent);
        }
        else{
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    // Call back for camera
    ActivityResultLauncher<Intent> cameraResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == RESULT_OK && result.getData() != null){
                Bundle data = result.getData().getExtras();
                Bitmap bitmap = (Bitmap) data.get("data");
                imageView.setImageBitmap(bitmap);
            }
        }
    });

    // Call back for gallery
    private void choosePictureFromGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        if(galleryIntent.resolveActivity(getPackageManager()) != null){
            Toast.makeText(this, "Gallery app found", Toast.LENGTH_SHORT).show();

            galleryResultLauncher.launch(galleryIntent);
        }
        else{
            Toast.makeText(this, "No gallery app found", Toast.LENGTH_SHORT).show();
        }
    }

    // Call back for gallery
    ActivityResultLauncher<Intent> galleryResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == RESULT_OK && result.getData() != null){
                // Set image
                Uri imageUri = result.getData().getData();
                // convert uri to bitmap
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Set image
                filePath = getPathFromUri(imageUri);
                imageView.setImageURI(imageUri);
                System.out.println("File path: " + filePath);

                model = preferences.getNoteById(valueOf(intentNoteID));
                model.setImage(filePath);
                preferences.saveNote(model);
                suggestCategoryForPicture(bitmap);
            }
        }
    });

    private void suggestCategoryForPicture(Bitmap bitmap) {
        if (SettingsActivity.tensorFlowEnabled == true) {
            try {
                LiteModelObjectDetectionMobileObjectLabelerV11 model = LiteModelObjectDetectionMobileObjectLabelerV11.newInstance(this);

                // Creates inputs for reference.
                TensorImage image = TensorImage.fromBitmap(bitmap);

                // Runs model inference and gets result.
                LiteModelObjectDetectionMobileObjectLabelerV11.Outputs outputs = model.process(image);
                List<Category> probability = outputs.getProbabilityAsCategoryList();
                probability.sort(Comparator.comparing(Category::getScore, Comparator.reverseOrder()));
                for (int i = 0; i < 4; i++) {
                    System.out.println(probability.get(i).getLabel());
                    labels.add(probability.get(i).getLabel());
                }
                // Releases model resources if no longer used.
                model.close();
            } catch (IOException e) {
                // TODO Handle the exception
            }
            //
            showImageLabelOptions();

        }
    }

    private void showImageLabelOptions(){
        final String[] options = {labels.get(0), labels.get(1), labels.get(2), labels.get(3), "None of the above"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an option, it will become the title of the Note.");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i){
                    case 0:
                        title.setText(labels.get(0));
                        break;
                    case 1:
                        title.setText(labels.get(1));
                        break;
                    case 2:
                        title.setText(labels.get(2));
                        break;
                    case 3:
                        title.setText(labels.get(3));
                        break;
                    case 4:
                        // Doesnt want any of the options, do nothing
                        break;
                }
                MainActivity.notesTitles.set(intentNoteID, title.getText().toString());
                MainActivity.arrayAdapter.notifyDataSetChanged();

                model = preferences.getNoteById(valueOf(intentNoteID));
                model.setTitle(title.getText().toString());
                preferences.saveNote(model);
            }
        });
        builder.show();

    }
}