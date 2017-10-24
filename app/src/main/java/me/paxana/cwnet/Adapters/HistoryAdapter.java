package me.paxana.cwnet.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import me.paxana.cwnet.Model.Movie;

/**
 * Created by paxie on 10/19/17.
 */

public class HistoryAdapter extends BaseAdapter{
    private Context mContext;
    private List<Movie> mMoviesHistory;

    public HistoryAdapter(Context context, List<Movie> moviesHistory) {
        mContext = context;
        mMoviesHistory = moviesHistory;
    }

    @Override
    public int getCount() {
        return this.mMoviesHistory.size();
    }

    @Override
    public Object getItem(int i) {
        return mMoviesHistory.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_2, null);

            holder = new ViewHolder();
            holder.movieTitle = view.findViewById(android.R.id.text1);
            holder.movieIMDB = view.findViewById(android.R.id.text2);

            view.setTag(holder);

        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        final Movie movie = mMoviesHistory.get(i);

        holder.movieTitle.setText(movie.getTitle());
        holder.movieIMDB.setText(movie.getImdbID());

        return view;
    }
    private static class ViewHolder {
        TextView movieTitle;
        TextView movieIMDB;
    }
}
