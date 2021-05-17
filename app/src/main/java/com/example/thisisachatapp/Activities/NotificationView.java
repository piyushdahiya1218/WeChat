package com.example.thisisachatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.thisisachatapp.R;
import com.example.thisisachatapp.databinding.ActivityNotificationViewBinding;

public class NotificationView extends AppCompatActivity {

    TextView textView;
    ActivityNotificationViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNotificationViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        textView = binding.textView;
        //getting the notification message
        String message = getIntent().getStringExtra("message");
        textView.setText(message);
    }
}