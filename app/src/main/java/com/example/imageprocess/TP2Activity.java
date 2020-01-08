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

public class TP2Activity extends AppCompatActivity implements View.OnClickListener {
    private Button next, previous, keepRed;
    private Intent intent;
    private SeekBar seekbar;
    private ImageView iv;
    private Bitmap colored_bitmap, bitmap_clone;
    private int[] pixels, pixels_copy;
    private int width, height;

    void keepColor(int[] pixelSource, int[] pixelDestination, int width, int height, int min, int max){
        float[] hsv = new float[3];

        for(int i = 0 ; i < width * height; i++){
            Conversion.rgb2hsl(Color.red(pixelSource[i]), Color.green(pixelSource[i]), Color.blue(pixelSource[i]), hsv);
            if( !(hsv[0] <= max || hsv[0] >= min) ){
                int r = Color.red(pixelSource[i]);
                int g = Color.green(pixelSource[i]);
                int b = Color.blue(pixelSource[i]);
                int gr = (r+g+b)/3;
                pixelDestination[i] = Color.argb(Color.alpha(pixelSource[i]),gr,gr,gr);
            }
            else
                pixelDestination[i] = Color.argb(Color.alpha(pixelSource[i]),Color.red(pixelSource[i]), Color.green(pixelSource[i]), Color.blue(pixelSource[i]));
        }
    }

    void colorize(int[] pixelSource, int[] pixelDestination, int width, int height, int color){
        float[] hsv = new float[3];
        int[] rgb;

        for(int i = 0 ; i < width * height; i++){
            Conversion.rgb2hsl(Color.red(pixelSource[i]), Color.green(pixelSource[i]), Color.blue(pixelSource[i]), hsv);
            hsv[0] = (float)color;
            rgb = Conversion.hsl2rgb(hsv);
            pixelDestination[i] = Color.argb(Color.alpha(pixelSource[i]),rgb[0],rgb[1],rgb[2]);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tp2);

        next = (Button) findViewById(R.id.next2);
        previous = (Button) findViewById(R.id.previous1);
        keepRed = (Button) findViewById(R.id.keepRed);
        seekbar = (SeekBar) findViewById(R.id.seekBar2);
        iv = (ImageView) findViewById(R.id.colored4);
        colored_bitmap = BitmapFactory.decodeResource(getResources() ,R.drawable.colored4);
        iv.setImageBitmap(colored_bitmap);
        bitmap_clone = colored_bitmap.copy(colored_bitmap.getConfig(),true);
        width = colored_bitmap.getWidth();
        height = colored_bitmap.getHeight();
        pixels = new int [height * width];
        pixels_copy = new int[height * width];
        colored_bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        next.setOnClickListener(this);
        previous.setOnClickListener(this);
        seekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        colorize(pixels , pixels_copy , width, height, seekBar.getProgress() + 20);
                        bitmap_clone.setPixels(pixels_copy, 0, width, 0, 0, width, height);
                        iv.setImageBitmap(bitmap_clone);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {}
                }

        );
        keepRed.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId()){
            case R.id.next2:
                intent = new Intent(TP2Activity.this, TP3Activity.class);
                startActivity(intent);
                break;

            case R.id.previous1:
                intent = new Intent(TP2Activity.this, MainActivity.class);
                startActivity(intent);
                break;

            case R.id.keepRed:
                keepColor(pixels , pixels_copy , width, height,325 , 7);
                bitmap_clone.setPixels(pixels_copy, 0, width, 0, 0, width, height);
                iv.setImageBitmap(bitmap_clone);
                break;

            default:
                break;
        }
    }
}
