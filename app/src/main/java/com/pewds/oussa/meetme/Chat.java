package com.pewds.oussa.meetme;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ClipData;
import android.content.ClipboardManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pewds.oussa.meetme.Maps.MapsActivity;
import com.pewds.oussa.meetme.Maps.ShowActivity;
import com.pewds.oussa.meetme.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;


public class Chat extends AppCompatActivity {
    private FirebaseListAdapter<ChatMessage> adapter;
    View progressBar;
    FirebaseAuth mAuth;
    List<Double> place = new ArrayList<>();
    FloatingActionButton map = null;
    View nothing;
    Boolean stop = false;
    EditText input;
    DatabaseReference conversation;
    ClipboardManager clipboard;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(Chat.this, MainActivity.class));
            this.finish();
        }
        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        setContentView(R.layout.activity_chat);
        input = findViewById(R.id.input);
        progressBar = findViewById(R.id.progress);
        nothing = findViewById(R.id.nothing);
        FloatingActionButton fab = findViewById(R.id.fab);
        map = findViewById(R.id.map);
        setTitle(getIntent().getStringExtra("name"));
        conversation = FirebaseDatabase.getInstance().getReference().child("hi").child(getIntent().getStringExtra("key"));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isOnline()) {
                    if (input.getText() != null && !input.getText().toString().isEmpty()) {

                        conversation.push()
                                .setValue(new ChatMessage(input.getText().toString(),
                                        FirebaseAuth.getInstance()
                                                .getCurrentUser()
                                                .getDisplayName(), place, mAuth.getCurrentUser().getUid()));
                    }
                    input.clearFocus();
                    input.setText("");
                    place.clear();
                    stop = false;
                    map.setImageResource(R.drawable.ic_add_location_black_24dp);
                }
            }
        });
        displayChatMessages();
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!stop) {
                    Intent i = new Intent(Chat.this, MapsActivity.class);
                    startActivityForResult(i, 1);
                } else {
                    place.clear();
                    map.setImageResource(R.drawable.ic_add_location_black_24dp);
                    stop = false;
                }
            }
        });
        //--------------------------------------------------------------------------
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

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


    private void displayChatMessages() {
        final ListView listOfMessages = findViewById(R.id.list_of_messages);
        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setLayout(R.layout.message)
                .setQuery(conversation, ChatMessage.class)
                .setLifecycleOwner(this)
                .build();
        adapter = new FirebaseListAdapter<ChatMessage>(options) {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            protected void populateView(View v, final ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageTextMe = v.findViewById(R.id.message_text_me);
                TextView messageTextHim = v.findViewById(R.id.message_text_him);
                TextView messageTime = v.findViewById(R.id.message_time);
                ImageView locationMe = v.findViewById(R.id.place_me);
                ImageView locationHim = v.findViewById(R.id.place_him);
                if (!model.getMessageUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    //------------------------------------HIM----------------------------------------
                    locationMe.setVisibility(View.GONE);
                    messageTextMe.setVisibility(View.GONE);
                    messageTextHim.setVisibility(View.VISIBLE);
                    if (model.getMessagelocation() != null && model.getMessagelocation().size() > 2) {
                        locationHim.setVisibility(View.VISIBLE);
                        messageTextHim.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent show = new Intent(Chat.this, ShowActivity.class);
                                show.putExtra("lat", model.getMessagelocation().get(0));
                                show.putExtra("long", model.getMessagelocation().get(1));
                                show.putExtra("zoom", model.getMessagelocation().get(2));
                                startActivity(show);
                            }
                        });
                    } else {
                        locationHim.setVisibility(View.GONE);
                    }

                    messageTextHim.setText(model.getMessageText());

                } else {
                    //------------------------------------ME----------------------------------------
                    messageTextHim.setVisibility(View.GONE);
                    locationHim.setVisibility(View.GONE);
                    messageTextMe.setVisibility(View.VISIBLE);
                    if (model.getMessagelocation() != null && model.getMessagelocation().size() > 2) {
                        locationMe.setVisibility(View.VISIBLE);
                        messageTextMe.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent show = new Intent(Chat.this, ShowActivity.class);
                                show.putExtra("lat", model.getMessagelocation().get(0));
                                show.putExtra("long", model.getMessagelocation().get(1));
                                show.putExtra("zoom", model.getMessagelocation().get(2));
                                startActivity(show);
                            }
                        });
                    } else {
                        locationMe.setVisibility(View.GONE);
                    }
                    messageTextMe.setText(model.getMessageText());
                }
                // Set their text
                String time = DateFormat.format("dd MMM",
                        model.getMessageTime()).toString() + " at " + DateFormat.format("HH:mm",
                        model.getMessageTime()).toString();
                // Format the date before showing it
                messageTime.setText(time);
                if(position != 0) {
                    ChatMessage mess = adapter.getItem(position - 1);
                    if(DateFormat.format("HH",mess.getMessageTime()).equals(DateFormat.format("HH",model.getMessageTime()))){
                        messageTime.setVisibility(View.GONE);
                    }else{
                        messageTime.setVisibility(View.VISIBLE);
                    }
                }
            }
        };
        listOfMessages.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ClipData clip = ClipData.newPlainText("label", adapter.getItem(position).getMessageText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "Text copied to clip board",Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        listOfMessages.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        conversation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    nothing.setVisibility(View.VISIBLE);
                } else {
                    listOfMessages.setEmptyView(progressBar);
                    nothing.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(!(netInfo != null && netInfo.isConnectedOrConnecting())){
            Snackbar.make(findViewById(android.R.id.content),"Please check your internet connection",Snackbar.LENGTH_SHORT).show();
        }
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
