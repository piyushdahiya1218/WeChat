package com.example.thisisachatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.thisisachatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.example.thisisachatapp.Models.User;
import com.example.thisisachatapp.databinding.ActivitySetupProfileBinding;

import java.util.Date;
import java.util.HashMap;

public class SetupProfileActivity extends AppCompatActivity {

    ActivitySetupProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
        dialog.setMessage("Updating profile...");
        dialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        getSupportActionBar().hide();                   /*hide action bar on the top of app*/

        /*clicking on profile image */
        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);            /*purpose of intent is to get content*/
                intent.setType("image/*");                              /*of image type*/
                startActivityForResult(intent, 45);
            }
        });

        /*continue button onclicklistener*/
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.nameBox.getText().toString();             /*extract entered name to 'nameinput*/

                if(name.isEmpty())               /*if no name is entered show error*/
                {
                    binding.nameBox.setError("Please type a name");
                    return;
                }

                dialog.show();               /*show loading dialog*/
                if(selectedImage != null)       /*if image is detected*/
                {
                    StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());             /*new folder is made in firebase storage named 'Profiles' with file named as current user id*/
                            /*put 'selectedimg' file in the storage reference*/
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {              /*download profile link*/
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl = uri.toString();                   /*'uri' is the link, stored in string now*/
                                        Log.i("USER_PROFILE_IMAGE_URL","        "+imageUrl);
                                        String uid = auth.getUid();                     /*current user id is saved in 'uid'*/
                                        String phone = auth.getCurrentUser().getPhoneNumber();       /*phone number is saved in string*/
                                        String name = binding.nameBox.getText().toString();          /*name is saved in string*/

                                        User user = new User(uid, name, phone, imageUrl);           /*new user is created with above fields*/

                                        database.getReference()
                                                .child("users")
                                                .child(uid)
                                                .setValue(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {             /* if user is added successfully then run intent*/
                                                        dialog.dismiss();                           /*dismiss the dialog before going to mainactivity*/
                                                        Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    });
                }
                else          /*if no image is selected*/
                {
                    String uid = auth.getUid();
                    String phone = auth.getCurrentUser().getPhoneNumber();

                    User user = new User(uid, name, phone, "No Image");         /* "No Image" string instead of image uri */

                    database.getReference()
                            .child("users")
                            .child(uid)
                            .setValue(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {              /* if user is added successfully then run intent*/
                                    dialog.dismiss();                /*dismiss the dialog before going to mainactivity*/
                                    Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                }

            }
        });
    }


    /*after the image selector dialog box is opened*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null)          /*if a picture is selected*/
        {
            if(data.getData() != null) {
                Uri uri = data.getData(); // filepath
                FirebaseStorage storage = FirebaseStorage.getInstance();
                long time = new Date().getTime();
                StorageReference reference = storage.getReference().child("Profiles").child(time+"");
                reference.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String filePath = uri.toString();
                                    HashMap<String, Object> obj = new HashMap<>();
                                    obj.put("image", filePath);
                                    database.getReference().child("users")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .updateChildren(obj).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    });
                                }
                            });
                        }
                    }
                });


                binding.imageView.setImageURI(data.getData());              /*set that picture as profile pic*/
                selectedImage = data.getData();                         /*selected img is set to 'selectimg'*/
            }
        }
    }
}