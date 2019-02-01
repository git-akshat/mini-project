package com.example.valarmorghulis.firebaseauth;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    ProgressBar progressBar;
    EditText editTextEmail,editTextPassword;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword= (EditText) findViewById((R.id.editTextPassword));
        progressBar = findViewById(R.id.progressbar);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.buttonSignUp).setOnClickListener(this);
        findViewById(R.id.textViewLogin).setOnClickListener(this);
    }

    private void registerUser(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        if(email.isEmpty()){
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return;
        }

        if(password.isEmpty()){
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if(password.length()<6){
            editTextPassword.setError("Password too short(min 6 character)");
            editTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                 if(task.isSuccessful()){
                     Toast.makeText(getApplicationContext(),"User Registered Successfully",Toast.LENGTH_SHORT).show();
                     Intent intent=new Intent(SignUpActivity.this,DrawerActivity.class);
                     intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                     startActivity(intent);
                     finish();


                 }
                 else{
                     if(task.getException() instanceof FirebaseAuthUserCollisionException){
                         Toast.makeText(getApplicationContext(),"You are already registered",Toast.LENGTH_SHORT).show();
                     }
                     else{
                         Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                     }
                 }
            }
        });




    }


    @Override
    public void onClick(View view) {
        switch(view.getId()) {

            case R.id.buttonSignUp:
                registerUser();
                break;

            case R.id.textViewLogin:
                Intent intentLogin = new Intent(this, LoginActivity.class);
                intentLogin.addFlags(intentLogin.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentLogin);
                finish();

                break;
        }
    }
}
