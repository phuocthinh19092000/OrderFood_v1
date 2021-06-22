package com.finaltest;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
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
import com.finaltest.Remote.IGoogleService;
import com.finaltest.ViewHolder.CartAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.midterm.finalexamorderfood.R;
import com.ornach.nobobutton.NoboButton;

import androidx.appcompat.app.AlertDialog;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class Cart extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {



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

    // google maps api
    IGoogleService mGoogleMapService ;

    //Location
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;
    private static  final int LOCATION_REQUEST_CODE = 9999;
    private static  final int PLAY_SERVICES_REQUEST = 9997;


    String address,comment;



    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case LOCATION_REQUEST_CODE:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(checkPlayService()){
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("src/main/assets/font/restaurant.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_cart);


        // Runtime Permission
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )

        {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
            },LOCATION_REQUEST_CODE);


        }
        else {
            if(checkPlayService()){
                buildGoogleApiClient();
                createLocationRequest();
            }

        }





        // init Google api

        String apiKey ="AIzaSyDnI_-f3xUsIV5329gsoPmTrGef3dTTlvQ";

        //String apiKey ="AIzaSyBYJvo8Izc8MyQ-bTvrYGx9NhieSxi-4YY";

        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(),apiKey);
        }

        placesClient = Places.createClient(this);




        //Init Service
        mService =Common.getFCMService();

        mGoogleMapService = Common.getGoogleMapsAPI();


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




                    final EditText editComment = (EditText)order_address_comment.findViewById(R.id.edtComment);
                    final EditText editAddress = (EditText)order_address_comment.findViewById(R.id.edtAddress);
                    // autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.NAME));

                    final AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

                    //Radio Group Get
                    RadioButton rdiShipToCurrentAddress = (RadioButton) order_address_comment.findViewById(R.id.rdiShipToAddress); // ship to address of the device
                    RadioButton rdiShipToHomeAddress = (RadioButton) order_address_comment.findViewById(R.id.rdiShipToHomeAddress); // ship to home address of  user


                    // Event Radio


                    rdiShipToCurrentAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            System.out.println("Latitude : " + mLastLocation.getLatitude());
                            System.out.println("Longtitude : " + mLastLocation.getLongitude());
                            if(isChecked){
                               mGoogleMapService.getAddressName(
                                            String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&key=AIzaSyBa5ESLzC5ihGpAJfGh2X9aFI8dG4ZWIvc",
                                                mLastLocation.getLatitude(),
                                                mLastLocation.getLongitude()))
                                       .enqueue(new Callback<String>() {
                                           @Override
                                           public void onResponse(Call<String> call, Response<String> response) {
                                                //if fetch api is ok
                                               try {
                                                   Log.i("responapi" ,"response : " + response.toString());
                                                   JSONObject jsonObject = new JSONObject(response.body());

                                                   JSONArray resultArray = jsonObject.getJSONArray("results");

                                                   JSONObject firstObject = resultArray.getJSONObject(0);

                                                   address = firstObject.getString("formatted_address");

                                                   //set this address to edtAddress
                                                   editAddress.setText(address);
                                                   autocompleteSupportFragment.setText("");


                                               } catch (JSONException e) {
                                                   e.printStackTrace();
                                               }

                                           }

                                           @Override
                                           public void onFailure(Call<String> call, Throwable t) {
                                                Toast.makeText(Cart.this,"" + t.getMessage(),Toast.LENGTH_SHORT).show();
                                           }
                                       });


                            }

                        }
                    });
                    rdiShipToHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked){

                                if(!Common.currentUser.getHomeAddress().equalsIgnoreCase("")){
                                    address = Common.currentUser.getHomeAddress();
                                    editAddress.setText(address);
                                }
                                else{
                                    Toast.makeText(Cart.this,"Please update your home address",Toast.LENGTH_SHORT).show();
                                }



                            }
                        }
                    });




                    // autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.NAME));

               //  final AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

                    autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG,Place.Field.NAME));
                    //autocompleteSupportFragment.setTypeFilter(TYPE.);
                    autocompleteSupportFragment.setCountry("VN");

                    autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                        @Override
                        public void onPlaceSelected(@NonNull Place place) {
                                shippingAddress = place;
                                editAddress.setText(place.getName() +  "\n"  +  place.getAddress() + "\n" + place.getAddressComponents());
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

                            startActivity(new Intent(Cart.this,Cart.class));
                        }
                    });
                    alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            
                            Request request = new Request();

                            if(editAddress.getText().toString().isEmpty()){
                                Toast.makeText(Cart.this,"Please enter address or select option address",Toast.LENGTH_SHORT).show();

                                // fix crash fragment
                                getFragmentManager().beginTransaction()
                                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                        .commit();
                                return;
                            }
                            else if(!rdiShipToCurrentAddress.isChecked() && !rdiShipToHomeAddress.isChecked()){
                                if(shippingAddress!= null) {
                                    // both radio check not checked
                                    request.setPhone(Common.currentUser.getPhone());
                                    request.setName(Common.currentUser.getName());
                                    request.setAddress(shippingAddress.getName());
                                    request.setTotal(txtTotalPrice.getText().toString());
                                    request.setStatus("0");
                                    request.setComment(editComment.getText().toString());
                                    request.setLatLng(String.format("%s,%s", shippingAddress.getLatLng().latitude, shippingAddress.getLatLng().longitude));
                                    request.setFoods(cart);
                                }
                                else {
                                    Toast.makeText(Cart.this,"Please enter address or select option address",Toast.LENGTH_SHORT).show();

                                    // fix crash fragment
                                    getFragmentManager().beginTransaction()
                                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                            .commit();
                                    return;
                                }

                            }
                            else if (rdiShipToCurrentAddress.isChecked() || rdiShipToHomeAddress.isChecked()) {
                                if(rdiShipToCurrentAddress.isChecked()) {
                                    request.setPhone(Common.currentUser.getPhone());
                                    request.setName(Common.currentUser.getName());
                                    request.setAddress(editAddress.getText().toString());
                                    request.setTotal(txtTotalPrice.getText().toString());
                                    request.setStatus("0");
                                    request.setComment(editComment.getText().toString());
                                    request.setLatLng(String.format("%s,%s", mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                                    request.setFoods(cart);
                                }
                                else if(rdiShipToHomeAddress.isChecked()){
                                    Log.i("HomeAddress :" , "API HomeAddress : " + Common.getLatitude() + Common.getLongitude());
                                    request.setPhone(Common.currentUser.getPhone());
                                    request.setName(Common.currentUser.getName());
                                    request.setAddress(editAddress.getText().toString());
                                    request.setTotal(txtTotalPrice.getText().toString());
                                    request.setStatus("0");
                                    request.setComment(editComment.getText().toString());
                                    request.setLatLng(String.format("%s,%s", Common.getLatitude(), Common.getLongitude()));
                                    request.setFoods(cart);
                                }


                            }

                            // Add to Firebase
                            String order_number = String.valueOf(System.currentTimeMillis());
                            requests.child(order_number)
                                    .setValue(request);

                            //Delete Cart


                            new Database(getBaseContext()).cleanCart();
                            // token
                            sendNotificationOrder(order_number);
                            Toast.makeText(Cart.this, "Thank you, Order Cart Place", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Cart.this, Home.class);
                            startActivity(intent);

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

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private synchronized void buildGoogleApiClient() { 
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }

    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS) {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_REQUEST).show();
            }
            else{
                Toast.makeText(this,"This device is not supported",Toast.LENGTH_SHORT).show();
                finish();

            }
            return false;

        }
        return true;
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        starLocationUpdates();


    }

    private void starLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this::onLocationChanged);
        

    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null){
            Log.d("Location","Your location" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
        }
        else {
            Log.d("Location","Count not get your location");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
        displayLocation();
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