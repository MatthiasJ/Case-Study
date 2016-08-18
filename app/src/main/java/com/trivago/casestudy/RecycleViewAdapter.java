package com.trivago.casestudy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.trivago.casestudy.models.Movie;

import java.util.List;

/**
 * Created by Matthias on 18.08.16 at 15:00.
 */
public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.MovieViewHolder> {


    private Context context;
    public List<Movie> movies;


    public RecycleViewAdapter(Context context) {
        this.context = context;
    }


    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {

        holder.movieName.setText(movies.get(position).getTitle());
        Picasso.with(context).load(movies.get(position).getImages().getFanart().getThumb()).placeholder(R.drawable.placeholder).into(holder.movieImage);
    }

    @Override
    public int getItemCount() {
        if (movies != null) {
            return movies.size();
        } else
            return 0;
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {

        public ImageView movieImage;
        public TextView movieName;


        public MovieViewHolder(View itemView) {
            super(itemView);

            movieImage = (ImageView) itemView.findViewById(R.id.imageView);
            movieName = (TextView) itemView.findViewById(R.id.title);
        }
    }
}



