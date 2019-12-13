package com.onets.wallet.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.onets.wallet.Fragment_Chongzhi.TextImageBean;
import com.onets.wallet.R;

import java.util.List;

/**
 * 安达证包功能适配器
 * Created by Hasee on 2018/1/14.
 */

public class AndaWalletFunctionAdapter extends RecyclerView.Adapter<AndaWalletFunctionAdapter.ViewHolder> {

    private MyItemClickListener mItemClickListener;
    private Context context;
    private LayoutInflater inflater;
    private List<TextImageBean> list;


    public AndaWalletFunctionAdapter(Context context, List<TextImageBean> list) {
        this.context = context.getApplicationContext();
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.anda_function_item, parent, false);
        ViewHolder holder = new ViewHolder(view,mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.function_name.setText(list.get(position).getName());
        holder.icon.setBackgroundResource(list.get(position).getIcon());
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        private MyItemClickListener mListener;
        private TextView function_name;
        private ImageView icon;

        public ViewHolder(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            //将全局的监听赋值给接口
            this.mListener = myItemClickListener;
            itemView.setOnClickListener(this);
            function_name = (TextView) itemView.findViewById(R.id.function_name);
            icon = (ImageView) itemView.findViewById(R.id.function_icon);

        }

        /**
         * 实现OnClickListener接口重写的方法
         * @param v
         */
        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getPosition());
            }

        }

    }


    /**
     * 创建一个回调接口
     */
    public interface MyItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * 在activity里面adapter就是调用的这个方法,将点击事件监听传递过来,并赋值给全局的监听
     *
     * @param myItemClickListener
     */
    public void setItemClickListener(MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }
}
