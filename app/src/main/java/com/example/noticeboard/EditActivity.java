package com.example.noticeboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;

public class EditActivity extends AppCompatActivity {
    StorageReference mstorageRef;
    private ImageView imgItem;
    private Uri uploadUri;
    private Spinner spinner;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private EditText etTitle, etPrice, etPhone, etDescription;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);
        Init();
    }

    private void Init(){
        etTitle = findViewById(R.id.et_title);
        etPhone = findViewById(R.id.et_phone);
        etPrice = findViewById(R.id.et_price);
        etDescription = findViewById(R.id.et_description);

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        mstorageRef = FirebaseStorage.getInstance().getReference("Images");
        imgItem = findViewById(R.id.imgItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 10 && data != null && data.getData() != null)
        {
            if(resultCode==RESULT_OK)
            {
                imgItem.setImageURI(data.getData());
                uploadImage();
            }
        }
    }

    private void uploadImage()
    {
        Bitmap bitmap = ((BitmapDrawable)imgItem.getDrawable()).getBitmap();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        byte[] byteArray = out.toByteArray();
        final StorageReference mRef = mstorageRef.child(System.currentTimeMillis() + "_image");
        UploadTask up = mRef.putBytes(byteArray);
        Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                uploadUri = task.getResult();
                assert uploadUri != null;
                Toast.makeText(EditActivity.this, "Upload done : " + uploadUri.toString(), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public void onClickSavePost(View view)
    {
        savePost();
    }

    public void onClickImage(View view)
    {
        getImage();
    }
    private void getImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 10);
    }

    private void savePost(){
        dbRef = FirebaseDatabase.getInstance("https://noticeboard-f57fb-default-rtdb.europe-west1.firebasedatabase.app/").getReference(spinner.getSelectedItem().toString());
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getUid() != null)
        {
            String key = dbRef.push().getKey();
            NewPost post = new NewPost();
            post.setImageId(uploadUri.toString());
            post.setTitle(etTitle.getText().toString());
            post.setPhone(etPhone.getText().toString());
            post.setPrice(etPrice.getText().toString());
            post.setDescription(etDescription.getText().toString());
            post.setKey(key);
            if(key != null) dbRef.child(mAuth.getUid()).child(key).setValue(post);
        }
    }
}
