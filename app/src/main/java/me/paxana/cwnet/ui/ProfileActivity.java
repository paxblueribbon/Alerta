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
    private Button triggersButton;
    final ArrayList<Integer> selectedItems=new ArrayList<>();
    CharSequence[] cs;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseUser user = firebaseAuth.getCurrentUser();

    FirebaseDatabase fbDB = FirebaseDatabase.getInstance();
    DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
    DatabaseReference thisUserHistory = users.child(user.getUid()).child("changes");
    DatabaseReference adminDB = fbDB.getReference("admin");
    DatabaseReference adminTriggerDB = adminDB.child("triggerList");

    String[] str = null;
    List<Trigger> mTriggerList = new ArrayList<>();
    final List<String> thisMyList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameView = (EditText) findViewById(R.id.displayNameTextview);
        emailView = (EditText) findViewById(R.id.emailAddressTextview);
        passwordView1 = (EditText) findViewById(R.id.passwordTextview1);
        passwordView2 = (EditText) findViewById(R.id.passwordTextview2);
        Button applyButton = (Button) findViewById(R.id.applyButton);
        historyListView = (ListView) findViewById(R.id.historyListView);
        triggersButton = (Button) findViewById(R.id.setTriggersButton);
        final ArrayList<String> triggerOptionText = new ArrayList<>();
        final ArrayList<Trigger> listOfTriggers = new ArrayList<>();
        final LinkedList<String> linkedList = new LinkedList<>();

        str = new String[10];
        final ArrayList<Trigger> myList = new ArrayList<Trigger>();

        getTriggerOptions(new Runnable() {
            @Override
            public void run() {
                cs = thisMyList.toArray(new CharSequence[triggerOptionText.size()]);
                buildDialog();
            }
        });


        final ArrayList<Movie> movieList = new ArrayList<>();
        thisUserHistory.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot movieSnapshot : dataSnapshot.getChildren()) {
                    Movie movie = new Movie();
                    movie.setTitle(movieSnapshot.child("details").child("title").getValue().toString());
                    movie.setImdbID(movieSnapshot.child("details").child("imdbID").getValue().toString());
                    movieList.add(movie);
                }
                // just a test of my sorting ability
                ArrayList<Movie> testSortList = new ArrayList<Movie>();
                Movie testMovie1 = new Movie("tt0113114", "Free Willy 2");
                testSortList.add(testMovie1);
                final LinkedList<Movie> resultList = new LinkedList<Movie>();

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

                    if (newDisplayName.matches("")) {
                        //
                    } else {
                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                .setDisplayName(newDisplayName).build();
                        user.updateProfile(userProfileChangeRequest);
                    }

                    if (newEmail.matches("")) {
                        //
                    } else {
                        user.updateEmail(newEmail);
                    }

                    if (newPass1.matches(newPass2)) {

                        if (newPass1.isEmpty()) {
                            //
                        } else {
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
                            String triggerName = trigger.getTriggerName();
                            thisMyList.add(triggerName);
                            onLoaded.run();
                        }
                    }

                    @Override
                    public void onCancelled (DatabaseError databaseError){

                    }
                });
    }

    private void buildDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select The Applicable Triggers")
                .setMultiChoiceItems(cs, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            selectedItems.add(indexSelected);
                        } else if (selectedItems.contains(indexSelected)) {
                            // Else, if the item is already in the array, remove it
                            selectedItems.remove(Integer.valueOf(indexSelected));
                        }
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final ArrayList<Trigger> triggerPrefList = new ArrayList<>();

                        for (Integer index : selectedItems) {
                            triggerPrefList.add(mTriggerList.get(index));
                        }
                        users.child(user.getUid()).child("preferences").child("trigger").setValue(triggerPrefList);



                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
                }).create();

        triggersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
    }

}
