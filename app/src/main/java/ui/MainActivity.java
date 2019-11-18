package ui;

import android.content.Intent;
import android.os.Bundle;
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
}
