package com.example.android.newsappstage2;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

import static com.example.android.newsappstage2.R.id.mainList;

public class MainActivity extends AppCompatActivity implements LoaderCallbacks<ArrayList<News>> {

    // Great Udacity reviewer You can try by this to check what app will show when api key will be wrong :)
    //private static final String REQUEST_URL = "https://content.guardianapis.com/search?&api-key=ab41e155-346a-41c0-bda2-4e561e90991f&show-fields=thumbnail&show-tags=contributor";
    private static final String REQUEST_URL = "https://content.guardianapis.com/search?&api-key=6b41e155-346a-41c0-bda2-4e561e90991f&show-fields=thumbnail&show-tags=contributor";
    private static final int NEWS_ID = 1;
    private ImageView mEmptyStateTextView;
    private NewsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity_main);

        ListView NewsListView = (ListView) findViewById(mainList);

        mAdapter = new NewsAdapter(this, new ArrayList<News>());
        NewsListView.setAdapter(mAdapter);

        mEmptyStateTextView = findViewById(R.id.emptyView);
        NewsListView.setEmptyView(mEmptyStateTextView);

        NewsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                News currentNews = mAdapter.getItem(i);
                Uri NewsUri = Uri.parse(currentNews.getmUrl());
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, NewsUri);
                startActivity(websiteIntent);
            }
        });
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWS_ID, null, this);
        } else {
            View loadingIndicator = findViewById(R.id.loadingIndicator);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setImageResource(R.drawable.noconnection);
        }
    }

    @Override
    // This method initialize the contents of the Activity's options menu.
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the Options Menu we specified in XML
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<ArrayList<News>> onCreateLoader(int i, Bundle bundle) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String Search = sharedPrefs.getString(getString(R.string.settings_search_key), getString(R.string.settings_Search_default));
        String orderBy = sharedPrefs.getString(getString(R.string.settings_order_by_key), getString(R.string.settings_order_by_default));
        String section = sharedPrefs.getString(getString(R.string.settings_section_key), getString(R.string.settings_section_default));


        Uri baseUri = Uri.parse(REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();


        if (!Search.equals("")) {
            uriBuilder.appendQueryParameter("q", Search);
            orderBy = getString(R.string.settings_order_by_relevance_value);
        }
        if (Search.equals("") && orderBy.equals(getString(R.string.settings_order_by_relevance_value))) {
            orderBy = getString(R.string.settings_order_by_newest_value);
        }
        if (!section.equals("")) {
            if (!section.equals(getString(R.string.settings_section_default_value))) {
                uriBuilder.appendQueryParameter("section", section);
            }
        }

        uriBuilder.appendQueryParameter("order-by", orderBy);

        Log.d("myTag", "Uri string" + uriBuilder.toString());
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<News>> loader, ArrayList<News> news) {
        mAdapter.clear();
        if (news != null && !news.isEmpty()) {
            mAdapter.addAll(news);
        }
        mEmptyStateTextView.setImageResource(R.drawable.nocontent);

        View loadingIndicator = findViewById(R.id.loadingIndicator);
        loadingIndicator.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<News>> loader) {
        mAdapter.clear();
    }
}
