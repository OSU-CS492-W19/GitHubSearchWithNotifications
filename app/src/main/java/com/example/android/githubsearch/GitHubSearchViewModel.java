package com.example.android.githubsearch;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.githubsearch.data.GitHubRepo;
import com.example.android.githubsearch.data.GitHubSearchRepository;
import com.example.android.githubsearch.data.Status;

import java.util.List;

public class GitHubSearchViewModel extends ViewModel {
    private LiveData<List<GitHubRepo>> mSearchResults;
    private LiveData<Status> mLoadingStatus;
    private GitHubSearchRepository mRepository;

    public GitHubSearchViewModel() {
        mRepository = new GitHubSearchRepository();
        mSearchResults = mRepository.getSearchResults();
        mLoadingStatus = mRepository.getLoadingStatus();
    }

    public LiveData<List<GitHubRepo>> getSearchResults() {
        return mSearchResults;
    }

    public LiveData<Status> getLoadingStatus() {
        return mLoadingStatus;
    }

    public void loadSearchResults(String query, String sort, String language, String user,
                                  boolean searchInName, boolean searchInDescription,
                                  boolean searchInReadme) {
        mRepository.loadSearchResults(query, sort, language, user, searchInName,
                searchInDescription, searchInReadme);
    }
}
