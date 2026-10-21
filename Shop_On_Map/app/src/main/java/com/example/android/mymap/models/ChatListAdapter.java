package com.example.android.mymap.models;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mymap.R;
import com.example.android.mymap.ui.ChatActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    private CircleImageView chatListProfileImage;
    private TextView chatListUsername, chatListMessage;
    private RelativeLayout chatListItemParentLayout;

    private ArrayList<ChatListContent> chatListArrayList;
    private Context context;

    public ChatListAdapter(Context context, ArrayList<ChatListContent> chatListArrayList) {
        this.chatListArrayList = chatListArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item_layout, null, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Picasso.get().load(chatListArrayList.get(position).getProfileImageUrl()).fit().into(chatListProfileImage);
        chatListUsername.setText(chatListArrayList.get(position).getUsername());
        chatListMessage.setText(chatListArrayList.get(position).getUserid());
        chatListItemParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, chatListArrayList.get(position).getUsername() + " clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(v.getContext(), ChatActivity.class);
                intent.putExtra("userid", chatListArrayList.get(position).getUserid());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                v.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return chatListArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            chatListProfileImage = itemView.findViewById(R.id.chat_list_item_profile_image);
            chatListUsername = itemView.findViewById(R.id.chat_list_item_username);
            chatListMessage = itemView.findViewById(R.id.chat_list_item_message);
            chatListItemParentLayout = itemView.findViewById(R.id.chat_list_item_parent_relativelayout);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
