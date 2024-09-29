package com.example.safar_smart_sos;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class SOSForegroundService extends Service {
    private static final String CHANNEL_ID = "SOS help";
    private Messenger messenger;
    public static final int UPDATE_TEXTVIEW = 16;
    public static final Integer RecordAudioRequestCode = 10;
    public SpeechRecognizer speechRecognizer;
    public Intent speechRecognizerIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());

        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }


        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.d("Listner", "Audio recording is started");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d("Begin", "Audio recording is begin");

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
                Log.d("errs", "Error is generated");
                speechRecognizer.startListening(speechRecognizerIntent);
            }

            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void onResults(Bundle bundle) {
                Log.d("Result", "Result is generated");
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                assert data != null;
                String current = data.get(0);
                sendMessageToMainActivity(current);
                if (current.contains("help")) {
                    sendMessageToMainActivity("Help is asked");
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(SOSForegroundService.this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("SOS Call")
                            .setContentText("Called for Help")
                            .setPriority(NotificationCompat.PRIORITY_MAX);
                    int notificationID = 45;
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(SOSForegroundService.this);

                    if (ActivityCompat.checkSelfPermission(SOSForegroundService.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        Log.d("Notify", "Permission check");
                        return;
                    }
                    notificationManager.notify(notificationID, builder.build());
                }

                speechRecognizer.startListening(speechRecognizerIntent);
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        speechRecognizer.startListening(speechRecognizerIntent);
        messenger = (Messenger) intent.getParcelableExtra("MESSENGER");
        return super.onStartCommand(intent, flags, startId);
}

    @Override
    public void onDestroy() {
        super.onDestroy();
        speechRecognizer.stopListening();
        speechRecognizer.destroy();
    }


    private void sendMessageToMainActivity(String newText) {
        Message msg = Message.obtain(null, UPDATE_TEXTVIEW);
        Bundle bundle = new Bundle();
        bundle.putString("NEW_TEXT", newText);
        msg.setData(bundle);
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}

