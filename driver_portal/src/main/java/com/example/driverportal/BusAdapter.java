package com.example.driverportal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;


public class BusAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> busNumbers;

    public BusAdapter(Context context, List<String> busNumbers) {
        super(context, R.layout.home_list_item, busNumbers);
        this.context = context;
        this.busNumbers = busNumbers;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.home_list_item, parent, false);
        }

        // Assuming that home_list_item.xml contains a TextView with the id "busNumberTextView"
        TextView busNumberTextView = convertView.findViewById(R.id.bus_num);
        busNumberTextView.setText("Bus No "+busNumbers.get(position));

        return convertView;
    }
}

