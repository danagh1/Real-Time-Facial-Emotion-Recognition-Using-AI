package com.example.myfirstapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SplashActivity extends Activity implements View.OnClickListener {
    LinearLayout myLogo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);
        myLogo=findViewById(R.id.logoll);
        myLogo.setOnClickListener(this);
    }//end onCreate
    @Override
    public void onClick(View v) {
        Intent myIntent=new Intent(this,MainActivity.class);
        startActivity(myIntent);
        Toast.makeText(this,"ًًًWelcome to App!",Toast.LENGTH_LONG).show();
    }//end onClick
}//end class


