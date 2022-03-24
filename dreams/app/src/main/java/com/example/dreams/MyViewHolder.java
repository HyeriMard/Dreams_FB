package com.example.dreams;

import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyViewHolder extends RecyclerView.ViewHolder {
    CircleImageView profileImage;
    ImageView postImage,likeImage,commentImage,commentSend,btndelete;
    TextView txtusername,txttimeago,txtpostDesc,txtlikeCounter,txtcmtCounter;
    EditText inputComment;
    public static RecyclerView recyclerView;
    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        profileImage = itemView.findViewById(R.id.profileImagePost);
        postImage = itemView.findViewById(R.id.postimage);
        txtusername = itemView.findViewById(R.id.profileUsernamePost);
        btndelete = itemView.findViewById(R.id.btndelete);
        txttimeago = itemView.findViewById(R.id.timeAgo);
        txtpostDesc = itemView.findViewById(R.id.postDesc);
        likeImage = itemView.findViewById(R.id.likeImage);
        commentImage = itemView.findViewById(R.id.commentsImage);
        txtlikeCounter = itemView.findViewById(R.id.likeCounter);
        txtcmtCounter = itemView.findViewById(R.id.commentCounter);
        commentSend = itemView.findViewById(R.id.sendComment);
        inputComment = itemView.findViewById(R.id.inputComments);
        recyclerView = itemView.findViewById(R.id.recyclerViewComment);
    }
    //Tổng số like
    public void countlike(String postkey, String uid, DatabaseReference likeRef) {
        likeRef.child(postkey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    int totallike = (int) snapshot.getChildrenCount();
                    txtlikeCounter.setText(totallike+"");
                }
                else
                {
                    txtlikeCounter.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        likeRef.child(postkey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(uid).exists())
                {
                    likeImage.setColorFilter(Color.GREEN);
                }
                else {
                    likeImage.setColorFilter(Color.GRAY);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //Tổng số comment
    public void countComment(String postkey, String uid, DatabaseReference commentRef) {
        commentRef.child(postkey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    int totalComment = (int) snapshot.getChildrenCount();
                    txtcmtCounter.setText(totalComment+"");
                }
                else
                {
                    txtcmtCounter.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
