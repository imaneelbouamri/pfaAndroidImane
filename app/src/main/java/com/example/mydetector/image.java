package com.example.mydetector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.mydetector.BitmapExt.BitmapHelper;
import com.example.mydetector.beans.Coordonnées;
import com.example.mydetector.beans.listItem;
import com.example.mydetector.beans.Shape;
import com.example.mydetector.beans.localisation;
import com.example.mydetector.beans.hotspots;
import com.example.mydetector.beans.imagemap;
import com.example.mydetector.cadre.PopUp;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class image extends AppCompatActivity implements View.OnClickListener, LocationListener {
    private FloatingActionButton floatingActionButton, addDataButton;
    private Button button,addDataButto;
    private ImageView image;
    private Bitmap bitmap = null;
    private LocationManager locationManager;
    private Double latitude, longitude;
    private Date capturedDate;
    private Uri uri;
    private double width, height;
    private EditText descriptionEditText;
    private String description;
    private boolean imageCharged = false;
    private List<Coordonnées> listPoint;
    private List<hotspots> hotspotsList;
    private Shape shape;
   /* private Float maxxa = 0f;
    private Float maxya =0f;
    private Float minxa =0f;
    private Float minya =0f;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        getSupportActionBar().hide();
        floatingActionButton = findViewById(R.id.floatingActionButton2);
        image = findViewById(R.id.imageView3);
        addDataButto = findViewById(R.id.buttonAdd);
        button = findViewById(R.id.button_Picture);
        descriptionEditText = findViewById(R.id.button_metadonnees);
        listPoint = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(com.example.mydetector.image.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(com.example.mydetector.image.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }
        getLocation();
        floatingActionButton.setOnClickListener(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(com.example.mydetector.image.this)
                        .crop()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });

        addDataButto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Location", "onLocationChanged: " + latitude + "/" + longitude);
                Log.d("Date", "capturedDate: " + capturedDate);
                description = descriptionEditText.getText().toString();
                Log.d("Description", description);
                if (description.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Veuillez rajouter une description",
                            Toast.LENGTH_LONG).show();
                } else if (!imageCharged) {
                    Toast.makeText(getApplicationContext(), "Veuillez importer ou capturer une " +
                                    "image",
                            Toast.LENGTH_LONG).show();
                } else {
                    uploadToFirebase();
                    Intent i = new Intent(getApplicationContext(), com.example.mydetector.MetaData.class);
                    startActivity(i);
                    finish();
                }

            }
        });
    }


    private void uploadToFirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final String imageName = "Image" + UUID.randomUUID().toString();
        Log.d("imageName", imageName);
        final StorageReference uploader = storage.getReference("pictures/" + imageName);

        uploader.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                FirebaseDatabase db = FirebaseDatabase.getInstance();
                                DatabaseReference root = db.getReference("images");

                                //getListFromPopUp
                                hotspotsList = new ArrayList<>();
                                listPoint = listItem.getInstance().getCoords();
                                Log.d("listAdded ", listPoint.toString());

                                hotspotsList.add(new hotspots(description, new Shape(convertList(listPoint))));
                                Log.d("hotspotsList->", "--->  " + hotspotsList.toString());
                                localisation location;
                                if (longitude == null || latitude == null) {
                                    location =
                                            new localisation(0, 0);
                                } else {
                                    location =
                                            new localisation(longitude, latitude);
                                }


                                imagemap image = new imagemap(uri.toString(), capturedDate.toString(),
                                        location, width + "", height + "", hotspotsList);

                                root.child(imageName).setValue(image);
                                Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    }
                });

    }

    private List<Double> convertList(List<Coordonnées> listPoint) {
        List<Double> integers = new ArrayList<>();
        for (Coordonnées c : listPoint) {
            integers.add((double) c.x);
            integers.add((double) c.y);
        }
        Log.d("TAG", integers.toString());
        return integers;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            uri = data.getData();
            try {
                getCapturedDate();
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                imageCharged = true;
                //create instance of bitmap with BitmapHelper Class
                BitmapHelper.getInstance().setBitmap(bitmap);
                Intent itent = new Intent(this, PopUp.class); //Show popup to draw polygon
                startActivity(itent);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Glide
                    .with(getApplicationContext())
                    .asBitmap()
                    .load(uri)
                    .apply(RequestOptions.fitCenterTransform())
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap,
                                                    Transition<? super Bitmap> transition) {
                            width = bitmap.getWidth();
                            height = bitmap.getHeight();
                            image.setImageBitmap(getRoundedCornerBitmap(bitmap, 30));
                        }
                    });
        }

    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }


    private void getCapturedDate() {
        Date c = Calendar.getInstance().getTime();
        capturedDate = c;
    }


    //Back Button
    @Override
    public void onClick(View v) {
        if (v == floatingActionButton) {
            Intent it = new Intent(this, com.example.mydetector.MetaData.class);
            startActivity(it);
            finish();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, com.example.mydetector.image.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //LocationListener.super.onStatusChanged(provider, status, extras);
    }


    // get max x from Point list
    private Float getMaxX(List<Coordonnées> point) {
        Float max = point.get(0).x;
        for (int i = 1; i < point.size(); i++) {
            if (point.get(i).x > max) {
                max = point.get(i).x;
            }
        }
        return max;
    }

    // get max Y from Point list
    private Float getMaxY(List<Coordonnées> point) {
        Float max = point.get(0).y;
        for (int i = 1; i < point.size(); i++) {
            if (point.get(i).y > max) {
                max = point.get(i).y;
            }
        }
        return max;
    }

    // get min Y from Point list
    private Float getMinY(List<Coordonnées> point) {
        Float min = point.get(0).y;
        for (int i = 1; i < point.size(); i++) {
            if (point.get(i).y < min) {
                min = point.get(i).y;
            }
        }
        return min;
    }

    // get min X from Point list
    private Float getMinX(List<Coordonnées> point) {
        Float min = point.get(0).x;
        for (int i = 1; i < point.size(); i++) {
            if (point.get(i).x < min) {
                min = point.get(i).x;
            }
        }
        return min;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, home.class);
        startActivity(intent);
    }
}