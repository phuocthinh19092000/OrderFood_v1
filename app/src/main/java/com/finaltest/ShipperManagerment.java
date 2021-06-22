package com.finaltest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.finaltest.Common.Common;
import com.finaltest.Model.Category;
import com.finaltest.Model.Shipper;
import com.finaltest.ViewHolder.MenuViewHolder;
import com.finaltest.ViewHolder.ShipperViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

public class ShipperManagerment extends AppCompatActivity {

    FloatingActionButton fabAdd;

    FirebaseDatabase database;
    DatabaseReference shippers;
    FirebaseRecyclerAdapter adapter;

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper_managerment);

        //Init view
        fabAdd =  (FloatingActionButton) findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowCreateShipperLayout();
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recyler_shippers);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Firebase
        database = FirebaseDatabase.getInstance();
        shippers = database.getReference(Common.SHIPPERS_TABLE);

        //Load all Shipper
        FirebaseRecyclerOptions<Shipper> allshipper = new FirebaseRecyclerOptions.Builder<Shipper>().setQuery(shippers, Shipper.class).build();
        adapter = new FirebaseRecyclerAdapter<Shipper, ShipperViewHolder>(allshipper) {
            @NonNull
            @Override
            public ShipperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shipper_layout, parent, false);
                return new ShipperViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ShipperViewHolder holder, int position, @NonNull Shipper model) {
                holder.shipper_phone.setText(model.getPhone());
                holder.shipper_name.setText(model.getName());

                holder.btn_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditDialog(adapter.getRef(position).getKey(), model);
                    }
                });
                holder.btn_remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeShipper(adapter.getRef(position).getKey());
                    }
                });

            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);



    }
    private void removeShipper(String key){
        shippers.child(key)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ShipperManagerment.this, "Remove Succeed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ShipperManagerment.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        adapter.notifyDataSetChanged();
    }
    private void showEditDialog(String key, Shipper model){
        AlertDialog.Builder create_shipper_dialog = new AlertDialog.Builder(ShipperManagerment.this);
        create_shipper_dialog.setTitle("Update Shipper");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.create_shipper_layout, null);

        final MaterialEditText edtName = (MaterialEditText) view.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = (MaterialEditText) view.findViewById(R.id.edtPhone);
        final MaterialEditText edtPassword = (MaterialEditText) view.findViewById(R.id.edtPassword);

        //Set data
        edtName.setText(model.getName());
        edtPhone.setText(model.getPhone());
        edtPassword.setText(model.getPassword());

        create_shipper_dialog.setView(view);

        create_shipper_dialog.setIcon(R.drawable.ic_baseline_local_shipping_24);

        create_shipper_dialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                Map<String, Object> update = new HashMap<>();
                update.put("name",edtName.getText().toString());
                update.put("password", edtPassword.getText().toString());
                update.put("phone", edtPhone.getText().toString());


                shippers.child(edtPhone.getText().toString())
                        .updateChildren(update)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ShipperManagerment.this, "Shipper Update ! ", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShipperManagerment.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        create_shipper_dialog.setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        create_shipper_dialog.show();
    }

    private void ShowCreateShipperLayout(){
        AlertDialog.Builder create_shipper_dialog = new AlertDialog.Builder(ShipperManagerment.this);
        create_shipper_dialog.setTitle("Create Shipper");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.create_shipper_layout, null);

        final MaterialEditText edtName = (MaterialEditText) view.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = (MaterialEditText) view.findViewById(R.id.edtPhone);
        final MaterialEditText edtPassword = (MaterialEditText) view.findViewById(R.id.edtPassword);

        create_shipper_dialog.setView(view);

        create_shipper_dialog.setIcon(R.drawable.ic_baseline_local_shipping_24);

        create_shipper_dialog.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                Shipper shipper = new Shipper();
                shipper.setName(edtName.getText().toString());
                shipper.setPassword(edtPassword.getText().toString());
                shipper.setPhone(edtPhone.getText().toString());

                shippers.child(edtPhone.getText().toString())
                        .setValue(shipper)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ShipperManagerment.this, "Shipper Create", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShipperManagerment.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        create_shipper_dialog.setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        create_shipper_dialog.show();
    }


}
