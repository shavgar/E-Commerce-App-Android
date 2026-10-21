package com.example.android.mymap.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.widget.Toolbar;

import com.example.android.mymap.R;
import com.example.android.mymap.models.ChatListAdapter;
import com.example.android.mymap.models.ChatListContent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatList extends AppCompatActivity {
    private static final String TAG = "ChatList";

    private ArrayList<ChatListContent> mChatListArryalist;
    private ArrayList<String> messageRecievers = new ArrayList<>();
    private RecyclerView mChatListRecyclerView;
    private String currentUserid;
    private ChatListAdapter mChatListAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private Toolbar mToolbar;

    private ChatListContent mChatListContent;

    private DatabaseReference mMessagesDatabaseReference, mUserDatabaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        mChatListRecyclerView = findViewById(R.id.activity_chat_list_recyclerview);
        mToolbar = findViewById(R.id.activity_chat_list_toolbar);
        mMessagesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Messages");
        mUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        currentUserid = mAuth.getCurrentUser().getUid();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chats");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);
        mChatListRecyclerView.setHasFixedSize(true);
        mChatListRecyclerView.setLayoutManager(mLinearLayoutManager);

        displayChatList();
    }

    private void displayChatList() {
        Log.d(TAG, "displayChatList: called");
        String postKey;
        mChatListArryalist = new ArrayList<>();
        mMessagesDatabaseReference.child(currentUserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot messageRecieversIDs : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: messagerecievers: " + messageRecieversIDs.getKey());
                        messageRecievers.add(messageRecieversIDs.getKey());
                        final String messageRecieverID = messageRecieversIDs.getKey();
                        mChatListArryalist.clear();
                        mUserDatabaseReference.child(messageRecieverID).addValueEventListener(new ValueEventListener() {
                            String userid, username, profileImageUrl;
                            ChatListContent chatListContent;

                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                userid = dataSnapshot.getKey();
                                username = dataSnapshot.child("firstName").getValue().toString() + " " + dataSnapshot.child("lastName").getValue().toString();
                                profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                                chatListContent = new ChatListContent(userid, username, profileImageUrl, "hii");

                                mChatListArryalist.add(chatListContent);
                                mChatListAdapter = new ChatListAdapter(ChatList.this, mChatListArryalist);
                                mChatListRecyclerView.setAdapter(mChatListAdapter);
                                mChatListAdapter.notifyDataSetChanged();
                                Log.d(TAG, "onDataChange: adapatersize " + mChatListAdapter.getItemCount());

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }
                    /*chatListAdapter = new ChatListAdapter(ChatList.this, mChatListArryalist);*/
                    Log.d(TAG, "onDataChange: chatListArrayListSize:  " + mChatListArryalist.size());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        /*for (int i = 0; i < messageRecievers.size(); i++) {

            mUserDatabaseReference.child(messageRecievers.get(i)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String firstName = dataSnapshot.child("firstName").getValue().toString();
                        String lastName = dataSnapshot.child("lastName").getValue().toString();
                        String username = firstName + lastName;
                        String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                        mChatListContent = new ChatListContent(profileImageUrl, username, "hello");
                        mChatListArryalist.add(mChatListContent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        mChatListAdapter = new ChatListAdapter(ChatList.this, mChatListArryalist);*/
    }
}
