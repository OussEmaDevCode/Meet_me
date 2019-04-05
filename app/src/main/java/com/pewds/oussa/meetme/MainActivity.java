package com.pewds.oussa.meetme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pewds.oussa.meetme.models.StoredUser;

public class MainActivity extends AppCompatActivity implements OnCompleteListener<AuthResult> {
    FirebaseAuth mAuth;
    TextInputEditText email, password, useredit = null;
    Boolean sign = true;
    AlertDialog alert;
    View main;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);
        main = findViewById(android.R.id.content);
        password = findViewById(R.id.login_password);
        email = findViewById(R.id.login_mail);
        useredit = findViewById(R.id.login_user);
        alert = new AlertDialog.Builder(MainActivity.this)
                .setCancelable(false)
                .setView(R.layout.progress)
                .create();
        final TextInputLayout userLayout = findViewById(R.id.login_user_wrapper);
        final Button login = findViewById(R.id.signIn);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline()) {
                    if (sign) {
                        if (checkSignIn(email, password)) {
                            alert.show();
                            SignIn();
                        }
                    } else {
                        if (checkSignUp(email, password, useredit)) {
                            alert.show();
                            FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        if (ds.child("userName").getValue().equals(useredit.getText().toString())) {
                                            useredit.setError("Username already exists");
                                            alert.dismiss();
                                            break;
                                        } else {
                                            SignUp();
                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }
        });
        final Button create = findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sign) {
                    sign = false;
                    userLayout.setVisibility(View.VISIBLE);
                    login.setText("Sign up");
                    create.setText("Already have an account");
                } else {
                    sign = true;
                    userLayout.setVisibility(View.GONE);
                    login.setText("Sign in");
                    create.setText("Create an account");
                }
            }
        });
    }

    private Boolean checkSignIn(TextInputEditText email, TextInputEditText password) {
        boolean status = true;
        if (password.getText() == null || password.getText().toString().isEmpty()) {
            status = false;
            password.setError("Password can't be empty");
        } else if (password.getText().toString().length() < 6) {
            password.setError("Password must be longer");
            status = false;
        }
        if (email.getText() == null || email.getText().toString().isEmpty()) {
            status = false;
            email.setError("Password can't be empty");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            status = false;
            email.setError("email format isn't correct");
        }
        return status;
    }

    private Boolean checkSignUp(TextInputEditText email, TextInputEditText password, TextInputEditText user) {
        boolean status = true;
        if (password.getText() == null || password.getText().toString().isEmpty()) {
            status = false;
            password.setError("Password can't be empty");
        } else if (password.getText().toString().length() < 6) {
            password.setError("Password must be longer");
            status = false;
        } else if(!password.getText().toString().matches(".*\\d.*")) {
            status = false;
            password.setError("Password must contain at least a number");
        }else if(!password.getText().toString().matches("(?s).*[A-Z].*")){
            status = false;
            password.setError("Password must contain a capital letter");
        }
        if (user.getText() == null || user.getText().toString().isEmpty()) {
            status = false;
            user.setError("User name can't be empty");
        } else if (user.getText().toString().length() < 4) {
            user.setError("User name must be longer");
            status = false;
        }else if (user.getText().toString().length()>40){
            user.setError("User name must be shorter");
            status = false;
        }
        if (email.getText() == null || email.getText().toString().isEmpty()) {
            status = false;
            email.setError("Password can't be empty");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            status = false;
            email.setError("email format isn't correct");
        }
        return status;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        if (task.isSuccessful()) {
            if (!sign) {
                addUserNameToUser(task.getResult().getUser());
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("Users")
                        .push()
                        .setValue(new StoredUser(useredit.getText().toString(), task.getResult().getUser().getUid()));
            }
            startActivity(new Intent(MainActivity.this, Principal.class));
            this.finish();
        } else {
            if (task.getException() != null) {
                Toast.makeText(this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Unknow error, Please try again later!", Toast.LENGTH_SHORT).show();
            }
            if (alert != null) {
                alert.dismiss();
            }
        }
    }

    private void addUserNameToUser(FirebaseUser user) {
        String username = useredit.getText().toString();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Signed Up", Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }

    public void SignIn() {
        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(this);
    }

    public void SignUp() {
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(this);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(!(netInfo != null && netInfo.isConnectedOrConnecting())){
            Snackbar.make(main,"Please check your internet connection",Snackbar.LENGTH_SHORT).show();
        }
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
