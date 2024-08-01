package com.example.chattingappproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chattingappproject.R;
import com.example.chattingappproject.adapters.UsersAdapter;
import com.example.chattingappproject.databinding.ActivityUsersBinding;
import com.example.chattingappproject.listeners.UserListener;
import com.example.chattingappproject.models.User;
import com.example.chattingappproject.utilities.Constants;
import com.example.chattingappproject.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {
private ActivityUsersBinding binding ;
private PreferenceManager preferenceManager ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setListeners();
        getUsers();
    }
    private void setListeners() {
    binding.imageBack.setOnClickListener(view ->
        onBackPressed());

    }
    private void getUsers() {
        loading(true); // Set loading state to true
        FirebaseFirestore database = FirebaseFirestore.getInstance(); // Get Firestore instance
        database.collection(Constants.KEY_COLLECTION_USERS) // Access the users collection
                .get()
                .addOnCompleteListener(task -> { // Add a completion listener
                    loading(false); // Set loading state to false
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID); // Get current user ID
                    if (task.isSuccessful() && task.getResult() != null) { // Check if task is successful and contains results
                        List<User> users = new ArrayList<>(); // Create a list to store user objects
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) { // Loop through the results
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) { // Skip the current user
                                continue;
                            }
                            User user = new User(); // Create a new user object
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME); // Set user name
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL); // Set user email
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE); // Set user image
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN); // Set user token
                            user.id = queryDocumentSnapshot.getId(); // Set user ID
                            users.add(user); // Add user to the list
                        }
                        if (users.size() > 0) {
                            UsersAdapter usersAdapter = new UsersAdapter(users,this); // Create a new UsersAdapter with the list of users
                            binding.UsersRecyclerView.setAdapter(usersAdapter); // Set the adapter for the RecyclerView
                            binding.UsersRecyclerView.setVisibility(View.VISIBLE); // Show the RecyclerView
                        } else {
                            showErrorMessage(); // Show error message if no users are found
                        }
                    } else {
                        showErrorMessage(); // Show error message if task is not successful or results are null
                    }
                });
    }
    private void showErrorMessage(){
        binding.textErrormsg.setText(String.format("%s", "No user available"));
        binding.textErrormsg.setVisibility(View.VISIBLE);
    }
    private void loading (Boolean isLoading){
        if (isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }

    }

 @Override
    public void onUserClicked(User user){
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }

}