package me.paxana.cwnet.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import me.paxana.cwnet.Model.Category;
import me.paxana.cwnet.Model.Movie;
import me.paxana.cwnet.Model.Trigger;
import me.paxana.cwnet.R;
import me.paxana.cwnet.ui.MovieActivity;

/**
 * Created by paxie on 10/30/17.
 */

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private DatabaseReference userDB;
    private DatabaseReference adminDB;

    private Context mContext;
    private ArrayList<Category> _listDataHeader; // header titles
    private String imdbID;
    private String title;
    private String mUserId;

    public ExpandableListAdapter(Context context, String imdbID, String title, ArrayList<Category> listDataHeader) {
        this.mContext = context;
        this._listDataHeader = listDataHeader;
        this.imdbID = imdbID;
        this.title = title;
    }

    @Override
    public Trigger getChild(int groupPosition, int childPosititon) {
        Category category = _listDataHeader.get(groupPosition);
        ArrayList<Trigger> categoryArrayList = category.getTriggerList();

        return categoryArrayList.get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Category category = _listDataHeader.get(groupPosition);
        ArrayList<Trigger> triggerList = category.getTriggerList();

        return triggerList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        Category category = _listDataHeader.get(groupPosition);
        String headerTitle = category.getCategoryName();
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (infalInflater != null) {
                convertView = infalInflater.inflate(R.layout.list_group, null);
            }
        }

        TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public View getChildView(final int i, final int i1, boolean b, View view, ViewGroup viewGroup) {

        final ViewHolder holder;
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();



        DatabaseReference movieDB = FirebaseDatabase.getInstance().getReference("movies");
        userDB = FirebaseDatabase.getInstance().getReference("users");
        adminDB = FirebaseDatabase.getInstance().getReference("admin");

        Category category = _listDataHeader.get(i);
        ArrayList<Trigger> categoryArrayList = category.getTriggerList();

        final Trigger trigger = categoryArrayList.get(i1);
        final String tId = trigger.getId();
        final String childText = trigger.getTriggerName();
        DatabaseReference triggerDB = movieDB.child(imdbID).child("Triggers").child(tId).child("votes");

                view = LayoutInflater.from(mContext).inflate(R.layout.trigger_list_item, null);

                holder = new ViewHolder();
                holder.triggerName = view.findViewById(R.id.triggerName);
                holder.upButton = view.findViewById(R.id.mTriggerButtonUp);
                holder.downButton = view.findViewById(R.id.mTriggerButtonDown);
                holder.total = view.findViewById(R.id.triggerCounter);

                view.setTag(holder);

        holder.triggerName.setText(childText);

        if (mContext instanceof MovieActivity) {
            triggerDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("value").exists()) {
                    int value = dataSnapshot.child("value").getValue(Integer.class);
                        String valStr = String.valueOf(value);
                        holder.total.setText(valStr);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            if (user != null) {
                mUserId = user.getUid();

                userDB.child(mUserId).child("changes").child(imdbID).child("triggers").child(trigger.getId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            int userVote = dataSnapshot.getValue(Integer.class);

                            if (userVote == 0) {
                                neitherButtonIsSelected(holder, i, mUserId, tId);
                            }
                            if (userVote == 1) {
                                //user has voted yuppers
                                upButtonIsSelected(holder, i, mUserId, tId);
                            }
                            if (userVote == -1) {
                                //user has voted nopers
                                downButtonIsSelected(holder, i, mUserId, tId);
                            }
                        } else {
                            userDB.child(mUserId).child("changes").child(imdbID).child("triggers").child(trigger.getId()).setValue(0);
                            Movie movie = new Movie(imdbID, title);
                            userDB.child(mUserId).child("changes").child(imdbID).child("details").setValue(movie);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
            return view;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private static class ViewHolder {
        TextView triggerName;
        Button upButton;
        Button downButton;
        TextView total;
    }

    private void neitherButtonIsSelected(final ExpandableListAdapter.ViewHolder holder, final int i, final String mUserId, final String tId) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = firebaseAuth.getCurrentUser();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.upButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.colorAccent, null));
            holder.downButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.colorAccent, null));

        }
//        else {
//            holder.upButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.colorAccent));
//            holder.downButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.colorAccent));
//        }
        assert mUser != null;
        if (mUser.isAnonymous()) {
            holder.upButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, "Sorry, you must be signed in to vote on triggers", Toast.LENGTH_SHORT).show();
                }
            });
            holder.downButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, "Sorry, you must be signed in to vote on triggers", Toast.LENGTH_SHORT).show();
                }
            });
        }

        else {
            holder.upButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userDB.child(mUserId).child("changes").child(imdbID).child("triggers").child(tId).setValue(1);
                }
            });

            holder.downButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View view) {
                    userDB.child(mUserId).child("changes").child(imdbID).child("triggers").child(tId).setValue(-1);
                }
            });
        }
    }

    private void upButtonIsSelected(final ViewHolder holder, final int i, final String mUserId, final String tId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.upButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.votebuttons, null));
            holder.downButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.colorAccent, null));
        }
//        else {
//            holder.upButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.votebuttons));
//            holder.downButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.colorAccent));
//        }

        holder.upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userDB.child(mUserId).child("changes").child(imdbID).child("triggers").child(tId).setValue(0);
            }
        });
        holder.downButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                userDB.child(mUserId).child("changes").child(imdbID).child("triggers").child(tId).setValue(-1);
            }
        });
    }
    private void downButtonIsSelected(final ViewHolder holder, final int i, final String mUserId, final String tId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.upButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.colorAccent, null));
            holder.downButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.votebuttons, null));

        }
//        else {
//            holder.upButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.colorAccent));
//            holder.downButton.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.votebuttons));
//        }

        holder.downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userDB.child(mUserId).child("changes").child(imdbID).child("triggers").child(tId).setValue(0);
            }
        });

        holder.upButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                userDB.child(mUserId).child("changes").child(imdbID).child("triggers").child(tId).setValue(1);
            }
        });
    }
}