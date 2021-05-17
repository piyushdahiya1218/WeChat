package com.example.thisisachatapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thisisachatapp.databinding.ImageReceiveBinding;
import com.example.thisisachatapp.databinding.ImageSentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.example.thisisachatapp.Models.Message;
import com.example.thisisachatapp.R;
import com.example.thisisachatapp.databinding.DeleteDialogBinding;
import com.example.thisisachatapp.databinding.ItemReceiveBinding;
import com.example.thisisachatapp.databinding.ItemSentBinding;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Message> messages;

    /*to differentiate bw item sent and received*/
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;
    final int IMG_SENT = 3;
    final int IMG_RECEIVE =4;

    String senderRoom;
    String receiverRoom;

    public MessagesAdapter(Context context, ArrayList<Message> messages, String senderRoom, String receiverRoom) {
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT)                    /*if viewtype is item_sent then set view accordingly*/
        {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            return new SentViewHolder(view);
        }
        else if (viewType == IMG_SENT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.image_sent,parent,false);
            return new SentImageViewHolder(view);
        }
        else if (viewType == IMG_RECEIVE)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.image_receive,parent,false);
            return new ReceiverImageViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId()))           /*if user id of current user logged in matches with the id of message sent then it means the message was sent, not received*/
        {
            if(message.getUrilink() != null)
            {
                return IMG_SENT;
            }
            else
            {
                return ITEM_SENT;
            }
        }
        else
        {
            if(message.getUrilink() != null)
            {
                return  IMG_RECEIVE;
            }
            else
            {
                return ITEM_RECEIVE;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if(holder.getClass() == SentViewHolder.class) {                /*if class of holder and sentviewholder matches then bind the holder to sent message textbox*/
            SentViewHolder viewHolder = (SentViewHolder)holder;
            viewHolder.binding.message.setText(message.getMessage());
            if(!message.getMessage().equals("**This message expired**"));
            {
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                        DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                        AlertDialog dialog = new AlertDialog.Builder(context)
/*
                            .setTitle("Delete Message")
*/
                                .setView(binding.getRoot())
                                .create();

                        binding.everyone.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                message.setMessage("This message is removed.");
                                FirebaseDatabase.getInstance().getReference()
                                        .child("chats")
                                        .child(senderRoom)
                                        .child("messages")
                                        .child(message.getMessageId()).setValue(message);

                                FirebaseDatabase.getInstance().getReference()
                                        .child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .child(message.getMessageId()).setValue(message);
                                dialog.dismiss();
                            }
                        });

                        binding.delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FirebaseDatabase.getInstance().getReference()
                                        .child("chats")
                                        .child(senderRoom)
                                        .child("messages")
                                        .child(message.getMessageId()).setValue(null);
                                dialog.dismiss();
                            }
                        });

                        binding.cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        dialog.show();

                        return false;
                    }
                });
            }

        }
        else if (holder.getClass() == SentImageViewHolder.class)
        {
            SentImageViewHolder viewHolder = (SentImageViewHolder)holder;
            Glide.with(context).load(message.getUrilink()).into(viewHolder.binding.imagemessage);
        }
        else if(holder.getClass() == ReceiverImageViewHolder.class)
        {
            ReceiverImageViewHolder viewHolder = (ReceiverImageViewHolder)holder;
            Glide.with(context).load(message.getUrilink()).into(viewHolder.binding.imagemessage);
        }
        else
        {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;
            viewHolder.binding.message.setText(message.getMessage());

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                    DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(context)
/*
                            .setTitle("Delete Message")
*/
                            .setView(binding.getRoot())
                            .create();

                    binding.everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            message.setMessage("This message is removed.");
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(message);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(message);
                            dialog.dismiss();
                        }
                    });

                    binding.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(null);
                            dialog.dismiss();
                        }
                    });

                    binding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    return false;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    /*two view holders for sender and receiver*/

    public class SentViewHolder extends RecyclerView.ViewHolder {

        ItemSentBinding binding;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);
        }
    }
    public class SentImageViewHolder extends RecyclerView.ViewHolder {

        ImageSentBinding binding;
        public SentImageViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ImageSentBinding.bind(itemView);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        ItemReceiveBinding binding;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }
    public class ReceiverImageViewHolder extends RecyclerView.ViewHolder {

        ImageReceiveBinding binding;
        public ReceiverImageViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ImageReceiveBinding.bind(itemView);
        }
    }
}
