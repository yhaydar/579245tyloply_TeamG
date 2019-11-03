package ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

import backend.CookingViewModel;
import backend.DatabaseController;

public class CookingActivity extends AppCompatActivity  implements setTimerDialog.SetTimerDialogListener {
    private static final String TAG = CookingActivity.class.getSimpleName();

    private CookingViewModel model;

    private TextView countdownText;
    private TextView instructionsText;

    private Button countdownButton;
    private Button editTimerButton;

    private CountDownTimer countDownTimer;

    private long startTimeInMillis = 310000;
    private long timeLeftInMilliseconds; //10 mins is 600000 milliseconds

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

    private void setupActivity() {
        //setup ui components
        countdownText   = findViewById(R.id.countdownText);
        instructionsText = findViewById(R.id.instructionSetTextView);
        countdownButton = findViewById(R.id.countdownButton);
        editTimerButton = findViewById(R.id.editTimerButton);

        //set the remaining time and the instruction text
        timeLeftInMilliseconds = startTimeInMillis;

        //add listeners to the countdown button
        countdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStop();
            }
        });
        editTimerButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                openSetTimerFrag();
            }
        });
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
        dbcontroller.readInstructionsFromDB(getIntent().getStringExtra("meatType"),getIntent().getStringExtra("meatCut"), model);
    }

    public void startStop(){
        if (timerRunning) {
            stopTimer();
            editTimerButton.setVisibility(View.VISIBLE);
        } else {
            startTimer();
            editTimerButton.setVisibility(View.INVISIBLE);
        }

    }

    public void startTimer(){
        countDownTimer = new CountDownTimer(timeLeftInMilliseconds,1000) {
                            // CountDownTimer(time left, countdown interval)
            @Override
            public void onTick(long l) {
                //l is variable that contains remaining time
                timeLeftInMilliseconds = l;

                if(timeLeftInMilliseconds % 300000 < 1500){
                    Uri notificationAlarm = Uri.parse("android.resource://"+ getPackageName() + "/" + R.raw.notification);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationAlarm);
                    ringtone.play();

                    //TODO replace with temperature based solution
                    sendNotification("PORK ROAST");
                }
                updateTimer();
            }

            @Override
            public void onFinish() {
                countdownText.setText("0:00");
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

    public void updateTimer(){
       countdownText.setText(convertMillisToString(timeLeftInMilliseconds));
    }

    public String convertMillisToString(long timeLeftInMilliseconds){
        int minutes = (int) timeLeftInMilliseconds/60000;
        int seconds = (int) timeLeftInMilliseconds % 60000 / 1000;

        String time;

        time = "" + minutes;
        time += ":";
        if (seconds <10) time += "0"; // if single digit seconds, adds 0 to hold place
        time += seconds;

        return time;
    }

    private void sendNotification(String mealName){
        //sends a notifications with an intent that brings back to cooking activity
        Intent intent = new Intent(this, CookingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(mealName)
                .setContentText("Your "+ mealName +" has " + convertMillisToString(timeLeftInMilliseconds) + " left")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(001,builder.build());
    }

    public void openSetTimerFrag(){
        setTimerDialog setTimer = new setTimerDialog();
        setTimer.show(getSupportFragmentManager(), "Set Timer frag");
    }

    @Override
    public void applyValue(int timeEntered) {
        long minutesEntered = timeEntered * 60000; // to get value in minutes
        timeLeftInMilliseconds = minutesEntered;   // adjusts timer
        countdownText.setText(convertMillisToString(minutesEntered));
    }
}
