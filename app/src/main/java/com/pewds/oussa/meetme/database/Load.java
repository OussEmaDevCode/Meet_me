package com.pewds.oussa.meetme.database;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.pewds.oussa.meetme.MainActivity;
import com.pewds.oussa.meetme.Principal;
import com.pewds.oussa.meetme.Send;

public class Load extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(Load.this, MainActivity.class));
        } else {
            if (getIntent() != null && getIntent().getStringExtra("type") != null
                    && getIntent().getStringExtra("type").equals("widget")) {
                startActivity(new Intent(Load.this, Send.class));
            } else {
                startActivity(new Intent(Load.this, Principal.class));
            }
        }
    }
}
