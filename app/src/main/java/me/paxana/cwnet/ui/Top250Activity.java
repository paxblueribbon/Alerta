package me.paxana.cwnet.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;

import me.paxana.cwnet.Adapters.SearchAdapter;
import me.paxana.cwnet.Model.Movie;
import me.paxana.cwnet.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Top250Activity extends AppCompatActivity {

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top250);

        mListView = findViewById(R.id.mylist);

        try {
            getResults();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getResults() throws JSONException {
        final String apiKey = "face7189";
        String theURL = "https://api.morph.io/btrav528/imdb-top-250/data.json?key=XYSXdz26OFIZ9vy5m312&query=select%20*%20from%20%22data%22%20limit%2010";
        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(theURL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String jsonData = response.body().string();

                JSONObject results;

                try {
                    JSONArray array = new JSONArray(jsonData);
                    final Movie[] searchResults = new Movie[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonResult = array.getJSONObject(i);
                        final Movie movie = new Movie();

                        movie.setTitle(jsonResult.getString("name"));
                        movie.setYear(jsonResult.getString("year"));
                        Uri uri = Uri.parse(jsonResult.getString("link"));
                        String path = uri.getPath();
                        Log.d("taggie", path);
                        String[] segments = uri.getPath().split("/");
                        String idStr = segments[segments.length-1];
                        Log.d("taggieidstr", idStr);

                        String posterUrl = "http://img.omdbapi.com/?i=" + idStr + "&h=600&apikey=" + apiKey;

                        movie.setPosterURL(posterUrl);
                        movie.setImdbID(idStr);

                        searchResults[i] = movie;
                    }
                        final SearchAdapter adapter = new SearchAdapter(getApplicationContext(), searchResults);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mListView.setAdapter(adapter);

                                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        Intent intent = new Intent(getApplicationContext(), MovieActivity.class);
                                        intent.putExtra("ID_KEY", searchResults[i].getImdbID());
                                        intent.putExtra("TITLE_KEY", searchResults[i].getTitle());
                                        intent.putExtra("YEAR_KEY", searchResults[i].getYear());
                                        intent.putExtra("POSTER_KEY", searchResults[i].getPosterURL());
                                        startActivity(intent);
                                    }
                                });
                            }
                        });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
