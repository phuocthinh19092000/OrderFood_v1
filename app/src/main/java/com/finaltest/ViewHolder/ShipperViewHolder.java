package com.finaltest.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.finaltest.Interface.ItemClickListener;
import com.finaltest.R;
import com.ornach.nobobutton.NoboButton;

public class ShipperViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView shipper_name, shipper_phone;
    public NoboButton btn_edit, btn_remove;
    private ItemClickListener itemClickListener;
    public ShipperViewHolder(@NonNull View itemView) {
        super(itemView);
        shipper_name = (TextView) itemView.findViewById(R.id.shipper_name);
        shipper_phone = (TextView) itemView.findViewById(R.id.shipper_phone);
        btn_edit = (NoboButton) itemView.findViewById(R.id.btnEdit);
        btn_remove = (NoboButton) itemView.findViewById(R.id.btnRemove);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);

    }


}
