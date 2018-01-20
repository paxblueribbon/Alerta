package me.paxana.cwnet.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Visibility;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import me.paxana.cwnet.Adapters.HistoryAdapter;
import me.paxana.cwnet.Model.Movie;
import me.paxana.cwnet.Model.Trigger;
import me.paxana.cwnet.R;

public class ProfileActivity extends AppCompatActivity {

    Context mContext;

    private EditText nameView;
    private EditText emailView;
    private EditText passwordView1;
    private EditText passwordView2;
    private ListView historyListView;
    private Button adminPanelButton;
    private FirebaseAuth mFirebaseAuth;
    final ArrayList<Integer> selectedItems=new ArrayList<>();
    CharSequence[] cs;
    List<String> adminUIDlist;
    int adminAccess;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser user = firebaseAuth.getCurrentUser();

    FirebaseDatabase fbDB = FirebaseDatabase.getInstance();
    DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
    DatabaseReference thisUserHistory = users.child(user.getUid()).child("changes");
    DatabaseReference adminDB = fbDB.getReference("admin");
    int mClearanceLevel;
    DatabaseReference adminTriggerDB = adminDB.child("triggerList");

    String[] str = null;
    List<Trigger> mTriggerList = new ArrayList<>();
    final List<String> thisMyList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameView = findViewById(R.id.displayNameTextview);
        emailView = findViewById(R.id.emailAddressTextview);
        passwordView1 = findViewById(R.id.passwordTextview1);
        passwordView2 = findViewById(R.id.passwordTextview2);
        Button applyButton = findViewById(R.id.applyButton);
        historyListView = findViewById(R.id.historyListView);
        adminPanelButton = findViewById(R.id.adminPanelButton);
        adminPanelButton.setVisibility(View.INVISIBLE);
        final ArrayList<String> triggerOptionText = new ArrayList<>();
        final ArrayList<Trigger> listOfTriggers = new ArrayList<>();
        final LinkedList<String> linkedList = new LinkedList<>();

        if (user != null) {
            adminAccess = findIfAdmin(user);
            }

        str = new String[10];
        final ArrayList<Trigger> myList = new ArrayList<>();

        getTriggerOptions(new Runnable() {
            @Override
            public void run() {
                cs = thisMyList.toArray(new CharSequence[triggerOptionText.size()]);}
        });


        final ArrayList<Movie> movieList = new ArrayList<>();
        thisUserHistory.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot movieSnapshot : dataSnapshot.getChildren()) {
                    Movie movie = new Movie();
                    movie.setTitle(String.valueOf(movieSnapshot.child("details").child("title").getValue()));
                    movie.setImdbID(String.valueOf(movieSnapshot.child("details").child("imdbID").getValue()));
                    movie.setImdbID(String.valueOf(movieSnapshot.child("details").child("imdbID").getValue()));
                    movieList.add(movie);
                }
                // just a test of my sorting ability
                ArrayList<Movie> testSortList = new ArrayList<>();
                Movie testMovie1 = new Movie("tt0113114", "Free Willy 2");
                testSortList.add(testMovie1);
                final LinkedList<Movie> resultList = new LinkedList<>();

                for (Movie aMovieList : movieList) {
                    boolean found = false;
                    for (Movie aTestSortList : testSortList) {
                        if (aTestSortList.getImdbID().equals(aMovieList.getImdbID())) {
                            found = true;
                        }
                    }
                    if (found) {
                        resultList.add(aMovieList);
                    }
                }
                movieList.removeAll(resultList);
                resultList.addAll(movieList);

                // end test

                HistoryAdapter adapter = new HistoryAdapter(ProfileActivity.this, resultList);
                historyListView.setAdapter(adapter);
                historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(getApplicationContext(), MovieActivity.class);
                        intent.putExtra("ID_KEY", resultList.get(i).getImdbID());
                        intent.putExtra("TITLE_KEY", resultList.get(i).getTitle());

                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (user != null) {

            applyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String newDisplayName = nameView.getText().toString().trim();
                    String newEmail = emailView.getText().toString().trim();
                    String newPass1 = passwordView1.getText().toString().trim();
                    String newPass2 = passwordView2.getText().toString().trim();

                    if (!newDisplayName.matches("")) {
                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                .setDisplayName(newDisplayName).build();
                        user.updateProfile(userProfileChangeRequest);
                    }

                    if (!newEmail.matches("")) {
                        user.updateEmail(newEmail);
                    }

                    if (newPass1.matches(newPass2)) {
                        if (!newPass1.isEmpty()) {
                            user.updatePassword(newPass1);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Passpass no Matchmatch", Toast.LENGTH_SHORT).show();
                    }
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            });
        }
    }


    private void getTriggerOptions(final Runnable onLoaded) {
        DatabaseReference adminDB = fbDB.getReference("admin");

        adminDB.child("triggerList").

                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange (DataSnapshot dataSnapshot){
                        for (DataSnapshot triggerSnapshot : dataSnapshot.getChildren()) {
                            String keyName = triggerSnapshot.getKey();
                            Log.d("keyName", keyName);
                            Trigger trigger = dataSnapshot.child(keyName).getValue(Trigger.class);
                            mTriggerList.add(trigger);
                            String triggerName = null;
                            if (trigger != null) {
                                triggerName = trigger.getTriggerName();
                            }
                            thisMyList.add(triggerName);
                            onLoaded.run();
                        }
                    }

                    @Override
                    public void onCancelled (DatabaseError databaseError){

                    }
                });
    }

    private int findIfAdmin(final FirebaseUser user){

        adminDB.child("adminUIDs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                adminUIDlist = dataSnapshot.getValue(t);
                if (adminUIDlist.contains(user.getUid())) {
                    mClearanceLevel = 1;
                    adminPanelButton.setVisibility(View.VISIBLE);
                    adminPanelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), AdminPanelActivity.class);
                            startActivity(intent);
                        }
                    });
                }

                else {
                    mClearanceLevel = 0;
                    adminPanelButton.setVisibility(View.INVISIBLE);
                }
                Toast.makeText(getApplicationContext(), "clearance level is  " + String.valueOf(mClearanceLevel), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return mClearanceLevel;
    }

}
