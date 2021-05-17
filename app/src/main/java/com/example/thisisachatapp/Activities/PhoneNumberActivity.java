package com.example.thisisachatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.example.thisisachatapp.databinding.ActivityPhoneNumberBinding;

public class PhoneNumberActivity extends AppCompatActivity {

    ActivityPhoneNumberBinding binding;
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() != null)               /*if a current user is present then skip phonenumberactivity and otpactivity and go straight to mainactivity*/
        {
            Intent intent = new Intent(PhoneNumberActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        getSupportActionBar().hide();           /*hide action bar on the top of app*/

        binding.phoneBox.requestFocus();

        /*otp button onclicklistener*/
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhoneNumberActivity.this, OTPActivity.class);
                intent.putExtra("phoneNumber", binding.phoneBox.getText().toString());                  /*transfer phone number as string to 'otpactivity'*/
                startActivity(intent);
            }
        });

    }
}