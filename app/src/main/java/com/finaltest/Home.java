package com.finaltest;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.andremion.counterfab.CounterFab;
import com.finaltest.Common.Common;
import com.finaltest.Database.Database;
import com.finaltest.Interface.ItemClickListener;
import com.finaltest.Model.Categories;
import com.finaltest.Model.Token;
import com.finaltest.Remote.IGoogleService;
import com.finaltest.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.midterm.finalexamorderfood.R;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    private AppBarConfiguration mAppBarConfiguration;
    FirebaseDatabase database;
    DatabaseReference category;

//    Query query;
    TextView txtFullName;
    RecyclerView recyclerMenu;
    LinearLayoutManager layoutManager;
    FirebaseRecyclerAdapter adapter;
    CounterFab fab;

    // google maps api
    IGoogleService mGoogleMapService ;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        View v = binding.getRoot();
        setContentView(v);*/
        super.onCreate(savedInstanceState);





        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("src/main/assets/font/restaurant.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_home);

        mGoogleMapService = Common.getGoogleMapsAPI();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = (CounterFab)findViewById(R.id.fab);
        database = FirebaseDatabase.getInstance();
        category = database.getReference().child("Categories");




        Paper.init(this);
//        query = FirebaseDatabase.getInstance().getReference().child("Categories");


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent(Home.this, Cart.class);
                //Common.currentUser = Common.currentUser;
                startActivity(cartIntent);
            }
        });

        //set count for fab counter button

        fab.setCount(new Database(this).getCountCart());


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
       navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        txtFullName = (TextView)headerView.findViewById(R.id.txtFullName);
//        txtFullName.setText(Common.currentUser.getName());


        recyclerMenu = (RecyclerView)findViewById(R.id.recycler_menu);
        recyclerMenu.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerMenu.setLayoutManager(new GridLayoutManager(this,2));

        if (Common.isConnectedToInternet(getBaseContext())) {
            //Load Menu

            FirebaseRecyclerOptions<Categories> options = new FirebaseRecyclerOptions.Builder<Categories>().setQuery(category, Categories.class).build();
        /*adapter1 = new MyAdapter(options);
        recyclerMenu.setAdapter(adapter1);*/
            adapter = new FirebaseRecyclerAdapter<Categories, MenuViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull MenuViewHolder holder, int position, @NonNull Categories model) {
                    holder.txtMenuName.setText(model.getName());
                    Picasso.get().load(model.getImage()).into(holder.imageView);
                    Categories clickItem = model;
                    holder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onClick(View view, int position, boolean isLongClick) {
                            Intent foodList = new Intent(Home.this, FoodList.class);
                            foodList.putExtra("CategoryId", adapter.getRef(position).getKey());
                            startActivity(foodList);

                        }
                    });
                }

                @NonNull
                @Override
                public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);

                    return new MenuViewHolder(view);
                }
            };
            recyclerMenu.setAdapter(adapter);


        }
        else{
            Toast.makeText(Home.this,"Please check your internet connection !!",Toast.LENGTH_SHORT).show();
            return ;
        }


        updateToken(FirebaseInstanceId.getInstance().getToken());


        



    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        fab.setCount(new Database(this).getCountCart());

        // fix click back button from Food and don't see category

        if(adapter!=null){
            adapter.startListening();
        }

    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token,false);
        tokens.child(Common.currentUser.getPhone()).setValue(data);

    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.menu_search){
            startActivity(new Intent(Home.this,SearchActivity.class));
        }


        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id =item.getItemId();

        if(id == R.id.nav_menu){

        }
        else if (id == R.id.nav_cart){
            Intent cartIntent = new Intent(Home.this, Cart.class);
            startActivity(cartIntent);
        }
        else if (id == R.id.nav_orders){
            Intent orderIntent = new Intent(Home.this, OrderStatus.class);
            startActivity(orderIntent);

        }
        else if (id == R.id.nav_log_out){
            // delete remember
            Paper.book().destroy();


            // Logout
            Intent signIn = new Intent(Home.this, SignIn.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);
        }
        else if (id == R.id.nav_change_pwd){

            //System.out.println("1231213123");
            //Toast.makeText(Home.this,"ssaasdsa",Toast.LENGTH_SHORT);
            //showChangePasswordDialog();


            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
            alertDialog.setTitle("CHANGE PASSWORD");
            alertDialog.setMessage("Please fill all information");


            LayoutInflater inflater = Home.this.getLayoutInflater();
            View layout_pwd = inflater.inflate(R.layout.change_password_layout,null);

            MaterialEditText edtCurrPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtCurrPassword);
            MaterialEditText edtNewPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtNewPassword);
            MaterialEditText edtConfirmPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtRepeatPassword);

            alertDialog.setView(layout_pwd);
            alertDialog.setIcon(R.drawable.ic_baseline_password_24);

            //Button

            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();
                }
            });
            alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    //change password

                    android.app.AlertDialog waitDialog = new SpotsDialog(Home.this);
                    waitDialog.show();

                    // check old password
                    if(edtCurrPassword.getText().toString().equals(Common.currentUser.getPassword())){
                        // check new password with confirm pass

                        if(edtNewPassword.getText().toString().equals(edtConfirmPassword.getText().toString()))
                        {
                            Map<String,Object> passwordUpdate = new HashMap<String,Object>();
                            passwordUpdate.put("password",edtNewPassword.getText().toString());

                            //make Update

                            DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                            user.child(Common.currentUser.getPhone())
                                    .updateChildren(passwordUpdate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            waitDialog.dismiss();
                                            System.out.println("Pass was update");
                                            Toast.makeText(Home.this,"Pass was update",Toast.LENGTH_SHORT);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            waitDialog.dismiss();
                                            System.out.println(e.getMessage());
                                            Toast.makeText(Home.this,e.getMessage(),Toast.LENGTH_SHORT);
                                        }
                                    });

                        }
                        else{
                            waitDialog.dismiss();
                            System.out.println("New Password Doesn't Match");
                            Toast.makeText(Home.this,"New Password Doesn't Match",Toast.LENGTH_SHORT);
                        }
                    }
                    else{
                        waitDialog.dismiss();
                        System.out.println("Wrong Old Password");
                        Toast.makeText(Home.this,"Wrong Old Password",Toast.LENGTH_SHORT);
                    }
                }
            });

            alertDialog.show();

        }
        else if (id == R.id.nav_home_address){
            showHomeAddressDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    private void showHomeAddressDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Change Home Address");
        alertDialog.setMessage("Please fill all information");


        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_homeAddress = inflater.inflate(R.layout.home_address_layout, null);

        final MaterialEditText edtHomeAddress = (MaterialEditText) layout_homeAddress.findViewById(R.id.edtHomeAddress);

        alertDialog.setView(layout_homeAddress);

        if(!Common.currentUser.getHomeAddress().equalsIgnoreCase("")){
            edtHomeAddress.setText(Common.currentUser.getHomeAddress());
        }


        //Button
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // set new Home Address
                Common.currentUser.setHomeAddress(edtHomeAddress.getText().toString());

                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                String apiKey = "AIzaSyBa5ESLzC5ihGpAJfGh2X9aFI8dG4ZWIvc";

                                    mGoogleMapService.getAddressName(
                                            String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
                                                    edtHomeAddress.getText().toString(),
                                                    apiKey))
                                            .enqueue(new Callback<String>() {
                                                @Override
                                                public void onResponse(Call<String> call, Response<String> response) {
                                                    //if fetch api is ok
                                                    try {
                                                        Log.i("responapi" ,"response : " + response.toString());
                                                        JSONObject jsonObject = new JSONObject(response.body());

                                                        JSONArray resultArray = jsonObject.getJSONArray("results");

                                                        JSONObject firstObject = resultArray.getJSONObject(0);

                                                        JSONObject geometry = firstObject.getJSONObject("geometry");
                                                        JSONObject location = geometry.getJSONObject("location");

                                                        Common.setLatitude(location.getString("lat"));
                                                        Common.setLongitude(location.getString("lng"));
                                                        Log.i("Home Address" ,"OnSelected HomeAddress : " + Common.getLatitude() + "\n" + Common.getLongitude());


                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                }

                                                @Override
                                                public void onFailure(Call<String> call, Throwable t) {
                                                    Toast.makeText(Home.this,"" + t.getMessage(),Toast.LENGTH_SHORT).show();
                                                }
                                            });


                                Toast.makeText(Home.this,"Update Home Address Succesfully", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
        alertDialog.show();
    }

//    private void showChangePasswordDialog() {
//
//        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
//        alertDialog.setTitle("CHANGE PASSWORD");
//        alertDialog.setMessage("Please fill all information");
//
//
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View layout_pwd = inflater.inflate(R.layout.change_password_layout,null);
//
//        MaterialEditText edtCurrPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtPassword);
//        MaterialEditText edtNewPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtNewPassword);
//        MaterialEditText edtConfirmPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtRepeatPassword);
//
//        alertDialog.setView(layout_pwd);
//
//        //Button
//        alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int i) {
//                //change password
//
//                final android.app.AlertDialog waitDialog = new SpotsDialog(Home.this);
//                waitDialog.show();
//                System.out.println(edtCurrPassword.getText().toString());
//                System.out.println(Common.currentUser.getPassword());
//                // check old password
//                if(edtCurrPassword.getText().toString().equals(Common.currentUser.getPassword())){
//                    // check new password with confirm pass
//
//                    if(edtNewPassword.getText().toString().equals(edtConfirmPassword.getText().toString()))
//                    {
//                        Map<String,Object> passwordUpdate = new HashMap<String,Object>();
//                        passwordUpdate.put("password",edtNewPassword.getText().toString());
//
//                        //make Update
//
//                        DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
//                        user.child(Common.currentUser.getPhone())
//                                .updateChildren(passwordUpdate)
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        waitDialog.dismiss();
//                                        System.out.println("Pass was update");
//                                        Toast.makeText(Home.this,"Pass was update",Toast.LENGTH_SHORT).show();
//
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        System.out.println("Wrong password");
//                                        System.out.println(e.getMessage());
//                                        Toast.makeText(Home.this,e.getMessage(),Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//
//                    }
//                    else{
//                        waitDialog.dismiss();
//                        System.out.println("New Password Doesn't Match");
//                        Toast.makeText(Home.this,"New Password Doesn't Match",Toast.LENGTH_SHORT).show();
//                    }
//                }
//                else{
//                    waitDialog.dismiss();
//                    System.out.println("Wrong Old Password");
//                    Toast.makeText(Home.this,"Wrong Old Password",Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        alertDialog.show();
//
//    };



}