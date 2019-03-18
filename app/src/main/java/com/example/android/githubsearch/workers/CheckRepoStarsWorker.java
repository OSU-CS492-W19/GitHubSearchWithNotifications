package com.example.android.githubsearch.workers;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.android.githubsearch.R;
import com.example.android.githubsearch.RepoDetailActivity;
import com.example.android.githubsearch.data.GitHubRepo;
import com.example.android.githubsearch.data.GitHubRepoRepository;
import com.example.android.githubsearch.utils.GitHubUtils;
import com.example.android.githubsearch.utils.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

            builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(context.getString(R.string.stars_notification_title))
                    .setContentText(context.getString(R.string.stars_notification_text, repo.full_name,
                            repo.stargazers_count))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            Intent intent = new Intent(context, RepoDetailActivity.class);
            intent.putExtra(RepoDetailActivity.EXTRA_GITHUB_REPO, repo);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(intent);

            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(repo.full_name.hashCode(), builder.build());
        }
    }
}
