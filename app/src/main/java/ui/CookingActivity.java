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

import java.util.Locale;

import backend.BlunoLibrary;
import backend.CookingViewModel;
import backend.DatabaseController;

public class CookingActivity extends AppCompatActivity {
    private static final String TAG = CookingActivity.class.getSimpleName();

    private CookingViewModel model;

    private TextView countdownText;
    private TextView instructionsText;

    private Button startButton;
    private Button resetButton;

    private TextView textReceived;
    private TextView textStatus;

    private TextView bluetoothStatus;
    private TextView temperatureText;

    private BlunoLibrary blunoLibrary;

    private CountDownTimer countDownTimer;

    private int finalTemp;
    private int cookingTime;
    private int restTime;
    private int flipTime;
    private String meatFoodSpec;
    private boolean hasNotified;
    private boolean hasFlipped;

    private long startTimeInMillis;
    private long timeLeftInMilliseconds; //10 mins is 600000 milliseconds
    private long endTime;

    //private long restTime = 31000;
    private boolean timerRunning; // tells us if timer is running
    private boolean restTimerSet = false;

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
        startTimeInMillis = 60000 * cookingTime;
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
            } else {
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
        countdownText = findViewById(R.id.countdownText);
        instructionsText = findViewById(R.id.instructionSetTextView);
        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);

        textReceived = findViewById(R.id.text_Received);
        textStatus = findViewById(R.id.textStatus);
        temperatureText = findViewById(R.id.temperatureText);
        bluetoothStatus = findViewById(R.id.bluetoothStatus);

        hasNotified = false;
        //add listeners to the countdown button
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStop();
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {

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

        //create observer for ECT
        final Observer<String> cookingTimeObserver = new Observer<String>() {
            @Override
            public void onChanged(String s) {
                cookingTime = Integer.parseInt(s);
                int cookingTimeMillis = cookingTime*60000;
                int minutes = (cookingTimeMillis / 1000) / 60;
                int seconds = (cookingTimeMillis / 1000) % 60;

                String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

                countdownText.setText(timeLeftFormatted);
            }
        };

        //create observer for temperature
        final Observer<String> finalTempObserver = new Observer<String>() {
            @Override
            public void onChanged(String s) {
                finalTemp = Integer.parseInt(s);
            }
        };

        final Observer<String> restTimeObserver = new Observer<String>() {
            @Override
            public void onChanged(String s) {
                restTime = Integer.parseInt(s);
            }
        };

        final Observer<String> flipTimeObserver = new Observer<String>() {
            @Override
            public void onChanged(String s) {
                flipTime = Integer.parseInt(s);
            }
        };

        model.getFinalTemp().observe(this, finalTempObserver);
        model.getInstructions().observe(this, instructionObserver);
        model.getCookingTime().observe(this, cookingTimeObserver);
        model.getRestTime().observe(this, restTimeObserver);
        model.getFlipTime().observe(this, flipTimeObserver);

        //get important values from the intent
        String meatCut = getIntent().getStringExtra("meatCut");
        String foodSpec = getIntent().getStringExtra("foodSpec");
        String doneness = getIntent().getStringExtra("doneness");

        if (foodSpec == null) {
            meatFoodSpec = meatCut;
        } else {
            meatFoodSpec = meatCut + "(" + foodSpec + ")";
        }

        //retrieve information from the database
        DatabaseController dbcontroller = new DatabaseController();
        dbcontroller.readFinalTempFromDB(getIntent().getStringExtra("meatType"), meatFoodSpec, doneness, model);
        dbcontroller.readInstructionsFromDB(getIntent().getStringExtra("meatType"), meatFoodSpec, model);
        dbcontroller.readCookingTimeFromDB(getIntent().getStringExtra("meatType"),meatFoodSpec, model);
        dbcontroller.readRestTimeFromDB(getIntent().getStringExtra("meatType"),meatFoodSpec,model);
        dbcontroller.readFlippingTimeFromDB(getIntent().getStringExtra("meatType"),meatFoodSpec,model);
    }

    public void startStop() {
        if (timerRunning) {
            if (restTimerSet == false) {
                stopTimer();
                resetButton.setVisibility(View.VISIBLE);
            } else {
                stopTimer();
                resetButton.setVisibility(View.INVISIBLE);
            }
        } else {
            if (restTimerSet == false) {
                startTimer();
                resetButton.setVisibility(View.VISIBLE);
            } else {
                startTimer();
                resetButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void startTimer() {
        endTime = System.currentTimeMillis() + timeLeftInMilliseconds;
        if (restTimerSet == false) {
            startTimeInMillis = cookingTime*60000;
            timeLeftInMilliseconds = startTimeInMillis;
        }

        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
            // CountDownTimer(time left, countdown interval)
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("DEBUG", cookingTime + " THIS IS THE COOKING TIME");
                //l is variable that contains remaining time
                timeLeftInMilliseconds = millisUntilFinished;
                updateTimer();

                //double currentTemp = blunoLibrary.getCurrentTemp();

                //TODO remove this code only for testing without bluetooth
                double currentTemp = 0;

                if (timeLeftInMilliseconds % 300000 < 1500) {
                    currentTemp = 0.9 * finalTemp;
                }

                if (timeLeftInMilliseconds % 294000 < 1500) {
                    currentTemp = finalTemp;
                }
                //todo end of test code

                //send notification for flipping meat
                if(timeLeftInMilliseconds < (flipTime * 60000) && !hasFlipped){
                    Uri notificationAlarm = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationAlarm);
                    ringtone.play();

                    String meatType = getIntent().getStringExtra("meatType");
                    sendNotification(meatType + " " + meatFoodSpec, "Your " + meatType + " " + meatFoodSpec + " has needs to be flipped!");
                    hasFlipped = true;
                }

                //send notification when meat is 90% of final temp
                if (currentTemp >= (0.9 * finalTemp) && !hasNotified) {
                    Uri notificationAlarm = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationAlarm);
                    ringtone.play();

                    int minutes = (int) (timeLeftInMilliseconds / 1000) / 60;
                    int seconds = (int) (timeLeftInMilliseconds / 1000) % 60;
                    String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

                    String meatType = getIntent().getStringExtra("meatType");
                    sendNotification(meatType + " " + meatFoodSpec, "Your " + meatType + " " + meatFoodSpec + " has " + timeLeftFormatted + " left!");
                    hasNotified = true;
                }

                //send notification when meat is finished
                if (currentTemp >= finalTemp) {
                    try {
                        Uri finishedAlarm = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm);
                        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), finishedAlarm);
                        ringtone.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFinish() {
                countdownText.setText("0:00");
                timerRunning = false;
                try {
                    Uri finishedAlarm = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), finishedAlarm);
                    ringtone.play();
                        startButton.setText("Set Rest Timer");
                        // create RestTimer on click
                        startButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                createRestTimer();

                            }

                        });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
                .start();

        startButton.setText("PAUSE");
        timerRunning = true;

    }

    public void stopTimer() {
        countDownTimer.cancel();
        startButton.setText("START");
        timerRunning = false;
    }

    private void resetTimer() {
        if (timerRunning == true) {
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

    public void updateTimer() {
        int minutes = (int) (timeLeftInMilliseconds / 1000) / 60;
        int seconds = (int) (timeLeftInMilliseconds / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        countdownText.setText(timeLeftFormatted);

    }

    private void sendNotification(String mealName, String message) {
        //sends a notifications with an intent that brings back to cooking activity
        Intent intent = new Intent(this, CookingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(mealName)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(001, builder.build());
    }

    private void createRestTimer() {
        timeLeftInMilliseconds = restTime * 60000;//convert rest time to millis
        stopTimer();
        updateTimer();
        timerRunning = false;
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStop();
            }
        });
        restTimerSet =true;
        resetButton.setVisibility(View.INVISIBLE);
    }
}

