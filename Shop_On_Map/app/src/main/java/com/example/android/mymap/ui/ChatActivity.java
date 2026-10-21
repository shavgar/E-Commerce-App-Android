package com.example.android.mymap.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mymap.R;
import com.example.android.mymap.models.Messages;
import com.example.android.mymap.models.MessagesAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    private RecyclerView mMessagesRecyclerView;
    private ImageButton mSendMessageButton, mSendPictureButton;
    private EditText mTextMessageField;
    private Toolbar mChatToolbar;

    private String messageRecieverid, messageRecieverFirstName, messageRecieverLastName,
            messageRecieverFullName, messageRecieverImageUrl, messageSenderid, currentDate, currentTime;
    private Calendar calendar;

    private ArrayList<Messages> messagesArrayList = new ArrayList<>();
    private MessagesAdapter mMessagesAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    /*custom_chat_toolbar_layout variables*/
    private TextView mChatToolbarUsername;
    private CircleImageView mChatToolbarProfileImage;

    /*Firebase*/
    private DatabaseReference mUserDatabaseReference;
    private DatabaseReference mRootDatabaseReference;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        /*instaniate the views*/
        mMessagesRecyclerView = findViewById(R.id.activity_chat_messages_recyclerview);
        mSendMessageButton = findViewById(R.id.actvity_chat_send_message_button);
        mSendPictureButton = findViewById(R.id.actvity_chat_send_picture_button);
        mTextMessageField = findViewById(R.id.activity_chat_send_message_field);
        mChatToolbar = findViewById(R.id.activity_chat_toolbar);

        /*show the toolbar*/
        setSupportActionBar(mChatToolbar);
        ActionBar chatActionBar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        View view = getLayoutInflater().inflate(R.layout.custom_chat_toolbar_layout, null);
        chatActionBar.setCustomView(view);
        /*these two views must be declared after inflating its layout above
         * otherwise the view will cause null pointer exeception because
         * you are tryint to access a view prior to inflating layout*/
        mChatToolbarUsername = findViewById(R.id.custom_chat_toolbar_username);
        mChatToolbarProfileImage = findViewById(R.id.custom_chat_toolbar_profile_image);

        /*get the user id from the intent sent from PostsAdapter or from chatListAdapter*/
        messageRecieverid = getIntent().getExtras().get("userid").toString();
        Log.d(TAG, "onCreate: try recieveruserid: " + messageRecieverid);

        /*Initiliaze firebase variables*/
        mUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(messageRecieverid);
        mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        messageSenderid = mAuth.getCurrentUser().getUid();

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessagesRecyclerView.setHasFixedSize(true);
        mMessagesRecyclerView.setLayoutManager(mLinearLayoutManager);

        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToUser();
            }
        });


        /*display the message reciever name and profile image*/
        displayUserNameAndImage();
        /*show all the messages*/
        fetchUsersMessages();

    }

    /*show all the messages*/
    private void fetchUsersMessages() {
        mRootDatabaseReference.child("Messages").child(messageSenderid).child(messageRecieverid)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()) {
                            Messages message = dataSnapshot.getValue(Messages.class);
                            messagesArrayList.add(message);
                            /*auto scroll the messages to show the last message*/
                            /*set up adapter layout manager for recyclerview*/
                            mMessagesAdapter = new MessagesAdapter(ChatActivity.this, messagesArrayList);
                            mMessagesRecyclerView.setAdapter(mMessagesAdapter);
                            mMessagesAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    /*send message to start chatting*/
    private void sendMessageToUser() {
        String message = mTextMessageField.getText().toString();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Type your message", Toast.LENGTH_LONG).show();
        } else {
            /*reference to the seder's copy of the message */
            String messageSenderReference = "Messages/" + messageSenderid + "/" + messageRecieverid;
            String messageRecieverReference = "Messages/" + messageRecieverid + "/" + messageSenderid;
            /*generate random unique key for each message using push()*/
            DatabaseReference userMessageReference = FirebaseDatabase.getInstance().getReference().child("Messages")
                    .child(messageSenderid).child(messageRecieverid).push();
            String messageUniqueKey = userMessageReference.getKey();

            /*get the current date*/
            calendar = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            currentDate = currentDateFormat.format(calendar.getTime());

            /*get the current time either AM or PM*/
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm aa");
            currentTime = currentTimeFormat.format(calendar.getTime());

            /*sender's message body*/
            Map senderMessageBody = new HashMap();
            senderMessageBody.put("message", message);
            senderMessageBody.put("time", currentTime);
            senderMessageBody.put("date", currentDate);
            senderMessageBody.put("type", "text");
            senderMessageBody.put("from", messageSenderid);

            /*Map recieverMessageBody = new HashMap();
            recieverMessageBody.put("message", message);
            recieverMessageBody.put("time", currentTime);
            recieverMessageBody.put("date", currentDate);
            recieverMessageBody.put("type", "text");
            recieverMessageBody.put("from", messageSenderid);*/

            Map messageBodyReference = new HashMap();
            messageBodyReference.put(messageSenderReference + "/" + messageUniqueKey, senderMessageBody);
            messageBodyReference.put(messageRecieverReference + "/" + messageUniqueKey, senderMessageBody);
            //messageBodyReference.put(messageRecieverReference + "/" + messageUniqueKey, recieverMessageBody);

            /*clear message text field*/
            mTextMessageField.setText("");

            /*add the message reference and body to firebase*/
            mRootDatabaseReference.updateChildren(messageBodyReference).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {

                    } else {
                        Toast.makeText(ChatActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }
    }

    /*display the message reciever name and profile image*/
    private void displayUserNameAndImage() {

        mUserDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    messageRecieverFirstName = dataSnapshot.child("firstName").getValue().toString();
                    messageRecieverLastName = dataSnapshot.child("lastName").getValue().toString();
                    messageRecieverFullName = messageRecieverFirstName + " " + messageRecieverLastName;
                    messageRecieverImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();

                    mChatToolbarUsername.setText(messageRecieverFullName);
                    Picasso.get().load(messageRecieverImageUrl).networkPolicy(NetworkPolicy.OFFLINE)
                            .fit().into(mChatToolbarProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(messageRecieverImageUrl).fit().into(mChatToolbarProfileImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
