package ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.bbqbuddy.R;

public class setTimerDialog extends AppCompatDialogFragment {

    private EditText setTimeEditText;

    private SetTimerDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.set_timer_frag, null);
        builder.setView(view)
                .setTitle(null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!setTimeEditText.getText().toString().trim().isEmpty()) {
                            int timeEntered = Integer.parseInt(setTimeEditText.getText().toString());

                            listener.applyValue(timeEntered);
                        }
                    }
                });

        setTimeEditText = view.findViewById(R.id.setTimeEditText);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (SetTimerDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement SetTimerDialogListener");
        }
    }

    public interface SetTimerDialogListener{
        void applyValue(int timeEntered);

    }
}
