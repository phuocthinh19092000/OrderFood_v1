package com.finaltest.shipper.Common;

import com.finaltest.shipper.Model.Shipper;

public class Common {
    public static final String SHIPPER_TABLE = "Shippers";
    public static final String ORDER_NEED_SHIP_TABLE = "OrdersNeedShip" ;
    public static final int REQUEST_CODE =1000 ;
    public static Shipper currentShipper;

    public static String converCodeToStatus(String code){
        if(code.equals("0"))
            return "Placed";
        else if(code.equals("1"))
            return "On my way";
        else
            return "Shipping";
    }
}
