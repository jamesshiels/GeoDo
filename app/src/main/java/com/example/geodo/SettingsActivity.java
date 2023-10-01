package com.example.geodo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

public class SettingsActivity extends AppCompatActivity {
    EditText geofenceRadius;
    Button buttonSave;
    ToggleButton toggleButton;
    public static Boolean tensorFlowEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        geofenceRadius = findViewById(R.id.editTextGeofenceRadius);
        buttonSave = findViewById(R.id.buttonSaveChanges);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Float.parseFloat(geofenceRadius.getText().toString()) == 0){
                    geofenceRadius.setText("150");
                }
                MapsActivity.GEOFENCE_RADIUS = Float.parseFloat(geofenceRadius.getText().toString());
            }
        });

        toggleButton = findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(toggleButton.isChecked()){
                    tensorFlowEnabled = true;
                }
                else{
                    tensorFlowEnabled = false;
                }
            }
        });
    }
}