package com.example.android.githubsearch.data;

import android.os.AsyncTask;

import com.example.android.githubsearch.utils.GitHubUtils;
import com.example.android.githubsearch.utils.NetworkUtils;

import java.io.IOException;
import java.util.List;

public class GitHubSearchAsyncTask extends AsyncTask<Void, Void, String> {
    private String mURL;
    private Callback mCallback;

    public interface Callback {
        void onSearchFinished(List<GitHubRepo> searchResults);
    }

    public GitHubSearchAsyncTask(String url, Callback callback) {
        mURL = url;
        mCallback = callback;
    }

    @Override
    protected String doInBackground(Void... voids) {
        if (mURL != null) {
            String results = null;
            try {
                results = NetworkUtils.doHTTPGet(mURL);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        List<GitHubRepo> searchResults = null;
        if (s != null) {
            searchResults = GitHubUtils.parseGitHubSearchResults(s);
        }
        mCallback.onSearchFinished(searchResults);
    }
}
