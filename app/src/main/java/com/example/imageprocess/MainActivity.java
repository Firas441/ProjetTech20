package com.example.imageprocess;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button normalizeRGB,toGray,colorize,keepRedColor,adjustContrastRGB, next;
    private Intent intent;
    private ImageView colored_iv;
    private Bitmap colored_bitmap, bitmap_clone;
    private SeekBar seekBar;
    private int[] pixels, pixels_copy;
    private int width, height;


    /*private void toColorRS (Bitmap bmp) {
        // 1) Creer un contexte RenderScript
        RenderScript rs = RenderScript.create( this );
        // 2) Creer des Allocations pour passer les donnees
        Allocation input = Allocation.createFromBitmap ( rs , bmp ) ;
        Allocation output = Allocation.createTyped ( rs , input.getType () ) ;
        // 3) Creer le script
        ScriptC_color colorScript = new ScriptC_color(rs);
        // 4) Copier les donnees dans les Allocations
        // ...
        // 5) Initialiser les variables globales potentielles
        // ...
        // 6) Lancer le noyau
        colorScript.forEach_toColor( input , output ) ;
        // 7) Recuperer les donnees des Allocation (s)
        output . copyTo ( bmp ) ;
        // 8) Detruire le context , les Allocation (s) et le script
        input . destroy () ; output . destroy () ;
        colorScript . destroy () ; rs . destroy () ;
    }

    private void toGrayRS (Bitmap bmp) {
        // 1) Creer un contexte RenderScript
        RenderScript rs = RenderScript.create( this );
        // 2) Creer des Allocations pour passer les donnees
        Allocation input = Allocation.createFromBitmap ( rs , bmp ) ;
        Allocation output = Allocation.createTyped ( rs , input.getType () ) ;
        // 3) Creer le script
        ScriptC_gray grayScript = new ScriptC_gray(rs);
        // 4) Copier les donnees dans les Allocations
        // ...
        // 5) Initialiser les variables globales potentielles
        // ...
        // 6) Lancer le noyau
        grayScript.forEach_toGray( input , output ) ;
        // 7) Recuperer les donnees des Allocation (s)
        output . copyTo ( bmp ) ;
        // 8) Detruire le context , les Allocation (s) et le script
        input . destroy () ; output . destroy () ;
        grayScript . destroy () ; rs . destroy () ;
    }*/


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
        setContentView(R.layout.activity_main);
        /***********BUTTONS***********/
        normalizeRGB = (Button) findViewById(R.id.normalizeRGB);
        toGray = (Button) findViewById(R.id.toGray);
        colorize = (Button) findViewById(R.id.colorize2);
        keepRedColor = (Button) findViewById(R.id.keepRedColor);
        adjustContrastRGB = (Button) findViewById(R.id.adjustContrastRGB);
        next = (Button) findViewById(R.id.next1);
        /*****************SEEKBAR********************/
        seekBar=(SeekBar)findViewById(R.id.seekBar);
        /***********IMAGE*VIEWS***********/
        colored_iv = (ImageView) findViewById(R.id.colored_iv);
        /***********BITMAPS***********/
        colored_bitmap = BitmapFactory.decodeResource(getResources() ,R.drawable.colored);
        bitmap_clone = colored_bitmap.copy(colored_bitmap.getConfig(),true);
        width = colored_bitmap.getWidth();
        height = colored_bitmap.getHeight();
        pixels = new int [height * width];
        pixels_copy = new int[height * width];
        colored_bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        /***********INITIALIZING*IMAGES***********/
        colored_iv.setImageBitmap(colored_bitmap);
        /***********SETTING*BUTTONS***********/
        normalizeRGB.setOnClickListener(this);
        toGray.setOnClickListener(this);
        colorize.setOnClickListener(this);
        keepRedColor.setOnClickListener(this);
        adjustContrastRGB.setOnClickListener(this);
        next.setOnClickListener(this);
    }


    @Override
    public void onClick(View view)
    {
        switch (view.getId()) {
            case R.id.normalizeRGB:
                colored_iv.setImageBitmap(colored_bitmap);
                break;

            case R.id.toGray:
                toGrayOptimized(pixels , pixels_copy , width, height);
                bitmap_clone.setPixels(pixels_copy, 0, width, 0, 0, width, height);
                colored_iv.setImageBitmap(bitmap_clone);
                break;

            case R.id.colorize2:
                colorize(pixels , pixels_copy , width, height);
                bitmap_clone.setPixels(pixels_copy, 0, width, 0, 0, width, height);
                colored_iv.setImageBitmap(bitmap_clone);
                break;

            case R.id.keepRedColor:
                keepRedColor(pixels , pixels_copy , width, height);
                bitmap_clone.setPixels(pixels_copy, 0, width, 0, 0, width, height);
                colored_iv.setImageBitmap(bitmap_clone);
                break;

            case R.id.adjustContrastRGB:
                seekBar.setOnSeekBarChangeListener(
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
                break;

            case R.id.next1:
                intent = new Intent(MainActivity.this,TP2Activity.class);
                startActivity(intent);
                break;

            default :
                break;
        }
    }

}