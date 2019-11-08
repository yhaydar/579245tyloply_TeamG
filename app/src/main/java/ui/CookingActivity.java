package ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.bbqbuddy.R;

import backend.BlunoLibrary;
import java.util.Locale;

import backend.CookingViewModel;
import backend.DatabaseController;

public class CookingActivity extends AppCompatActivity  {//implements setTimerDialog.SetTimerDialogListener {
    private static final String TAG = CookingActivity.class.getSimpleName();

    private CookingViewModel model;

    private TextView countdownText;
    private TextView instructionsText;

    private Button countdownButton;
    private Button resetButton;

    private TextView textReceived;
    private TextView textStatus;

    private TextView bluetoothStatus;
    private TextView temperatureText;

    private BlunoLibrary blunoLibrary;

    private CountDownTimer countDownTimer;

    private long startTimeInMillis = 310000;
    private long timeLeftInMilliseconds; //10 mins is 600000 milliseconds
    private long endTime;

    private boolean timerRunning; // tells us if timer is running


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooking);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupViewModel();
        setupActivity();

        Log.d(TAG, "Cooking Activity On Create Built");
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("millisLeft", timeLeftInMilliseconds);
        editor.putBoolean("timerRunning", timerRunning);
        editor.putLong("endTime", endTime);

        editor.apply();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        blunoLibrary.scanLeDevice(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        timeLeftInMilliseconds = prefs.getLong("millisLeft", startTimeInMillis);
        timerRunning = prefs.getBoolean("timerRunning", false);

        updateTimer();

        if (timerRunning) {
            endTime = prefs.getLong("endTime", 0);
            timeLeftInMilliseconds = endTime - System.currentTimeMillis();

            if (timeLeftInMilliseconds < 0) {
                timeLeftInMilliseconds = 0;
                timerRunning = false;
                updateTimer();
            }
            else {
                startTimer();
            }
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResumeBegins");
        super.onResume();
        blunoLibrary.onResumeProcess();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        blunoLibrary.onDestroyProcess();
    }


    private void setupActivity() {
        //setup ui components
        countdownText   = findViewById(R.id.countdownText);
        instructionsText = findViewById(R.id.instructionSetTextView);
        countdownButton = findViewById(R.id.countdownButton);
        resetButton = findViewById(R.id.resetButton);

        textReceived = findViewById(R.id.text_Received);
        textStatus = findViewById(R.id.textStatus);
        temperatureText = findViewById(R.id.temperatureText);
        bluetoothStatus = findViewById(R.id.bluetoothStatus);



        //add listeners to the countdown button
        countdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStop();
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                resetTimer();
            }
        });

        blunoLibrary = new BlunoLibrary(this);
        blunoLibrary.scanLeDevice(true);
    }

    private void setupViewModel() {
        //get the view model
        model = ViewModelProviders.of(this).get(CookingViewModel.class);

        //create observer to update UI
        final Observer<String> instructionObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String s) {
                //update instruction text view
                instructionsText.setText(s);
            }
        };

        model.getInstructions().observe(this, instructionObserver);

        DatabaseController dbcontroller = new DatabaseController();
        String meatCut = getIntent().getStringExtra("meatCut");
        String foodSpec = getIntent().getStringExtra("foodSpec");
        String meatFoodSpec;
        if(foodSpec == null){
            meatFoodSpec = meatCut;
        }
        else{
            meatFoodSpec = meatCut+"("+foodSpec+")";
        }
        dbcontroller.readInstructionsFromDB(getIntent().getStringExtra("meatType"),meatFoodSpec, model);
    }

    public void startStop(){
        if (timerRunning) {
            stopTimer();
            resetButton.setVisibility(View.VISIBLE);

        } else {
            startTimer();
            resetButton.setVisibility(View.VISIBLE);
        }

    }

    public void startTimer(){
        endTime = System.currentTimeMillis() + timeLeftInMilliseconds;

        countDownTimer = new CountDownTimer(timeLeftInMilliseconds,1000) {

            // CountDownTimer(time left, countdown interval)
            @Override
            public void onTick(long millisUntilFinished) {
                //l is variable that contains remaining time
                timeLeftInMilliseconds = millisUntilFinished;
                updateTimer();

                if(timeLeftInMilliseconds % 300000 < 1500){
                    Uri notificationAlarm = Uri.parse("android.resource://"+ getPackageName() + "/" + R.raw.notification);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationAlarm);
                    ringtone.play();

                    //TODO replace with temperature based solution
                    sendNotification("PORK ROAST");
                }

            }

            @Override
            public void onFinish() {
                countdownText.setText("0:00");
                timerRunning = false;
                try{
                    Uri finishedAlarm = Uri.parse("android.resource://"+ getPackageName() + "/" + R.raw.alarm);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), finishedAlarm);
                    ringtone.play();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        .start();

        countdownButton.setText("PAUSE");
        timerRunning = true;

    }

    public void stopTimer(){
        countDownTimer.cancel();
        countdownButton.setText("START");
        timerRunning = false;
    }
    private void resetTimer(){
        if(timerRunning == true) {
            stopTimer();
            timeLeftInMilliseconds = startTimeInMillis;
            updateTimer();
            timerRunning = false;
        } else {
            timeLeftInMilliseconds = startTimeInMillis;
            updateTimer();
            timerRunning = false;
        }
    }

    public void updateTimer(){
            int minutes = (int) (timeLeftInMilliseconds / 1000) / 60;
            int seconds = (int) (timeLeftInMilliseconds / 1000) % 60;

            String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

            countdownText.setText(timeLeftFormatted);

    }

    private void sendNotification(String mealName){
        //sends a notifications with an intent that brings back to cooking activity
        Intent intent = new Intent(this, CookingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(mealName)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(001,builder.build());
    }


}

