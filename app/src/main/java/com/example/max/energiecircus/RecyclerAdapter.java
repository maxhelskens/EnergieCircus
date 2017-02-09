package com.example.max.energiecircus;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Max on 22/11/16.
 */

public class RecyclerAdapter  extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView schoolimage;
        public TextView schoolname, classname, highscore;

        public ViewHolder(View itemView) {
            super(itemView);
            schoolimage = (ImageView) itemView.findViewById(R.id.card_imageview);
            schoolname = (TextView) itemView.findViewById(R.id.card_schoolname);
            classname = (TextView) itemView.findViewById(R.id.card_classname);
            highscore = (TextView) itemView.findViewById(R.id.card_highscore);
        }
    }

    public ArrayList<Classroom> dataSet;

    public RecyclerAdapter(ArrayList<Classroom> dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.school_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.schoolname.setText(dataSet.get(position).getGroepsnaam());
        holder.classname.setText(dataSet.get(position).getClassname());
        holder.highscore.setText("Resterende energie: " + dataSet.get(position).getHighscore() + " Watt per leerling");
        switch(position){
            case 0:
                holder.schoolimage.setImageResource(R.drawable.goud);
                break;
            case 1:
                holder.schoolimage.setImageResource(R.drawable.zilver);
                break;
            case 2:
                holder.schoolimage.setImageResource(R.drawable.bronze);
                break;
            default:
                holder.schoolimage.setImageResource(R.drawable.rest);
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}