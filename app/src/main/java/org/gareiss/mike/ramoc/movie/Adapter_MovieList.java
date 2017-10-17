package org.gareiss.mike.ramoc.movie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.model.Movie;

import java.util.ArrayList;

/**
 * Created by drue on 23.02.17.
 */

public class Adapter_MovieList extends RecyclerView.Adapter<Adapter_MovieList.MyViewHolder>
{
    private ArrayList<Movie> movieList;

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView imageView_Cover;
        public MyViewHolder(View view)
        {
            super(view);
            imageView_Cover = (ImageView) view.findViewById(R.id.item_MovieList_imageview);
        }
    }


    public Adapter_MovieList(ArrayList<Movie> results)
    {
        movieList = results;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie_list, parent, false);

        return new MyViewHolder(itemView);
    }

    public int getCount()
    {
        return movieList.size();
    }

    public Object getItem(int position)
    {
        return movieList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position)
    {
        Movie movie = movieList.get(position);
        holder.imageView_Cover.setImageBitmap(movie.getCover());

    }

    @Override
    public int getItemCount()
    {
        return movieList.size();
    }
}
