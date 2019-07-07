package com.pewds.oussa.Pox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pewds.oussa.Pox.fragments.Image;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class editsActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 100;
    private static final int TAKE_PHOTO = 0;
    CircleImageView circleImageView;
    Button ok,no;
    View parent;
    AlertDialog choose;
    Bitmap image;
    AlertDialog alert;
    FirebaseAuth mAuth;
    int i;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_edits);
        alert = new AlertDialog.Builder(editsActivity.this)
                .setCancelable(false)
                .setView(R.layout.progress)
                .create();
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(editsActivity.this,Settings.class));
                finish();
            }
        });
        circleImageView = findViewById(R.id.imageEdit);
        parent = findViewById(R.id.parent);
        ok = findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               alert.show();
               uploadBitmap(mAuth.getCurrentUser());
            }
        });
        no = findViewById(R.id.no);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowImageChoose();
            }
        });
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowImageChoose();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(i==0) {
            Toast.makeText(getApplicationContext(), "Tap on the photo to change it", Toast.LENGTH_LONG).show();
            i++;
        }
    }

    private void uploadBitmap(final FirebaseUser user) {
        final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("images").child(user.getUid());
        filePath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if (image != null) {
                    image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                }
                byte[] data = baos.toByteArray();
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
                                Toast.makeText(getApplicationContext(),"Profile photo changed",Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                            }
                        });
                    }
                });
            }
        });

    }
    private void ShowImageChoose() {
        //setting up the dialog box...
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(editsActivity.this);
        alertDialogBuilder.setIcon(android.R.drawable.ic_menu_camera);
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setTitle("How do you want to add the photo?");
        alertDialogBuilder.setPositiveButton("from gallery",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                        startActivityForResult(gallery, PICK_IMAGE);
                    }
                });
        alertDialogBuilder.setNegativeButton("take a photo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent takephto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takephto, TAKE_PHOTO);
            }
        });
        choose = alertDialogBuilder.create();
        //...showing it
        choose.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        choose.dismiss();
        Bitmap current = null;
        if (choose.isShowing()) {
            choose.dismiss();
        }
        if (choose.isShowing()) {
            choose.dismiss();
        }
        //choose form gallery
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            current = getBitmap(data.getData());
        }
        //take a photo
        else if (resultCode == RESULT_OK && requestCode == TAKE_PHOTO) {
            current = (Bitmap) data.getExtras().get("data");
        }
        if(current != null) {
            image = current;
            circleImageView.setImageBitmap(image);
            parent.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap getBitmap(Uri uri) {
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = getContentResolver().openInputStream(uri);
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d("w", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height : " + o.outHeight);

            Bitmap bitmap = null;
            in = getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                bitmap = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = bitmap.getHeight();
                int width = bitmap.getWidth();
                Log.d("w", "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) x,
                        (int) y, true);
                bitmap.recycle();
                bitmap = scaledBitmap;

                System.gc();
            } else {
                bitmap = BitmapFactory.decodeStream(in);
            }
            in.close();

            Log.d("w", "bitmap size - width: " + bitmap.getWidth() + ", height: " +
                    bitmap.getHeight());
            return bitmap;
        } catch (IOException e) {
            Log.e("w", e.getMessage(), e);
            return null;
        }
    }
}
