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
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import com.example.bbqbuddy.R;

public class CookingActivity extends AppCompatActivity implements setTimerDialog.SetTimerDialogListener {
    private static final String TAG = CookingActivity.class.getSimpleName();

    private EditText mEditTextInput;

    private TextView countdownText;

    private Button countdownButton;
    private Button editTimerButton;


    private CountDownTimer countDownTimer;

    private long mStartTimeInMillis = 310000;
    private long timeLeftInMilliseconds; //10 mins is 600000 milliseconds

    private boolean timerRunning; // tells us if timer is running


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooking);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.d(TAG, "Cooking Activity On Create Built");

        countdownText   = findViewById(R.id.countdownText);
        countdownButton = findViewById(R.id.countdownButton);
        editTimerButton = findViewById(R.id.editTimerButton);


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

    public void startStop(){
        if (timerRunning) {
            stopTimer();
        } else {
            startTimer();
        }

    }

    public void startTimer(){
        countDownTimer = new CountDownTimer(mStartTimeInMillis,1000) {
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

    public String convertMillisToString(double timeLeftInMilliseconds){
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
        setTimer.show(getSupportFragmentManager(), " ");
    }

    @Override
    public void applyValue(String timeEntered) {

    }
}
