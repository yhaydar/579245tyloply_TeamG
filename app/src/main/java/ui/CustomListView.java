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

    private String[] foodNames;
    private Integer[] imageIds;
    private ArrayAdapter[] adapters;
    private Activity context;

    public CustomListView(Activity context, String[] foodNames, Integer[] imgid, ArrayAdapter[] adapters) {
        super(context, R.layout.listview_layout,foodNames);
        this.context = context;
        this.foodNames = foodNames;
        this.imageIds = imgid;
        this.adapters = adapters;
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
        viewHolder.getImageView().setImageResource(imageIds[position]);
        viewHolder.getTextView().setText(foodNames[position]);
        viewHolder.getSpinner().setAdapter(adapters[position]);

        return r;
    }
}
