package com.example.android.githubsearch;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.githubsearch.data.GitHubRepo;
import com.example.android.githubsearch.data.Status;
import com.example.android.githubsearch.utils.GitHubUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements GitHubSearchAdapter.OnSearchItemClickListener,
            NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mSearchResultsRV;
    private EditText mSearchBoxET;
    private TextView mLoadingErrorTV;
    private ProgressBar mLoadingPB;
    private DrawerLayout mDrawerLayout;

    private GitHubSearchAdapter mGitHubSearchAdapter;
    private GitHubSearchViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchBoxET = findViewById(R.id.et_search_box);
        mSearchResultsRV = findViewById(R.id.rv_search_results);
        mLoadingErrorTV = findViewById(R.id.tv_loading_error);
        mLoadingPB = findViewById(R.id.pb_loading);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nv_nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_nav_menu);

        mSearchResultsRV.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultsRV.setHasFixedSize(true);

        mGitHubSearchAdapter = new GitHubSearchAdapter(this);
        mSearchResultsRV.setAdapter(mGitHubSearchAdapter);

        mViewModel = ViewModelProviders.of(this).get(GitHubSearchViewModel.class);

        mViewModel.getSearchResults().observe(this, new Observer<List<GitHubRepo>>() {
            @Override
            public void onChanged(@Nullable List<GitHubRepo> gitHubRepos) {
                mGitHubSearchAdapter.updateSearchResults(gitHubRepos);
            }
        });

        mViewModel.getLoadingStatus().observe(this, new Observer<Status>() {
            @Override
            public void onChanged(@Nullable Status status) {
                if (status == Status.LOADING) {
                    mLoadingPB.setVisibility(View.VISIBLE);
                } else if (status == Status.SUCCESS) {
                    mLoadingPB.setVisibility(View.INVISIBLE);
                    mSearchResultsRV.setVisibility(View.VISIBLE);
                    mLoadingErrorTV.setVisibility(View.INVISIBLE);
                } else {
                    mLoadingPB.setVisibility(View.INVISIBLE);
                    mSearchResultsRV.setVisibility(View.INVISIBLE);
                    mLoadingErrorTV.setVisibility(View.VISIBLE);
                }
            }
        });

        Button searchButton = findViewById(R.id.btn_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = mSearchBoxET.getText().toString();
                if (!TextUtils.isEmpty(searchQuery)) {
                    doGitHubSearch(searchQuery);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(Gravity.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void doGitHubSearch(String query) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sort = preferences.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));
        String language = preferences.getString(getString(R.string.pref_language_key),
                getString(R.string.pref_language_default));
        String user = preferences.getString(getString(R.string.pref_user_key),"");
        boolean searchInName = preferences.getBoolean(getString(R.string.pref_in_name_key), true);
        boolean searchInDescription = preferences.getBoolean(getString(R.string.pref_in_description_key), true);
        boolean searchInReadme = preferences.getBoolean(getString(R.string.pref_in_readme_key), false);

        mViewModel.loadSearchResults(query, sort, language, user, searchInName, searchInDescription,
                searchInReadme);
    }

    @Override
    public void onSearchItemClick(GitHubRepo repo) {
        Intent intent = new Intent(this, RepoDetailActivity.class);
        intent.putExtra(GitHubUtils.EXTRA_GITHUB_REPO, repo);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        mDrawerLayout.closeDrawers();
        switch (menuItem.getItemId()) {
            case R.id.nav_search:
                return true;
            case R.id.nav_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.nav_saved_repos:
                Intent savedReposIntent = new Intent(this, SavedReposActivity.class);
                startActivity(savedReposIntent);
                return true;
            default:
                return false;
        }
    }
}