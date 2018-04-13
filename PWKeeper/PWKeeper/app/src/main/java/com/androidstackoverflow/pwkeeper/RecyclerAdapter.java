package com.androidstackoverflow.pwkeeper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    static List<DatabaseModel> dbList;
    static private Context context;

    RecyclerAdapter(Context context, List<DatabaseModel> dbList) {

        RecyclerAdapter.dbList = new ArrayList<>();
        RecyclerAdapter.context = context;
        RecyclerAdapter.dbList = dbList;
    }

    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, null);
        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.ViewHolder holder, int position) {
        holder.rowid.setText(dbList.get(position).getRowid());
        holder.website.setText(dbList.get(position).getWebsite());
    }

    @Override
    public int getItemCount() {

        return dbList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView website, rowid;

        public ViewHolder(View itemView) {
            super(itemView);

            rowid = (TextView) itemView.findViewById(R.id.rvROWID);
            website = (TextView) itemView.findViewById(R.id.rvWEBSITE);

            // Attach a click listener to the entire row view
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            Intent intentN = new Intent(context, DetailsActivity.class);
            Bundle extras = new Bundle();
            extras.putInt("POSITION", getAdapterPosition());
            extras.putString("FROM_LIST_ACTIVITY", "false");
            //position = getAdapterPosition();
            //position = getLayoutPosition();// Both work the same
            intentN.putExtras(extras);

            context.startActivity(intentN);
        }
    }
}

