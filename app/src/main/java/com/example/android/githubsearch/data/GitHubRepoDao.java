package com.example.android.githubsearch.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface GitHubRepoDao {
    @Insert
    void insert(GitHubRepo repo);

    @Delete
    void delete(GitHubRepo repo);

    @Update
    void update(GitHubRepo repo);

    @Query("SELECT * FROM repos")
    LiveData<List<GitHubRepo>> getAllRepos();

    @Query("SELECT * FROM repos")
    List<GitHubRepo> getAllReposSync();

    @Query("SELECT * FROM repos WHERE full_name = :fullName LIMIT 1")
    LiveData<GitHubRepo> getRepoByName(String fullName);
}
