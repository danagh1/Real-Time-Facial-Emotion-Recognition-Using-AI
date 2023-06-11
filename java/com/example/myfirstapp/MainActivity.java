package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static String LOGTAG = "OpenCV_Log";
    Button Take_a_Photo, Detect_in_RealTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //to link the layout with the activity

        Take_a_Photo= findViewById(R.id.Take_a_PhotoBtn); //to get component from xml by its id, R mean resource
        Take_a_Photo.setOnClickListener(this); //to make the object clickable
        Detect_in_RealTime= findViewById(R.id.Detect_in_RealTimeBtn);
        Detect_in_RealTime.setOnClickListener(this);

        if(OpenCVLoader.initDebug()){
            Log.d(LOGTAG, "OpenCV is loaded");
        }
        else {
            Log.d(LOGTAG, "OpenCV is not loaded");
        }
    }//end onCreate

    @Override
    public void onClick(View view) {
        final MediaPlayer mp= MediaPlayer.create(MainActivity.this,R.raw.clicksoundeffect);//object for media file
           switch (view.getId()) {
               case R.id.Take_a_PhotoBtn:
                   //what to do when click it
                   mp.start();//play the sound
                   Intent myIntent1 = new Intent(MainActivity.this,FirstButtonActivity.class); //to move from MainActivity screen to FirstButtonActivity, i enter first my place then i enter where i went to go
                   startActivity(myIntent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                   break;
               case R.id.Detect_in_RealTimeBtn:
                   //what to do when click it
                   mp.start();//play the sound
                   Intent myIntent2 = new Intent(MainActivity.this,SecondButtonActivity.class); //to move from MainActivity screen to SecondButtonActivity, i enter first my place then i enter where i went to go
                   startActivity(myIntent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                   break;
           }//end switch
    }//end OnClick
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();//when talk to a different type than your type, it will be the mediator between them, because menu is java and want to talk with xml menu in res
        inflater.inflate(R.menu.my_option_menu, menu);
        return true;
    }//end onCreateOptionsMenu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.aboutUsOptionItem:
                //code what do when click it
                showAboutDialog();
                return true;
            case R.id.usageOptionItem:
                //code what do when click it
                showUsageDialog();
                return true;
            case R.id.exitOptionItem:
                //finish all activities in the task and destroys the task. This will effectively close the app.
                finishAffinity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }//end switch
    }//end onOptionsItemSelected

    private void showUsageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Usage");

        // Set up the custom view
        TextView messageView = new TextView(this);
        messageView.setText("If you press the first button \"Take Photo\", a screen will show up with three buttons. Press \"Capture Image\" to take a photo, or press \"Choose Image From Gallery\" to select a picture from your gallery, and to view the results press \"Process Image\". Once the image is processed, the app will display the photo you captured along with your feelings and advice based on your feelings. Additionally, you can click on the \"Watch Video\" button to view a video.\n" +
                "\n" +
                "On the other hand, if you press the second button \"Real-Time\", the camera screen will open and the app will detect your feelings in real-time. There is a button at the top to flip the camera and next to it there is also a button to set the delay time and based on the time you set, you will receive a notification containing your emotions and some tips. Clicking on the notification will open a video for you to watch.\n");
        messageView.setPadding(40, 20, 40, 20);
        messageView.setTextSize(15);
        messageView.setGravity(Gravity.START);
        messageView.setGravity(Gravity.FILL);
        builder.setView(messageView);

        // Add the buttons
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }//end showUsageDialog
    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About Us");

        // Set up the custom view
        TextView messageView = new TextView(this);
        messageView.setText("Our app uses artificial intelligence technology to read and interpret your facial expressions, allowing us to determine your emotional state. We believe that being aware of your emotions is an essential component of leading a healthy and balanced life, and our app is designed to help you achieve just that.\n" +
                "\n" +
                "Our app provides you with your emotional state, and offers personalized tips and suggestions to help you improve your mood. In addition our app provides you with videos and content to help you enhance your mood and overall well-being.\n" +
                "\n" +
                "At our core, we believe that everyone deserves to live a fulfilling and happy life. We designed this app with that goal in mind, and we hope that it will help you achieve your emotional and mental well-being goals.");
        messageView.setPadding(40, 20, 40, 20);
        messageView.setTextSize(15);
        messageView.setGravity(Gravity.START);
        messageView.setGravity(Gravity.FILL);
        builder.setView(messageView);

        // Add the buttons
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }//end showAboutDialog
}//end class