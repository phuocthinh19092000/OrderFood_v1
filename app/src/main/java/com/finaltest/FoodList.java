package com.finaltest;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finaltest.Interface.ItemClickListener;
import com.finaltest.Model.Food;
import com.finaltest.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.midterm.finalexamorderfood.R;
import com.squareup.picasso.Picasso;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;

    FirebaseDatabase db;
    DatabaseReference foodlist;

    String categoryId= "";

    FirebaseRecyclerAdapter adapter;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        db = FirebaseDatabase.getInstance();
        foodlist = db.getReference().child("Food");

        recyclerView = (RecyclerView) findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if(getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");
        if (!categoryId.isEmpty() && categoryId!=null){

            FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>().setQuery(foodlist.orderByChild("MenuId").equalTo(categoryId), Food.class).build();
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
                    Picasso.get().load(model.getImage()).into(holder.foodView);

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