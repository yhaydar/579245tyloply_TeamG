package ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.bbqbuddy.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //temp switch
    private Switch temporary;

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
        }
        else setTheme(R.style.AppTheme);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //tempswtich
        temporary = findViewById(R.id.tempswitch);
        if (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            temporary.setChecked(true);
        }

        temporary.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    restartApp();
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    restartApp();
                }
            }
        });


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
            startActivity(enableBtIntent);
        }

        IntentFilter BTAdapterFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(BTAdapterReceiver, BTAdapterFilter);

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

    public void restartApp(){
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
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

}
