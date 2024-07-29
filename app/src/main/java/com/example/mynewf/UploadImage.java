package com.example.mynewf;

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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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

public class UploadImage extends AppCompatActivity {

    private Spinner imageCategory;
    private CardView selectImage;
    private Button uploadImage;
    private ImageView galleryImageView;

    private String Category;
    // create REQ variable--->
    private final int REQ=1;
    //complete--->
    private Bitmap bitmap;
    ProgressDialog pd;
    private DatabaseReference reference;
    private StorageReference storageReference;
    String downloadUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        selectImage =findViewById(R.id.addGalleryImage);
        imageCategory =findViewById(R.id.image_category);
        uploadImage = findViewById(R.id.uploadImageBtn);
        galleryImageView = findViewById(R.id.galleryImageView);

        reference = FirebaseDatabase.getInstance().getReference().child("gallery");

        storageReference= FirebaseStorage.getInstance().getReference().child("gallery");

        pd = new ProgressDialog(this);

        String items[]=new String[]{"Select Category","Class Room","Lab","Building","Faculty","Event"};
        // ---->Set adapter for spinner--->
        imageCategory.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,items));
        // complete--->

        imageCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Category= imageCategory.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bitmap==null)
                {
                    Toast.makeText(UploadImage.this, "Please Select Image", Toast.LENGTH_SHORT).show();
                }
                else if(Category.equals("Select Category"))
                {
                    Toast.makeText(UploadImage.this, "Please Select Category", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    pd.setMessage("Uploading..");
                    pd.show();
                    uploadImage();
                }
            }
        });

    }

    private void uploadImage() {

        // **--->  Image Compress  and final img will com in finalimg--->
        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        int quality;
        bitmap.compress(Bitmap.CompressFormat.JPEG,50 ,baos );
        byte[] finalimg=baos.toByteArray();
        //----complete compress--->

        //-----create file path to img--->
        final StorageReference filePath;
        filePath =storageReference.child(finalimg+"jpg");
        //<---complete


        //--->>upload image and we have to pass our activity to add onCompleteListner  task uri making
        final UploadTask uploadTask= filePath.putBytes(finalimg);

        uploadTask.addOnCompleteListener(UploadImage.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
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
                                    uploadData();
                                }
                            });
                        }
                    });
                }
                else
                {
                    pd.dismiss();
                    Toast.makeText(UploadImage.this, "Something event Wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    // ---> to strore data on firebase--->
    private void uploadData() {

        reference= reference.child(Category);
        final String uniquekey= reference.push().getKey();

        //---> set Data--->
        reference.child(uniquekey).setValue(downloadUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(UploadImage.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(UploadImage.this, "Something Went Wrong in category", Toast.LENGTH_SHORT).show();
            }
        });

    }





    // ----->  image open full process----->

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
            galleryImageView.setImageBitmap(bitmap);

        }
    }

    //-----complete----->
}