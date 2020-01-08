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
    private Bitmap bitmap, bitmap_clone;
    private Button normalizeRGB,toGray,colorize,keepRedColor;
    private SeekBar seekBar;
    private int[] pixels, pixels_copy;
    private int width, height;


    void increaseContrastRGBHist(int[] pixelSource, int[] pixelDestination, int width, int height){
        int[] CR = new int[256], CG = new int[256], CB = new int[256], histR = new int[256], histG = new int[256], histB = new int[256];
        int size = width * height;
        int red_value = 0, green_value = 0, blue_value = 0;

        for(int i=0; i < width * height; i++){
            red_value = Color.red(pixelSource[i]);
            histR[red_value]++;
            green_value = Color.green(pixelSource[i]);
            histG[green_value]++;
            blue_value = Color.blue(pixelSource[i]);
            histB[blue_value]++;
        }

        CR[0] = histR[0];
        CG[0] = histG[0];
        CB[0] = histB[0];
        for (int k = 1; k < 256; k++){
            for(int i = 1; i <= k; i++){
                CR[k] += histR[i];
                CG[k] += histG[i];
                CB[k] += histB[i];
            }
        }

        for(int i = 0 ; i < width * height; i++){
            red_value = 255 * CR[Color.red(pixelSource[i])] / size;
            green_value = 255 * CG[Color.green(pixelSource[i])] / size;
            blue_value = 255 * CB[Color.blue(pixelSource[i])] / size;
            pixelDestination[i] = Color.argb(Color.alpha(pixelSource[i]), CR[red_value], CG[green_value] , CB[blue_value]);
        }
    }

    void increaseContrastGrayHist(int[] pixelSource, int[] pixelDestination, int width, int height){
        int[] C = new int[256], hist = new int[256];
        int value = 0, size = width * height;

        for(int i=0; i < size; i++){
            value = Color.red(pixelSource[i]);
            hist[value] ++;
        }

        C[0] = hist[0];
        for (int k = 1; k < 256; k++){
            for(int i = 1; i <= k; i++)
                C[k] += hist[i];
        }

        for(int i = 0 ; i < size; i++){
            value = 255 * C[Color.red(pixelSource[i])] / size;
            pixelDestination[i] = Color.argb(Color.alpha(pixelSource[i]), value, value, value);
        }
    }

    void adjustContrastRGB(int[] pixelSource, int[] pixelDestination, int width, int height, int c){
        int[] LUTL = new int[256];
        int brightness_value, brightness_max = -1, brightness_min = 256;
        float[] hsv = new float[3];

        for(int i=0; i < width * height; i++){
            Conversion.rgb2hsl(Color.red(pixelSource[i]), Color.green(pixelSource[i]),Color.blue(pixelSource[i]), hsv);
            brightness_value = (int)(hsv[2] * 255f);
            if (brightness_value < brightness_min)
                brightness_min = brightness_value;
            if (brightness_value > brightness_max)
                brightness_max = brightness_value;
        }

        if( c >= 0 )
            for (int ng = 0; ng <= 255; ng++){
                LUTL[ng] = -c + (255 + 2 *c ) * (ng - brightness_min) / (brightness_max - brightness_min);
                if (LUTL[ng] > 255)
                    LUTL[ng] = 255;
                else if(LUTL[ng] < 0)
                    LUTL[ng] = 0;
            }
        else{
            c *= -1;
            for (int ng = 0; ng < 256; ng++){
                LUTL[ng] = brightness_min + c + ( (255 - brightness_min - c) * (ng -brightness_min)  / (brightness_max - brightness_min));
            }
        }

        int[] rgb;
        for(int i = 0 ; i < width * height; i++){
            Conversion.rgb2hsl(Color.red(pixelSource[i]), Color.green(pixelSource[i]),Color.blue(pixelSource[i]), hsv);
            brightness_value = (int)(hsv[2] * 255f);
            hsv[2] = ((float)(LUTL[brightness_value])) / 255f;
            rgb = Conversion.hsl2rgb(hsv);
            pixelDestination[i] = Color.argb(Color.alpha(pixelSource[i]), rgb[0], rgb[1] , rgb[2]);
        }
    }

    void decreaseContrastGray(int[] pixelSource, int[] pixelDestination, int width, int height){
        int[] LUT = new int[256];
        int value = 0, max = -1, min = 256;

        for(int i=0; i < width * height; i++){
            value = Color.red(pixelSource[i]);
            if (value < min)
                min = value;
            if (value > max)
                max = value;
        }

        for (int ng = 0; ng < 256; ng++){
            LUT[ng] = min + 30 + ( (255 - min - 30) * (ng -min)  / (max - min));
        }

        for(int i = 0 ; i < width * height; i++){
            value = Color.red(pixelSource[i]);
            pixelDestination[i] = Color.argb(Color.alpha(pixelSource[i]), LUT[value], LUT[value] , LUT[value]);
        }
    }

    void increaseContrastGray(int[] pixelSource, int[] pixelDestination, int width, int height){
        int[] LUT = new int[256];
        int value = 0, max = -1, min = 256;

        for(int i=0; i < width * height; i++){
            value = Color.red(pixelSource[i]);
            if (value < min)
                min = value;
            if (value > max)
                max = value;
        }

        for (int ng = 0; ng < 256; ng++){
            LUT[ng] = ( 255 * (ng -min) ) / (max - min);
        }

        for(int i = 0 ; i < width * height; i++){
            value = Color.red(pixelSource[i]);
            pixelDestination[i] = Color.argb(Color.alpha(pixelSource[i]), LUT[value], LUT[value] , LUT[value]);
        }
    }

    void keepRedColor(int[] pixelSource, int[] pixelDestination, int width, int height){
        float[] hsv = new float[3];

        for(int i = 0 ; i < width * height; i++){
            Conversion.rgb2hsl(Color.red(pixelSource[i]), Color.green(pixelSource[i]), Color.blue(pixelSource[i]), hsv);
            if( !(hsv[0]<=7 || hsv[0]>325) ){
                int r = Color.red(pixelSource[i]);
                int g = Color.green(pixelSource[i]);
                int b = Color.blue(pixelSource[i]);
                int gr = (r+g+b)/3;
                pixelDestination[i] = Color.argb(Color.alpha(pixelSource[i]),gr,gr,gr);}
            else
                pixelDestination[i] = Color.argb(Color.alpha(pixelSource[i]),Color.red(pixelSource[i]), Color.green(pixelSource[i]), Color.blue(pixelSource[i]));
        }
    }

    void colorize(int[] pixelSource, int[] pixelDestination, int width, int height){
        float[] hsv = new float[3];
        int[] rgb;
        float random = ((float)Math.random()) * 360f;

        for(int i = 0 ; i < width * height; i++){
            Conversion.rgb2hsl(Color.red(pixelSource[i]), Color.green(pixelSource[i]), Color.blue(pixelSource[i]), hsv);
            hsv[0] = random;
            rgb = Conversion.hsl2rgb(hsv);
            pixelDestination[i] = Color.argb(Color.alpha(pixelSource[i]),rgb[0],rgb[1],rgb[2]);
        }
    }

    void toGrayOptimized(int[] pixelSource, int[] pixelDestination, int width, int height){
        int r = 0,g = 0,b = 0,gr = 0;
        for(int i = 0 ; i < width * height ; i++){
            r = Color.red(pixelSource[i]);
            g = Color.green(pixelSource[i]);
            b = Color.blue(pixelSource[i]);
            gr = (r + g + b) / 3;
            pixelDestination[i] = Color.argb(Color.alpha(pixelSource[i]), gr, gr, gr);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        /***********BUTTONS***********/
        normalizeRGB = (Button) findViewById(R.id.normalizeRGB);
        toGray = (Button) findViewById(R.id.toGray);
        colorize = (Button) findViewById(R.id.colorize);
        keepRedColor = (Button) findViewById(R.id.keepRed);
        previous = (Button) findViewById(R.id.previous3);
        pick = (Button) findViewById(R.id.pickImage);
        /*****************SEEKBAR********************/
        seekBar=(SeekBar)findViewById(R.id.seekBar4);
        /***********IMAGE*VIEWS***********/
        imageView = (ImageView) findViewById(R.id.imageView);
        /***********SETTING*BUTTONS***********/
        pick.setOnClickListener(this);
        previous.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        adjustContrastRGB(pixels , pixels_copy , width, height, seekBar.getProgress() - 100);
                        bitmap_clone.setPixels(pixels_copy, 0, width, 0, 0, width, height);
                        imageView.setImageBitmap(bitmap_clone);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {}
                }
        );
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

            case R.id.normalizeRGB:
                imageView.setImageBitmap(bitmap);
                break;

            case R.id.toGray:
                toGrayOptimized(pixels , pixels_copy , width, height);
                bitmap_clone.setPixels(pixels_copy, 0, width, 0, 0, width, height);
                imageView.setImageBitmap(bitmap_clone);
                break;

            case R.id.colorize:
                colorize(pixels , pixels_copy , width, height);
                bitmap_clone.setPixels(pixels_copy, 0, width, 0, 0, width, height);
                imageView.setImageBitmap(bitmap_clone);
                break;

            case R.id.keepRed:
                keepRedColor(pixels , pixels_copy , width, height);
                bitmap_clone.setPixels(pixels_copy, 0, width, 0, 0, width, height);
                imageView.setImageBitmap(bitmap_clone);
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
                        bitmap_clone = bitmap.copy(bitmap.getConfig(),true);
                        width = bitmap.getWidth();
                        height = bitmap.getHeight();
                        pixels = new int [height * width];
                        pixels_copy = new int[height * width];
                        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                        bitmap.getPixels(pixels_copy, 0, width, 0, 0, width, height);
                        normalizeRGB.setOnClickListener(this);
                        toGray.setOnClickListener(this);
                        colorize.setOnClickListener(this);
                        keepRedColor.setOnClickListener(this);
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