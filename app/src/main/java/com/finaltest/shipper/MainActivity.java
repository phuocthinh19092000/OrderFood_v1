package com.finaltest.shipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.hotspot2.pps.HomeSp;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.finaltest.shipper.Common.Common;
import com.finaltest.shipper.Model.Shipper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ornach.nobobutton.NoboButton;
import com.rengwuxian.materialedittext.MaterialEditText;

public class MainActivity extends AppCompatActivity {

    NoboButton btn_sign_in;
    MaterialEditText edt_phone, edt_password;

    FirebaseDatabase database;
    DatabaseReference shippers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_sign_in = (NoboButton) findViewById(R.id.btnSignIn);
        edt_phone = (MaterialEditText) findViewById(R.id.edtPhone);
        edt_password = (MaterialEditText) findViewById(R.id.edtPassword);

        //FireBase
        database = FirebaseDatabase.getInstance();
        shippers = database.getReference(Common.SHIPPER_TABLE);

        btn_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(edt_phone.getText().toString(), edt_password.getText().toString());
            }
        });

    }
    private void login(String phone, String password){
        shippers.child(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!phone.isEmpty() && !password.isEmpty()) {
                            if (snapshot.exists()) {
                                Shipper shipper = snapshot.getValue(Shipper.class);
                                if (shipper.getPassword().equals(password)) {
                                    //login success
                                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                    Common.currentShipper = shipper;
                                    finish();

                                } else
                                    Toast.makeText(MainActivity.this, "Password incorrect !", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Your shipper's phone is not exist", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Empty !!", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}