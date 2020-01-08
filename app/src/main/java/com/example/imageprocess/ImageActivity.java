package com.example.imageprocess;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener{
    private Button pick, previous;
    private Intent intent;
    private ImageView imageView;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        previous = (Button) findViewById(R.id.previous3);
        pick = (Button) findViewById(R.id.pickImage);
        imageView = (ImageView) findViewById(R.id.imageView);
        pick.setOnClickListener(this);
        previous.setOnClickListener(this);
    }


    @Override
    public void onClick(View view)
    {
        switch (view.getId()) {
            case R.id.previous3:
                intent = new Intent(ImageActivity.this, TP3Activity.class);
                startActivity(intent);
                break;

            case R.id.pickImage:
                showFilePicker();
                break;

            default :
                break;
        }
    }

    public void showFilePicker(){
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        try{
            startActivityForResult(Intent.createChooser(i,"Select a file to upload"),1000);

        }catch (android.content.ActivityNotFoundException ex) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch(requestCode){
            case 1000:
                if(resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    Log.i("test",uri.toString());
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    }
                    catch(FileNotFoundException e){ }
                    catch(IOException e){ }
                }
                imageView.setImageBitmap(bitmap);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}