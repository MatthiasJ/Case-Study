package com.trivago.casestudy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.trivago.casestudy.models.Movie;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matthias on 15.08.16 at 17:58.
 */
public class ListAdapter extends BaseExpandableListAdapter {


    private Context context;
    private LayoutInflater inflater;
    private List<Movie> movies;

    public ListAdapter(Context context) {
        this.context = context;
        movies = new ArrayList<>();
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getGroupCount() {
        return movies.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {


        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item, null);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);

        TextView title = (TextView) convertView.findViewById(R.id.title);

        title.setText(movies.get(groupPosition).getTitle());

        Picasso.with(context).load(movies.get(groupPosition).getImages().getFanart().getThumb()).placeholder(R.drawable.placeholder).into(imageView);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public List<Movie> getMovies() {
        return movies;
    }
}
