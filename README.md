# Real-Time-Facial-Emotion-Recognition-Using-AI (Android App) (Graduation Project)
Facial emotion recognition (FER) is a technology that analyzes facial expressions from images or videos to identify an individual's emotional state. One of the most important challenges faced by current artificial intelligence (AI) based mobile applications in recognizing facial emotions is bias as there is a lack of representation of faces wearing hijabs and Middle Eastern faces in the databases of faces used in available applications, which leads to inaccurate results of these applications. The proposed project aims to build a real-time emotion analysis application focusing on Middle Eastern faces particularly women wearing hijabs to increase its usability in the Arab world.

The traditional method of emotion recognition may not meet the need of mobile application users for their emerging value-added services, so providing techniques for classifying facial expressions by taking advantage of the benefits of deep learning (DL) is important. We used CNN model was trained using the FER2013 dataset and retrained it on a hybrid dataset that includes the Extended Cohn-Kanade (CK+) dataset, Japanese female facial expression (JAFFE) dataset, and Iranian emotional face database (IEFDB). We achieved a test accuracy of 88% on the hybrid test set and 90% test accuracy on the IEFDB test set.

# Emotional ID
## Usage:
Our Android app is simple enough to use as it will consist of five screens. In the first screen, the user will see the name of the app and the logo, then it will move to the second screen that contains two buttons, the first to take the photo with the camera, and the other to detect the user's emotions in real-time. 

When press the first button, a screen will appear with three buttons, one to capture an image, the other to choose an image from gallery, and the last to process the image. Once the image has been processed, you will proceed to the next screen, which displays the image captured, the user's emotion, and tips on how to act positively while experiencing that feeling. In addition, there is a button that, when clicked, will open a video for the user to watch to help him improve his emotional intelligence and motivate him to improve his mood. 

When the second button is pressed, a camera screen will appear for the user, which will detect his emotions in real-time. At the top of the camera there will be a button to flip the camera and next to it there is a button to set the delay time, and based on the time that the user sets, a notification will be sent to the user containing the user's emotion and some tips, and when clicking on the notification, a video will open for the user to watch to give him a visual reference to understand how to manage his feeling.

<img align="center" src="https://github.com/danagh1/Real-Time-Facial-Emotion-Recognition-Using-AI/blob/main/screen.png" width="800" >
<img align="center" src="https://github.com/danagh1/Real-Time-Facial-Emotion-Recognition-Using-AI/blob/main/screen2.PNG" width="800" />

_____

## Setup:
First you must download these in order to run the application:
1) Android Studio Electric Eel | 2022.1.1 Patch 1
2) OpenCV - 4.7.0 android sdk and configure it in Android studio

Then you can fork this project on GitHub, download it, and then open it as a project in Android Studio. Once you have done so, it can be run on your Android device.
