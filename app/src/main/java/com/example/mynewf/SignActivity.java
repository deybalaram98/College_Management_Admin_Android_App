package com.example.mynewf;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignActivity extends AppCompatActivity {

    private EditText mEmail,mPass;
    private TextView mTextView;
    private Button signInBtn;
    private String email,pass;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        mEmail = findViewById(R.id.emailSign);
        mPass= findViewById(R.id.passwordSign);
        mTextView = findViewById(R.id.textSign);
        signInBtn = findViewById(R.id.buttonSign);

        mAuth= FirebaseAuth.getInstance();

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignActivity.this,RegActivity.class));
            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String email =mEmail.getText().toString();
        String pass =mPass.getText().toString();
        if(!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            if(!pass.isEmpty()){
                mAuth.signInWithEmailAndPassword(email,pass)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(SignActivity.this, "Login Successfully..", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignActivity.this,MainActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignActivity.this, "Login Failed!!", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                mPass.setError("Please Enter Password");
            }
        }else if( email.isEmpty()){
            mEmail.setError("Please Enter Email Id");
        }else {
            mEmail.setError("Please Enter Currect Email");
        }


    }
}