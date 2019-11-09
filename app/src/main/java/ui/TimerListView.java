package ui;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.bbqbuddy.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimerListView extends Activity {

    private ListView lvItems;
    private List<Product> lstProducts;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_list_view);

        lvItems = (ListView) findViewById(R.id.lvItems);
        lstProducts = new ArrayList<>();
        lstProducts.add(new Product("B", System.currentTimeMillis() + 20000));
        lstProducts.add(new Product("C", System.currentTimeMillis() + 20000));
        lstProducts.add(new Product("D", System.currentTimeMillis() + 20000));
        lstProducts.add(new Product("E", System.currentTimeMillis() + 20000));
        lstProducts.add(new Product("F", System.currentTimeMillis() + 20000));
        lstProducts.add(new Product("G", System.currentTimeMillis() + 30000));
        lstProducts.add(new Product("H", System.currentTimeMillis() + 20000));
        lstProducts.add(new Product("I", System.currentTimeMillis() + 20000));
        lstProducts.add(new Product("J", System.currentTimeMillis() + 40000));
        lstProducts.add(new Product("K", System.currentTimeMillis() + 20000));
        lstProducts.add(new Product("L", System.currentTimeMillis() + 50000));
        lstProducts.add(new Product("M", System.currentTimeMillis() + 60000));
        lstProducts.add(new Product("N", System.currentTimeMillis() + 20000));
        lstProducts.add(new Product("O", System.currentTimeMillis() + 10000));

        lvItems.setAdapter(new CountdownAdapter(TimerListView.this, lstProducts));
    }

    private class Product {
        String name;
        long expirationTime;

        public Product(String name, long expirationTime) {
            this.name = name;
            this.expirationTime = expirationTime;
        }
    }


    public class CountdownAdapter extends ArrayAdapter<Product> {

        private LayoutInflater lf;
        private List<ViewHolder> lstHolders;
        private Handler mHandler = new Handler();
        private Runnable updateRemainingTimeRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (lstHolders) {
                    long currentTime = System.currentTimeMillis();
                    for (ViewHolder holder : lstHolders) {
                        holder.updateTimeRemaining(currentTime);
                    }
                }
            }
        };

        public CountdownAdapter(Context context, List<Product> objects) {
            super(context, 0, objects);
            lf = LayoutInflater.from(context);
            lstHolders = new ArrayList<>();
            startUpdateTimer();
        }

        private void startUpdateTimer() {
            Timer tmr = new Timer();
            tmr.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(updateRemainingTimeRunnable);
                }
            }, 1000, 1000);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = lf.inflate(R.layout.list_item_t, parent, false);
                holder.tvProduct = (TextView) convertView.findViewById(R.id.tvProduct);
                holder.tvTimeRemaining = (TextView) convertView.findViewById(R.id.tvTimeRemaining);
                convertView.setTag(holder);
                synchronized (lstHolders) {
                    lstHolders.add(holder);
                }
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.setData(getItem(position));

            return convertView;
        }
    }

    private class ViewHolder {
        TextView tvProduct;
        TextView tvTimeRemaining;
        Product mProduct;

        public void setData(Product item) {
            mProduct = item;
            tvProduct.setText(item.name);
            updateTimeRemaining(System.currentTimeMillis());
        }

        public void updateTimeRemaining(long currentTime) {
            long timeDiff = mProduct.expirationTime - currentTime;
            if (timeDiff > 0) {
                int seconds = (int) (timeDiff / 1000) % 60;
                int minutes = (int) ((timeDiff / (1000 * 60)) % 60);
                int hours = (int) ((timeDiff / (1000 * 60 * 60)) % 24);
                tvTimeRemaining.setText(hours + " hrs " + minutes + " mins " + seconds + " sec");
            } else {
                tvTimeRemaining.setText("Expired!!");
            }
        }
    }
}