package com.example.golecadminbigbasket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateDriver extends AppCompatActivity {
    private EditText name, phoneNumber, password;
    private Button selectWarehouse;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_driver);
        name = findViewById(R.id.editText);
        phoneNumber = findViewById(R.id.editText2);
        password = findViewById(R.id.editText3);
        sharedPreferences = getSharedPreferences("com.example.golecadminbigbasket", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();



        selectWarehouse = findViewById(R.id.button2);
        selectWarehouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String driver_name = name.getText().toString();
                String driver_phone_number = phoneNumber.getText().toString();
                String driver_password = password.getText().toString();

                if(driver_name.isEmpty()){
                    name.setError("Required");
                    name.requestFocus();
                    return;
                }
                else if(driver_phone_number.isEmpty()){
                    phoneNumber.setError("Required");
                    phoneNumber.requestFocus();
                    return;
                }
                else if(driver_password.isEmpty()){
                    password.setError("Required");
                    password.requestFocus();
                    return;
                }

                insertValuesInDatabase(driver_name, driver_phone_number, driver_password);
                Intent intent = new Intent(CreateDriver.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void insertValuesInDatabase(String driver_name, String driver_phone_number, String driver_password) {
        editor.putString("driver_name", driver_name);
        editor.putString("driver_phone_number", driver_phone_number);
        editor.putString("driver_password", driver_password);
        editor.commit();
    }
}
