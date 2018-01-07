package me.paxana.cwnet.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.andremion.counterfab.CounterFab;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
    }

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
    public View getView(final int i, View view, final ViewGroup viewGroup) {
        final ViewHolder holder;

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        DatabaseReference movieDB = FirebaseDatabase.getInstance().getReference("movies");
        userDB = FirebaseDatabase.getInstance().getReference("users");
        adminDB = FirebaseDatabase.getInstance().getReference("admin");

        final String mUserId = user.getUid();
        final Trigger trigger = mTriggers.get(i);

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.trigger_list_item, null);

            holder = new ViewHolder();
            holder.triggerName = view.findViewById(R.id.triggerName);
            holder.upButton = view.findViewById(R.id.mTriggerButtonUp);
            holder.downButton = view.findViewById(R.id.mTriggerButtonDown);
            holder.total = view.findViewById(R.id.triggerCounter);

            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        holder.triggerName.setText(trigger.getTriggerName());

        if (mContext instanceof AdminPanelActivity) {
            holder.upButton.setBackgroundResource(R.drawable.icons8edit);
            holder.downButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Trigger trigger1 = mTriggers.get(i);
                    String key = trigger.getId();
                    adminDB.child("triggerList").child(key).removeValue();
                    int i = mTriggers.indexOf(trigger1);
                    mTriggers.remove(i);
                    TriggerAdapter.this.notifyDataSetChanged();
                }
            });
        }

        if (mContext instanceof MovieActivity) {
            triggerDB = movieDB.child(mImdbID).child("Triggers").child(trigger.getTriggerName());

            triggerDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            userDB.child(mUserId).child("changes").child(mImdbID).child("triggers").child(trigger.getTriggerName()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        int userVote = dataSnapshot.getValue(Integer.class);

                        if (userVote == 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                holder.upButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.colorAccent, null));
                                holder.downButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.colorAccent, null));

                            }
//                            else {
//                                holder.upButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.colorAccent));
//                                holder.downButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.colorAccent));
//                            }

                            neitherButtonIsSelected(holder, i, mUserId);
                        }
                        if (userVote == 1) {
                            //user has voted yuppers
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                holder.upButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.votebuttons, null));
                                holder.downButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.colorAccent, null));

                            }
//                            else {
//                                holder.upButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.votebuttons));
//                                holder.downButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.colorAccent));
//                            }
                            upButtonIsSelected(holder, i, mUserId);
                        }
                        if (userVote == -1) {
                            //user has voted nopers
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                holder.upButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.colorAccent, null));
                                holder.downButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.votebuttons, null));

                            }
//                            else {
//                                holder.upButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.colorAccent));
//                                holder.downButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.votebuttons));
//                            }
                            downButtonIsSelected(holder, i, mUserId);
                        }
                    }
                    else {
                        Trigger trigger1 = mTriggers.get(i);
                        userDB.child(mUserId).child("changes").child(mImdbID).child("triggers").child(trigger1.getTriggerName()).setValue(0);
                        Movie movie = new Movie(mImdbID, mTitle);
                        userDB.child(mUserId).child("changes").child(mImdbID).child("details").setValue(movie);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        return view;
    }

    private static class ViewHolder {
        TextView triggerName;
        Button upButton;
        Button downButton;
        TextView total;
    }
    private void neitherButtonIsSelected(final ViewHolder holder, final int i, final String mUserId) {
        final DatabaseReference movieDB = FirebaseDatabase.getInstance().getReference("movies");

        holder.upButton.setOnClickListener(new View.OnClickListener() {
            Trigger trigger1 = mTriggers.get(i);
            DatabaseReference triggerDB = movieDB.child(mImdbID).child("Triggers").child(trigger1.getTriggerName());

            @Override
            public void onClick(View view) {
                Log.d("fvck", trigger1.getTriggerName());

                triggerDB.child("triggerVotesYes").runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        int count = mutableData.getValue(Integer.class);
                        mutableData.setValue(count + 1);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                    }
                });
                triggerDB.child("triggerVotesTotal").runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        int count = mutableData.getValue(Integer.class);
                        mutableData.setValue(count + 1);

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                    }
                });
                userDB.child(mUserId).child("changes").child(mImdbID).child("triggers").child(trigger1.getTriggerName()).setValue(1);
            }
        });

        holder.downButton.setOnClickListener(new View.OnClickListener() {
            Trigger trigger1 = mTriggers.get(i);
            DatabaseReference triggerDB = movieDB.child(mImdbID).child("Triggers").child(trigger1.getTriggerName());

            @Override
            public void onClick(final View view) {

                triggerDB.child("triggerVotesTotal").runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        int count = mutableData.getValue(Integer.class);
                        mutableData.setValue(count + 1);

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    }
                });
                userDB.child(mUserId).child("changes").child(mImdbID).child("triggers").child(trigger1.getTriggerName()).setValue(-1);
            }
        });
    }

    private void upButtonIsSelected(final ViewHolder holder, final int i, final String mUserId) {
        final DatabaseReference movieDB = FirebaseDatabase.getInstance().getReference("movies");

        holder.upButton.setOnClickListener(null);
        holder.downButton.setOnClickListener(new View.OnClickListener() {
            Trigger trigger1 = mTriggers.get(i);
            DatabaseReference triggerDB = movieDB.child(mImdbID).child("Triggers").child(trigger1.getTriggerName());

            @Override
            public void onClick(final View view) {
                triggerDB.child("triggerVotesYes").runTransaction(new Transaction.Handler() {
                    Trigger trigger1 = mTriggers.get(i);
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        int count = mutableData.getValue(Integer.class);
                        mutableData.setValue(count - 1);
                        userDB.child(mUserId).child("changes").child(mImdbID).child("triggers").child(trigger1.getTriggerName()).setValue(-1);
                        return Transaction.success(mutableData);
                    }
                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                    }
                });
                userDB.child(mUserId).child("changes").child(mImdbID).child("triggers").child(trigger1.getTriggerName()).setValue(-1);
            }
        });
    }
    private void downButtonIsSelected(final ViewHolder holder, final int i, final String mUserId) {
        final DatabaseReference movieDB = FirebaseDatabase.getInstance().getReference("movies");

        holder.downButton.setOnClickListener(null);
        holder.upButton.setOnClickListener(new View.OnClickListener() {
            Trigger trigger1 = mTriggers.get(i);
            DatabaseReference triggerDB = movieDB.child(mImdbID).child("Triggers").child(trigger1.getTriggerName());

            @Override
            public void onClick(final View view) {
                triggerDB.child("triggerVotesYes").runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        int count = mutableData.getValue(Integer.class);
                        mutableData.setValue(count + 1);
                        userDB.child(mUserId).child("changes").child(mImdbID).child("triggers").child(trigger1.getTriggerName()).setValue(1);
                        return Transaction.success(mutableData);
                    }
                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                    }
                });
                userDB.child(mUserId).child("changes").child(mImdbID).child("triggers").child(trigger1.getTriggerName()).setValue(1);
            }
        });
    }
}
