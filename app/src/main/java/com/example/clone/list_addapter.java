package com.example.clone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class list_addapter extends ArrayAdapter<UserData> {
    public list_addapter(Context context, ArrayList<UserData> object) {
        super(context, 0, object);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view, parent, false);
        }
        UserData data=getItem(position);
        TextView mesageTextView=convertView.findViewById(R.id.list_text);
        ImageView imageView=convertView.findViewById(R.id.list_image);
        imageView.setImageResource(R.drawable.ic_list_folder);
    if(data.getImage()!=null){
        String location=data.getLocation();
        if(location.length()>=24){
            location=location.substring(0,24);
        }
            mesageTextView.setText(location);

            Glide.with(imageView.getContext())
                    .load(data.getImage())
                    .into(imageView);
        }
        if(data.getDocu()!=null){
            String location=data.getLocation();
            if(location.length()>=24){
                location=location.substring(0,24);
            }

            mesageTextView.setText(location);
            imageView.setImageResource(R.drawable.ic_list_pdf_24);
        }
        if(data.getFilename()!=null){
            mesageTextView.setText(data.getFilename());
        }

        return convertView;
    }
}

