package com.example.valarmorghulis.firebaseauth;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener{


    private FirebaseAuth mAuth;
    EditText editTextEmail;
    TextInputEditText editTextPassword;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (TextInputEditText) findViewById(R.id.editTextPassword);
        progressBar=findViewById(R.id.progressbar);

        mAuth=FirebaseAuth.getInstance();

        findViewById(R.id.textViewSignup).setOnClickListener(this);
        findViewById(R.id.buttonLogin).setOnClickListener(this);
        findViewById(R.id.textView_forgotPass).setOnClickListener(this);

    }

    private void userLogin(){
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

        if(password.length()<6 || password.length()>15){
            editTextPassword.setError("Password should be of 6-15 characters");
            editTextPassword.requestFocus();
            return;
        }

        String upperCaseChars = "(.*[A-Z].*)";
        if (!password.matches(upperCaseChars ))
        {
            //editTextPassword.setError("Password should contain atleast one upper case alphabet");
            editTextPassword.setError("Password should contain at least one number, one lowercase letter, one uppercase letter, and one special character.");
            editTextPassword.requestFocus();
            return;
        }

        String lowerCaseChars = "(.*[a-z].*)";
        if (!password.matches(lowerCaseChars ))
        {
            editTextPassword.setError("Password should contain at least one number, one lowercase letter, one uppercase letter, and one special character.");
            editTextPassword.requestFocus();
            return;
        }

        String numbers = "(.*[0-9].*)";
        if (!password.matches(numbers))
        {
            editTextPassword.setError("Password should contain at least one number, one lowercase letter, one uppercase letter, and one special character.");
            editTextPassword.requestFocus();
            return;
        }

        String specialChars = "(.*[,~,!,@,#,$,%,^,&,*,(,),-,_,=,+,[,{,],},|,;,:,<,>,/,?].*$)";
        if (!password.matches(specialChars ))
        {
            editTextPassword.setError("Password should contain at least one number, one lowercase letter, one uppercase letter, and one special character.");
            editTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if(task.isSuccessful()) {
                    if (mAuth.getCurrentUser().isEmailVerified()) {
                        finish();
                        Intent intent = new Intent(LoginActivity.this, DrawerActivity.class);
                        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    else{
                        editTextPassword.setText("");
                        Toast.makeText(LoginActivity.this,"Verify your email to login",Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    @Override
    protected void onStart(){
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(this,DrawerActivity.class));
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){

            case R.id.textViewSignup:
                Intent intentSignup = new Intent(this,SignUpActivity.class);
                intentSignup.addFlags(intentSignup.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentSignup);
                finish();
                break;

            case R.id.buttonLogin:
                userLogin();
                break;

            case R.id.textView_forgotPass:
                Intent intentResetPass = new Intent(this,ResetPassActivity.class);
                intentResetPass.addFlags(intentResetPass.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentResetPass);
                finish();
                break;

        }

    }
}
