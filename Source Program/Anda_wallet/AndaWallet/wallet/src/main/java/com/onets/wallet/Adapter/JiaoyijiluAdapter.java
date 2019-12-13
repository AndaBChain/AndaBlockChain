package com.onets.wallet.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.onets.wallet.R;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/6/27.
 */

public class JiaoyijiluAdapter extends BaseAdapter {

    List<Map<String,String>> list;
    Context context;

    public JiaoyijiluAdapter(List<Map<String, String>> list, Context context) {
        this.list = list;
        this.context = context.getApplicationContext();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Map<String,String> map=list.get(position);

        ViewHolder holder;

        if (convertView==null){

            holder=new ViewHolder();

            convertView= LayoutInflater.from(context).inflate(R.layout.item_jiaoyijilu,null);

            holder.riqi= (TextView) convertView.findViewById(R.id.riqi_3);
            holder.flag= (TextView) convertView.findViewById(R.id.flag_3);
            holder.message= (TextView) convertView.findViewById(R.id.message_3);
            holder.btc= (TextView) convertView.findViewById(R.id.sum_3);

            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }
        holder.riqi.setText(map.get("riqi"));
        holder.flag.setText(map.get("flag"));
        if(map.get("flag").equals("send")){
            holder.flag.setTextColor(Color.RED);
        }else if(map.get("flag").equals("receive")){
            holder.flag.setTextColor(Color.GREEN);
        }
        holder.message.setText(map.get("message"));
        holder.btc.setText(map.get("sum"));

        return convertView;
    }
    class ViewHolder{
        TextView riqi,flag,message,btc;
    }
}
