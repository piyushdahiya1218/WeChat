package com.example.thisisachatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
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
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.thisisachatapp.Adapters.MessagesAdapter;
import com.example.thisisachatapp.Models.Message;
import com.example.thisisachatapp.R;
import com.example.thisisachatapp.databinding.ActivityChatBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom;

    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;

    Uri fileuri;

    String randomKey;
    String filePath;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        progress dialog till image uploads
        dialog = new ProgressDialog(this,R.style.AppCompatAlertDialogStyle);
        dialog.setMessage("Uploading image");
        dialog.setCancelable(false);

        messages = new ArrayList<>();

        /*extract name and uid of receiver from Useradapter*/
        String name = getIntent().getStringExtra("name");
        String receiverUid = getIntent().getStringExtra("uid");
        String senderUid = FirebaseAuth.getInstance().getUid();         /*get uid of sender*/

        /*make room for sender and receiver*/
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        adapter = new MessagesAdapter(this, messages, senderRoom, receiverRoom);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);                                        /*messages will start from bottom of screen*/

        binding.recyclerView.setLayoutManager(layoutManager);

        binding.recyclerView.setAdapter(adapter);

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();


        int reqcode = 1;
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);

        Log.i("SENDER ROOM",senderRoom );
        Log.i("RECEIVER ROOM",receiverRoom);

        /*for receiver*/
        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }
                        adapter.notifyDataSetChanged();
                        binding.recyclerView.smoothScrollToPosition(messages.size());           /*after receiving a message the screen will scroll to bottom*/
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });

        /*when camera button is clicked, intent to choose an image will be started*/
        binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,45);      /*onActivityResult is started*/
                dialog.show();                                  /*when image is selected*/

            }
        });



        /*when send button is clicked*/
        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String messageTxt = binding.messageBox.getText().toString();            /*message typed by user is extracted to string*/
                String temp = "";

                if(!messageTxt.equals(temp))
                {
                    Date date = new Date();                                                   /* current date and time*/
                    String datestring = new SimpleDateFormat("dd/MM", Locale.getDefault()).format(new Date());
                    Message message = new Message(messageTxt, senderUid, date.getTime(), datestring);       /*new message object is created*/
                    binding.messageBox.setText("");                                             /*clear the message box after the text is sent*/

                    String[] parts = message.getMessage().split(":");                   /*split the message string at every " : " */
                    String part1 = parts[0];
                    if(part1.equals("Del") || part1.equals("del"))                              /*check if the part before first " : " is equal to del or Del*/
                    {
                        String mainmssgstring = parts[1];                       /*if entered mssg  = a:b then save b to mainmssgstring*/
                        if (parts.length > 2)                                   /*if entered mssg  = a:b:c:d.... then save b:c:d... to mainmssgstring*/
                        {
                            for (int i = 2; i < parts.length; i++)
                            {
                                mainmssgstring = mainmssgstring + ":" + parts[i];
                            }
                        }
                        // send main mssg without "del:" part
                        message.setMessage(mainmssgstring);
                        HashMap<String, Object> lastMsgObj = new HashMap<>();
                        lastMsgObj.put("lastMsg", message.getMessage());                        /*last message    and    last message time    are created and updated in database*/
                        lastMsgObj.put("lastMsgTime", date.getTime());
                        lastMsgObj.put("lastMsgDate", datestring);
                        database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);
//                          random key is generated
                        randomKey = database.getReference().push().getKey();
                        Log.i("Activity","WHEN TEXT IS SENT     "+randomKey);

                        database.getReference().child("chats")              /*message is saved in database*/
                                .child(senderRoom)                          /*in senderroom*/
                                .child("messages")
                                .child(randomKey)
                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                database.getReference().child("chats")
                                        .child(receiverRoom)                /*in receiverroom*/
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });
                            }
                        });
                        // delete the mssg after specified time
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                message.setMessage("**This message expired**");             /*exchange the main mssg with custom string*/
                                lastMsgObj.put("lastMsg", message.getMessage());
                                Date date = new Date();
                                lastMsgObj.put("lastMsgTime", date.getTime());
                                lastMsgObj.put("lastMsgDate", datestring);
                                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);
                                database.getReference().child("chats")              /*message is saved in database*/
                                        .child(senderRoom)                          /*in senderroom*/
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        database.getReference().child("chats")
                                                .child(receiverRoom)                /*in receiverroom*/
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                            }
                                        });
                                    }
                                });

                            }
                        }, 3000);
                    }
                    else                // if the entered mssg does not contain "del:"
                    {
                        HashMap<String, Object> lastMsgObj = new HashMap<>();
                        lastMsgObj.put("lastMsg", message.getMessage());                        /*last message    and    last message time    are created and updated in database*/
                        lastMsgObj.put("lastMsgTime", date.getTime());
                        lastMsgObj.put("lastMsgDate", datestring);
                        database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);
//                          random key is generated
                        randomKey = database.getReference().push().getKey();
                        Log.i("Activity","WHEN TEXT IS SENT     "+randomKey);

                        database.getReference().child("chats")              /*message is saved in database*/
                                .child(senderRoom)                          /*in senderroom*/
                                .child("messages")
                                .child(randomKey)
                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                database.getReference().child("chats")
                                        .child(receiverRoom)                /*in receiverroom*/
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });
                            }
                        });
                    }

                    binding.recyclerView.smoothScrollToPosition(messages.size());
                }

                /*if a image is selected + there is no input text*/
                if(fileuri != null  &&  messageTxt.equals(temp)) {

                    binding.messageBox.setHint("Type a message...");

                    Date date = new Date();
                    Message message = new Message(senderUid, date.getTime(), filePath);         /*image url 'filepath' is passed to Message constructor*/
                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                    lastMsgObj.put("lastMsg", "image");
                    lastMsgObj.put("lastMsgTime", date.getTime());
                    database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                    database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                    Log.i("AHAHAHAHAHHAHA","      "+randomKey);

                    database.getReference().child("chats")                          /*message object is passed to database*/
                            .child(senderRoom)
                            .child("messages")
                            .child(randomKey)
                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            database.getReference().child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(randomKey)
                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                        }
                    });



                    binding.recyclerView.smoothScrollToPosition(messages.size());

                    /*both are set to null so that if send button is clicked without again selecting an image, nothing happens*/
                    fileuri=null;
                    filePath=null;

                }

            }
        });



        getSupportActionBar().setTitle(name);               /*set name of contact as title of action bar*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if an image is selected
        if(data!=null && data.getData()!=null)
        {
            Uri uri = data.getData();   /*selected images uri is extracted*/
            FirebaseStorage storage = FirebaseStorage.getInstance();
            long time = new Date().getTime();
            StorageReference reference = storage.getReference().child("ChatImages").child(time+"");     /*image is saved in storage under folder ChatImages with name 'time when they are uploaded'*/
            reference.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {          /*download url of uploaded image*/
                            @Override
                            public void onSuccess(Uri uri) {
                                filePath = uri.toString();                          /*url is extracted to string*/
                                HashMap<String, Object> obj = new HashMap<>();
                                obj.put("image", filePath);

                                randomKey=database.getReference().push().getKey();
                                Log.i("Activity","ON ACTIVITY METHOD    "+randomKey);
                                Log.i("Activity", "FILEPATH   "+filePath);

                                dialog.dismiss();

                            }
                        });
                    }
                }
            });

//            save the selected image to fileuri
            fileuri = data.getData();
            binding.messageBox.setHint("Click send to upload image");
            Log.i("ACTIVITY","    FILEURI.TOSTRING    "+data.getData().toString());
        }
        else            /*if no image is selected then dismiss the progress dialog and return to chat*/
        {
            dialog.dismiss();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(ChatActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        return super.onSupportNavigateUp();
    }
}