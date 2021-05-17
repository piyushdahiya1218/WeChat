package com.example.thisisachatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.thisisachatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.example.thisisachatapp.databinding.ActivityOTPBinding;
import com.mukesh.OnOtpCompletionListener;


import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    ActivityOTPBinding binding;
    FirebaseAuth auth;

    String verificationId;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOTPBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /*"sending otp" dialog properties*/
        dialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
        dialog.setMessage("Sending OTP...");
        dialog.setCancelable(false);
        dialog.show();

        auth = FirebaseAuth.getInstance();

        getSupportActionBar().hide();               /*hide action bar on the top of app*/



        String phoneNumber = getIntent().getStringExtra("phoneNumber");         /*get phone number as string from phonenumberactivity*/

        binding.phoneLbl.setText("Verify   " + phoneNumber);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)                /*create options for phone authorisation*/
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)                      /*OTP timeout set to 60 sec*/
                .setActivity(OTPActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {                 /*different methods for different situations*/
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                    @Override
                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verifyId, forceResendingToken);
                        dialog.dismiss();               /*when otp is sent, the dialog will dismiss*/
                        verificationId = verifyId;          /*otp provided by firebase is saved in string 'verificationID'*/

                        InputMethodManager imm = (InputMethodManager)   getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        binding.otpView.requestFocus();
                    }
                }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);               /*code will be sent on 'phonenumber' with above settings, declared as "options"  */

        /*verify button onclicklistener*/
        binding.verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = binding.otpView.getText().toString();             /*Extract entered otp to String 'otp'*/
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);          /*verificationID=otp provided by firebase, otp=otp entered by user----probably checking if they are same*/
                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {      /*apply this 'credential' on 'auth'*/
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())                                 /*if otp matches*/
                        {
                            Toast.makeText(OTPActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(OTPActivity.this, SetupProfileActivity.class);
                            startActivity(intent);
                            finishAffinity();           /*closes all the previous activities*/
                        }
                        else
                        {
                            Toast.makeText(OTPActivity.this, "Failed to log in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


/*
        binding.otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Intent intent = new Intent(OTPActivity.this, SetupProfileActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        } else {
                            Toast.makeText(OTPActivity.this, "Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
*/


    }
}