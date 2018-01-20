package me.paxana.cwnet.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import me.paxana.cwnet.Adapters.ExpandableListAdapter;
import me.paxana.cwnet.Adapters.TriggerAdapter;
import me.paxana.cwnet.Model.Category;
import me.paxana.cwnet.Model.Movie;
import me.paxana.cwnet.Model.Trigger;
import me.paxana.cwnet.R;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieActivity extends AppCompatActivity {

    private static final String KEY_IMDB = "KEY_IMDB";
    private ImageView mPosterImageView;
    private TextView mTitleTextView;
    private TextView mYearTextView;
    private TextView mSummaryTextView;
    private ExpandableListView triggerListView;
    private ArrayList<Trigger> mTriggerPrefList;
    private Movie mMovie;
    private LinkedList<Trigger> resultList;
    private ArrayList<String> templist;
    private TextView plzConsiderTextview;
    private Button signUpButton;
    private AdView mAdView;
    private MaterialRatingBar mRatingBar;
    ArrayList<Category> mCategoryList = new ArrayList<>();
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;


    DatabaseReference movieDB;
    DatabaseReference userDB;
    DatabaseReference  thisMovieDB;
    DatabaseReference triggerDB;
    DatabaseReference adminDB;

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mUser;

    ArrayList<Trigger> mTriggerList;
    ArrayList<Trigger> triggerList;

    String imdbID;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        mPosterImageView = findViewById(R.id.posterImageView);
        mTitleTextView = findViewById(R.id.titleTextView);
        mYearTextView = findViewById(R.id.yearTextView);
        mSummaryTextView = findViewById(R.id.summaryTextView);
        triggerListView = findViewById(R.id.triggerListView);
        plzConsiderTextview = findViewById(R.id.plzConsiderTextView);
        signUpButton = findViewById(R.id.signUpButton);
        mRatingBar = findViewById(R.id.materialRatingBar);
        MobileAds.initialize(this, "ca-app-pub-7338499199160030~7379357533");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        plzConsiderTextview.setVisibility(View.INVISIBLE);
        signUpButton.setVisibility(View.INVISIBLE);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mUser = mFirebaseAuth.getCurrentUser();

        movieDB = FirebaseDatabase.getInstance().getReference("movies");
        userDB = FirebaseDatabase.getInstance().getReference("users");
        adminDB = FirebaseDatabase.getInstance().getReference("admin");

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(tv);

        Intent intent = getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        imdbID = intent.getStringExtra("ID_KEY");
        final String keyTitle = intent.getStringExtra("TITLE_KEY");
        final String keyYear = intent.getStringExtra("YEAR_KEY");
        final String keyPoster = intent.getStringExtra("POSTER_KEY");

        try {
            getMovieDetails(imdbID, new Runnable() {
                @Override
                public void run() {
                    CheckForTriggerValues(new Runnable() {
                        @Override
                        public void run() {
                            populateTriggers(new Runnable() {
                                @Override
                                public void run() {
                                    populatePrefs(new Runnable() {
                                        @Override
                                        public void run() {
                                            reOrderList(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Movie passMovie = new Movie();
                                                    passMovie.setImdbID(imdbID);
                                                    passMovie.setPosterURL(keyPoster);
                                                    passMovie.setYear(keyYear);
                                                    passMovie.setTitle(keyTitle);

                                                    ExpandableListAdapter adapter = new ExpandableListAdapter(MovieActivity.this, imdbID, keyTitle, mCategoryList);
                                                    triggerListView.setAdapter(adapter);

                                                }
                                            });

                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
                thisMovieDB = movieDB.child(imdbID);
                triggerDB = thisMovieDB.child("Triggers");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_IMDB, imdbID);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        imdbID = intent.getStringExtra("ID_KEY");
    }

    private void getMovieDetails(String imdbID, Runnable runnable) throws JSONException {
        String apiKey = "face7189";
        String theURL = "http://www.omdbapi.com/?i=" + imdbID + "&apikey=" + apiKey + "&plot=full";
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

                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Movie movie = parseMovieDetails(jsonData);
                                updateMovieUI(movie);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }

        });
        runnable.run();
    }

    private Movie parseMovieDetails(String jsonData) throws JSONException {

            JSONObject jsonObject = new JSONObject(jsonData);

            Movie movie = new Movie();

            movie.setPosterURL(jsonObject.getString("Poster"));
            movie.setTitle(jsonObject.getString("Title"));
            movie.setYear(jsonObject.getString("Year"));
            movie.setSummary(jsonObject.getString("Plot"));
            movie.setImdbRating(jsonObject.getDouble("imdbRating"));


            mMovie = movie;
            return movie;
        }

        private void updateMovieUI(Movie movie){
            Context mContext = getApplicationContext();
            mSummaryTextView.setText(movie.getSummary());
            mYearTextView.setText(movie.getYear());
            mTitleTextView.setText(movie.getTitle());
            mRatingBar.setRating(BigDecimal.valueOf(movie.getImdbRating()).floatValue());
            mRatingBar.setClickable(false);
            mRatingBar.setOnRatingChangeListener(null);
            mRatingBar.setEnabled(false);
            String imdbRatingString = String.valueOf(movie.getImdbRating());
            Toast toast = Toast.makeText(getApplicationContext(), imdbRatingString, Toast.LENGTH_LONG);
            toast.show();
            if (!movie.getPosterURL().equals("N/A")) {
                Picasso.with(mContext).load(movie.getPosterURL()).into(mPosterImageView);
            }
            else {
                Picasso.with(mContext).load("https://i.imgur.com/eelMQnl.png").into(mPosterImageView);
            }

        }

        private void populatePrefs(final Runnable runnable) {
//            mTriggerPrefList = new ArrayList<>();
//
//            mFirebaseAuth = FirebaseAuth.getInstance();
//
//            mUser = mFirebaseAuth.getCurrentUser();
//
//            movieDB = FirebaseDatabase.getInstance().getReference("movies");
//            userDB = FirebaseDatabase.getInstance().getReference("users");
//            adminDB = FirebaseDatabase.getInstance().getReference("admin");
//            thisMovieDB = movieDB.child(imdbID);
//            //thisMovieDB.child("Details").setValue(mMovie);
//            triggerDB = thisMovieDB.child("Triggers");
//
//            userDB.child(mUser.getUid()).child("preferences").child("trigger").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//
//                    for (DataSnapshot triggerSnapshot : dataSnapshot.getChildren()) {
//
//                        String name = String.valueOf(triggerSnapshot.child("triggerName").getValue());
//                        Trigger trigger = new Trigger();
//                        trigger.setTriggerName(name);
//                        trigger.setTriggerVotesTotal(0);
//                        trigger.setTriggerVotesYes(0);
//                        mTriggerPrefList.add(trigger);
//                    }
//
//                    Intent intent = getIntent();
//                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//
//                    imdbID = intent.getStringExtra("ID_KEY");
                    runnable.run();

//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                }
//            });
        }

        private void populateTriggers(final Runnable runnable) {
            triggerList = new ArrayList<>();
            movieDB = FirebaseDatabase.getInstance().getReference("movies");
            userDB = FirebaseDatabase.getInstance().getReference("users");
            adminDB = FirebaseDatabase.getInstance().getReference("admin");
            thisMovieDB = movieDB.child(imdbID);
            thisMovieDB.child("Details").setValue(mMovie);
            triggerDB = thisMovieDB.child("Triggers");

            triggerDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot triggerSnapshot : dataSnapshot.getChildren()) {
                        Trigger trigger = triggerSnapshot.getValue(Trigger.class);
                        triggerList.add(trigger);
                    }
                    runnable.run();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        private void reOrderList(Runnable runnable) {

            Map<String, List<Trigger>> subs = new HashMap<>();

            for (Trigger t : triggerList) {
                ArrayList<String> catList = t.getCategory();
                for (String cat : catList) {
                    ArrayList<Trigger> temp = (ArrayList<Trigger>) subs.get(cat);

                    if (temp == null) {
                        temp = new ArrayList<>();
                        subs.put(cat, temp);
                    }
                    temp.add(t);
                }
            }

            for (Map.Entry entry : subs.entrySet()) {
                String key = entry.getKey().toString();
                ArrayList value = (ArrayList) entry.getValue();

                Category category = new Category(key, 0, 0, value);

                mCategoryList.add(category);
            }

            Log.d("ZZZZZ1", String.valueOf(mCategoryList));

//            resultList = new LinkedList<>();
//
//            for (Trigger triggerA : mTriggerList) {
//                boolean found = false;
//                for (Trigger triggerB : mTriggerPrefList) {
//                    if (triggerB.getTriggerName().equals(triggerA.getTriggerName())) {
//                        found = true;
//                    }
//                }
//                if (found) {
//                    if (!resultList.contains(triggerA)) {
//                        resultList.add(triggerA);
//                    }
//                }
//            }
//            mTriggerList.removeAll(resultList);
//            resultList.addAll(mTriggerList);
//
//            mTriggerList.addAll(resultList);

            runnable.run();
    }

    private void CheckForTriggerValues(final Runnable runnable) {
            if (thisMovieDB == null && mFirebaseAuth.getCurrentUser() == null) {
                plzConsiderTextview.setVisibility(View.VISIBLE);
                signUpButton.setVisibility(View.VISIBLE);
            }

        adminDB.child("triggerList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, Trigger>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Trigger>>() {};

                final Map<String, Trigger> triggerList = dataSnapshot.getValue(genericTypeIndicator);

                triggerDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        assert triggerList != null;
                        for (Map.Entry<String, Trigger> entry : triggerList.entrySet())
                        {
                            if (dataSnapshot.hasChild(entry.getKey())){
                                //TODO: Make sure categories match and if I remove a trigger from admin it removes from movie

                                Trigger t1 = entry.getValue();
                                String t1n = t1.getTriggerName();
                                Trigger t2 = dataSnapshot.child(entry.getKey()).getValue(Trigger.class);
                                String t2n = null;
                                if (t2 != null) {
                                    t2n = t2.getTriggerName();
                                }

                                if (!t1n.equals(t2n)) {
                                    if (t2 != null) {
                                        t2.setTriggerName(t1n);
                                    }
                                    triggerDB.child(t1.getId()).child("triggerName").setValue(t1n);
                                }
                            }
                            else {
                                Trigger trigger1 = entry.getValue();
                                triggerDB.child(entry.getKey()).setValue(trigger1);
                            }
                        }
                        runnable.run();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

