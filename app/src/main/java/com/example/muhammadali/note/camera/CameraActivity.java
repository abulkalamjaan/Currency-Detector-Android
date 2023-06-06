package com.example.muhammadali.note.camera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.muhammadali.note.R;
import com.example.muhammadali.note.currencies.ColorModel;
import com.example.muhammadali.note.currencies.Constants;
import com.example.muhammadali.note.currencies.PKR;
import com.example.muhammadali.note.currencies.Vision;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

@SuppressWarnings("DanglingJavadoc")
public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        Detector.Processor<TextBlock> {

    private int REQUEST_CODE_PERMISSIONS = 10; //permission code number
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"}; //array permissions
    private TextToSpeech textToSpeech;// text to speach
    private SurfaceView mCameraView;//camera preview surface
    private CameraSource mCameraSource;//camera instance
    private ArrayList<Rect> detectTextBlocks; // array of text blocks which are being detected
    private static final String TAG = CameraActivity.class.getName();//tag for debugging purpose
    private Map<String, Object> noteLandMarks;//data set of notes
    private ExecutorService executorService; // thread frame work
    private Handler handler;// threading communication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mCameraView = findViewById(R.id.surfaceView);
        textToSpeech = new TextToSpeech(this, onInitListener);
        /**
         *Threadpool which recycles number of thread created at our case #threads are 1
         */
        executorService = Executors.newFixedThreadPool(2);

        noteLandMarks = PKR.getInstance().noteLandmarks();

        detectTextBlocks = new ArrayList<>();
        handler = new Handler();
        //check camera and storage permissions
        if (allPermissionsGranted()) {
            startCameraSource();//method to set up camera resource
            //speak dialog on another thread
            executorService.submit(() -> speak("Camera is open place a note"));

        } else {
            //ask for permissions
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void startCameraSource() {

        //initialization of the TextRecognizer.
        //text recognizer instance
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies not loaded yet");
        } else {

            /**
             * Initialize camera source to use high resolution and set Auto focus on.
             * High resolution will provide 99% accuracy but speed will be slow
             * Low resolution will increase speed but low confidence results in accuracy
             * */
            mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(420, 360)// this size increases speed and confidence
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)//20FPS
                    .build();
            //attach preview callbacks , Surface holder
            mCameraView.getHolder().addCallback(this);

            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(this);
        }
    }


    //permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //start camera when permissions have been granted otherwise exit app
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCameraSource();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //permission check
    private boolean allPermissionsGranted() {
        //check if req permissions have been granted
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private TextToSpeech.OnInitListener onInitListener = i ->
            speak("Camera is open, show note");

    //method to speak words
    private synchronized void speak(final String msg) {
        if (textToSpeech.getEngines().size() == 0) {
            Toast.makeText(CameraActivity.this, "There isn't TTS engine on your device",
                    Toast.LENGTH_LONG).show();
            finish();
        } else {
            textToSpeech.setLanguage(Locale.getDefault());
            //slowing speech rate
            textToSpeech.setSpeechRate(-10);
            if (Build.VERSION.SDK_INT >= 24) {
                textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null, null);

            } else {
                textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }


    @Override
    protected void onDestroy() {
        //return results to previous activity
        Intent intent = new Intent();
        intent.putExtra("MSG", "Place your message here");
        // activity result code
        int CAM_CODE = 875;
        setResult(CAM_CODE, intent);
        textToSpeech.shutdown();
        mCameraSource.release();
        super.onDestroy();


    }

    //when camera surface is created
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            //check runtime permissions
            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //ask permissions
                ActivityCompat.requestPermissions(CameraActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CODE_PERMISSIONS);
                return;
            }
            //start preview
            mCameraSource.start(mCameraView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // interface invoked when camera properties has changed but not at this case.

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // when preview destroyed do something here if you want
    }

    @Override
    public void release() {
        // when text recognizer has been released
    }

    /**
     * interface invoked when some text is detected. this method is invoked with respect to processor
     * speed avg 200ms
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        // getting text blocks
        final SparseArray<TextBlock> items = detections.getDetectedItems();
        if (items.size() != 0) {
            //storing all blocks into builder form for better results
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                //the text item detected
                TextBlock item = items.valueAt(i);
                stringBuilder.append(item.getValue());
                //separate with space to easily perform algorithm
                stringBuilder.append(" ");

                //adding block into list
                detectTextBlocks.add(item.getBoundingBox());// these are segments where data of note
                // is located
                /**
                 * Point[] points=item.getCornerPoints();
                 * returns array of points where text edges
                 * are detected.
                 * String lang=item.getLanguage();
                 * returns language of text
                 * String val=item.getValue();
                 * returns value of text
                 * Frame.Metadata metadata=detections.getFrameMetadata();
                 * returns frame metadata
                 * which include total width , height container and rotation of image
                 *
                 * */
            }

            String data = stringBuilder.toString();
            if (!data.isEmpty() && !PKR.getInstance().whichNoteIsIt(data).isEmpty()) {
                executorService.submit(() -> {
                    if (!textToSpeech.isSpeaking()) {
                        //if note value is detected
                        speak("Move little up");
                        SystemClock.sleep(1500);
                        capture(data);
                    }
                });

            } else if (data.contains(Constants.STAMP)) {
                executorService.submit(() -> {
                    //if backside of SBP detected
                    if (!textToSpeech.isSpeaking()) {
                        speak("Move little down");
                        SystemClock.sleep(1500);
                        capture(data);
                    }
                });

            } else {
                if (isLandscape()) {
                    speak("Can't detect in landscape mode!");
                    SystemClock.sleep(1500);
                }
                if (!textToSpeech.isSpeaking()) {
                    speak("Focusing");
                    SystemClock.sleep(1500);//wait for 1.5s

                }
            }

        }
    }

    public boolean isLandscape() {
        final int screenOrientation = getResources().getConfiguration().orientation;
        System.out.println("ROTATION " + screenOrientation);
        return screenOrientation == Configuration.ORIENTATION_LANDSCAPE;
    }


    /**
     * Method which captures image and calculate value and confidence of note
     * Takes data which is detected text as parameter
     */
    private synchronized void capture(final String data) {
        mCameraSource.takePicture(null, bytes -> {
            speak("Captured. Processing");
            //stopping camera
            mCameraSource.stop();
            mCameraView.setVisibility(View.GONE);
            //converting image raw data into bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            int pixel = getDominantColor(bitmap);//getting pixel color
            int redValue = Color.red(pixel);//red value inside note
            int blueValue = Color.blue(pixel);//blue value inside note
            int greenValue = Color.green(pixel);//green values inside note

            int color = Color.rgb(redValue, greenValue, blueValue);//RGB format color

            //Color data object
            ColorModel model = new ColorModel();
            model.setGreenMax(greenValue);
            model.setRedMax(redValue);
            model.setBlueMax(blueValue);

            //getting results
            Map<String, String> results = Vision.getInstance().getResults(data, model);
            System.out.println(results);
            if (results != null) {
                String note = results.get(Constants.NOTE);

                System.out.println(note);

                String confidence = note + " and " + Constants.CONFIDENCE + " is " + results.get(Constants.CONFIDENCE)
                        + " percent";
                System.out.println(confidence);
                speak(confidence);
                //can't touch UI items from another thread so handler is important
                handler.post(() -> {
                    TextView textView = findViewById(R.id.cameraActivityResults);
                    textView.setText(confidence);
                    //setting image
                    ImageView imageView = findViewById(R.id.cameraActivityResultImage);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(bitmap);
                    //changing color of button
                    ExtendedFloatingActionButton button = findViewById(R.id.cameraActivityBack);
                    button.setBackgroundColor(color);
                });
                //after 5s destroy activity
                handler.postDelayed(this::finish, 5000); //destroy after 5s
            }
        });

    }

    /**
     * Method responsible  to get most frequent color in the note.
     */
    public static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    //on back button click
    public void onCameraBackClick(View view) {
        this.finish();

    }
}
