package com.example.valarmorghulis.firebaseauth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import static android.support.v7.app.AppCompatActivity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private static final int CHOOSE_IMAGE = 101;
    TextView textView, textViewEmail;
    ImageView imageView;
    EditText editText;
    Uri uriProfileImage;
    ProgressBar progressBar;
    String profileimageUrl;
    FirebaseAuth mAuth;
    //private DatabaseReference mDatabaseRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_profile, container, false);


        mAuth = FirebaseAuth.getInstance();
        editText = (EditText) v.findViewById(R.id.editTextDisplayName);
        imageView = (ImageView) v.findViewById(R.id.imageView);
        progressBar = v.findViewById(R.id.progressbar);
        textView = v.findViewById(R.id.textViewVerified);
        textViewEmail = v.findViewById(R.id.text_view_email);
        textViewEmail.setText(mAuth.getCurrentUser().getEmail());

        //mDatabaseRef = FirebaseDatabase.getInstance().getReference("user");

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
            }
        });
        loadUserInformation();

        v.findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle bundle = getArguments();
        if (bundle != null && bundle.getInt("seller") == 1) {
            Toast.makeText(getActivity(), "Complete your profile first", Toast.LENGTH_SHORT).show();
            return;
        }

            /*if (mAuth.getCurrentUser() == null) {
                getActivity().finish();
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }*/

    }

    private void loadUserInformation() {
        NetworkConnection networkConnection = new NetworkConnection();
        if (networkConnection.isConnectedToInternet(getActivity())
                || networkConnection.isConnectedToMobileNetwork(getActivity())
                || networkConnection.isConnectedToWifi(getActivity())) {

        } else {
            networkConnection.showNoInternetAvailableErrorDialog(getActivity());
            return;
        }
        final FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {

            if (user.getPhotoUrl() != null) {
                String photoUrl = user.getPhotoUrl().toString();
                Glide.with(this)
                        .load(photoUrl)
                        .into(imageView);
            }

            if (user.getDisplayName() != null) {
                String displayName = user.getDisplayName();
                editText.setText(displayName);
            }

            if (user.isEmailVerified()) {
                textView.setText("Verified");
            } else {
                textView.setText("Email Not Verified(click here to verify)");

                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        textView.setTextColor(1);
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getActivity(), "Verification email sent", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        }
    }


    private void saveUserInformation() {
        NetworkConnection networkConnection = new NetworkConnection();
        if (networkConnection.isConnectedToInternet(getActivity())
                || networkConnection.isConnectedToMobileNetwork(getActivity())
                || networkConnection.isConnectedToWifi(getActivity())) {

        } else {
            networkConnection.showNoInternetAvailableErrorDialog(getActivity());
            return;
        }
        String displayName = editText.getText().toString();
        if (displayName.isEmpty()) {
            editText.setError("Name Required");
            editText.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        //profileimageUrl = user.getPhotoUrl().toString();

        if (profileimageUrl == null && imageView.getDrawable() == null) {
            Toast.makeText(getActivity(), "No image selected. Click on camera to select profile image", Toast.LENGTH_SHORT).show();
            return;
        }



        /*FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            String token = task.getResult().getToken();
                            User currentUser = new User(token.trim());
                            String uId = mAuth.getCurrentUser().getUid();
                            mDatabaseRef.child(uId).setValue(currentUser);
                        } else {
                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });*/


        if (user != null && profileimageUrl != null) {
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(Uri.parse(profileimageUrl))
                    .build();

            user.updateProfile(profile)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    });

        } else {
            Toast.makeText(getActivity(), "Some error occured", Toast.LENGTH_LONG).show();
            return;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uriProfileImage);
                imageView.setImageBitmap(bitmap);
                uploadImageToFirebaseStorage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebaseStorage() {
        final StorageReference ProfileImageRef;
        ProfileImageRef = FirebaseStorage.getInstance().getReference(mAuth.getCurrentUser().getEmail() + ".jpg");

        if (uriProfileImage != null) {
            progressBar.setVisibility(View.VISIBLE);

            ProfileImageRef.putFile(uriProfileImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.GONE);
                            ProfileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    profileimageUrl = uri.toString();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }


    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select profile image"), CHOOSE_IMAGE);
    }

}
