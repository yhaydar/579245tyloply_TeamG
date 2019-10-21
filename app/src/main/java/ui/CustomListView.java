package ui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bbqbuddy.R;

public class CustomListView extends ArrayAdapter<String> {

    private String[] foodName;
    private Integer[] imgid;
    private Activity context;

    public CustomListView(Activity context, String[] foodName,Integer[] imgid) {
        super(context, R.layout.listview_layout,foodName);
        this.context = context;
        this.foodName = foodName;
        this.imgid = imgid;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View r = convertView;
        ViewHolder viewHolder = null;
        if(r == null){
            LayoutInflater inflater = context.getLayoutInflater();
            r = inflater.inflate(R.layout.listview_layout,null,true);
            viewHolder = new ViewHolder(r);
            r.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) r.getTag();
        }
        viewHolder.getImageView().setImageResource(imgid[position]);
        viewHolder.getTextView().setText(foodName[position]);

        return r;
    }
}
