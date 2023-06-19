package com.example.compressimage;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;

import android.database.Cursor;
import 	android.text.format.Formatter;
import android.Manifest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import id.zelory.compressor.Compressor;


public class MainActivity extends AppCompatActivity {

    public static final int RESULT_IMAGE=1;

    ImageView imgOriginal,imgCompressed;
    TextView txtOriginal,txtCompressed,txtQuality;
    EditText txtHeight,txtWidth;
    SeekBar  seekBar;
    Button btnPick,btnCompress;
    File originalImage,compressedImage;
    private static String filepath;
    File path=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/myCompressor");

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askPermission();
        imgOriginal=findViewById(R.id.imgOriginal);
        imgCompressed=findViewById(R.id.imgCompressed);
        txtOriginal=findViewById(R.id.txtOriginal);
        txtCompressed=findViewById(R.id.txtCompressed);
        txtQuality=findViewById(R.id.txtQuality);
        txtWidth=findViewById(R.id.txtWidth);
        txtHeight=findViewById(R.id.txtHeight);
        seekBar=findViewById(R.id.seekQuality);
        btnPick=findViewById(R.id.btnPick);
        btnCompress=findViewById(R.id.btnCompress);

        filepath=path.getAbsolutePath();
        if(!path.exists()) {
            path.mkdirs();
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
           @SuppressLint("SetTextI18n")
           @Override
           public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
               txtQuality.setText("Quality:"+i);
               seekBar.setMax(100);

           }

           @Override
           public void onStartTrackingTouch(SeekBar seekBar) {

           }

           @Override
           public void onStopTrackingTouch(SeekBar seekBar) {

           }
       });
        btnPick.setOnClickListener(view -> openGallery()
        );
        btnCompress.setOnClickListener(this::onClick);
    }

    public void openGallery() {
        Intent gallery= new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery,RESULT_IMAGE);
    }
    private String getPathFromUri(Uri uri) {
        String path = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            if (cursor.moveToFirst()) {
                path = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return path;
    }
    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            btnCompress.setVisibility(View.VISIBLE);
            final Uri imageUri = Objects.requireNonNull(data).getData();
            if (imageUri != null) {
                Log.d("Image URI", imageUri.toString());
                Log.d("Image URI Scheme", imageUri.getScheme());
                try {
                    // Rest of the code to process the image
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    if (imageStream != null) {
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        imgOriginal.setImageBitmap(selectedImage);
                        originalImage = new File(getPathFromUri(imageUri));
                        if (originalImage.exists()) {
                            String fileSize = Formatter.formatShortFileSize(MainActivity.this, originalImage.length());
                            txtOriginal.setText("Size: " + fileSize);
                        } else {
                            Toast.makeText(this, "Failed to load original image file", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to open image stream", Toast.LENGTH_SHORT).show();
                    }
                    // ...
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Something went wrong while loading the image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to get image URI", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }


    private void askPermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                 permissionToken.continuePermissionRequest();
            }
        }).check();
}

    @SuppressLint("SetTextI18n")
    private void onClick(View view) {
        int quality = seekBar.getProgress();
        //Toast.makeText(this, "Not Working", Toast.LENGTH_SHORT).show();
        int width = Integer.parseInt(txtWidth.getText().toString());
        int height = Integer.parseInt(txtHeight.getText().toString());
        try {
            Toast.makeText(MainActivity.this, "Upper Try block", Toast.LENGTH_SHORT).show();
            compressedImage = new Compressor(MainActivity.this)
                    .setMaxWidth(width)
                    .setMaxHeight(height)
                    .setQuality(quality)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setDestinationDirectoryPath(filepath)
                    .compressToFile(originalImage);

            Toast.makeText(MainActivity.this, "Try block", Toast.LENGTH_SHORT).show();
            File finaleFile = new File(filepath, originalImage.getName());
            Bitmap finalBitmap = BitmapFactory.decodeFile(finaleFile.getAbsolutePath());
            imgCompressed.setImageBitmap(finalBitmap);
            txtCompressed.setText("Size:" + Formatter.formatFileSize(MainActivity.this, finaleFile.length()));
            Toast.makeText(MainActivity.this, "Image Compressed and Saved! ", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error While Compressing", Toast.LENGTH_SHORT).show();
        }
    }
}