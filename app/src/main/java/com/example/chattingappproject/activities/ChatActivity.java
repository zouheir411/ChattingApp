package com.example.chattingappproject.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chattingappproject.R;
import com.example.chattingappproject.adapters.ChatAdapter;
import com.example.chattingappproject.databinding.ActivityChatBinding;
import com.example.chattingappproject.models.ChatMessage;
import com.example.chattingappproject.models.User;
import com.example.chattingappproject.utilities.Constants;
import com.example.chattingappproject.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatActivity extends BaseActivity {
private ActivityChatBinding binding ;
private User recieverUser ;
private List<ChatMessage> chatMessages;
private ChatAdapter chatAdapter;
private PreferenceManager preferenceManager;
private FirebaseFirestore database;
private String conversationId = null ;
private Boolean isReceiverAvailable = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
    }
private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(recieverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();

}
private void sendMessage(){
    HashMap<String,Object> message = new HashMap<>();
    message.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
    message.put(Constants.KEY_RECEIVER_ID,recieverUser.id);
    message.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());
    message.put(Constants.KEY_TIMESTAMP,new Date());
    database.collection(Constants.KEY_COLLECTION_CHAT).add(message);

    if(conversationId != null) {
        updateConversation(binding.inputMessage.getText().toString());
    }
    else {
    HashMap<String,Object>conversations = new HashMap<>() ;
    conversations.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
    conversations.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
    conversations.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
    conversations.put(Constants.KEY_RECEIVER_ID,recieverUser.id);
    conversations.put(Constants.KEY_RECEIVER_NAME,recieverUser.name);
    conversations.put(Constants.KEY_RECEIVER_IMAGE,recieverUser.image);
    conversations.put(Constants.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString());
    conversations.put(Constants.KEY_TIMESTAMP,new Date());
    addConversion(conversations);


    }
    binding.inputMessage.setText(null);
}
private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(
recieverUser.id ).addSnapshotListener(ChatActivity.this,((value, error) -> {
    if(error != null){
        return;
    }
    if(value != null){
        if(value.getLong(Constants.KEY_AVAILABILITY) != null){
        int availability = Objects.requireNonNull(
                value.getLong(Constants.KEY_AVAILABILITY)).intValue();
        isReceiverAvailable = availability == 1 ;


        }
    }
    if(isReceiverAvailable){
        binding.TextAvailability.setVisibility(View.VISIBLE);
    }
    else {
        binding.TextAvailability.setVisibility(View.GONE);
    }
        }));

}
private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,recieverUser.id)
                .addSnapshotListener(eventListener) ;
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,recieverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener) ;

}

private final EventListener<QuerySnapshot> eventListener = ((value, error) ->{
    if(error != null){
        return;
    }
    if (value != null) {
        int count = chatMessages.size();
        for (DocumentChange documentChange : value.getDocumentChanges()) {
            if (documentChange.getType() == DocumentChange.Type.ADDED) {
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                chatMessages.add(chatMessage);
            }
        }
        Collections.sort(chatMessages , (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
        if(count == 0) {
            chatAdapter.notifyDataSetChanged();
        }else{
        chatAdapter.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
        binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() -1 );

        }
        binding.chatRecyclerView.setVisibility(View.VISIBLE);

    }
    binding.progressBar.setVisibility(View.GONE);
    if (conversationId == null){
        checkForConversation();    }
});




    private Bitmap getBitmapFromEncodedString(String EncodedImage){
        byte [] bytes = Base64.decode(EncodedImage,Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        return bitmap ;
    }
    private void loadReceiverDetails(){
        recieverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(recieverUser.name);

    }
    private void setListeners() {
        binding.imageBack.setOnClickListener(view ->
                onBackPressed());
        binding.layoutSend.setOnClickListener(view -> sendMessage());
    }
    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }
    public void updateConversation(String Message){
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE,Message,
                Constants.KEY_TIMESTAMP,new Date()
        );
    }
    private void checkForConversation() {
        if (chatMessages.size() != 0) {
            checkForConversationRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    recieverUser.id
            );
            checkForConversationRemotely(
                    recieverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void  checkForConversationRemotely(String senderID , String ReceiverID){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderID)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,ReceiverID)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);

    }
    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

@Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }


}