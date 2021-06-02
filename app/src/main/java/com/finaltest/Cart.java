package com.finaltest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finaltest.Common.Common;
import com.finaltest.Database.Database;
import com.finaltest.Model.MyResponse;
import com.finaltest.Model.Notification;
import com.finaltest.Model.Order;
import com.finaltest.Model.Request;
import com.finaltest.Model.Sender;
import com.finaltest.Model.Token;
import com.finaltest.Remote.APIService;
import com.finaltest.ViewHolder.CartAdapter;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.util.JsonUtils;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.midterm.finalexamorderfood.R;
import com.ornach.nobobutton.NoboButton;

import androidx.appcompat.app.AlertDialog;


import org.w3c.dom.ls.LSOutput;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Cart extends AppCompatActivity {

    private static  final int PAYPAL_REQUEST_CODE =9999;

    private static String TAG = Cart.class.getSimpleName();

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView txtTotalPrice;
    NoboButton btnPlace;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    APIService mService ;

    Place shippingAddress;

    PlacesClient placesClient;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("src/main/assets/font/restaurant.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_cart);

        // init Google api

        String apiKey ="AIzaSyDnI_-f3xUsIV5329gsoPmTrGef3dTTlvQ";

        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(),apiKey);
        }

        placesClient = Places.createClient(this);




        //Init Service
        mService =Common.getFCMService();


        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = (RecyclerView) findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice = (TextView) findViewById(R.id.total_price);
        btnPlace = (NoboButton)findViewById(R.id.btnPlaceOrder);
        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cart.size()>0){





                    //User user = Common.currentUser;
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
                    alertDialog.setTitle("One more step!");
                    alertDialog.setMessage("Enter your address: ");


//                    final EditText editAddr = new EditText(Cart.this);
//                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                            LinearLayout.LayoutParams.MATCH_PARENT,
//                            LinearLayout.LayoutParams.MATCH_PARENT
//                    );
//                    editAddr.setLayoutParams(lp);
//                    alertDialog.setView(editAddr); // Add editText  to alert dialog
                    LayoutInflater inflater = Cart.this.getLayoutInflater();
                    View order_address_comment = inflater.inflate(R.layout.order_address_comment,null);

                    //ấnndsadds


                    final EditText editComment = (EditText)order_address_comment.findViewById(R.id.edtComment);
                    final AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

                    autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG,Place.Field.NAME));
                    autocompleteSupportFragment.setTypeFilter(TypeFilter.ADDRESS);
                    autocompleteSupportFragment.setCountry("VN");

                   // autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.NAME));

                    autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                        @Override
                        public void onPlaceSelected(@NonNull Place place) {
                                shippingAddress = place;
                                Log.i("Place API" ,"OnSelected : " + shippingAddress.getLatLng().latitude + "\n" + shippingAddress.getLatLng().longitude + "\n" + place.getName());
                        }

                        @Override
                        public void onError(@NonNull Status status) {

                            Log.i(TAG, "An error occurred: " + status);
                        }
                    });




                    alertDialog.setView(order_address_comment);
                    alertDialog.setIcon(R.drawable.ic_baseline_shopping_cart_24);
                    alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();


                            //remove fragment
//                            getFragmentManager().beginTransaction()
//                                    .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
//                                    .commit();
                        }
                    });
                    alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {



                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            System.out.println("Yes Choice");



                            // Create new Request
                            Request request = new Request(
                                    Common.currentUser.getPhone(),
                                    Common.currentUser.getName(),
                                    shippingAddress.getAddress(),
                                    txtTotalPrice.getText().toString(),
                                    "0",// status
                                    editComment.getText().toString(),
                                    String.format("%s,%s",shippingAddress.getLatLng().latitude,shippingAddress.getLatLng().longitude),
                                    cart);
                            // Add to Firebase
                            String order_number = String.valueOf(System.currentTimeMillis());
                            requests.child(order_number)
                                    .setValue(request);

                            //Delete Cart

                            // token
                            new Database(getBaseContext()).cleanCart();

                            sendNotificationOrder(order_number);
                       //     Toast.makeText(Cart.this, "Thank you, Order Cart Place", Toast.LENGTH_SHORT).show();
                            //    Toast.makeText(Cart.this, "Thank you, Order Cart Place", Toast.LENGTH_SHORT).show();
                        //    finish();


                            //Common.currentUser = user;
                        }



                    });

                    alertDialog.show();
                }

                else {

                    Toast.makeText(Cart.this, "Your Cart is empty , please add some food to order", Toast.LENGTH_SHORT).show();
                }


            }

            private void sendNotificationOrder(final String order_number) {

                System.out.println("In Send Notification Order");
                DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
                Query data = tokens.orderByChild("isServerToken").equalTo(true);
                data.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot postSnapshot : snapshot.getChildren())
                        {
                            Token serverToken = postSnapshot.getValue(Token.class);

                            // create raw payload to send
                            Notification notification = new Notification("Order Food App " , "You have new order "+ order_number  );
                            Sender content = new Sender(serverToken.getToken(),notification);
                            System.out.println("Server Token " + serverToken.getToken());
                            mService.sendNotification(content)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success == 1){


                                              //  System.out.println("Thank you, Order Cart Place");
                                                Toast.makeText(Cart.this, "Thank you, Order Cart Place", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                            else{
                                                Toast.makeText(Cart.this, "Failed !!!", Toast.LENGTH_SHORT).show();

                                            }


                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            System.out.println("Failed Order");
                                            Log.e("ERROR",t.getMessage());
                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



            }


        });


        ///Load
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart, this);
        recyclerView.setAdapter(adapter);

        //Caculate total price
        int total = 0;

        for (Order order: cart)
            total += (Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));




    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals((Common.DELETE)))
            deleteCart(item.getOrder());

        return true;
    }

    private void deleteCart(int position) {
        // remove item at List Order by position
        cart.remove(position);
        // delete all old data from SQL lite
        new Database(this).cleanCart();
        // update new data from list order to sql lite
        for(Order item:cart){
            new Database(this).addToCart(item);
        }

        //refresh
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart, this );
        recyclerView.setAdapter(adapter);

        //Caculate total price
        int total = 0;

        for (Order order: cart)
            total += (Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//
//
//        if (requestCode == PAYPAL_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
//                if (confirmation != null) {
//                    try {
//                        String paymentDetail = confirmation.toJSONObject().toString(4);
//
//                        JSONObject jsonObject = new JSONObject(paymentDetail);
//
//
//                        // Create new Request
//                        Request request = new Request(
//                                Common.currentUser.getPhone(),
//                                Common.currentUser.getName(),
//                                address,
//                                txtTotalPrice.getText().toString(),
//                                "0",// status
//                                comment,
//                                jsonObject.getJSONObject("response").getString("state"),
//                                cart); // state from json object
//                        // Add to Firebase
//                        String order_number = String.valueOf(System.currentTimeMillis());
//                        requests.child(order_number)
//                                .setValue(request);
//
//                        //Delete Cart
//                        new Database(getBaseContext()).cleanCart();
//
//                        sendNotificationOrder(order_number);
//                          Toast.makeText(Cart.this, "Thank you, Order Cart Place", Toast.LENGTH_SHORT).show();
//                       finish();
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//
//            }
//            else if (resultCode == RESULT_CANCELED){
//                Toast.makeText(this,"Payment Cancel",Toast.LENGTH_SHORT).show();
//            }
//            else if(resultCode == PaymentActivity.RESULT_EXTRAS_INVALID){
//                Toast.makeText(this,"Invalid Payment",Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }


}