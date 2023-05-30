package com.example.cnn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity implements View.OnClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_activity);
        TextView classInfo = findViewById(R.id.classInfo);
        TextView mainInformation = findViewById(R.id.info);
        String id = getIntent().getStringExtra("Sasha");
        switch (id.toLowerCase()) {
            case "deconstructivism":
                mainInformation.setText(R.string.deconstructivism);
                break;
            case "deconstructivizm_2":
                mainInformation.setText(R.string.deconstructivism);
                break;
            case "classicism":
                mainInformation.setText(R.string.classicism);
                break;
            case "gothic":
                mainInformation.setText(R.string.gothic);
                break;
            case "baroque":
                mainInformation.setText(R.string.baroque);
                break;
            case "modernism":
                mainInformation.setText(R.string.modernism);
                break;
        }
        classInfo.setText(id);
        Button button = (Button) findViewById(R.id.goBack);
        button.setOnClickListener((View.OnClickListener) this);
    }
    @Override
    public void onClick(View v) {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}