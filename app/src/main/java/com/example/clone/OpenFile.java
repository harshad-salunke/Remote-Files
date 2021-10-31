package com.example.clone;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

public class OpenFile extends AppCompatActivity {
    String DATA_BASE_PATH=null;
    FloatingActionButton fmain,imagepicker,filecreater,docpicker;
    OvershootInterpolator overshootInterpolator=new OvershootInterpolator();
    boolean menuopen=false;
    final   static int CAM_PERMI_CODE=101;
    final   int CAM_OPEN_CODE=1001;
    final int PHOTO_PICKER=1;
//    ProgressBar progressBar;
    final int DOCUMENT_PICKER=2;
    Uri selectedImageUrl=null;
    ListView listView;
    ArrayList<UserData> ALL_DATA=new ArrayList<>();
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    String storagerefrencechild;
    DatabaseReference databaseReference;
    FirebaseStorage firebaseStorage;
    list_addapter list_addapter;
    ChildEventListener childEventListener;
    boolean check=false;
    String USER_DataBase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_file);
        Intent intent=getIntent();
        firebaseAuth=FirebaseAuth.getInstance();
        USER_DataBase=firebaseAuth.getCurrentUser().getEmail().replace("@gmail.com","");
        ActionBar actionBar=getSupportActionBar();
        DATA_BASE_PATH=intent.getStringExtra("package com.example.clone");
        String file_name=intent.getStringExtra("package com.example.clone2");
            ColorDrawable colorDrawable=new ColorDrawable(Color.parseColor("#3EBFA5"));
            actionBar.setBackgroundDrawable(colorDrawable);
            actionBar.setTitle(file_name);
            actionBar.setSubtitle("Folder");
            ShowMenu();
            firebaseAuth=FirebaseAuth.getInstance();
        storagerefrencechild=firebaseAuth.getCurrentUser().getEmail().replace("@gmail.com","");

             firebaseStorage=FirebaseStorage.getInstance();
            firebaseDatabase=FirebaseDatabase.getInstance();
            databaseReference=firebaseDatabase.getReference().child(DATA_BASE_PATH);

            listView=findViewById(R.id.file_list_item_view);
           list_addapter=new list_addapter(this,ALL_DATA);
        listView.setAdapter(list_addapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserData userData=(UserData)parent.getItemAtPosition(position);
                if(userData.getFilename()!=null){
                    String file_path=userData.getFilename();
                    String FOLDER_PATH=DATA_BASE_PATH+"/"+file_path;
                    Intent intent=new Intent(getApplicationContext(),OpenFile.class);
                    intent.putExtra("package com.example.clone",FOLDER_PATH);
                    intent.putExtra("package com.example.clone2",file_path);
                    startActivity(intent);

                }
                if (userData.getImage()!=null){
                    String file_name=userData.getLocation();
                    String file_url=userData.getImage();
                    Intent intent=new Intent(getApplicationContext(),Open_Image.class);
                    intent.putExtra("package com.example.clone.image_location",file_name);
                    intent.putExtra("package com.example.clone.image_url",file_url);
                    startActivity(intent);
                }
                if (userData.getDocu()!=null){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(userData.getDocu()),"application/pdf");
                    startActivity(intent);
                }
            }
        });
        RETRIVEDATA();
    }


    private void ShowMenu() {
        fmain=findViewById(R.id.File_floating_main);
        fmain.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
        imagepicker=findViewById(R.id.File_floating_image);
        docpicker=findViewById(R.id.File_floating_doc);
        filecreater=findViewById(R.id.File_floating_file);
        filecreater.setVisibility(View.INVISIBLE);
        docpicker.setVisibility(View.INVISIBLE);
        imagepicker.setVisibility(View.INVISIBLE);
        imagepicker.setAlpha(0f);
        filecreater.setAlpha(0f);
        docpicker.setAlpha(0f);

        imagepicker.setTranslationY(100f);
        filecreater.setTranslationY(100f);
        docpicker.setTranslationY(100f);

        fmain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(menuopen){
                    CloseMenu();
                }
                else{
                    OpenMenu();
                }
            }
        });

        imagepicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseMenu();
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityIfNeeded(intent,PHOTO_PICKER);
            }
        });

        filecreater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseMenu();
                ShowFileAler();

            }
        });

        docpicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseMenu();
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityIfNeeded(intent,DOCUMENT_PICKER);            }
        });

    }


    private void OpenMenu() {

        menuopen=!menuopen;
        fmain.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
        filecreater.animate().translationY(0f).alpha(1f).setInterpolator(overshootInterpolator).setDuration(1000).start();
        imagepicker.animate().translationY(0f).alpha(1f).setInterpolator(overshootInterpolator).setDuration(2000).start();
        docpicker.animate().translationY(0f).alpha(1f).setInterpolator(overshootInterpolator).setDuration(3000).start();
        filecreater.setVisibility(View.VISIBLE);
        imagepicker.setVisibility(View.VISIBLE);
        docpicker.setVisibility(View.VISIBLE);
    }
    private void CloseMenu()  {
        menuopen=!menuopen;
        fmain.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
        filecreater.animate().translationY(100f).alpha(0f).setInterpolator(overshootInterpolator).setDuration(3000).start();
        imagepicker.animate().translationY(100f).alpha(0f).setInterpolator(overshootInterpolator).setDuration(2000).start();
        docpicker.animate().translationY(100f).alpha(0f).setInterpolator(overshootInterpolator).setDuration(1000).start();
        new CountDownTimer(1000,100){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                filecreater.setVisibility(View.INVISIBLE);
                docpicker.setVisibility(View.INVISIBLE);
                imagepicker.setVisibility(View.INVISIBLE);
            }
        }.start();
    }
    private void ShowFileAler() {
        final AlertDialog.Builder alerfile=new AlertDialog.Builder(OpenFile.this);
        View alertview=getLayoutInflater().inflate(R.layout.file_input,null);
        EditText filename=alertview.findViewById(R.id.file_name_input);
        Button btn_cancel=alertview.findViewById(R.id.cancel_file);
        Button btn_create=alertview.findViewById(R.id.cerate_file);
        alerfile.setView(alertview);
        final AlertDialog alertDialog=alerfile.create();
        alertDialog.setCanceledOnTouchOutside(false);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String File=filename.getText().toString();
                if(!File.isEmpty()){
                    UserData userData=new UserData();
                    userData.setFilename(File);
                    databaseReference.push().setValue(userData);
                    alertDialog.dismiss();
                }
                else {
                    filename.setError("File name is Empty");
                }
            }
        });
        alertDialog.show();
    }


    public void  RETRIVEDATA(){

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                UserData data=null;
                try{
                    data=snapshot.getValue(UserData.class);
                    if(data.getImage()!=null || data.getFilename()!=null || data.getDocu()!=null){
                        list_addapter.add(data);
                    }

                }catch (Exception e){
                }

            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        };
       databaseReference.addChildEventListener(childEventListener);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PHOTO_PICKER && resultCode==RESULT_OK){
            AddtoStorage(data,requestCode,null);
        }
        if(requestCode==DOCUMENT_PICKER && resultCode==RESULT_OK){
            AddtoStorage(data,requestCode,null);

        }

    }

    private void AddtoStorage(Intent data,int requestCode,Uri uri) {

        selectedImageUrl=null;
        String text_head=" Name";
        if(uri!=null){
            selectedImageUrl=uri;

        }
        else {
            selectedImageUrl=data.getData();


        }
        check=false;

        String file_name=selectedImageUrl.getLastPathSegment();
        if (file_name.contains("document")){
            check=true;
        }
        final AlertDialog.Builder alerfile=new AlertDialog.Builder(OpenFile.this);
        View alertview=getLayoutInflater().inflate(R.layout.file_input,null);
        TextView textView=alertview.findViewById(R.id.textView);
        EditText filename=alertview.findViewById(R.id.file_name_input);
        Button btn_cancel=alertview.findViewById(R.id.cancel_file);
        Button btn_create=alertview.findViewById(R.id.cerate_file);
        textView.setText(text_head);
        btn_create.setText("Save");
        filename.setText(file_name);
        alerfile.setView(alertview);
        final AlertDialog alertDialog=alerfile.create();
        alertDialog.setCanceledOnTouchOutside(false);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();
            }
        });

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String file_name=filename.getText().toString();
                if(!file_name.isEmpty()){
                    if(check){
                        file_name=file_name+".pdf";
                    }
                    Save_fileDatabase(requestCode,selectedImageUrl, file_name);

                    alertDialog.dismiss();
                }
                else {
                    filename.setError("File name is Empty");
                }
            }
        });
        alertDialog.show();


    }

    private void Save_fileDatabase(int requestCode,Uri selectedImageUrl,String file_name){
        ProgressBar uprogress=findViewById(R.id.file_progress);
        uprogress.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Uploading Started", Toast.LENGTH_SHORT).show();
        String File_location_name=file_name;
        StorageReference photoRef= firebaseStorage.getReference().child(storagerefrencechild).child(file_name);
        //upload photo to firebase storage
        photoRef.putFile(selectedImageUrl).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if(taskSnapshot.getMetadata()!=null){
                    if(taskSnapshot.getMetadata().getReference()!=null){
                        Task<Uri> result=taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                try{
                                    String src_location=uri.toString();
                                    UserData data=new UserData();
                                    if(requestCode==PHOTO_PICKER || requestCode==CAM_OPEN_CODE){
                                        data.setImage(src_location);
                                        String FOLDER_PATH=DATA_BASE_PATH;
                                        databaseReference=FirebaseDatabase.getInstance().getReference().child(FOLDER_PATH);
                                    }
                                    if(requestCode==DOCUMENT_PICKER){
                                        data.setDocu(src_location);
                                        String FOLDER_PATH=DATA_BASE_PATH;
                                        databaseReference=FirebaseDatabase.getInstance().getReference().child(FOLDER_PATH);
                                    }
                                    data.setLocation(File_location_name);
                                    databaseReference.push().setValue(data);
                                    uprogress.setVisibility(View.GONE);
                                    Toast.makeText(OpenFile.this, "Successfully Uploaded", Toast.LENGTH_SHORT).show();

                                }
                                catch (Exception e){
                                    Toast.makeText(OpenFile.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure( Exception e) {
                                Toast.makeText(OpenFile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
    }

}