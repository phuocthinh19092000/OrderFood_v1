package com.finaltest.Common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.finaltest.Model.Request;
import com.finaltest.Model.User;
import com.finaltest.Remote.FCMRetrofitClient;
import com.finaltest.Remote.IGeocoordinates;
import com.finaltest.Remote.RetrofitClient;
import com.finaltest.Service.APIService;

public class Common {
    public static final String SHIPPERS_TABLE = "Shippers" ;
    public static final String ORDER_NEED_SHIP_TABLE = "OrdersNeedShip" ;
    public static User currentUser;
    public static Request currentRequest;
    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";
    public static final int PICK_IMAGE_REQUEST = 71;
    public static final String baseUrl = "https://maps.googleapis.com";
    public static final String fcmUrl = "https://fcm.googleapis.com/";
    public static String PHONE_TEXT = "userPhone";


    public static String converCodeToStatus(String code){
        if(code.equals("0"))
            return "Placed";
        else if(code.equals("1"))
            return "On my way";
        else
            return "Shipping";
    }
    public static APIService getFCMClient(){
        return FCMRetrofitClient.getClient(fcmUrl).create(APIService.class);
    }
    public static IGeocoordinates getGeoCodeService(){
        return RetrofitClient.getClient(baseUrl).create(IGeocoordinates.class);
    }
    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight){
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        float scaleX = newWidth/(float)bitmap.getWidth();
        float scaleY = newHeight/(float)bitmap.getHeight();
        float pivotX=0;
        float pivotY=0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        return scaledBitmap;
    }
}
