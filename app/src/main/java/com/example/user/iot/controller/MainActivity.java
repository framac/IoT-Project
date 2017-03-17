package com.example.user.iot.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.user.iot.R;


public class MainActivity extends AppCompatActivity {

    Button b6,b7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b6 = (Button) findViewById(R.id.button6);
        b7 = (Button) findViewById(R.id.button7);

        b7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openPageBluetooth = new Intent(getApplicationContext(),GestioneConnessioneBA.class);
                startActivity(openPageBluetooth);

            }
        });

    }
    
}


