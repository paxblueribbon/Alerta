package me.paxana.cwnet.UI;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import me.paxana.cwnet.Adapters.JsonAdapter;
import me.paxana.cwnet.Adapters.SearchAdapter;
import me.paxana.cwnet.Model.Movie;
import me.paxana.cwnet.Model.Result;
import me.paxana.cwnet.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static me.paxana.cwnet.Adapters.JsonAdapter.jsonData;

public class MainActivity extends AppCompatActivity {

    Button searchButton;

    private EditText mMovieTitle;
    private ListView mListView;
    private TextView mGreetingsTextView;

    FirebaseAuth mFirebaseAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();

        searchButton = (Button) findViewById(R.id.searchButton);
        mMovieTitle = (EditText) findViewById(R.id.movieTitle);
        mListView = (ListView) findViewById(R.id.resultsList);
        mGreetingsTextView = (TextView) findViewById(R.id.greetingsTextView);
        user = mFirebaseAuth.getCurrentUser();

        setGreetText();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = mMovieTitle.getText().toString().trim();
                try {
                    title = URLEncoder.encode(title, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                try {
                    getResults(title);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Toast toast = Toast.makeText(getApplicationContext(), title, Toast.LENGTH_LONG);
                toast.show();
            }
        });

    }

    public void setGreetText(){
        if (mFirebaseAuth.getCurrentUser() != null) {
            mGreetingsTextView.setText(getString(R.string.Greetings_message, user.getDisplayName()));

        }

        else {
            mGreetingsTextView.setText("No user is currently logged in");
        }
    }

    private void getResults(String searchParam) throws JSONException {
        String apiKey = "face7189";
        String theURL = "http://www.omdbapi.com/?s=" + searchParam + "&apikey=" + apiKey;
        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(theURL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String jsonData = response.body().string();

                JSONObject results = null;

                try {
                    results = new JSONObject(jsonData);
                    JSONArray data = results.getJSONArray("Search");
                    final Movie[] searchResults = new Movie[data.length()];
                    if (data != null) {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jsonResult = data.getJSONObject(i);
                            final Movie movie = new Movie();

                            movie.setTitle(jsonResult.getString("Title"));
                            movie.setYear(jsonResult.getString("Year"));
                            movie.setPosterURL(jsonResult.getString("Poster"));
                            movie.setImdbID(jsonResult.getString("imdbID"));

                            searchResults[i] = movie;
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
                                            startActivity(intent);

                                        }
                                    });
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signUp:
                Intent intent = new Intent(this, SignupActivity.class);
                startActivity(intent);
                break;

            case R.id.signIn:
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivity(loginIntent);
                break;

            case R.id.logOut:
                mFirebaseAuth.signOut();
                setGreetText();
                break;
        }
        return super.onOptionsItemSelected(item);
    };

}
