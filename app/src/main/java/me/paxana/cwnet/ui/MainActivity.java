package me.paxana.cwnet.ui;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.facebook.CallbackManager;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import me.paxana.cwnet.Adapters.SearchAdapter;
import me.paxana.cwnet.Model.Movie;
import me.paxana.cwnet.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class MainActivity extends AppCompatActivity {

    Button searchButton;

    private FirebaseAnalytics mFirebaseAnalytics;

    private EditText mMovieTitle;
    private ListView mListView;
    private Menu mMenu;
    private RelativeLayout mLoaderLayout;
    DatabaseReference adminDB;

    FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

    FirebaseUser mUser = mFirebaseAuth.getCurrentUser();
    String mUserName;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //stylize the action bar
        TextView tv = new TextView(getApplicationContext());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(lp);
        tv.setText(R.string.Title);
        tv.setTextSize(50);
        tv.setTextColor(Color.parseColor("#FFFFFF"));
        Typeface tf = Typeface.createFromAsset(getAssets(), "KGALittleSwag.ttf");
        tv.setTypeface(tf);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(tv);


        updateOptionsMenu();
        MobileAds.initialize(this, "ca-app-pub-7338499199160030~7379357533");

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        mLoaderLayout = findViewById(R.id.loadingPanel);
        mLoaderLayout.setVisibility(View.INVISIBLE);
        searchButton = findViewById(R.id.searchButton);
        searchButton.setVisibility(View.INVISIBLE);
        mMovieTitle = findViewById(R.id.movieTitle);
        mMovieTitle.setVisibility(View.INVISIBLE);
        mListView = findViewById(R.id.resultsList);
        SliderLayout sliderShow = findViewById(R.id.slider);

        adminDB = FirebaseDatabase.getInstance().getReference("admin");

        TextSliderView textSliderView = new TextSliderView(this);
        TextSliderView textSliderView2 = new TextSliderView(this);
        textSliderView.description("IMDB's Top 250 Films")
                .image("https://az616578.vo.msecnd.net/files/2017/01/28/6362123427518587751592918376_maxresdefault%20(2).jpg")
        .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
            @Override
            public void onSliderClick(BaseSliderView slider) {
                Intent intent = new Intent(getApplicationContext(), Top250Activity.class);
                startActivity(intent);
            }
        });
        textSliderView2.description("Top Films in Theaters Right Now")
                .image("http://cdn1.sciencefiction.com/wp-content/uploads/2017/11/Coco-banner.jpg");

        sliderShow.addSlider(textSliderView);
        sliderShow.addSlider(textSliderView2);

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

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoaderLayout.setVisibility(View.VISIBLE);
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

    private void animate() {

        int titleStartValue = mMovieTitle.getTop();
        int titleEndValue = mMovieTitle.getBottom();

        int buttonStartValue = searchButton.getTop();
        int buttonEndValue = searchButton.getBottom();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


        if (mMovieTitle.getVisibility() == View.INVISIBLE) {
            mMovieTitle.setVisibility(View.VISIBLE);

            ObjectAnimator.ofInt(mMovieTitle, "bottom", titleStartValue, titleEndValue).start();
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }

            searchButton.setVisibility(View.VISIBLE);
            ObjectAnimator.ofInt(searchButton, "bottom", buttonStartValue, buttonEndValue).start();
        }

        else {
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0);
            }
            mMovieTitle.setVisibility(View.INVISIBLE);
            searchButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
//        updateOptionsMenu();
        super.onResume();
    }


    private void getResults(String searchParam) throws JSONException {
        String apiKey = "face7189";
        String theURL = "http://www.omdbapi.com/?s=" + searchParam + "&apikey=" + apiKey + "&type=movie";
        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(theURL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String jsonData = String.valueOf(response.body());

                JSONObject results;

                try {
                    results = new JSONObject(jsonData);
                    JSONArray data = results.getJSONArray("Search");
                    final Movie[] searchResults = new Movie[data.length()];
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jsonResult = data.getJSONObject(i);
                            final Movie movie = new Movie();

                            movie.setTitle(jsonResult.getString("Title"));
                            movie.setYear(jsonResult.getString("Year"));
                            if (jsonResult.getString("Poster").equals("N/A")) {
                                movie.setPosterURL("https://i.imgur.com/eelMQnl.png");
                            }
                            else {
                                movie.setPosterURL(jsonResult.getString("Poster"));
                            }
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
                                    mLoaderLayout.setVisibility(View.INVISIBLE);
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
        if (mUser != null && (!mUser.isAnonymous())) {
            mUserName = mUser.getDisplayName();
        }
        toggleMenuLogState(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        updateOptionsMenu();
//        toggleMenuLogState(menu);
        return true;
    }

    private void toggleMenuLogState(Menu menu) {
        mMenu = menu;
        menu.clear();

        if (mUser != null) {
            mUserName = mUser.getDisplayName();
        }

        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem logOut = menu.findItem(R.id.logOut);
        MenuItem signUp = menu.findItem(R.id.signUp);
        MenuItem profile = menu.findItem(R.id.profile);
        MenuItem searchButton = menu.findItem(R.id.optionsSearchButton);

        FirebaseUser user = mFirebaseAuth.getCurrentUser();

        if (mFirebaseAuth.getCurrentUser() != null) {
            signUp.setVisible(false);
            logOut.setVisible(true);
            if (mFirebaseAuth.getCurrentUser() == null || user.isAnonymous()) {
                profile.setTitle("Sign Up/In");
            }
            else {
                String arr[] = mUserName.split(" ");
                String firstName = arr[0];
                profile.setTitle(firstName);
            }
            profile.setVisible(true);
        }
        else {
            logOut.setVisible(false);
            signUp.setVisible(true);
            profile.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signUp:
                Intent intent = new Intent(this, SignupActivity.class);
                startActivity(intent);
                break;

            case R.id.logOut:
                AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //refresh the menu
                    }
                });
                invalidateOptionsMenu();
                break;

            case R.id.profile:
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                startActivity(profileIntent);
                break;

            case R.id.optionsSearchButton:
                animate();
                mMovieTitle.requestFocus();
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    private void updateOptionsMenu() {
        if (mMenu != null) {
            onPrepareOptionsMenu(mMenu);
        }
    }

    }
