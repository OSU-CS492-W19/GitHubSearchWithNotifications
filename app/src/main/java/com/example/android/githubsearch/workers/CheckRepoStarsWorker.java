package com.example.android.githubsearch.workers;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.android.githubsearch.R;
import com.example.android.githubsearch.data.GitHubRepo;
import com.example.android.githubsearch.data.GitHubRepoRepository;
import com.example.android.githubsearch.utils.GitHubUtils;
import com.example.android.githubsearch.utils.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class CheckRepoStarsWorker extends Worker {

    private static final String TAG = CheckRepoStarsWorker.class.getSimpleName();

    private GitHubRepoRepository mRepository;

    public CheckRepoStarsWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
        mRepository = new GitHubRepoRepository((Application)getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork() {
        List<GitHubRepo> savedRepos = mRepository.getAllGitHubReposSync();
        if (savedRepos != null) {
            ArrayList<GitHubRepo> updatedRepos = new ArrayList<>();
            for (GitHubRepo savedRepo : savedRepos) {
                String results = null;
                try {
                    results = NetworkUtils.doHTTPGet(savedRepo.url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (results != null) {
                    GitHubRepo updatedRepo = GitHubUtils.parseGitHubRepoResults(results);
                    Log.d(TAG, updatedRepo.full_name + " current stars: " + updatedRepo.stargazers_count);
                    if (updatedRepo.stargazers_count > savedRepo.stargazers_count) {
                        updatedRepos.add(updatedRepo);
                    }
                }
            }
            sendNotifications(updatedRepos);
        }
        return Result.success();
    }

    private void sendNotifications(List<GitHubRepo> repos) {
        Context context = getApplicationContext();
        for (GitHubRepo repo : repos) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                    context.getString(R.string.stars_notification_channel));

            builder.setSmallIcon(R.drawable.ic_star_border_black_24dp)
                    .setContentTitle(context.getString(R.string.stars_notification_title))
                    .setContentText(context.getString(R.string.stars_notification_text, repo.full_name,
                            repo.stargazers_count))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NotificationID.get(), builder.build());
        }
    }

    private static class NotificationID {
        private final static AtomicInteger ID = new AtomicInteger(0);
        static int get() {
            return ID.incrementAndGet();
        }
    }
}
