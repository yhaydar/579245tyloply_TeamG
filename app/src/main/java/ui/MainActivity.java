package ui;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bbqbuddy.R;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    String[] foodNames = {"Poultry","Beef","Pork"};
    Integer[] imgid = {R.drawable.chicken,R.drawable.beef,R.drawable.pork};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.foodView);
        CustomListView customListView = new CustomListView(this,foodNames,imgid);
        listView.setAdapter(customListView);
    }
}
