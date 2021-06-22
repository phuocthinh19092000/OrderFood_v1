package com.finaltest.shipper.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;



import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.finaltest.shipper.R;
import com.ornach.nobobutton.NoboButton;

public class OrderViewHolder extends RecyclerView.ViewHolder{
    public TextView txtHolderId, txtOrderStatus,txtOrderPhone, txtOrderAddress;

    //private ItemClickListener itemClickListener;

    public NoboButton btnShipping;
    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);
        txtHolderId = (TextView)itemView.findViewById(R.id.order_id);
        txtOrderAddress = (TextView) itemView.findViewById(R.id.order_address);
        txtOrderPhone = (TextView) itemView.findViewById(R.id.order_phone);
        txtOrderStatus = (TextView) itemView.findViewById(R.id.order_status);
        btnShipping = (NoboButton)itemView.findViewById(R.id.btnShipping);
        //itemView.setOnClickListener(this);
        //itemView.setOnLongClickListener(this);
        //itemView.setOnCreateContextMenuListener(this);
    }

    /*public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(),false);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select the action");
        menu.add(0,0,getAdapterPosition(), "Update");
        menu.add(0,1,getAdapterPosition(), "Delete");
    }*/

    /*@Override
    public boolean onLongClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(),true);
        return true;
    }*/
}