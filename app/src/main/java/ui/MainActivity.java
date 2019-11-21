package ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.bbqbuddy.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static ui.SettingsActivity.SHARED_PREFS;
import static ui.SettingsActivity.TempUnitSwitch;
import static ui.SettingsActivity.ThemeSwitch;
import static ui.SettingsActivity.WeightUnitSwitch;

public class MainActivity extends AppCompatActivity {

    //invisible Switch Setup
    Switch thmSwitch;
    Switch tmpSwitch;
    Switch wtSwitch;

    //Elements for the list view in main activity
    ExpandableListView listView;
    List<String> foodTypes;
    HashMap<String,List<String>> listOptions;
    CustomListView customListView;
    Integer[] imageIds;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.settingsItem){
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.darktheme);
        }else setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Switch initialization
        tmpSwitch = findViewById(R.id.tmpswitch);
        thmSwitch = findViewById(R.id.thmswitch);
        wtSwitch = findViewById(R.id.wtswitch);

        //retrieve boolean value from settings page
        Boolean thmChecked;
        Boolean tmpChecked;
        Boolean wtChecked;

        SharedPreferences preferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        thmChecked = preferences.getBoolean(ThemeSwitch,false);
        tmpChecked = preferences.getBoolean(TempUnitSwitch, false);
        wtChecked = preferences.getBoolean(WeightUnitSwitch, false);

        //set the hidden switches to value of settings page
        thmSwitch.setChecked(thmChecked);
        tmpSwitch.setChecked(tmpChecked);
        wtSwitch.setChecked(wtChecked);

        if (thmSwitch.isChecked()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //initialize the containers and the list view
        listView = findViewById(R.id.foodView);
        foodTypes = new ArrayList<>();
        listOptions = new HashMap<>();
        imageIds = new Integer[]{R.drawable.chicken,R.drawable.beef,R.drawable.pork};
        customListView = new CustomListView(this, foodTypes,listOptions,imageIds,this.getSupportFragmentManager());
        listView.setAdapter(customListView);
        initializeData();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            Log.d("BluetoothLE", "REQUEST BLE ENABLE");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            IntentFilter BTAdapterFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            this.registerReceiver(BTAdapterReceiver, BTAdapterFilter);
        }


    }

    private void initializeData(){
        //add food categories to list
        foodTypes.add(getString(R.string.poultry));
        foodTypes.add(getString(R.string.beef));
        foodTypes.add(getString(R.string.pork));

        //create temporary array for resources in strings.xml
        String[] mealOptions;

        //copy poultry options into list
        List<String> poultryList = new ArrayList<>();
        mealOptions = getResources().getStringArray(R.array.poultry);
        for(String item: mealOptions){
            poultryList.add(item);
        }

        //copy beef options into list
        List<String> beefList = new ArrayList<>();
        mealOptions = getResources().getStringArray(R.array.beef);
        for(String item: mealOptions){
            beefList.add(item);
        }

        //copy pork options into list
        List<String> porkList = new ArrayList<>();
        mealOptions = getResources().getStringArray(R.array.pork);
        for(String item: mealOptions){
            porkList.add(item);
        }

        //place lists in the map
        listOptions.put(foodTypes.get(0),poultryList);
        listOptions.put(foodTypes.get(1),beefList);
        listOptions.put(foodTypes.get(2),porkList);
        customListView.notifyDataSetChanged();
    }

    private final BroadcastReceiver BTAdapterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivity(enableBtIntent);
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                this.unregisterReceiver(BTAdapterReceiver);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("BluetoothLE", "onActivityResult");
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
            }
            else if(resultCode == RESULT_CANCELED) {
                Log.d("BluetoothLE", "REQUEST BLE ENABLE");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
}
