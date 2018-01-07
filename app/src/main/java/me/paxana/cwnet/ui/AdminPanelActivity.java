package me.paxana.cwnet.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
    ArrayList<String> categoryList;
    Button addTriggerButton;
    TriggerAdapter mAdapter;
    CheckBox mCheckBox1;
    CheckBox mCheckBox2;
    CheckBox mCheckBox3;
    CheckBox mCheckBox4;
    CheckBox mCheckBox5;
    CheckBox mCheckBox6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mUser = mFirebaseAuth.getCurrentUser();
        adminDB = FirebaseDatabase.getInstance().getReference("admin");
        addTriggerButton = findViewById(R.id.addTriggerButton);

        final ArrayList<CheckBox> cbs = new ArrayList<>();

        mCheckBox1 = findViewById(R.id.checkBox);
        mCheckBox2 = findViewById(R.id.checkBox2);
        mCheckBox3 = findViewById(R.id.checkBox3);
        mCheckBox4 = findViewById(R.id.checkBox4);
        mCheckBox5 = findViewById(R.id.checkBox5);
        mCheckBox6 = findViewById(R.id.checkBox6);

        cbs.add(mCheckBox1);
        cbs.add(mCheckBox2);
        cbs.add(mCheckBox3);
        cbs.add(mCheckBox4);
        cbs.add(mCheckBox5);
        cbs.add(mCheckBox6);

        final ArrayList<String> theList = new ArrayList<>();
        theList.add("Category 1");

        mTriggerListView = findViewById(R.id.triggerListView);
        mTriggerAddEdittext = findViewById(R.id.triggerAddEdittext);

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
                ArrayList<String> catList = new ArrayList<>();
                for (CheckBox cb : cbs) {
                    if (cb.isChecked()) {
                        catList.add(cb.getText().toString());
                    }
                }
                Log.d("PLURRR", catList.toString());
                String newTriggerTitle = mTriggerAddEdittext.getText().toString();
                String id = adminDB.push().getKey();
                Trigger trigger = new Trigger(newTriggerTitle, catList, id);
                adminDB.child("triggerList").child(id).setValue(trigger);

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
                    String id = String.valueOf(triggerSnapshot.child("id").getValue());
                    ArrayList<String> testCatList = new ArrayList<>();
                    Trigger trigger = new Trigger(name, testCatList, id);
                    trigger.setTriggerName(name);
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
