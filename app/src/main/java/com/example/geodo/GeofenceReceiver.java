package com.example.geodo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceReceiver extends BroadcastReceiver {
    private Model note;
    private String title;
    private String description;
    private static final String TAG = "GeofenceReceiver";
    TaskSharedPreferences preferences;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        NotificationUtility notificationUtility = new NotificationUtility(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        preferences = new TaskSharedPreferences(context);

        // Check if the geofence event has any error
        if(geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event..." + geofencingEvent.getErrorCode());
            System.out.println("inside if error in geofencing event");
            return;
        }

        // Get the geofence that were triggered
        List<Geofence> geoFenceList = geofencingEvent.getTriggeringGeofences();
        for(Geofence geofence : geoFenceList){
            Log.d(TAG, "onReceive: " + geofence.getRequestId());
            System.out.println("inside for loop" + geofence.getRequestId());
            setTitleAndDescription(geofence.getRequestId());
        }


        // Location location = geofencingEvent.getTriggeringLocation();
        int transitionType = geofencingEvent.getGeofenceTransition();
        switch (transitionType){
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                // Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                notificationUtility.sendNotifications(title, description, MainActivity.class);
                System.out.println("Inside switch for notificaions");
//            case Geofence.GEOFENCE_TRANSITION_DWELL:
//                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show();
//                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_DWELL", "", MapsActivity.class);
//
//            case Geofence.GEOFENCE_TRANSITION_EXIT:
//                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
//                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_EXIT", "", MapsActivity.class);
            default:
                System.out.println("Error in GeofenceReceiver");
                break;
        }
    }

    // Get the title and description of the note
    public void setTitleAndDescription(String geofenceId) {
        note = preferences.getNoteById(geofenceId);
        if (note != null) {
        title = note.getTitle();
        description = note.getDescription();
        }
    }
}