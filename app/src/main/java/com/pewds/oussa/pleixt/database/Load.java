package com.pewds.oussa.pleixt.database;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.pewds.oussa.pleixt.Intro;
import com.pewds.oussa.pleixt.MainActivity;
import com.pewds.oussa.pleixt.Principal;
import com.pewds.oussa.pleixt.Send;

public class Load extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        if(getSharedPreferences("Meet", MODE_PRIVATE).getBoolean("first",true)){
            startActivity(new Intent(Load.this,Intro.class));
        }else {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivity(new Intent(Load.this, MainActivity.class));
                finish();
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
}
