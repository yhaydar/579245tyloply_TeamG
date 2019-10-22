package ui;

import android.os.Bundle;

import com.example.bbqbuddy.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class cookingActivity extends AppCompatActivity {

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
                updateTimer();
            }

            @Override
            public void onFinish() {

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
}
