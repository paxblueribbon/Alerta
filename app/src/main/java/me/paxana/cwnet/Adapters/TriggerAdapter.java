package me.paxana.cwnet.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import me.paxana.cwnet.Model.Movie;
import me.paxana.cwnet.Model.Trigger;
import me.paxana.cwnet.R;
import me.paxana.cwnet.ui.AdminPanelActivity;
import me.paxana.cwnet.ui.MovieActivity;

/**
 * Created by paxie on 10/11/17.
 */

public class TriggerAdapter extends BaseAdapter {

    private Context mContext;
    private List<Trigger> mTriggers;
    private Movie mMovie;
    private String mImdbID;
    private String mTitle;
    private String mYear;
    private String mPosterURL;

    private DatabaseReference movieDB;
    private DatabaseReference userDB;
    private DatabaseReference triggerDB;
    private DatabaseReference adminDB;


    public TriggerAdapter(Context context, List<Trigger> triggerList, Movie movie, String imdbID){
        mContext = context;
        this.mTriggers = triggerList;
        mMovie = movie;
        mImdbID = imdbID;
        mTitle = movie.getTitle();
        mYear = movie.getYear();
        mPosterURL = movie.getPosterURL();
    };

    public TriggerAdapter(Context context, List<Trigger> triggerList) {
        mContext = context;
        this.mTriggers = triggerList;
    }

    @Override
    public int getCount() {
        return this.mTriggers.size();
    }

    @Override
    public Object getItem(int i) {
        return mTriggers.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        movieDB = FirebaseDatabase.getInstance().getReference("movies");
        userDB = FirebaseDatabase.getInstance().getReference("users");
        adminDB = FirebaseDatabase.getInstance().getReference("admin");

        final String mUserId = user.getUid();



        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.trigger_list_item, null);

            holder = new ViewHolder();
            holder.triggerName = view.findViewById(R.id.triggerName);
            holder.upButton = view.findViewById(R.id.mTriggerButtonUp);
            holder.downButton = view.findViewById(R.id.mTriggerButtonDown);
            holder.total = view.findViewById(R.id.triggerCounter);
            if (mContext instanceof AdminPanelActivity) {
                holder.upButton.setVisibility(View.INVISIBLE);
            }

            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        final Trigger trigger = mTriggers.get(i);
        holder.triggerName.setText(trigger.getTriggerName());

        if (mContext instanceof MovieActivity) {
            holder.upButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    triggerDB = movieDB.child(mImdbID).child("Triggers").child(trigger.getTriggerName()).child("triggerVotesYes");
                    triggerDB.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            int count = mutableData.getValue(Integer.class);
                            mutableData.setValue(count + 1);
                            userDB.child(mUserId).child("changes").child(mImdbID).setValue(mMovie);
                            userDB.child(mUserId).child("changes").child(mImdbID).child("triggers").child(trigger.getTriggerName()).setValue(1);
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                        }
                    });
                    ;
                    holder.upButton.setTypeface(null, Typeface.BOLD);

                }
            });
            holder.downButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    triggerDB = movieDB.child(mImdbID).child("Triggers").child(trigger.getTriggerName()).child("triggerVotesYes");
                    triggerDB.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            int count = mutableData.getValue(Integer.class);
                            mutableData.setValue(count - 1);
                            userDB.child(mUserId).child("changes").child(mImdbID).setValue(mMovie);
                            userDB.child(mUserId).child("changes").child(mImdbID).child("triggers").child(trigger.getTriggerName()).setValue(-1);
                            return Transaction.success(mutableData);
                        }


                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                        }
                    });
                }
            });
        }
        holder.total.setText(String.format(Integer.toString(trigger.getTriggerVotesYes())));

        return view;
    }

    private static class ViewHolder {
        TextView triggerName;
        Button upButton;
        Button downButton;
        TextView total;
    }
}
