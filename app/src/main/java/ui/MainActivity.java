package ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bbqbuddy.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //temporary button to access second activity (testing purposes)
    private Button tempButton;

    //Elements for the list view in main activity
    ExpandableListView listView;
    List<String> foodTypes;
    HashMap<String,List<String>> listOptions;
    CustomListView customListView;
    Integer[] imageIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize the containers and the list view
        listView = findViewById(R.id.foodView);
        foodTypes = new ArrayList<>();
        listOptions = new HashMap<>();
        imageIds = new Integer[]{R.drawable.chicken,R.drawable.beef,R.drawable.pork};
        customListView = new CustomListView(this, foodTypes,listOptions,imageIds,this.getSupportFragmentManager());
        listView.setAdapter(customListView);
        initializeData();

        //temp button intent
        tempButton = findViewById(R.id.tempButton);
        tempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity2();
            }
        });

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

    //temp function to open activity2 (to be removed later)
     public void openActivity2(){
        Intent intent = new Intent(this, CookingActivity.class);
        startActivity(intent);
    }
}
