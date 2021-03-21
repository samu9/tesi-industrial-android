package com.example.industrial.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.industrial.R;
import com.example.industrial.models.MachineLog;

import java.util.ArrayList;

public class MachineLogAdapter extends RecyclerView.Adapter<MachineLogAdapter.ViewHolder> {
    ArrayList<MachineLog> logs;

    public MachineLogAdapter(ArrayList<MachineLog> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.log_layout, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.user.setText(logs.get(position).getUser());
        holder.action.setText(logs.get(position).getAction());
        holder.timestamp.setText(logs.get(position).getTimestamp());

    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView user, action, timestamp;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            user = itemView.findViewById(R.id.log_user);
            action = itemView.findViewById(R.id.log_action);
            timestamp = itemView.findViewById(R.id.log_timestamp);
        }
    }
}
