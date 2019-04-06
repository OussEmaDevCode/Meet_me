package com.pewds.oussa.meetme;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.pewds.oussa.meetme.models.ChatMessage;
import com.pewds.oussa.meetme.models.conversation;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class Send extends AppCompatActivity {
    FirebaseAuth mAuth;
    private FirebaseListAdapter<conversation> ConverAdapter;
    DatabaseReference me;
    View nothing;
    View progressBar;
    EditText message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(Send.this, MainActivity.class));
            this.finish();
        }
        setContentView(R.layout.activity_send);
        progressBar = findViewById(R.id.progress);
        nothing = findViewById(R.id.nothing);
        message = findViewById(R.id.message_text);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if ("android.intent.action.SEND".equals(action) && type != null && "text/plain".equals(type)) {
            message.setText(intent.getStringExtra("android.intent.extra.TEXT"));
        }
        if (mAuth.getCurrentUser() != null) {
            Query databaseReference = FirebaseDatabase.getInstance().getReference("Users").orderByChild("userId").equalTo(mAuth.getCurrentUser().getUid());
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        me = ds.getRef();
                        me.keepSynced(true);
                        displayConversations();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }
    }

    private void displayConversations() {
        final ListView conversations = findViewById(R.id.list_of_conversations);
        FirebaseListOptions<conversation> options = new FirebaseListOptions.Builder<conversation>()
                .setLayout(R.layout.conversation)
                .setQuery(me.child("conversations"), conversation.class)
                .setLifecycleOwner(this)
                .build();
        ConverAdapter = new FirebaseListAdapter<conversation>(options) {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            protected void populateView(View v, final conversation model, int position) {
                TextView name = v.findViewById(R.id.conver);
                final CircleImageView profile = v.findViewById(R.id.profile);
                final TextView last = v.findViewById(R.id.last);
                View parent = v.findViewById(R.id.parent);
                name.setText(model.getUserName());
                last(model.getConversationId(),last);
                FirebaseStorage.getInstance().getReference().child("images").child(model.getUserId())
                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get()
                                .load(uri)
                                .resize(58, 58)
                                .centerCrop()
                                .into(profile);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        profile.setImageResource(R.drawable.ic_person_black_24dp);
                    }
                });
                parent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isOnline()) {
                            if (message.getText() != null && !message.getText().toString().isEmpty()) {
                                FirebaseDatabase.getInstance().getReference().child("hi").child(model.getConversationId()).push()
                                        .setValue(new ChatMessage(message.getText().toString(),
                                                FirebaseAuth.getInstance()
                                                        .getCurrentUser()
                                                        .getDisplayName(), null, mAuth.getCurrentUser().getUid()))
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(getApplicationContext(),"sent",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                last(model.getConversationId(),last);
                            }
                        }
                    }
                });
            }
        };
        me.child("conversations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    nothing.setVisibility(View.VISIBLE);
                } else {
                    conversations.setEmptyView(progressBar);
                    nothing.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        conversations.setAdapter(ConverAdapter);
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
    public void last(String conv ,final TextView last){
        Query lastQuery = FirebaseDatabase.getInstance().getReference().child("hi").child(conv).orderByKey().limitToLast(1);
        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if(ds.child("messageUserId").getValue().toString().equals(mAuth.getCurrentUser().getUid())) {
                        last.setText("You: "+ds.child("messageText").getValue().toString());
                    }else {
                        last.setText(ds.child("messageText").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
