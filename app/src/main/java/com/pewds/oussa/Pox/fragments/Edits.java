package com.pewds.oussa.Pox.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.pewds.oussa.Pox.R;

public class Edits extends Fragment {
    TextInputEditText user,email,password;
    Button login,create;
    OnDataPass dataPasser;
    Boolean sign = true;

    public Edits() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edits, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        user = view.findViewById(R.id.login_user);
        email = view.findViewById(R.id.login_mail);
        password = view.findViewById(R.id.login_password);
        login = view.findViewById(R.id.signIn);
        create = view.findViewById(R.id.create);
        final TextInputLayout userLayout = view.findViewById(R.id.login_user_wrapper);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sign){
                    if(checkSignIn()){
                        dataPasser.onDataPass(null,email.getText().toString().trim()
                                ,password.getText().toString().trim());
                    }
                }else {
                    if(checkSignUp()){
                        dataPasser.onDataPass(user.getText().toString().trim()
                                ,email.getText().toString().trim()
                                ,password.getText().toString().trim());
                    }
                }
            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sign) {
                    sign = false;
                    userLayout.setVisibility(View.VISIBLE);
                    user.requestFocus();
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }

    public interface OnDataPass {
        void onDataPass(String name,String email,String pass);
    }

    private Boolean checkSignIn() {
        boolean status = true;
        if (password.getText() == null || password.getText().toString().trim().isEmpty()) {
            status = false;
            password.setError("Password can't be empty");
        } else if (password.getText().toString().trim().length() < 6) {
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

    private Boolean checkSignUp() {
        boolean status = true;
        if (password.getText() == null || password.getText().toString().trim().isEmpty()) {
            status = false;
            password.setError("Password can't be empty");
        } else if (password.getText().toString().trim().length() < 6) {
            password.setError("Password must be longer");
            status = false;
        } else if (!password.getText().toString().matches(".*\\d.*")) {
            status = false;
            password.setError("Password must contain at least a number");
        } else if (!password.getText().toString().matches("(?s).*[A-Z].*")) {
            status = false;
            password.setError("Password must contain a capital letter");
        }
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
        if (email.getText() == null || email.getText().toString().isEmpty()) {
            status = false;
            email.setError("Password can't be empty");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            status = false;
            email.setError("email format isn't correct");
        }
        return status;
    }
}
