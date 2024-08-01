package com.example.chattingappproject.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chattingappproject.R;
import com.example.chattingappproject.databinding.ActivitySignupBinding;
import com.example.chattingappproject.utilities.Constants;
import com.example.chattingappproject.utilities.PreferenceManager;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;


public class SignupActivity extends AppCompatActivity {
private ActivitySignupBinding binding ;
private PreferenceManager preferenceManager ;
private String encodedImage ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setListeners();
    }
    private void setListeners(){
        binding.textSignIn.setOnClickListener(view ->
        onBackPressed());
        binding.buttonSignUp.setOnClickListener(view ->{
            if(isValidSignupDetails()){
                Signup();
            }
        }
        );
        binding.LayoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

    }
    private void Signup(){
    loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String,Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME,binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME,binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());

                });

    }
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150 ;
        int previewHeight = bitmap.getHeight() * previewWidth /bitmap.getWidth() ;
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth ,previewHeight ,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte [] bytes =byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() ==RESULT_OK){
                    if (result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textImageProfile.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }

            }

    );

    private Boolean isValidSignupDetails(){
    if (encodedImage == null ){
        showToast(" Select Profile Image ");
        return false ;
    } else if (binding.inputName.getText().toString().trim().isEmpty()){
        showToast("Enter Your Name ");
        return false;
    }  else if (binding.inputEmail.getText().toString().trim().isEmpty()){
        showToast("Enter Your Email ");
        return false;
    } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
        showToast("Enter a Valid Email-Address");
        return false;
    } else if(binding.inputPassword.getText().toString().trim().isEmpty()){
        showToast("Enter a Password");
        return false;
    }else if(binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
        showToast("Confirm your Password");
        return false;
    }else if(!binding.inputConfirmPassword.getText().toString().equals(binding.inputPassword.getText().toString())){
        showToast("The Passwords don't match kindly re-enter the passwords");
        return false;
    }else{
        return true ;
    }
    }
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.ProgressBar.setVisibility(View.VISIBLE);
        } else {
            binding.ProgressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}