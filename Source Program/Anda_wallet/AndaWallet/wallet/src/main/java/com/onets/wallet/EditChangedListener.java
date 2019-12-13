package com.onets.wallet;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import static com.onets.wallet.BuildConfig.DEBUG;

/**
 * @author Yu K.Q.
 * Created by Administrator on 2017/11/14.
 */

public class EditChangedListener implements TextWatcher {

    private static final String TAG = "EditChangedListener";
    private CharSequence temp;//监听前的文本
    private int editStart;//光标开始位置
    private int editEnd;//光标结束位置
    private final int charMaxNum = 20;
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (DEBUG)
            Log.i(TAG, "输入文本之前的状态");
        temp = charSequence;
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (DEBUG)
            Log.i(TAG, "输入文字中的状态，count是一次性输入字符数");
//        mTvAvailableCharNum.setText("还能输入" + (charMaxNum - charSequence.length()) + "字符");
    }

    @Override
    public void afterTextChanged(Editable s) {
//        if (DEBUG)
//            Log.i(TAG, "输入文字后的状态");
//        /** 得到光标开始和结束位置 ,超过最大数后记录刚超出的数字索引进行控制 */
//        editStart = mEditTextMsg.getSelectionStart();
//        editEnd = mEditTextMsg.getSelectionEnd();
//        if (temp.length() > charMaxNum) {
//            Toast.makeText(getApplicationContext(), "你输入的字数已经超过了限制！", Toast.LENGTH_LONG).show();
//            s.delete(editStart - 1, editEnd);
//            int tempSelection = editStart;
//            mEditTextMsg.setText(s);
//            mEditTextMsg.setSelection(tempSelection);
//        }

    }
}

