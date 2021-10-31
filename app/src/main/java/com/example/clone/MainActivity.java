package com.example.clone;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

FloatingActionButton fmain,imagepicker,filecreater,docpicker;
OvershootInterpolator overshootInterpolator=new OvershootInterpolator();
TextView textView;
    ArrayList<UserData> ALL_DATA=new ArrayList<>();
    String storagerefrencechild;
    boolean menuopen=false;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    Boolean check=false;
    String USER_DataBase="anyone";
  final   static int CAM_PERMI_CODE=101;
  final   int CAM_OPEN_CODE=1001;
   final int PHOTO_PICKER=1;
   final int DOCUMENT_PICKER=2;
   ChildEventListener childEventListener;
    Uri selectedImageUrl;
    ProgressBar progressBar;
   ListView listView;
   list_addapter list_addapter;
BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar=getSupportActionBar();
        progressBar=findViewById(R.id.progressBar);
        actionBar.hide();
        firebaseAuth=FirebaseAuth.getInstance();
        USER_DataBase=firebaseAuth.getCurrentUser().getEmail().replace("@gmail.com","");
        storagerefrencechild=firebaseAuth.getCurrentUser().getEmail().replace("@gmail.com","");
        listView=findViewById(R.id.list_item_view);
        list_addapter=new list_addapter(this,ALL_DATA);
        listView.setAdapter(list_addapter);
        firebaseDatabase = FirebaseDatabase.getInstance();


        databaseReference = firebaseDatabase.getReference().child(USER_DataBase);

        firebaseStorage=FirebaseStorage.getInstance();
        try{
            RETRIVEDATA();
        }catch (Exception e){

        }


        bottomNavigationView=findViewById(R.id.bottom_nevigation);
        bottomNavigationView.setSelectedItemId(R.id.Mhome);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected( MenuItem item) {
                switch (item.getItemId()){
                    case R.id.Msearchbar:
                        startActivity(new Intent(getApplicationContext(),SearchActivity.class));
                        overridePendingTransition(0,0);
                        finish();
                        return true;
                    case R.id.Mhome:
                        return true;
                    case R.id.MclickCamera:
                        CheckCameraPermission();
                        return  true;
                    case R.id.MProfile:
                        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                        overridePendingTransition(0,0);
                        finish();
                        return true ;
                }
                return false;
            }
        });

        ShowMenu();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserData userData=(UserData)parent.getItemAtPosition(position);

                if(userData.getFilename()!=null){
                    String file_path=userData.getFilename();
                    String FOLDER_PATH=USER_DataBase+"/"+"FOLDERS/"+file_path;
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
                    intent.putExtra("data_base_root",storagerefrencechild);
                    startActivity(intent);

                }
                if (userData.getDocu()!=null){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(userData.getDocu()),"application/pdf");
                    startActivity(intent);
                }
            }
        });

        new CountDownTimer(3000,200){

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
               progressBar.setVisibility(View.GONE);
            }
        }.start();


    }



    private void CheckCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},CAM_PERMI_CODE);
        }
        else {
            dispatchTakePictureIntent();
        }
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    Uri photoURI=null;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                System.out.println(photoFile.getAbsolutePath());
                startActivityIfNeeded(takePictureIntent, CAM_OPEN_CODE);
            }
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {
        if(requestCode==CAM_PERMI_CODE){
            if(grantResults.length>0 &&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();

            }
            else {
                Toast.makeText(this, "Camera Permission is Required to use camera", Toast.LENGTH_SHORT).show();
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void ShowMenu() {
        fmain=findViewById(R.id.floating_main);
        fmain.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
        imagepicker=findViewById(R.id.floating_image);
        docpicker=findViewById(R.id.floating_doc);
        filecreater=findViewById(R.id.floating_file);
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

    private void ShowFileAler() {
        final AlertDialog.Builder alerfile=new AlertDialog.Builder(MainActivity.this);
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
                 String FOLDER_PATH=USER_DataBase+"/"+"FOLDERS";
                 databaseReference=FirebaseDatabase.getInstance().getReference().child(FOLDER_PATH);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PHOTO_PICKER && resultCode==RESULT_OK){
            AddtoStorage(data,requestCode,null);

    }
        if(requestCode==DOCUMENT_PICKER && resultCode==RESULT_OK){
            AddtoStorage(data,requestCode,null);

        }
        if(requestCode==CAM_OPEN_CODE && resultCode==RESULT_OK){

            File f= new File(currentPhotoPath);
//            Toast.makeText(this, f.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            Uri contentUri = Uri.fromFile(f);
            try{
                AddtoStorage(null,requestCode,contentUri);
            }catch (Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

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
        if(requestCode==DOCUMENT_PICKER){
            check=true;
        }
        final AlertDialog.Builder alerfile=new AlertDialog.Builder(MainActivity.this);
        View alertview=getLayoutInflater().inflate(R.layout.file_input,null);
        TextView textView=alertview.findViewById(R.id.textView);
        EditText filename=alertview.findViewById(R.id.file_name_input);
        Button btn_cancel=alertview.findViewById(R.id.cancel_file);
        Button btn_create=alertview.findViewById(R.id.cerate_file);
        textView.setText(text_head);
        filename.setHint("File name");
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
                    if (check){
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
     public void  RETRIVEDATA(){
         String FOLDER_PATH_Docu=USER_DataBase+"/"+"DOCUMENT";
         DatabaseReference DOCUMENT_FOLDER=FirebaseDatabase.getInstance().getReference(FOLDER_PATH_Docu);
         String FOLDER_PATH_IMAGE=USER_DataBase+"/"+"IMAGES";
         DatabaseReference IMAGE_FOLDER =FirebaseDatabase.getInstance().getReference(FOLDER_PATH_IMAGE);
         String FOLDER_PATH=USER_DataBase+"/"+"FOLDERS";
         DatabaseReference FILE_FOLDER=FirebaseDatabase.getInstance().getReference(FOLDER_PATH);
         childEventListener = new ChildEventListener() {
             @Override
             public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                 UserData data=null;
                 try{
                      data=snapshot.getValue(UserData.class);
                     if(data.getImage()!=null || data.getFilename()!=null || data.getDocu()!=null){
                         progressBar.setVisibility(View.GONE);
                         list_addapter.add(data);
                     }

                 }catch (Exception e){
                 }



             }

             @Override
             public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                 progressBar.setVisibility(View.GONE);
             }

             @Override
             public void onChildRemoved(DataSnapshot snapshot) {
                 progressBar.setVisibility(View.GONE);
             }

             @Override
             public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                 progressBar.setVisibility(View.GONE);
             }

             @Override
             public void onCancelled(DatabaseError error) {
                 progressBar.setVisibility(View.GONE);
             }
         };
         try {
             FILE_FOLDER.addChildEventListener(childEventListener);
             IMAGE_FOLDER.addChildEventListener(childEventListener);
             DOCUMENT_FOLDER.addChildEventListener(childEventListener);
         }
      catch (Exception e){
      }
         int size=listView.getCount();
     }

        private void Save_fileDatabase(int requestCode,Uri selectedImageUrl,String file_name){
            ProgressBar uprogress=findViewById(R.id.Uploading_progress);
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
                                            String FOLDER_PATH=USER_DataBase+"/"+"IMAGES";
                                            databaseReference=FirebaseDatabase.getInstance().getReference().child(FOLDER_PATH);
                                        }
                                        if(requestCode==DOCUMENT_PICKER){
                                            data.setDocu(src_location);
                                            String FOLDER_PATH=USER_DataBase+"/"+"DOCUMENT";
                                            databaseReference=FirebaseDatabase.getInstance().getReference().child(FOLDER_PATH);
                                        }
                                        data.setLocation(File_location_name);
                                        databaseReference.push().setValue(data);
                                        uprogress.setVisibility(View.GONE);
                                        Toast.makeText(MainActivity.this, "Successfully Uploaded", Toast.LENGTH_SHORT).show();
                                    }
                                    catch (Exception e){
                                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure( Exception e) {
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            });
        }


}