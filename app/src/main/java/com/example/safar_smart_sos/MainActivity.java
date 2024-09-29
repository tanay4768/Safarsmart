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
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        messenger = new Messenger(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_TEXTVIEW:
                        String newText = msg.getData().getString("NEW_TEXT");
                        // Update the TextView here
                        TextView textView = findViewById(R.id.text);
                        textView.setText(newText);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        });
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                assert data != null;
                String current = data.get(0);
                if(current.contains("help")){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel("sos_channel", "SOS Channel", NotificationManager.IMPORTANCE_DEFAULT);
                        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        manager.createNotificationChannel(channel);
                        Notification notification = new Notification.Builder(MainActivity.this, "sos_channel")
                                .setContentTitle("SOS Service")
                                .setContentText("SOS called")
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .build();
                        startForeground(1, notification);
                    } else {
                        Notification notification = new Notification.Builder(MainActivity.this)
                                .setContentTitle("SOS Service")
                                .setContentText("SOS called")
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .build();
                        startForeground(1, notification);
                    }
                }
                speechRecognizer.cancel();
                speechRecognizer.startListening(speechRecognizerIntent);
            }

            private void startForeground(int i, Notification notification) {
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
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