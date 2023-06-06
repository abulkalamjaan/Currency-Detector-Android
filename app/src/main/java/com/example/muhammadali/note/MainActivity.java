package com.example.muhammadali.note;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import com.example.muhammadali.note.camera.CameraActivity;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextToSpeech myTTS;
    private Handler handler; // handler for thread communication
    private int CAM_CODE = 875; // activity result code
    private Context context;//global context instance
    private GestureDetector detector;//gesture detector for handling taps


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        handler = new Handler();
        new Thread(this::initializeTextToSpeech).start();//thread to handle TTS events
        addGestures();//init Gestures
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addGestures() {
        //init gesture detector
        detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;//should return true to listen for event
            }

        });
        detector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                //launch camera
                startActivityForResult(new Intent(context, CameraActivity.class), CAM_CODE);
                return true;//should return true to listen for event
            }

            @Override
            public boolean onDoubleTap(MotionEvent motionEvent) {
                return true;
                //should return true to listen for event
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent motionEvent) {
                //add action on double tap gesture
                if (!myTTS.isSpeaking())//if TTF is free
                    speak("Document reading action coming soon");
                return true;
            }
        });

        //attaching gestures to parent layout of screen
        findViewById(R.id.mainActivityParent).setOnTouchListener((view, motionEvent) -> {
            detector.onTouchEvent(motionEvent);
            return true;
        });
    }

    private void initializeTextToSpeech() {
        myTTS = new TextToSpeech(this, i -> {
            if (myTTS.getEngines().size() == 0) {
                handler.post(() -> {
                    Toast.makeText(MainActivity.this, "There isn't TTS engine on your device",
                            Toast.LENGTH_LONG).show();
                });
            } else {
                myTTS.setLanguage(Locale.getDefault());
                myTTS.setSpeechRate(-10);
                speak("I m ready, tap on screen for note detection ");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Resume");
        if (myTTS != null && !myTTS.isSpeaking())
            speak("I m ready, tap on screen for note detection");
    }

    private void speak(String message) {
        if (Build.VERSION.SDK_INT >= 24) {
            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);

        } else {
            if (!myTTS.isSpeaking())
                myTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Get your message from CameraActivity here onActivityResults
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == CAM_CODE) {
            // user is back from camera activity
            String yourMSG = data.getStringExtra("MSG");
            System.out.println(yourMSG);
            speak("Tap for note detection ");

        }
    }


}
