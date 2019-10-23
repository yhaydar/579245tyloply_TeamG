package ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import com.example.bbqbuddy.R;

public class CookingActivity extends AppCompatActivity {

    private TextView countdownText;
    private Button countdownButton;

    private CountDownTimer countDownTimer;
    private long timeLeftInMilliseconds = 600000; //10 mins is 600000 milliseconds
    private boolean timerRunning; // tells us if timer is running


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooking);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        countdownText = findViewById(R.id.countdownText);
        countdownButton = findViewById(R.id.countdownButton);

        countdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStop();
            }
        });

        //not sure if this works yet
        createNotificationChannel();

    }

    public void startStop(){
        if (timerRunning) {
            stopTimer();
        } else {
            startTimer();
        }

    }

    public void startTimer(){
        countDownTimer = new CountDownTimer(timeLeftInMilliseconds,1000) {
                            // CountDownTimer(time left, countdown interval)
            @Override
            public void onTick(long l) {
                //l is variable that contains remaining time
                timeLeftInMilliseconds = l;
                if(timeLeftInMilliseconds % 300000 == 0){
                    Uri finishNotification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), finishNotification);
                    ringtone.play();
                    //Todo add reminders notifications
                    setupNotifications();
                }
                updateTimer();
            }

            @Override
            public void onFinish() {
                Uri finishNotification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), finishNotification);
                ringtone.play();
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
        int minutes = (int) timeLeftInMilliseconds/60000;
        int seconds = (int) timeLeftInMilliseconds % 60000 / 1000;

        String timeLeftText;

        timeLeftText = "" + minutes;
        timeLeftText += ":";
        if (seconds <10) timeLeftText += "0"; // if single digit seconds, adds 0 to hold place
        timeLeftText += seconds;

        countdownText.setText(timeLeftText);
    }

    public void setupNotifications(){
        Intent intent = new Intent(this, CookingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        try{
            pendingIntent.send();
        } catch(PendingIntent.CanceledException e){
            e.printStackTrace();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "id")
                .setSmallIcon(R.drawable.beef)//todo change this to match the type of food cooked
                .setContentTitle("Text notification") //todo change this to the name of the food cooked
                .setContentText("X minutes remaining") //todo add time remaining in meal
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "TestName";
            String description = "TestChannelDescription";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("id",name,importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
