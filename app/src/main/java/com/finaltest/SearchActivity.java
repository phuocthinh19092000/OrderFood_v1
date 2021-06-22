package com.finaltest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.adapters.TextViewBindingAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.finaltest.Database.Database;
import com.finaltest.Interface.ItemClickListener;
import com.finaltest.Model.Food;
import com.finaltest.Model.Order;
import com.finaltest.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.midterm.finalexamorderfood.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

     // Search
     FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestedList = new ArrayList<String>();
    MaterialSearchBar materialSearchBar;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;

    // Favorites
    Database localDB;

    /// Create Targer from Picasso


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference().child("Food");

        //Local DB
        localDB = new Database(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_search);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    //    LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(),
           //    R.anim.);
      //  recyclerView.setLayoutAnimation(controller);

        // Search

        materialSearchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter the food you want : " );


        loadSuggest();

        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //When user typing, we will change suggest list
                List<String> suggest = new ArrayList<>();
                for(String search : suggestedList){
                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);


                }
                materialSearchBar.setLastSuggestions(suggest);
                if (!materialSearchBar.getText().isEmpty())
                    materialSearchBar.showSuggestionsList();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //When search bar is close
                //Restore originral suggest

                if(!enabled)
                    recyclerView.setAdapter(searchAdapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                searchAdapter.stopListening();
            }
        });
        
        loadAllFoods();


    }

    private void loadAllFoods() {
        // create query by name
        Query searchByName = foodList;

        // options
        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName,Food.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item_for_search, parent, false);

                return new FoodViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {
                holder.txtFoodName.setText(model.getName());
                holder.food_price.setText(String.format("$ %f",model.getPrice()));
                Picasso.get().load(model.getImage()).into(holder.foodView);

                // add Favorites
                if(localDB.isFavorite(adapter.getRef(position).getKey()))
                    holder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_24);
                else {
                    holder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }                // add quick cart

                holder.quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Database(getBaseContext()).addToCart(new Order(
                                adapter.getRef(position).getKey(),
                                model.getName(),
                                "1",
                                model.getPrice(),
                                model.getDiscount(),
                                model.getImage()
                        ));
                        Toast.makeText(SearchActivity.this,"Added to Cart", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(SearchActivity.this,""+model.getName()+" was added to your Favorites List ", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey());
                            holder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                            Toast.makeText(SearchActivity.this,""+model.getName()+" was remove from your Favorites List ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail = new Intent(SearchActivity.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", adapter.getRef(position).getKey());
                        startActivity(foodDetail);
                    }
                });

            }
        };
        recyclerView.setAdapter(adapter);


        // animation
        recyclerView.getAdapter().notifyDataSetChanged();
        //recyclerView.scheduleLayoutAnimation();
    }

    private void startSearch(CharSequence text) {
        // create query by name
        Query searchByName = foodList.orderByChild("name").equalTo(text.toString());

        // options
        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName,Food.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {
                holder.txtFoodName.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.foodView);

                final Food local = model;
                holder.setItemClickListener((view, position1, isLongClick) -> {
                  // start new activity

                    Intent foodDetail = new Intent(SearchActivity.this,FoodDetail.class);
                    foodDetail.putExtra("FoodId",searchAdapter.getRef(position).getKey());
                    startActivity(foodDetail);
                });

            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item_for_search,parent,false);
                return  new FoodViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter);
    }

    private void loadSuggest() {
        foodList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()){
                    Food item = postSnapshot.getValue(Food.class);
                    suggestedList.add(item.getName());

                }
                materialSearchBar.setLastSuggestions(suggestedList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStop() {
        if(adapter != null){
            adapter.stopListening();
        }

        if(searchAdapter != null){
            searchAdapter.stopListening();
        }

        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

}