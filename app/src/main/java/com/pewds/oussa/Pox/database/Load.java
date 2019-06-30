package com.pewds.oussa.Pox.database;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.pewds.oussa.Pox.Intro;
import com.pewds.oussa.Pox.MainActivity;
import com.pewds.oussa.Pox.Principal;
import com.pewds.oussa.Pox.R;
import com.pewds.oussa.Pox.Send;

public class Load extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_layout);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
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
        },1000);
    }
}
