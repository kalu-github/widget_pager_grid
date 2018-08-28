package com.kalu.pagermanager;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public final class MainAdapter extends RecyclerView.Adapter<MainAdapter.MyViewHolder> {

    public static List<String> data = new ArrayList<>();

    static {
        for (int i = 1; i <= 33; i++) {
            data.add(i + "");
        }
    }

    public void clear() {
        data.clear();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final String title = data.get(position);
        holder.text.setText(title);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        public MyViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
        }
    }
}
