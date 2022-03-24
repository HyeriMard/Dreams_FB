package com.example.dreams;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.chrisbanes.photoview.PhotoView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatMyViewHolder extends RecyclerView.ViewHolder {
    CircleImageView fistUserProfile,secondUserProfile;
    TextView fistUserText,secondUserText;
    PhotoView firstImage,secondImage;
    ImageView secondChatCall,firstChatCall;
    public ChatMyViewHolder(@NonNull View itemView) {
        super(itemView);
        secondChatCall = itemView.findViewById(R.id.secondChatCall);
        firstChatCall = itemView.findViewById(R.id.firstChatCall);
        fistUserProfile= itemView.findViewById(R.id.fistUserProfile);
        secondUserProfile= itemView.findViewById(R.id.secondUserProfile);
        fistUserText= itemView.findViewById(R.id.fistUserText);
        secondUserText= itemView.findViewById(R.id.secondUserText);
        secondImage= itemView.findViewById(R.id.secondImage);
        firstImage = itemView.findViewById(R.id.firstImage);
    }
}
