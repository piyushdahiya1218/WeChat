package com.example.thisisachatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
/*
import com.example.thisisachatapp.Adapters.TopStatusAdapter;
import com.example.thisisachatapp.Models.Status;
*/
/*
import com.example.thisisachatapp.Models.UserStatus;
*/
import com.example.thisisachatapp.R;
import com.example.thisisachatapp.Models.User;
import com.example.thisisachatapp.Adapters.UsersAdapter;
import com.example.thisisachatapp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    ArrayList<User> users;
    UsersAdapter usersAdapter;
    ProgressDialog dialog;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();

        database.getReference().child("users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        usersAdapter = new UsersAdapter(this, users);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);

        binding.recyclerView.setAdapter(usersAdapter);          /*set adapter on recycler view*/

        binding.recyclerView.showShimmerAdapter();

        database.getReference().child("users").addValueEventListener(new ValueEventListener() {           /*adding users from firebase database to arraylist*/
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();              /*clear arraylist*/
                for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                    User user = snapshot1.getValue(User.class);
                    if(!user.getUid().equals(FirebaseAuth.getInstance().getUid()))
                    {
                        users.add(user);
                    }
                }
                binding.recyclerView.hideShimmerAdapter();
                usersAdapter.notifyDataSetChanged();                /*notify adapter that dataset has changed*/

/*
                NewNotification(MainActivity.this,"Title" , "RECEIVED  ", intent, reqcode);
*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    /*when an item of topmenu is clicked -> perform certain task*/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                Toast.makeText(this, "Search clicked.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Toast.makeText(this, "Settings Clicked.", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /* 'topmenu' is attached to mainactivity*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}