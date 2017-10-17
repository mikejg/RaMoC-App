package org.gareiss.mike.ramoc.movie;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.model.Actor;

import java.util.ArrayList;

/**
 * Created by drue on 27.02.17.
 */

public class Adapter_ActorList extends RecyclerView.Adapter<Adapter_ActorList.MyViewHolder>
{

    private ArrayList<Actor> actorList;

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView imageView_Cover;
        public TextView textView_Name;
        public TextView textView_Charactar;
        public MyViewHolder(View view)
        {
            super(view);
            imageView_Cover = (ImageView) view.findViewById(R.id.item_ActorList_ImageView);
            textView_Name = (TextView) view.findViewById(R.id.item_ActorList_TextView_Name);
            textView_Charactar = (TextView) view.findViewById(R.id.item_ActorList_TextView_Character);
        }
    }

    public Adapter_ActorList(ArrayList<Actor> results)
    {
        actorList = results;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_actor_list, parent, false);

        return new MyViewHolder(itemView);
    }

    public int getCount()
    {
        return actorList.size();
    }

    public Object getItem(int position)
    {
        return actorList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position)
    {
        Actor actor = actorList.get(position);
        holder.imageView_Cover.setImageBitmap(actor.getPortrait());
        holder.textView_Name.setText(actor.getName());
        holder.textView_Charactar.setText(actor.getCharacter());
    }

    @Override
    public int getItemCount()
    {
        return actorList.size();
    }
}

