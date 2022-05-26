package com.example.mydetector;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.StorageReference;

public class home extends AppCompatActivity implements View.OnClickListener{

    Button dataButton, annotateButton;
    String currentPhotoPath;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();
        dataButton = findViewById(R.id.dataButton);
        annotateButton = findViewById(R.id.annotateButton);

        dataButton.setOnClickListener((View.OnClickListener) this);

        annotateButton.setOnClickListener((View.OnClickListener) this);
    }

    @Override
    public void onClick(View v) {
        if(v== dataButton) {
            Intent it=new Intent(this,MetaData.class);
            startActivity(it);
            finish();
        }

        if(v== annotateButton) {
            Intent it=new Intent(this, image.class);
            startActivity(it);
            finish();
        }
    }
    @Override
    public void onBackPressed() {
    }
}