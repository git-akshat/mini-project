package com.example.valarmorghulis.firebaseauth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.example.valarmorghulis.firebaseauth.R.mipmap.ic_loading;

public class BuyFragment extends Fragment {

    ImageView pImage;
    private TextView name;
    private TextView price;
    private TextView seller;
    private TextView sellDate;
    private TextView Desc_tag;
    private TextView Desc_text;
    private Button button_make_offer;
    private Button button_message;
    private Button button_delete;
    boolean mItemClicked = false;
    private String sName;
    private String sEmail;
    private String pName;
    private String bName;
    private String bEmail;
    private int position;
    private String key;
    int imagePosition;
    String stringImageUri;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef;
    private FirebaseStorage mStorage;
    private ValueEventListener mDBListener;
    DatabaseReference userDatabase;
    private List<User> mUser;
    private List<Upload> mUploads;

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
        String testEmail = mAuth.getInstance().getCurrentUser().getEmail();
        if (testEmail.equals(sEmail)) {
            button_make_offer.setVisibility(View.GONE);
            button_message.setVisibility(View.GONE);
            button_delete.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(), "You are seller of this product", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_buy, container, false);
        name = (TextView) v.findViewById(R.id.product_name);
        price = (TextView) v.findViewById(R.id.product_price);
        seller = (TextView) v.findViewById(R.id.product_seller);
        sellDate = (TextView) v.findViewById(R.id.product_date);
        button_make_offer = (Button) v.findViewById(R.id.offer_button);
        button_message = (Button) v.findViewById(R.id.msg_button);
        button_delete = (Button) v.findViewById(R.id.delete_button);

        pImage = (ImageView) v.findViewById(R.id.product_image);
        Desc_tag = (TextView) v.findViewById(R.id.Description_tag);
        Desc_text = (TextView) v.findViewById(R.id.Description);
        bName = mAuth.getInstance().getCurrentUser().getDisplayName();
        bEmail = mAuth.getInstance().getCurrentUser().getEmail();


        mUploads = new ArrayList<>();

        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        Bundle bundle = getArguments();
        if (bundle != null) {
            position = bundle.getInt("position");
            pName = bundle.getString("name");
            String pImageUrl = bundle.getString("imageUrl");
            String pPrice = bundle.getString("price");
            //Bitmap bitmapImage = bundle.getParcelable("bitmapImage");
            sName = bundle.getString("userName");
            key = bundle.getString("key");
            String date = bundle.getString("date");
            String desc = bundle.getString("desc");
            sEmail = bundle.getString("email");
            name.setText(pName);
            price.setText("₹ " + pPrice);
            seller.setText(sName);
            sellDate.setText(date);
            if (desc != null) {
                Desc_tag.setVisibility(View.VISIBLE);
                Desc_text.setVisibility(View.VISIBLE);
                Desc_text.setText(desc);
            }

            //pImage.setImageURI(Uri.parse(pImageUrl));
//            if (bitmapImage != null)
//                pImage.setImageBitmap(bitmapImage);
            if (pImageUrl != null) {
                String photoUrl = pImageUrl;
                Glide.with(this)
                        .load(photoUrl)
                        .into(pImage);
            }


            /*DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            DatabaseReference current = ref.child("fir-auth-431b5").child("user").child("token");
            //User currentUser = mUser.get(email);
*/

        }


        button_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MsgFragment msgFragment = new MsgFragment();
                Bundle bundle = new Bundle();
                bundle.putString("sEmail", sEmail);
                bundle.putString("pName", pName);
                bundle.putString("sName", sName);
                bundle.putString("bName", mAuth.getInstance().getCurrentUser().getDisplayName());
                bundle.putString("bEmail", mAuth.getInstance().getCurrentUser().getEmail());
                msgFragment.setArguments(bundle);
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction().replace(R.id.frag_container, msgFragment)
                        .addToBackStack(null).commit();


//                startActivity(new Intent(getActivity(), MsgActivity.class));
//                getActivity().finish();
            }
        });

        button_make_offer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Are you sure about this?");
                builder.setMessage("This will send an email notification along with your email id to the seller.");

                builder.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendEmailToSeller();
                        sendEmailToBuyer();
                    }
                });

                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });



                AlertDialog ad = builder.create();
                ad.show();



            }
        });

        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mUploads.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setKey(postSnapshot.getKey());
                    mUploads.add(upload);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Alert!");
                builder.setMessage("Deletion is permanent. Are you sure you want to delete?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProduct();
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog ad = builder.create();
                ad.show();
            }
        });

        return v;
    }

    private void deleteProduct(){
        Upload selectedItem = mUploads.get(position);
        final String selectedKey = selectedItem.getKey();

        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startActivity(new Intent(getActivity(), DrawerActivity.class));
                mDatabaseRef.child(selectedKey).removeValue();
                Toast.makeText(getActivity(), "Item deleted", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        });
    }

    private void sendEmailToSeller() {
        String email = sEmail;
        String subject = "[Juggler] Request for product " + pName;

        String msg = "unknown-user";
        if (bName != "")
            msg = bName;
        String thankMsg = "\n\nThank you for using Juggler :)";
        String autoMsg = "\n\nThis is an auto generated email. Please do not reply to this email.";

        String message = "Hey " + sName + ". " + msg + " is requesting for your product \"" + pName + "\". Wait for further response from " + msg + ". If you want you can write to " + msg + " on email id " + bEmail + " ." + thankMsg + autoMsg;
        SendMail sm2s = new SendMail(getActivity(), email, subject, message);
        sm2s.execute();
    }

    private void sendEmailToBuyer() {
        String email = bEmail;
        String subject = "[Juggler] Request Successful for " + pName;
        String thankMsg = "\n\nThank you for using Juggler :)";
        String autoMsg = "\n\nThis is an auto generated email. Please do not reply to this email.";

        String message = "Hello " + bName + ". You have requested " + sName  +" for \"" + pName + "\". You can send message to " + sName + " in the app by clicking on message button." + thankMsg + autoMsg ;
        SendMail sm2b = new SendMail(getActivity(), email, subject, message);
        sm2b.execute();
    }


}
