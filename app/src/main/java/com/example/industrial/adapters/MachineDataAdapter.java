package com.example.industrial.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.industrial.R;
import com.example.industrial.models.MachineData;

import java.util.List;

public class MachineDataAdapter extends RecyclerView.Adapter<MachineDataAdapter.ViewHolder> {
    List<MachineData> data;

    public MachineDataAdapter(List<MachineData> data) {
        System.out.println("constructor");
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        System.out.println("on create view holder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.machine_data, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        System.out.println("from adapter " + data.get(position).getValues()[0]);

        holder.value1.setText(Integer.toString(data.get(position).getValues()[0]));
        holder.value2.setText(Integer.toString(data.get(position).getValues()[1]));
        holder.value3.setText(Integer.toString(data.get(position).getValues()[2]));
    }

    @Override
    public int getItemCount() {

        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView value1, value2, value3;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.value1 = itemView.findViewById(R.id.value1);
            this.value2 = itemView.findViewById(R.id.value2);
            this.value3 = itemView.findViewById(R.id.value3);
        }
    }
}
