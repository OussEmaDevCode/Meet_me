package com.pewds.oussa.Pox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.pewds.oussa.Pox.NonSwipeableViewPager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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
import com.pewds.oussa.Pox.fragments.Edits;
import com.pewds.oussa.Pox.fragments.Image;
import com.pewds.oussa.Pox.fragments.Pox;
import com.pewds.oussa.Pox.models.StoredUser;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity implements OnCompleteListener<AuthResult>,Edits.OnDataPass,Image.ImageData ,Pox.PoxInterface {
    FirebaseAuth mAuth;
    FirebaseUser user;
    Boolean sign = true;
    AlertDialog alert;
    View main;
    Bitmap image;
    String Pass;
    String Email;
    String Name;
    NonSwipeableViewPager vpPager;
    FragmentPagerAdapter adapterViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);
        main = findViewById(android.R.id.content);
        vpPager = findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        try {
            Field mScroller;
            mScroller = NonSwipeableViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(vpPager.getContext());
            mScroller.set(vpPager, scroller);
        } catch (NoSuchFieldException e) { }
          catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        vpPager.setCurrentItem(0);
        alert = new AlertDialog.Builder(MainActivity.this)
                .setCancelable(false)
                .setView(R.layout.progress)
                .create();
    }

    @Override
    public void onBackPressed() {
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        if (task.isSuccessful()) {
            if (!sign) {
                user = task.getResult().getUser();
                addUserNameToUser();
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

    private void addUserNameToUser() {
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
                            .setValue(new StoredUser(Name, user.getUid()," "," "));
                    alert.dismiss();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            vpPager.setCurrentItem(1);
                        }
                    },500);
                }
            }
        });
    }

    public void SignIn() {
        mAuth.signInWithEmailAndPassword(Email, Pass).addOnCompleteListener(this);
    }

    public void SignUp() {
        mAuth.createUserWithEmailAndPassword(Email, Pass).addOnCompleteListener(this);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (!(netInfo != null && netInfo.isConnectedOrConnecting())) {
            Snackbar.make(main, "Please check your internet connection", Snackbar.LENGTH_SHORT).show();
        }
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    private void uploadBitmap() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (image != null) {
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        }
        byte[] data = baos.toByteArray();
        final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("images").child(user.getUid());
        UploadTask uploadTask = filePath.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        user.updateProfile(profileUpdates);
                        FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Users")
                                .child(user.getUid())
                                .child("photoUri")
                                .setValue(uri.toString());
                        alert.dismiss();
                       new Handler().postDelayed(new Runnable() {
                           @Override
                           public void run() {
                               vpPager.setCurrentItem(2);
                           }
                       },500);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                    }
                });
            }
        });

    }

    @Override
    public void onDataPass(String name, String email, String pass) {
         Name = name;
         Email = email;
         Pass = pass;
        if (isOnline()) {
            if (Name == null) {
                alert.show();
                sign = true;
                SignIn();
            } else {
                alert.show();
                sign = false;
                FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Boolean present = false;
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (ds.child("userName").getValue().equals(Name)) {
                                Toast.makeText(getApplicationContext(),"User name already exists",Toast.LENGTH_SHORT).show();
                                alert.dismiss();
                                present = true;
                                break;
                            }
                        }
                        if (!present) {
                            SignUp();

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    @Override
    public void onImageData(Bitmap b) {
        image = b;
        alert.show();
        if(image!= null){
            uploadBitmap();
        }
    }

    @Override
    public void onPoxChanged(String pox) {
        alert.show();
        FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(user.getUid())
                .child("pox")
                .setValue(pox)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                alert.dismiss();
                Toast.makeText(getApplicationContext(), "Signed Up", Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this, Principal.class));
                finish();
            }
        });

    }

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return new Edits();
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return new Image();
                case 2:
                    return new Pox();
                default:
                    return null;
            }
        }
    }

    public class FixedSpeedScroller extends Scroller {

        private int mDuration = 750;

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator, boolean flywheel) {
            super(context, interpolator, flywheel);
        }


        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }

}
