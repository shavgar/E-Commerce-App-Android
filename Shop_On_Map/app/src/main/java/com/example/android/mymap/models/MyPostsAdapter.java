package com.example.android.mymap.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.mymap.R;
import com.example.android.mymap.ui.NewPost;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.ViewHolder> {
    private ImageView myPostImage;
    private TextView myPostType, myPostPrice, myPostDescription, myPostDate;
    private ArrayList<NewPostDetails> myPostsArrayList;
    private Context context;
    private DatabaseReference mMyPostsDatabaseReference;
    public MyPostsAdapter(Context context ,ArrayList<NewPostDetails> myPostsArrayList){
        this.context = context;
        this.myPostsArrayList = myPostsArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_myposts_layout, null, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        myPostType.setText(myPostsArrayList.get(position).getPostType());
        myPostPrice.setText("$ "+myPostsArrayList.get(position).getPostPrice());
        myPostDescription.setText(myPostsArrayList.get(position).getPostDescription());
        myPostDate.setText(myPostsArrayList.get(position).getPostUploadDate());
        Picasso.get().load(myPostsArrayList.get(position).getPostImageUrl()).fit().into(myPostImage);


    }

    @Override
    public int getItemCount() {
        return myPostsArrayList.size();
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

            myPostImage = itemView.findViewById(R.id.myposts_post_image);
            myPostType = itemView.findViewById(R.id.myposts_post_type);
            myPostPrice = itemView.findViewById(R.id.myposts_post_price);
            myPostDescription = itemView.findViewById(R.id.myposts_post_description);
            myPostDate = itemView.findViewById(R.id.myposts_post_date);;

        }
    }
}
