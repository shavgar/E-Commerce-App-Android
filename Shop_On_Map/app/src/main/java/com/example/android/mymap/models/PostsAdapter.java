package com.example.android.mymap.models;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.mymap.ui.ChatActivity;
import com.example.android.mymap.MapsActivity;
import com.example.android.mymap.R;
import com.example.android.mymap.ui.EditPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "RecyclerViewAdapter";
    private static final int CALL_REQUEST =1 ;

    private Context context;
    private ArrayList<NewPostDetails> postArrayList;

    private ImageView cardImage;
    private CircleImageView cardUserImage, cardEdit, cardDelete, cardPhone, cardChat, cardLike;
    private TextView cardTitle, cardDescription, cardUsername, cardPrice, cardLikesCounter;
    private LinearLayout parentLayout;
    private String postKey;
    private boolean isPostEditable;
    private boolean isPhoneAvailable;
    private String userid, userName, profileImageUrl;


    /*Firebase*/
    private DatabaseReference mPostDatabaseReference, mUserDatabaseReference, mPostLikesDatabaseReference;
    private StorageReference mImageStorageReference;
    private FirebaseAuth mAuth;

    public PostsAdapter(Context context, ArrayList<NewPostDetails> newPostArrayList) {
        this.context = context;
        this.postArrayList = newPostArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called");

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_listitems_layout, parent, false);
        ViewHolder viewholder = new ViewHolder(view);
        return viewholder;


    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewholder, final int position) {
        Log.d(TAG, "onBindViewHolder: called");

        /*get the post key(id) from firebase */
        postKey = postArrayList.get(position).getPostKey();
        userid = postArrayList.get(position).getUserid();
        mUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);
        mPostLikesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        mUserDatabaseReference.keepSynced(true);

        /*load the content of the post into the cardview*/
        Picasso.get().load(postArrayList.get(position).getPostImageUrl()).networkPolicy(NetworkPolicy.OFFLINE)
                .fit().into(cardImage, new Callback() {
            @Override
            public void onSuccess() {
                /*load the image offline*/
            }

            @Override
            public void onError(Exception e) {
                /*if the user synced for the first time, then there will be no offlince image to load
                 * so, we will load the image online*/
                Picasso.get().load(postArrayList.get(position).getPostImageUrl()).fit().into(cardImage);
            }
        });//END OF Picass...

        cardTitle.setText(postArrayList.get(position).getPostType());
        cardDescription.setText(postArrayList.get(position).getPostDescription());
        cardPrice.setText("$" + postArrayList.get(position).getPostPrice());

        /*get the user id
         * fetch the user name and image from "Users" node in the database
         * assign the user name and image to the post*/
        mUserDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                userName = dataSnapshot.child("firstName").getValue().toString();
                Log.d(TAG, "onDataChange: firstname" + userName + "image :" + profileImageUrl);
                Picasso.get().load(profileImageUrl).networkPolicy(NetworkPolicy.OFFLINE).fit().into(cardUserImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(profileImageUrl).fit().into(cardUserImage);
                    }
                });
                cardUsername.setText(userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });//END OF  mUserDatabaseReference.addValueEventListener

        /*check if the post belongs to the current user or to ther users*/
        isPostEditable = postArrayList.get(position).isPostEditable();
        Log.d(TAG, "onBindViewHolder: postEditable " + isPostEditable);

        if (isPostEditable) {
            cardEdit.setVisibility(View.VISIBLE);
            cardDelete.setVisibility(View.VISIBLE);
            cardChat.setVisibility(View.GONE);
            cardPhone.setVisibility(View.GONE);
        }

        isPhoneAvailable = postArrayList.get(position).isPhoneAvailable();
        Log.d(TAG, "onBindViewHolder: phoneAvailable: "+isPhoneAvailable);
        if(isPhoneAvailable == false){
            cardPhone.setVisibility(View.GONE);
        }

        cardEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Edit Post")
                        .setMessage("Do you want to edit this post?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /*get the post key when the post gets clicked*/
                                postKey = postArrayList.get(position).getPostKey();
                                /*send the user to the "EditPost" activity*/
                                Intent intent = new Intent(v.getContext(), EditPost.class);
                                /*send the post key to "EditPost" activity*/
                                intent.putExtra("PostKey", postKey);
                                v.getContext().startActivity(intent);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        });//END OF cardEdit.setOnClickListener

        /*delete post*/
        cardDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                /*get the post key when the post gets clicked*/
                postKey = postArrayList.get(position).getPostKey();
                /*thid post has my user id because it belongs to me*/
                userid = postArrayList.get(position).getUserid();
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Do you want to delete this post?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPostDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Posts").child(userid).child(postKey);
                                mPostDatabaseReference.keepSynced(true);
                                mImageStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(postArrayList.get(position).getPostImageUrl());
                                /*remove item from firebase database*/
                                mPostDatabaseReference.removeValue();
                                /*remove item image from firebase storage*/
                                mImageStorageReference.delete();
                                postArrayList.remove(viewholder.getAdapterPosition());
                                /*update the recyclerview content after item deletion*/
                                notifyDataSetChanged();
                                notifyItemRemoved(viewholder.getAdapterPosition());
                                Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_LONG).show();

                                if (postArrayList.isEmpty()) {
                                    Intent intent = new Intent(v.getContext(), MapsActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    v.getContext().startActivity(intent);
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        });//end of cardDelete listener

        /*check the post database and look for current user id
        * if database has it then change the like image to red
        * if database doesn't have it change the like image to black*/
        final String currentUserid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mPostLikesDatabaseReference.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(currentUserid).exists()) {
                    Log.d(TAG, "onDataChange: if has child " + dataSnapshot.hasChild(currentUserid));
                    cardLike.setImageResource(R.drawable.like);
                    cardLike.setTag("liked");
                } else {
                    Log.d(TAG, "onDataChange: datasnapshot child" + dataSnapshot.getChildren().toString()+" currentuserid+ "+currentUserid);
                    Log.d(TAG, "onDataChange: else has child " + dataSnapshot.hasChild(currentUserid));
                    cardLike.setImageResource(R.drawable.dislike);
                    cardLike.setTag("disliked");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*check if the current user whether already liked the post or not
        * if he already liked the post then delete his user id from database
        * if he didn't like the post already then add his user id to the database*/
        cardLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: cardLike + clikced");
                if (cardLike.getTag().equals("disliked")) {
                    Log.d(TAG, "onDataChange: if cardLike tag " + cardLike.getTag());
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postKey).child(currentUserid).setValue(true);
                } else {
                    Log.d(TAG, "onDataChange: else cardLike tag " + cardLike.getTag());
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postKey).child(currentUserid).removeValue();
                }
            }
        });//END OF cardLike.setOnClickListener

        /*get the number of likes for the post*/
        mPostLikesDatabaseReference.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    cardLikesCounter.setText(dataSnapshot.getChildrenCount() + "");
                }else{
                    cardLikesCounter.setText(dataSnapshot.getChildrenCount() + "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });//END OF cardLikedCounter
        ;
        /*click on chat button*/
        cardChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onBindViewHolder: newPostArrayList::: "+postArrayList.get(position).getUserid());
                Intent intent = new Intent(v.getContext(), ChatActivity.class);
                intent.putExtra("userid",postArrayList.get(position).getUserid());
                v.getContext().startActivity(intent);
            }
        });

        /*click on phone button*/
        cardPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Phone call")
                        .setMessage("Do you want to call this person?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                               makePhoneCall(v);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create();
                builder.show();

            }
        });

    }//END OF onBindViewHolder

    /*make a phone call to the user*/
    private void makePhoneCall(View view) {
        Toast.makeText(context, "phone clicked", Toast.LENGTH_SHORT).show();
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, CALL_REQUEST);
        }else{
            /*view.getContext().startActivity();*/
            String phoneNumber = "tel:"+ "07824853858";
            view.getContext().startActivity(new Intent (Intent.ACTION_CALL, Uri.parse(phoneNumber)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CALL_REQUEST){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                String phoneNumber = "tel:"+"07824853858";
                context.startActivity(new Intent (Intent.ACTION_CALL, Uri.parse(phoneNumber)));
            }else{
                Toast.makeText(context, "Phone call permission denied", Toast.LENGTH_SHORT).show();
            }
        }

    }
    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: called");
        if (postArrayList != null) {
            return postArrayList.size();
        }/*else{
            return 1;
        }*/
        return postArrayList.size();
    }

    /*getItemId() & getItemViewType() do the following:
     * prevent the items in the recyclerview to be duplicated
     * avoid the items order to be mixed up*/
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "ViewHolder";

        public ViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder: called");

            cardImage = itemView.findViewById(R.id.cardview_image);
            cardUserImage = itemView.findViewById(R.id.cardview_user_image);
            cardTitle = itemView.findViewById(R.id.cardview_title);
            cardDescription = itemView.findViewById(R.id.cardview_description);
            cardUsername = itemView.findViewById(R.id.cardview_username);
            cardPrice = itemView.findViewById(R.id.cardview_price);
            parentLayout = itemView.findViewById(R.id.cardview_item_parent_layout);
            cardEdit = itemView.findViewById(R.id.cardview_edit_button);
            cardDelete = itemView.findViewById(R.id.cardview_delete_button);
            cardPhone = itemView.findViewById(R.id.cardview_phone_button);
            cardChat = itemView.findViewById(R.id.cardview_chat_button);
            cardLike = itemView.findViewById(R.id.cardview_like_button);
            cardLikesCounter = itemView.findViewById(R.id.cardview_likes_counter);

        }
    }
}
