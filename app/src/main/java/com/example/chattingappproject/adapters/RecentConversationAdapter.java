package com.example.chattingappproject.adapters;

import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chattingappproject.databinding.ItemRecentConversationsBinding;
import com.example.chattingappproject.listeners.ConversationListener;
import com.example.chattingappproject.models.ChatMessage;
import com.example.chattingappproject.models.User;

import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.conversationHolder> {
    private final List<ChatMessage> chatmsgs;
    private final ConversationListener conversationListener;

    public RecentConversationAdapter(List<ChatMessage> chatmsgs,ConversationListener conversationListenr) {
        this.chatmsgs = chatmsgs;
        this.conversationListener = conversationListenr ;
    }

    @NonNull
    @Override
    public conversationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new conversationHolder(
                ItemRecentConversationsBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull conversationHolder holder, int position) {
        holder.setData(chatmsgs.get(position));
    }

    @Override
    public int getItemCount() {
        return chatmsgs.size() ;
    }

    class conversationHolder extends RecyclerView.ViewHolder{
     ItemRecentConversationsBinding binding;
     conversationHolder(ItemRecentConversationsBinding itemRecentConversationsBinding){
      super(itemRecentConversationsBinding.getRoot());
      binding = itemRecentConversationsBinding;
     }
     void setData(ChatMessage msg){
         binding.imageProfile.setImageBitmap(getConversationImage(msg.conversationImage));
         binding.textName.setText(msg.conversationName);
         binding.textRecentMessage.setText(msg.message);
         binding.getRoot().setOnClickListener(view -> {
             User user = new User();
             user.id = msg.conversationID;
             user.name = msg.conversationName;
             user.image = msg.conversationImage;
             conversationListener.onConversationClicked(user);
         });
     }
}
    private Bitmap getConversationImage(String ImageDecoded){
        byte [] bytes = Base64.decode(ImageDecoded,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
