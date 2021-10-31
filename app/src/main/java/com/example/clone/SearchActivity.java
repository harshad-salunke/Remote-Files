package com.example.clone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.sql.SQLOutput;
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FirebaseStorage firebaseStorage;
    String image_link=null;
    String storagerefrencechild;
    FirebaseAuth firebaseAuth;
    list_addapter addapter;
    OvershootInterpolator overshootInterpolator=new OvershootInterpolator();
    SearchView searchView;
    ArrayList<UserData> data_addapter_array=new ArrayList<>();
    ArrayList<UserData> image_document_array=new ArrayList<>();

    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();
        bottomNavigationView=findViewById(R.id.bottom_nevigation);
        bottomNavigationView.setSelectedItemId(R.id.Msearchbar);
      firebaseStorage=FirebaseStorage.getInstance();
      firebaseAuth=FirebaseAuth.getInstance();
      listView=findViewById(R.id.search_list);


      storagerefrencechild=firebaseAuth.getCurrentUser().getEmail().replace("@gmail.com","");
      bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected( MenuItem item) {
              switch (item.getItemId()){
                  case R.id.Mhome:
                      startActivity(new Intent(getApplicationContext(),MainActivity.class));
                      overridePendingTransition(0,0);
                      finish();
                      return true;
                  case R.id.Msearchbar:
                      return true;
                  case R.id.MclickCamera:
                      startActivity(new Intent(getApplicationContext(),MainActivity.class));
                      overridePendingTransition(0,0);
                      finish();
                      return  true;
                  case R.id.MProfile:
                      startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                      overridePendingTransition(0,0);
                      finish();
                      return true;
              }
              return false;
          }
      });
        GETDATA();
        searchView=findViewById(R.id.search_bar);
        searchView.setVisibility(View.INVISIBLE);
      showSarchbar();



        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                    Search(newText);
                return false;
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserData userData=(UserData)parent.getItemAtPosition(position);

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

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(SearchActivity.this, "", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    private void GETDATA() {
        addapter=new list_addapter(getApplicationContext(),data_addapter_array);
        listView.setAdapter(addapter);
        firebaseStorage.getReference().child(storagerefrencechild).listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for(StorageReference re:listResult.getItems()){
                    String image_name=re.getName();
                    Task<Uri> result=re.getDownloadUrl();

                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            image_link =uri.toString();
                            UserData userData=new UserData();
                            if(image_name.contains(".pdf")){
                                userData.setLocation(image_name);
                                userData.setDocu(image_link);
                            }
                            else {
                                userData.setLocation(image_name);
                                userData.setImage(image_link);
                            }
                            addapter.add(userData);
                         ALLDATALOADED(userData);
                        }

                    });

                }

            }
        });

    }

    private void Search(String newText) {
        ArrayList<UserData> mylist=new ArrayList<>();
        for(UserData userData:image_document_array){

            if(userData.getLocation().toLowerCase().contains(newText.toLowerCase())){
                mylist.add(userData);
            }
        }
        list_addapter list_addapter=new list_addapter(getApplicationContext(),mylist);
        listView.setAdapter(list_addapter);
    }

    private void showSarchbar() {
        searchView=findViewById(R.id.search_bar);
        searchView.setVisibility(View.INVISIBLE);
        searchView.setAlpha(0f);
        searchView.setTranslationY(0f);
        searchView.animate().translationY(60f).alpha(1f).setInterpolator(overshootInterpolator).setDuration(2000).start();
        
        searchView.setVisibility(View.VISIBLE);


    }

    private void ALLDATALOADED(UserData userData) {
        if(userData!=null){
            image_document_array.add(userData);
        }

    }
}