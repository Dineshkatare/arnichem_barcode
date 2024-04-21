package com.arnichem.arnichem_barcode.FileUpload;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.File;

public class FileUploadData {
    private RequestBody type;
    private RequestBody doc_number;

    private RequestBody size;
    private RequestBody email;

    private RequestBody username;

    private File file;

    private RequestBody db_host;
    private RequestBody db_username;
    private RequestBody db_password;
    private RequestBody db_name;

    public FileUploadData(RequestBody type, RequestBody doc_number, RequestBody size, RequestBody email, File file, RequestBody db_host, RequestBody db_username, RequestBody db_password, RequestBody db_name,RequestBody username) {
        this.type = type;
        this.doc_number = doc_number;
        this.size = size;
        this.email = email;
        this.file = file;
        this.db_host = db_host;
        this.db_username = db_username;
        this.db_password = db_password;
        this.db_name = db_name;
        this.username =username;
    }

    public RequestBody getUsername() {
        return username;
    }

    public void setUsername(RequestBody username) {
        this.username = username;
    }

    public RequestBody getType() {
        return type;
    }

    public void setType(RequestBody type) {
        this.type = type;
    }

    public RequestBody getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(RequestBody doc_number) {
        this.doc_number = doc_number;
    }

    public RequestBody getSize() {
        return size;
    }

    public void setSize(RequestBody size) {
        this.size = size;
    }

    public RequestBody getEmail() {
        return email;
    }

    public void setEmail(RequestBody email) {
        this.email = email;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public RequestBody getDb_host() {
        return db_host;
    }

    public void setDb_host(RequestBody db_host) {
        this.db_host = db_host;
    }

    public RequestBody getDb_username() {
        return db_username;
    }

    public void setDb_username(RequestBody db_username) {
        this.db_username = db_username;
    }

    public RequestBody getDb_password() {
        return db_password;
    }

    public void setDb_password(RequestBody db_password) {
        this.db_password = db_password;
    }

    public RequestBody getDb_name() {
        return db_name;
    }

    public void setDb_name(RequestBody db_name) {
        this.db_name = db_name;
    }

    public File getFile() {
        return file;
    }

    // Method to create MultipartBody.Part for the file
    public MultipartBody.Part getFilePart() {
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        return MultipartBody.Part.createFormData("file", file.getName(), requestBody);
    }


}
