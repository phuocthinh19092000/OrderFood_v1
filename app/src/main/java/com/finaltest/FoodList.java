package com.finaltest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finaltest.Common.Common;
import com.finaltest.Database.Database;
import com.finaltest.Interface.ItemClickListener;
import com.finaltest.Model.Food;
import com.finaltest.Model.Order;
import com.finaltest.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.midterm.finalexamorderfood.R;
import com.squareup.picasso.Picasso;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;

    FirebaseDatabase db;
    DatabaseReference foodlist;

    String categoryId= "";

    FirebaseRecyclerAdapter adapter;


    // Favorites
    Database localDB;

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
        setContentView(R.layout.activity_food_list);

        db = FirebaseDatabase.getInstance();
        foodlist = db.getReference().child("Food");

        //Local DB
        localDB = new Database(this);


        recyclerView = (RecyclerView) findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if(getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");
        if (!categoryId.isEmpty() && categoryId!=null){

            if (Common.isConnectedToInternet(getBaseContext())) {

                FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>().setQuery(foodlist.orderByChild("menuId").equalTo(categoryId), Food.class).build();
                adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {

                    @NonNull
                    @Override
                    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item, parent, false);

                        return new FoodViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {
                        holder.txtFoodName.setText(model.getName());
                        holder.food_price.setText(String.format("$ %s",model.getPrice().toString()));
                        Picasso.get().load(model.getImage()).into(holder.foodView);

                        // add Favorites
                        if(localDB.isFavorite(adapter.getRef(position).getKey()))
                            holder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_24);

                        // add quick cart

                        holder.quick_cart.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new Database(getBaseContext()).addToCart(new Order(
                                        adapter.getRef(position).getKey(),
                                        model.getName(),
                                        "1",
                                        model.getPrice(),
                                        model.getDiscount()
                                ));
                                Toast.makeText(FoodList.this,"Added to Cart", Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Click to change state of Favorites
                        holder.fav_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(!localDB.isFavorite(adapter.getRef(position).getKey()))
                                {
                                    localDB.addToFavorites(adapter.getRef(position).getKey());
                                    holder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_24);
                                    Toast.makeText(FoodList.this,""+model.getName()+" was added to your Favorites List ", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    localDB.removeFromFavorites(adapter.getRef(position).getKey());
                                    holder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                                    Toast.makeText(FoodList.this,""+model.getName()+" was remove from your Favorites List ", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        final Food local = model;

                        holder.setItemClickListener(new ItemClickListener() {
                            @Override
                            public void onClick(View view, int position, boolean isLongClick) {
//                            Toast.makeText(FoodList.this,""+local.getName(), Toast.LENGTH_SHORT).show();
                                Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                                foodDetail.putExtra("FoodId", adapter.getRef(position).getKey());
                                startActivity(foodDetail);
                            }
                        });

                    }
                };
                recyclerView.setAdapter(adapter);
            }
            else {
                Toast.makeText(FoodList.this, "Please check your internet connection !!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();

        // fix click back button from Food and don't see category

        if(adapter!=null){
            adapter.startListening();
        }

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
}