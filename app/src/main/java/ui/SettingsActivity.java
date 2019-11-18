package ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bbqbuddy.R;

public class SettingsActivity extends AppCompatActivity {
    Switch themeSwitch;
    Switch tempUnitSwitch;
    Switch weightUnitSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        themeSwitch = findViewById(R.id.themeSwitch);
        tempUnitSwitch = findViewById(R.id.tempUnitSwitch);
        weightUnitSwitch = findViewById(R.id.weightUnitSwitch);

        setupSwitches();
    }

    private void setupSwitches(){
        themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO do shared preferences stuff
            }
        });

        tempUnitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO do shared preferences stuff
            }
        });

        weightUnitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO do shared preferences stuff
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        return true;
    }
}
