package com.example.foodapporderserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.foodapporderserver.Common.Common;
import com.example.foodapporderserver.Interface.ItemClickListener;
import com.example.foodapporderserver.Model.Category;
import com.example.foodapporderserver.Model.MyRespone;
import com.example.foodapporderserver.Model.Notification;
import com.example.foodapporderserver.Model.Request;
import com.example.foodapporderserver.Model.Sender;
import com.example.foodapporderserver.Model.Token;
import com.example.foodapporderserver.Service.APIService;
import com.example.foodapporderserver.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderStatus extends AppCompatActivity {
    public RecyclerView recyclerView;
    public LinearLayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;
    MaterialSpinner spinner;
    FirebaseDatabase database;
    DatabaseReference requests;
    APIService mService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        mService = Common.getFCMClient();
        recyclerView = (RecyclerView) findViewById(R.id.listOrder);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>().setQuery(requests, Request.class).build();
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_layout, parent, false);
                return new OrderViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, int position, @NonNull Request model) {
                holder.txtHolderId.setText(adapter.getRef(position).getKey());
                holder.txtOrderStatus.setText(Common.converCodeToStatus(model.getStatus()));
                holder.txtOrderAddress.setText(model.getAddress());
                holder.txtOrderPhone.setText(model.getPhone());
                holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUpdateDialog(adapter.getRef(position).getKey(), (Request) adapter.getItem(position));
                    }
                });
                holder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteCategory(adapter.getRef(position).getKey());
                    }
                });
                holder.btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent orderDetails = new Intent(OrderStatus.this,OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetails.putExtra("OrderId", adapter.getRef(position).getKey());
                        startActivity(orderDetails);
                    }
                });
                holder.btnDirection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent trackingOrder = new Intent(OrderStatus.this, OrderTracking.class);
                        Common.currentRequest = model;
                        startActivity(trackingOrder);
                    }
                });

            }
        };
        recyclerView.setAdapter(adapter);
    }
    /*@Override
    public boolean onContextItemSelected(MenuItem item){
        if(item.getTitle().equals(Common.UPDATE)){
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), (Request) adapter.getItem(item.getOrder()));
        }
        else{
            deleteCategory(adapter.getRef(item.getOrder()).getKey());

        }
        return super.onContextItemSelected(item);
    }*/
    private void deleteCategory(String key) {
        requests.child(key).removeValue();
        adapter.notifyDataSetChanged();
        Toast.makeText(this,"Item deleted !!!", Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(String key, Request item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("please choose status");
        LayoutInflater inflater = this.getLayoutInflater();
        final View add_menu_layout = inflater.inflate(R.layout.update_order_layout, null);
        spinner = add_menu_layout.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed", "On my way", "Shipped");
        /*List<String> list=new ArrayList<String>();
        list.add("Placed");
        list.add("On my way");
        list.add("Shipped");*/
        //spinner.setItems(list);
        alertDialog.setView(add_menu_layout);
        final String localKey = key;


        // Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // Update information
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));
                requests.child(key).setValue(item);
                adapter.notifyDataSetChanged();
                sendOrderStatusToUser(key, item);
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void sendOrderStatusToUser(String key,Request item) {
        DatabaseReference tokens = database.getReference("Tokens");
        tokens.orderByKey().equalTo(item.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for( DataSnapshot snapshot1:snapshot.getChildren()){
                    Token token = snapshot1.getValue(Token.class);
                    Notification notification = new Notification("Laughing boiz", "Your order "+ key + " was updated");
                    Sender content = new Sender(token.getToken(), notification);
                    mService.sendNotification(content).enqueue(new Callback<MyRespone>() {
                        @Override
                        public void onResponse(Call<MyRespone> call, Response<MyRespone> response) {
                            if(response.body().success == 1){
                                Toast.makeText(OrderStatus.this, "Order was updated", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(OrderStatus.this, "Order was updated but failed to send notification", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<MyRespone> call, Throwable t) {
                            Log.e("ERROR", t.getMessage());

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
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