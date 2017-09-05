package com.example.ntd.tpapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by ntd on 29/09/2015.
 */
public class WifiAdapter  extends BaseAdapter{

    Context mContext;
    String[] ssid;
    int[] linkSpeed;
    WifiAdapter(Context context,String[]ssid,int[] linkSpeed)
    {
        this.mContext=context;
        this.ssid=ssid;
        this.linkSpeed=linkSpeed;
    }
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater mInflater =
                (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.list_row_wifi, parent, false);
        TextView textView = (TextView)view.findViewById(R.id.txtssd);
        textView.setText(ssid[position]);
        TextView textView2 = (TextView)view.findViewById(R.id.txtsignal);
        textView2.setText(linkSpeed[position]);
        return view;
    }
}
