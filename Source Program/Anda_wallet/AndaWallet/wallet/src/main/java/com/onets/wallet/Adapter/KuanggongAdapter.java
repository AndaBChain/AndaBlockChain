package com.onets.wallet.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.onets.wallet.R;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/6/22.
 */

public class KuanggongAdapter extends BaseAdapter {
    List<Map<String,String>> list;
    Context context;

    public KuanggongAdapter(List<Map<String, String>> list, Context context) {
        this.list = list;
        this.context = context.getApplicationContext();
    }
    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public int getCount() {
        return list.size();
    }
    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, final ViewGroup viewGroup) {
        Map<String,String> map = list.get(i);
        final ViewHolder viewHolder;
        if(view==null){
            viewHolder=new ViewHolder();
            view= LayoutInflater.from(context).inflate(R.layout.item_kuanggong,null);
            viewHolder.kuanggongming= (TextView) view.findViewById(R.id.tv_item_kuanggongming);
            viewHolder.kuangchi= (TextView) view.findViewById(R.id.tv_item_kuangchi);
            viewHolder.zhuangtai= (TextView) view.findViewById(R.id.tv_item_zhuangtai);
            viewHolder.imageView= (ImageView) view.findViewById(R.id.image_kuanggong_stop);
            viewHolder.progressBar= (ProgressBar) view.findViewById(R.id.proBar);

            viewHolder.progressBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.progressBar.setVisibility(View.GONE);
                    viewHolder.imageView.setVisibility(View.VISIBLE);
                }
            });
            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                    viewHolder.imageView.setVisibility(View.GONE);
                }
            });
            view.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) view.getTag();
        }
        viewHolder.kuanggongming.setText(map.get("kuanggongming"));
        viewHolder.kuangchi.setText(map.get("kuangchi"));
        viewHolder.zhuangtai.setText(map.get("zhuangtai"));
        return view;
    }
    class ViewHolder{
        TextView kuanggongming,kuangchi,zhuangtai;
        ImageView imageView;
        ProgressBar progressBar;
    }
}
