package me.paxana.cwnet.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import me.paxana.cwnet.Model.Movie;
import me.paxana.cwnet.R;
import me.paxana.cwnet.Model.Result;
import me.paxana.cwnet.UI.MovieActivity;

/**
 * Created by paxie on 10/5/17.
 */

public class SearchAdapter extends BaseAdapter {

    private Context mContext;
    private Movie[] mMovies;

    public SearchAdapter(Context context, Movie[] movies) {
        mContext = context;
        mMovies = movies;
    };

    @Override
    public int getCount() {
        return mMovies.length;
    }

    @Override
    public Object getItem(int i) {
        return mMovies[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;  // won't be used
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.search_results, null);
            holder = new ViewHolder();
            holder.posterImageView = view.findViewById(R.id.posterImage);
            holder.titleTextView = view.findViewById(R.id.movieTitle);
            holder.yearTextView = view.findViewById(R.id.yearTextView);
            holder.topLayout = view.findViewById(R.id.lowerLayout);

            view.setTag(holder);
        }

        else {
            holder = (ViewHolder) view.getTag();
        }

        Movie movie = mMovies[i];
        Picasso.with(mContext).load(movie.getPosterURL()).into(holder.posterImageView);
        holder.titleTextView.setText(movie.getTitle());
        holder.yearTextView.setText(movie.getYear());

        return view;
    }

    private static class ViewHolder {
        ImageView posterImageView;
        TextView titleTextView;
        TextView yearTextView;
        RelativeLayout topLayout;
    }




}
