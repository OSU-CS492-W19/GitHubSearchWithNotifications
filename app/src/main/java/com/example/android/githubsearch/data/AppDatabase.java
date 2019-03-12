package com.example.android.githubsearch.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {GitHubRepo.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    /*
                     * Note: the call to fallbackToDestructiveMigration() below will destroy old
                     * versions of the database instead of migrating them properly.  Be careful
                     * about using this in a real app, since it will delete all data the user
                     * might have saved in the database.
                     */
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                AppDatabase.class, "github_repos_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract GitHubRepoDao gitHubRepoDao();
}
