package com.example.catalyst.androidtodo.models;

public class ImageUpload {

    private String fileName;
    private byte[] newImage;

    public byte[] getNewImage() {
        return newImage;
    }

    public void setNewImage(byte[] newImage) {
        this.newImage = newImage;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


}
