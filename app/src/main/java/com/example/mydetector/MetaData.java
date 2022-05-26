package com.example.mydetector;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydetector.adapters.MyAdapter;
import com.example.mydetector.beans.imagemap;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;

public class MetaData extends AppCompatActivity {

    private FloatingActionButton floatingActionButton_metadonnee;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meta_data);
        getSupportActionBar().hide();

        //Firebase data to recycleView
        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirebaseRecyclerOptions<imagemap> options =
                new FirebaseRecyclerOptions.Builder<imagemap>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("images"),
                                imagemap.class)
                        .build();
        myAdapter = new MyAdapter(options);
        recyclerView.setAdapter(myAdapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        myAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        myAdapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, home.class);
        startActivity(intent);
    }
}