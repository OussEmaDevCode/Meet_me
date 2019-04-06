package com.pewds.oussa.meetme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pewds.oussa.meetme.models.StoredUser;

import java.io.ByteArrayOutputStream;
import java.net.URI;

public class MainActivity extends AppCompatActivity implements OnCompleteListener<AuthResult> {
    FirebaseAuth mAuth;
    TextInputEditText email, password, useredit = null;
    Boolean sign = true;
    AlertDialog alert;
    View main;
    private static final int PICK_IMAGE = 100;
    private static final int TAKE_PHOTO = 0;
    private Uri imageUri;
    Bitmap image;
    AlertDialog choose = null;
    AlertDialog accept = null;
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
                                    Boolean present = false;
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        if (ds.child("userName").getValue().equals(useredit.getText().toString())) {
                                            useredit.setError("Username already exists");
                                            alert.dismiss();
                                            present =true;
                                            break;
                                        }
                                    }
                                    if (!present){
                                        ShowDialogBox();
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
                if (imageUri != null) {
                    uploadURI(imageUri, task.getResult().getUser().getUid());
                } else {
                    uploadBitmap(image, task.getResult().getUser().getUid());
                }
            } else {
                startActivity(new Intent(MainActivity.this, Principal.class));
                this.finish();
            }
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

        user.updateProfile(profileUpdates);
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

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery,PICK_IMAGE);
    }
    private void openCamera(){
        Intent takephto =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takephto,TAKE_PHOTO);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        choose.dismiss();
        if(choose.isShowing()) {
            choose.dismiss();
        }
        if(choose.isShowing()) {
            choose.dismiss();
        }
        //choose form gallery
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE ){
            imageUri = data.getData();
            ImageDialgo();
        }
        //take a photo
        else if(resultCode == RESULT_OK && requestCode == TAKE_PHOTO){
            image = (Bitmap) data.getExtras().get("data");
            ImageDialgo();
        }
    }


    private void ShowDialogBox(){
        //setting up the dialog box...
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setIcon(android.R.drawable.ic_menu_camera);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setTitle("How do you want to add the photo?");
        alertDialogBuilder.setPositiveButton("from gallery",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        openGallery();

                    }
                });
        alertDialogBuilder.setNegativeButton("take a photo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openCamera();
            }
        });
        choose = alertDialogBuilder.create();
        //...showing it
        choose.show();
    }
    private  void ImageDialgo(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.pick_image, null);
        dialogBuilder.setView(dialogView);

        Button ok = dialogView.findViewById(R.id.ok);
        CircleImageView circularImageView = dialogView.findViewById(R.id.circular_image);
        if(image != null){
            circularImageView.setImageBitmap(image);
        }else {
            circularImageView.setImageURI(imageUri);
        }
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accept!=null) {
                    accept.dismiss();
                    alert.show();
                    SignUp();
                }

            }
        });
        accept = dialogBuilder.create();
        accept.show();
    }

    private void uploadBitmap(Bitmap bitmap,String id){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference filePath = FirebaseStorage.getInstance().getReference().child("images").child(id);
        UploadTask uploadTask = filePath.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(), "Signed Up", Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this, Principal.class));
                finish();
            }
        });

    }

    private void uploadURI(Uri file,String id){
        StorageReference filePath = FirebaseStorage.getInstance().getReference().child("images").child(id);
        UploadTask uploadTask = filePath.putFile(file);

// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(), "Signed Up", Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this, Principal.class));
                finish();
            }
        });
    }
}
