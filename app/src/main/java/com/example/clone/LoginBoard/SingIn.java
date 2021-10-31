package com.example.clone.LoginBoard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.clone.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SingIn extends AppCompatActivity {
    EditText name;
    EditText email;
    EditText password;
    EditText repassword;
    Button resister,arAccount;
    FirebaseAuth firebaseAuth;
    AlertDialog.Builder reset_aler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_in);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();
        name=findViewById( R.id.SUname);
        email=findViewById(R.id.SUemai);
        password=findViewById(R.id.SUpassword);
        repassword=findViewById(R.id.SUrepassword);
        resister=findViewById(R.id.SuResisbutton);
        firebaseAuth=FirebaseAuth.getInstance();
        arAccount=findViewById(R.id.alerady);
        reset_aler = new AlertDialog.Builder(this);
        arAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
            }
        });

        resister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emails=email.getText().toString();
                String pass=password.getText().toString();
                String repass=repassword.getText().toString();

                if( emails.isEmpty()){
                    email.setError("Email is empty");
                    return;
                }
                if(pass.isEmpty()){
                    password.setError("password is empty");
                    return;
                }
                if (!pass.equals(repass)){
                    repassword.setError("Same Password Required");
                    return;
                }
                resister.setEnabled(false);
                firebaseAuth.createUserWithEmailAndPassword(emails,pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        reset_aler.setTitle("Successfully Created")
                                .setMessage("Your Account Successfully Created Go To Login Page and Login ")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                startActivity(new Intent(getApplicationContext(),Login.class));
                                                finish();
                                            }
                                        }).create().show();


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure( Exception e) {
                        resister.setEnabled(true);
                        Toast.makeText(SingIn.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }
}