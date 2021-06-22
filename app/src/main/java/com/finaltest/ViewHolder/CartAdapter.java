package com.finaltest.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.finaltest.Cart;
import com.finaltest.Common.Common;
import com.finaltest.Database.Database;
import com.finaltest.Interface.ItemClickListener;
import com.finaltest.Model.Order;
import com.midterm.finalexamorderfood.R;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

    public TextView txt_cart_name, txt_price;
    public ElegantNumberButton btn_quantity;
    public ImageView cart_image;


    private ItemClickListener itemClickListener;

    public void setTxt_cart_name(TextView txt_cart_name) {
        this.txt_cart_name = txt_cart_name;
    }

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);
        txt_cart_name = (TextView) itemView.findViewById(R.id.cart_item_name);
        txt_price = (TextView)itemView.findViewById(R.id.cart_item_price);
        btn_quantity =(ElegantNumberButton) itemView.findViewById(R.id.btn_quantity);
        cart_image = (ImageView) itemView.findViewById(R.id.cart_image);
        itemView.setOnCreateContextMenuListener(this);
    }
    @Override
    public void onClick(View v) {

    }


    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        System.out.println("Check In");
        contextMenu.setHeaderTitle("Select Your Action : ");
        contextMenu.add(0,0,getAdapterPosition(),Common.DELETE);
    }
}

public class CartAdapter extends RecyclerView.Adapter<CartViewHolder> {

    private List<Order> listData = new ArrayList<>();
    private Cart cart;

    public CartAdapter(List<Order> listData, Cart cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(cart);

        View itemview = inflater .inflate(R.layout.cart_layout, parent,false);
        return new CartViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
//        TextDrawable drawable = TextDrawable.builder()
//                .buildRound(""+listData.get(position).getQuantity(), Color.RED);
//        holder.img_cart_count.setImageDrawable(drawable);

        // init image for each food has in cart

        //
        Picasso.get().load(listData.get(position).getImage())
                .resize(70,70)
                .centerCrop()
                .into(holder.cart_image);
        //Picasso.get().load(currentFood.getImage()).into(food_image);

        holder.btn_quantity.setNumber(listData.get(position).getQuantity());
        holder.btn_quantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order = listData.get(position);
                order.setQuantity(String.valueOf(newValue));
                new Database(cart).updateCart(order);

                //update txt price

                int total = 0 ;
                List<Order> orders = new Database(cart).getCarts();
                for (Order item: orders){
                    total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));

                    Locale locale = new Locale("en", "US");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                    cart.txtTotalPrice.setText(fmt.format(total));
                }




            }
        });



        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        int price  = Integer.parseInt(listData.get(position).getPrice());
        holder.txt_price.setText(fmt.format(price));
        holder.txt_cart_name.setText(listData.get(position).getProductName());

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}