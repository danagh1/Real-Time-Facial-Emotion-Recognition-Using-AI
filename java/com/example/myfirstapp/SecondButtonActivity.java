package com.example.myfirstapp;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class SecondButtonActivity extends CameraActivity {
    private static final String TAG="OpenCV_Log";
    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase mOpenCvCameraView;
    private ImageView flip_camera;
    private int mCameraId=0; //back camera
    private FacialExpressionRecognition facialExpressionRecognition; //call java class
    private static final String CHANNEL_ID = "FeelingNotificationChannel";
    private int notificationId = 1; // unique ID for the notification
    private NotificationCompat.Builder builder; // notification builder
    private Handler mHandler = new Handler();
    private boolean mShouldSendNotification = true;
    private long delayTime = 60 * 1000; // 1 minute by default

    private BaseLoaderCallback mLoaderCallback =new BaseLoaderCallback(SecondButtonActivity.this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:{
                    Log.v(TAG,"OpenCV Loaded");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    private HashMap<String, String> videoLinks = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secondbtn_layout);

        mOpenCvCameraView =(CameraBridgeViewBase) findViewById(R.id.my_camera_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(cvCameraViewListener);
        flip_camera=findViewById(R.id.flip_camera);
        //when flip camera button is clicked
        flip_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //this function will change camera
                swapCamera();
            }
        });
        //this will load cascade classifier and model
        try {
            int inputSize=48; //input size of model is 48
            facialExpressionRecognition= new FacialExpressionRecognition(getAssets(),SecondButtonActivity.this,"tf_lite_model.tflite",inputSize);

        }
        catch (IOException e){
            e.printStackTrace();
        }
        videoLinks.put("Practice deep breathing, meditation, yoga, or other relaxation techniques can help you manage your anger in the moment.", "https://www.youtube.com/watch?v=4pLUleLdwY4");
        videoLinks.put("Try to focus on the present. It’s easy to get caught up in negative thoughts and worries about the future or regrets about the part. Try to focusing on the present moment and the things you can control.", "https://www.youtube.com/watch?v=YkOlMKD8b7Y");
        videoLinks.put("Stay present. When we’re happy, it can be tempting to focus on the future or dwell on the past, but it’s important to stay present and enjoy the moment.", "https://www.youtube.com/watch?v=YkOlMKD8b7Y");
        videoLinks.put("Reach out to someone you trust. Talking to someone you trust about how you’re feeling can be a great way to get some support, or express your gratitude to someone important in your life, which will make you a happier person.", "https://www.youtube.com/watch?v=oHv6vTKD6lg");
        videoLinks.put("Practice self-care. Take care of yourself can help improve your mood, this could mean getting enough sleep, eating nutritious foods, exercising, and doing things you enjoy.", "https://www.youtube.com/watch?v=3QIfkeA6HBY");
        videoLinks.put("Try something new. Sometimes feeling neutral can be a sign that we’re stuck in a rut. Trying something new can help break up the monotony and add some excitement to your life.", "https://www.youtube.com/watch?v=ATHb_LskphI");
        videoLinks.put("Take care of yourself. Maintaining good physical and mental health is important for sustaining happiness this could mean getting enough sleep, eating nutritious foods, exercising, taking photos, and doing things you enjoy.", "https://www.youtube.com/watch?v=YY9H-6bEoKU");
        videoLinks.put("Do something you love. Engage in hobbies or activities that you find enjoyable or that give you a sense of purpose. This can help to distract you from negative thoughts and feelings and bring more positivity into your life.", "https://www.youtube.com/watch?v=KVymp-xjYKg&t=63s");
        videoLinks.put("Practice self-care. Taking care of yourself is important no matter how you’re feeling. Consider doing things that make you feel good, such as taking a warm bath, reading a book, or spending time in nature.", "https://www.youtube.com/watch?v=W7_lafxj8ok");
        videoLinks.put("Face your fears gradually. Avoiding your fears can actually make them worse over time. Try to identify your fears and confront them in a safe and controlled manner.", "https://www.youtube.com/watch?v=s_jaJlyNreM");
        videoLinks.put("Challenge negative thoughts. Fear can be fueled by negative thought and beliefs. Try to challenge these thoughts and frame them in a more positive or realistic way.","https://www.youtube.com/watch?v=teVE3VGrBhM");
        videoLinks.put("Identify the source of your fear. Understanding why you’re feeling afraid can help you address the root cause of your emotions.", "https://www.youtube.com/watch?v=CdaLtJOaLAo");
        videoLinks.put("Consider seeking professional help. If you’re struggling with intense or persistent feelings of sadness, it may be helpful to talk to a mental health professional. They can help you work through your emotions and develop coping strategies.", "https://www.youtube.com/watch?v=9FbBwehUp5Q");
        videoLinks.put("Remember, it’s okay to feel sad sometime but if your sadness is impacting your daily life or does not seem to be improving, it’s important to seek help.", "https://www.youtube.com/watch?v=InDEc1sDfE4");
        videoLinks.put("Take a step back. When you’re feeling angry, it’s easy to say or do things you’ll regret later. Take step back and try to calm down before responding.","https://www.youtube.com/watch?v=BsVq5R_F6RA");
        videoLinks.put("Identify the source of your anger. Understanding why you’re feeling angry can help you address the root cause of your emotions.", "https://www.youtube.com/watch?v=sm3YJ_ndSWc");
        videoLinks.put("Communicate calmly assertively. When you’re ready to respond, try to communicate calmly and assertively. Use ”I” statements to express how you’re feeling and avoid blaming or attacking the other person.", "https://www.youtube.com/watch?v=hAxCpAnV3-E");
        videoLinks.put("Consider seeking professional help. If you’re struggling with persistent feelings of anger or find that your anger is impacting your daily life or relationships, it may be helpful to talk to mental health professional, they can develop strategies to manage your emotions in a healthy way.","https://www.youtube.com/watch?v=9FbBwehUp5Q");
        videoLinks.put("Remember, it’s okay to feel angry sometimes, but it’s important to learn how to manage you’re anger in a way that’s healthy and productive.", "https://www.youtube.com/watch?v=sbVBsrNnBy8&t=144s");
        videoLinks.put("Identify the source of your disgust. Understanding why you’re feeling disgusted can help you address the root cause of your emotions.", "https://www.youtube.com/watch?v=74Z6WrQiu5k");
        videoLinks.put("Practice acceptance and compassion. It’s important to remember that every one has different experiences and perspectives. Try to practice acceptance and compassion towards yourself and others.","https://www.youtube.com/watch?v=YxqFqCdNfoQ");
        videoLinks.put("Try to focus on the positive things in your life such as things you’re grateful for or things that bring you joy.","https://www.youtube.com/watch?v=3h3t2P6-AlE");
        videoLinks.put("Consider seeking professional help. If you’re straggling with persistent feeling of disgust or find that your emotions are impacting your daily life, it may be helpful to talk to a mental health professional.", "https://www.youtube.com/watch?v=9FbBwehUp5Q");
        videoLinks.put("Remember ,its okay to feel disgust sometime but if your emotions are impacting your well-being it’s important to seek help.", "https://www.youtube.com/watch?v=DxIDKZHW3-E");
        videoLinks.put("Consider seeking professional help. If you’re struggling wit persistent feelings of fear or find that your fear is impacting your daily life. Its may be helpful to talk to a mental health professional.", "https://www.youtube.com/watch?v=9FbBwehUp5Q");
        videoLinks.put("Share your happiness. Happiness is contagious, and sharing it with others can help spread positive emotions. Consider doing something kind for someone else, or doing volunteer work, or helping others make ends meet, or simply sharing your joy with the people you care about.", "https://www.youtube.com/watch?v=lUKhMUZnLuw");
        videoLinks.put("Practice gratitude. Taking time to appreciate the good things in your life can help boost your mood and extend feelings of happiness. Consider keeping a gratitude journal, or simply taking a few minutes each day to reflect on the things you’re thankful for.", "https://www.youtube.com/watch?v=OLE1kAKQWIQ");
        videoLinks.put("Set goals and work towards them. Having goals to work towards can give you a sense of purpose and help you maintain a positive outlook. Consider setting achievable goals that align with your values and interest.", "https://www.youtube.com/watch?v=XpKvs-apvOs");
        videoLinks.put("Remember, happiness is a state of mind that requires some effort to maintain by practicing habits regularly, you can cultivate and extend your feelings of happiness.", "https://www.youtube.com/watch?v=W7_lafxj8ok&t=2s");
        videoLinks.put("Allow yourself to experience the surprise. Surprise is a natural human emotion, and it’s okay to feel it. Allow yourself to experience the surprise fully without judgment.", "https://www.youtube.com/watch?v=8ZpDAStom74");
        videoLinks.put("Reflect on the source of your surprise. Understanding why you’re feeling surprised can help you process and integrate the experience. Take some time to reflect on what triggered the surprise.", "https://www.youtube.com/watch?v=uFhsm5eGCgM");
        videoLinks.put("Consider the positive aspects. Some time surprise can be positive even if they feel unexpected at first try to consider any positive aspects of the surprise and focus on these.", "https://www.youtube.com/watch?v=jt9YvXHfBfw");
        videoLinks.put("Embrace change. Surprise can often bring about change, which can be scary but also exciting. Try to embrace any changes that may come with the surprise and see them as opportunities for growth and new experience.", "https://www.youtube.com/watch?v=hnqjQrSWbGQ");
        videoLinks.put("Share your surprise with others. Sometimes sharing your surprise with others can help you process the experience and gain new perspective. Consider talking to a trusted friend or family member about your surprise.", "https://www.youtube.com/watch?v=WNdPA7OVPgM");
        videoLinks.put("Remember, feeling surprise is normal part of the human experience by allowing yourself to fully experience the surprise, reflecting on its source, and embracing any positive aspects or changes that may come with it, you can move through the surprise in a healthy way.", "https://www.youtube.com/watch?v=MOQcSzkKfbs");
        videoLinks.put("Take some time to reflect. Sometime feeling neutral can be a sign that we need to take a step back and reflect on our lives. Consider taking sometimes to reflect on what’s important to you, what you’re grateful for, and what you want to achieve.", "https://www.youtube.com/watch?v=L2GjmmbvfYY");
        videoLinks.put("Set new goals. If you’re feeling neutral, it may be a sign that you’re ready for a new challenge consider setting new goals for yourself that align with your values and interest.", "https://www.youtube.com/watch?v=yA53yhiOe04");
        videoLinks.put("Connect with others. Sometime feeling neutral can be a sign that we’re feeling disconnected from others consider reaching out friends and family, or getting involved in a community or social group.", "https://www.youtube.com/watch?v=x1EYcVpQeeE");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "FeelingNotificationChannel";
            String description = "Channel for feeling notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Button notificationButton = findViewById(R.id.notification_button);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] options = {"1 minute", "5 minutes", "10 minutes", "15 minutes", "20 minutes", "30 minutes"};
                AlertDialog.Builder builder = new AlertDialog.Builder(SecondButtonActivity.this);
                builder.setTitle("Select delay time to send notification");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                delayTime = 60 * 1000; // 1 minute
                                break;
                            case 1:
                                delayTime = 5 * 60 * 1000; // 5 minutes
                                break;
                            case 2:
                                delayTime = 10 * 60 * 1000; // 10 minutes
                                break;
                            case 3:
                                delayTime = 15 * 60 * 1000; // 15 minutes
                                break;
                            case 4:
                                delayTime = 20 * 60 * 1000; // 20 minutes
                                break;
                            case 5:
                                delayTime = 30 * 60 * 1000; // 30 minutes
                                break;
                        }
                    }
                });
                builder.show();
            }
        });
    }//end onCreate

    private void swapCamera() {
        //we will change mCameraId if 0 change it to 1, if 1 change it to 0
        mCameraId=mCameraId^1;
        //disable current cameraView
        mOpenCvCameraView.disableView();
        // setCameraIndex
        mOpenCvCameraView.setCameraIndex(mCameraId);
        //enable view
        mOpenCvCameraView.enableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    private CameraBridgeViewBase.CvCameraViewListener2 cvCameraViewListener = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStarted(int width, int height) {
            mRgba=new Mat(height,width, CvType.CV_8UC4);
            mGray=new Mat(height,width,CvType.CV_8UC1);
        }

        @Override
        public void onCameraViewStopped() {
            mRgba.release();
        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            mRgba=inputFrame.rgba();
            mGray=inputFrame.gray();

            String expression = facialExpressionRecognition.detectEmotion(mRgba);

            String[] adviceForAngry = {"Take a step back. When you’re feeling angry, it’s easy to say or do things you’ll regret later. Take step back and try to calm down before responding.", "Identify the source of your anger. Understanding why you’re feeling angry can help you address the root cause of your emotions.", "Practice deep breathing, meditation, yoga, or other relaxation techniques can help you manage your anger in the moment.", "Communicate calmly assertively. When you’re ready to respond, try to communicate calmly and assertively. Use ”I” statements to express how you’re feeling and avoid blaming or attacking the other person.", "Consider seeking professional help. If you’re struggling with persistent feelings of anger or find that your anger is impacting your daily life or relationships, it may be helpful to talk to mental health professional, they can develop strategies to manage your emotions in a healthy way.", "Remember, it’s okay to feel angry sometimes, but it’s important to learn how to manage you’re anger in a way that’s healthy and productive."};
            String[] adviceForDisgust = {"Identify the source of your disgust. Understanding why you’re feeling disgusted can help you address the root cause of your emotions.", "Practice self-care. Take care of yourself can help improve your mood, this could mean getting enough sleep, eating nutritious foods, exercising, and doing things you enjoy.", "Practice acceptance and compassion. It’s important to remember that every one has different experiences and perspectives. Try to practice acceptance and compassion towards yourself and others.", "Try to focus on the positive things in your life such as things you’re grateful for or things that bring you joy.", "Consider seeking professional help. If you’re straggling with persistent feeling of disgust or find that your emotions are impacting your daily life, it may be helpful to talk to a mental health professional.", "Remember ,its okay to feel disgust sometime but if your emotions are impacting your well-being it’s important to seek help."};
            String[] adviceForFear = {"Identify the source of your fear. Understanding why you’re feeling afraid can help you address the root cause of your emotions.", "Practice self-care. Take care of yourself can help improve your mood, this could mean getting enough sleep, eating nutritious foods, exercising, and doing things you enjoy.", "Challenge negative thoughts. Fear can be fueled by negative thought and beliefs. Try to challenge these thoughts and frame them in a more positive or realistic way.", "Face your fears gradually. Avoiding your fears can actually make them worse over time. Try to identify your fears and confront them in a safe and controlled manner.", "Consider seeking professional help. If you’re struggling wit persistent feelings of fear or find that your fear is impacting your daily life. Its may be helpful to talk to a mental health professional."};
            String[] adviceForHappy = {"Share your happiness. Happiness is contagious, and sharing it with others can help spread positive emotions. Consider doing something kind for someone else, or doing volunteer work, or helping others make ends meet, or simply sharing your joy with the people you care about.", "Stay present. When we’re happy, it can be tempting to focus on the future or dwell on the past, but it’s important to stay present and enjoy the moment.", "Practice gratitude. Taking time to appreciate the good things in your life can help boost your mood and extend feelings of happiness. Consider keeping a gratitude journal, or simply taking a few minutes each day to reflect on the things you’re thankful for.", "Take care of yourself. Maintaining good physical and mental health is important for sustaining happiness this could mean getting enough sleep, eating nutritious foods, exercising, taking photos, and doing things you enjoy.", "Set goals and work towards them. Having goals to work towards can give you a sense of purpose and help you maintain a positive outlook. Consider setting achievable goals that align with your values and interest.", "Remember, happiness is a state of mind that requires some effort to maintain by practicing habits regularly, you can cultivate and extend your feelings of happiness."};
            String[] adviceForSad = {"Reach out to someone you trust. Talking to someone you trust about how you’re feeling can be a great way to get some support, or express your gratitude to someone important in your life, which will make you a happier person.", "Practice self-care. Take care of yourself can help improve your mood, this could mean getting enough sleep, eating nutritious foods, exercising, and doing things you enjoy.", "Try to focus on the present. It’s easy to get caught up in negative thoughts and worries about the future or regrets about the part. Try to focusing on the present moment and the things you can control.", "Consider seeking professional help. If you’re struggling with intense or persistent feelings of sadness, it may be helpful to talk to a mental health professional. They can help you work through your emotions and develop coping strategies.", "Do something you love. Engage in hobbies or activities that you find enjoyable or that give you a sense of purpose. This can help to distract you from negative thoughts and feelings and bring more positivity into your life.","Remember, it’s okay to feel sad sometime but if your sadness is impacting your daily life or does not seem to be improving, it’s important to seek help."};
            String[] adviceForSurprise = {"Allow yourself to experience the surprise. Surprise is a natural human emotion, and it’s okay to feel it. Allow yourself to experience the surprise fully without judgment.", "Reflect on the source of your surprise. Understanding why you’re feeling surprised can help you process and integrate the experience. Take some time to reflect on what triggered the surprise.", "Consider the positive aspects. Some time surprise can be positive even if they feel unexpected at first try to consider any positive aspects of the surprise and focus on these.", "Embrace change. Surprise can often bring about change, which can be scary but also exciting. Try to embrace any changes that may come with the surprise and see them as opportunities for growth and new experience.", "Share your surprise with others. Sometimes sharing your surprise with others can help you process the experience and gain new perspective. Consider talking to a trusted friend or family member about your surprise.", "Remember, feeling surprise is normal part of the human experience by allowing yourself to fully experience the surprise, reflecting on its source, and embracing any positive aspects or changes that may come with it, you can move through the surprise in a healthy way."};
            String[] adviceForNeutral = {"Take some time to reflect. Sometime feeling neutral can be a sign that we need to take a step back and reflect on our lives. Consider taking sometimes to reflect on what’s important to you, what you’re grateful for, and what you want to achieve.", "Set new goals. If you’re feeling neutral, it may be a sign that you’re ready for a new challenge consider setting new goals for yourself that align with your values and interest.", "Try something new. Sometimes feeling neutral can be a sign that we’re stuck in a rut. Trying something new can help break up the monotony and add some excitement to your life.", "Practice self-care. Taking care of yourself is important no matter how you’re feeling. Consider doing things that make you feel good, such as taking a warm bath, reading a book, or spending time in nature.", "Connect with others. Sometime feeling neutral can be a sign that we’re feeling disconnected from others consider reaching out friends and family, or getting involved in a community or social group."};

            String advice = "";
            String videoLink = "";
            switch(expression) {
                case "Angry":
                    advice = adviceForAngry[(int) (Math.random() * adviceForAngry.length)];
                    videoLink = videoLinks.get(advice);
                    if (mShouldSendNotification) {
                        // Create a notification builder
                        builder = new NotificationCompat.Builder(SecondButtonActivity.this, CHANNEL_ID)
                                .setSmallIcon(R.mipmap.logo)
                                .setContentTitle("Emotion Detected")
                                .setContentText("You're feeling: " + expression)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("You're feeling: " + expression + "\n" + "Here's some advice: " + advice + "\n" +"Tap to watch a video"))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        builder.setContentIntent(createPendingIntent(advice)); // create a unique PendingIntent for each feeling
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(SecondButtonActivity.this);
                        notificationManager.notify(notificationId++, builder.build());// send the notification
                        mShouldSendNotification = false;
                        // Post a Runnable to reset the flag after 1 minute
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mShouldSendNotification = true;
                            }
                        }, delayTime);
                    }
                    break;
                case "Disgust":
                    advice = adviceForDisgust[(int) (Math.random() * adviceForDisgust.length)];
                    videoLink = videoLinks.get(advice);
                    if (mShouldSendNotification) {
                        // Create a notification builder
                        builder = new NotificationCompat.Builder(SecondButtonActivity.this, CHANNEL_ID)
                                .setSmallIcon(R.mipmap.logo)
                                .setContentTitle("Emotion Detected")
                                .setContentText("You're feeling: " + expression)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("You're feeling: " + expression + "\n" + "Here's some advice: " + advice + "\n" +"Tap to watch a video"))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        builder.setContentIntent(createPendingIntent(advice)); // create a unique PendingIntent for each feeling
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(SecondButtonActivity.this);
                        notificationManager.notify(notificationId++, builder.build());// send the notification
                        mShouldSendNotification = false;
                        // Post a Runnable to reset the flag after 1 minute
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mShouldSendNotification = true;
                            }
                        }, delayTime);
                    }
                    break;
                case "Fear":
                    advice = adviceForFear[(int) (Math.random() * adviceForFear.length)];
                    videoLink = videoLinks.get(advice);
                    if (mShouldSendNotification) {
                        // Create a notification builder
                        builder = new NotificationCompat.Builder(SecondButtonActivity.this, CHANNEL_ID)
                                .setSmallIcon(R.mipmap.logo)
                                .setContentTitle("Emotion Detected")
                                .setContentText("You're feeling: " + expression)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("You're feeling: " + expression + "\n" + "Here's some advice: " + advice + "\n" +"Tap to watch a video"))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        builder.setContentIntent(createPendingIntent(advice)); // create a unique PendingIntent for each feeling
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(SecondButtonActivity.this);
                        notificationManager.notify(notificationId++, builder.build());// send the notification
                        mShouldSendNotification = false;
                        // Post a Runnable to reset the flag after 1 minute
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mShouldSendNotification = true;
                            }
                        }, delayTime);
                    }
                    break;
                case "Happy":
                    advice = adviceForHappy[(int) (Math.random() * adviceForHappy.length)];
                    videoLink = videoLinks.get(advice);
                    if (mShouldSendNotification) {
                        // Create a notification builder
                        builder = new NotificationCompat.Builder(SecondButtonActivity.this, CHANNEL_ID)
                                .setSmallIcon(R.mipmap.logo)
                                .setContentTitle("Emotion Detected")
                                .setContentText("You're feeling: " + expression)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("You're feeling: " + expression + "\n" + "Here's some advice: " + advice + "\n" +"Tap to watch a video"))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        builder.setContentIntent(createPendingIntent(advice)); // create a unique PendingIntent for each feeling
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(SecondButtonActivity.this);
                        notificationManager.notify(notificationId++, builder.build());// send the notification
                        mShouldSendNotification = false;
                        // Post a Runnable to reset the flag after 1 minute
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mShouldSendNotification = true;
                            }
                        }, delayTime);
                    }
                    break;
                case "Sad":
                    advice = adviceForSad[(int) (Math.random() * adviceForSad.length)];
                    videoLink = videoLinks.get(advice);
                    if (mShouldSendNotification) {
                        // Create a notification builder
                        builder = new NotificationCompat.Builder(SecondButtonActivity.this, CHANNEL_ID)
                                .setSmallIcon(R.mipmap.logo)
                                .setContentTitle("Emotion Detected")
                                .setContentText("You're feeling: " + expression)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("You're feeling: " + expression + "\n" + "Here's some advice: " + advice + "\n" +"Tap to watch a video"))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        builder.setContentIntent(createPendingIntent(advice)); // create a unique PendingIntent for each feeling
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(SecondButtonActivity.this);
                        notificationManager.notify(notificationId++, builder.build());// send the notification
                        mShouldSendNotification = false;
                        // Post a Runnable to reset the flag after 1 minute
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mShouldSendNotification = true;
                            }
                        }, delayTime);
                    }
                    break;
                case "Surprise":
                    advice = adviceForSurprise[(int) (Math.random() * adviceForSurprise.length)];
                    videoLink = videoLinks.get(advice);
                    if (mShouldSendNotification) {
                        // Create a notification builder
                        builder = new NotificationCompat.Builder(SecondButtonActivity.this, CHANNEL_ID)
                                .setSmallIcon(R.mipmap.logo)
                                .setContentTitle("Emotion Detected")
                                .setContentText("You're feeling: " + expression)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("You're feeling: " + expression + "\n" + "Here's some advice: " + advice + "\n" +"Tap to watch a video"))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        builder.setContentIntent(createPendingIntent(advice)); // create a unique PendingIntent for each feeling
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(SecondButtonActivity.this);
                        notificationManager.notify(notificationId++, builder.build());// send the notification
                        mShouldSendNotification = false;
                        // Post a Runnable to reset the flag after 1 minute
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mShouldSendNotification = true;
                            }
                        }, delayTime);
                    }
                    break;
                case "Neutral":
                    advice = adviceForNeutral[(int) (Math.random() * adviceForNeutral.length)];
                    videoLink = videoLinks.get(advice);
                    if (mShouldSendNotification) {
                        // Create a notification builder
                        builder = new NotificationCompat.Builder(SecondButtonActivity.this, CHANNEL_ID)
                                .setSmallIcon(R.mipmap.logo)
                                .setContentTitle("Emotion Detected")
                                .setContentText("You're feeling: " + expression)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText("You're feeling: " + expression + "\n" + "Here's some advice: " + advice + "\n" +"Tap to watch a video"))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        builder.setContentIntent(createPendingIntent(advice)); // create a unique PendingIntent for each feeling
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(SecondButtonActivity.this);
                        notificationManager.notify(notificationId++, builder.build());// send the notification
                        mShouldSendNotification = false;
                        // Post a Runnable to reset the flag after 1 minute
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mShouldSendNotification = true;
                            }
                        }, delayTime);
                    }
                    break;
            }
            // when mCameraId is 1 (front) rotate camera frame with 180 degree
            if(mCameraId==1){
                Core.flip(mRgba,mRgba,1);
                Core.flip(mGray,mGray,-1);
            }
            return mRgba;
        }
    };

    private PendingIntent createPendingIntent(String advice) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoLinks.get(advice))); // opens a URL in a web browser when the notification is tapped
        return PendingIntent.getActivity(this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library loaded");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }
    }
}//end SecondButtonActivity class
