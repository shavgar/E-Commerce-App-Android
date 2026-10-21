package com.example.android.mymap.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.mymap.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Messages> messagesArrayList;
    private TextView senderMessageTextView, recieverMessageTextView;

    /*Firebase*/
    private FirebaseAuth mAuth;


    public MessagesAdapter(Context context, ArrayList<Messages> messages) {
        this.context = context;
        this.messagesArrayList = messages;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_text_message_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        String senderUserid = mAuth.getCurrentUser().getUid();
        Messages userMessage = messagesArrayList.get(position);
        String fromUserid = userMessage.getFrom();
        String messageType = userMessage.getType();

        if (messageType.equals("text")) {
            recieverMessageTextView.setVisibility(View.GONE);
            senderMessageTextView.setVisibility(View.GONE);
            if (fromUserid.equals(senderUserid)) {
                senderMessageTextView.setVisibility(View.VISIBLE);
                senderMessageTextView.setText(userMessage.getMessage());
            } else {
                recieverMessageTextView.setVisibility(View.VISIBLE);
                recieverMessageTextView.setText(userMessage.getMessage());
            }
        }

    }

    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageTextView = itemView.findViewById(R.id.custom_text_message_layout_sender_message);
            recieverMessageTextView = itemView.findViewById(R.id.custom_text_message_layout_reciever_message);

        }
    }

}
