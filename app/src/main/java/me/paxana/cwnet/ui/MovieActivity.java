package me.paxana.cwnet.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import me.paxana.cwnet.Adapters.TriggerAdapter;
import me.paxana.cwnet.Model.Movie;
import me.paxana.cwnet.Model.Trigger;
import me.paxana.cwnet.R;
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
    private ListView triggerListView;
    private ArrayList<Trigger> mTriggerPrefList;
    private Movie mMovie;
    private LinkedList<Trigger> resultList;

    DatabaseReference movieDB;
    DatabaseReference userDB;
    DatabaseReference  thisMovieDB;
    DatabaseReference triggerDB;
    DatabaseReference adminDB;

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mUser;

    ArrayList<Trigger> mTriggerList;

    String imdbID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        mPosterImageView = (ImageView) findViewById(R.id.posterImageView);
        mTitleTextView = (TextView) findViewById(R.id.titleTextView);
        mYearTextView = (TextView) findViewById(R.id.yearTextView);
        mSummaryTextView = (TextView) findViewById(R.id.summaryTextView);
        triggerListView = (ListView) findViewById(R.id.triggerListView);


        mFirebaseAuth = FirebaseAuth.getInstance();

        mUser = mFirebaseAuth.getCurrentUser();

        movieDB = FirebaseDatabase.getInstance().getReference("movies");
        userDB = FirebaseDatabase.getInstance().getReference("users");
        adminDB = FirebaseDatabase.getInstance().getReference("admin");

//        mTriggerList = new ArrayList<>();
//        mTriggerPrefList = new ArrayList<>();
//

//        mTriggerList.clear();

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
                                            for (Trigger trigger : mTriggerList) {
                                                Log.d("CWWNET mtriggerlist", trigger.getTriggerName());

                                            }
                                            for (Trigger trigger : mTriggerList) {
                                                Log.d("CWWNET mtriggerpreflist", trigger.getTriggerName());
                                            }

                                            reOrderList(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Log.d("Lastest TORPEDO", "FIRED");
//                                String s1 = resultList.get(0).getTriggerName();
//                                Log.d("NameyName", s1);
                                                    Movie passMovie = new Movie();
                                                    passMovie.setImdbID(imdbID);
                                                    passMovie.setPosterURL(keyPoster);
                                                    passMovie.setYear(keyYear);
                                                    passMovie.setTitle(keyTitle);
                                                    TriggerAdapter adapter = new TriggerAdapter(MovieActivity.this, resultList, passMovie, imdbID );
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
                thisMovieDB.child("Details").setValue(mMovie);
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
        String theURL = "http://www.omdbapi.com/?i=" + imdbID + "&apikey=" + apiKey;
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
        Log.d("extra torpedo: ", "FIRED");
        runnable.run();
    }

    private Movie parseMovieDetails(String jsonData) throws JSONException {

            JSONObject jsonObject = new JSONObject(jsonData);

            Movie movie = new Movie();

            movie.setPosterURL(jsonObject.getString("Poster"));
            movie.setTitle(jsonObject.getString("Title"));
            movie.setYear(jsonObject.getString("Year"));
            movie.setSummary(jsonObject.getString("Plot"));

            mMovie = movie;

            return movie;
        }

        private void updateMovieUI(Movie movie){
            Context mContext = getApplicationContext();
            mSummaryTextView.setText(movie.getSummary());
            mYearTextView.setText(movie.getYear());
            mTitleTextView.setText(movie.getTitle());
            Picasso.with(mContext).load(movie.getPosterURL()).into(mPosterImageView);

        }

        private void populatePrefs(final Runnable runnable) {
            mTriggerPrefList = new ArrayList<>();
            Log.d("FIRING TORPEDOS", "Populate Prefs");

            mFirebaseAuth = FirebaseAuth.getInstance();

            mUser = mFirebaseAuth.getCurrentUser();

            movieDB = FirebaseDatabase.getInstance().getReference("movies");
            userDB = FirebaseDatabase.getInstance().getReference("users");
            adminDB = FirebaseDatabase.getInstance().getReference("admin");
            thisMovieDB = movieDB.child(imdbID);
            thisMovieDB.child("Details").setValue(mMovie);
            triggerDB = thisMovieDB.child("Triggers");

            userDB.child(mUser.getUid()).child("preferences").child("trigger").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot triggerSnapshot : dataSnapshot.getChildren()) {

                        String name = String.valueOf(triggerSnapshot.child("triggerName").getValue());
                        Log.d("TriggerName wots", name);
                        Trigger trigger = new Trigger();
                        trigger.setTriggerName(name);
                        trigger.setTriggerVotesTotal(0);
                        trigger.setTriggerVotesYes(0);
                        mTriggerPrefList.add(trigger);
                    }

                    Intent intent = getIntent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                    imdbID = intent.getStringExtra("ID_KEY");
                    runnable.run();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
        }

        private void populateTriggers(final Runnable runnable) {
            mTriggerList = new ArrayList<>();
            mFirebaseAuth = FirebaseAuth.getInstance();
            Log.d("FIRING TORPEDOS", "Populate Triggers");

            mUser = mFirebaseAuth.getCurrentUser();

            movieDB = FirebaseDatabase.getInstance().getReference("movies");
            userDB = FirebaseDatabase.getInstance().getReference("users");
            adminDB = FirebaseDatabase.getInstance().getReference("admin");
            thisMovieDB = movieDB.child(imdbID);
            thisMovieDB.child("Details").setValue(mMovie);
            triggerDB = thisMovieDB.child("Triggers");

            triggerDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    mTriggerList.clear();

                    for (DataSnapshot triggerSnapshot : dataSnapshot.getChildren()) {
                        Trigger trigger = triggerSnapshot.getValue(Trigger.class);
                        mTriggerList.add(trigger);
                    }
                    runnable.run();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        private void reOrderList(Runnable runnable) {
            resultList = new LinkedList<>();
            // just a test of my sorting ability

            for (Trigger triggerA : mTriggerList) {
                boolean found = false;
                for (Trigger triggerB : mTriggerPrefList) {
                    Log.d("Compare A", triggerA.getTriggerName());
                    Log.d("Compare B", triggerB.getTriggerName());
                    if (triggerB.getTriggerName().equals(triggerA.getTriggerName())) {
                        found = true;
                        Log.d("holeup", triggerB.getTriggerName());
                    }
                }
                if (found) {
                    resultList.add(triggerA);
                }
            }
            mTriggerList.removeAll(resultList);
            resultList.addAll(mTriggerList);
            // end test

            runnable.run();
    }

    private void CheckForTriggerValues(final Runnable runnable) {
        adminDB.child("triggerList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {};

                final Map<String, Object> triggerList = dataSnapshot.getValue(genericTypeIndicator);

                triggerDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (Map.Entry<String, Object> entry : triggerList.entrySet())
                        {
                            if (dataSnapshot.hasChild(entry.getKey())){
//
                            }
                            else {
                                Trigger trigger1 = new Trigger(entry.getKey(), 0, 0);
                                triggerDB.child(trigger1.getTriggerName()).setValue(trigger1);
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

        int isTrue(Boolean boolDown, Boolean boolUp) {
            int x = 0;
            if (boolUp) {
                 x = 1;
            }
            if (boolDown) {
                x = -1;
            }
            return x;
        }

}

