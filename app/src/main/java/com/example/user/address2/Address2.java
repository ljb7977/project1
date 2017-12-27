package com.example.user.address2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class Address2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address2);
        Intent intent = getIntent();

        String name = intent.getStringExtra("name");
        String number = intent.getStringExtra("number");
        String email = intent.getStringExtra("email");

        TextView nameTextView = (TextView)findViewById(R.id.textView1);
        TextView numberTextView = (TextView)findViewById(R.id.textView2);
        TextView emailTextView = (TextView)findViewById(R.id.textView3);

        nameTextView.setText(name);
        numberTextView.setText(number);
        emailTextView.setText(email);
    }

}