package com.example.geodo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CheckNoteDueDate extends Service {

    // Check every hour
    private static final long CHECK_INTERVAL = 60 * 60 * 1000; // 1 hour
    private Timer timer;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkForDueDate();
            }
        }, 0, CHECK_INTERVAL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timer != null){
            timer.cancel();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkForDueDate() {
        TaskSharedPreferences preferences = new TaskSharedPreferences(this);
        List<Model> preferencesModelList = preferences.getAllNotes();
        for (Model note : preferencesModelList) {
            if (note.getDueDate() != null && note.getDueDate().before(new Date())) {
                NotificationUtility notificationUtility = new NotificationUtility(this);
                notificationUtility.sendNotifications("Task is overdue: " + note.getTitle(), note.getDescription(), MainActivity.class);
            }
        }
    }
}