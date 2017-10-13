package me.paxana.cwnet.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

/**
 * Created by paxie on 10/12/17.
 */

public class TriggerAdapter2 extends FirebaseRecyclerAdapter {
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public TriggerAdapter2(FirebaseRecyclerOptions options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(RecyclerView.ViewHolder holder, int position, Object model) {

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }
}
