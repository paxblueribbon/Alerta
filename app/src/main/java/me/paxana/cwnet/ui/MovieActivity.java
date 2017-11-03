package me.paxana.cwnet.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
    ArrayList<Category> mCategoryList = new ArrayList<>();
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    ArrayList<Trigger> l1 = new ArrayList<>();
    ArrayList<Trigger> l2 = new ArrayList<>();
    Trigger t1;
    Trigger t2;
    Trigger t3;
    Trigger t4;
    Trigger t5;
    Trigger t6;
    Category c1;
    Category c2;

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
        triggerListView = (ExpandableListView) findViewById(R.id.triggerListView);


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
                                            reOrderList(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Movie passMovie = new Movie();
                                                    passMovie.setImdbID(imdbID);
                                                    passMovie.setPosterURL(keyPoster);
                                                    passMovie.setYear(keyYear);
                                                    passMovie.setTitle(keyTitle);
//                                                    TriggerAdapter adapter = new TriggerAdapter(MovieActivity.this, resultList, passMovie, imdbID );
//                                                    triggerListView.setAdapter(adapter);

                                                    ExpandableListAdapter adapter = new ExpandableListAdapter(MovieActivity.this, mCategoryList);
                                                    triggerListView.setAdapter(adapter);

//                                                    String key1 = adminDB.child("triggerList").push().getKey();
//                                                    String key2 = adminDB.child("triggerList").push().getKey();
//                                                    String key3 = adminDB.child("triggerList").push().getKey();
//                                                    String key4 = adminDB.child("triggerList").push().getKey();
//
//                                                    Trigger trigger1 = new Trigger("Trigger 1", 0, 0);
//                                                    Trigger trigger2 = new Trigger("Trigger 2", 0, 0);
//                                                    Trigger trigger3 = new Trigger("Trigger 3", 0, 0);
//                                                    Trigger trigger4 = new Trigger("Trigger 4", 0, 0);
//
//                                                    adminDB.child("triggerList").child(key1).setValue(trigger1);
//                                                    adminDB.child("triggerList").child(key2).setValue(trigger2);
//                                                    adminDB.child("triggerList").child(key3).setValue(trigger3);
//                                                    adminDB.child("triggerList").child(key4).setValue(trigger4);
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
//            listDataHeader = new ArrayList<String>();
//            listDataChild = new HashMap<String, List<String>>();
//
//            // Adding child data
//            listDataHeader.add("Top 250");
//            listDataHeader.add("Now Showing");
//            listDataHeader.add("Coming Soon..");
//
//            // Adding child data
//            List<String> top250 = new ArrayList<String>();
//            top250.add("The Shawshank Redemption");
//            top250.add("The Godfather");
//            top250.add("The Godfather: Part II");
//            top250.add("Pulp Fiction");
//            top250.add("The Good, the Bad and the Ugly");
//            top250.add("The Dark Knight");
//            top250.add("12 Angry Men");
//
//            List<String> nowShowing = new ArrayList<String>();
//            nowShowing.add("The Conjuring");
//            nowShowing.add("Despicable Me 2");
//            nowShowing.add("Turbo");
//            nowShowing.add("Grown Ups 2");
//            nowShowing.add("Red 2");
//            nowShowing.add("The Wolverine");
//
//            List<String> comingSoon = new ArrayList<String>();
//            comingSoon.add("2 Guns");
//            comingSoon.add("The Smurfs 2");
//            comingSoon.add("The Spectacular Now");
//            comingSoon.add("The Canyons");
//            comingSoon.add("Europa Report");
//
//            listDataChild.put(listDataHeader.get(0), top250); // Header, Child data
//            listDataChild.put(listDataHeader.get(1), nowShowing);
//            listDataChild.put(listDataHeader.get(2), comingSoon);
//
//            runnable.run();


//            mTriggerList = new ArrayList<>();
//            mFirebaseAuth = FirebaseAuth.getInstance();
//            mUser = mFirebaseAuth.getCurrentUser();
//
//            movieDB = FirebaseDatabase.getInstance().getReference("movies");
//            userDB = FirebaseDatabase.getInstance().getReference("users");
//            adminDB = FirebaseDatabase.getInstance().getReference("admin");
//            thisMovieDB = movieDB.child(imdbID);
//            thisMovieDB.child("Details").setValue(mMovie);
//            triggerDB = thisMovieDB.child("Triggers");
//
//            triggerDB.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//
//                    for (DataSnapshot triggerSnapshot : dataSnapshot.getChildren()) {
//                        Trigger trigger = triggerSnapshot.getValue(Trigger.class);
//                        mTriggerList.add(trigger);
//                    }
//                    runnable.run();
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
            ArrayList<String> cat1 = new ArrayList<>();
            ArrayList<String> cat2 = new ArrayList<>();
            ArrayList<String> cat3 = new ArrayList<>();

            String c1 = "Category 1";
            String c2 = "Category 2";
            String c3 = "Category 3";
            cat1.add(c1);
            cat1.add(c2);
            cat2.add(c2);
            cat3.add(c3);

            t1 = new Trigger("Trigger 1", "1", 0, 0, cat1);
            t2 = new Trigger("Trigger 2", "2", 0, 0, cat2);
            t3 = new Trigger("Trigger 3", "3", 0, 0, cat3);

            t4 = new Trigger("Trigger 4", "4", 0, 0, cat2);
            t5 = new Trigger("Trigger 5", "5", 0, 0, cat3);
            t6 = new Trigger("Trigger 6", "6", 0, 0, cat2);

            ArrayList<Trigger> testList = new ArrayList<>();
            testList.add(t1);
            testList.add(t2);
            testList.add(t3);
            testList.add(t4);
            testList.add(t5);
            testList.add(t6);

            Map<String, List<Trigger>> subs = new HashMap<String, List<Trigger>>();

            for (Trigger t : testList) {
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

            
//            ArrayList<Trigger> tl1 = new ArrayList<>();
//            ArrayList<Trigger> cl1 = new ArrayList<>();
//            ArrayList<Trigger> cl2 = new ArrayList<>();
//
//            cl1.add(t1);
//            cl1.add(t2);
//            cl1.add(t3);
//            cl2.add(t4);
//            cl2.add(t5);
//            cl2.add(t6);
//
//            Category c1 = new Category("Category 1", 0, 0, cl1);
//            Category c2 = new Category("Category 2", 0, 0, cl2);
//
//            String k1 = adminDB.child("categoryList").push().getKey();
//            String k2 = adminDB.child("categoryList").push().getKey();
//
//            adminDB.child("categoryList").child(k1).setValue(c1);
//            adminDB.child("categoryList").child(k2).setValue(c2);

//            adminDB.child("categoryList").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    for (DataSnapshot childsnapshot : dataSnapshot.getChildren()) {
//                        Category category = childsnapshot.getValue(Category.class);
//                        mCategoryList.add(category);
//
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });


//            for (Trigger trigger : tl1) {
//                for (Category category : mCategoryList) {
//                    ArrayList<String> categoryList = trigger.getCategories();
//                    if (categoryList.contains(category.getCategoryName())) {
//                        category.addToTriggerList(trigger);
//                    }
//                }
//            }
            Log.d("ZZZZZ1", String.valueOf(mCategoryList));

            Log.d("ZZZZZ", t6.getTriggerName());

//            mCategoryList.add(c1);
//            mCategoryList.add(c2);
            runnable.run();

        }

        private void reOrderList(Runnable runnable) {
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
//        adminDB.child("triggerList").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {};
//
//                final Map<String, Object> triggerList = dataSnapshot.getValue(genericTypeIndicator);
//
//                triggerDB.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//
//                        for (Map.Entry<String, Object> entry : triggerList.entrySet())
//                        {
//                            if (dataSnapshot.hasChild(entry.getKey())){
////
//                            }
//                            else {
//                                Trigger trigger1 = new Trigger(entry.getKey(), 0, 0);
//                                triggerDB.child(trigger1.getTriggerName()).setValue(trigger1);
//                            }
//                        }
                        runnable.run();
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

}

