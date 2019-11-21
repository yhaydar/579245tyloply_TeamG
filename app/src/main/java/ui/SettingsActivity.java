package ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.bbqbuddy.R;

public class SettingsActivity extends AppCompatActivity {
    Switch themeSwitch;
    Switch tempUnitSwitch;
    Switch weightUnitSwitch;

    Button settingsSaveButton;
    Button settingsResetButton;

    public static final String SHARED_PREFS  = "sharedPrefs";
    public static final String ThemeSwitch = "themeSwitch";
    public static final String TempUnitSwitch = "tempUnitSwitch";
    public static final String WeightUnitSwitch = "weightUnitSwitch";

    private boolean themeSwitchOnOff;
    private boolean tempUnitSwitchOnOff;
    private boolean weightUnitSwitchOnOff;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setting up dark mode if checked.
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.darktheme);
        }
        else setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        themeSwitch = findViewById(R.id.themeSwitch);
        tempUnitSwitch = findViewById(R.id.tempUnitSwitch);
        weightUnitSwitch = findViewById(R.id.weightUnitSwitch);

        settingsResetButton = findViewById(R.id.settingsResetButton);
        settingsSaveButton = findViewById(R.id.settingsSaveButton);

        setupSwitches();
        setupButtons();
        loadData();
        updateViews();
    }

    private void setupSwitches(){
        themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    //restartApp();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    //restartApp();
                }

            }
        });

        tempUnitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });

        weightUnitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
    }

    public void setupButtons(){
        settingsSaveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                saveData();
            }
        });


        settingsResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                resetDefault();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (themeSwitch.isChecked()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
            builder.setMessage(Html.fromHtml("<b>Are you sure you want to go Back without saving?</b>"
            ))
                    .setCancelable(false)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            saveData();
                            SettingsActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

            builder.setMessage(Html.fromHtml("<b>Are you sure you want to go back without saving?</b>"
                    ))
                    .setCancelable(false)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            saveData();
                            SettingsActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
    public boolean onOptionsItemSelected(MenuItem item){
        onBackPressed();
        return true;
    }

    public void saveData(){
        Intent settingsIntent = new Intent(this, MainActivity.class);
        this.startActivity(settingsIntent);

        SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(ThemeSwitch,themeSwitch.isChecked());
        editor.putBoolean(TempUnitSwitch,tempUnitSwitch.isChecked());
        editor.putBoolean(WeightUnitSwitch,weightUnitSwitch.isChecked());

        editor.apply();

    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        themeSwitchOnOff = sharedPreferences.getBoolean(ThemeSwitch, false);
        tempUnitSwitchOnOff = sharedPreferences.getBoolean(TempUnitSwitch,false);
        weightUnitSwitchOnOff = sharedPreferences.getBoolean(WeightUnitSwitch,false);
    }
    public void updateViews(){
        themeSwitch.setChecked(themeSwitchOnOff);
        tempUnitSwitch.setChecked(tempUnitSwitchOnOff);
        weightUnitSwitch.setChecked(weightUnitSwitchOnOff);
    }

    public void resetDefault(){
        themeSwitch.setChecked(false);
        tempUnitSwitch.setChecked(false);
        weightUnitSwitch.setChecked(false);
    }

    public void restartApp(){
        Intent restartIntent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(restartIntent);
        finish();

    }
}
