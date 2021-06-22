package com.finaltest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.finaltest.Common.Common;
import com.finaltest.Interface.ItemClickListener;
import com.finaltest.Model.Category;
import com.finaltest.Model.MyRespone;
import com.finaltest.Model.Notification;
import com.finaltest.Model.Request;
import com.finaltest.Model.Sender;
import com.finaltest.Model.Token;
import com.finaltest.Service.APIService;
import com.finaltest.ViewHolder.OrderViewHolder;
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
    MaterialSpinner spinner, shipperSpinner;
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
        spinner = (MaterialSpinner) findViewById(R.id.statusSpinner);
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
        //alertDialog.setMessage("please choose status");
        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.update_order_layout, null);
        spinner = add_menu_layout.findViewById(R.id.statusSpinner);
        //spinner.setItems("Placed", "On my way", "Shipped");
        List<String> list=new ArrayList<String>();
        /*list.add("Placed");
        list.add("On my way");
        list.add("Shipping");
        spinner.setItems(list);*/

        if (item.getStatus().equals("0")){
            spinner.setItems("Placed", "On my way", "Shipping");
        }
        else if (item.getStatus().equals("1")){
            spinner.setItems("On my way","Placed", "Shipping");
        }
        else
            spinner.setItems("Shipping","On my way","Placed");

        //Load all shipper phone to spinner
        shipperSpinner = add_menu_layout.findViewById(R.id.shipperSpinner);
        List<String> shipperList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.SHIPPERS_TABLE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot shipperSnapshot:snapshot.getChildren())
                            shipperList.add(shipperSnapshot.getKey());
                        shipperSpinner.setItems(shipperList);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        alertDialog.setView(add_menu_layout);


        final String localKey = key;


        // Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                if (item.getStatus().equals("2"))
                {
                    FirebaseDatabase.getInstance().getReference(Common.ORDER_NEED_SHIP_TABLE)
                            .child(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString())
                            .child(localKey)
                            .setValue(item);
                    requests.child(localKey).setValue(item);

                    adapter.notifyDataSetChanged();

                }
                else {
                    // Update information

                    requests.child(localKey).setValue(item);
                    adapter.notifyDataSetChanged();
                    //sendOrderStatusToUser(key, item);
                }
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
                    Notification notification = new Notification("EDMT Dev", "Your order "+ key + " was updated");
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