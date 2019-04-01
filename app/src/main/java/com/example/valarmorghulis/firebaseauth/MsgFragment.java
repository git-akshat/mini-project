package com.example.valarmorghulis.firebaseauth;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MsgFragment extends Fragment implements View.OnClickListener {

    private EditText editTextMessage;
    private Button buttonSend;
    private String sName;
    private String sEmail;
    private String pName;
    private String bName;
    private String bEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_msg, container, false);
        editTextMessage = (EditText) v.findViewById(R.id.editTextMessage);
        buttonSend = (Button) v.findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(this);

        Bundle bundle = getArguments();
        if(bundle != null){
            sName = bundle.getString("sName");
            sEmail = bundle.getString("sEmail");
            bEmail = bundle.getString("bEmail");
            bName = bundle.getString("bName");
            pName = bundle.getString("pName");

        }

        return v;
    }

    private void sendEmail() {
        String email = sEmail;
        String subject = "[Juggler] Querry regarding product " + pName;
        String autoMsg = "\n\nThis is an auto generated email. Please do not reply to this email.";
        String message = editTextMessage.getText().toString().trim() + "\n\nsent by: " + sName + "(" + sEmail + ")\n" + autoMsg;
        SendMail sm = new SendMail(getActivity(), email, subject, message);
        sm.execute();
    }

    @Override
    public void onClick(View v) {
        if(editTextMessage.getText().toString().length() < 1){
            editTextMessage.setError("Message can't be empty.");
            editTextMessage.requestFocus();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Message");
        builder.setMessage("Your message will be send as an email to the owner of this product");

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendEmail();
                editTextMessage.setText("");
            }
        });

        builder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editTextMessage.setText("");
                Toast.makeText(getActivity(),"Message Discarded", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog ad = builder.create();
        ad.show();

    }

}
