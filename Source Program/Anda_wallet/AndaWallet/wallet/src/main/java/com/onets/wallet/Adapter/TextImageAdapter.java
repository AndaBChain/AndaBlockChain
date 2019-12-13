package com.onets.wallet.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onets.wallet.Fragment_Chongzhi.TextImageBean;
import com.onets.wallet.R;

import java.util.List;

/**
 * 文本图像适配器
 * Created by Hasee on 2018/1/14.
 */

public class TextImageAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;//布局填充器
    private List<TextImageBean> list;//文字图片基类列表
    private int itemType = 0;
    private int icon_size = 0;//图标尺寸

    public TextImageAdapter(Context context, List<TextImageBean> list) {
        this.context = context.getApplicationContext();
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    /**
     * 根据输入的值设置图标尺寸
     * @param icon_size
     */
    public void setIconSize(int icon_size) {
        this.icon_size = icon_size;
        notifyDataSetChanged();
    }

    /*获取当前列表中的元素数量*/
    @Override
    public int getCount() {
        return list.size();
    }

    /*获取指定位置的列表项*/
    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    /*获取指定位置的列表项值*/
    @Override
    public long getItemId(int i) {
        return i;
    }

    /*文字列表样式设置*/
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if (view == null) {
            view = inflater.inflate(R.layout.minepool_item, viewGroup, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if (icon_size != 0) {
            viewHolder.icon.setMaxWidth(icon_size);
            viewHolder.icon.setMaxHeight(icon_size);
        }
        viewHolder.minePool.setText(list.get(i).getName());
        viewHolder.icon.setBackgroundResource(list.get(i).getIcon());

        return view;
    }

    /**
     *  视图固定器 icon text
     */
    class ViewHolder {
        TextView minePool;
        ImageView icon;
        RelativeLayout layout_bg;

        public ViewHolder(View view) {
            minePool = view.findViewById(R.id.minePool_name);
            icon =  view.findViewById(R.id.minePool_icon);
            layout_bg = view.findViewById(R.id.choose_layout_bg);
        }
    }
}
