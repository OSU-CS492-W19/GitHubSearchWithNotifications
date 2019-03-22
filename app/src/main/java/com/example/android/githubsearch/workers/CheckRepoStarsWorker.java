package com.example.android.githubsearch.workers;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.githubsearch.R;
import com.example.android.githubsearch.RepoDetailActivity;
import com.example.android.githubsearch.SavedReposActivity;
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

    private static final String STARS_NOTIFICATION_GROUP = "starsNotificationGroup";
    private static final int STARS_SUMMARY_ID = 0;

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

    private void sendNotifications(@NonNull List<GitHubRepo> repos) {
        Context context = getApplicationContext();

        for (GitHubRepo repo : repos) {
            sendIndividualNotification(repo, context);
        }

        if (repos.size() > 1) {
            sendSummaryNotification(repos, context);
        }
    }

    private void sendIndividualNotification(GitHubRepo repo, Context context) {
        Intent intent = new Intent(context, RepoDetailActivity.class);
        intent.putExtra(RepoDetailActivity.EXTRA_GITHUB_REPO, repo);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                context.getString(R.string.stars_notification_channel));
        builder.setSmallIcon(R.drawable.ic_github)
                .setContentTitle(context.getString(R.string.stars_notification_title, repo.full_name))
                .setContentText(context.getString(R.string.stars_notification_text, repo.full_name,
                        repo.stargazers_count))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroup(STARS_NOTIFICATION_GROUP)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(repo.full_name.hashCode(), builder.build());
    }

    private void sendSummaryNotification(List<GitHubRepo> repos, Context context) {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        ArrayList<String> repoNames = new ArrayList<>();
        for (GitHubRepo repo : repos) {
            inboxStyle.addLine(context.getString(R.string.stars_notification_text, repo.full_name, repo.stargazers_count));
            repoNames.add(repo.full_name);
        }

        Intent intent = new Intent(context, SavedReposActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                context.getString(R.string.stars_notification_channel));
        builder.setSmallIcon(R.drawable.ic_github)
                .setContentTitle(context.getString(R.string.stars_notification_summary_title, repos.size()))
                .setContentText(TextUtils.join(", ", repoNames))
                .setStyle(inboxStyle)
                .setGroup(STARS_NOTIFICATION_GROUP)
                .setGroupSummary(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(STARS_SUMMARY_ID, builder.build());
    }
}
