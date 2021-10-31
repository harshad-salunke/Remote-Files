package com.example.clone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.clone.LoginBoard.Login;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.URL;

public class ProfileActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FirebaseAuth firebaseAuth;
    TextView textView;
    ImageView imageView;
    Button signou;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();
        bottomNavigationView=findViewById(R.id.profile_navigation);
        bottomNavigationView.setSelectedItemId(R.id.MProfile);
      bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(MenuItem item) {
              switch (item.getItemId()){
                  case R.id.Msearchbar:
                      startActivity(new Intent(getApplicationContext(),SearchActivity.class));
                      overridePendingTransition(0,0);
                      finish();
                      return  true;
                  case R.id.MProfile:
                      return true;
                  case R.id.MclickCamera:
                      startActivity(new Intent(getApplicationContext(),MainActivity.class));
                      overridePendingTransition(0,0);
                      finish();
                      return  true;
                  case R.id.Mhome:
                      startActivity(new Intent(getApplicationContext(),MainActivity.class));
                      overridePendingTransition(0,0);
                      finish();
                      return true;
              }
              return false;
          }
      });


      signou=findViewById(R.id.profile_signout);
        textView=findViewById(R.id.profile_email);
      firebaseAuth=FirebaseAuth.getInstance();
        imageView=findViewById(R.id.profile_pic);
        String images="https://png.pngtree.com/png-clipart/20190614/original/pngtree-vector-cloud-upload-icon-png-image_3767250.jpg";
        Glide.with(imageView.getContext())
                .load(images)
                .into(imageView);

      FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
      String email=firebaseUser.getEmail();
      textView.setText(email);
      signou.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              startActivity(new Intent(getApplicationContext(),Login.class));
              firebaseAuth.signOut();
              finish();
          }
      });



    }
}