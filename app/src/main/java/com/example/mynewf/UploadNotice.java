package com.example.mynewf;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UploadNotice extends AppCompatActivity {

   private CardView addImage;
   private EditText noticeTitle;
   private ImageView noticeImageView;
   private final int REQ=1;
   private Bitmap bitmap;
   private Button uploadNoticebtn;
   //  ----variable for Database Reference and Storage Reference
   private DatabaseReference reference,dbRef;
   private StorageReference storageReference;
   ///complete---->
    // variable string to store url
    String downloadUrl = "";
    //complete
    private ProgressDialog pd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_notice);

        //----After create variable reference and storageReference we have to declare initialise
        reference= FirebaseDatabase.getInstance().getReference();
        storageReference= FirebaseStorage.getInstance().getReference();
        //--complete

        pd=new ProgressDialog(this);

        addImage= findViewById(R.id.addImage);
        noticeImageView= findViewById(R.id.noticeImageView);
        noticeTitle= findViewById(R.id.noticeTitle);
        uploadNoticebtn=findViewById(R.id.uploadNotice);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                //*****---> creat method for gallery Access**--->
                openGallery();

            }
        });
       uploadNoticebtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if(noticeTitle.getText().toString().isEmpty())
               {
                   noticeTitle.setError("Empty");
                   noticeTitle.requestFocus();
               }
              else if (bitmap==null)
                  {
                     uploadData();
                  }
              else
               {
                   uploadImage();
               }
           }
       });
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
        filePath =storageReference.child("Notice").child(finalimg+"jpg");
        //<---complete


        //--->>upload image and we have to pass our activity to add onCompleteListner  task uri making
        final UploadTask uploadTask= filePath.putBytes(finalimg);

        uploadTask.addOnCompleteListener(UploadNotice.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
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
                    Toast.makeText(UploadNotice.this, "Something event Wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

  //   <<------complete url making

    }

    private void uploadData() {
        dbRef = reference.child("Notice");

        String title= noticeTitle.getText().toString();
        //----->create unic key to store date in firebase   "in database at first notice -->unit key--> data
        final String uniquekey =dbRef.push().getKey();
        //<<----complete

        //---->  process to get current Date--->
      Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate= new SimpleDateFormat("dd-MM-yy");
        String date= currentDate.format(calForDate.getTime());
        //<-----complete

        //---->  process to get current time--->
        Calendar calForTime= Calendar.getInstance();
        SimpleDateFormat currentTime= new SimpleDateFormat("hh:mm a");
        String time= currentTime.format(calForDate.getTime());
        //<-----complete

        // ----> we have to create a object variable of NoticeData class as the name noticeData for sending all data to NoticeData java class-->
        NoticeData noticeData= new NoticeData(title,downloadUrl,date,time,uniquekey);
        //-----complete

        // ---->to store data on firebase--->
        dbRef.child(uniquekey).setValue(noticeData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(UploadNotice.this, "Notice Uploaded", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(UploadNotice.this, "Something Went wrong", Toast.LENGTH_SHORT).show();
            }
        });
        //-->complete
    }



//-----> open galllery mehod--->
    private void openGallery() {

        Intent pickImage =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImage,REQ);


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
            noticeImageView.setImageBitmap(bitmap);

        }
    }
}