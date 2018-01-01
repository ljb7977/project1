package com.example.user.project2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ContactViewer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address2);
        Intent intent = getIntent();

        String name = intent.getStringExtra("name");
        String number = intent.getStringExtra("number");
        String email = intent.getStringExtra("email");

        TextView nameTextView = findViewById(R.id.textView1);
        TextView numberTextView = findViewById(R.id.textView2);
        TextView emailTextView = findViewById(R.id.textView3);

        nameTextView.setText(name);
        numberTextView.setText(number);
        emailTextView.setText(email);
    }
}