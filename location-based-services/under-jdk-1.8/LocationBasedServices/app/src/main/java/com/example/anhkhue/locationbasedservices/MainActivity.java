package com.example.anhkhue.locationbasedservices;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getCanonicalName();

    private Button btnOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Create Activity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpen = findViewById(R.id.btnOpen);
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }
        });
    }
}
