package com.pewds.oussa.Pox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pewds.oussa.Pox.Maps.MapsActivity;
import com.pewds.oussa.Pox.models.StoredUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.Inflater;

public class Settings extends AppCompatActivity {
    FirebaseAuth mAuth;
    androidx.appcompat.app.AlertDialog alert;
    View sets;
    TextView name;
    List<Double> place = new ArrayList<>();
    Boolean stop = false;
    FloatingActionButton fab;
    CircleImageView circleImageView;
    String PhotoUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sets = findViewById(R.id.sets);
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(Settings.this, MainActivity.class));
                    finish();
                }
            }
        });
        alert = new androidx.appcompat.app.AlertDialog.Builder(Settings.this)
                .setCancelable(false)
                .setView(R.layout.progress)
                .create();
        circleImageView = findViewById(R.id.photo);
        PhotoUri = mAuth.getCurrentUser().getPhotoUrl().toString();
        if(PhotoUri!= null && !PhotoUri.equals("")) {
            Picasso.get()
                    .load(PhotoUri)
                    .resize(150, 150)
                    .centerCrop()
                    .into(circleImageView);
        }else {
            circleImageView.setImageResource(R.drawable.ic_profile_user);
        }
        name = findViewById(R.id.userName);
        name.setText(mAuth.getCurrentUser().getDisplayName());
        TextView date = findViewById(R.id.date);
        String time = DateFormat.format("MMMM yyyy",
                mAuth.getCurrentUser().getMetadata().getCreationTimestamp()).toString();
        date.setText("joined pox on "+time);
        fab = findViewById(R.id.map);
        final EditText pox = findViewById(R.id.ediTpox);
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(mAuth.getCurrentUser().getUid())
                .child("pox").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String[] poxValue = dataSnapshot.getValue().toString().split(",");
                pox.setText(poxValue[0]);
                if(poxValue.length>2){
                    stop = true;
                    fab.setImageResource(R.drawable.ic_close_black_24dp);
                    place.add(Double.valueOf(poxValue[1]));
                    place.add(Double.valueOf(poxValue[2]));
                    place.add(Double.valueOf(poxValue[3]));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        pox.setImeOptions(EditorInfo.IME_ACTION_DONE);
        pox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE && isOnline()){
                    OnCompleteListener onCompleteListener = new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(),"Pox changed",Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                    if(place.size()>1) {
                        FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid())
                                .child("pox").setValue(pox.getText().toString()
                                +","+Double.toString(place.get(0))
                                +","+Double.toString(place.get(1))
                                +","+Double.toString(place.get(2))).addOnCompleteListener(onCompleteListener);
                    }else {
                        FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid())
                                .child("pox").setValue(pox.getText().toString()).addOnCompleteListener(onCompleteListener);
                    }
                    return true;
                }
                return false;
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!stop) {
                    Intent i = new Intent(Settings.this, MapsActivity.class);
                    startActivityForResult(i, 1);
                } else {
                    place.clear();
                    fab.setImageResource(R.drawable.ic_add_location_black_24dp);
                    stop = false;
                }
            }
        });
        final EditText nameEdit = findViewById(R.id.editName);
        nameEdit.setText(mAuth.getCurrentUser().getDisplayName());
        nameEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        nameEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    if(isOnline() && checkName(nameEdit) && !nameEdit.getText().toString().equals(mAuth.getCurrentUser().getDisplayName())){
                    alert.show();
                    FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Boolean present = false;
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                if (ds.child("userName").getValue().equals(nameEdit.getText().toString())) {
                                    nameEdit.setError("User name already exists");
                                    alert.dismiss();
                                    present = true;
                                    break;
                                }
                            }
                            if (!present) {
                                addUserNameToUser(nameEdit.getText().toString(),mAuth.getCurrentUser());
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    }
                    return true;
                }
                return false;
            }
        });
        findViewById(R.id.editPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.this, editsActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.signOut) {
            final AlertDialog.Builder signOut = new AlertDialog.Builder(Settings.this).setTitle("Sign out")
                    .setMessage("Are you sure you want to sign out ?")
                    .setIcon(R.drawable.exit_black)
                    .setCancelable(true)
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAuth.signOut();
                        }
                    }).setNegativeButton("no", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            AlertDialog a = signOut.create();
            a.show();
        }else if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                place.add(data.getDoubleExtra("lat", 0));
                place.add(data.getDoubleExtra("long", 0));
                place.add((double) data.getFloatExtra("zoom", 0));
                fab.setImageResource(R.drawable.ic_close_black_24dp);
                stop = true;
            }
        }
    }
    public boolean checkName(EditText user){
        boolean status = true;
        if (user.getText() == null || user.getText().toString().trim().isEmpty()) {
            status = false;
            user.setError("User name can't be empty");
        } else if (user.getText().toString().trim().length() < 4) {
            user.setError("User name must be longer");
            status = false;
        } else if (user.getText().toString().trim().length() > 25) {
            user.setError("User name must be shorter");
            status = false;
        }
        return status;
    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (!(netInfo != null && netInfo.isConnectedOrConnecting())) {
            Snackbar.make(sets, "Please check your internet connection", Snackbar.LENGTH_SHORT).show();
        }
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    private void addUserNameToUser(final String Name, final FirebaseUser user) {
        String username = Name;

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child("Users")
                            .child(user.getUid())
                            .child("userName")
                            .setValue(Name);
                    name.setText(Name);
                    alert.dismiss();
                    Toast.makeText(getApplicationContext(),"Username changed",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
