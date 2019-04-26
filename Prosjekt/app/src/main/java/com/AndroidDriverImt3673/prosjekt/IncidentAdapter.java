package com.AndroidDriverImt3673.prosjekt;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class IncidentAdapter extends RecyclerView.Adapter<IncidentAdapter.IncidentViewHolder> {

    private List<Incident> incidents;
    private Context context;

    public static class IncidentViewHolder extends RecyclerView.ViewHolder {
        private View IView;

        public IncidentViewHolder(View v) {
            super(v);
            IView = v;
        }

    }

    public IncidentAdapter(List<Incident> incidents, Context context) {
        this.incidents = incidents;
        this.context = context;
    }

    @NonNull
    @Override
    public IncidentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_item, parent, false);
        IncidentViewHolder holder = new IncidentViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull IncidentViewHolder holder, int i) {
        final Incident incident = incidents.get(holder.getAdapterPosition());
        ((TextView)holder.IView.findViewById(R.id.txtDesc)).setText(incident.description);
    }

    @Override
    public int getItemCount() {
        return incidents.size();
    }

}
