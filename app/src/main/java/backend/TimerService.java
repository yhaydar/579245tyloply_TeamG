package backend;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.bbqbuddy.R;

import java.util.Locale;

import ui.CookingActivity;

public class TimerService extends Service {
    //todo
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

    private int finalTemp;
    private double tempRate;
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


    private boolean timerRunning; // tells us if timer is running
    private boolean restTimerSet = false;
    private boolean timerStarted = false;

    private boolean DegressC = false;

    private String CHANNEL_ID = "1";

    private Context cookingContext = this;

    private Switch cThmSwitch;
    private Switch cTmpSwitch;
    private Switch cWtSwitch;

    private boolean isthereconnecion;
    private double currentTemp;
    //todo

    private final static String TAG = "BroadcastService";

    public static final String COUNTDOWN_BR = "backend.countdown_br";
    Intent broadcastIntent = new Intent(COUNTDOWN_BR);

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        countDownTimer.cancel();
        Log.i(TAG, "Timer cancelled");
        timerRunning = false;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        cookingTime = intent.getIntExtra("cookingTime",0);
        finalTemp = intent.getIntExtra("finalTemp",0);
        flipTime = intent.getIntExtra("flipTime",0);

        Log.d("DEBUG@", " "+cookingTime);
        timeLeftInMilliseconds = 60000 * cookingTime;
        updateTimerUI();
        startTimer();

        createNotificationChannel();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    //TODO stuff from cooking activity

    public void startStop() {
        if (timerRunning) {
            if (restTimerSet == false) {
                stopTimer();
//                resetButton.setVisibility(View.VISIBLE);
            } else {
                stopTimer();
//                resetButton.setVisibility(View.INVISIBLE);
            }
        } else {
            if (restTimerSet == false) {
                startTimer();
//                resetButton.setVisibility(View.VISIBLE);
            } else {
                startTimer();
//                resetButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void startTimer() {
        Log.d("DEBUG", " THIS IS THE startTimer");
        endTime = System.currentTimeMillis() + timeLeftInMilliseconds;
        nextFlipTime = cookingTime - flipTime;
//        if (restTimerSet == false) {
//            if (timerStarted == false){
//                startTimeInMillis = cookingTime*60000;
//                timeLeftInMilliseconds = startTimeInMillis;
//                updateTimerUI();
//                timerStarted = true;
//            }
//        }

        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
            // CountDownTimer(time left, countdown interval)

            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("DEBUG", cookingTime + " THIS IS THE COOKING TIME");
                //l is variable that contains remaining time
                timeLeftInMilliseconds = millisUntilFinished;
                Log.d("TIME",""+timeLeftInMilliseconds);
                int minutes = (int) (timeLeftInMilliseconds / 1000) / 60;
                int seconds = (int) (timeLeftInMilliseconds / 1000) % 60;
                String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                //double barmax = (double) (startTimeInMillis);
                //double progress = (double) (timeLeftInMilliseconds);
               // double barVal = (progress / barmax *100);
//                progressBar.setProgress((int)barVal);
                updateTimerUI();

                //if the app is run on the emulator
//                if(blunoLibrary.mBluetoothLeService != null) {
//                    if (blunoLibrary.mBluetoothLeService.mConnectionState == 0) {
//
//                        BluetoothAlert();
//                        isthereconnecion = false;
//                    } else {
//                        hasBeenAlerted = false;
//                        isthereconnecion = true;
//                        textBTDisconnect.setVisibility(View.INVISIBLE);
//                    }
//                }

                if(isthereconnecion) {
                    currentTemp = blunoLibrary.getCurrentTemp();
                }
                else{
                    currentTemp = 0;
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
                        (timeLeftInMilliseconds >= 0.98*((nextFlipTime) * 60000))) && !doneFlipping){
                    Uri notificationAlarm = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationAlarm);
                    ringtone.play();

                    String meatType = "chicken";// getIntent().getStringExtra("meatType"); TODO add intent for meat type
                    sendNotification(meatType + " " + meatFoodSpec, "Your " + meatType + " " + meatFoodSpec + " has needs to be flipped!");
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
                    timeLeftInMilliseconds = (long) ((finalTemp - currentTemp)/tempRate);
                    Log.d(TAG, "Second time measurement   " + timeLeftInMilliseconds);
                    measuredSecondTime = true;

                    Uri notificationAlarm = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationAlarm);
                    ringtone.play();

                    minutes = (int) (timeLeftInMilliseconds / 1000) / 60;
                    seconds = (int) (timeLeftInMilliseconds / 1000) % 60;

                    timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

                    String meatType = "Chicken";//getIntent().getStringExtra("meatType"); TODO add intent for meat type
                    sendNotification(meatType + " " + meatFoodSpec, "Your " + meatType + " " + meatFoodSpec + " has " + timeLeftFormatted + " left!");
                    vibrate();

                    ChangeTime();
                }

                //send notification when meat is finished
                if (currentTemp >= finalTemp) {
                    try {
                        Log.d("TEMP",currentTemp + " Current temp");
                        Log.d("TEMP",finalTemp + " final temp");
                        Uri finishedAlarm = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm);
                        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), finishedAlarm);
                        ringtone.play();
                        vibrate();
                        onFinish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }


            @Override
            public void onFinish() {
                Log.d("DEBUG", " THIS IS THE onFinish");
                //countdownText.setText("00:00");
                //timerRunning = false;

                if(restTime==0){
//                    createRestTimer();
                }
                try {
                    Uri finishedAlarm = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), finishedAlarm);
                    ringtone.play();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
                .start();


        timerRunning = true;
    }
    public void stopTimer() {
        countDownTimer.cancel();
        timerRunning = false;
    }

    private void resetTimer() {
        if (timerRunning == true) {
            stopTimer();
            timeLeftInMilliseconds = startTimeInMillis;
            updateTimerUI();
            timerRunning = false;
        } else {
            timeLeftInMilliseconds = startTimeInMillis;
            updateTimerUI();
            timerRunning = false;
        }
    }

    public void updateTimerUI() {
        int minutes = (int) (timeLeftInMilliseconds / 1000) / 60;
        int seconds = (int) (timeLeftInMilliseconds / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        broadcastIntent.putExtra("time",timeLeftFormatted);
        sendBroadcast(broadcastIntent);
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
//        stopTimer();

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
                                //CookingActivity.super.onBackPressed(); TODO find solution for this
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
                                //CookingActivity.super.onBackPressed(); TODO find solution for this
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        }
        updateTimerUI();
        timerRunning = false;
//        startButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startStop();
//            }
//        });
        restTimerSet =true;
        resetButton.setVisibility(View.INVISIBLE);
    }

    private void ChangeTime(){
        stopTimer();
    //    startStop();
    }
    private void BluetoothAlert(){
        if(!hasBeenAlerted) {
            hasBeenAlerted = true;
            if (cThmSwitch.isChecked()) {
                alertDialogBT = new AlertDialog.Builder(cookingContext,AlertDialog.THEME_DEVICE_DEFAULT_DARK)
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
}