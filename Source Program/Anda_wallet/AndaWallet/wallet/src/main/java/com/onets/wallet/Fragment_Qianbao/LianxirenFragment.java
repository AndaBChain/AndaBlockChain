package com.onets.wallet.Fragment_Qianbao;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.onets.wallet.Contacts;
import com.onets.wallet.DbHelper_Contacts;
import com.onets.wallet.R;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/6/14.
 * 联系人Fragment
 */

public class LianxirenFragment extends Fragment implements View.OnTouchListener{
    ListView listView;
    Ladapter ladapter;
    List<Map<String,String>> list = new ArrayList<>();
    EditText et_lianxiren_name,et_lianxiren_email,et_lianxiren_coinaddr,et_lianxiren_flag;
    EditText et_lianxiren_name1,et_lianxiren_email1,et_lianxiren_coinaddr1,et_lianxiren_flag1;
    public static LianxirenFragment newInstance(){
        LianxirenFragment fragment=new LianxirenFragment();
        return fragment;
    }
    int screenWidth;
    int screenHeight;
    int lastX;
    int lastY;
    private long startTime = 0;
    private long endTime = 0;
    private boolean isclick;

    /**
     * 创建
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.qianbao_lianxiren,null);
        listView = (ListView) view.findViewById(R.id.lianxiren_listview);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels - 50;
        //FloatingActionButton button = (FloatingActionButton) view.findViewById(R.id.Mybutton);


        //final List<Map<String,String>> list = new ArrayList<>();

       // String dirPath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/databases/";
        String dirPath = "/data/data/com.onets.wallet/databases/";
        File dir=new File(dirPath);
        if(!dir.exists())
            dir.mkdirs();
        DbHelper_Contacts dbHelper_contacts = new DbHelper_Contacts(getContext());
        SQLiteDatabase db_contacts = dbHelper_contacts.getWritableDatabase();


        //SQLiteDatabase db_contacts = SQLiteDatabase.openOrCreateDatabase("/data/data/com.openwallet.wallet.dev/databases/Contacts.db",null);

        /*Cursor cursor = db_contacts.rawQuery("select * from Contacts",null);
        if(cursor.moveToFirst()){
            do{
//                String id = cursor.getString(cursor.getColumnIndex("id"));
                String name=cursor.getString(cursor.getColumnIndex("name"));
                String email=cursor.getString(cursor.getColumnIndex("email"));
//                String coin_addr=cursor.getString(cursor.getColumnIndex("coin_addr"));
                String phone = cursor.getString(cursor.getColumnIndex("phone"));

                Map<String,String> map = new HashMap<>();
                map.put("name",name);
                map.put("email",email);
                //map.put("coin_addr",coin_addr);
                map.put("phone",phone);
                list.add(map);

            }while(cursor.moveToNext());
        }
        db_contacts.close();*/


        for(int i=0;i<10;i++){
            Map<String,String> map = new HashMap<>();
            map.put("name","安达通证");
            map.put("邮箱","jnmian8888@163.com");
            map.put("电话","0065-90623576");
            //map.put("tag","标签"+i);
            list.add(map);

        }
        ladapter = new Ladapter(list,getActivity());
        listView.setAdapter(ladapter);
        /*button.setOnTouchListener(this);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Toast.makeText(getActivity(), "点击和拖动并存", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder buidler = new AlertDialog.Builder(getActivity());
                buidler.setIcon(R.mipmap.ic_launcher1);
                buidler.setTitle("添加联系人");
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_lianxiren_bianji,null);
                et_lianxiren_name1 = (EditText) view.findViewById(R.id.et_lianxiren_name);
                et_lianxiren_email1= (EditText) view.findViewById(R.id.et_lianxiren_email);
                et_lianxiren_coinaddr1= (EditText) view.findViewById(R.id.et_lianxiren_coinaddr);
                et_lianxiren_flag1= (EditText) view.findViewById(R.id.et_lianxiren_flag);

                buidler.setView(view);
                buidler.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getActivity(), "取消操作", Toast.LENGTH_SHORT).show();
                    }
                });
                buidler.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(et_lianxiren_name1.getText().toString().trim().equals("")){
                            Toast.makeText(getActivity(), "新建联系人名字不能为空", Toast.LENGTH_SHORT).show();
                        }else{
                            SQLiteDatabase db_contacts1 = SQLiteDatabase.openOrCreateDatabase("/data/data/com.onets.wallet/databases/Contacts.db",null);
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("name",et_lianxiren_name1.getText().toString());
                            contentValues.put("coin_addr",et_lianxiren_coinaddr1.getText().toString());
                            contentValues.put("email",et_lianxiren_email1.getText().toString());
                            contentValues.put("flag",et_lianxiren_flag1.getText().toString());
                            db_contacts1.insert("Contacts",null,contentValues);
                            db_contacts1.close();



                            Map<String,String> map = new HashMap<>();
                            map.put("name",et_lianxiren_name1.getText().toString());
                            map.put("email",et_lianxiren_email1.getText().toString());
                            map.put("coin_addr",et_lianxiren_coinaddr1.getText().toString());
                            map.put("flag",et_lianxiren_flag1.getText().toString());
                            list.add(map);
                        }
                        ladapter.notifyDataSetChanged();
                    }
                });
                AlertDialog dialog = buidler.create();
                dialog.show();
                dialog.getWindow().setLayout(800,800);



            }
        });*/
        return view;
    }

    /**
     * 触摸时
     * @param view
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) motionEvent.getRawX();
                lastY = (int) motionEvent.getRawY();
                isclick = false;//当按下的时候设置isclick为false，具体原因看后边的讲解
                startTime = System.currentTimeMillis();
                System.out.println("执行顺序down");
                break;
            /**
             * layout(l,t,r,b) l Left position, relative to parent t Top position,
             * relative to parent r Right position, relative to parent b Bottom
             * position, relative to parent
             * */
            case MotionEvent.ACTION_MOVE:
                System.out.println("执行顺序move");

                isclick = true;//当按钮被移动的时候设置isclick为true
                int dx = (int) motionEvent.getRawX() - lastX;
                int dy = (int) motionEvent.getRawY() - lastY;

                int left = view.getLeft() + dx;
                int top = view.getTop() + dy;
                int right = view.getRight() + dx;
                int bottom = view.getBottom() + dy;
                if (left < 0) {
                    left = 0;
                    right = left + view.getWidth();
                }
                if (right > screenWidth) {
                    right = screenWidth;
                    left = right - view.getWidth();
                }
                if (top < 0) {
                    top = 0;
                    bottom = top + view.getHeight();
                }
                if (bottom > screenHeight) {
                    bottom = screenHeight;
                    top = bottom - view.getHeight();
                }
                view.layout(left, top, right, bottom);
                lastX = (int) motionEvent.getRawX();
                lastY = (int) motionEvent.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                endTime = System.currentTimeMillis();
                //当从点击到弹起小于半秒的时候,则判断为点击,如果超过则不响应点击事件
                if ((endTime - startTime) > 0.1 * 1000L) {
                    isclick = true;
                } else {
                    isclick = false;
                }
                System.out.println("执行顺序up");

                break;
        }
        return isclick;
    }

    /**
     * Ladapter
     */
    class Ladapter extends BaseSwipeAdapter {
        List<Map<String,String>> list;
        Context context;

        public Ladapter(List<Map<String, String>> list, Context context) {
            this.list = list;
            this.context = context.getApplicationContext();
        }

        @Override
        public int getSwipeLayoutResourceId(int i) {
            return R.id.swipe_lianxiren;
        }

        @Override
        public View generateView(int i, ViewGroup viewGroup) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_lianxiren,null);
            //pos = i;
            final SwipeLayout swipeLayout = (SwipeLayout) v.findViewById(R.id.swipe_lianxiren);
            swipeLayout.addSwipeListener(new SimpleSwipeListener(){
                @Override
                public void onOpen(SwipeLayout layout) {
                   // YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.trash));
                    super.onOpen(layout);
                }
            });


            swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
                @Override
                public void onDoubleClick(SwipeLayout layout, boolean surface) {
                    //Toast.makeText(context, "双击显示详细信息", Toast.LENGTH_SHORT).show();

                }
            });
            return v;
        }

        /**
         * 填充Values
         * @param i
         * @param view
         */
        @Override
        public void fillValues(final int i, View view) {
            Map<String,String> map=list.get(i);

            TextView name = (TextView) view.findViewById(R.id.lianxiren_name);
            name.setText(map.get("name"));
            TextView post = (TextView) view.findViewById(R.id.lianxiren_post);
            post.setText(map.get("email"));
            TextView add = (TextView) view.findViewById(R.id.lianxiren_add);
            add.setText((String) map.get("coin_addr"));
            TextView tag = (TextView) view.findViewById(R.id.lianxiren_tag);
            tag.setText((String) map.get("flag"));

            final SwipeLayout sl = (SwipeLayout) view.findViewById(getSwipeLayoutResourceId(i));
            final Button button1 = (Button) view.findViewById(R.id.btn_lxr_bianji);
            button1.setTag(i);
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setIcon(R.mipmap.ic_launcher1);
                    builder.setTitle("修改联系人信息");
                    builder.setCancelable(false);
                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_lianxiren_bianji,null);
                    et_lianxiren_name = (EditText) view.findViewById(R.id.et_lianxiren_name);
                    et_lianxiren_email= (EditText) view.findViewById(R.id.et_lianxiren_email);
                    et_lianxiren_coinaddr= (EditText) view.findViewById(R.id.et_lianxiren_coinaddr);
                    et_lianxiren_flag= (EditText) view.findViewById(R.id.et_lianxiren_flag);

                    final int pos1 = (int) button1.getTag();
                    Map<String,String> map = list.get(pos1);
                    et_lianxiren_name.setHint(map.get("name"));
                    et_lianxiren_email.setHint(map.get("email"));
                    et_lianxiren_coinaddr.setHint(map.get("coin_addr"));
                    et_lianxiren_flag.setHint(map.get("flag"));

                    //ladapter.notifyDataSetChanged();

                    builder.setView(view);
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getActivity(), "取消操作", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (et_lianxiren_name.getText().toString().equals(null)) {
                                Toast.makeText(getActivity(), "联系人名不能为空！", Toast.LENGTH_SHORT).show();
                            } else {
//                                ContentValues value = new ContentValues();
//                                value.put("name",et_lianxiren_name.getText().toString());
//                                value.put("email",et_lianxiren_email.getText().toString());
//                                value.put("coin_addr",et_lianxiren_coinaddr.getText().toString());
//                                value.put("flag",et_lianxiren_flag.getText().toString());
//                                DataSupport.update(Contacts.class,value,pos1);


                                Map<String, String> map = new HashMap<>();
                                map.put("name", et_lianxiren_name.getText().toString());
                                map.put("email", et_lianxiren_coinaddr.getText().toString());
                                map.put("coin_addr", et_lianxiren_email.getText().toString());
                                map.put("flag", et_lianxiren_flag.getText().toString());
                                list.remove(pos1);
                                list.add(pos1,map);





                                DataSupport.deleteAll(Contacts.class);
                                for(Map<String,String> map1:list){
                                    Contacts con = new Contacts();
                                    con.setName(map1.get("name"));
                                    con.setEmail(map1.get("email"));
                                    con.setCoin_addr(map1.get("coin_addr"));
                                    con.setFlag(map1.get("flag"));
                                    con.save();

                                }

                                ladapter.notifyDataSetChanged();

                                Toast.makeText(getActivity(), "修改完成", Toast.LENGTH_SHORT).show();
                                                            }
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getWindow().setLayout(800,800);
//                    int pos = (int) button1.getTag();
//                    Map<String, String> obj = list.get(pos);
//                    list.remove(obj);
//                    sl.close();
                    //Toast.makeText(context, "编辑。。。", Toast.LENGTH_SHORT).show();
                    sl.close();
                }
            });
            final Button button2 = (Button) view.findViewById(R.id.btn_lxr_shanchu);
            button2.setTag(i);
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (int) button2.getTag();
                    Map<String,String> map = list.get(pos);
                    list.remove(map);
                    DataSupport.deleteAll(Contacts.class);
                    for(Map<String,String> map1:list){
                        Contacts con = new Contacts();
                        con.setName(map1.get("name"));
                        con.setEmail(map1.get("email"));
                        con.setCoin_addr(map1.get("coin_addr"));
                        con.setFlag(map1.get("flag"));
                        con.save();

                    }

                    ladapter.notifyDataSetChanged();

                    Toast.makeText(context, "删除成功！", Toast.LENGTH_SHORT).show();
                    sl.close();

                }
            });


        }

        /**
         * 获取Count
         * @return
         */
        @Override
        public int getCount() {
            return list.size();
        }

        /**
         * 获取选项
         * @param position
         * @return
         */
        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        /**
         * 获取选项ID
         * @param position
         * @return
         */
        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    /**
     * 联系人适配器
     */
    class lianxirenadapter extends BaseAdapter {
     List<Map<String,String>> list;
        Context context;

        public lianxirenadapter(List<Map<String, String>> list, Context context) {
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
            Map<String,String> map = list.get(position);
            ViewHolder holder;
            if(convertView==null){
                holder=new ViewHolder();
                convertView= LayoutInflater.from(getActivity()).inflate(R.layout.item_lianxiren,null);
                holder.name= (TextView) convertView.findViewById(R.id.lianxiren_name);
                holder.post= (TextView) convertView.findViewById(R.id.lianxiren_post);
                holder.add= (TextView) convertView.findViewById(R.id.lianxiren_add);
                holder.tag= (TextView) convertView.findViewById(R.id.lianxiren_tag);
                convertView.setTag(holder);
            }else{
                holder= (ViewHolder) convertView.getTag();
            }
            holder.name.setText(map.get("name"));
            holder.post.setText(map.get("email"));
            holder.add.setText(map.get("coin_addr"));
            holder.tag.setText(map.get("flag"));

            return convertView;
        }
    }
    class ViewHolder{
   TextView name,post,add,tag;
    }
//    void hideKeyBoard() {
//        View view = getActivity().getCurrentFocus();
//        if (view != null) {
//            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//        }
//    }
//    private void setupToolbar() {
//        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
//        toolbar.setTitle(getResources().getString(R.string.toolbar_name));
//        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
//        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arcbit_menu_white));
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                hideKeyBoard();
//                DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
//                if (drawer.isDrawerOpen(GravityCompat.START)) {
//                    drawer.closeDrawer(GravityCompat.START);
//                } else {
//                    drawer.openDrawer(GravityCompat.START);
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        setupToolbar();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        setupToolbar();
//    }
}

