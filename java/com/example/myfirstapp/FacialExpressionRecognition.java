package com.example.myfirstapp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

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


public class FacialExpressionRecognition {
    //define interpreter
    private Interpreter interpreter;
    //define input size
    private int INPUT_SIZE;
    //define height and width of original frame
    private int height=0;
    private int width=0;
    //define GpuDelegate that use to implement gpu in interpreter
    private GpuDelegate gpuDelegate=null;
    //define cascadeClassifier for face detection
    private CascadeClassifier cascadeClassifier;
    FacialExpressionRecognition(AssetManager assetManager, Context context, String modelPath, int inputSize) throws IOException {
        INPUT_SIZE=inputSize;
        //set GPU for the interpreter
        Interpreter.Options options=new Interpreter.Options();
        gpuDelegate=new GpuDelegate();
        //add gpuDelegate to option
        options.addDelegate(gpuDelegate);
        //set number of threads to options
        options.setNumThreads(4);
        //load model weight to interpreter
        interpreter=new Interpreter(loadModelFile(assetManager,modelPath),options);
        //if model is load print
        Log.d("facial_Expression","Model is loaded");

        //load haarCascade classifier
        try {
            //define input stream to read classifier
            InputStream is=context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            //create a folder
            File cascadeDir=context.getDir("cascade",Context.MODE_PRIVATE);
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
    }
    private int argmax(float[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
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
   }

    public String detectEmotion(Mat mat_image) {
        //before predicting we have to rotate image by 90 degree for proper prediction
        Core.flip(mat_image.t(),mat_image,1); //rotate mat_image by 90 degree
        //start with our process
        //convert mat_image to gray scale image
        Mat grayscaleImage = new Mat();
        Imgproc.cvtColor(mat_image,grayscaleImage,Imgproc.COLOR_RGBA2GRAY);
        //set height and width
        height=grayscaleImage.height();
        width=grayscaleImage.width();

        //define minimum height of face in original image
        int absoluteFaceSize=(int)(height*0.1);
        //create MatOfRect to store face
        MatOfRect faces=new MatOfRect();
        //check if cascadeClassifier is loaded or not
        if (cascadeClassifier !=null){
            //detect face in frame
            //                                  input          output                                  minimum size
            cascadeClassifier.detectMultiScale(grayscaleImage,faces,1.1,2,2,new Size(absoluteFaceSize,absoluteFaceSize),new Size());
        }
        //convert it to array
        Rect[] faceArray=faces.toArray();
        //loop through each face
        String emotion_t = "";
        for(int i=0;i<faceArray.length;i++){
            //draw rectangle around face
            //                input/output starting point ending point          color  R G   B alpha  thickness
            Imgproc.rectangle(mat_image,faceArray[i].tl(),faceArray[i].br(),new Scalar(0,255,0,255),2);
            //crop face from original frame and grayscaleImage
            //                 starting x coordinate      starting y coordinate
            Rect roi=new Rect((int)faceArray[i].tl().x,(int)faceArray[i].tl().y,
                    ((int)faceArray[i].br().x)-(int)(faceArray[i].tl().x),
                    ((int)faceArray[i].br().y)-(int)(faceArray[i].tl().y));
            Mat cropped_rgba=new Mat(mat_image,roi);
            //convert cropped_rgba to bitmap
            Bitmap bitmap=null;
            bitmap=Bitmap.createBitmap(cropped_rgba.cols(),cropped_rgba.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(cropped_rgba,bitmap);
            //resize bitmap to (48,48)
            Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap,48,48,false);
            //convert scaledBitmap to byteBuffer
            ByteBuffer byteBuffer=convertBitmapToByteBuffer(scaledBitmap);
            int numClasses = 7;
            //create an object to hold output
            float[][] emotion = new float[1][numClasses];
            //predict with bytebuffer as an input and emotion as an output
            interpreter.run(byteBuffer, emotion);
            //define float value of emotion
            float emotion_v=(float) Array.get(Array.get(emotion,0),0);
            //if emotion is recognize print value of it
            Log.d("facial_expression","Output: "+ emotion_v);
            emotion_t = getEmotion(emotion);
            //put text on original frame(mat_image)
            //              input/output             text: Angry (2.934234)        starting x coordinate            starting y coordinate              use to scale text   color R G  B  alpha thickness
            Imgproc.putText(mat_image,emotion_t+" ("+emotion_v+")",new Point((int)faceArray[i].tl().x-10,(int)faceArray[i].tl().y-20),1,1.6,new Scalar(0,0,255,150),2);
        }
        //after prediction rotate mat_image -90 degree
        Core.flip(mat_image.t(),mat_image,0);
        return emotion_t;
        }

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
    }
    //use to load model
    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException{
        //give description of file
        AssetFileDescriptor assetFileDescriptor=assetManager.openFd(modelPath);
        //create a inputStream to read file
        FileInputStream inputStream=new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=assetFileDescriptor.getStartOffset();
        long declaredLength=assetFileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
    }
}
