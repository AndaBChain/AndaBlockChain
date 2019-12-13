package com.onets.wallet.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.onets.wallet.Constants;
import com.onets.wallet.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 开始创建钱包,包括权限申请
 */
public class WelcomeFragment extends Fragment {
    private static final Logger log = LoggerFactory.getLogger(WelcomeFragment.class);
    private static final String TAG = "WelcomeFragment";

    private Listener listener;

    public WelcomeFragment() { }

    /**
     * 创建
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 创建视图
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        //创建新证包
        view.findViewById(R.id.create_wallet).setOnClickListener(getOnCreateListener());
        //恢复证包
        view.findViewById(R.id.restore_wallet).setOnClickListener(getOnRestoreListener());

        //权限配置部分
        requestPermissions();

        return view;
    }

    /*创建新证包监听事件*/
    private View.OnClickListener getOnCreateListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log.info("Clicked create new wallet");
                Log.d(TAG, Constants.LOG_LABLE + "onClick: getOnCreateListener()");
                if (listener != null) {
                    listener.onCreateNewWallet();
                }
            }
        };
    }

    /*恢复证包监听事件*/
    private View.OnClickListener getOnRestoreListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log.info("Clicked restore new wallet");
                if (listener != null) {
                    listener.onRestoreWallet();
                }
            }
        };
    }

    /**
     * 触摸操作
     * @param context
     */
    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        try {
            listener = (Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + Listener.class);
        }
    }

    /**
     * 当有多个权限需要申请的时候
     * 这里以打电话和SD卡读写权限为例
     */

    private void requestPermissions() {

        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.VIBRATE);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WAKE_LOCK);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.RECEIVE_BOOT_COMPLETED);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.BLUETOOTH);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.INTERNET);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.NFC);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "requestPermissions: ---------------------------------youwoCall");
            permissionList.add(Manifest.permission.READ_PHONE_NUMBERS);
        }
        if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "requestPermissions: ---------------------------------youwoRead");
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "requestPermissions: ---------------------------------youwo");
            permissionList.add(Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS);
        }
        if (!permissionList.isEmpty()){  //申请的集合不为空时，表示有需要申请的权限
            ActivityCompat.requestPermissions(getActivity(),permissionList.toArray(new String[permissionList.size()]),1);
        }else { //所有的权限都已经授权过了

        }
    }
    /**
     * 权限申请返回结果
     * @param requestCode 请求码
     * @param permissions 权限数组
     * @param grantResults  申请结果数组，里面都是int类型的数
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0){ //安全写法，如果小于0，肯定会出错了
                    for (int i = 0; i < grantResults.length; i++) {

                        int grantResult = grantResults[i];
                        if (grantResult == PackageManager.PERMISSION_DENIED){ //这个是权限拒绝
                            String s = permissions[i];
                            Toast.makeText(getActivity(),s+"权限被拒绝了",Toast.LENGTH_SHORT).show();
                        }else{ //授权成功了
                            //do Something
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 失去触摸
     */
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * 监听接口
     */
    public interface Listener {
        void onCreateNewWallet();
        void onRestoreWallet();
        void onSeedCreated(String seed);
        void onSeedVerified(Bundle args);
    }
}
