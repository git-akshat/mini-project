package com.example.valarmorghulis.firebaseauth;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.example.valarmorghulis.firebaseauth.R.mipmap.ic_loading;

public class BuyFragment extends Fragment {

    ImageView pImage;
    private TextView name;
    private TextView price;
    private  TextView seller;
    private TextView sellDate;
    private TextView Desc_tag;
    private TextView Desc_text;
    private Button button_make_offer;
    private Button button_message;
    boolean mItemClicked = false;
    int imagePosition;
    String stringImageUri;
    FirebaseAuth mAuth;

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
        pImage = (ImageView) v.findViewById(R.id.product_image);
        Desc_tag = (TextView) v.findViewById(R.id.Description_tag);
        Desc_text =(TextView) v.findViewById(R.id.Description);

        Bundle bundle = getArguments();
        if(bundle != null) {
            int position = bundle.getInt("position");
            String pName = bundle.getString("name");
            String pImageUrl = bundle.getString("imageUrl");
            String pPrice = bundle.getString("price");
            Bitmap bitmapImage = bundle.getParcelable("bitmapImage");
            String userName = bundle.getString("userName");
            String date = bundle.getString("date");
            String desc = bundle.getString("desc");
            name.setText(pName);
            price.setText("₹ " + pPrice);
            seller.setText(userName);
            sellDate.setText(date);
            if(desc != null){
                Desc_tag.setVisibility(View.VISIBLE);
                Desc_text.setVisibility(View.VISIBLE);
                Desc_text.setText(desc);
            }

            //pImage.setImageURI(Uri.parse(pImageUrl));
            if(bitmapImage != null)
                pImage.setImageBitmap(bitmapImage);
            else {
                String photoUrl = pImageUrl.toString();
                Glide.with(this)
                        .load(photoUrl)
                        .into(pImage);
            }
        }


            return v;

    }
}
