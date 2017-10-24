package me.paxana.cwnet.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import me.paxana.cwnet.Adapters.SearchAdapter;
import me.paxana.cwnet.Model.Movie;
import me.paxana.cwnet.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    Button searchButton;

    private EditText mMovieTitle;
    private ListView mListView;
    private TextView mGreetingsTextView;
    private Menu mMenu;
    DatabaseReference adminDB;


    FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mFirebaseAuth.getCurrentUser();


    String mUserName;


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateOptionsMenu();

        searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setVisibility(View.INVISIBLE);
        mMovieTitle = (EditText) findViewById(R.id.movieTitle);
        mMovieTitle.setVisibility(View.INVISIBLE);
        mListView = (ListView) findViewById(R.id.resultsList);
        mGreetingsTextView = (TextView) findViewById(R.id.greetingsTextView);
        if (mFirebaseAuth.getCurrentUser() != null) {
            mUserName = mFirebaseAuth.getCurrentUser().getDisplayName();
        }

        mMovieTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    searchButton.performClick();
                    return true;
                }

                return false;
            }
        });

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

    private void animate(){

        int titleStartValue = mMovieTitle.getTop();
        int titleEndValue = mMovieTitle.getBottom();

        int buttonStartValue = searchButton.getTop();
        int buttonEndValue = searchButton.getBottom();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


        if (mMovieTitle.getVisibility() == View.INVISIBLE) {
            mMovieTitle.setVisibility(View.VISIBLE);

            ObjectAnimator.ofInt(mMovieTitle, "bottom", titleStartValue, titleEndValue).start();
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            searchButton.setVisibility(View.VISIBLE);
            ObjectAnimator.ofInt(searchButton, "bottom", buttonStartValue, buttonEndValue).start();
        }

        else {
            imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0);
            mMovieTitle.setVisibility(View.INVISIBLE);
            searchButton.setVisibility(View.INVISIBLE);
        }
    }

    public void setGreetText(){
        if (mFirebaseAuth.getCurrentUser() != null) {
            mGreetingsTextView.setText(getString(R.string.Greetings_message, mUser.getDisplayName()));
        }

        else {
            mGreetingsTextView.setText("No mUser is currently logged in");
        }
    }

    @Override
    protected void onResume() {
        updateOptionsMenu();
        super.onResume();
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
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String jsonData = response.body().string();

                JSONObject results = null;

                try {
                    results = new JSONObject(jsonData);
                    JSONArray data = results.getJSONArray("Search");
                    final Movie[] searchResults = new Movie[data.length()];
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
                                            intent.putExtra("TITLE_KEY", searchResults[i].getTitle());
                                            intent.putExtra("YEAR_KEY", searchResults[i].getYear());
                                            intent.putExtra("POSTER_KEY", searchResults[i].getPosterURL());
                                            startActivity(intent);

                                        }
                                    });
                                }
                            });
                        }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUser = mFirebaseAuth.getCurrentUser();
        if (mUser != null) {
            mUserName = mUser.getDisplayName();
        }
        toggleMenuLogState(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        updateOptionsMenu();
        toggleMenuLogState(menu);
        return true;
    }

    private void toggleMenuLogState(Menu menu) {
        mMenu = menu;

        menu.clear();
        if (mUser != null) {
            mUserName = mUser.getDisplayName();
        }
        int userPermissions = findIfAdmin(mUser);

        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem logOut = menu.findItem(R.id.logOut);
        MenuItem logIn = menu.findItem(R.id.signIn);
        MenuItem signUp = menu.findItem(R.id.signUp);
        MenuItem profile = menu.findItem(R.id.profile);
        MenuItem searchButton = menu.findItem(R.id.optionsSearchButton);
        MenuItem adminPanel = menu.findItem(R.id.adminPanelMenu);

        if (mFirebaseAuth.getCurrentUser() != null) {
            logIn.setVisible(false);
            signUp.setVisible(false);
            logOut.setVisible(true);
            profile.setTitle(mUserName);
            profile.setVisible(true);
            if (userPermissions > 0)
            adminPanel.setVisible(true);

        }
        else {
            logOut.setVisible(false);
            logIn.setVisible(true);
            signUp.setVisible(true);
            profile.setVisible(false);
            adminPanel.setVisible(false);
        }

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
                invalidateOptionsMenu();
                break;

            case R.id.profile:
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                startActivity(profileIntent);
                break;

            case R.id.optionsSearchButton:
                animate();
                break;
            case R.id.adminPanelMenu:
                Intent adminPanelIntent = new Intent(this, AdminPanelActivity.class);
                startActivity(adminPanelIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateOptionsMenu() {
        if (mMenu != null) {
            onPrepareOptionsMenu(mMenu);
        }
    }

    private int findIfAdmin(FirebaseUser user){
        final int[] permissions = new int[1];
        if (mFirebaseAuth.getCurrentUser() != null) {
            final String uuid = user.getUid();

            adminDB = FirebaseDatabase.getInstance().getReference("admin");

            DatabaseReference userAdmins = adminDB.child("adminUsers");

            userAdmins.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(uuid)) {
                        permissions[0] = (dataSnapshot.child(uuid).getValue(Integer.class));
                        Toast.makeText(getApplicationContext(), "User is admin", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        permissions[0] = 0;
                        Toast.makeText(getApplicationContext(), "User is not admin", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        return permissions[0];

    }
}
