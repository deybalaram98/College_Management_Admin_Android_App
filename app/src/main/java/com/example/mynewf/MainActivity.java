package com.example.mynewf;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.mynewf.faculty.UpdateFaculty;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //***--->creat card view variable--**
    CardView uploadNotice,addGalleryImage,addEbook,faculty;
    private Button LogoutBtn;
    private FirebaseAuth mAuth;
   // complete-->


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       uploadNotice= findViewById(R.id.addNotice);
       addGalleryImage= findViewById(R.id.addGalleryImage);
        addEbook =findViewById(R.id.addEbook);
        faculty =findViewById(R.id.faculty);
        LogoutBtn= findViewById(R.id.logoutBtn);

        // -----Sign out--->
        mAuth= FirebaseAuth.getInstance();
        LogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this,SignActivity.class));
                finish();
            }
        });
        //---complete-->

        uploadNotice.setOnClickListener(this);
        addGalleryImage.setOnClickListener(this);
        addEbook.setOnClickListener(this);
        faculty.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
       Intent intent;
        switch (view.getId()){
            case R.id.addNotice:
                intent= new Intent(MainActivity.this, UploadNotice.class);
                startActivity(intent);
                break;

            case R.id.addGalleryImage:
                intent= new Intent(MainActivity.this, UploadImage.class);
                startActivity(intent);
                break;

            case R.id.addEbook:
                intent= new Intent(MainActivity.this, UploadPdfActivity.class);
                startActivity(intent);
                break;

            case R.id.faculty:
                intent= new Intent(MainActivity.this, UpdateFaculty.class);
                startActivity(intent);
                break;

        }

    }
}