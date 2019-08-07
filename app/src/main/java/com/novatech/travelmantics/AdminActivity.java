package com.novatech.travelmantics;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.Objects;


public class AdminActivity extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private static final int PICTURE_RESULT = 24;
    TravelDeal mDeal;
    EditText editTxtTitle;
    EditText editTxtDescription;
    EditText editTxtPrice;
    StorageTask uploadTask;
    ImageView dealImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

       Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mFirebaseDatabase =FirebaseUtil.mFirebaseDb;
        mDatabaseReference=FirebaseUtil.mDbReference;
        editTxtTitle = findViewById(R.id.et_title);
        editTxtDescription =findViewById(R.id.et_description);
        editTxtPrice =findViewById(R.id.et_price);
        dealImageView = findViewById(R.id.image);
        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) getIntent().getSerializableExtra("Deal");
        mDeal = deal == null ? new TravelDeal() : deal;
        createDeal();

        Button btnImage = findViewById(R.id.img_btn);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                imageIntent.setType("image/*").putExtra(imageIntent.EXTRA_LOCAL_ONLY, true);
                AdminActivity.this.startActivityForResult(imageIntent.createChooser(imageIntent, "Insert Picture"), PICTURE_RESULT);
            }
        });
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                final StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());

                uploadTask = ref.putFile(imageUri);

                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            String mUri = downloadUri.toString();
                            String pictureName = task.getResult().getPath();
                            mDeal.setImageUrl(mUri);
                            mDeal.setImageName(pictureName);
                            Log.d("Uri", mUri);
                            Log.d("Name", pictureName);
                            AdminActivity.this.showImage(mUri);
                        } else {
                            Toast.makeText(AdminActivity.this, "Image Upload Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void createDeal() {
        editTxtTitle.setText(mDeal.getTitle());
        editTxtPrice.setText(mDeal.getPrice());
        editTxtDescription.setText(mDeal.getDescription());
        showImage(mDeal.getImageUrl());
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu,menu);
        if (FirebaseUtil.isAdmin) {
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditTexts(true);
            findViewById(R.id.img_btn).setEnabled(true);
        } else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditTexts(false);
            findViewById(R.id.img_btn).setEnabled(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item ) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                finish();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                backToList();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void backToList() {
        startActivity(new Intent(this, UserActivity.class));
        finish();
    }

    private void showImage(String url) {
        if (url != null && !url.isEmpty()) return;
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        Picasso.get()
                .load(url)
                .resize(width, width*2/3)
                .centerCrop()
                .into(dealImageView);
    }

    private void switchEditTexts(boolean isAdmin) {
        editTxtPrice.setEnabled(isAdmin);
        editTxtDescription.setEnabled(isAdmin);
        editTxtTitle.setEnabled(isAdmin);
        editTxtDescription.setFocusable(isAdmin);
        editTxtPrice.setFocusable(isAdmin);
        editTxtTitle.setFocusable(isAdmin);
    }

    private void saveDeal() {
        mDeal.setTitle(editTxtTitle.getText().toString());
        mDeal.setDescription(editTxtDescription.getText().toString());
        mDeal.setPrice(editTxtPrice.getText().toString());
        if(mDeal.getId() == null) {
            mDatabaseReference.push().setValue(mDeal);
        } else {
            mDatabaseReference.child(mDeal.getId()).setValue(mDeal);
        }
    }

    private void deleteDeal() {
        if (mDeal.getId() == null) {
            Toast.makeText(this, "Please ensure to save deal before deleting ", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReference.child(mDeal.getId()).removeValue();
        if (mDeal.getImageName() != null && !mDeal.getImageName().isEmpty()) {
            StorageReference picRef = FirebaseUtil.mStorage.getReference().child(mDeal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image", "Image Successfully deleted");
                    Toast.makeText(AdminActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete image", e.getMessage());
                }
            });
        }
        Toast.makeText(this, "Deal Deleted", Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void clean() {
        editTxtTitle.setText("");
        editTxtDescription.setText("");
        editTxtPrice.setText("");
        editTxtTitle.requestFocus();
    }

    private void enableEditTexts(boolean isEnabled) {
        editTxtTitle.setEnabled(isEnabled);
        editTxtDescription.setEnabled(isEnabled);
        editTxtPrice.setEnabled(isEnabled);
    }

}
