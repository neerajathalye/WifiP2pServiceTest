package com.group12.wifip2pservicetest;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Neeraj Athalye on 11-Mar-19.
 */
public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    ArrayList<Map<String, String>> record = new ArrayList<>();
    Context context;

    public MyAdapter(ArrayList<Map<String, String>> record, Context context) {
        this.record = record;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row, viewGroup, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

        myViewHolder.nameTextView.setText(record.get(i).get("Name"));
        myViewHolder.macTextView.setText(record.get(i).get("From") + " --> " + record.get(i).get("To"));

    }

    @Override
    public int getItemCount() {
        return record.size();
    }
}
