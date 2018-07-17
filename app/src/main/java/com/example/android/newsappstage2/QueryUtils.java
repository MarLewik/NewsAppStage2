package com.example.android.newsappstage2;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
public final class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();


    private QueryUtils() {
    }


    public static List<News> fetchNewsData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Earthquake}s
        List<News> newses = extractFeatureFromJson(jsonResponse);

        // Return the list of {@link Earthquake}s
        return newses;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link News} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<News> extractFeatureFromJson(String newsesJSON) {
        // If the JSON string is empty or null, then return early.
        ArrayList<News> NewsToday = new ArrayList<>();
        if (TextUtils.isEmpty(newsesJSON)) {
            return null;
        }


        List<News> newses = new ArrayList<>();


        try {

            JSONObject baseJsonResponse = new JSONObject(newsesJSON);

            JSONObject NewsResponse = baseJsonResponse.getJSONObject("response");

            JSONArray newsArray = NewsResponse.getJSONArray("results");


            for (int i = 0; i < newsArray.length(); i++) {

                String thumbnail = "";
                String contributor = "";


                JSONObject currentNews = newsArray.getJSONObject(i);


                Log.d("myTag", currentNews.toString());
                JSONObject properties = currentNews;


                String sectionName = properties.getString("sectionName");


                String webTitle = properties.getString("webTitle");


                String DateTime = properties.getString("webPublicationDate");


                String webUrl = properties.getString("webUrl");

                if (properties.has("tags")) {
                    JSONArray tagsArray = properties.getJSONArray("tags");
                    if (tagsArray.length() != 0) {
                        JSONObject tags = tagsArray.getJSONObject(0);
                        if (tags.has("webTitle")) {
                            contributor = tags.getString("webTitle");
                        }
                    }
                }

                if (properties.has("fields")) {
                    JSONObject fields = currentNews.getJSONObject("fields");
                    if (fields.has("thumbnail")) {
                        thumbnail = fields.getString("thumbnail");
                    }
                }
                News NewNews = new News(webTitle, sectionName, webUrl, thumbnail, DateTime, contributor);
                NewsToday.add(NewNews);
            }

        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        return NewsToday;
    }

}

