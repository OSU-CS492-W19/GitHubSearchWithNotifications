package com.example.android.githubsearch;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.example.android.githubsearch.workers.CheckRepoStarsWorker;

import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class App extends Application {

    private static final String CHECK_STARS_WORK_NAME = "checkStars";

    @Override
    public void onCreate() {
        super.onCreate();
        startCheckStarsWorker();
        createStarsNotificationChannel();
    }

    private void startCheckStarsWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(CheckRepoStarsWorker.class,
                15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance()
                .enqueueUniquePeriodicWork(CHECK_STARS_WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }

    private void createStarsNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    getString(R.string.stars_notification_channel),
                    getString(R.string.stars_notification_channel_title),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
