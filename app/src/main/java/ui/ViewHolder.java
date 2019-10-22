package ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bbqbuddy.R;

public class ViewHolder {
    private TextView textView;
    private ImageView imageView;
    //private Spinner spinner;

    ViewHolder(View view){
        this.textView = view.findViewById(R.id.foodName);
        this.imageView = view.findViewById(R.id.imageView);
    }

    public TextView getTextView(){
        return this.textView;
    }

    public ImageView getImageView(){
        return this.imageView;
    }
}
