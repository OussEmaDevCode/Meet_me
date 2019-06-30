package com.pewds.oussa.Pox.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.pewds.oussa.Pox.Maps.MapsActivity;
import com.pewds.oussa.Pox.R;

import java.util.ArrayList;
import java.util.List;


public class Pox extends Fragment {

    private PoxInterface mListener;
    private TextInputEditText pox;
    private Button what;
    AlertDialog how;
    private FloatingActionButton map;
    List<Double> place = new ArrayList<>();
    Boolean stop = false;
    Button submit;
    public Pox() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pox, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (PoxInterface) context;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                place.add(data.getDoubleExtra("lat", 0));
                place.add(data.getDoubleExtra("long", 0));
                place.add((double) data.getFloatExtra("zoom", 0));
                map.setImageResource(R.drawable.ic_close_black_24dp);
                stop = true;
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pox = view.findViewById(R.id.pox);
        map = view.findViewById(R.id.map);
        what = view.findViewById(R.id.what);
        what.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                how.show();
            }
        });
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!stop) {
                    Intent i = new Intent(getActivity(), MapsActivity.class);
                    startActivityForResult(i, 1);
                } else {
                    place.clear();
                    map.setImageResource(R.drawable.ic_add_location_black_24dp);
                    stop = false;
                }
            }
        });

        submit = view.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pox.getText() == null || pox.getText().toString().trim().isEmpty()){
                    pox.setError("Please enter a Pox");
                }else {
                    if(place.size() > 1){
                        mListener.onPoxChanged(pox.getText().toString()
                                +","+Double.toString(place.get(0))
                                +","+Double.toString(place.get(1))
                                +","+Double.toString(place.get(2)));
                    }else {
                        mListener.onPoxChanged(pox.getText().toString());
                    }
                }
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder= new AlertDialog.Builder(getContext())
                .setTitle("Poxes")
                .setMessage("Poxes are quick messages you send by just pressing a button !")
                .setIcon(R.drawable.ic_info_black_24dp)
                .setPositiveButton("got it !",null);
        how = builder.create();
    }

    public interface PoxInterface{
        // TODO: Update argument type and name
        void onPoxChanged(String pox);
    }
}
