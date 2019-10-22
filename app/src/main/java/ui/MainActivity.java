package ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bbqbuddy.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    //temporary button to access second activity (testing purposes)
    private Button tempButton;


    ListView listView;
    String[] foodNames = {"Poultry","Beef","Pork"};
    Integer[] imageIds = {R.drawable.chicken,R.drawable.beef,R.drawable.pork};


    String[] poultryList = {"Chicken","Turkey"};
    String[] beefList = {"Burger","Steak"};
    String[] porkList = {"Roast","Bacon"};

    ArrayAdapter<String> poultryAdapter;
    ArrayAdapter<String> beefAdapter;
    ArrayAdapter<String> porkAdapter;
    ArrayAdapter[] adapters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        poultryAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout, poultryList);
        beefAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout, beefList);
        porkAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout,porkList);
        adapters = new ArrayAdapter[] { poultryAdapter,beefAdapter,porkAdapter };

        listView = findViewById(R.id.foodView);
        CustomListView customListView = new CustomListView(this, foodNames,imageIds,adapters);
        listView.setAdapter(customListView);

        //temp button intent
        tempButton = findViewById(R.id.tempButton);
        tempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity2();
            }
        });

        }

        //temp function to open activity2 (to be removed later)
         public void openActivity2(){
        Intent intent = new Intent(this, cookingActivity.class);
        startActivity(intent);

    }


}
