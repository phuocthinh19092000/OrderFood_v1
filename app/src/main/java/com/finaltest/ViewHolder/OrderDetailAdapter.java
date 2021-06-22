package com.finaltest.ViewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import com.finaltest.Model.Order;
import com.finaltest.R;

import java.util.List;

/*class MyViewHolder extends RecyclerView.ViewHolder{
    public TextView name, quantity, price, discount;
    public MyViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
        name = (TextView)itemView.findViewById(R.id.product_name);
        quantity = (TextView)itemView.findViewById(R.id.product_quantity);
        price = (TextView)itemView.findViewById(R.id.product_price);
        discount = (TextView)itemView.findViewById(R.id.product_discount);
    }
}*/
public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder>{
    List<Order> myOrders;

    public OrderDetailAdapter(List<Order> myOrders) {
        this.myOrders = myOrders;
    }

    @NonNull
    @NotNull
    @Override
    public OrderDetailAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_detail_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull OrderDetailAdapter.ViewHolder holder, int position) {
        Order order = myOrders.get(position);
        holder.name.setText(String.format("Name : %s", order.getProductName()));
        holder.quantity.setText(String.format("Name : %s", order.getQuantity()));
        holder.price.setText(String.format("Name : %s", order.getPrice()));
        holder.discount.setText(String.format("Name : %s", order.getDiscount()));
    }

    @Override
    public int getItemCount() {
        return myOrders.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name, quantity, price, discount;
        public ViewHolder(View view) {
            super(view);
            name = (TextView)itemView.findViewById(R.id.product_name);
            quantity = (TextView)itemView.findViewById(R.id.product_quantity);
            price = (TextView)itemView.findViewById(R.id.product_price);
            discount = (TextView)itemView.findViewById(R.id.product_discount);
        }


    }
}
