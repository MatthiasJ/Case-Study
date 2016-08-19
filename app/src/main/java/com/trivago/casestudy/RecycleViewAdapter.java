package com.trivago.casestudy;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.picassopalette.PicassoPalette;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.trivago.casestudy.models.Movie;

import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by Matthias on 18.08.16 at 15:00.
 * Simple RecycleViewAdapter that loads items into RecyclerView by making use of
 * ViewHolder Concept
 * Image and Memory Recource Handling is done by Picasso
 */
public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.MovieViewHolder> {


    private Context context;
    public List<Movie> movies;

    final Transformation transformation = new RoundedCornersTransformation(2, 0);


    public RecycleViewAdapter(Context context) {
        this.context = context;
    }


    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MovieViewHolder holder, int position) {

        holder.name.setText(movies.get(position).getTitle());
        holder.overview.setText(movies.get(position).getOverview());
        holder.year.setText(movies.get(position).getYear());



        String imageResource1 = movies.get(position).getImages().getFanart().getThumb();
        String imageResource2 = movies.get(position).getImages().getLogo().getFull();
        String imageResource3 = movies.get(position).getImages().getThumb().getFull();




        Picasso.with(context).load(imageResource1).transform(transformation).placeholder(R.drawable.placeholder).into(holder.image, PicassoPalette.with(movies.get(position).getImages().getFanart().getThumb(),holder.image).intoCallBack(new PicassoPalette.CallBack() {
            @Override
            public void onPaletteLoaded(Palette palette) {
             int tempColor= palette.getDarkVibrantColor(Color.BLACK);
        holder.cardView.setCardBackgroundColor(tempColor);
                holder.overview.setBackgroundColor(tempColor);
            }
        }));



    }

    @Override
    public int getItemCount() {
        if (movies != null) {
            return movies.size();
        } else
            return 0;
    }






    class MovieViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView name;
        public TextView year;
        public TextView overview;
        public CardView cardView;


        public MovieViewHolder(View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.imageView);
            name = (TextView) itemView.findViewById(R.id.title);
            year = (TextView) itemView.findViewById(R.id.year);
            overview = (TextView) itemView.findViewById(R.id.overview);
            cardView = (CardView) itemView.findViewById(R.id.card_view);

        }
    }




}



