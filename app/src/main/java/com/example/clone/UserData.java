package com.example.clone;

public class UserData {
    String Filename=null;
    String Image=null;
    String Docu=null;
    String Location=null;

    public String getFilename() {
        return Filename;
    }

    public void setFilename(String filename) {
        Filename = filename;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getDocu() {
        return Docu;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public void setDocu(String docu) {
        Docu = docu;
    }
}
