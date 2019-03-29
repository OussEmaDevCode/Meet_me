package com.pewds.oussa.meetme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class Load extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivity(new Intent(Load.this,MainActivity.class));
        }
        else {
            startActivity(new Intent(Load.this,Principal.class));
        }
    }
}
