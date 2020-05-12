package umn.ac.id.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jsibbold.zoomage.ZoomageView;

import umn.ac.id.chatapp.Model.User;

public class FullPictureActivity extends AppCompatActivity {

    ZoomageView fullPic;

    Intent intent;

    String Imgurl, barname;

    Boolean barTogle = true;

    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_picture);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        intent = getIntent();
        Imgurl = intent.getStringExtra("source");
        barname = intent.getStringExtra("userid");

        getSupportActionBar().setTitle(barname);

        reference = FirebaseDatabase.getInstance().getReference("Users").child(barname);
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                getSupportActionBar().setTitle(user.getDisplayname());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        fullPic = (ZoomageView) findViewById(R.id.fullpicture);

        Glide.with(FullPictureActivity.this).load(Imgurl).into(fullPic);




    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
