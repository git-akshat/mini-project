package com.example.valarmorghulis.firebaseauth;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.EventLogTags;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;

public class SellFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_CAMERA_REQUEST = 0;

    private Button mButtonChooseImage;
    private Button mButtonUpload;
    private EditText mEditTextFileName;
    private EditText mEditTextFilePrice;
    private ImageView mImageView;
    private TextView mDescription;
    private ProgressBar mProgressBar;
    private Uri mImageUri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;
    FirebaseAuth mAuth;
    Uri uri;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sell, container, false);

        mButtonChooseImage = v.findViewById(R.id.button_choose_image);
        mButtonUpload = v.findViewById(R.id.button_upload);
        mEditTextFileName = v.findViewById(R.id.edit_text_file_name);
        mEditTextFilePrice = v.findViewById(R.id.edit_text_file_price);
        mImageView = v.findViewById(R.id.image_view);
        mProgressBar = v.findViewById(R.id.progress_bar);
        mDescription = v.findViewById(R.id.Description);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(getActivity(), "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    uploadFile();
                }


            }
        });


        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        NetworkConnection networkConnection = new NetworkConnection();
        if (networkConnection.isConnectedToInternet(getActivity())
                || networkConnection.isConnectedToMobileNetwork(getActivity())
                || networkConnection.isConnectedToWifi(getActivity())) {

        } else {
            networkConnection.showNoInternetAvailableErrorDialog(getActivity());
            return;
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            //cropImage();

            Picasso.with(getActivity()).load(mImageUri).into(mImageView);
        }
    }

    private void cropImage() {
        try {
            Intent cropIntent;

            cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(mImageUri, "image/*");

            cropIntent.putExtra("crop", " true");
            cropIntent.putExtra("outputx", 180);
            cropIntent.putExtra("outputY", 180);
            cropIntent.putExtra("aspectx", 3);
            cropIntent.putExtra("aspecty", 4);
            cropIntent.putExtra("scaleUpIfNeeded", true);
            cropIntent.putExtra("return-data ", true);

            startActivityForResult(cropIntent, 1);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (mAuth.getInstance().getCurrentUser().getDisplayName() == null) {
            ProfileFragment profileFragment = new ProfileFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("seller", 1);
            profileFragment.setArguments(bundle);
            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction().replace(R.id.frag_container, profileFragment)
                    .commit();

            return;
        }
        if (mEditTextFileName.getText().toString().trim().isEmpty()) {
            mEditTextFileName.setError("Name required");
            mEditTextFileName.requestFocus();
            return;
        }

        if (mEditTextFilePrice.getText().toString().trim().isEmpty()) {
            mEditTextFilePrice.setError("Price required");
            mEditTextFilePrice.requestFocus();
            return;
        }

        if (mImageUri != null) {
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setProgress(0);
                                }
                            }, 500);

                            mImageUri = null;
                            mImageView.setImageBitmap(null);
                            Toast.makeText(getActivity(), "Upload successful", Toast.LENGTH_LONG).show();

                            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Upload upload = new Upload(mEditTextFileName.getText().toString().trim(),
                                            uri.toString(), mEditTextFilePrice.getText().toString().trim(), mDescription.getText().toString().trim());
                                    String uploadId = mDatabaseRef.push().getKey();
                                    mDatabaseRef.child(uploadId).setValue(upload);
                                    mEditTextFileName.setText("");
                                    mEditTextFilePrice.setText("");
                                    mDescription.setText("");

                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mProgressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(getActivity(), "No file selected", Toast.LENGTH_SHORT).show();
        }

    }


}
