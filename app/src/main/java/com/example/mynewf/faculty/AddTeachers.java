package com.example.mynewf.faculty;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mynewf.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddTeachers extends AppCompatActivity {

    private ImageView addTeacherImage;
    private EditText addTeacherName,addTeacherEmail,addTeacherPost;
    private Spinner addTeacherCategory;
    private Button addTeacherBtn;
    private Bitmap bitmap=null;
    private String Category;

    private final int REQ=1;
    private String name,email,post,downloadUrl = "";
    private ProgressDialog pd;

    private StorageReference storageReference;
    private DatabaseReference reference, dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_teachers);

        addTeacherImage = findViewById(R.id.addTeacherImage);
        addTeacherName = findViewById(R.id.addTeacherName);
        addTeacherEmail = findViewById(R.id.addTeacherEmail);
        addTeacherPost = findViewById(R.id.addTeacherPost);
        addTeacherCategory = findViewById(R.id.addTeacherCategory);
        addTeacherBtn = findViewById(R.id.addTeacherBtn);

        pd= new ProgressDialog(this);   //pd initialise

        reference= FirebaseDatabase.getInstance().getReference().child("teacher");
        storageReference= FirebaseStorage.getInstance().getReference();

        String items[]=new String[]{"Select Category","Computer Science","Master of Computer Application"};
        // ---->Set adapter for spinner--->
        addTeacherCategory.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,items));
        // complete--->

        addTeacherCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Category= addTeacherCategory.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        addTeacherImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        addTeacherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkValidation();
            }
        });
    }

    private void checkValidation() {
        name= addTeacherName.getText().toString();
        email= addTeacherEmail.getText().toString();
        post = addTeacherPost.getText().toString();
        if(name.isEmpty()){
            addTeacherName.setError("Empty");
            addTeacherName.requestFocus();
        }else if (email.isEmpty()){
            addTeacherEmail.setError("Empty");
            addTeacherEmail.requestFocus();
        }else if (post.isEmpty()) {
            addTeacherPost.setError("Empty");
            addTeacherPost.requestFocus();
        }else if(Category.equals("Select Category")){
            Toast.makeText(AddTeachers.this, "please select teacher's category", Toast.LENGTH_SHORT).show();
        }else if(bitmap==null){
            insertData();
        }else {
            uploadImage();
        }
    }

    private void uploadImage() {
        // Progress Dialog show---->
        pd.setMessage("Uploading...");
        pd.show();
        //---complete

        // **--->  Image Compress  and final img will com in finalimg--->
        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        int quality;
        bitmap.compress(Bitmap.CompressFormat.JPEG,50 ,baos );
        byte[] finalimg=baos.toByteArray();
        //----complete compress--->

        //-----create file path to img--->
        final StorageReference filePath;
        filePath =storageReference.child("Teachers").child(finalimg+"jpg");
        //<---complete


        //--->>upload image and we have to pass our activity to add onCompleteListner  task uri making
        final UploadTask uploadTask= filePath.putBytes(finalimg);

        uploadTask.addOnCompleteListener(AddTeachers.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful())
                {
                    // if successfully upload our image in storage then we have to get image path since we can add it in our Database from storage---
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // we use addOnSuccessListner since it can be successfully download
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //  --- WE are getting uri from on success method
                                    downloadUrl = String.valueOf(uri);
                                    insertData();
                                }
                            });
                        }
                    });
                }
                else
                {
                    pd.dismiss();
                    Toast.makeText(AddTeachers.this, "Something event Wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //   <<------complete url making

    }

    private void insertData() {

        dbRef= reference.child(Category);
        final String uniquekey= dbRef.push().getKey();

        TeacherData teacherData= new TeacherData(name,email,post,downloadUrl,uniquekey);
        //---> set Data--->
        dbRef.child(uniquekey).setValue(teacherData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(AddTeachers.this, "Teacher Added Successfully", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddTeachers.this, "Something Went Wrong in category", Toast.LENGTH_SHORT).show();
            }
        });

    }



    private void openGallery() {

        Intent pickImage =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImage,REQ);
        // then we have to create REQ variable--->
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ &&  resultCode == RESULT_OK)
        {
            Uri uri= data.getData();
            // ***  to store data on bitmap , we have to create bitmap variable**-->
            try {
                bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            addTeacherImage.setImageBitmap(bitmap);

        }
    }
}