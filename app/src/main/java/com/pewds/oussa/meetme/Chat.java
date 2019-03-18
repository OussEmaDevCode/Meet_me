package com.pewds.oussa.meetme;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class Chat extends AppCompatActivity {
    private FirebaseListAdapter<ChatMessage> adapter;
    View progressBar;
    FirebaseAuth mAuth;
    List<Double> place = new ArrayList<>();
    int PLACE_PICKER_REQUEST = 1;
    FloatingActionButton map =null;
    Boolean stop = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null){
            startActivity(new Intent(Chat.this,MainActivity.class));
            finish();
        }
        else {
            Toast.makeText(this, "welcome " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), Toast.LENGTH_LONG).show();

        }
        setContentView(R.layout.activity_chat);
        progressBar = findViewById(R.id.progress);
        FloatingActionButton fab = findViewById(R.id.fab);
        map = findViewById(R.id.map);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = findViewById(R.id.input);
                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database

                    if (input.getText() != null && !input.getText().toString().isEmpty()) {
                        FirebaseDatabase.getInstance()
                                .getReference()
                                .push()
                                .setValue(new ChatMessage(input.getText().toString(),
                                        FirebaseAuth.getInstance()
                                                .getCurrentUser()
                                                .getDisplayName(), place,mAuth.getCurrentUser().getUid()));
                    }
                    // Clear the input
                    input.setText("");
                    place.clear();
                    map.setImageResource(R.drawable.ic_add_location_black_24dp);

            }
        });
        displayChatMessages();
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!stop) {
                    Intent i = new Intent(Chat.this, MapsActivity.class);
                    startActivityForResult(i, 1);
                }
                else {
                    place.clear();
                    map.setImageResource(R.drawable.ic_add_location_black_24dp);
                    stop = false;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                place.add(data.getDoubleExtra("lat", 0));
                place.add(data.getDoubleExtra("long", 0));
                place.add((double) data.getFloatExtra("zoom",0));
                map.setImageResource(R.drawable.ic_close_black_24dp);
                stop =true;
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    private void displayChatMessages() {
        final ListView listOfMessages = findViewById(R.id.list_of_messages);
        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setLayout(R.layout.message)
                .setQuery(FirebaseDatabase.getInstance().getReference(), ChatMessage.class)
                .setLifecycleOwner(this)
                .build();
        adapter = new FirebaseListAdapter<ChatMessage>(options) {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            protected void populateView(View v, final ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageTextMe = v.findViewById(R.id.message_text_me);
                TextView messageTextHim = v.findViewById(R.id.message_text_him);
                TextView messageUser = v.findViewById(R.id.message_user);
                TextView messageTime = v.findViewById(R.id.message_time);
                ImageView locationMe = v.findViewById(R.id.place_me);
                ImageView locationHim = v.findViewById(R.id.place_him);
                if (!model.getMessageUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    messageUser.setVisibility(View.VISIBLE);
                    messageTextMe.setVisibility(View.GONE);
                    messageTextHim.setVisibility(View.VISIBLE);
                    if (model.getMessagelocation()!= null) {
                        locationHim.setVisibility(View.VISIBLE);
                    }
                    else {
                        locationHim.setVisibility(View.GONE);
                    }
                    messageTextHim.setText(model.getMessageText());

                } else {
                    messageUser.setVisibility(View.GONE);
                    messageTextHim.setVisibility(View.GONE);
                    messageTextMe.setVisibility(View.VISIBLE);
                    if (model.getMessagelocation()!= null && model.getMessagelocation().size()>2) {
                        locationMe.setVisibility(View.VISIBLE);
                        messageTextMe.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent show = new Intent(Chat.this,ShowActivity.class);
                                show.putExtra("lat",model.getMessagelocation().get(0));
                                show.putExtra("long",model.getMessagelocation().get(1));
                                show.putExtra("zoom",model.getMessagelocation().get(2));
                                startActivity(show);
                            }
                        });
                    }
                    else {
                        locationMe.setVisibility(View.GONE);
                    }
                    messageTextMe.setText(model.getMessageText());
                }
                // Set their text
                messageUser.setText(model.getMessageUser());
                String time = DateFormat.format("dd MMM",
                        model.getMessageTime()).toString() + " at " + DateFormat.format("HH:mm",
                        model.getMessageTime()).toString();
                // Format the date before showing it
                messageTime.setText(time);
            }
        };
        listOfMessages.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listOfMessages.setEmptyView(progressBar);
    }
}
