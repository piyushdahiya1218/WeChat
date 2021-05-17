package com.example.thisisachatapp.Adapters;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thisisachatapp.Activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.thisisachatapp.Activities.ChatActivity;
import com.example.thisisachatapp.R;
import com.example.thisisachatapp.Models.User;
import com.example.thisisachatapp.databinding.RowConversationBinding;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    Context context;
    ArrayList<User> users;
    String selectedusername = null;
    String lastMsg;
    int temp=0;

    /*constructor*/
    public UsersAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }


    private void NewNotification(Context context, String title, String message, Intent intent, int reqCode)
    {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_ONE_SHOT);
        String CHANNEL_ID = "channel_name";// The id of the channel.
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_fb_love)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(reqCode, notificationBuilder.build()); // 0 is the request code, it should be unique id

        Log.d("showNotification", "showNotification: " + reqCode);
    }


    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);

        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        /*Class    variable      arraylist*/
        User         user   =    users.get(position);                       /* "users" is set to "user" according to the position */

        String senderId = FirebaseAuth.getInstance().getUid();

        String senderRoom = senderId + user.getUid();

        int reqcode = 1;
        Intent intent = new Intent(context, MainActivity.class);
        holder.binding.readimage.setVisibility(View.INVISIBLE);
        FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            lastMsg = snapshot.child("lastMsg").getValue(String.class);
                            long time = snapshot.child("lastMsgTime").getValue(Long.class);
                            String date = snapshot.child("lastMsgDate").getValue(String.class);
                            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                            holder.binding.msgTime.setText(timeFormat.format(new Date(time)));
                            holder.binding.lastMsg.setText(lastMsg);
                            holder.binding.msgdate.setText(date);

                            if(temp>getItemCount() && selectedusername!=user.getName())
                            {
                                holder.binding.readimage.setVisibility(View.VISIBLE);
                                if(lastMsg.equals("image"))
                                {
                                    NewNotification(context, user.getName(), "sent an image", intent, reqcode);
                                }
                                else
                                {
                                    NewNotification(context, user.getName(), lastMsg, intent, reqcode);
                                }
                                selectedusername = null;
                            }
                            temp++;
                        }
                        else
                        {
                            holder.binding.lastMsg.setText("Tap to chat");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.binding.username.setText(user.getName());                    /*get name from 'user' and set to name textbox*/

        Glide.with(context).load(user.getProfileImage())
                .placeholder(R.drawable.avatar)                         /*default profile pic if no image was selected*/
                .into(holder.binding.profile);                           /*apply uploaded image*/

        holder.itemView.setOnClickListener(new View.OnClickListener() {                     /*click a contact to go to chatsactivity*/
            @Override
            public void onClick(View v) {
                holder.binding.readimage.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("name", user.getName());                     /*pass name and userid of contact to chatsactivity*/
                intent.putExtra("uid", user.getUid());
                selectedusername = user.getName();
                context.startActivity(intent);
            }
        });
    }



    @Override
    public int getItemCount() {
        return users.size();                    /*return size of array (no. of contacts)*/
    }


    public class UsersViewHolder extends RecyclerView.ViewHolder {

        RowConversationBinding binding;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }

}
