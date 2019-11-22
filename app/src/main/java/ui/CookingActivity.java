package ui;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.bbqbuddy.R;

import java.util.Locale;

import backend.BlunoLibrary;
import backend.CookingViewModel;
import backend.DatabaseController;

import static ui.SettingsActivity.SHARED_PREFS;
import static ui.SettingsActivity.TempUnitSwitch;
import static ui.SettingsActivity.ThemeSwitch;
import static ui.SettingsActivity.WeightUnitSwitch;

public class CookingActivity extends AppCompatActivity {
    private static final String TAG = "CookingActivity";

    private CookingViewModel model;

    private TextView countdownText;
    private TextView instructionsText;

    private Button startButton;
    private Button resetButton;

    private TextView textReceived;
    private TextView textStatus;
    private TextView textBTDisconnect;

    private TextView bluetoothStatus;
    private TextView temperatureText;
    private TextView target_temp;

    private BlunoLibrary blunoLibrary;
    private AlertDialog alertDialogBT;
    private boolean hasBeenAlerted = false;

    private CountDownTimer countDownTimer;

    private ProgressBar progressBar;

    private double finalTemp;
    private double startingTemp;
    private double tempRate;
    private double minTempRate = 5.55E-6;
    private double currentTempInF;
    private  double currentTempInC;
    private int cookingTime;
    private int restTime;
    private int flipTime;
    private long nextFlipTime;
    private String meatFoodSpec;
    private boolean hasNotified;
    private boolean doneFlipping = false;
    private long system_time;

    private long startTimeInMillis;
    private long timeLeftInMilliseconds; //10 mins is 600000 milliseconds
    private long endTime;
    private boolean measuredFirstTime = false;
    private boolean measuredSecondTime = false;
    private long timeInterval;
    private long timeBuffer = 60000;


    private boolean timerRunning; // tells us if timer is running
    private boolean restTimerSet = false;
    private boolean timerStarted = false;

    private boolean DegreesC = false;

    private String CHANNEL_ID = "1";

    private Context cookingContext = this;

    private Switch cThmSwitch;
    private Switch cTmpSwitch;
    private Switch cWtSwitch;

    private boolean isthereconnection;
    private double currentTemp;

    private ImageView meatImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.darkactionbartheme);
        }else setTheme(R.style.lightactionbartheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooking);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //Switch initialization
        cThmSwitch = findViewById(R.id.cThmswitch);
        cTmpSwitch = findViewById(R.id.cTmpswitch);
        cWtSwitch = findViewById(R.id.cWtswitch);

        Log.d(TAG, "Cooking Activity On Create Built");

        //retrieve boolean value from settings page
        Boolean cThmChecked;
        Boolean cTmpChecked;
        Boolean cWtChecked;

        SharedPreferences preferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        cThmChecked = preferences.getBoolean(ThemeSwitch,false);
        cTmpChecked = preferences.getBoolean(TempUnitSwitch, false);
        cWtChecked = preferences.getBoolean(WeightUnitSwitch, false);

        //set the hidden switches to value of settings page
        cThmSwitch.setChecked(cThmChecked);
        cTmpSwitch.setChecked(cTmpChecked);
        cWtSwitch.setChecked(cWtChecked);

        if (cThmSwitch.isChecked()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        if(cTmpSwitch.isChecked()){
            DegreesC = false;
        }else{
            DegreesC = true;
        }

        setupViewModel();
        setupActivity();
        createNotificationChannel();
    }

    @Override
    public void onBackPressed() {
        if (cThmSwitch.isChecked()){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
            builder.setMessage(Html.fromHtml("<b>Are you sure you want to go Back?</b>" + " <br> </br>" +
                    "This will cancel the current timer and return you to the main page."))
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (timerRunning) {
                                Log.d(TAG, "Cooking Activity if onBackpressed" + cookingTime);
                                stopTimer();
                            }
                            Log.d(TAG, "Cooking Activity onBackpressed" + cookingTime);
                            CookingActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

            AlertDialog alertDialog = builder.create();

            alertDialog.show();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(Html.fromHtml("<b>Are you sure you want to go Back?</b>" + " <br> </br>" +
                    "This will cancel the current timer and return you to the main page."))
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (timerRunning) {
                                Log.d(TAG, "Cooking Activity if onBackpressed" + cookingTime);
                                stopTimer();
                            }
                            Log.d(TAG, "Cooking Activity onBackpressed" + cookingTime);
                            CookingActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

            AlertDialog alertDialog = builder.create();

            alertDialog.show();
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Cooking Activity On OnStop");
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Log.d(TAG,"CookingActivity " + timeLeftInMilliseconds);
        editor.putLong("millisLeft", timeLeftInMilliseconds);
        editor.putBoolean("timerRunning", timerRunning);
        editor.putLong("endTime", endTime);
        editor.putLong("systemtime",System.currentTimeMillis());

        editor.apply();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        blunoLibrary.scanLeDevice(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Cooking Activity OnStart");
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        startTimeInMillis = 60000 * cookingTime;
        timeLeftInMilliseconds = prefs.getLong("millisLeft", startTimeInMillis);
        Log.d(TAG, "Cooking Activity" + timeLeftInMilliseconds );
        timerRunning = prefs.getBoolean("timerRunning", false);

        updateTimer();

        if (timerRunning) {
            endTime = prefs.getLong("endTime", 0);
            system_time = prefs.getLong("systemtime",0);
            timeLeftInMilliseconds = timeLeftInMilliseconds - (System.currentTimeMillis()-system_time);
            Log.d(TAG, "Cooking Activity after if" + timeLeftInMilliseconds );
            if (timeLeftInMilliseconds < 0) {
                Log.d(TAG, "Cooking Activity <0 " + timeLeftInMilliseconds );
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
        Log.d(TAG, "Cooking Activity onResumeBegins");
        super.onResume();
        blunoLibrary.onResumeProcess();
    }

    @Override
    protected void onPause() {
        super.onPause();

        blunoLibrary.onPauseProcess();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Cooking Activity onDestroy");
        blunoLibrary.onDestroyProcess();
        timerRunning = false;
        super.onDestroy();
    }


    private void setupActivity() {
        //setup ui components
        countdownText = findViewById(R.id.countdownText);
        instructionsText = findViewById(R.id.instructionSetTextView);
        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);
        progressBar = findViewById(R.id.progressBar);


        textReceived = findViewById(R.id.text_Received);
        textStatus = findViewById(R.id.textStatus);
        temperatureText = findViewById(R.id.temperatureText);
        bluetoothStatus = findViewById(R.id.bluetoothStatus);
        textBTDisconnect = findViewById(R.id.textBTDisconnect);
        target_temp = findViewById(R.id.t_temp);



        String meatType = getIntent().getStringExtra("meatType");
        String meatCut = getIntent().getStringExtra("meatCut");
        getSupportActionBar().setTitle(meatType + " " + meatCut);


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

        //progress bar
        Animation an = new RotateAnimation(0.0f, 270.0f, 250f, 273f);
        an.setFillAfter(true);
        progressBar.startAnimation(an);
        progressBar.setProgress(100);

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
                if(DegreesC) {
                    finalTemp = Double.parseDouble(s);
                    target_temp.setText(finalTemp + "°C"); //To view temp on app
                }else {
                    finalTemp = Double.parseDouble(s) * (1.8) + 32;
                    target_temp.setText(finalTemp + ("°F"));
                }
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
        String meatType = getIntent().getStringExtra("meatType");
        String meatCut = getIntent().getStringExtra("meatCut");
        String foodSpec = getIntent().getStringExtra("foodSpec");
        String doneness = getIntent().getStringExtra("doneness");

        meatImage = findViewById(R.id.imageMeat);
        setMeatImage(meatType, meatCut, doneness);

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
        Log.d("DEBUG", " THIS IS THE startTimer");
        endTime = System.currentTimeMillis() + timeLeftInMilliseconds;
        nextFlipTime = cookingTime - flipTime;
        startingTemp = currentTemp;
        if (restTimerSet == false) {
            if (timerStarted == false){
            startTimeInMillis = cookingTime*60000;
            timeLeftInMilliseconds = startTimeInMillis;
            updateTimer();
            timerStarted = true;
            }
        }


        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
            // CountDownTimer(time left, countdown interval)

            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("DEBUG", cookingTime + " THIS IS THE COOKING TIME");
                //l is variable that contains remaining time
                timeLeftInMilliseconds = millisUntilFinished;
                int minutes = (int) (timeLeftInMilliseconds / 1000) / 60;
                int seconds = (int) (timeLeftInMilliseconds / 1000) % 60;
                String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                double barmax = (double) (startTimeInMillis);
                double progress = (double) (timeLeftInMilliseconds);
                double barVal = (progress / barmax *100);
                progressBar.setProgress((int)barVal);
                updateTimer();

                //if the app is run on the emulator
                if(blunoLibrary.mBluetoothLeService != null) {
                    if (blunoLibrary.mBluetoothLeService.mConnectionState == 0) {

                        BluetoothAlert();
                        isthereconnection = false;
                    } else {
                        hasBeenAlerted = false;
                        isthereconnection = true;
                        textBTDisconnect.setVisibility(View.INVISIBLE);
                    }
                }


                if(isthereconnection) {
                    currentTempInC = blunoLibrary.getCurrentTemp();
                    currentTempInF = blunoLibrary.getCurrentTemp() * 1.8 + 32;
                    if(DegreesC){
                        currentTemp = currentTempInC;
                    }else{
                        currentTemp = currentTempInF;
                    }
                } else{

                    /*Let temperature values remain what they were before.
                    If bluetooth is disconnected, it is better to keep temp
                    values at what they were and not reset them to 0.
                    */

                    //currentTempInC = 0;
                    //currentTempInF = 0;
                    //currentTemp = 0;
                }
                //TODO remove this code only for testing without bluetooth
            /*   double currentTemp = 0;

               if (timeLeftInMilliseconds % 300000 < 1500) {
                    currentTemp = 0.9 * finalTemp;
                }

                if (timeLeftInMilliseconds % 294000 < 1500) {
                    currentTemp = finalTemp;
                }
              */ //todo end of test code

                //send notification for flipping meat
                if(((timeLeftInMilliseconds <= 1.02*((nextFlipTime) * 60000)) &&
                        (timeLeftInMilliseconds >= 0.98*((nextFlipTime) * 60000))) && !doneFlipping && (restTimerSet == false)){
                    Uri notificationAlarm = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationAlarm);
                    ringtone.play();

                    String meatType = getIntent().getStringExtra("meatType");
                    sendNotification(meatType + " " + meatFoodSpec, "Your " + meatType + " " + meatFoodSpec + " needs to be flipped!");
                    vibrate();
                    nextFlipTime = nextFlipTime - flipTime;
                    Log.d(TAG, "Next flip Time: " + nextFlipTime);

                    if (nextFlipTime* 60000 > timeLeftInMilliseconds)
                        doneFlipping = true;

                }


                if ((currentTemp >= (0.7 * finalTemp) && !measuredFirstTime)) {
                    timeInterval = timeLeftInMilliseconds;
                    measuredFirstTime = true;
                    Log.d(TAG, "First time measurement   " + timeInterval);
                }

                if ((currentTemp >= (0.9 * finalTemp) && !measuredSecondTime)) {
                    timeInterval = timeInterval - timeLeftInMilliseconds;
                    tempRate = (finalTemp*(0.9 - 0.7)/timeInterval);
                    timeLeftInMilliseconds = (long) ((finalTemp - currentTemp)/tempRate) + timeBuffer;
                    Log.d(TAG, "Second time measurement   " + timeLeftInMilliseconds);
                    measuredSecondTime = true;

                    Uri notificationAlarm = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationAlarm);
                    ringtone.play();

                    //Doing this because update time function wont use latest timeleftinmilliseconds
                    minutes = (int) (timeLeftInMilliseconds / 1000) / 60;
                    seconds = (int) (timeLeftInMilliseconds / 1000) % 60;
                    timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

                    String meatType = getIntent().getStringExtra("meatType");
                    sendNotification(meatType + " " + meatFoodSpec, "Your " + meatType + " " + meatFoodSpec + " has " + timeLeftFormatted + " left!");
                    vibrate();

                    ChangeTime();
                }

                if ((currentTemp < 0.9*finalTemp)&& (timeLeftInMilliseconds < 10000)&&(restTimerSet == false)) {
                    if (currentTemp > 0.65*finalTemp) {
                        measuredFirstTime = true;
                        measuredSecondTime = true;
                    }
                    timeInterval = cookingTime*60000;
                    tempRate = ((currentTemp - startingTemp)/timeInterval);

                    if (tempRate > minTempRate)
                        timeLeftInMilliseconds = (long) ((finalTemp - currentTemp)/tempRate) + timeBuffer;
                    else
                        timeLeftInMilliseconds = cookingTime*60000;

                    Uri notificationAlarm = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationAlarm);
                    ringtone.play();

                    //Doing this because update time function wont use latest timeleftinmilliseconds
                    minutes = (int) (timeLeftInMilliseconds / 1000) / 60;
                    seconds = (int) (timeLeftInMilliseconds / 1000) % 60;
                    timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

                    String meatType = getIntent().getStringExtra("meatType");
                    sendNotification(meatType + " " + meatFoodSpec, "Your " + meatType + " " + meatFoodSpec + " has " + timeLeftFormatted + " left!");
                    vibrate();

                    ChangeTime();
                }

                //send notification when meat is finished
                if ((currentTemp >= finalTemp)&&(restTimerSet == false)) {
                    try {
                        onFinish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }


            @Override
            public void onFinish() {
                Log.d("DEBUG", " THIS IS THE onFinish");
                countdownText.setText("00:00");
                timerRunning = false;

                if(restTime==0){
                    createRestTimer();
                }

                try {
                        if(restTimerSet == false) {
                            String meatType = getIntent().getStringExtra("meatType");
                            sendNotification(meatType + " " + meatFoodSpec, "Your " + meatType + " " + meatFoodSpec + " is done cooking");
                            Uri finishedAlarm = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm);
                            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), finishedAlarm);
                            ringtone.play();
                            countDownTimer.cancel();
                            startButton.setText("Set Rest Timer");
                            // create RestTimer on click
                            startButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    createRestTimer();
                                }
                            });
                        }
                        else {
                            String meatType = getIntent().getStringExtra("meatType");
                            sendNotification(meatType + " " + meatFoodSpec, "Your " + meatType + " " + meatFoodSpec + " is done cooking");
                            Uri notificationAlarm = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);
                            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationAlarm);
                            ringtone.play();
                            restTime=0;
                            createRestTimer();
                        }
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

    private void vibrate(){
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            vibrator.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));
        }
        else{
            vibrator.vibrate(500);
        }
    }

    private void sendNotification(String mealName, String message) {
        //sends a notifications with an intent that brings back to cooking activity
        Intent intent = new Intent(this, CookingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(mealName)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true);

        //NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(001, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Test";
            String description = "Test";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createRestTimer() {
        timeLeftInMilliseconds = restTime * 60000;//convert rest time to millis
        stopTimer();

        if(restTime==0) {
            timerRunning = false;
            if (cThmSwitch.isChecked()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                builder.setMessage(Html.fromHtml("<b>Your Food is ready</b>"))
                        .setCancelable(false)
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (timerRunning) {
                                    stopTimer();
                                }
                                cookingTime = 0;
                                timeLeftInMilliseconds = 0;
                                CookingActivity.super.onBackPressed();
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(Html.fromHtml("<b>Your Food is ready</b>"))
                        .setCancelable(false)
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (timerRunning) {
                                    stopTimer();
                                }
                                cookingTime = 0;
                                timeLeftInMilliseconds = 0;
                                CookingActivity.super.onBackPressed();
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        }
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

    private void ChangeTime(){
        stopTimer();
        startStop();
    }
    private void BluetoothAlert(){
        Log.d("BluetoothLE", "BluetoothAlert() started");
        if(!hasBeenAlerted) {
            hasBeenAlerted = true;
            if (cThmSwitch.isChecked()) {
                alertDialogBT = new AlertDialog.Builder(cookingContext,AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                        .setIcon(R.drawable.ic_bluetooth_disabled_black_24dp)
                        .setTitle("Bluetooth Connection Lost")
                        .setMessage("BBQ Buddy is no longer able to communicate " +
                                "with the Bluetooth device and is trying to " +
                                "reconnect. Please make sure that the device " +
                                "is turned on.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                textBTDisconnect.setVisibility(View.VISIBLE);
                blunoLibrary.scanLeDevice(true);
                Log.d("BluetoothLE", "scanLeDevice from onTick");
            } else {
                alertDialogBT = new AlertDialog.Builder(cookingContext)
                        .setIcon(R.drawable.ic_bluetooth_disabled_black_24dp)
                        .setTitle("Bluetooth Connection Lost")
                        .setMessage("BBQ Buddy is no longer able to communicate" +
                                "with the Bluetooth device and is trying to " +
                                "reconnect. Please make sure that the device " +
                                "is turned on.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                textBTDisconnect.setVisibility(View.VISIBLE);
                blunoLibrary.scanLeDevice(true);
                Log.d("BluetoothLE", "scanLeDevice from onTick");
            }
        }
    }

    private void setMeatImage(String meatType, String meatCut, String doneness)
    {
        Log.d("meatImage", "meatImage started with " + meatType + " " + meatCut + " " + doneness);
        switch (meatType) {
            case "Poultry":
                switch(meatCut){
                    case "Drumstick":
                        Log.d("meatImage", "PoultryDrumStick");
                        meatImage.setImageResource(R.drawable.poultry_drumstick);
                        break;
                    case "Breast":
                        meatImage.setImageResource(R.drawable.poultry_breast);
                        break;
                    case "Wings":
                        meatImage.setImageResource(R.drawable.poultry_wings);
                        break;
                    default:
                        break;
                }
                break;
            case "Beef":
                switch(meatCut) {
                    case "Steak":
                        Log.d("meatImage", "."+doneness+".");
                        if(doneness.equals("Rare")){
                            meatImage.setImageResource(R.drawable.steak_rare);
                        }
                        else if(doneness.equals("Medium")){
                            meatImage.setImageResource(R.drawable.steak_medium);
                        }
                        else if(doneness.equals("Well")){
                            meatImage.setImageResource(R.drawable.steak_well);
                        }
                        break;
                    case "Tenderloin":
                        if(doneness.equals("Rare")){
                            meatImage.setImageResource(R.drawable.tenderloin_rare);
                        }
                        else if(doneness.equals("Medium")){
                            meatImage.setImageResource(R.drawable.tenderloin_medium);
                        }
                        else if(doneness.equals("Well")){
                            meatImage.setImageResource(R.drawable.tenderloin_well);
                        }
                        break;
                    case "GroundPatty":
                        meatImage.setImageResource(R.drawable.beef_patty);
                        break;
                    default:
                        break;
                }
                break;
            case "Pork":
                switch(meatCut) {
                    case "HamSlices":
                        meatImage.setImageResource(R.drawable.pork_slices);
                        break;
                    case "Chop":
                        meatImage.setImageResource(R.drawable.pork_chop);
                        break;
                    case "Tenderloin":
                        meatImage.setImageResource(R.drawable.pork_tenderloin);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }
}

