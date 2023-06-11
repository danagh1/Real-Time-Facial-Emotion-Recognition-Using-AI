package com.example.myfirstapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView feelingTextView, tipTextView, videoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Get the byte array from the intent
        byte[] byteArray = getIntent().getByteArrayExtra("image");
        // Decode the byte array into a bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        // get the image view and text views from the layout
        imageView = findViewById(R.id.imageView);
        feelingTextView = findViewById(R.id.feelingTextView);
        tipTextView = findViewById(R.id.tipTextView);
        videoButton = findViewById(R.id.videoButton);
        // get the extras from the intent
        Intent intent = getIntent();
        imageView.setImageBitmap(bitmap);
        String feeling = intent.getStringExtra("feeling");
        String advice = intent.getStringExtra("advice");
        String video = intent.getStringExtra("video");
        // set the image, feeling, and advice in the UI
        imageView.setImageBitmap(bitmap);
        feelingTextView.setText(feeling);
        tipTextView.setText(advice);

         // set the video button OnClickListener to open the video link
        videoButton.setOnClickListener(new View.OnClickListener()
        {
        @Override
        public void onClick (View v){
            final MediaPlayer mp= MediaPlayer.create(ResultActivity.this,R.raw.clicksoundeffect);//object for media file
            mp.start();//play the sound
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(video));
        startActivity(intent);
    }
    });
}
    private void makeToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
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
                "Our app provides you with your emotional state, and offers personalized tips and suggestions to help you improve your mood. In addition our app provides you with videos and content to help you enhance your mood and overall well-being. \n" +
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
}//end ResultActivity class