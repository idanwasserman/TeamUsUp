package com.idan.teamusup.services;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;

import java.io.ByteArrayOutputStream;
import java.util.Date;

public class MyCamera {
    private final static String TAG = "TAG_MyCamera";

    private static MyCamera instance;
    private CallBack_PhotoUrl callBack_photoUrl;
    private String title;

    public static MyCamera getInstance() {
        return instance;
    }
    public static void init() {
        if (instance == null) {
            instance = new MyCamera();
        }
    }

    public void openCamera(
            String title,
            final ActivityResultLauncher<Intent> activityResultLauncher,
            CallBack_PhotoUrl callBack_photoUrl) {
        this.callBack_photoUrl = callBack_photoUrl;
        this.title = title;
        activityResultLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
    }

    public void onCameraResult(Intent data) {
        if (data == null) return;
        Bitmap photo = (Bitmap) data.getExtras().get("data");
        // Set the image in imageview for display
        this.callBack_photoUrl.setPhotoUrl(photo);
    }

    // FIXME check permissions
    public String getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(
                inContext.getContentResolver(), inImage, this.title, new Date().toString());
        if (path == null) {
            Toast.makeText(inContext, "Path is null", Toast.LENGTH_SHORT).show();
            return "";
        }
        return Uri.parse(path).toString();
    }

    public interface CallBack_PhotoUrl {
        void setPhotoUrl(Bitmap photo);
    }

}
