package com.finaltest.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.finaltest.Model.Order;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

public class Database extends  SQLiteAssetHelper {
    private static final String DB_NAME = "OrderFood_DB_1.db";
    private static final int DB_VER = 3;
    private Context context;
    public Database(Context context) {
        super(context, DB_NAME, null, DB_VER);
        this.context = context;
    }
    public List<Order> getCarts(){
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"ID","ProductName", "ProductId", "Quantity", "Price", "Discount","Image"};
        String sqlTable = "OrderDetail";

        qb.setTables(sqlTable);
        Cursor c = qb.query(db ,sqlSelect,null, null, null, null ,null);

        final List<Order> result = new ArrayList<>();
        if (c.moveToFirst()){
            do{
                result.add( new Order(
                        c.getInt(c.getColumnIndex("ID")),
                        c.getString(c.getColumnIndex("ProductId")),
                        c.getString(c.getColumnIndex("ProductName")),
                        c.getString(c.getColumnIndex("Quantity")),
                        c.getString(c.getColumnIndex("Price")),
                        c.getString(c.getColumnIndex("Discount")),
                        c.getString(c.getColumnIndex("Image"))
                           ));
            } while (c.moveToNext());
        }
        return result; 
    }
    public void addToCart(Order order){

        SQLiteDatabase db = getReadableDatabase();
        /*
        if(checkFoodIdExist(order) == true){

            String query = String.format("UPDATE OrderDetail SET Quantity = '%s' WHERE ProductId='%s' ;",
                    order.getQuantity(),
                    order.getProductId());
            db.execSQL(query);
        }
        else {
                    String query = String.format("INSERT INTO OrderDetail(ProductId,ProductName,Quantity,Price,Discount) VALUES('%s','%s','%s','%s','%s');",
                            order.getProductId(),
                            order.getProductName(),
                            order.getQuantity(),
                            order.getPrice(),
                            order.getDiscount());
                    db.execSQL(query);

        }
        db.close();
        */

    //    boolean check =checkFoodIdExist(order);
     //   System.out.println("True123 :" + check);
        System.out.println("FoodId Add " + order.getProductId() + " abc" + order.getID());

        String query = String.format("INSERT INTO OrderDetail(ProductId,ProductName,Quantity,Price,Discount,Image) VALUES ('%s','%s','%s','%s','%s','%s')",
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getPrice(),
                order.getDiscount(),
                order.getImage());
        db.execSQL(query);
    }

    public boolean checkFoodIdExist(Order order){
        SQLiteDatabase db = getReadableDatabase();
        String query= String.format("SELECT * FROM OrderDetail");
        Cursor c = db.rawQuery(query,null);
        System.out.println("c count " + c.getCount());
        List<String> checkIDFood = new ArrayList<>();
        if (c.moveToFirst()){
            do {
               checkIDFood.add(c.getString(c.getColumnIndex("ProductId")));
            } while(c.moveToNext());
        }
        System.out.println("FoodID Size :" + checkIDFood.size());

        for (String foodId : checkIDFood){
            System.out.println("check123  " + foodId);
            if(foodId.equalsIgnoreCase(order.getProductId())){
                return true;

            }

        }
        return false;
    }

    public void cleanCart(){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail");
        db.execSQL(query);
    }

    //Favorites Update
    public void addToFavorites(String foodId){
        SQLiteDatabase db = getReadableDatabase();
        String query= String.format("INSERT INTO Favorites(FoodId) VALUES('%s');",foodId);
        db.execSQL(query);
    }

    public void removeFromFavorites(String foodId){
        SQLiteDatabase db = getReadableDatabase();
        String query= String.format("DELETE From Favorites WHERE FoodId='%s';",foodId);
        db.execSQL(query);
    }

    public boolean isFavorite(String foodId){

        SQLiteDatabase db = getReadableDatabase();
        String query= String.format("SELECT * FROM Favorites WHERE FoodId='%s';",foodId);
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.getCount() <= 0 ){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public int getCountCart() {
        int count=0;
        SQLiteDatabase db = getReadableDatabase();
        String query= String.format("SELECT COUNT(*) FROM OrderDetail");
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.moveToFirst() ){
           do{
               count = cursor.getInt(0);
           }
           while (cursor.moveToNext());
        }
        return  count;

    }

    public void updateCart(Order order) {

        SQLiteDatabase db = getReadableDatabase();
        String query= String.format("UPDATE OrderDetail SET Quantity = %s WHERE ID=%d",order.getQuantity(),order.getID());
        db.execSQL(query);

    }
}
