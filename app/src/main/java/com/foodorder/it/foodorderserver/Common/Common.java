package com.foodorder.it.foodorderserver.Common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.foodorder.it.foodorderserver.Model.Request;
import com.foodorder.it.foodorderserver.Model.User;
import com.foodorder.it.foodorderserver.Remote.IGeoCoordinates;
import com.foodorder.it.foodorderserver.Remote.RetrofitClient;

public class Common {

    public static User CurrentUser;
    public static Request CurrentRequest;

    public static String UPDATE= "UPDATE";
    public static String DELETE= "DELETE";

    public static final String baseUrl = "https://maps.googleapis.com";

    public static String convertCodeToStatus (String code)
    {
        if(code.equals("0"))
            return "PLACED";
        else if (code.equals("1"))
            return "ON MY WAY";
        else
            return "SHIPPED";

    }

    public static IGeoCoordinates getGeoCodeService (){
        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap , int newWidth , int newHeight ) {
           Bitmap scaledBitmap = Bitmap.createBitmap(newWidth , newHeight , Bitmap.Config.ARGB_8888);

           float scaleY = newHeight / (float)bitmap.getHeight();
           float scaleX = newWidth /  (float) bitmap.getWidth();
           float pivotX =0 , pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX,scaleY ,pivotX,pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));
        return scaledBitmap;
    }

    public static Boolean isConnectToTheInternet (Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager !=null)
        {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if(info !=null)
            {
                for (int i= 0 ;i<info.length;i++)
                {
                    if(info[i].getState()== NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }
}
