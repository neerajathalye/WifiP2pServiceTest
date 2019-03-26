package com.group12.wifip2pservicetest;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Neeraj Athalye on 11-Mar-19.
 */
public class MyViewHolder extends RecyclerView.ViewHolder {

    public TextView nameTextView, macTextView;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        nameTextView = itemView.findViewById(R.id.nameTextView);
        macTextView = itemView.findViewById(R.id.macTextView);
    }
}
