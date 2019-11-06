package ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.bbqbuddy.R;

public class FoodSpecDialog extends DialogFragment {
    View view;
    TextView specTitle;
    TextView subTitle;
    Spinner spinner;
    RadioGroup radioGroup;
    Button cancelButton;
    Button doneButton;
    String meatType;
    String meatCut;
    String[] mealOptions;

    public View onCreateView(LayoutInflater inflator, @Nullable ViewGroup container, Bundle savedInstanceState){
        View view = inflator.inflate(R.layout.specifications_fragment, container, false);
        this.view = view;

        //map the objects to their values in the layout
        specTitle = view.findViewById(R.id.specTitleTextView);
        subTitle = view.findViewById(R.id.subTitleTextView);
        radioGroup = view.findViewById(R.id.donenessGroup);
        cancelButton = view.findViewById(R.id.cancelButton);
        doneButton = view.findViewById(R.id.doneButton);
        spinner = view.findViewById(R.id.spinner);

        //get info from the bundle
        meatType = getArguments().getString("meatType");
        meatCut = getArguments().getString("meatCut");
        mealOptions = getArguments().getStringArray("optionsArray");

        //add listeners to both buttons
        setupDialogFragment();
        setupDoneButton();
        setupCancelButton();
        setupSpinner();

        return view;
    }
    private void setupSpinner(){
        if(mealOptions.length > 0){
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_layout, mealOptions);
            spinner.setAdapter(adapter);
        }
        else{
            spinner.setEnabled(false);
        }
    }

    private void setupDialogFragment() {
       // specTitle.setText(getArguments().getString("meal"));
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
                boolean spinnerReq = false;
                boolean radioButtonReq = false;

                //Make intent and send all the cooking values to cooking activity
                Intent intent = new Intent(getActivity(), CookingActivity.class);
                intent.putExtra("meatType", meatType);
                intent.putExtra("meatCut", meatCut);
                if(spinner.isEnabled()) {
                    if(!(spinner.getSelectedItem() == null)) {
                        intent.putExtra("foodSpec", spinner.getSelectedItem().toString());
                        spinnerReq = true;
                    }
                    else{
                        spinnerReq = false;
                    }
                }
                else{
                    //if spinner is disabled, we ignore its input
                    spinnerReq = true;
                }
                if(radioGroup.isEnabled()) {
                    if(!(radioGroup.getCheckedRadioButtonId() == -1)) {
                        RadioButton clickedButton = view.findViewById(radioGroup.getCheckedRadioButtonId());
                        intent.putExtra("doneness", clickedButton.getText().toString());
                        radioButtonReq = true;
                    }
                    else{
                        radioButtonReq = false;
                    }
                }
                else{
                    //if radiogroup is disabled, we can ignore it
                    radioButtonReq = true;
                }

                //if both the spinner and radio button requirements are good, we can start the intent
                if(radioButtonReq && spinnerReq){
                    startActivity(intent);
                }

            }
        });
    }
}
