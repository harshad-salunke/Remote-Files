package com.example.clone.LoginBoard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clone.MainActivity;
import com.example.clone.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    Button login;
    Button SingUp;
    EditText email;
    EditText password;
    FirebaseAuth firebaseAuth;
    AlertDialog.Builder reset_aler;
    TextView textView;
    TextView forgetPass;

    LayoutInflater layoutInflater;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();

        textView=findViewById(R.id.verifytext);
        email = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.alerady);
        forgetPass=findViewById(R.id.forgetPassword);
        layoutInflater=this.getLayoutInflater();
        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               forgtoPassword();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        reset_aler = new AlertDialog.Builder(this);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.setEnabled(false);
                if (email.getText().toString().isEmpty()) {
                    email.setError("Enter Email");
                    login.setEnabled(true);
                    return;
                }
                if (password.getText().toString().isEmpty()) {
                    password.setError("Password is required");
                    login.setEnabled(true);
                    return;
                }
                LoginUser(email.getText().toString(), password.getText().toString());
            }
        });


        SingUp = findViewById(R.id.singin);
        SingUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SingIn.class));
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void forgtoPassword() {
        View view=layoutInflater.inflate(R.layout.reset_passwordview,null);


            reset_aler.setTitle(" Forgot Password")
                    .setMessage("Enter Your Email to get Password Forgot link")
                    .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            forgetPass.setTextColor(R.color.red);
                            EditText editText=view.findViewById(R.id.reset_passwordText);
                            if(editText.getText().toString().isEmpty()){
                                editText.setError("Required Field");
                                Toast.makeText(Login.this, "Not Sended", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            firebaseAuth.sendPasswordResetEmail(editText.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    textView.setVisibility(View.VISIBLE);
                                    textView.setText("Password Forgot Link Send To Your Email");
                                    forgetPass.setTextColor(R.color.red);
                                    Toast.makeText(Login.this, "Forgot Password Link Send To"+editText.getText().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure( Exception e) {
                                    forgetPass.setTextColor(R.color.red);
                                    textView.setEnabled(false);
                                    Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).setNegativeButton("Cancel",null)
                    .setView(view)
                    .create().show();
        forgetPass.setTextColor(R.color.red);

    }

    public void LoginUser(String Email, String Password) {

        firebaseAuth.signInWithEmailAndPassword(Email, Password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                if (!firebaseAuth.getCurrentUser().isEmailVerified()) {
                    SendVerification();

                } else {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();


                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                login.setEnabled(true);
                Log.d("harshad",e.getMessage());
                Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    public void SendVerification() {
        firebaseAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                reset_aler.setTitle("Verify Your Email")
                        .setMessage("Verification Link send to your email if not found check spam Folder")
                        .setPositiveButton("Yes", null).create().show();
                login.setEnabled(false);
                email.setError("Verify your email");
                textView.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure( Exception e) {
                Toast.makeText(Login.this, "fail to send", Toast.LENGTH_SHORT).show();
                login.setEnabled(true);
            }
        });


    }

    @Override
    protected void onPause() {
        login.setEnabled(true);
        textView.setVisibility(View.GONE);
        email.setError("");
        super.onPause();
    }

    @Override
    protected void onStart() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
        super.onStart();
    }
}