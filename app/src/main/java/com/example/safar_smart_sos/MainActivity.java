package com.example.safar_smart_sos;

import static androidx.core.app.ServiceCompat.startForeground;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Messenger messenger;
    public static final int UPDATE_TEXTVIEW = 16;
    private Button startButton;
    public SpeechRecognizer speechRecognizer;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQUEST_RECORD_NOTIFICATION_PERMISSION = 500;
    private boolean isListening = false;

    @SuppressLint("HandlerLeak")
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
            String[] permissions = new String[]{
        Manifest.permission.RECORD_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS,
    };
            int permissioncode = 11;
           for(String permission: permissions) {
               permissioncode+=1;
               requestPermissions(new String[] {permission}, permissioncode);
           }
        startButton = findViewById(R.id.start_button);

        messenger = new Messenger(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == UPDATE_TEXTVIEW) {
                    String newText = msg.getData().getString("NEW_TEXT");
                    // Update the TextView here
                    TextView textView = findViewById(R.id.text);
                    textView.setText(newText);
                } else {
                    super.handleMessage(msg);
                }
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_RECORD_NOTIFICATION_PERMISSION);
                } else {
                    if (!isListening) {
//                        speechRecognizer.startListening(speechRecognizerIntent);
                        Intent intent = new Intent(MainActivity.this, SOSForegroundService.class);
                        intent.putExtra("MESSENGER", messenger);
                        startService(intent);
                        startButton.setBackgroundResource(R.drawable.onsos);
                        startButton.setText("Stop \n Listening");
                        isListening = true;
                    } else{
//                        speechRecognizer.stopListening();
                        startButton.setBackgroundResource(R.drawable.offsos);
                        stopService(new Intent(MainActivity.this, SOSForegroundService.class));
                        startButton.setText("Start \n Listening");

                        isListening = false;
                    }
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!isListening) {
                    startService(new Intent(MainActivity.this, SOSForegroundService.class));
                    startButton.setText("Stop Listening");
                    startButton.setBackgroundResource(R.drawable.onsos);
                    isListening = true;
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}