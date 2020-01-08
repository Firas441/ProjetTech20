package com.example.imageprocess;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

public class TP3Activity extends AppCompatActivity implements View.OnClickListener {
    private Button next, previous;
    private Intent intent;
    private Button normalizeGray, normalizeRGB,hist , increaseContrastGrey, decreaseContrastGrey;
    private ImageView colored_iv, gray_iv;
    private Bitmap gray_bitmap, colored_bitmap, bitmap_clone, bitmap_clone2;
    private int[] pixels, pixels_copy, pixels2, pixels_copy2;
    private int width, height, width2, height2;
    private SeekBar adjustContrast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tp3);

        next = (Button) findViewById(R.id.next3);
        previous = (Button) findViewById(R.id.previous2);
        adjustContrast = (SeekBar) findViewById(R.id.seekBar3);
        /***********BUTTONS***********/
        normalizeRGB = (Button) findViewById(R.id.normalizeRGB);
        normalizeGray = (Button) findViewById(R.id.normalizeGray);
        hist = (Button) findViewById(R.id.hist);
        increaseContrastGrey = (Button) findViewById(R.id.increaseContrastGray);
        decreaseContrastGrey = (Button) findViewById(R.id.decreaseContrastGray);
        /***********IMAGE*VIEWS***********/
        colored_iv = (ImageView) findViewById(R.id.colored);
        gray_iv = (ImageView) findViewById(R.id.gray_iv);
        /***********BITMAPS***********/
        colored_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.colored);
        gray_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gray);
        bitmap_clone = colored_bitmap.copy(colored_bitmap.getConfig(),true);
        bitmap_clone2 = gray_bitmap.copy(gray_bitmap.getConfig(), true);
        width = colored_bitmap.getWidth();
        height = colored_bitmap.getHeight();
        pixels = new int [height * width];
        pixels_copy = new int[height * width];
        width2 = gray_bitmap.getWidth();
        height2 = gray_bitmap.getHeight();
        pixels2 = new int [height2 * width2];
        pixels_copy2 = new int[height2 * width2];
        colored_bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        gray_bitmap.getPixels(pixels2, 0, width2, 0, 0, width2, height2);
        /***********INITIALIZING*IMAGES***********/
        colored_iv.setImageBitmap(colored_bitmap);
        gray_iv.setImageBitmap(gray_bitmap);
        /***********SETTING*BUTTONS***********/
        next.setOnClickListener(this);
        previous.setOnClickListener(this);
        hist.setOnClickListener(this);
        normalizeRGB.setOnClickListener(this);
        normalizeGray.setOnClickListener(this);
        increaseContrastGrey.setOnClickListener(this);
        decreaseContrastGrey.setOnClickListener(this);

        adjustContrast.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        adjustContrastRGB(pixels , pixels_copy , width, height, seekBar.getProgress() - 100);
                        bitmap_clone.setPixels(pixels_copy, 0, width, 0, 0, width, height);
                        colored_iv.setImageBitmap(bitmap_clone);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.normalizeRGB:
                colored_iv.setImageBitmap(colored_bitmap);
                break;

            case R.id.normalizeGray:
                gray_iv.setImageBitmap(gray_bitmap);
                break;

            case R.id.hist:
                increaseContrastGrayHist(pixels2 , pixels_copy2 , width2, height2);
                bitmap_clone2.setPixels(pixels_copy2, 0, width2, 0, 0, width2, height2);
                gray_iv.setImageBitmap(bitmap_clone2);
                break;

            case R.id.increaseContrastGray:
                increaseContrastGray(pixels2 , pixels_copy2 , width2, height2);
                bitmap_clone2.setPixels(pixels_copy2, 0, width2, 0, 0, width2, height2);
                gray_iv.setImageBitmap(bitmap_clone2);
                break;

            case R.id.decreaseContrastGray:
                decreaseContrastGray(pixels2 , pixels_copy2 , width2, height2);
                bitmap_clone2.setPixels(pixels_copy2, 0, width2, 0, 0, width2, height2);
                gray_iv.setImageBitmap(bitmap_clone2);
                break;

            case R.id.next3:
                intent = new Intent(TP3Activity.this, ImageActivity.class);
                startActivity(intent);
                break;

            case R.id.previous2:
                intent = new Intent(TP3Activity.this, TP2Activity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }

}