package me.paxana.cwnet.UI;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.paxana.cwnet.Adapters.TriggerAdapter;
import me.paxana.cwnet.Model.Movie;
import me.paxana.cwnet.Model.Trigger;
import me.paxana.cwnet.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static me.paxana.cwnet.R.layout.trigger_list_item;

public class MovieActivity extends AppCompatActivity {

    private static final String KEY_IMDB = "KEY_IMDB";
    private static final String PREFS_FILE = "me.paxana.cwnet.preferences";
    private ImageView mPosterImageView;
    private TextView mTitleTextView;
    private TextView mYearTextView;
    private TextView mSummaryTextView;
    boolean t1countUp = false;
    boolean t1countDown = false;
    private ListView triggerListView;

    private Movie mMovie;

    DatabaseReference movieDB;
    DatabaseReference userDB;
    DatabaseReference  thisMovieDB;
    DatabaseReference triggerDB;

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mUser;

    List<Trigger> mTriggerList;

    String imdbID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        mPosterImageView = findViewById(R.id.posterImageView);
        mTitleTextView = findViewById(R.id.titleTextView);
        mYearTextView = findViewById(R.id.yearTextView);
        mSummaryTextView = findViewById(R.id.summaryTextView);
        triggerListView = findViewById(R.id.triggerListView);
        mTriggerList = new ArrayList<>();

        mFirebaseAuth = FirebaseAuth.getInstance();

        mUser = mFirebaseAuth.getCurrentUser();

        movieDB = FirebaseDatabase.getInstance().getReference("movies");
        userDB = FirebaseDatabase.getInstance().getReference("users");

        Intent intent = getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        imdbID = intent.getStringExtra("ID_KEY");

        try {
            getMovieDetails(imdbID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

                thisMovieDB = movieDB.child(imdbID);
                thisMovieDB.child("Details").setValue(mMovie);
                triggerDB = thisMovieDB.child("Triggers");

        final Query query = thisMovieDB.orderByChild("Triggers").equalTo("Trigger 1");

        triggerDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Trigger 1")){

                }
                else {
                    Trigger trigger1 = new Trigger("Trigger 1", 0, 0);
                    Trigger trigger2 = new Trigger("Trigger 2", 0, 0);
                    Trigger trigger3 = new Trigger("Trigger 3", 0, 0);

                    triggerDB.child(trigger1.getTriggerName()).setValue(trigger1);
                    triggerDB.child(trigger2.getTriggerName()).setValue(trigger2);
                    triggerDB.child(trigger3.getTriggerName()).setValue(trigger3);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        triggerDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mTriggerList.clear();

                for (DataSnapshot triggerSnapshot : dataSnapshot.getChildren()) {
                    Trigger trigger = triggerSnapshot.getValue(Trigger.class);

                    mTriggerList.add(trigger);
                }

                TriggerAdapter adapter = new TriggerAdapter(MovieActivity.this, mTriggerList, imdbID);
                triggerListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





//        trigger1PlusButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                upboat();
//
//            }
//        });
//        trigger1MinusButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                downboat();
//            }
//        });

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

    //    private void upboat() {
//        if (!t1countDown) {
//            if (!t1countUp) {
//                t1count++;
//                updateCounter();
//                t1countUp = true;
//                t1countDown = false;
//                return;
//            } else if (t1countUp) {
//                t1count--;
//                updateCounter();
//                t1countUp = false;
//                t1countDown = false;
//                return;
//            }
//        }
//        if (t1countDown) {
//            if (!t1countUp) {
//                t1count+=2;
//                updateCounter();
//                t1countUp = true;
//                t1countDown = false;
//            }
//        }
//    }
//
//    private void updateCounter() {
//        mTrigger1Counter.setText(String.valueOf(t1count));
//    }

//    private void downboat() {
//        if (!t1countUp) {
//            if (!t1countDown) {
//                t1count--;
//                updateCounter();
//                t1countDown = true;
//                t1countUp = false;
//                return;
//            } else if (t1countDown) {
//                t1count++;
//                updateCounter();
//                t1countUp = false;
//                t1countDown = false;
//                return;
//            }
//        }
//        else if (t1countUp) {
//            if (!t1countDown) {
//                t1count-=2;
//                mTrigger1Counter.setText(String.valueOf(t1count));
//                t1countDown = true;
//                t1countUp = false;
//            }
//        }
//    }

    private void getMovieDetails(String imdbID) throws JSONException {
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


//    @Override
//    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//        String item = adapterView.getItemAtPosition(i).toString();
//        Toast.makeText(getApplicationContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
//    }
//
//    @Override
//    public void onNothingSelected(AdapterView<?> adapterView) {
//
//    }

}

