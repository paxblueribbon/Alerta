package me.paxana.cwnet.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import me.paxana.cwnet.Adapters.TriggerAdapter;
import me.paxana.cwnet.Model.Trigger;
import me.paxana.cwnet.R;

public class AdminPanelActivity extends AppCompatActivity {


    ListView mTriggerListView;
    EditText mTriggerAddEdittext;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mUser;
    DatabaseReference adminDB;
    ArrayList<Trigger> adminTriggerList;
    Button addTriggerButton;
    TriggerAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mUser = mFirebaseAuth.getCurrentUser();
        adminDB = FirebaseDatabase.getInstance().getReference("admin");
        addTriggerButton = (Button) findViewById(R.id.addTriggerButton);

        mTriggerListView = (ListView) findViewById(R.id.triggerListView);
        mTriggerAddEdittext = (EditText) findViewById(R.id.triggerAddEdittext);

        populateAdminTriggers(new Runnable() {
            @Override
            public void run() {
                mAdapter = new TriggerAdapter(AdminPanelActivity.this, adminTriggerList);
                mTriggerListView.setAdapter(mAdapter);
            }
        });


        addTriggerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newTriggerTitle = mTriggerAddEdittext.getText().toString();
                Date date = new Date();
                Long timeInMilli = date.getTime();
                String id = timeInMilli.toString();
                Trigger trigger = new Trigger(newTriggerTitle, id, 0, 0, null);
                adminDB.child("triggerList").child(newTriggerTitle).setValue(trigger);

                adminTriggerList.add(trigger);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void populateAdminTriggers(final Runnable runnable) {
        adminTriggerList = new ArrayList<>();


        adminDB.child("triggerList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot triggerSnapshot : dataSnapshot.getChildren()) {

                    String name = String.valueOf(triggerSnapshot.child("triggerName").getValue());
                    Trigger trigger = new Trigger();
                    trigger.setTriggerName(name);
                    trigger.setTriggerVotesTotal(0);
                    trigger.setTriggerVotesYes(0);
                    adminTriggerList.add(trigger);
                }
                runnable.run();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }
}
