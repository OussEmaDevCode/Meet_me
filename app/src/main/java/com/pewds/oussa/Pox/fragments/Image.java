package com.pewds.oussa.Pox.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.pewds.oussa.Pox.R;

import java.io.IOException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

public class Image extends Fragment {
    private static final int PICK_IMAGE = 100;
    private static final int TAKE_PHOTO = 0;
    CircleImageView  circleImageView;
    Button ok,no;
    View parent;
    ImageData imageData;
    AlertDialog choose;
    Bitmap image;
    public Image() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        circleImageView = view.findViewById(R.id.circular_image);
        parent = view.findViewById(R.id.parent);
        ok = view.findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageData.onImageData(image);
            }
        });
        no = view.findViewById(R.id.no);
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowImageChoose();
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowImageChoose();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        imageData = (ImageData)context;
    }

    public interface ImageData{
        void onImageData(Bitmap b);
    }
    private void ShowImageChoose() {
        //setting up the dialog box...
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setIcon(android.R.drawable.ic_menu_camera);
        alertDialogBuilder.setCancelable(false);
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
            in = getActivity().getApplicationContext().getContentResolver().openInputStream(uri);
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
            in = getActivity().getApplicationContext().getContentResolver().openInputStream(uri);
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
