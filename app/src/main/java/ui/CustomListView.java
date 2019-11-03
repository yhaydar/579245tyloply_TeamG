package ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.example.bbqbuddy.R;

import java.util.HashMap;
import java.util.List;

public class CustomListView extends BaseExpandableListAdapter {
    private Context context;
    private List<String> foodTypes;
    private HashMap<String, List<String>> listOptions;
    private Integer[] imageIds;
    private FragmentManager fragmentManager;

    public CustomListView(Context context, List<String> foodTypes, HashMap<String,List<String>> listOptions, Integer[] imageIds, FragmentManager fragmentManager){
        this.context = context;
        this.foodTypes = foodTypes;
        this.listOptions = listOptions;
        this.imageIds = imageIds;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        //inflate view with custom list view layout
        ViewHolder viewHolder;
        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.listview_layout, null, true);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //set images and text in list view item
        viewHolder.getImageView().setImageResource(imageIds[groupPosition]);
        viewHolder.getTextView().setText(foodTypes.get(groupPosition));

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String child = (String) getChild(groupPosition,childPosition);
        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item,null);
        }
        //set text in child items
        TextView textView = convertView.findViewById(R.id.list_child);
        textView.setText(child);

        //set onclick listener for sub items
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create dialog box
                FoodSpecDialog dialog = new FoodSpecDialog();
                //pass meat info to the box
                Bundle info = new Bundle();
                info.putString("meatType",foodTypes.get(groupPosition));
                info.putString("meatCut",listOptions.get(foodTypes.get(groupPosition)).get(childPosition));
                dialog.setArguments(info);
                dialog.show(fragmentManager,"Meat Specifications");
            }
        });
        return convertView;
    }
    @Override
    public int getGroupCount() {
        return foodTypes.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listOptions.get(this.foodTypes.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.foodTypes.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listOptions.get(this.foodTypes.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
