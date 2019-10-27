package ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.bbqbuddy.R;

public class FoodSpecDialog extends DialogFragment {

    EditText weightText;
    TextView specTitle;
    TextView subTitle;
    RadioGroup radioGroup;
    Button cancelButton;
    Button doneButton;
    String meatType;

    public View onCreateView(LayoutInflater inflator, @Nullable ViewGroup container, Bundle savedInstanceState){
        View view = inflator.inflate(R.layout.specifications_fragment, container, false);

        //map the objects to their values in the layout
        weightText = view.findViewById(R.id.weightEditText);
        specTitle = view.findViewById(R.id.specTitleTextView);
        subTitle = view.findViewById(R.id.subTitleTextView);
        radioGroup = view.findViewById(R.id.donenessGroup);
        cancelButton = view.findViewById(R.id.cancelButton);
        doneButton = view.findViewById(R.id.doneButton);
        meatType = getArguments().getString("meatType");
        //add listeners to both buttons
        setupDialogFragment();
        setupDoneButton();
        setupCancelButton();

        return view;
    }

    private void setupDialogFragment() {
        specTitle.setText(getArguments().getString("meal"));
        //disable checkboxes for rare, medium, well if it isn't beef
        Log.d("debug", meatType);

        if(!(meatType.equals(getString(R.string.beef)))){
            radioGroup.setEnabled(false);
            for(int i = 0; i< radioGroup.getChildCount(); i++){
                radioGroup.getChildAt(i).setEnabled(false);
            }
        }
    }

    private void setupCancelButton() {
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //close dialog when canceling
                getDialog().dismiss();
            }
        });
    }

    private void setupDoneButton() {
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String weight = weightText.getText().toString();

                //TODO retrive timer data from the database
                if(radioGroup.isEnabled() == true){
                    //checks if a weight was entered and if an item was checked in radio group
                    if(!(weight.equals("") || radioGroup.getCheckedRadioButtonId() == -1 )) {
                        Intent intent = new Intent(getActivity(), CookingActivity.class);
                        intent.putExtra("foodWeight", weight);
                        startActivity(intent);
                    }
                }
                else{
                    //checks if a weight was entered and if an item was checked in radio group
                    if(!(weight.equals(""))) {
                        Intent intent = new Intent(getActivity(), CookingActivity.class);
                        intent.putExtra("foodWeight", weight);
                        startActivity(intent);
                    }
                }
            }
        });
    }
}
