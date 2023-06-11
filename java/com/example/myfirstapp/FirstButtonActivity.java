package com.example.myfirstapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.core.Rect;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

public class FirstButtonActivity extends AppCompatActivity {
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private static final int PERMISSION_CODE_ = 1001;
    private static final int IMAGE_PICK_CODE = 1000;
    Button mCaptureBtn, mChooseBtn, mProcess;
    ImageView mImageView, hidden;
    Uri image_uri;
    Bitmap mBitmap;
    Boolean ready = false;
    //define interpreter
    private Interpreter interpreter;
    //define input size
    private int INPUT_SIZE;
    //define GpuDelegate that use to implement gpu in interpreter
    private GpuDelegate gpuDelegate=null;
    //define cascadeClassifier for face detection
    private CascadeClassifier cascadeClassifier;
    private HashMap<String, String> videoLinks = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstbtn_layout); //to link the layout with the activity
        hidden = findViewById(R.id.hidden);
        mImageView= findViewById(R.id.imageView); //to get component from xml by its id, R mean resource
        mImageView.setVisibility(View.INVISIBLE);
        mProcess= findViewById(R.id.process_image_btn); //to get component from xml by its id, R mean resource
        mCaptureBtn= findViewById(R.id.capture_image_btn); //to get component from xml by its id, R mean resource
        mChooseBtn= findViewById(R.id.choose_image_btn); //to get component from xml by its id, R mean resource
        //button click
        mCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MediaPlayer mp= MediaPlayer.create(FirstButtonActivity.this,R.raw.clicksoundeffect);//object for media file
                mp.start();//play the sound
                //if system os is >= marshmallow, request runtime permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED) {
                        //permission not enabled, request it
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        //show popup to request permissions
                        requestPermissions(permission, PERMISSION_CODE);
                    } else {
                        //permission already granted
                        openCamera();
                    }//end else
                }//end if
                else {
                    //system os is less than marshmallow
                    openCamera();
                }//end else
            }//end onClick
            }); //to make the object clickable

        //button click
        mChooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MediaPlayer mp= MediaPlayer.create(FirstButtonActivity.this,R.raw.clicksoundeffect);//object for media file
                mp.start();//play the sound
                //check runtime permission
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                        //permission not granted, request it
                        String [] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        //show popup for runtime permission
                        requestPermissions(permissions, PERMISSION_CODE_);
                    }//end if
                    else {
                          //permission already granted
                           pickImageFromGallery();
                    }//end else
                }//end if
                else{
                    //system os is less than marshmallow
                    pickImageFromGallery();
                }//end else
            }//end onClick
        }); //to make the object clickable
        INPUT_SIZE=48;
        //set GPU for the interpreter
        Interpreter.Options options=new Interpreter.Options();
        gpuDelegate=new GpuDelegate();
        //add gpuDelegate to option
        options.addDelegate(gpuDelegate);
        //set number of threads to options
        options.setNumThreads(4);
        //load model weight to interpreter
        try {
            interpreter=new Interpreter(loadModelFile(getAssets(),"tf_lite_model.tflite"),options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //if model is load print
        Log.d("facial_Expression","Model is loaded");

        //load haarCascade classifier
        try {
            //define input stream to read classifier
            InputStream is=FirstButtonActivity.this.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            //create a folder
            File cascadeDir=FirstButtonActivity.this.getDir("cascade",Context.MODE_PRIVATE);
            //create a new file in that folder
            File mCascadeFile=new File(cascadeDir,"haarcascade_frontalface_alt");
            //define output stream to transfer data to file we created
            FileOutputStream os=new FileOutputStream(mCascadeFile);
            //create buffer to store byte
            byte[] buffer=new byte[4096];
            int byteRead;
            //read byte in while loop when it read -1 that mean no data to read
            while ((byteRead=is.read(buffer)) != -1){
                //write on mCascade file
                os.write(buffer,0,byteRead);
            }
            //close input and output stream
            is.close();
            os.close();
            cascadeClassifier=new CascadeClassifier(mCascadeFile.getAbsolutePath());
            //if cascade file is loaded print
            Log.d("facial_Expression","Classifier is loaded");
        }
        catch (IOException e){
            e.printStackTrace();
        }

        mProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MediaPlayer mp= MediaPlayer.create(FirstButtonActivity.this,R.raw.clicksoundeffect);//object for media file
                mp.start();//play the sound
                if (ready) {
                        detectAndFrame(mBitmap);
                } else {
                       makeToast("Please capture or choose a photo");
                }
            }//end onClick
        });
        videoLinks.put("Practice deep breathing, meditation, yoga, or other relaxation techniques can help you manage your anger in the moment.", "https://www.youtube.com/watch?v=wkse4PPxkk4");
        videoLinks.put("Try to focus on the present. It’s easy to get caught up in negative thoughts and worries about the future or regrets about the part. Try to focusing on the present moment and the things you can control.", "https://www.youtube.com/watch?v=YkOlMKD8b7Y");
        videoLinks.put("Stay present. When we’re happy, it can be tempting to focus on the future or dwell on the past, but it’s important to stay present and enjoy the moment.", "https://www.youtube.com/watch?v=YkOlMKD8b7Y");
        videoLinks.put("Reach out to someone you trust. Talking to someone you trust about how you’re feeling can be a great way to get some support, or express your gratitude to someone important in your life, which will make you a happier person.", "https://www.youtube.com/watch?v=oHv6vTKD6lg");
        videoLinks.put("Practice self-care. Take care of yourself can help improve your mood, this could mean getting enough sleep, eating nutritious foods, exercising, and doing things you enjoy.", "https://www.youtube.com/watch?v=k7X7sZzSXYs");
        videoLinks.put("Try something new. Sometimes feeling neutral can be a sign that we’re stuck in a rut. Trying something new can help break up the monotony and add some excitement to your life.", "https://www.youtube.com/watch?v=ATHb_LskphI");
        videoLinks.put("Take care of yourself. Maintaining good physical and mental health is important for sustaining happiness this could mean getting enough sleep, eating nutritious foods, exercising, taking photos, and doing things you enjoy.", "https://www.youtube.com/watch?v=iz0yM5YoIow");
        videoLinks.put("Do something you love. Engage in hobbies or activities that you find enjoyable or that give you a sense of purpose. This can help to distract you from negative thoughts and feelings and bring more positivity into your life.", "https://www.youtube.com/watch?v=KVymp-xjYKg&t=63s");
        videoLinks.put("Practice self-care. Taking care of yourself is important no matter how you’re feeling. Consider doing things that make you feel good, such as taking a warm bath, reading a book, or spending time in nature.", "https://www.youtube.com/watch?v=W7_lafxj8ok");
        videoLinks.put("Face your fears gradually. Avoiding your fears can actually make them worse over time. Try to identify your fears and confront them in a safe and controlled manner.", "https://www.youtube.com/watch?v=1PV7Hy_8fhA");
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
        videoLinks.put("Practice acceptance and compassion. It’s important to remember that every one has different experiences and perspectives. Try to practice acceptance and compassion towards yourself and others.","https://www.youtube.com/watch?v=ZRnxyxdpkko");
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
        videoLinks.put("Embrace change. Surprise can often bring about change, which can be scary but also exciting. Try to embrace any changes that may come with the surprise and see them as opportunities for growth and new experience.", "https://www.youtube.com/watch?v=pUmTQ-86-YI");
        videoLinks.put("Share your surprise with others. Sometimes sharing your surprise with others can help you process the experience and gain new perspective. Consider talking to a trusted friend or family member about your surprise.", "https://www.youtube.com/watch?v=WNdPA7OVPgM");
        videoLinks.put("Remember, feeling surprise is normal part of the human experience by allowing yourself to fully experience the surprise, reflecting on its source, and embracing any positive aspects or changes that may come with it, you can move through the surprise in a healthy way.", "https://www.youtube.com/watch?v=MOQcSzkKfbs");
        videoLinks.put("Take some time to reflect. Sometime feeling neutral can be a sign that we need to take a step back and reflect on our lives. Consider taking sometimes to reflect on what’s important to you, what you’re grateful for, and what you want to achieve.", "https://www.youtube.com/watch?v=L2GjmmbvfYY");
        videoLinks.put("Set new goals. If you’re feeling neutral, it may be a sign that you’re ready for a new challenge consider setting new goals for yourself that align with your values and interest.", "https://www.youtube.com/watch?v=yA53yhiOe04");
        videoLinks.put("Connect with others. Sometime feeling neutral can be a sign that we’re feeling disconnected from others consider reaching out friends and family, or getting involved in a community or social group.", "https://www.youtube.com/watch?v=WKUgVpCqvfY");
    }//end onCreate

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException{
        //give description of file
        AssetFileDescriptor assetFileDescriptor=assetManager.openFd(modelPath);
        //create a inputStream to read file
        FileInputStream inputStream=new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();

        long startOffset=assetFileDescriptor.getStartOffset();
        long declaredLength=assetFileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
    }//end loadModelFile

    private int argmax(float[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }//end argmax

    public String getEmotion(float[][] emotion){
        String emotion_s = "";
        switch (argmax(emotion[0])) {
            case 0:
                emotion_s = "Angry";
                break;
            case 1:
                emotion_s = "Disgust";
                break;
            case 2:
                emotion_s = "Fear";
                break;
            case 3:
                emotion_s = "Happy";
                break;
            case 4:
                emotion_s = "Sad";
                break;
            case 5:
                emotion_s = "Surprise";
                break;
            case 6:
                emotion_s = "Neutral";
                break;
        }
        return emotion_s;
    }//end getEmotion

    private String getAdvice(String expression) {
        String[] adviceForAngry = {"Take a step back. When you’re feeling angry, it’s easy to say or do things you’ll regret later. Take step back and try to calm down before responding.", "Identify the source of your anger. Understanding why you’re feeling angry can help you address the root cause of your emotions.", "Practice deep breathing, meditation, yoga, or other relaxation techniques can help you manage your anger in the moment.", "Communicate calmly assertively. When you’re ready to respond, try to communicate calmly and assertively. Use ”I” statements to express how you’re feeling and avoid blaming or attacking the other person.", "Consider seeking professional help. If you’re struggling with persistent feelings of anger or find that your anger is impacting your daily life or relationships, it may be helpful to talk to mental health professional, they can develop strategies to manage your emotions in a healthy way.", "Remember, it’s okay to feel angry sometimes, but it’s important to learn how to manage you’re anger in a way that’s healthy and productive."};
        String[] adviceForDisgust = {"Identify the source of your disgust. Understanding why you’re feeling disgusted can help you address the root cause of your emotions.", "Practice self-care. Take care of yourself can help improve your mood, this could mean getting enough sleep, eating nutritious foods, exercising, and doing things you enjoy.", "Practice acceptance and compassion. It’s important to remember that every one has different experiences and perspectives. Try to practice acceptance and compassion towards yourself and others.", "Try to focus on the positive things in your life such as things you’re grateful for or things that bring you joy.", "Consider seeking professional help. If you’re straggling with persistent feeling of disgust or find that your emotions are impacting your daily life, it may be helpful to talk to a mental health professional.", "Remember ,its okay to feel disgust sometime but if your emotions are impacting your well-being it’s important to seek help."};
        String[] adviceForFear = {"Identify the source of your fear. Understanding why you’re feeling afraid can help you address the root cause of your emotions.", "Practice self-care. Take care of yourself can help improve your mood, this could mean getting enough sleep, eating nutritious foods, exercising, and doing things you enjoy.", "Challenge negative thoughts. Fear can be fueled by negative thought and beliefs. Try to challenge these thoughts and frame them in a more positive or realistic way.", "Face your fears gradually. Avoiding your fears can actually make them worse over time. Try to identify your fears and confront them in a safe and controlled manner.", "Consider seeking professional help. If you’re struggling wit persistent feelings of fear or find that your fear is impacting your daily life. Its may be helpful to talk to a mental health professional."};
        String[] adviceForHappy = {"Share your happiness. Happiness is contagious, and sharing it with others can help spread positive emotions. Consider doing something kind for someone else, or doing volunteer work, or helping others make ends meet, or simply sharing your joy with the people you care about.", "Stay present. When we’re happy, it can be tempting to focus on the future or dwell on the past, but it’s important to stay present and enjoy the moment.", "Practice gratitude. Taking time to appreciate the good things in your life can help boost your mood and extend feelings of happiness. Consider keeping a gratitude journal, or simply taking a few minutes each day to reflect on the things you’re thankful for.", "Take care of yourself. Maintaining good physical and mental health is important for sustaining happiness this could mean getting enough sleep, eating nutritious foods, exercising, taking photos, and doing things you enjoy.", "Set goals and work towards them. Having goals to work towards can give you a sense of purpose and help you maintain a positive outlook. Consider setting achievable goals that align with your values and interest.", "Remember, happiness is a state of mind that requires some effort to maintain by practicing habits regularly, you can cultivate and extend your feelings of happiness."};
        String[] adviceForSad = {"Reach out to someone you trust. Talking to someone you trust about how you’re feeling can be a great way to get some support, or express your gratitude to someone important in your life, which will make you a happier person.", "Practice self-care. Take care of yourself can help improve your mood, this could mean getting enough sleep, eating nutritious foods, exercising, and doing things you enjoy.", "Try to focus on the present. It’s easy to get caught up in negative thoughts and worries about the future or regrets about the part. Try to focusing on the present moment and the things you can control.", "Consider seeking professional help. If you’re struggling with intense or persistent feelings of sadness, it may be helpful to talk to a mental health professional. They can help you work through your emotions and develop coping strategies.", "Do something you love. Engage in hobbies or activities that you find enjoyable or that give you a sense of purpose. This can help to distract you from negative thoughts and feelings and bring more positivity into your life.","Remember, it’s okay to feel sad sometime but if your sadness is impacting your daily life or does not seem to be improving, it’s important to seek help."};
        String[] adviceForSurprise = {"Allow yourself to experience the surprise. Surprise is a natural human emotion, and it’s okay to feel it. Allow yourself to experience the surprise fully without judgment.", "Reflect on the source of your surprise. Understanding why you’re feeling surprised can help you process and integrate the experience. Take some time to reflect on what triggered the surprise.", "Consider the positive aspects. Some time surprise can be positive even if they feel unexpected at first try to consider any positive aspects of the surprise and focus on these.", "Embrace change. Surprise can often bring about change, which can be scary but also exciting. Try to embrace any changes that may come with the surprise and see them as opportunities for growth and new experience.", "Share your surprise with others. Sometimes sharing your surprise with others can help you process the experience and gain new perspective. Consider talking to a trusted friend or family member about your surprise.", "Remember, feeling surprise is normal part of the human experience by allowing yourself to fully experience the surprise, reflecting on its source, and embracing any positive aspects or changes that may come with it, you can move through the surprise in a healthy way."};
        String[] adviceForNeutral = {"Take some time to reflect. Sometime feeling neutral can be a sign that we need to take a step back and reflect on our lives. Consider taking sometimes to reflect on what’s important to you, what you’re grateful for, and what you want to achieve.", "Set new goals. If you’re feeling neutral, it may be a sign that you’re ready for a new challenge consider setting new goals for yourself that align with your values and interest.", "Try something new. Sometimes feeling neutral can be a sign that we’re stuck in a rut. Trying something new can help break up the monotony and add some excitement to your life.", "Practice self-care. Taking care of yourself is important no matter how you’re feeling. Consider doing things that make you feel good, such as taking a warm bath, reading a book, or spending time in nature.", "Connect with others. Sometime feeling neutral can be a sign that we’re feeling disconnected from others consider reaching out friends and family, or getting involved in a community or social group."};

        String advice = "";
        switch (expression) {
            case "Angry":
                advice = adviceForAngry[(int) (Math.random() * adviceForAngry.length)];
                break;
            case "Disgust":
                advice = adviceForDisgust[(int) (Math.random() * adviceForDisgust.length)];
                break;
            case "Fear":
                advice = adviceForFear[(int) (Math.random() * adviceForFear.length)];
                break;
            case "Happy":
                advice = adviceForHappy[(int) (Math.random() * adviceForHappy.length)];
                break;
            case "Sad":
                advice = adviceForSad[(int) (Math.random() * adviceForSad.length)];
                break;
            case "Surprise":
                advice = adviceForSurprise[(int) (Math.random() * adviceForSurprise.length)];
                break;
            case "Neutral":
                advice = adviceForNeutral[(int) (Math.random() * adviceForNeutral.length)];
                break;
        }
        return advice;
    }//end getAdvice

    private String getVideo(String advice) {
        String videoLink = "";
        switch (advice) {
            case "Practice deep breathing, meditation, yoga, or other relaxation techniques can help you manage your anger in the moment.":
            case "Try to focus on the present. It’s easy to get caught up in negative thoughts and worries about the future or regrets about the part. Try to focusing on the present moment and the things you can control.":
            case "Stay present. When we’re happy, it can be tempting to focus on the future or dwell on the past, but it’s important to stay present and enjoy the moment.":
            case "Reach out to someone you trust. Talking to someone you trust about how you’re feeling can be a great way to get some support, or express your gratitude to someone important in your life, which will make you a happier person.":
            case "Practice self-care. Take care of yourself can help improve your mood, this could mean getting enough sleep, eating nutritious foods, exercising, and doing things you enjoy.":
            case "Try something new. Sometimes feeling neutral can be a sign that we’re stuck in a rut. Trying something new can help break up the monotony and add some excitement to your life.":
            case "Take care of yourself. Maintaining good physical and mental health is important for sustaining happiness this could mean getting enough sleep, eating nutritious foods, exercising, taking photos, and doing things you enjoy.":
            case "Do something you love. Engage in hobbies or activities that you find enjoyable or that give you a sense of purpose. This can help to distract you from negative thoughts and feelings and bring more positivity into your life.":
            case "Practice self-care. Taking care of yourself is important no matter how you’re feeling. Consider doing things that make you feel good, such as taking a warm bath, reading a book, or spending time in nature.":
            case "Face your fears gradually. Avoiding your fears can actually make them worse over time. Try to identify your fears and confront them in a safe and controlled manner.":
            case "Challenge negative thoughts. Fear can be fueled by negative thought and beliefs. Try to challenge these thoughts and frame them in a more positive or realistic way.":
            case "Identify the source of your fear. Understanding why you’re feeling afraid can help you address the root cause of your emotions.":
            case "Consider seeking professional help. If you’re struggling with intense or persistent feelings of sadness, it may be helpful to talk to a mental health professional. They can help you work through your emotions and develop coping strategies.":
            case "Remember, it’s okay to feel sad sometime but if your sadness is impacting your daily life or does not seem to be improving, it’s important to seek help.":
            case "Take a step back. When you’re feeling angry, it’s easy to say or do things you’ll regret later. Take step back and try to calm down before responding.":
            case "Identify the source of your anger. Understanding why you’re feeling angry can help you address the root cause of your emotions.":
            case "Communicate calmly assertively. When you’re ready to respond, try to communicate calmly and assertively. Use ”I” statements to express how you’re feeling and avoid blaming or attacking the other person.":
            case "Consider seeking professional help. If you’re struggling with persistent feelings of anger or find that your anger is impacting your daily life or relationships, it may be helpful to talk to mental health professional, they can develop strategies to manage your emotions in a healthy way.":
            case "Remember, it’s okay to feel angry sometimes, but it’s important to learn how to manage you’re anger in a way that’s healthy and productive.":
            case "Identify the source of your disgust. Understanding why you’re feeling disgusted can help you address the root cause of your emotions.":
            case "Practice acceptance and compassion. It’s important to remember that every one has different experiences and perspectives. Try to practice acceptance and compassion towards yourself and others.":
            case "Try to focus on the positive things in your life such as things you’re grateful for or things that bring you joy.":
            case "Consider seeking professional help. If you’re straggling with persistent feeling of disgust or find that your emotions are impacting your daily life, it may be helpful to talk to a mental health professional.":
            case "Remember ,its okay to feel disgust sometime but if your emotions are impacting your well-being it’s important to seek help.":
            case "Consider seeking professional help. If you’re struggling wit persistent feelings of fear or find that your fear is impacting your daily life. Its may be helpful to talk to a mental health professional.":
            case "Share your happiness. Happiness is contagious, and sharing it with others can help spread positive emotions. Consider doing something kind for someone else, or doing volunteer work, or helping others make ends meet, or simply sharing your joy with the people you care about.":
            case "Practice gratitude. Taking time to appreciate the good things in your life can help boost your mood and extend feelings of happiness. Consider keeping a gratitude journal, or simply taking a few minutes each day to reflect on the things you’re thankful for.":
            case "Set goals and work towards them. Having goals to work towards can give you a sense of purpose and help you maintain a positive outlook. Consider setting achievable goals that align with your values and interest.":
            case "Remember, happiness is a state of mind that requires some effort to maintain by practicing habits regularly, you can cultivate and extend your feelings of happiness.":
            case "Allow yourself to experience the surprise. Surprise is a natural human emotion, and it’s okay to feel it. Allow yourself to experience the surprise fully without judgment.":
            case "Reflect on the source of your surprise. Understanding why you’re feeling surprised can help you process and integrate the experience. Take some time to reflect on what triggered the surprise.":
            case "Consider the positive aspects. Some time surprise can be positive even if they feel unexpected at first try to consider any positive aspects of the surprise and focus on these.":
            case "Embrace change. Surprise can often bring about change, which can be scary but also exciting. Try to embrace any changes that may come with the surprise and see them as opportunities for growth and new experience.":
            case "Share your surprise with others. Sometimes sharing your surprise with others can help you process the experience and gain new perspective. Consider talking to a trusted friend or family member about your surprise.":
            case "Remember, feeling surprise is normal part of the human experience by allowing yourself to fully experience the surprise, reflecting on its source, and embracing any positive aspects or changes that may come with it, you can move through the surprise in a healthy way.":
            case "Take some time to reflect. Sometime feeling neutral can be a sign that we need to take a step back and reflect on our lives. Consider taking sometimes to reflect on what’s important to you, what you’re grateful for, and what you want to achieve.":
            case "Set new goals. If you’re feeling neutral, it may be a sign that you’re ready for a new challenge consider setting new goals for yourself that align with your values and interest.":
            case "Connect with others. Sometime feeling neutral can be a sign that we’re feeling disconnected from others consider reaching out friends and family, or getting involved in a community or social group.":
                videoLink = videoLinks.get(advice);
                break;
            default:
                videoLink = "There is no video link for this advice";
        }
        return videoLink;
    }//end getVideo
    private ByteBuffer convertBitmapToByteBuffer(Bitmap scaledBitmap) {
        ByteBuffer byteBuffer;
        int size_image=INPUT_SIZE;//48
        byteBuffer=ByteBuffer.allocateDirect(4 * 1 * size_image * size_image * 3);
        //4 is multiplied for float input
        //3 is multiplied for rgb
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues=new int[size_image*size_image];
        scaledBitmap.getPixels(intValues,0,scaledBitmap.getWidth(),0,0,scaledBitmap.getWidth(),scaledBitmap.getHeight());
        int pixel=0;
        for(int i=0; i<size_image;++i){
            for(int j=0; j<size_image;++j){
                final int val=intValues[pixel++];
                //put float value to bytebuffer
                //scale image to convert image from 0-255 to 0-1
                byteBuffer.putFloat((((val>>16)&0xFF))/255.0f);
                byteBuffer.putFloat((((val>>8)&0xFF))/255.0f);
                byteBuffer.putFloat(((val & 0xFF))/255.0f);
            }
        }
        return byteBuffer;
    }//end convertBitmapToByteBuffer

    private void detectAndFrame(final Bitmap mBitmap) {
        // Show progress dialog
        final ProgressDialog progressDialog = ProgressDialog.show(FirstButtonActivity.this, "", "Detecting...", true);
        // Run face detection on a separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {
        // Convert the bitmap to a Mat object
        Mat mat = new Mat();
        Utils.bitmapToMat(mBitmap, mat);
        // Convert the Mat object to grayscale
        Mat grayMat = new Mat();
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY);
        // Detect the face in the grayscale image
        MatOfRect faces = new MatOfRect();
        cascadeClassifier.detectMultiScale(grayMat, faces);
        // Dismiss the ProgressDialog
        progressDialog.dismiss();
        // Check if a face is detected in the image
        if (faces.toArray().length != 0) {
            // Extract the face from the image
            Rect rect = faces.toArray()[0];
            // Crop the original image to the detected face
            Bitmap croppedBitmap = Bitmap.createBitmap(mBitmap, rect.x, rect.y, rect.width, rect.height);
            // Resize the cropped image to 48 x 48
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, INPUT_SIZE, INPUT_SIZE, false);
             // Add the processed image to the intent
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            //convert scaledBitmap to byteBuffer
            ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);
            int numClasses = 7;
            //create an object to hold output
            float[][] emotion = new float[1][numClasses];
            //predict with bytebuffer as an input and emotion as an output
            interpreter.run(byteBuffer, emotion);
            //define float value of emotion
            float emotion_v = (float) Array.get(Array.get(emotion, 0), 0);
            //if emotion is recognize print value of it
            Log.d("facial_expression", "Output: " + emotion_v);
            String emotion_t = getEmotion(emotion);
            String advice_s = getAdvice(emotion_t);
            String video_s = getVideo(advice_s);
            // Create a new intent to launch the new activity
            Intent intent = new Intent(FirstButtonActivity.this, ResultActivity.class);
            // Add the processed image, the detected feeling, and the advice to the intent
            intent.putExtra("image", byteArray);
            intent.putExtra("feeling",emotion_t);
            intent.putExtra("advice", advice_s);
            intent.putExtra("video", video_s);
            // Launch the new activity
            startActivity(intent);
        }else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "No face detected in the image", Toast.LENGTH_LONG).show();
                }
            });
        }
            }
        }).start();
    }//end detectAndFrame

    private void makeToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }//end makeToast

    private void pickImageFromGallery() {
      //intent to pick image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }//end pickImageFromGallery

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put (MediaStore.Images.Media.TITLE, "New Picture");
        values.put (MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri= getContentResolver().insert (MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //Camera intent
        Intent cameraIntent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra (MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }//end openCamera

    //handling permissions result
    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //this method is called, when user presses Allow or Deny from Permission Request Popup
        switch (requestCode){
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    //permission from popup was granted
                    openCamera();
                } else {
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
                }//end else
            }//end case1
            case PERMISSION_CODE_:{
                if (grantResults.length >0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    //permission was granted
                    pickImageFromGallery();
                } else {
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
                }//end else
            }//end case2
        }// end switch
    }//end onRequestPermissionsResult

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //called when image was captured from camera
        if (resultCode == RESULT_OK && requestCode == IMAGE_CAPTURE_CODE) {
            //set the image captured to our ImageView
            mImageView.setVisibility(View.VISIBLE);
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), image_uri);
                Bitmap rotatedBitmap = mBitmap;
                ExifInterface ei = new ExifInterface(getRealPathFromURI(image_uri));
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                switch (orientation) {

                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBitmap = rotateImage(mBitmap, 90);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBitmap = rotateImage(mBitmap, 180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotatedBitmap = rotateImage(mBitmap, 270);
                        break;

                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        rotatedBitmap = mBitmap;
                }
                mImageView.setImageBitmap(rotatedBitmap);

            } catch (IOException e) {
                //Error getting high quality image --> Use low quality thumbnail.
                makeToast("Error: " + e.toString());
                //mBitmap = (Bitmap) data.getExtras().get("data");
                e.printStackTrace();
                mImageView.setImageBitmap(mBitmap);
            }
            ready = true;
            hidden.setVisibility(View.INVISIBLE);
               // mImageView.setImageURI(image_uri);
        }//end if
        else if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            //set the image captured to our ImageView
            mImageView.setVisibility(View.VISIBLE);
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), data.getData());
                Bitmap rotatedBitmap = mBitmap;
                ExifInterface ei = new ExifInterface(getRealPathFromURI(data.getData()));
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                switch (orientation) {

                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBitmap = rotateImage(mBitmap, 90);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBitmap = rotateImage(mBitmap, 180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotatedBitmap = rotateImage(mBitmap, 270);
                        break;

                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        rotatedBitmap = mBitmap;
                }
                mImageView.setImageBitmap(rotatedBitmap);

            } catch (IOException e) {
                //Error getting high quality image --> Use low quality thumbnail.
                makeToast("Error: " + e.toString());
                //mBitmap = (Bitmap) data.getExtras().get("data");
                e.printStackTrace();
                mImageView.setImageBitmap(mBitmap);
            }
            ready = true;
            hidden.setVisibility(View.INVISIBLE);
            //set image to image view
           // mImageView.setImageURI(data.getData());
        }//end else if
    }//end onActivityResult
    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
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
}//end FirstButtonActivity class