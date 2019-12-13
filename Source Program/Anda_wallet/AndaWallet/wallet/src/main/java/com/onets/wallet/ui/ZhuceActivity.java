package com.onets.wallet.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.onets.wallet.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 注册界面
 */
public class ZhuceActivity extends AppCompatActivity {
    Button btn_zhuce1;
    EditText et_zhuce_name,et_zhuce_email,et_zhuce_password,et_zhuce_password1,
             et_zhuce_phone;
    int count;
    String name;
    String password;
    ViewPager vv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhuce);

        btn_zhuce1= (Button) findViewById(R.id.btn_zhuce1);
        et_zhuce_name= (EditText) findViewById(R.id.et_zhuce_name);
        et_zhuce_password= (EditText) findViewById(R.id.et_zhuce_password);
        et_zhuce_password1= (EditText) findViewById(R.id.et_zhuce_password1);
        et_zhuce_phone= (EditText) findViewById(R.id.et_zhuce_phone);
        et_zhuce_email= (EditText) findViewById(R.id.et_zhuce_email);
        vv= (ViewPager) findViewById(R.id.v_v);


        btn_zhuce1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count=1;

                SQLiteDatabase db_user = SQLiteDatabase.openOrCreateDatabase("/data/data/"+getPackageName()+"/databases/User.db",null);
                Cursor cursor = db_user.rawQuery("select * from User",null);
                cursor.moveToFirst();
                    do{
                        String id = cursor.getString(cursor.getColumnIndex("id"));
                        name = cursor.getString(cursor.getColumnIndex("name"));
                        password=cursor.getString(cursor.getColumnIndex("password"));
                        if(name.equals(et_zhuce_name.getText().toString())){
                                count++;
                                break;
                        }

                    }while(cursor.moveToNext());


                if(count==2){
                    Toast.makeText(ZhuceActivity.this, "该用户名已经注册，请更换用户名", Toast.LENGTH_SHORT).show();
                }else if(et_zhuce_name.getText().toString().trim().equals("")){
                    Toast.makeText(ZhuceActivity.this, "用户名不能为空！", Toast.LENGTH_SHORT).show();
                }else{
                    if(!et_zhuce_password.getText().toString().equals(et_zhuce_password1.getText().toString())){
                        Toast.makeText(ZhuceActivity.this, "两次输入的密码不一致！", Toast.LENGTH_SHORT).show();
                    }else{
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("name",et_zhuce_name.getText().toString().trim());
                        contentValues.put("password",et_zhuce_password.getText().toString().trim());
                        contentValues.put("phone",et_zhuce_phone.getText().toString().trim());
                        contentValues.put("email",et_zhuce_email.getText().toString().trim());
                        db_user.insert("User",null,contentValues);

                        db_user.close();

                        Toast.makeText(ZhuceActivity.this, "注册成功，正在跳转到首页。。。", Toast.LENGTH_SHORT).show();
                        final Intent intent = new Intent(ZhuceActivity.this,First_activity.class);
                        TimerTask task = new TimerTask(){
                            public void run(){
                                //execute the task
                                startActivity(intent);
                                finish();
                            }
                        };
                        Timer timer = new Timer();
                        timer.schedule(task, 1000);

                    }
                }



            }
        });
    }
}
