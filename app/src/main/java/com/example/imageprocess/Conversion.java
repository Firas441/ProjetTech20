package com.example.imageprocess;

public abstract class Conversion {

    public static void rgb2hsl(int red, int green, int blue, float hsv[])
    {
        float r = red /  255f, g = green / 255f, b = blue / 255f;
        float min, max, delta;

        min = r < g ? r : g;
        min = min  < b ? min  : b;

        max = r > g ? r : g;
        max = max  > b ? max  : b;

        delta = max - min;

        //lightness
        hsv[2] = (max + min) / 2;

        //saturation
        if (max < 0.00001 || min > 0.99999)
            hsv[1] = 0f;
        else
            hsv[1] = (delta / (1 - Math.abs(max + min - 1f)));

        //hue
        if (delta == 0f)
            hsv[0] = 0f;
        else if( r == max )// > is bonus, just keeps compiler happy
            hsv[0] = ( g - b ) / delta;
        else if( g == max )
            hsv[0] =(float) 2.0 + ( b - r ) / delta;
        else
            hsv[0] =(float) 4.0 + ( r - g ) / delta;

        hsv[0] *= 60.0;// degrees

        if( hsv[0] < 0f )
            hsv[0] += 360f;
    }


    private static int f(float n, float h, float s, float l){
        float a = s * Math.min(l, 1f - l);
        float k = ( ( n + ( h / 30f ) ) % 12 );
        return  (int) ( ( l - ( a * Math.max( Math.min( Math.min( k - 3 , 9 - k ) , 1f ) , -1f ) ) ) * 255f );
    }

    public static int[] hsl2rgb(float[] hsv){
        int[] out = new int[3];
        out[0] = f(0, hsv[0], hsv[1], hsv[2]);
        out[1] = f(8, hsv[0], hsv[1], hsv[2]);
        out[2] = f(4, hsv[0], hsv[1], hsv[2]);
        return out;
    }
}

